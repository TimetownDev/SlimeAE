package me.ddggdd135.slimeae.core.slimefun.tools;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import java.util.stream.IntStream;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBT;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.iface.ReadWriteNBT;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.iface.ReadableNBT;
import me.ddggdd135.slimeae.api.interfaces.IBlockData;
import me.ddggdd135.slimeae.api.interfaces.IBlockDataAdapter;
import me.ddggdd135.slimeae.api.interfaces.IDataBlock;
import me.ddggdd135.slimeae.api.interfaces.IInventoryBlockData;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class MemoryCard extends SlimefunItem {
    public static final String DATA_KEY = "data";
    public static final String NAMESPACEDKEY_KEY = "namespacedkey";

    public MemoryCard(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        addItemHandler((ItemUseHandler) e -> {
            e.cancel();
            if (e.getClickedBlock().isEmpty()) return;
            Block block = e.getClickedBlock().get();
            SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(block.getLocation());
            if (slimefunBlockData == null) return;
            SlimefunItem slimefunItem = SlimefunItem.getById(slimefunBlockData.getSfId());
            if (slimefunItem == null) return;
            if (!(slimefunItem instanceof IDataBlock iDataBlock)) {
                e.getPlayer().sendMessage(CMIChatColor.translate("&e你确定你对着拥有数据的方块"));
                return;
            }
            BlockMenu blockMenu = slimefunBlockData.getBlockMenu();
            if (blockMenu == null) return;
            if (!Slimefun.getProtectionManager().hasPermission(e.getPlayer(), block, Interaction.INTERACT_BLOCK))
                return;
            IBlockDataAdapter<?> adapter = iDataBlock.getAdapter();
            if (e.getPlayer().isSneaking()) {
                IBlockData blockData = iDataBlock.getData(block.getLocation());
                if (blockData == null) {
                    e.getPlayer().sendMessage(CMIChatColor.translate("&e这个方块没有数据"));
                    return;
                }
                ReadWriteNBT nbt = adapter.serialize(blockData);
                NBT.modify(e.getItem(), x -> {
                    ReadWriteNBT data = x.getOrCreateCompound(DATA_KEY);
                    data.clearNBT();
                    data.mergeCompound(nbt);

                    x.setString(NAMESPACEDKEY_KEY, blockData.getNamespacedKey().asString());
                });

                e.getPlayer().sendMessage(CMIChatColor.translate("&e成功存储了方块设置"));
            } else {
                NBT.get(e.getItem(), x -> {
                    ReadableNBT nbt = x.getCompound(DATA_KEY);
                    if (nbt == null) {
                        e.getPlayer().sendMessage(CMIChatColor.translate("&e你还没有存储数据"));
                        return;
                    }

                    IBlockData data = adapter.deserialize(nbt);

                    if (!iDataBlock.canApplyData(block.getLocation(), data)) {
                        e.getPlayer().sendMessage(CMIChatColor.translate("&e无法应用数据"));
                        return;
                    }

                    if (data instanceof IInventoryBlockData iInventoryBlockData) {
                        ItemStack[] itemStacks = iInventoryBlockData.getItemStacks();
                        if (iInventoryBlockData.needItems()) {
                            if (!ItemUtils.contains(
                                    e.getPlayer().getInventory(),
                                    IntStream.rangeClosed(0, 35).toArray(),
                                    itemStacks)) {
                                e.getPlayer().sendMessage(CMIChatColor.translate("&e你确定你背包里有足够的物品？"));
                                return;
                            }

                            ItemUtils.takeItems(
                                    e.getPlayer().getInventory(),
                                    IntStream.rangeClosed(0, 35).toArray(),
                                    ItemUtils.createRequests(ItemUtils.getAmounts(itemStacks)));
                        }
                    }

                    iDataBlock.applyData(block.getLocation(), data);

                    e.getPlayer().sendMessage(CMIChatColor.translate("&e成功应用了方块设置"));
                });
            }
        });
    }
}
