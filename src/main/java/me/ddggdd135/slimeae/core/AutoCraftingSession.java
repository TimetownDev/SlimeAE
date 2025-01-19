package me.ddggdd135.slimeae.core;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.api.AEMenu;
import me.ddggdd135.guguslimefunlib.items.AdvancedCustomItemStack;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.CraftingRecipe;
import me.ddggdd135.slimeae.api.ItemRequest;
import me.ddggdd135.slimeae.api.ItemStorage;
import me.ddggdd135.slimeae.api.exceptions.NoEnoughMaterialsException;
import me.ddggdd135.slimeae.api.interfaces.IMECraftDevice;
import me.ddggdd135.slimeae.api.interfaces.IMECraftHolder;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.ddggdd135.slimeae.utils.KeyValuePair;
import net.Zrips.CMILib.Items.CMIMaterial;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class AutoCraftingSession {
    public static NamespacedKey CRAFTING_KEY = new NamespacedKey(SlimeAEPlugin.getInstance(), "auto_crafting");
    private final CraftingRecipe recipe;
    private final NetworkInfo info;
    private final int count;
    private final List<KeyValuePair<CraftingRecipe, Integer>> craftingSteps;
    private final ItemStorage itemCache = new ItemStorage();
    private int running = 0;
    private final AEMenu menu = new AEMenu("&e合成任务");
    private boolean isCancelling = false;
    private final Set<CraftingRecipe> craftingPath = new HashSet<>();

    public AutoCraftingSession(@Nonnull NetworkInfo info, @Nonnull CraftingRecipe recipe, int count) {
        //        ItemStorage storage = new ItemStorage();
        //        storage.addItem(
        //                ItemUtils.createItems(new AdvancedCustomItemStack(SlimefunAEItems.CRYSTAL_CERTUS_QUARTZ),
        // 5000000));
        //        storage.addItem(ItemUtils.createItems(new AdvancedCustomItemStack(SlimefunAEItems.LOGIC_PROCESSOR),
        // 5000000));
        //        storage.addItem(
        //                ItemUtils.createItems(new AdvancedCustomItemStack(SlimefunAEItems.CALCULATION_PROCESSOR),
        // 5000000));
        //        storage.addItem(ItemUtils.createItems(new ItemStack(Material.IRON_INGOT), 64));
        //        storage.addItem(ItemUtils.createItems(new ItemStack(Material.REDSTONE), 5000000));
        //        storage.addItem(ItemUtils.createItems(new ItemStack(Material.GLASS), 5000000));
        //        List<Pair<CraftingRecipe, Integer>> pairList =
        //                match(getRecipe(new ItemStack(SlimefunAEItems.ME_ITEM_STORAGE_CELL_16M)), 1, storage);
        this.info = info;
        this.recipe = recipe;
        this.count = count;
        menu.setSize(54);
        craftingSteps = match(recipe, count, new ItemStorage(info.getStorage()));
    }

    @Nonnull
    public CraftingRecipe getRecipe() {
        return recipe;
    }

    @Nonnull
    public NetworkInfo getNetworkInfo() {
        return info;
    }

    public int getCount() {
        return count;
    }

    @Nonnull
    public List<KeyValuePair<CraftingRecipe, Integer>> getCraftingSteps() {
        return craftingSteps;
    }

    private List<KeyValuePair<CraftingRecipe, Integer>> match(CraftingRecipe recipe, int count, ItemStorage storage) {
        if (!craftingPath.add(recipe)) {
            throw new IllegalStateException("检测到循环依赖的合成配方");
        }

        try {
            if (!info.getRecipes().contains(recipe)) {
                // 记录直接缺少的材料
                ItemStorage missing = new ItemStorage();
                Map<ItemStack, Integer> in = ItemUtils.getAmounts(recipe.getInput());
                for (ItemStack template : in.keySet()) {
                    int amount = storage.getStorage().getOrDefault(template, 0);
                    int need = in.get(template) * count;
                    if (amount < need) {
                        missing.addItem(ItemUtils.createItems(template, need - amount));
                    }
                }
                throw new NoEnoughMaterialsException(missing.getStorage());
            }

            List<KeyValuePair<CraftingRecipe, Integer>> result = new ArrayList<>();
            ItemStorage missing = new ItemStorage();
            Map<ItemStack, Integer> in = ItemUtils.getAmounts(recipe.getInput());

            // 遍历所需材料
            for (ItemStack template : in.keySet()) {
                int amount = storage.getStorage().getOrDefault(template, 0);
                int need = in.get(template) * count;

                if (amount >= need) {
                    storage.tryTakeItem(new ItemRequest(template, need));
                } else {
                    int remainingNeed = need - amount;
                    if (amount > 0) {
                        storage.tryTakeItem(new ItemRequest(template, amount));
                    }

                    // 尝试合成缺少的材料
                    CraftingRecipe craftingRecipe = getRecipe(template);
                    if (craftingRecipe == null) {
                        missing.addItem(ItemUtils.createItems(template, remainingNeed));
                        continue;
                    }

                    Map<ItemStack, Integer> output = ItemUtils.getAmounts(craftingRecipe.getOutput());
                    Map<ItemStack, Integer> input = ItemUtils.getAmounts(craftingRecipe.getInput());

                    // 计算需要合成多少次
                    int out = output.get(template) - input.getOrDefault(template, 0);
                    int countToCraft = (int) Math.ceil(remainingNeed / (double) out);

                    try {
                        result.addAll(match(craftingRecipe, countToCraft, storage));
                    } catch (NoEnoughMaterialsException e) {
                        // 合并子合成缺少的材料
                        for (Map.Entry<ItemStack, Integer> entry :
                                e.getMissingMaterials().entrySet()) {
                            missing.addItem(ItemUtils.createItems(entry.getKey(), entry.getValue()));
                        }
                    }
                }
            }

            // 如果有缺少的材料就抛出异常
            if (!missing.getStorage().isEmpty()) {
                throw new NoEnoughMaterialsException(missing.getStorage());
            }

            result.add(new KeyValuePair<>(recipe, count));
            return result;
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

    public void moveNext(int maxDevices) {
        if (!hasNext()) return;
        KeyValuePair<CraftingRecipe, Integer> next = craftingSteps.get(0);
        boolean doCraft = !isCancelling;
        if (running == 0 && isCancelling) info.getCraftingSessions().remove(this);
        if (next.getValue() <= 0) {
            if (running <= 0) {
                craftingSteps.remove(0);
                return;
            }

            doCraft = false;
        }
        Location[] locations = info.getRecipeMap().entrySet().stream()
                .filter(x -> x.getValue().contains(next.getKey()))
                .map(Map.Entry::getKey)
                .toArray(Location[]::new);
        int allocated = 0;
        IStorage networkStorage = info.getStorage();
        if (!networkStorage.contains(ItemUtils.createRequests(
                        ItemUtils.getAmounts(next.getKey().getInput())))
                && running <= 0) {
            // 合成出现错误 重新规划
            try {
                info.getCraftingSessions().remove(this);
                new AutoCraftingSession(
                        info,
                        recipe,
                        craftingSteps.get(craftingSteps.size() - 1).getValue());
            } catch (Throwable ignored) {
            }
        }
        for (Location location : locations) {
            IMECraftHolder holder = (IMECraftHolder)
                    SlimefunItem.getById(StorageCacheUtils.getBlock(location).getSfId());
            if (!Arrays.stream(holder.getSupportedRecipes(location.getBlock())).anyMatch(x -> x.equals(next.getKey())))
                continue;
            for (Block deviceBlock : holder.getCraftingDevices(location.getBlock())) {
                IMECraftDevice device = (IMECraftDevice) SlimefunItem.getById(
                        StorageCacheUtils.getBlock(deviceBlock.getLocation()).getSfId());
                if (!device.isSupport(deviceBlock, next.getKey())) continue;
                if (allocated > maxDevices) return;
                if (doCraft
                        && device.canStartCrafting(deviceBlock, next.getKey())
                        && networkStorage.contains(ItemUtils.createRequests(
                                ItemUtils.getAmounts(next.getKey().getInput())))) {
                    networkStorage.tryTakeItem(ItemUtils.createRequests(
                            ItemUtils.getAmounts(next.getKey().getInput())));
                    device.startCrafting(deviceBlock, next.getKey());
                    running++;
                    next.setValue(next.getValue() - 1);
                    if (next.getValue() == 0) doCraft = false;
                } else if (running > 0
                        && device.isFinished(deviceBlock)
                        && device.getFinishedCraftingRecipe(deviceBlock).equals(next.getKey())) {
                    CraftingRecipe finished = device.getFinishedCraftingRecipe(deviceBlock);
                    device.finishCrafting(deviceBlock);
                    itemCache.addItem(finished.getOutput());
                    running--;
                }
                allocated++;
            }
        }

        Set<ItemStack> toPush = new HashSet<>(itemCache.getStorage().keySet());
        for (ItemStack itemStack : toPush) {
            ItemStack[] items = itemCache.tryTakeItem(new ItemRequest(itemStack, Integer.MAX_VALUE));
            networkStorage.pushItem(items);
            items = Arrays.stream(items).filter(x -> x.getAmount() != 0).toArray(ItemStack[]::new);
            itemCache.pushItem(items);
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
        List<KeyValuePair<CraftingRecipe, Integer>> process = getCraftingSteps();
        List<KeyValuePair<CraftingRecipe, Integer>> process2 = new ArrayList<>();
        if (process.size() > maxSize - 1) {
            process2 = process.subList(maxSize, process.size());
            process = process.subList(0, maxSize);
        }
        for (int i = 0; i < maxSize; i++) {
            menu.replaceExistingItem(i, null);
            menu.addMenuClickHandler(i, ChestMenuUtils.getEmptyClickHandler());
        }
        for (int i = 0; i < process.size(); i++) {
            KeyValuePair<CraftingRecipe, Integer> item = process.get(i);
            ItemStack[] itemStacks = item.getKey().getOutput();
            ItemStack itemStack;
            if (itemStacks.length == 1) {
                itemStack = itemStacks[0].clone();
            } else {
                itemStack = new AdvancedCustomItemStack(
                        Material.BARREL,
                        "&e&l多物品",
                        Arrays.stream(itemStacks)
                                .map(x -> "  &e- &f" + ItemUtils.getItemName(x) + "&f x " + x.getAmount())
                                .toArray(String[]::new));
            }
            ItemMeta meta = itemStack.getItemMeta();
            meta.getPersistentDataContainer().set(CRAFTING_KEY, PersistentDataType.BOOLEAN, true);
            List<String> lore = meta.getLore();
            if (lore == null) lore = new ArrayList<>();
            lore.add("");
            lore.add("&a计划合成 &e" + item.getValue());
            if (i == 0 && running != 0) lore.add("&a合成中 &e" + running);
            meta.setLore(CMIChatColor.translate(lore));
            itemStack.setItemMeta(meta);
            menu.addItem(i, itemStack);
            menu.addMenuClickHandler(i, ChestMenuUtils.getEmptyClickHandler());
        }
        if (!process2.isEmpty()) {
            ItemStack itemStack = new AdvancedCustomItemStack(Material.BARREL, "&e&l省略" + process2.size() + "项");
            List<String> lore = new ArrayList<>();
            for (KeyValuePair<CraftingRecipe, Integer> item : process2) {
                SlimefunItem slimefunItem = SlimefunItem.getByItem(item.getKey().getOutput()[0]);
                if (slimefunItem != null) {
                    lore.add("  - " + CMIChatColor.stripColor(slimefunItem.getItemName()) + " x "
                            + item.getKey().getOutput()[0].getAmount());
                } else {
                    lore.add("  - "
                            + CMIMaterial.get(item.getKey().getOutput()[0].getType())
                                    .getTranslatedName() + " x "
                            + item.getKey().getOutput()[0].getAmount());
                }
            }
            ItemMeta meta = itemStack.getItemMeta();
            meta.getPersistentDataContainer().set(CRAFTING_KEY, PersistentDataType.BOOLEAN, true);
            meta.setLore(CMIChatColor.translate(lore));
            itemStack.setItemMeta(meta);
            menu.addItem(maxSize - 1, itemStack);
        }

        if (cancelButton) {
            menu.addItem(maxSize, MenuItems.CANCEL);
            menu.addMenuClickHandler(maxSize, (player, i1, itemStack, clickAction) -> {
                if (isCancelling) {
                    info.getCraftingSessions().remove(this);
                    player.sendMessage(CMIChatColor.translate("&a&l成功强制取消了合成任务"));
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
        info.getCraftingSessions().add(this);
    }

    public AEMenu getMenu() {
        return menu;
    }
}
