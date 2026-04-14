package me.ddggdd135.slimeae.api.autocraft;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.api.AEMenu;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.items.AdvancedCustomItemStack;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBT;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.events.AutoCraftingTaskDisposingEvent;
import me.ddggdd135.slimeae.api.events.AutoCraftingTaskStartingEvent;
import me.ddggdd135.slimeae.api.exceptions.NoEnoughMaterialsException;
import me.ddggdd135.slimeae.api.interfaces.*;
import me.ddggdd135.slimeae.api.items.ItemRequest;
import me.ddggdd135.slimeae.api.items.ItemStorage;
import me.ddggdd135.slimeae.api.items.StorageCollection;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.utils.ItemUtils;
import net.Zrips.CMILib.Items.CMIMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AutoCraftingTask implements IDisposable {
    private static final int MAX_LORE_LINES = 15;
    private static final String MORE_ITEMS_INDICATOR = "&7... 还有%d项未显示";

    public static final String CRAFTING_KEY = "auto_crafting";
    private final UUID taskId;
    private final CraftingRecipe recipe;
    private final NetworkInfo info;
    private final long count;
    private final List<CraftStep> craftingSteps;
    private final Map<CraftStep, Set<CraftStep>> stepDependencies;
    private final Set<CraftStep> completedSteps = new HashSet<>();
    private int globalFailTimes;
    private int cancelFailTimes;
    private volatile List<CraftStep> lastActiveSteps = Collections.emptyList();
    private final AEMenu menu;
    private boolean isCancelling = false;
    private final Set<CraftingRecipe> craftingPath = new HashSet<>();
    private ItemStorage storage;
    private volatile TaskState taskState = TaskState.RUNNING;
    private final long createdAt;

    public AutoCraftingTask(@Nonnull NetworkInfo info, @Nonnull CraftingRecipe recipe, long count) {
        this.taskId = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
        this.info = info;
        this.recipe = recipe;
        this.count = count;

        List<CraftStep> newSteps = null;
        IterativeCraftCalculator successCalculator = null;

        List<CraftStep> usingSteps;
        // 强制使存储缓存失效，确保获取最新数据快照
        // （防止 dispose 归还物品后 F3 缓存仍保存旧数据）
        info.getStorage().invalidateStorageCache();
        info.getStorage().clearNotIncluded();

        ItemHashMap<Long> baseSnapshot = info.getStorage().getStorageUnsafe();

        // === 调试日志 ===
        java.util.logging.Logger debugLog = SlimeAEPlugin.getInstance().getLogger();
        if (SlimeAEPlugin.isDebug()) {
            debugLog.info("[AutoCraft-Debug] === 开始创建合成任务 ===");
            debugLog.info("[AutoCraft-Debug] 配方输出: " + ItemUtils.getItemName(recipe.getOutput()[0]) + " x" + count);
            debugLog.info(
                    "[AutoCraft-Debug] recipe在getRecipes中: " + info.getRecipes().contains(recipe));
            debugLog.info("[AutoCraft-Debug] getRecipes大小: " + info.getRecipes().size());
            for (Map.Entry<ItemKey, Long> de : baseSnapshot.keyEntrySet()) {
                if (de.getValue() > 0) {
                    debugLog.info("[AutoCraft-Debug] 存储内容: "
                            + ItemUtils.getItemName(de.getKey().getItemStack()) + " = " + de.getValue());
                }
            }
        }
        // === 调试日志结束 ===

        try {
            ItemStorage calcStorage = new ItemStorage(baseSnapshot);
            IterativeCraftCalculator calculator = new IterativeCraftCalculator(info, recipe, count, calcStorage);
            calculator.processAll();
            if (calculator.getState() == IterativeCraftCalculator.State.COMPLETED) {
                newSteps = calculator.getResult();
                if (!checkCraftStepsValid(newSteps, new ItemStorage(baseSnapshot)))
                    throw new IllegalStateException("新版算法出错，退回旧版算法");
                usingSteps = newSteps;
                successCalculator = calculator;
            } else if (calculator.getFailureException() != null) {
                throw calculator.getFailureException();
            } else {
                throw new IllegalStateException("新版算法出错，退回旧版算法");
            }
        } catch (Exception ignored) {
            // 新版算法出错，退回旧版算法
            if (SlimeAEPlugin.isDebug()) {
                debugLog.info("[AutoCraft-Debug] 新版算法异常: " + ignored.getClass().getSimpleName() + " - "
                        + ignored.getMessage());
                if (ignored instanceof NoEnoughMaterialsException nee) {
                    debugLog.info("[AutoCraft-Debug] 新版算法缺失材料:");
                    for (Map.Entry<ItemStack, Long> me :
                            nee.getMissingMaterials().entrySet()) {
                        debugLog.info(
                                "[AutoCraft-Debug]   " + ItemUtils.getItemName(me.getKey()) + " x " + me.getValue());
                    }
                }
                debugLog.info("[AutoCraft-Debug] 回退到旧版算法 match()");
            }
            try {
                usingSteps = match(recipe, count, new ItemStorage(baseSnapshot));
                if (SlimeAEPlugin.isDebug()) {
                    debugLog.info("[AutoCraft-Debug] 旧版算法成功，步骤数: " + usingSteps.size());
                }
            } catch (NoEnoughMaterialsException matchEx) {
                if (SlimeAEPlugin.isDebug()) {
                    debugLog.info("[AutoCraft-Debug] 旧版算法也失败了! 缺失材料:");
                    for (Map.Entry<ItemStack, Long> me :
                            matchEx.getMissingMaterials().entrySet()) {
                        debugLog.info(
                                "[AutoCraft-Debug]   " + ItemUtils.getItemName(me.getKey()) + " x " + me.getValue());
                    }
                }
                throw matchEx;
            }
        }

        craftingSteps = usingSteps;

        if (successCalculator != null) {
            Map<CraftingRecipe, Set<CraftingRecipe>> recipeDeps = successCalculator.getDependencyMap();
            Map<CraftingRecipe, CraftStep> recipeToStep = new HashMap<>();
            for (CraftStep step : craftingSteps) {
                recipeToStep.put(step.getRecipe(), step);
            }
            Map<CraftStep, Set<CraftStep>> deps = new HashMap<>();
            for (CraftStep step : craftingSteps) {
                Set<CraftingRecipe> recipeDep = recipeDeps.get(step.getRecipe());
                if (recipeDep != null) {
                    Set<CraftStep> stepDep = new HashSet<>();
                    for (CraftingRecipe r : recipeDep) {
                        CraftStep depStep = recipeToStep.get(r);
                        if (depStep != null) {
                            stepDep.add(depStep);
                        }
                    }
                    if (!stepDep.isEmpty()) {
                        deps.put(step, stepDep);
                    }
                }
            }
            this.stepDependencies = deps;
        } else {
            this.stepDependencies = Collections.emptyMap();
        }

        this.storage = new ItemStorage();

        // 所有材料都能拿到
        for (CraftStep step : craftingSteps) {
            for (Map.Entry<ItemKey, Long> entry :
                    step.getRecipe().getInputAmounts().keyEntrySet()) {
                storage.addItem(entry.getKey(), entry.getValue() * step.getAmount());
            }
        }

        ItemHashMap<Long> requested = storage.copyStorage();
        storage = info.getStorage().takeItem(ItemUtils.createRequests(requested));

        ItemHashMap<Long> actual = storage.getStorageUnsafe();
        ItemStorage missingStorage = new ItemStorage();
        for (Map.Entry<ItemKey, Long> entry : requested.keyEntrySet()) {
            long need = entry.getValue();
            long got = actual.getOrDefault(entry.getKey(), 0L);
            if (got < need) {
                missingStorage.addItem(entry.getKey(), need - got);
            }
        }
        if (!missingStorage.getStorageUnsafe().isEmpty()) {
            ItemHashMap<Long> toReturn = new ItemHashMap<>(actual);
            info.getStorage().pushItem(toReturn);
            ItemUtils.trim(toReturn);
            if (!toReturn.isEmpty()) {
                synchronized (info) {
                    info.getTempStorage().addItem(toReturn, true);
                }
            }
            storage = new ItemStorage();
            throw new NoEnoughMaterialsException(missingStorage.getStorageUnsafe());
        }

        String TitleInfo = craftingSteps == newSteps ? "&2(新版算法)" : "&7(旧版算法)";
        menu = new AEMenu("&e合成任务" + TitleInfo);
        menu.setSize(54);
        menu.addMenuCloseHandler(player -> dispose());
    }

    private AutoCraftingTask(
            UUID taskId,
            NetworkInfo info,
            CraftingRecipe recipe,
            long count,
            List<CraftStep> steps,
            Map<CraftStep, Set<CraftStep>> deps,
            Set<CraftStep> completed,
            ItemStorage storage,
            int globalFailTimes,
            int cancelFailTimes,
            boolean isCancelling,
            long createdAt) {
        this.taskId = taskId;
        this.info = info;
        this.recipe = recipe;
        this.count = count;
        this.craftingSteps = steps;
        this.stepDependencies = deps;
        this.completedSteps.addAll(completed);
        this.storage = storage;
        this.globalFailTimes = globalFailTimes;
        this.cancelFailTimes = cancelFailTimes;
        this.isCancelling = isCancelling;
        this.createdAt = createdAt;
        this.menu = new AEMenu("&e合成任务 &d(恢复)");
        this.menu.setSize(54);
        this.menu.addMenuCloseHandler(player -> dispose());
    }

    public static AutoCraftingTask restore(
            UUID taskId,
            NetworkInfo info,
            CraftingRecipe recipe,
            long count,
            List<CraftStep> steps,
            Map<CraftStep, Set<CraftStep>> deps,
            Set<CraftStep> completed,
            ItemStorage storage,
            int globalFailTimes,
            int cancelFailTimes,
            boolean isCancelling,
            long createdAt) {
        return new AutoCraftingTask(
                taskId,
                info,
                recipe,
                count,
                steps,
                deps,
                completed,
                storage,
                globalFailTimes,
                cancelFailTimes,
                isCancelling,
                createdAt);
    }

    public synchronized void suspend() {
        if (taskState != TaskState.RUNNING && taskState != TaskState.CANCELLING) return;
        taskState = TaskState.SUSPENDED;

        for (CraftStep step : craftingSteps) {
            if (step.getVirtualRunning() > 0) {
                ItemHashMap<Long> refund = new ItemHashMap<>();
                for (Map.Entry<ItemKey, Long> entry :
                        step.getRecipe().getInputAmounts().keyEntrySet()) {
                    refund.putKey(entry.getKey(), entry.getValue() * step.getVirtualRunning());
                }
                storage.addItem(refund);
                step.setAmount(step.getAmount() + step.getVirtualRunning());
                step.setVirtualRunning(0);
                step.setVirtualProcess(0);
            }

            if (step.getRecipe().getCraftType().isProcess()) {
                Set<Location> toRemove = new HashSet<>();
                for (Location deviceLoc : step.getRunningDevices()) {
                    Block deviceBlock = deviceLoc.getBlock();
                    var blockData = StorageCacheUtils.getBlock(deviceLoc);
                    if (blockData == null) {
                        ItemHashMap<Long> refund = new ItemHashMap<>();
                        for (Map.Entry<ItemKey, Long> entry :
                                step.getRecipe().getInputAmounts().keyEntrySet()) {
                            refund.putKey(entry.getKey(), entry.getValue());
                        }
                        storage.addItem(refund);
                        toRemove.add(deviceLoc);
                        continue;
                    }
                    SlimefunItem sfItem = SlimefunItem.getById(blockData.getSfId());
                    if (!(sfItem instanceof IMERealCraftDevice device)) {
                        ItemHashMap<Long> refund = new ItemHashMap<>();
                        for (Map.Entry<ItemKey, Long> entry :
                                step.getRecipe().getInputAmounts().keyEntrySet()) {
                            refund.putKey(entry.getKey(), entry.getValue());
                        }
                        storage.addItem(refund);
                        toRemove.add(deviceLoc);
                        continue;
                    }
                    if (device.isFinished(deviceBlock)) {
                        CraftingRecipe finished = device.getFinishedCraftingRecipe(deviceBlock);
                        if (finished != null && finished.equals(step.getRecipe())) {
                            device.finishCrafting(deviceBlock);
                            storage.addItem(step.getRecipe().getOutput());
                            toRemove.add(deviceLoc);
                        }
                    }
                }
                for (Location loc : toRemove) {
                    step.removeRunningDevice(loc);
                }
            }
        }

        if (SlimeAEPlugin.getCraftTaskPersistence() != null) {
            SlimeAEPlugin.getCraftTaskPersistence().save(this);
        }

        info.getAutoCraftingSessions().remove(this);
    }

    @Nonnull
    public CraftingRecipe getRecipe() {
        return recipe;
    }

    @Nonnull
    public NetworkInfo getNetworkInfo() {
        return info;
    }

    public long getCount() {
        return count;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public TaskState getTaskState() {
        return taskState;
    }

    public Map<CraftStep, Set<CraftStep>> getStepDependencies() {
        return stepDependencies;
    }

    public Set<CraftStep> getCompletedSteps() {
        return completedSteps;
    }

    public ItemStorage getStorage() {
        return storage;
    }

    public int getGlobalFailTimes() {
        return globalFailTimes;
    }

    public int getCancelFailTimes() {
        return cancelFailTimes;
    }

    public boolean isCancelling() {
        return isCancelling;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    @Nonnull
    public List<CraftStep> getCraftingSteps() {
        return craftingSteps;
    }

    private boolean checkCraftStepsValid(List<CraftStep> steps, ItemStorage storage) {
        for (CraftStep step : steps) {
            for (Map.Entry<ItemKey, Long> input :
                    step.getRecipe().getInputAmounts().keyEntrySet()) {
                long amount = input.getValue() * step.getAmount();
                if (amount > storage.getStorageUnsafe().getOrDefault(input.getKey(), 0L)) return false;
                storage.takeItem(new ItemRequest(input.getKey(), amount));
            }
            for (Map.Entry<ItemKey, Long> output :
                    step.getRecipe().getOutputAmounts().keyEntrySet()) {
                storage.addItem(output.getKey(), output.getValue() * step.getAmount());
            }
        }
        return true;
    }

    private List<CraftStep> match(CraftingRecipe recipe, long count, ItemStorage storage) {
        if (!craftingPath.add(recipe)) {
            throw new IllegalStateException("检测到循环依赖的合成配方");
        }

        try {
            if (!info.getRecipes().contains(recipe)) {
                // 配方不可用，记录所有所需材料作为缺失
                ItemStorage missing = new ItemStorage();
                ItemHashMap<Long> in = recipe.getInputAmounts();
                for (ItemKey key : in.sourceKeySet()) {
                    long amount = storage.getStorageUnsafe().getOrDefault(key, 0L);
                    long need = in.getKey(key) * count;
                    if (amount < need) {
                        missing.addItem(key, need - amount);
                    }
                }
                // 如果所有材料够用但配方不可用，仍然报告所有输入为缺失
                if (missing.getStorageUnsafe().isEmpty()) {
                    for (ItemKey key : in.sourceKeySet()) {
                        missing.addItem(key, in.getKey(key) * count);
                    }
                }
                throw new NoEnoughMaterialsException(missing.getStorageUnsafe());
            }

            List<CraftStep> result = new ArrayList<>();
            ItemStorage missing = new ItemStorage();
            ItemHashMap<Long> in = recipe.getInputAmounts();

            // 遍历所需材料
            for (ItemKey key : in.sourceKeySet()) {
                long amount = storage.getStorageUnsafe().getOrDefault(key, 0L);
                long need = in.getKey(key) * count;

                if (amount >= need) {
                    storage.takeItem(new ItemRequest(key, need));
                } else {
                    long remainingNeed = need - amount;
                    if (amount > 0) {
                        storage.takeItem(new ItemRequest(key, amount));
                    }

                    // 尝试合成缺少的材料
                    CraftingRecipe craftingRecipe = getRecipe(key.getItemStack());
                    if (craftingRecipe == null) {
                        missing.addItem(new ItemKey(key.getItemStack()), remainingNeed);
                        continue;
                    }

                    ItemHashMap<Long> output = craftingRecipe.getOutputAmounts();
                    ItemHashMap<Long> input = craftingRecipe.getInputAmounts();

                    // 计算需要合成多少次
                    long out = output.getKey(key) - input.getOrDefault(key, 0L);
                    long countToCraft = (long) Math.ceil(remainingNeed / (double) out);

                    try {
                        result.addAll(match(craftingRecipe, countToCraft, storage));
                        for (Map.Entry<ItemKey, Long> o : output.keyEntrySet()) {
                            storage.addItem(o.getKey(), o.getValue() * countToCraft);
                        }
                        storage.takeItem(new ItemRequest(key, remainingNeed));
                    } catch (NoEnoughMaterialsException e) {
                        // 合并子合成缺少的材料
                        for (Map.Entry<ItemStack, Long> entry :
                                e.getMissingMaterials().entrySet()) {
                            missing.addItem(new ItemKey(entry.getKey()), entry.getValue());
                        }
                    }
                }
            }

            // 如果有缺少的材料就抛出异常
            if (!missing.getStorageUnsafe().isEmpty()) {
                throw new NoEnoughMaterialsException(missing.getStorageUnsafe());
            }

            result.add(new CraftStep(recipe, count));
            return result;
        } finally {
            // 无论是否成功,都要从路径中移除当前配方
            craftingPath.remove(recipe);
        }
    }

    @Nullable private CraftingRecipe getRecipe(@Nonnull ItemStack itemStack) {
        return info.getRecipeFor(itemStack);
    }

    public synchronized boolean hasNext() {
        if (craftingSteps.isEmpty()) return false;
        for (CraftStep step : craftingSteps) {
            if (!step.isCompleted()) return true;
        }
        return false;
    }

    @Nonnull
    public List<CraftStep> getActiveSteps() {
        return lastActiveSteps;
    }

    public synchronized void moveNext(int maxDevices) {
        if (!hasNext()) return;

        int parallelism = 1;
        if (NetworkInfo.isParallelEnabled()) {
            parallelism = info.getParallelProcessorCount() + 1;
            parallelism = Math.min(parallelism, NetworkInfo.getMaxParallelism());
        }

        if (stepDependencies.isEmpty()) {
            parallelism = 1;
        }

        List<CraftStep> readySteps = getReadySteps(parallelism);
        if (readySteps.isEmpty()) {
            cleanupCompletedSteps();
            return;
        }

        lastActiveSteps = List.copyOf(readySteps);

        if (isCancelling) {
            handleCancellation();
            menu.getContents();
            if (!menu.getInventory().getViewers().isEmpty()) refreshGUI(54);
            return;
        }

        boolean anySuccess = false;

        for (CraftStep step : readySteps) {
            boolean success = processStep(step, maxDevices);
            if (success) anySuccess = true;
        }

        if (anySuccess) {
            globalFailTimes = 0;
        } else {
            globalFailTimes++;
        }

        cleanupCompletedSteps();

        if (globalFailTimes >= 32) {
            dispose();
            try {
                new AutoCraftingTask(info, recipe, count).start();
            } catch (Exception ignored) {
            }
            return;
        }

        menu.getContents();
        if (!menu.getInventory().getViewers().isEmpty()) refreshGUI(54);
    }

    private List<CraftStep> getReadySteps(int maxParallelism) {
        List<CraftStep> ready = new ArrayList<>();
        for (CraftStep step : craftingSteps) {
            if (completedSteps.contains(step)) continue;
            if (step.isCompleted()) {
                completedSteps.add(step);
                continue;
            }

            Set<CraftStep> deps = stepDependencies.get(step);
            if (deps == null || completedSteps.containsAll(deps)) {
                ready.add(step);
                if (ready.size() >= maxParallelism) break;
            }
        }
        return ready;
    }

    private void cleanupCompletedSteps() {
        Iterator<CraftStep> it = craftingSteps.iterator();
        while (it.hasNext()) {
            CraftStep step = it.next();
            if (step.isCompleted()) {
                completedSteps.add(step);
                it.remove();
            }
        }
    }

    private void handleCancellation() {
        boolean anyRunning = false;
        for (CraftStep step : craftingSteps) {
            if (step.getRunning() > 0 || step.getVirtualRunning() > 0) {
                anyRunning = true;
                CraftingRecipe nextRecipe = step.getRecipe();
                CraftType craftType = nextRecipe.getCraftType();

                if (craftType.isProcess()) {
                    Set<Location> toRemove = new HashSet<>();
                    for (Location deviceLoc : step.getRunningDevices()) {
                        Block deviceBlock = deviceLoc.getBlock();
                        var blockData = StorageCacheUtils.getBlock(deviceLoc);
                        if (blockData == null) {
                            ItemHashMap<Long> refund = new ItemHashMap<>();
                            for (Map.Entry<ItemKey, Long> entry :
                                    nextRecipe.getInputAmounts().keyEntrySet()) {
                                refund.putKey(entry.getKey(), entry.getValue());
                            }
                            storage.addItem(refund);
                            toRemove.add(deviceLoc);
                            continue;
                        }
                        SlimefunItem sfItem = SlimefunItem.getById(blockData.getSfId());
                        if (!(sfItem instanceof IMERealCraftDevice device)) {
                            ItemHashMap<Long> refund = new ItemHashMap<>();
                            for (Map.Entry<ItemKey, Long> entry :
                                    nextRecipe.getInputAmounts().keyEntrySet()) {
                                refund.putKey(entry.getKey(), entry.getValue());
                            }
                            storage.addItem(refund);
                            toRemove.add(deviceLoc);
                            continue;
                        }
                        if (device.isFinished(deviceBlock)) {
                            CraftingRecipe finished = device.getFinishedCraftingRecipe(deviceBlock);
                            if (finished != null && finished.equals(nextRecipe)) {
                                device.finishCrafting(deviceBlock);
                                storage.addItem(nextRecipe.getOutput());
                                toRemove.add(deviceLoc);
                            }
                        }
                    }
                    for (Location loc : toRemove) {
                        step.removeRunningDevice(loc);
                    }
                }

                if (step.getVirtualRunning() > 0) {
                    ItemHashMap<Long> resultItems = new ItemHashMap<>();
                    for (Map.Entry<ItemKey, Long> entry :
                            nextRecipe.getInputAmounts().keyEntrySet()) {
                        resultItems.putKey(entry.getKey(), entry.getValue() * step.getVirtualRunning());
                    }
                    storage.addItem(resultItems);
                    step.setVirtualRunning(0);
                }
            }
        }

        if (!anyRunning) {
            dispose();
            return;
        }

        cancelFailTimes++;
        if (cancelFailTimes >= 64) {
            for (CraftStep step : craftingSteps) {
                if (step.getRunning() > 0) {
                    CraftingRecipe nextRecipe = step.getRecipe();
                    int remaining = step.getRunning();
                    ItemHashMap<Long> refund = new ItemHashMap<>();
                    for (Map.Entry<ItemKey, Long> entry :
                            nextRecipe.getInputAmounts().keyEntrySet()) {
                        refund.putKey(entry.getKey(), entry.getValue() * remaining);
                    }
                    storage.addItem(refund);
                    for (Location loc : new HashSet<>(step.getRunningDevices())) {
                        step.removeRunningDevice(loc);
                    }
                }
            }
            dispose();
        }
    }

    private boolean processStep(CraftStep step, int maxDevices) {
        CraftingRecipe nextRecipe = step.getRecipe();
        CraftType craftType = nextRecipe.getCraftType();
        boolean doCraft = !isCancelling;
        boolean hasProgress = false;

        if (step.getAmount() <= 0) {
            if (step.isIdle()) {
                return false;
            }
            doCraft = false;
        }

        List<Location> holderLocations = info.getRecipeToHolders().getOrDefault(nextRecipe, Collections.emptyList());

        if (craftType.isProcess()) {
            Set<Location> invalidDevices = new HashSet<>();
            for (Location deviceLoc : step.getRunningDevices()) {
                Block deviceBlock = deviceLoc.getBlock();
                var blockData = StorageCacheUtils.getBlock(deviceLoc);
                if (blockData == null) {
                    invalidDevices.add(deviceLoc);
                    continue;
                }
                SlimefunItem sfItem = SlimefunItem.getById(blockData.getSfId());
                if (!(sfItem instanceof IMERealCraftDevice device)) {
                    invalidDevices.add(deviceLoc);
                    continue;
                }
                if (device.isFinished(deviceBlock)) {
                    CraftingRecipe finished = device.getFinishedCraftingRecipe(deviceBlock);
                    if (finished != null && finished.equals(nextRecipe)) {
                        device.finishCrafting(deviceBlock);
                        storage.addItem(finished.getOutput());
                        invalidDevices.add(deviceLoc);
                        hasProgress = true;
                    }
                }
            }
            for (Location loc : invalidDevices) {
                step.removeRunningDevice(loc);
            }

            if (doCraft) {
                Map<Location, Block[]> deviceCache = info.getCachedCraftingDevices();
                for (Location location : holderLocations) {
                    IMECraftHolder holder =
                            SlimeAEPlugin.getNetworkData().AllCraftHolders.get(location);
                    if (holder == null) continue;
                    Block[] devices = deviceCache.get(location);
                    if (devices == null) devices = holder.getCraftingDevices(location.getBlock());
                    for (Block deviceBlock : devices) {
                        if (step.getAmount() <= 0 || step.getRunning() >= maxDevices) break;
                        var blockData = StorageCacheUtils.getBlock(deviceBlock.getLocation());
                        if (blockData == null) continue;
                        SlimefunItem sfItem = SlimefunItem.getById(blockData.getSfId());
                        if (!(sfItem instanceof IMERealCraftDevice device)) continue;
                        if (!device.isSupport(deviceBlock, nextRecipe)) continue;
                        if (device.canStartCrafting(deviceBlock, nextRecipe)) {
                            ItemRequest[] inputRequests = ItemUtils.createRequests(nextRecipe.getInputAmounts());
                            if (storage.contains(inputRequests)) {
                                storage.takeItem(inputRequests);
                                if (device.startCrafting(deviceBlock, nextRecipe)) {
                                    step.addRunningDevice(deviceBlock.getLocation());
                                    step.decreaseAmount(1);
                                    hasProgress = true;
                                } else {
                                    ItemHashMap<Long> refund = new ItemHashMap<>();
                                    for (Map.Entry<ItemKey, Long> re :
                                            nextRecipe.getInputAmounts().keyEntrySet()) {
                                        refund.putKey(re.getKey(), re.getValue());
                                    }
                                    storage.addItem(refund);
                                }
                            }
                        }
                    }
                }
            }
        }

        int totalSpeed = info.getVirtualCraftingDeviceSpeeds().getOrDefault(craftType, 0);
        int used = info.getVirtualCraftingDeviceUsed().getOrDefault(craftType, 0);
        int available = totalSpeed - used;
        if (available > 0) {
            long neededSpeed = Math.min(step.getVirtualRunning() * 4L, maxDevices * 4L);
            int speed = totalSpeed;
            if (speed > maxDevices * 4) speed = maxDevices * 4;
            if (speed > neededSpeed) speed = (int) neededSpeed;
            if (speed > available) speed = available;

            step.addVirtualProcess(speed);
            info.getVirtualCraftingDeviceUsed().put(craftType, used + speed);
        }

        int result = Math.min(step.getVirtualProcess() / 4, step.getVirtualRunning());
        step.setVirtualProcess(step.getVirtualProcess() - result * 4);

        step.setVirtualRunning(step.getVirtualRunning() - result);
        ItemHashMap<Long> outputAmounts = nextRecipe.getOutputAmounts();
        ItemHashMap<Long> inputAmounts = nextRecipe.getInputAmounts();
        ItemHashMap<Long> resultItems = new ItemHashMap<>();
        for (Map.Entry<ItemKey, Long> entry : outputAmounts.keyEntrySet()) {
            resultItems.putKey(entry.getKey(), entry.getValue() * result);
        }
        storage.addItem(resultItems);
        if (result > 0) hasProgress = true;

        long actualAmount = Math.min(maxDevices - step.getVirtualRunning(), step.getAmount());
        if (actualAmount > 0) {
            ItemHashMap<Long> neededItems = new ItemHashMap<>();
            for (Map.Entry<ItemKey, Long> entry : inputAmounts.keyEntrySet()) {
                neededItems.putKey(entry.getKey(), entry.getValue() * actualAmount);
            }

            ItemRequest[] requests = ItemUtils.createRequests(neededItems);
            if (storage.contains(requests)) {
                boolean hasRealDevices = false;
                if (craftType.isProcess()) {
                    for (Location loc : holderLocations) {
                        Block[] devs = info.getCachedCraftingDevices().get(loc);
                        if (devs != null) {
                            for (Block db : devs) {
                                var bd = StorageCacheUtils.getBlock(db.getLocation());
                                if (bd == null) continue;
                                SlimefunItem si = SlimefunItem.getById(bd.getSfId());
                                if (si instanceof IMERealCraftDevice) {
                                    hasRealDevices = true;
                                    break;
                                }
                            }
                        }
                        if (hasRealDevices) break;
                    }
                }
                if (doCraft && (!craftType.isProcess() || !hasRealDevices)) {
                    storage.takeItem(requests);
                    step.setVirtualRunning(step.getVirtualRunning() + (int) actualAmount);
                    step.decreaseAmount(actualAmount);
                    return true;
                }
            }
        }

        return hasProgress;
    }

    public void showGUI(Player player) {
        refreshGUI(54);
        menu.open(player);
    }

    public void refreshGUI(int maxSize) {
        refreshGUI(maxSize, true);
    }

    public void refreshGUI(int maxSize, boolean cancelButton) {
        if (cancelButton) maxSize--;
        List<CraftStep> process = getCraftingSteps();
        List<CraftStep> process2 = new ArrayList<>();
        if (process.size() > maxSize - 1) {
            process2 = process.subList(maxSize, process.size());
            process = process.subList(0, maxSize);
        }

        int filledUpTo = -1;

        for (int i = 0; i < process.size(); i++) {
            CraftStep item = process.get(i);
            ItemStack[] itemStacks = item.getRecipe().getOutput();
            ItemStack itemStack;
            if (itemStacks.length == 1) {
                itemStack = itemStacks[0].clone();
            } else {
                itemStack = new AdvancedCustomItemStack(
                        Material.BARREL,
                        "&e&l多物品",
                        Arrays.stream(itemStacks)
                                .map(x -> "  &e- &f" + ItemUtils.getItemName(x) + "&f x " + x.getAmount() + "次")
                                .toArray(String[]::new));
            }
            ItemMeta meta = itemStack.getItemMeta();
            List<String> lore = meta.getLore();
            if (lore == null) lore = new ArrayList<>();
            lore.add("");
            lore.add("&a计划合成 &e" + item.getAmount() + "&a次");
            if (item.getRunning() + item.getVirtualRunning() != 0)
                lore.add("&a合成中 &e" + (item.getRunning() + item.getVirtualRunning()));
            meta.setLore(CMIChatColor.translate(lore));
            itemStack.setItemMeta(meta);
            NBT.modify(itemStack, x -> {
                x.setBoolean(CRAFTING_KEY, true);
            });
            menu.replaceExistingItem(i, itemStack);
            menu.addMenuClickHandler(i, ChestMenuUtils.getEmptyClickHandler());
            filledUpTo = i;
        }
        // 优化，防止任务依赖配方过多时 lore 超限
        if (!process2.isEmpty()) {
            ItemStack itemStack = new AdvancedCustomItemStack(Material.BARREL, "&e&l省略" + process2.size() + "项");

            ArrayList<String> lore = new ArrayList<>();
            int maxLines = MAX_LORE_LINES - 1;
            int displayCount = Math.min(process2.size(), maxLines);
            int remaining = process2.size() - displayCount;

            for (CraftStep item : process2.subList(0, displayCount)) {
                CraftingRecipe craftingRecipe = item.getRecipe();
                SlimefunItem slimefunItem =
                        SlimefunItem.getByItem(craftingRecipe.getOutput()[0]);
                if (slimefunItem != null) {
                    lore.add("  - " + CMIChatColor.stripColor(slimefunItem.getItemName()) + " x "
                            + craftingRecipe.getOutput()[0].getAmount());
                } else {
                    lore.add("  - "
                            + CMIMaterial.get(craftingRecipe.getOutput()[0].getType())
                                    .getTranslatedName() + " x "
                            + craftingRecipe.getOutput()[0].getAmount());
                }
            }
            if (remaining > 0) {
                lore.add(CMIChatColor.translate(String.format(MORE_ITEMS_INDICATOR, remaining)));
            }
            ItemMeta meta = itemStack.getItemMeta();
            meta.setLore(CMIChatColor.translate(lore));
            itemStack.setItemMeta(meta);
            NBT.modify(itemStack, x -> {
                x.setBoolean(CRAFTING_KEY, true);
            });
            menu.replaceExistingItem(maxSize - 1, itemStack);
            menu.addMenuClickHandler(maxSize - 1, ChestMenuUtils.getEmptyClickHandler());
            filledUpTo = maxSize - 1;
        }

        // 只清空多余的槽位（之前有内容但现在不再需要的位置），避免闪烁
        for (int i = filledUpTo + 1; i < maxSize; i++) {
            menu.replaceExistingItem(i, null);
            menu.addMenuClickHandler(i, ChestMenuUtils.getEmptyClickHandler());
        }

        if (cancelButton) {
            menu.replaceExistingItem(maxSize, MenuItems.CANCEL);
            menu.addMenuClickHandler(maxSize, (player, i1, itemStack, clickAction) -> {
                if (isCancelling) {
                    dispose();
                    player.sendMessage(CMIChatColor.translate("&a&l成功强制取消了合成任务"));
                    player.closeInventory();
                    return false;
                }

                isCancelling = true;
                player.sendMessage(CMIChatColor.translate("&a&l开始取消合成任务"));
                player.closeInventory();
                return false;
            });
        }
        // build inventory
        menu.getContents();
    }

    public void start() {
        if (!SlimeAEPlugin.getNetworkData().AllNetworkData.contains(info)) return;

        AutoCraftingTaskStartingEvent e = new AutoCraftingTaskStartingEvent(this);
        Bukkit.getPluginManager().callEvent(e);

        menu.addMenuCloseHandler(player -> {});

        info.getAutoCraftingSessions().add(this);
    }

    public AEMenu getMenu() {
        return menu;
    }

    @Override
    public synchronized void dispose() {
        if (taskState == TaskState.DISPOSED) return;
        taskState = TaskState.DISPOSED;

        if (SlimeAEPlugin.getCraftTaskPersistence() != null) {
            SlimeAEPlugin.getCraftTaskPersistence().delete(taskId);
        }

        AutoCraftingTaskDisposingEvent e = new AutoCraftingTaskDisposingEvent(this);
        Bukkit.getPluginManager().callEvent(e);

        info.getAutoCraftingSessions().remove(this);

        // 把材料直接推回 StorageCollection（而非通过 tempStorage 间接转移），
        // 确保物品立即回到真实存储中，避免以下问题：
        // 1. tempStorage 的 addItem 与 updateTempStorage 的 takeItem 并发竞态导致物品丢失或不可见
        // 2. getStorageUnsafe() 缓存 (F3) 过期导致快照中不含已归还物品
        // 3. notIncluded 负面缓存导致 takeItem/contains 错误跳过已归还物品
        ItemHashMap<Long> toReturn = new ItemHashMap<>(storage.getStorageUnsafe());

        // === 调试日志 ===
        java.util.logging.Logger debugLog = SlimeAEPlugin.getInstance().getLogger();
        if (SlimeAEPlugin.isDebug()) {
            debugLog.info("[AutoCraft-Debug] === dispose() 开始归还物品 ===");
            for (Map.Entry<ItemKey, Long> de : toReturn.keyEntrySet()) {
                if (de.getValue() > 0) {
                    debugLog.info("[AutoCraft-Debug] 待归还: "
                            + ItemUtils.getItemName(de.getKey().getItemStack()) + " = " + de.getValue());
                }
            }
        }
        // === 调试日志结束 ===

        StorageCollection currentStorage = info.getStorage();
        currentStorage.clearNotIncluded();
        currentStorage.clearTakeAndPushCache();
        currentStorage.pushItem(toReturn);
        // pushItem 会将剩余量写回 toReturn 的 entry 中，
        // 推不进去的物品放入 tempStorage 作为后备
        ItemUtils.trim(toReturn);

        // === 调试日志 ===
        if (!toReturn.isEmpty()) {
            if (SlimeAEPlugin.isDebug()) {
                debugLog.info("[AutoCraft-Debug] pushItem后仍有剩余（将放入tempStorage）:");
                for (Map.Entry<ItemKey, Long> de : toReturn.keyEntrySet()) {
                    debugLog.info("[AutoCraft-Debug]   剩余: "
                            + ItemUtils.getItemName(de.getKey().getItemStack()) + " = " + de.getValue());
                }
            }
            synchronized (info) {
                info.getTempStorage().addItem(toReturn, true);
            }
        } else {
            if (SlimeAEPlugin.isDebug()) {
                debugLog.info("[AutoCraft-Debug] 所有物品已成功推回存储");
            }
        }
        // === 调试日志结束 ===

        currentStorage.invalidateStorageCache();

        // === 调试日志: 验证归还后存储 ===
        if (SlimeAEPlugin.isDebug()) {
            currentStorage.invalidateStorageCache();
            ItemHashMap<Long> afterReturn = currentStorage.getStorageUnsafe();
            debugLog.info("[AutoCraft-Debug] 归还后存储快照:");
            for (Map.Entry<ItemKey, Long> de : afterReturn.keyEntrySet()) {
                if (de.getValue() > 0) {
                    debugLog.info("[AutoCraft-Debug]   "
                            + ItemUtils.getItemName(de.getKey().getItemStack()) + " = " + de.getValue());
                }
            }
            debugLog.info("[AutoCraft-Debug] === dispose() 完成 ===");
        }

        Bukkit.getScheduler()
                .runTask(SlimeAEPlugin.getInstance(), () -> menu.getInventory().close());
    }
}
