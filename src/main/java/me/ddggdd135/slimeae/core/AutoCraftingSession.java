package me.ddggdd135.slimeae.core;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectMutablePair;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.slimeae.api.CraftingRecipe;
import me.ddggdd135.slimeae.api.ItemRequest;
import me.ddggdd135.slimeae.api.ItemStorage;
import me.ddggdd135.slimeae.api.exceptions.NoEnoughMaterialsException;
import me.ddggdd135.slimeae.api.interfaces.IMECraftDevice;
import me.ddggdd135.slimeae.api.interfaces.IMECraftHolder;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.ddggdd135.slimeae.utils.KeyPair;
import me.ddggdd135.slimeae.utils.RecipeUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class AutoCraftingSession {
    private final CraftingRecipe recipe;
    private final NetworkInfo info;
    private final int count;
    private List<KeyPair<CraftingRecipe, Integer>> craftingSteps;
    private final ItemStorage itemCache = new ItemStorage();
    private int running = 0;

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
        // if (!info.getRecipes().contains(recipe)) throw new NoEnoughMaterialsException();
        List<KeyPair<CraftingRecipe, Integer>> result = new ArrayList<>();
        Map<ItemStack, Integer> input = ItemUtils.getAmounts(recipe.getInput());
        for (ItemStack template : input.keySet()) {
            int amount = storage.getStorage().getOrDefault(template, 0);
            int need = input.get(template) * count;
            if (amount >= need) {
                storage.tryTakeItem(new ItemRequest(template, need));
            } else if (amount == 0) {
                // 计划合成
                CraftingRecipe craftingRecipe = getRecipe(template);
                if (craftingRecipe == null) throw new NoEnoughMaterialsException();
                // 计算需要合成多少次
                int out = ItemUtils.getAmounts(craftingRecipe.getOutput()).get(template)
                        - input.getOrDefault(template, 0);
                int countToCraft = (int) Math.ceil(need / (double) out);
                result.addAll(match(craftingRecipe, countToCraft, storage));
            } else {
                storage.tryTakeItem(new ItemRequest(template, amount));
                // 计算还需要多少
                need -= amount;
                // 计划合成
                CraftingRecipe craftingRecipe = getRecipe(template);
                if (craftingRecipe == null) throw new NoEnoughMaterialsException();
                // 计算需要合成多少次
                int out = ItemUtils.getAmounts(craftingRecipe.getOutput()).get(template)
                        - input.getOrDefault(template, 0);
                int countToCraft = (int) Math.ceil(need / (double) out);
                result.addAll(match(craftingRecipe, countToCraft, storage));
            }
        }
        result.add(new KeyPair<>(recipe, count));
        return result;
    }

    @Nullable private CraftingRecipe getRecipe(@Nonnull ItemStack itemStack) {
        return RecipeUtils.getRecipe(itemStack);
    }

    public boolean hasNext() {
        return !craftingSteps.isEmpty();
    }

    public void moveNext(int maxDevices) {
        if (!hasNext()) return;
        KeyPair<CraftingRecipe, Integer> next = craftingSteps.get(0);
        boolean doCraft = true;
        if (next.getValue() <= 0) {
            if (running <= 0) {
                craftingSteps.remove(0);
                return;
            }

            doCraft = false;
        }
        Location[] locations = info.getRecipeMap().entrySet().stream()
                //.filter(x -> x.getValue().contains(next.key()))
                .map(x -> x.getKey())
                .toArray(Location[]::new);
        int allocated = 0;
        IStorage networkStorage = info.getStorage();
        for (Location location : locations) {
            IMECraftHolder holder = (IMECraftHolder)
                    SlimefunItem.getById(StorageCacheUtils.getBlock(location).getSfId());
            for (Block deviceBlock : holder.getCraftingDevices(location.getBlock())) {
                IMECraftDevice device = (IMECraftDevice) SlimefunItem.getById(
                        StorageCacheUtils.getBlock(deviceBlock.getLocation()).getSfId());
                if (!device.isSupport(deviceBlock, next.getKey())) continue;
                if (allocated > maxDevices) return;
                if (doCraft && device.canStartCrafting(deviceBlock, next.getKey())
                        && networkStorage.contains(ItemUtils.createRequests(
                                ItemUtils.getAmounts(next.getKey().getInput())))) {
                    networkStorage.tryTakeItem(ItemUtils.createRequests(
                            ItemUtils.getAmounts(next.getKey().getInput())));
                    device.startCrafting(deviceBlock, next.getKey());
                    running++;
                    next.setValue(next.getValue() - 1);
                }
                if (device.isFinished(deviceBlock)
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
    }
}
