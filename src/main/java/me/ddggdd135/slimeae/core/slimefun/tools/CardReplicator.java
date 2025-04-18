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
import me.ddggdd135.guguslimefunlib.api.ItemHashSet;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBT;
import me.ddggdd135.slimeae.api.interfaces.*;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class CardReplicator extends SlimefunItem {
    public static final String SIZE_KEY = "size";
    public static final String CARDS_KEY = "cards";

    public CardReplicator(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        addItemHandler((ItemUseHandler) e -> {
            e.cancel();
            if (e.getClickedBlock().isEmpty()) return;
            Block block = e.getClickedBlock().get();
            SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(block.getLocation());
            if (slimefunBlockData == null) return;
            SlimefunItem slimefunItem = SlimefunItem.getById(slimefunBlockData.getSfId());
            if (slimefunItem == null) return;
            if (!(slimefunItem instanceof ICardHolder iCardHolder)) {
                e.getPlayer().sendMessage(CMIChatColor.translate("&e你确定你对着拥有卡槽的方块"));
                return;
            }
            BlockMenu blockMenu = slimefunBlockData.getBlockMenu();
            if (blockMenu == null) return;
            if (!Slimefun.getProtectionManager().hasPermission(e.getPlayer(), block, Interaction.INTERACT_BLOCK))
                return;
            int[] slots = iCardHolder.getCardSlots();
            if (e.getPlayer().isSneaking()) {
                ItemStack[] cards = new ItemStack[slots.length];
                for (int i = 0; i < slots.length; i++) {
                    cards[i] = ItemUtils.getSettingItem(blockMenu.getInventory(), slots[i]);
                }
                NBT.modify(e.getItem(), x -> {
                    x.setInteger(SIZE_KEY, slots.length);
                    x.setItemStackArray(CARDS_KEY, cards);
                });

                e.getPlayer().sendMessage(CMIChatColor.translate("&e成功存储了升级卡设置"));
            } else {
                NBT.get(e.getItem(), x -> {
                    if (!x.hasTag(SIZE_KEY)) {
                        e.getPlayer().sendMessage(CMIChatColor.translate("&e你还没有存储数据"));
                        return;
                    }
                    int size = x.getInteger(SIZE_KEY);
                    if (slots.length != size) {
                        e.getPlayer().sendMessage(CMIChatColor.translate("&e升级卡卡槽大小和存储的不匹配"));
                        return;
                    }
                    ItemHashSet placeHolders = new ItemHashSet();
                    placeHolders.add(MenuItems.CARD);
                    ItemStack[] cards = x.getItemStackArray(CARDS_KEY);
                    ItemStack[] need = ItemUtils.removeAll(cards, placeHolders);
                    if (!ItemUtils.contains(
                            e.getPlayer().getInventory(),
                            IntStream.rangeClosed(0, 35).toArray(),
                            need)) {
                        e.getPlayer().sendMessage(CMIChatColor.translate("&e你确定你背包里有足够的物品？"));
                        return;
                    }

                    ItemUtils.takeItems(
                            e.getPlayer().getInventory(),
                            IntStream.rangeClosed(0, 35).toArray(),
                            ItemUtils.createRequests(ItemUtils.getAmounts(need)));

                    iCardHolder.dropCards(blockMenu);

                    for (int i = 0; i < size; i++) {
                        blockMenu.replaceExistingItem(slots[i], cards[i]);
                    }

                    ICardHolder.updateCache(block, iCardHolder, slimefunBlockData);

                    e.getPlayer().sendMessage(CMIChatColor.translate("&e成功应用了升级卡设置"));
                });
            }
        });
    }
}
