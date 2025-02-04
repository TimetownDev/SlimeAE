package me.ddggdd135.slimeae.core.slimefun.tools;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.LocationUtils;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBT;
import me.ddggdd135.slimeae.core.slimefun.MESecurityTerminal;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class WirelessTerminal extends SlimefunItem {

    public static final String LOCATION_KEY = "location";

    public WirelessTerminal(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        addItemHandler((ItemUseHandler) e -> {
            e.cancel();
            Location location = getBindBlock(e.getItem());
            if (location == null) {
                e.getPlayer().sendMessage(CMIChatColor.translate("&e使用ME安全终端将它绑定到AE网络"));
                return;
            }
            SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(location);
            if (slimefunBlockData == null) return;
            SlimefunItem slimefunItem = SlimefunItem.getById(slimefunBlockData.getSfId());
            if (!(slimefunItem instanceof MESecurityTerminal)) return;
            BlockMenu blockMenu = slimefunBlockData.getBlockMenu();
            if (blockMenu == null) return;
            blockMenu.open(e.getPlayer());
        });
    }

    public static void bindTo(@Nonnull Block block, @Nonnull ItemStack itemStack) {
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(block.getLocation());
        if (slimefunBlockData == null) return;
        SlimefunItem slimefunItem = SlimefunItem.getById(slimefunBlockData.getSfId());
        if (!(slimefunItem instanceof MESecurityTerminal)) return;
        String location = LocationUtils.getLocKey(block.getLocation());
        NBT.modify(itemStack, x -> {
            x.setString(LOCATION_KEY, location);
        });

        itemStack.setLore(CMIChatColor.translate(List.of("", "&e已绑定到" + location)));
    }

    @Nullable public static Location getBindBlock(@Nonnull ItemStack itemStack) {
        return NBT.get(itemStack, x -> {
            return LocationUtils.toLocation(x.getString(LOCATION_KEY));
        });
    }
}
