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
import java.util.List;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBT;
import me.ddggdd135.slimeae.api.abstracts.MEBus;
import me.ddggdd135.slimeae.core.slimefun.MEExportBus;
import me.ddggdd135.slimeae.core.slimefun.MEInterface;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

public class MemoryCard extends SlimefunItem {
    public static final String DIRECTION_KEY = "direction";
    public static final String OUTPUTS_KEY = "outputs";

    public MemoryCard(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        addItemHandler((ItemUseHandler) e -> {
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
                e.getPlayer().sendMessage(CMIChatColor.translate("&e成功存储了方块设置"));
            } else {
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

                        for (int i = 0; i < meExportBus.getSettingSlots().length; i++) {
                            blockMenu.replaceExistingItem(meExportBus.getSettingSlots()[i], itemStacks[i]);
                        }
                    }
                }
                if (slimefunItem instanceof MEInterface meInterface) {
                    ItemStack[] itemStacks = NBT.get(e.getItem(), x -> {
                        return x.getItemStackArray(OUTPUTS_KEY);
                    });

                    if (itemStacks == null) return;

                    for (int i = 0; i < meInterface.getSettingSlots().length; i++) {
                        blockMenu.replaceExistingItem(meInterface.getSettingSlots()[i], itemStacks[i]);
                    }
                }
                e.getPlayer().sendMessage(CMIChatColor.translate("&e成功应用了方块设置"));
            }

            e.cancel();
        });
    }
}
