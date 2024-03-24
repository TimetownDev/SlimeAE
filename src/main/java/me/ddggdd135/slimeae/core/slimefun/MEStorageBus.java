package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.HashMap;
import java.util.Map;
import me.ddggdd135.slimeae.api.ItemRequest;
import me.ddggdd135.slimeae.api.ItemStorage;
import me.ddggdd135.slimeae.api.interfaces.IMEStorageObject;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.api.interfaces.MEBus;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MEStorageBus extends MEBus implements IMEStorageObject {
    public MEStorageBus(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public void onNetworkUpdate(Block block, NetworkInfo networkInfo) {}

    @Override
    public int[] getInputSlots() {
        return new int[0];
    }

    @Override
    public int[] getOutputSlots() {
        return new int[0];
    }

    @Nullable @Override
    public IStorage getStorage(Block block) {
        BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
        BlockFace blockFace = getDirection(inv);
        if (blockFace == BlockFace.SELF) return null;
        Block b = block.getRelative(blockFace);
        if (b.getBlockData().getMaterial().isAir()) return null;
        if (StorageCacheUtils.getMenu(b.getLocation()) != null) {
            int[] outputSlots = StorageCacheUtils.getMenu(b.getLocation())
                    .getPreset()
                    .getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            if (outputSlots == null || outputSlots.length == 0) return null;
            return new IStorage() {
                @Override
                public void pushItem(@NotNull @NonNull ItemStack[] itemStacks) {}

                @Override
                public boolean contains(ItemRequest[] requests) {
                    BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
                    if (inv == null) return false;
                    return ItemUtils.contains(getStorage(), requests);
                }

                @Override
                public ItemStack[] tryTakeItem(ItemRequest[] requests) {
                    BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
                    if (inv == null) return new ItemStack[0];
                    Map<ItemStack, Integer> amounts = ItemUtils.getAmounts(ItemUtils.createItems(requests));
                    ItemStorage found = new ItemStorage();

                    for (ItemStack itemStack : amounts.keySet()) {
                        for (int slot : outputSlots) {
                            ItemStack item = inv.getItemInSlot(slot);
                            if (item == null || item.getType().isAir()) continue;
                            if (SlimefunUtils.isItemSimilar(item, itemStack, true, false)) {
                                if (item.getAmount() > amounts.get(itemStack)) {
                                    found.addItem(ItemUtils.createItems(itemStack, amounts.get(itemStack)));
                                    int rest = item.getAmount() - amounts.get(itemStack);
                                    item.setAmount(rest);
                                    break;
                                } else {
                                    found.addItem(ItemUtils.createItems(itemStack, item.getAmount()));
                                    inv.replaceExistingItem(slot, new ItemStack(Material.AIR));
                                    int rest = amounts.get(itemStack) - item.getAmount();
                                    if (rest != 0) amounts.put(itemStack, rest);
                                    else {
                                        amounts.remove(itemStack);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    return found.toItemStacks();
                }

                @Override
                public @NotNull Map<ItemStack, Integer> getStorage() {
                    BlockMenu inv = StorageCacheUtils.getMenu(block.getLocation());
                    if (inv == null) return new HashMap<>();
                    return ItemUtils.getAmounts(inv.getContents());
                }

                @Override
                public int getEmptySlots() {
                    return 0;
                }
            };
        } else if (b.getState() instanceof Container) {
            return new IStorage() {
                @Override
                public void pushItem(@NotNull @NonNull ItemStack[] itemStacks) {}

                @Override
                public boolean contains(ItemRequest[] requests) {
                    return ItemUtils.contains(getStorage(), requests);
                }

                @Override
                public ItemStack[] tryTakeItem(ItemRequest[] requests) {
                    Container container = (Container) b.getState();
                    ItemStack[] items = new ItemStack[0];
                    if (container instanceof Furnace furnace) {
                        items = new ItemStack[] {furnace.getInventory().getResult()};
                    } else if (container instanceof Chest chest) {
                        items = chest.getInventory().getContents();
                    }
                    Map<ItemStack, Integer> amounts = ItemUtils.getAmounts(ItemUtils.createItems(requests));
                    ItemStorage found = new ItemStorage();

                    for (ItemStack itemStack : amounts.keySet()) {
                        for (ItemStack item : items) {
                            if (item == null || item.getType().isAir()) continue;
                            if (SlimefunUtils.isItemSimilar(item, itemStack, true, false)) {
                                if (item.getAmount() > amounts.get(itemStack)) {
                                    found.addItem(ItemUtils.createItems(itemStack, amounts.get(itemStack)));
                                    int rest = item.getAmount() - amounts.get(itemStack);
                                    item.setAmount(rest);
                                    break;
                                } else {
                                    found.addItem(ItemUtils.createItems(itemStack, item.getAmount()));
                                    int rest = amounts.get(itemStack) - item.getAmount();
                                    item.setAmount(0);
                                    if (rest != 0) amounts.put(itemStack, rest);
                                    else {
                                        amounts.remove(itemStack);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (container instanceof Furnace furnace) {
                        furnace.getInventory().setResult(items[0]);
                    } else if (container instanceof Chest chest) {
                        chest.getInventory().setContents(items);
                    }
                    container.update();
                    return found.toItemStacks();
                }

                @Override
                public @NotNull Map<ItemStack, Integer> getStorage() {
                    Container container = (Container) b.getState();
                    ItemStack[] items = new ItemStack[0];
                    if (container instanceof Furnace furnace) {
                        items = new ItemStack[] {furnace.getInventory().getResult()};
                    } else if (container instanceof Chest chest) {
                        items = chest.getInventory().getContents();
                    }
                    return ItemUtils.getAmounts(items);
                }

                @Override
                public int getEmptySlots() {
                    return 0;
                }
            };
        }
        return null;
    }
}
