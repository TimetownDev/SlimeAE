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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBT;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.iface.ReadWriteNBTList;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.iface.ReadableNBTList;
import me.ddggdd135.slimeae.api.abstracts.AdvancedMEBus;
import me.ddggdd135.slimeae.api.abstracts.MEBus;
import me.ddggdd135.slimeae.core.slimefun.MEInterface;
import me.ddggdd135.slimeae.core.slimefun.buses.MEAdvancedExportBus;
import me.ddggdd135.slimeae.core.slimefun.buses.MEExportBus;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

public class MemoryCard extends SlimefunItem {
    public static final String DIRECTION_KEY = "direction";
    public static final String OUTPUTS_KEY = "outputs";
    public static final String TYPE_KEY = "type";

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
            if (!(slimefunItem instanceof MEBus || slimefunItem instanceof MEInterface)) {
                e.getPlayer().sendMessage(CMIChatColor.translate("&e你确定你对着ME总线或ME接口？"));
                return;
            }
            BlockMenu blockMenu = slimefunBlockData.getBlockMenu();
            if (blockMenu == null) return;
            if (!Slimefun.getProtectionManager().hasPermission(e.getPlayer(), block, Interaction.INTERACT_BLOCK))
                return;
            if (e.getPlayer().isSneaking()) {
                if (slimefunItem instanceof AdvancedMEBus advancedMEBus)
                    NBT.modify(e.getItem(), x -> {
                        ReadWriteNBTList<String> directions = x.getStringList(DIRECTION_KEY);
                        directions.clear();
                        for (BlockFace direction : advancedMEBus.getDirections(block.getLocation())) {
                            directions.add(direction.name());
                        }

                        if (advancedMEBus instanceof MEAdvancedExportBus meAdvancedExportBus) {
                            List<ItemStack> items = new ArrayList<>();
                            for (int slot : meAdvancedExportBus.getSettingSlots()) {
                                items.add(blockMenu.getItemInSlot(slot));
                            }
                            x.setItemStackArray(OUTPUTS_KEY, items.toArray(ItemStack[]::new));
                        } else {
                            x.removeKey(OUTPUTS_KEY);
                        }
                    });
                if (slimefunItem instanceof MEBus meBus)
                    NBT.modify(e.getItem(), x -> {
                        x.setEnum(DIRECTION_KEY, meBus.getDirection(blockMenu));
                        if (meBus instanceof MEExportBus meExportBus) {
                            List<ItemStack> items = new ArrayList<>();
                            for (int slot : meExportBus.getSettingSlots()) {
                                items.add(blockMenu.getItemInSlot(slot));
                            }
                            x.setItemStackArray(OUTPUTS_KEY, items.toArray(ItemStack[]::new));
                        } else {
                            x.removeKey(OUTPUTS_KEY);
                        }
                    });
                if (slimefunItem instanceof MEInterface meInterface) {
                    NBT.modify(e.getItem(), x -> {
                        List<ItemStack> items = new ArrayList<>();
                        for (int slot : meInterface.getSettingSlots()) {
                            items.add(blockMenu.getItemInSlot(slot));
                        }
                        x.setItemStackArray(OUTPUTS_KEY, items.toArray(ItemStack[]::new));
                    });
                }
                NBT.modify(e.getItem(), x -> {
                    x.setString(TYPE_KEY, slimefunItem.getId());
                });
                e.getPlayer().sendMessage(CMIChatColor.translate("&e成功存储了方块设置"));
            } else {
                String id = NBT.get(e.getItem(), x -> {
                    return x.getString(TYPE_KEY);
                });
                if (!id.equals(slimefunItem.getId())) return;
                if (slimefunItem instanceof AdvancedMEBus advancedMEBus) {
                    Set<BlockFace> blockFace = NBT.get(e.getItem(), x -> {
                        ReadableNBTList<String> strings = x.getStringList(DIRECTION_KEY);
                        Set<BlockFace> directions = new HashSet<>();
                        for (String direction : strings) {
                            directions.add(BlockFace.valueOf(direction));
                        }

                        return directions;
                    });

                    if (blockFace == null) {
                        e.getPlayer().sendMessage(CMIChatColor.translate("&e你还没有存储方块信息！"));
                        return;
                    }

                    advancedMEBus
                            .getDirections(block.getLocation())
                            .forEach(x -> advancedMEBus.setDirection(blockMenu, x));
                    for (BlockFace direction : blockFace) {
                        advancedMEBus.setDirection(blockMenu, direction);
                    }

                    if (advancedMEBus instanceof MEAdvancedExportBus meAdvancedExportBus) {
                        ItemStack[] itemStacks = NBT.get(e.getItem(), x -> {
                            return x.getItemStackArray(OUTPUTS_KEY);
                        });

                        if (itemStacks == null) return;
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
                        blockMenu.dropItems(block.getLocation(), meAdvancedExportBus.getSettingSlots());
                        for (int i = 0; i < meAdvancedExportBus.getSettingSlots().length; i++) {
                            blockMenu.replaceExistingItem(meAdvancedExportBus.getSettingSlots()[i], itemStacks[i]);
                        }
                    }

                    e.getPlayer().sendMessage(CMIChatColor.translate("&e成功应用了方块设置"));
                    return;
                }
                if (slimefunItem instanceof MEBus meBus) {
                    BlockFace blockFace = NBT.get(e.getItem(), x -> {
                        return x.getEnum(DIRECTION_KEY, BlockFace.class);
                    });

                    if (blockFace == null) {
                        e.getPlayer().sendMessage(CMIChatColor.translate("&e你还没有存储方块信息！"));
                        return;
                    }

                    meBus.setDirection(blockMenu, blockFace);

                    if (meBus instanceof MEExportBus meExportBus) {
                        ItemStack[] itemStacks = NBT.get(e.getItem(), x -> {
                            return x.getItemStackArray(OUTPUTS_KEY);
                        });

                        if (itemStacks == null) return;
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
                        blockMenu.dropItems(block.getLocation(), meExportBus.getSettingSlots());
                        for (int i = 0; i < meExportBus.getSettingSlots().length; i++) {
                            blockMenu.replaceExistingItem(meExportBus.getSettingSlots()[i], itemStacks[i]);
                        }
                    }

                    e.getPlayer().sendMessage(CMIChatColor.translate("&e成功应用了方块设置"));
                    return;
                }
                if (slimefunItem instanceof MEInterface meInterface) {
                    ItemStack[] itemStacks = NBT.get(e.getItem(), x -> {
                        return x.getItemStackArray(OUTPUTS_KEY);
                    });

                    if (itemStacks == null) return;

                    for (int i = 0; i < meInterface.getSettingSlots().length; i++) {
                        blockMenu.replaceExistingItem(meInterface.getSettingSlots()[i], itemStacks[i]);
                    }

                    e.getPlayer().sendMessage(CMIChatColor.translate("&e成功应用了方块设置"));
                    return;
                }
            }
        });
    }
}
