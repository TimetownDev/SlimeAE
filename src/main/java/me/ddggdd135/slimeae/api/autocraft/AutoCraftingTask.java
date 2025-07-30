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
import me.ddggdd135.slimeae.api.events.AutoCraftingTaskDisposingEvent;
import me.ddggdd135.slimeae.api.events.AutoCraftingTaskStartingEvent;
import me.ddggdd135.slimeae.api.exceptions.NoEnoughMaterialsException;
import me.ddggdd135.slimeae.api.interfaces.*;
import me.ddggdd135.slimeae.api.items.ItemRequest;
import me.ddggdd135.slimeae.api.items.ItemStorage;
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

    public AutoCraftingTask(@Nonnull NetworkInfo info, @Nonnull CraftingRecipe recipe, long count) {
        this.info = info;
        this.recipe = recipe;
        this.count = count;

        // 先使用旧版算法，验证checkCraftStepsValid是否有误
        // 如果无误可以将这一步放在新版算法出错后，节省一次计算（当前版本计算量是原来的2倍）
        List<CraftStep> oldSteps = match(recipe, count, new ItemStorage(info.getStorage()));

        //TODO 删除调试信息
        String errorInfo;
        if(!checkCraftStepsValid(oldSteps,new ItemStorage(info.getStorage()))){
            errorInfo="检查合成步骤出错（这是一条开发者消息，如果您看到这条信息，请截图告知开发者，多谢）";
        }else errorInfo="";

        List<CraftStep> newSteps = calcCraftSteps(recipe, count, new ItemStorage(info.getStorage()));

        // 如果新版算法出错，则退回旧版算法
        if(!checkCraftStepsValid(newSteps,new ItemStorage(info.getStorage())))
            craftingSteps = oldSteps;
        else craftingSteps = newSteps;

        this.storage = new ItemStorage();

        // 所有材料都能拿到
        for (CraftStep step : craftingSteps) {
            for (Map.Entry<ItemKey, Long> entry :
                    step.getRecipe().getInputAmounts().keyEntrySet()) {
                storage.addItem(entry.getKey(), entry.getValue() * step.getAmount());
            }
        }

        storage = info.getStorage().takeItem(ItemUtils.createRequests(storage.copyStorage()));

        //TODO 删除调试信息
        boolean debug_initialItemEnough;
        debug_initialItemEnough =checkCraftStepsValid(craftingSteps,new ItemStorage(storage));

        String TitleInfo=craftingSteps==newSteps?"&2(新版算法)":"&7(旧版算法)";
        if(debug_initialItemEnough)menu=new AEMenu("&e合成任务"+TitleInfo+errorInfo);
        else menu=new AEMenu("&e合成任务"+TitleInfo+"&c(拿取材料失败，合成可能卡住)"+errorInfo);
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
        for(CraftStep step : steps) {
            for(Map.Entry<ItemKey, Long> input : step.getRecipe().getInputAmounts().keyEntrySet()) {
                long amount = input.getValue() * step.getAmount();
                if(amount>storage.getStorageUnsafe().getOrDefault(input.getKey(), 0L))
                    return false;
                storage.takeItem(new ItemRequest(input.getKey(), amount));
            }
            for(Map.Entry<ItemKey, Long> output : step.getRecipe().getOutputAmounts().keyEntrySet()) {
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
                // 记录直接缺少的材料
                ItemStorage missing = new ItemStorage();
                ItemHashMap<Long> in = recipe.getInputAmounts();
                for (ItemStack template : in.keySet()) {
                    long amount = storage.getStorageUnsafe().getOrDefault(template, 0L);
                    long need = in.get(template) * count;
                    if (amount < need) {
                        missing.addItem(new ItemKey(template), need - amount);
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


    private class CraftTaskCalcData{
        public class CraftTaskCalcItem{
            List<CraftingRecipe> before=new ArrayList<>();
            CraftingRecipe thisone;
            long count;
            CraftTaskCalcItem(CraftingRecipe recipe, long count){
                this.thisone=recipe;
                this.count=count;
            }
        }
        public final Map<CraftingRecipe, CraftTaskCalcItem> recipeMap = new ConcurrentHashMap<>();
        public void addRecipe(CraftingRecipe recipe, long count, CraftingRecipe parent) {
            if(recipeMap.containsKey(recipe)) { //加到之前的合成上
                recipeMap.get(recipe).count+=count;
            }else{  //第一次合成
                recipeMap.put(recipe, new CraftTaskCalcItem(recipe,count));
                recipeMap.get(parent).before.add(recipe);
            }
        }
        public void rootRecipe(CraftingRecipe recipe, long count) {
            recipeMap.clear();
            recipeMap.put(recipe, new CraftTaskCalcItem(recipe,count));
        }
    }
    private List<CraftStep> calcCraftSteps(CraftingRecipe recipe, long count, ItemStorage storage){
        recipeCalcData.rootRecipe(recipe, count);
        calcCraftStep(recipe, count, storage);
        return unpackCraftSteps(recipe);
    }
    private List<CraftStep> unpackCraftSteps(CraftingRecipe recipe){
        List<CraftStep> result = new ArrayList<>();
        CraftTaskCalcData.CraftTaskCalcItem recipeInfo=recipeCalcData.recipeMap.get(recipe);
        for(CraftingRecipe beforeRecipe:recipeInfo.before){
            result.addAll(unpackCraftSteps(beforeRecipe));
        }
        result.add(new CraftStep(recipe, recipeInfo.count));
        return result;
    }
    private void calcCraftStep(CraftingRecipe recipe, long count, ItemStorage storage) {
        if (!craftingPath.add(recipe)) {
            throw new IllegalStateException("检测到循环依赖的合成配方");
        }

        try {
            if (!info.getRecipes().contains(recipe)) {
                // 记录直接缺少的材料
                ItemStorage missing = new ItemStorage();
                ItemHashMap<Long> in = recipe.getInputAmounts();
                for (ItemStack template : in.keySet()) {
                    long amount = storage.getStorageUnsafe().getOrDefault(template, 0L);
                    long need = in.get(template) * count;
                    if (amount < need) {
                        missing.addItem(new ItemKey(template), need - amount);
                    }
                }
                throw new NoEnoughMaterialsException(missing.getStorageUnsafe());
            }

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
                        recipeCalcData.addRecipe(craftingRecipe,countToCraft,recipe);
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
        for (int i = 0; i < maxSize; i++) {
            menu.replaceExistingItem(i, null);
            menu.addMenuClickHandler(i, ChestMenuUtils.getEmptyClickHandler());
        }
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
            menu.addItem(i, itemStack);
            menu.addMenuClickHandler(i, ChestMenuUtils.getEmptyClickHandler());
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
            menu.addItem(maxSize - 1, itemStack);
        }

        if (cancelButton) {
            menu.addItem(maxSize, MenuItems.CANCEL);
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
        AutoCraftingTaskDisposingEvent e = new AutoCraftingTaskDisposingEvent(this);
        Bukkit.getPluginManager().callEvent(e);

        info.getAutoCraftingSessions().remove(this);
        info.getTempStorage().addItem(storage.getStorageUnsafe(), true);

        Bukkit.getScheduler()
                .runTask(SlimeAEPlugin.getInstance(), () -> menu.getInventory().close());
    }
}

