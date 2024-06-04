package me.ddggdd135.slimeae.core;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.api.AEMenu;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.CraftingRecipe;
import me.ddggdd135.slimeae.api.ItemRequest;
import me.ddggdd135.slimeae.api.ItemStorage;
import me.ddggdd135.slimeae.api.exceptions.NoEnoughMaterialsException;
import me.ddggdd135.slimeae.api.interfaces.IMECraftDevice;
import me.ddggdd135.slimeae.api.interfaces.IMECraftHolder;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.utils.AdvancedCustomItemStack;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.ddggdd135.slimeae.utils.KeyPair;
import net.Zrips.CMILib.Colors.CMIChatColor;
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
    private final List<KeyPair<CraftingRecipe, Integer>> craftingSteps;
    private final ItemStorage itemCache = new ItemStorage();
    private int running = 0;
    private final AEMenu menu = new AEMenu("&e合成任务 - " + "&a&l运行中  " + running);
    private boolean isCancelling = false;

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
    public List<KeyPair<CraftingRecipe, Integer>> getCraftingSteps() {
        return craftingSteps;
    }

    private List<KeyPair<CraftingRecipe, Integer>> match(CraftingRecipe recipe, int count, ItemStorage storage) {
        if (!info.getRecipes().contains(recipe)) throw new NoEnoughMaterialsException();
        List<KeyPair<CraftingRecipe, Integer>> result = new ArrayList<>();
        Map<ItemStack, Integer> in = ItemUtils.getAmounts(recipe.getInput());
        for (ItemStack template : in.keySet()) {
            int amount = storage.getStorage().getOrDefault(template, 0);
            int need = in.get(template) * count;
            if (amount >= need) {
                storage.tryTakeItem(new ItemRequest(template, need));
            } else if (amount == 0) {
                // 计划合成
                CraftingRecipe craftingRecipe = getRecipe(template);
                if (craftingRecipe == null) throw new NoEnoughMaterialsException();
                Map<ItemStack, Integer> output = ItemUtils.getAmounts(craftingRecipe.getOutput());
                Map<ItemStack, Integer> input = ItemUtils.getAmounts(craftingRecipe.getInput());
                // 计算需要合成多少次
                int out = output.get(template) - input.getOrDefault(template, 0);
                int countToCraft = (int) Math.ceil(need / (double) out);
                result.addAll(match(craftingRecipe, countToCraft, storage));
            } else {
                storage.tryTakeItem(new ItemRequest(template, amount));
                // 计算还需要多少
                need -= amount;
                // 计划合成
                CraftingRecipe craftingRecipe = getRecipe(template);
                if (craftingRecipe == null) throw new NoEnoughMaterialsException();
                Map<ItemStack, Integer> output = ItemUtils.getAmounts(craftingRecipe.getOutput());
                Map<ItemStack, Integer> input = ItemUtils.getAmounts(craftingRecipe.getInput());
                // 计算需要合成多少次
                int out = output.get(template) - input.getOrDefault(template, 0);
                int countToCraft = (int) Math.ceil(need / (double) out);
                result.addAll(match(craftingRecipe, countToCraft, storage));
            }
        }
        result.add(new KeyPair<>(recipe, count));
        return result;
    }

    @Nullable private CraftingRecipe getRecipe(@Nonnull ItemStack itemStack) {
        return info.getRecipeFor(itemStack);
    }

    public boolean hasNext() {
        return !craftingSteps.isEmpty();
    }

    public void moveNext(int maxDevices) {
        if (!hasNext()) return;
        KeyPair<CraftingRecipe, Integer> next = craftingSteps.get(0);
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
                .map(x -> x.getKey())
                .toArray(Location[]::new);
        int allocated = 0;
        IStorage networkStorage = info.getStorage();
        if (!networkStorage.contains(ItemUtils.createRequests(
                        ItemUtils.getAmounts(next.getKey().getInput())))
                && running <= 0) {
            // 合成出现错误 重新规划
            info.getCraftingSessions().remove(this);
            new AutoCraftingSession(
                    info, recipe, craftingSteps.get(craftingSteps.size() - 1).getValue());
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
        List<KeyPair<CraftingRecipe, Integer>> process = getCraftingSteps();
        List<KeyPair<CraftingRecipe, Integer>> process2 = getCraftingSteps();
        if (process.size() > maxSize - 1) {
            process2 = process.subList(maxSize, process.size());
            process = process.subList(0, maxSize);
        }
        for (int i = 0; i < maxSize; i++) {
            menu.replaceExistingItem(i, null);
            menu.addMenuClickHandler(i, ChestMenuUtils.getEmptyClickHandler());
        }
        int i = 0;
        for (KeyPair<CraftingRecipe, Integer> item : process) {
            ItemStack[] itemStacks = item.getKey().getOutput();
            ItemStack itemStack;
            if (itemStacks.length == 1) {
                itemStack = itemStacks[0].clone();
            } else {
                itemStack = new AdvancedCustomItemStack(
                        Material.BARREL,
                        "&e&l多物品",
                        Arrays.stream(itemStacks)
                                .map(x -> {
                                    SlimefunItem slimefunItem = SlimefunItem.getByItem(x);
                                    if (slimefunItem != null) {
                                        return "  - " + CMIChatColor.stripColor(slimefunItem.getItemName()) + " x "
                                                + x.getAmount();
                                    } else {
                                        return "  - "
                                                + CMIMaterial.get(x.getType()).getTranslatedName() + " x "
                                                + x.getAmount();
                                    }
                                })
                                .toArray(String[]::new));
                itemStack.setAmount(Math.min(64, item.getValue()));
            }
            ItemMeta meta = itemStack.getItemMeta();
            meta.getPersistentDataContainer().set(CRAFTING_KEY, PersistentDataType.BOOLEAN, true);
            itemStack.setItemMeta(meta);
            menu.addItem(i, itemStack);
            menu.addMenuClickHandler(i, ChestMenuUtils.getEmptyClickHandler());
            i++;
        }
        if (!process2.isEmpty()) {
            ItemStack itemStack = new AdvancedCustomItemStack(Material.BARREL, "&e&l省略" + process2.size() + "项");
            List<String> lore = new ArrayList<>();
            for (KeyPair<CraftingRecipe, Integer> item : process2) {
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
            menu.addItem(maxSize, itemStack);
        }

        if (cancelButton) {
            menu.replaceExistingItem(maxSize, MenuItems.CANCEL);
            menu.addMenuClickHandler(maxSize, (player, i1, itemStack, clickAction) -> {
                isCancelling = true;
                player.sendMessage(CMIChatColor.translate("&a&l成功取消了合成任务"));
                player.closeInventory();
                return false;
            });
        }
        // build inventory
        menu.getContents();
        menu.reset(true);
    }

    public void start() {
        if (!SlimeAEPlugin.getNetworkData().AllNetworkData.contains(info)) return;
        info.getCraftingSessions().add(this);
    }

    public AEMenu getMenu() {
        return menu;
    }
}
