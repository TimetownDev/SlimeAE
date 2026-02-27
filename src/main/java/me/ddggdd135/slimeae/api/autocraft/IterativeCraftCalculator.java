package me.ddggdd135.slimeae.api.autocraft;

import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.slimeae.api.exceptions.NoEnoughMaterialsException;
import me.ddggdd135.slimeae.api.items.ItemRequest;
import me.ddggdd135.slimeae.api.items.ItemStorage;
import me.ddggdd135.slimeae.core.NetworkInfo;
import org.bukkit.inventory.ItemStack;

public class IterativeCraftCalculator {
    private static final long TIME_BUDGET_NS = 5_000_000L;

    public enum State {
        RUNNING,
        COMPLETED,
        FAILED
    }

    private final NetworkInfo info;
    private final CraftingRecipe rootRecipe;
    private final long rootCount;
    private final ItemStorage storage;

    private final Deque<CalcFrame> stack = new ArrayDeque<>();
    private final Set<CraftingRecipe> craftingPath = new HashSet<>();
    private final Map<CraftingRecipe, CalcResult> resultMap = new LinkedHashMap<>();
    private final Map<CraftingRecipe, Set<CraftingRecipe>> dependencyMap = new LinkedHashMap<>();

    private State state = State.RUNNING;
    private NoEnoughMaterialsException failureException;
    private List<CraftStep> finalSteps;

    public IterativeCraftCalculator(
            @Nonnull NetworkInfo info, @Nonnull CraftingRecipe recipe, long count, @Nonnull ItemStorage storage) {
        this.info = info;
        this.rootRecipe = recipe;
        this.rootCount = count;
        this.storage = storage;

        pushFrame(recipe, count, null);
    }

    public State getState() {
        return state;
    }

    @Nullable public NoEnoughMaterialsException getFailureException() {
        return failureException;
    }

    @Nonnull
    public List<CraftStep> getResult() {
        if (finalSteps == null) {
            throw new IllegalStateException("Calculation not completed");
        }
        return finalSteps;
    }

    public void processAll() {
        while (state == State.RUNNING) {
            processSlice(Long.MAX_VALUE);
        }
    }

    public boolean processSlice() {
        return processSlice(TIME_BUDGET_NS);
    }

    public boolean processSlice(long budgetNs) {
        if (state != State.RUNNING) return true;

        long deadline = System.nanoTime() + budgetNs;

        while (!stack.isEmpty()) {
            CalcFrame frame = stack.peek();

            try {
                processFrame(frame);
            } catch (NoEnoughMaterialsException e) {
                state = State.FAILED;
                failureException = e;
                return true;
            } catch (IllegalStateException e) {
                state = State.FAILED;
                failureException = null;
                return true;
            }

            if (System.nanoTime() > deadline) {
                return false;
            }
        }

        buildFinalSteps();
        state = State.COMPLETED;
        return true;
    }

    private void processFrame(CalcFrame frame) {
        if (frame.phase == CalcFrame.Phase.INIT) {
            if (!craftingPath.add(frame.recipe)) {
                throw new IllegalStateException("Circular dependency detected");
            }

            if (!info.getRecipes().contains(frame.recipe)) {
                craftingPath.remove(frame.recipe);
                ItemStorage missing = new ItemStorage();
                ItemHashMap<Long> in = frame.recipe.getInputAmounts();
                for (ItemKey key : in.sourceKeySet()) {
                    long amount = storage.getStorageUnsafe().getOrDefault(key, 0L);
                    long need = in.getKey(key) * frame.count;
                    if (amount < need) {
                        missing.addItem(key, need - amount);
                    }
                }
                if (missing.getStorageUnsafe().isEmpty()) {
                    for (ItemKey key : in.sourceKeySet()) {
                        missing.addItem(key, in.getKey(key) * frame.count);
                    }
                }
                stack.pop();
                throw new NoEnoughMaterialsException(missing.getStorageUnsafe());
            }

            frame.inputKeys = new ArrayList<>(frame.recipe.getInputAmounts().sourceKeySet());
            frame.keyIndex = 0;
            frame.missing = new ItemStorage();
            frame.phase = CalcFrame.Phase.PROCESS_INPUTS;
        }

        if (frame.phase == CalcFrame.Phase.PROCESS_INPUTS) {
            processInputs(frame);
        }
    }

    private void processInputs(CalcFrame frame) {
        ItemHashMap<Long> in = frame.recipe.getInputAmounts();

        while (frame.keyIndex < frame.inputKeys.size()) {
            if (frame.waitingForChild) {
                frame.waitingForChild = false;
                CraftingRecipe childRecipe = frame.pendingChildRecipe;
                long countToCraft = frame.pendingCountToCraft;
                ItemKey key = frame.inputKeys.get(frame.keyIndex);
                long remainingNeed = frame.pendingRemainingNeed;

                if (frame.childFailed) {
                    frame.childFailed = false;
                    if (frame.childException != null) {
                        for (Map.Entry<ItemStack, Long> entry :
                                frame.childException.getMissingMaterials().entrySet()) {
                            frame.missing.addItem(new ItemKey(entry.getKey()), entry.getValue());
                        }
                        frame.childException = null;
                    }
                } else {
                    ItemHashMap<Long> output = childRecipe.getOutputAmounts();
                    for (Map.Entry<ItemKey, Long> o : output.keyEntrySet()) {
                        storage.addItem(o.getKey(), o.getValue() * countToCraft);
                    }
                    storage.takeItem(new ItemRequest(key, remainingNeed));

                    mergeResult(childRecipe, countToCraft);
                    addDependency(frame.recipe, childRecipe);
                }

                frame.keyIndex++;
                continue;
            }

            ItemKey key = frame.inputKeys.get(frame.keyIndex);
            long amount = storage.getStorageUnsafe().getOrDefault(key, 0L);
            long need = in.getKey(key) * frame.count;

            if (amount >= need) {
                storage.takeItem(new ItemRequest(key, need));
                frame.keyIndex++;
            } else {
                long remainingNeed = need - amount;
                if (amount > 0) {
                    storage.takeItem(new ItemRequest(key, amount));
                }

                CraftingRecipe childRecipe = info.getRecipeFor(key.getItemStack());
                if (childRecipe == null) {
                    frame.missing.addItem(new ItemKey(key.getItemStack()), remainingNeed);
                    frame.keyIndex++;
                    continue;
                }

                ItemHashMap<Long> output = childRecipe.getOutputAmounts();
                ItemHashMap<Long> input = childRecipe.getInputAmounts();

                long out = output.getKey(key) - input.getOrDefault(key, 0L);
                long countToCraft = (long) Math.ceil(remainingNeed / (double) out);

                frame.waitingForChild = true;
                frame.pendingChildRecipe = childRecipe;
                frame.pendingCountToCraft = countToCraft;
                frame.pendingRemainingNeed = remainingNeed;

                pushFrame(childRecipe, countToCraft, frame);
                return;
            }
        }

        if (!frame.missing.getStorageUnsafe().isEmpty()) {
            craftingPath.remove(frame.recipe);
            stack.pop();
            NoEnoughMaterialsException ex = new NoEnoughMaterialsException(frame.missing.getStorageUnsafe());

            if (frame.parent != null) {
                frame.parent.childFailed = true;
                frame.parent.childException = ex;
            } else {
                throw ex;
            }
            return;
        }

        mergeResult(frame.recipe, frame.count);
        craftingPath.remove(frame.recipe);
        stack.pop();
    }

    private void pushFrame(CraftingRecipe recipe, long count, @Nullable CalcFrame parent) {
        CalcFrame frame = new CalcFrame();
        frame.recipe = recipe;
        frame.count = count;
        frame.parent = parent;
        frame.phase = CalcFrame.Phase.INIT;
        stack.push(frame);
    }

    private void mergeResult(CraftingRecipe recipe, long count) {
        CalcResult existing = resultMap.get(recipe);
        if (existing != null) {
            existing.count += count;
        } else {
            resultMap.put(recipe, new CalcResult(recipe, count));
        }
    }

    private void addDependency(CraftingRecipe parent, CraftingRecipe child) {
        dependencyMap.computeIfAbsent(parent, k -> new LinkedHashSet<>()).add(child);
    }

    private void buildFinalSteps() {
        finalSteps = new ArrayList<>();
        Set<CraftingRecipe> visited = new HashSet<>();
        Set<CraftingRecipe> inStack = new HashSet<>();

        for (CraftingRecipe recipe : resultMap.keySet()) {
            if (!visited.contains(recipe)) {
                topoSort(recipe, visited, inStack);
            }
        }
    }

    private void topoSort(CraftingRecipe recipe, Set<CraftingRecipe> visited, Set<CraftingRecipe> inStack) {
        if (inStack.contains(recipe)) return;
        if (visited.contains(recipe)) return;
        inStack.add(recipe);

        Set<CraftingRecipe> deps = dependencyMap.get(recipe);
        if (deps != null) {
            for (CraftingRecipe dep : deps) {
                topoSort(dep, visited, inStack);
            }
        }

        visited.add(recipe);
        inStack.remove(recipe);
        CalcResult result = resultMap.get(recipe);
        if (result != null) {
            finalSteps.add(new CraftStep(result.recipe, result.count));
        }
    }

    private static class CalcFrame {
        enum Phase {
            INIT,
            PROCESS_INPUTS
        }

        CraftingRecipe recipe;
        long count;
        CalcFrame parent;
        Phase phase;

        List<ItemKey> inputKeys;
        int keyIndex;
        ItemStorage missing;

        boolean waitingForChild;
        CraftingRecipe pendingChildRecipe;
        long pendingCountToCraft;
        long pendingRemainingNeed;
        boolean childFailed;
        NoEnoughMaterialsException childException;
    }

    private static class CalcResult {
        final CraftingRecipe recipe;
        long count;

        CalcResult(CraftingRecipe recipe, long count) {
            this.recipe = recipe;
            this.count = count;
        }
    }
}
