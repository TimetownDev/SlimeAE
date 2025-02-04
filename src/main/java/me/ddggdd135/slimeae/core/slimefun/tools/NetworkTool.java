package me.ddggdd135.slimeae.core.slimefun.tools;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.*;
import me.ddggdd135.guguslimefunlib.api.AEMenu;
import me.ddggdd135.guguslimefunlib.items.AdvancedCustomItemStack;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBT;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.utils.BlockUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NetworkTool extends SlimefunItem {
    public static final String NETWORK_INFO_KEY = "network_info";

    public NetworkTool(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        addItemHandler((ItemUseHandler) e -> {
            e.cancel();
            if (e.getClickedBlock().isEmpty()) return;
            Block block = e.getClickedBlock().get();
            SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(block.getLocation());
            if (slimefunBlockData == null) return;
            if (e.getPlayer().isSneaking()) {
                if (!Wrench.SUPPORTED_BLOCK_ID.contains(slimefunBlockData.getSfId())) return;

                BlockUtils.breakBlock(block, e.getPlayer());
                return;
            }

            NetworkInfo networkInfo = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
            if (networkInfo == null) {
                e.getPlayer().sendMessage(CMIChatColor.translate("&e你确定你对着的方块在AE网络中？"));
                return;
            }

            Map<SlimefunItem, Integer> amount = new HashMap<>();
            for (Location location : networkInfo.getChildren()) {
                SlimefunItem slimefunItem = (SlimefunItem)
                        SlimeAEPlugin.getNetworkData().AllNetworkBlocks.get(location);
                if (slimefunItem == null) continue;
                if (!amount.containsKey(slimefunItem)) {
                    amount.put(slimefunItem, 1);
                } else {
                    amount.put(slimefunItem, amount.get(slimefunItem) + 1);
                }
            }

            List<Map.Entry<SlimefunItem, Integer>> sorted = new ArrayList<>(amount.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .toList());
            Collections.reverse(sorted);

            AEMenu menu = new AEMenu("&eAE网络信息");
            int maxSize = 54;
            menu.setSize(maxSize);
            List<Map.Entry<SlimefunItem, Integer>> amount1 = sorted;
            List<Map.Entry<SlimefunItem, Integer>> amount2 = new ArrayList<>();
            if (amount1.size() > maxSize - 1) {
                amount2 = amount1.subList(maxSize, amount1.size());
                amount1 = amount1.subList(0, maxSize);
            }
            for (int i = 0; i < maxSize; i++) {
                menu.replaceExistingItem(i, null);
                menu.addMenuClickHandler(i, ChestMenuUtils.getEmptyClickHandler());
            }
            for (int i = 0; i < amount1.size(); i++) {
                Map.Entry<SlimefunItem, Integer> data = amount1.get(i);
                ItemStack itemStack = data.getKey().getItem().clone();

                ItemMeta meta = itemStack.getItemMeta();
                List<String> lore = meta.getLore();
                if (lore == null) lore = new ArrayList<>();
                lore.add("");
                lore.add("&a存在 &e" + data.getValue() + "&a个");
                meta.setLore(CMIChatColor.translate(lore));
                itemStack.setItemMeta(meta);
                NBT.modify(itemStack, x -> {
                    x.setBoolean(NETWORK_INFO_KEY, true);
                });
                menu.addItem(i, itemStack);
                menu.addMenuClickHandler(i, ChestMenuUtils.getEmptyClickHandler());
            }
            if (!amount2.isEmpty()) {
                ItemStack itemStack = new AdvancedCustomItemStack(Material.BARREL, "&e&l省略" + amount2.size() + "项");
                List<String> lore = new ArrayList<>();
                for (Map.Entry<SlimefunItem, Integer> data : amount2) {
                    lore.add("  - " + CMIChatColor.stripColor(data.getKey().getItemName()) + " x " + data.getValue());
                }
                ItemMeta meta = itemStack.getItemMeta();
                meta.setLore(CMIChatColor.translate(lore));
                itemStack.setItemMeta(meta);
                NBT.modify(itemStack, x -> {
                    x.setBoolean(NETWORK_INFO_KEY, true);
                });
                menu.addItem(maxSize - 1, itemStack);
            }

            menu.open(e.getPlayer());
        });
    }
}
