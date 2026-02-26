package me.ddggdd135.slimeae.api.autocraft;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.api.AEMenu;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.items.AdvancedCustomItemStack;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBT;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.ConcurrentHashSet;
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
    private final CraftingRecipe recipe;
    private final NetworkInfo info;
    private final long count;
    private final List<CraftStep> craftingSteps;
    private int running = 0;
    private int virtualRunning = 0;
    private int virtualProcess = 0;
    private final AEMenu menu;
    private boolean isCancelling = false;
    private final Set<CraftingRecipe> craftingPath = new HashSet<>();
    private ItemStorage storage;
    private int failTimes;
    private CraftTaskCalcData recipeCalcData = new CraftTaskCalcData();
    private boolean disposed;

    public AutoCraftingTask(@Nonnull NetworkInfo info, @Nonnull CraftingRecipe recipe, long count) {
        this.info = info;
        this.recipe = recipe;
        this.count = count;

        List<CraftStep> newSteps = null;

        List<CraftStep> usingSteps;
        // 强制使存储缓存失效，确保获取最新数据快照
        // （防止 dispose 归还物品后 F3 缓存仍保存旧数据）
        info.getStorage().invalidateStorageCache();
        info.getStorage().clearNotIncluded();

        // === 调试日志 ===
        java.util.logging.Logger debugLog = SlimeAEPlugin.getInstance().getLogger();
        if (SlimeAEPlugin.isDebug()) {
            debugLog.info("[AutoCraft-Debug] === 开始创建合成任务 ===");
            debugLog.info("[AutoCraft-Debug] 配方输出: " + ItemUtils.getItemName(recipe.getOutput()[0]) + " x" + count);
            debugLog.info(
                    "[AutoCraft-Debug] recipe在getRecipes中: " + info.getRecipes().contains(recipe));
            debugLog.info("[AutoCraft-Debug] getRecipes大小: " + info.getRecipes().size());
            ItemHashMap<Long> debugSnapshot = info.getStorage().getStorageUnsafe();
            for (Map.Entry<ItemKey, Long> de : debugSnapshot.keyEntrySet()) {
                if (de.getValue() > 0) {
                    debugLog.info("[AutoCraft-Debug] 存储内容: "
                            + ItemUtils.getItemName(de.getKey().getItemStack()) + " = " + de.getValue());
                }
            }
        }
        // === 调试日志结束 ===

        try {
            newSteps = calcCraftSteps(recipe, count, new ItemStorage(info.getStorage()));
            if (checkCraftStepsValid(newSteps, new ItemStorage(info.getStorage()))) usingSteps = newSteps;
            else throw new IllegalStateException("新版算法出错，退回旧版算法");
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
                usingSteps = match(recipe, count, new ItemStorage(info.getStorage()));
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

        this.storage = new ItemStorage();

        // 所有材料都能拿到
        for (CraftStep step : craftingSteps) {
            for (Map.Entry<ItemKey, Long> entry :
                    step.getRecipe().getInputAmounts().keyEntrySet()) {
                storage.addItem(entry.getKey(), entry.getValue() * step.getAmount());
            }
        }

        storage = info.getStorage().takeItem(ItemUtils.createRequests(storage.copyStorage()));

        String TitleInfo = craftingSteps == newSteps ? "&2(新版算法)" : "&7(旧版算法)";
        menu = new AEMenu("&e合成任务" + TitleInfo);
        menu.setSize(54);
        menu.addMenuCloseHandler(player -> dispose());
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

    private class CraftTaskCalcData {
        public class CraftTaskCalcItem {
            Set<CraftingRecipe> before = new ConcurrentHashSet<>();
            CraftingRecipe thisone;
            long count;

            CraftTaskCalcItem(CraftingRecipe recipe, long count) {
                this.thisone = recipe;
                this.count = count;
            }
        }

        public final Map<CraftingRecipe, CraftTaskCalcItem> recipeMap = new ConcurrentHashMap<>();
        public final List<CraftStep> result = new ArrayList<>();

        public void addRecipe(CraftingRecipe recipe, long count, CraftingRecipe parent) {
            recipeMap.get(parent).before.add(recipe);
            if (recipeMap.containsKey(recipe)) { // 加到之前的合成上
                recipeMap.get(recipe).count += count;
            } else { // 第一次合成
                recipeMap.put(recipe, new CraftTaskCalcItem(recipe, count));
            }
        }

        public void rootRecipe(CraftingRecipe recipe, long count) {
            recipeMap.clear();
            result.clear();
            recipeMap.put(recipe, new CraftTaskCalcItem(recipe, count));
        }
    }

    private List<CraftStep> calcCraftSteps(CraftingRecipe recipe, long count, ItemStorage storage) {
        recipeCalcData.rootRecipe(recipe, count);
        calcCraftStep(recipe, count, storage);
        unpackCraftSteps(recipe);
        return recipeCalcData.result;
    }

    private void unpackCraftSteps(CraftingRecipe recipe) {
        if (!craftingPath.add(recipe)) {
            throw new IllegalStateException("新版算法出错，退回旧版算法");
        }
        CraftTaskCalcData.CraftTaskCalcItem recipeInfo = recipeCalcData.recipeMap.get(recipe);
        for (CraftingRecipe beforeRecipe : recipeInfo.before) {
            if (recipeCalcData.recipeMap.containsKey(beforeRecipe)) unpackCraftSteps(beforeRecipe);
        }
        recipeCalcData.result.add(new CraftStep(recipe, recipeInfo.count));
        recipeCalcData.recipeMap.remove(recipe);
        craftingPath.remove(recipe);
    }

    private void calcCraftStep(CraftingRecipe recipe, long count, ItemStorage storage) {
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

            ItemStorage missing = new ItemStorage();
            ItemHashMap<Long> in = recipe.getInputAmounts();

            java.util.logging.Logger cLog = SlimeAEPlugin.getInstance().getLogger();
            if (SlimeAEPlugin.isDebug()) {
                cLog.info("[AutoCraft-Debug] calcCraftStep: 配方=" + ItemUtils.getItemName(recipe.getOutput()[0])
                        + " count=" + count);
                cLog.info("[AutoCraft-Debug] calcCraftStep: storage快照大小="
                        + storage.getStorageUnsafe().size() + ", input种类=" + in.size());
            }

            // 遍历所需材料
            for (ItemKey key : in.sourceKeySet()) {
                long amount = storage.getStorageUnsafe().getOrDefault(key, 0L);
                long need = in.getKey(key) * count;

                if (SlimeAEPlugin.isDebug()) {
                    cLog.info("[AutoCraft-Debug] calcCraftStep: 材料=" + ItemUtils.getItemName(key.getItemStack())
                            + " amount(存储中)=" + amount + " need=" + need
                            + " keyHash=" + key.hashCode());
                    // 额外检查：遍历 storage 的 key 看看是否有"同物品但不同hash"的情况
                    for (Map.Entry<ItemKey, Long> se :
                            storage.getStorageUnsafe().keyEntrySet()) {
                        if (se.getValue() > 0) {
                            boolean eq = key.equals(se.getKey());
                            cLog.info("[AutoCraft-Debug]   storage key: "
                                    + ItemUtils.getItemName(se.getKey().getItemStack())
                                    + "=" + se.getValue() + " hash="
                                    + se.getKey().hashCode()
                                    + " equals=" + eq);
                        }
                    }
                }

                if (amount >= need) {
                    storage.takeItem(new ItemRequest(key, need));
                } else {
                    long remainingNeed = need - amount;
                    if (amount > 0) {
                        storage.takeItem(new ItemRequest(key, amount));
                    }

                    // 尝试合成缺少的材料
                    CraftingRecipe craftingRecipe = getRecipe(key.getItemStack());
                    if (SlimeAEPlugin.isDebug()) {
                        cLog.info("[AutoCraft-Debug] calcCraftStep: 尝试子合成 " + ItemUtils.getItemName(key.getItemStack())
                                + " craftingRecipe=" + (craftingRecipe != null ? "found" : "null"));
                    }
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
                        recipeCalcData.addRecipe(craftingRecipe, countToCraft, recipe);
                        calcCraftStep(craftingRecipe, countToCraft, storage);
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
                if (SlimeAEPlugin.isDebug()) {
                    cLog.info("[AutoCraft-Debug] calcCraftStep: 缺失材料! 将抛出异常");
                }
                throw new NoEnoughMaterialsException(missing.getStorageUnsafe());
            }
        } finally {
            // 无论是否成功,都要从路径中移除当前配方
            craftingPath.remove(recipe);
        }
    }

    @Nullable private CraftingRecipe getRecipe(@Nonnull ItemStack itemStack) {
        return info.getRecipeFor(itemStack);
    }

    public boolean hasNext() {
        return !craftingSteps.isEmpty();
    }

    public synchronized void moveNext(int maxDevices) {
        if (!hasNext()) return;
        CraftStep next = craftingSteps.get(0);
        CraftType craftType = next.getRecipe().getCraftType();
        boolean doCraft = !isCancelling;
        if (running <= 0 && virtualRunning <= 0 && isCancelling) dispose();
        if (next.getAmount() <= 0) {
            if (running <= 0 && virtualRunning <= 0) {
                craftingSteps.remove(0);
                return;
            }

            doCraft = false;
        }
        Location[] locations = info.getRecipeMap().entrySet().stream()
                .filter(x -> x.getValue().contains(next.getRecipe()))
                .map(Map.Entry::getKey)
                .toArray(Location[]::new);

        for (Location location : locations) {
            IMECraftHolder holder =
                    SlimeAEPlugin.getNetworkData().AllCraftHolders.get(location);
            CraftingRecipe nextRecipe = next.getRecipe();
            if (Arrays.stream(holder.getSupportedRecipes(location.getBlock())).noneMatch(x -> x.equals(nextRecipe)))
                continue;
            for (Block deviceBlock : holder.getCraftingDevices(location.getBlock())) {
                IMECraftDevice imeCraftDevice = (IMECraftDevice) SlimefunItem.getById(
                        StorageCacheUtils.getBlock(deviceBlock.getLocation()).getSfId());
                if (!(imeCraftDevice instanceof IMERealCraftDevice device)) continue;
                if (!device.isSupport(deviceBlock, nextRecipe)) continue;
                if (running < maxDevices
                        && doCraft
                        && device.canStartCrafting(deviceBlock, nextRecipe)
                        && storage.contains(ItemUtils.createRequests(nextRecipe.getInputAmounts()))) {
                    storage.takeItem(ItemUtils.createRequests(nextRecipe.getInputAmounts()));
                    device.startCrafting(deviceBlock, nextRecipe);
                    running++;
                    next.decreaseAmount(1);
                    if (next.getAmount() <= 0) doCraft = false;
                } else if (running > 0
                        && device.isFinished(deviceBlock)
                        && device.getFinishedCraftingRecipe(deviceBlock).equals(nextRecipe)) {
                    CraftingRecipe finished = device.getFinishedCraftingRecipe(deviceBlock);
                    device.finishCrafting(deviceBlock);
                    storage.addItem(finished.getOutput());
                    running--;
                }
            }
        }

        // 计算虚拟设备
        if (isCancelling) {
            ItemHashMap<Long> resultItems = new ItemHashMap<>();
            for (Map.Entry<ItemKey, Long> entry :
                    next.getRecipe().getInputAmounts().keyEntrySet()) {
                resultItems.putKey(entry.getKey(), entry.getValue() * virtualRunning);
            }
            storage.addItem(resultItems);
            virtualRunning = 0;

            menu.getContents();
            if (!menu.getInventory().getViewers().isEmpty()) refreshGUI(54);
            return;
        }

        int available = info.getVirtualCraftingDeviceSpeeds().getOrDefault(craftType, 0)
                - info.getVirtualCraftingDeviceUsed().getOrDefault(craftType, 0);
        if (available > 0) {
            int tasks = 0;

            for (AutoCraftingTask task : info.getAutoCraftingSessions()) {
                if (task.getCraftingSteps().isEmpty()) continue;
                if (task.getCraftingSteps().get(0).getRecipe().getCraftType() == craftType) tasks++;
            }

            long neededSpeed = Math.min(virtualRunning * 4L, maxDevices * 4L);
            int speed = info.getVirtualCraftingDeviceSpeeds().getOrDefault(craftType, 0) / tasks;
            if (speed == 0) speed++;
            if (speed > maxDevices * 4) speed = maxDevices * 4;
            if (speed > neededSpeed) speed = (int) neededSpeed;

            virtualProcess += speed;
            info.getVirtualCraftingDeviceUsed()
                    .put(craftType, info.getVirtualCraftingDeviceUsed().getOrDefault(craftType, 0) + speed);
        }

        int result = Math.min(virtualProcess / 4, virtualRunning);
        virtualProcess -= result * 4;

        virtualRunning -= result;
        ItemHashMap<Long> resultItems = new ItemHashMap<>();
        for (Map.Entry<ItemKey, Long> entry :
                next.getRecipe().getOutputAmounts().keyEntrySet()) {
            resultItems.putKey(entry.getKey(), entry.getValue() * result);
        }
        storage.addItem(resultItems);

        long actualAmount = Math.min(maxDevices - virtualRunning, next.getAmount());
        ItemHashMap<Long> neededItems = new ItemHashMap<>();
        for (Map.Entry<ItemKey, Long> entry : next.getRecipe().getInputAmounts().keyEntrySet()) {
            neededItems.putKey(entry.getKey(), entry.getValue() * actualAmount);
        }

        ItemRequest[] requests = ItemUtils.createRequests(neededItems);
        if (storage.contains(requests)) {
            if (doCraft && next.getRecipe().getCraftType() != CraftType.COOKING) {
                failTimes = 0;
                storage.takeItem(requests);
                virtualRunning += (int) actualAmount;
                next.decreaseAmount(actualAmount);

                if (virtualRunning == actualAmount) {
                    menu.getContents();
                    if (!menu.getInventory().getViewers().isEmpty()) refreshGUI(54);

                    return;
                }
            }
        } else {
            failTimes++;
        }

        if (failTimes >= 32) {
            dispose();
            try {
                new AutoCraftingTask(info, recipe, count).start();
            } catch (Exception ignored) {
            }
        }

        menu.getContents();
        if (!menu.getInventory().getViewers().isEmpty()) refreshGUI(54);
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
            if (i == 0 && running + virtualRunning != 0) lore.add("&a合成中 &e" + (running + virtualRunning));
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
    public void dispose() {
        if (disposed) return;
        disposed = true;

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
            info.getTempStorage().addItem(toReturn, true);
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
