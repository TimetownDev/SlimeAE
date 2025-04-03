package me.ddggdd135.slimeae.utils;

import java.util.Comparator;
import me.ddggdd135.slimeae.api.autocraft.AutoCraftingTask;
import org.bukkit.Location;

public class ComparatorUtils {
    public static final Comparator<Location> LOCATION_COMPARATOR = (o1, o2) -> {
        int var1 = o1.getBlockX()
                ^ o1.getBlockY()
                ^ o1.getBlockZ()
                ^ o1.getWorld().getUID().hashCode();
        int var2 = o2.getBlockX()
                ^ o2.getBlockY()
                ^ o2.getBlockZ()
                ^ o2.getWorld().getUID().hashCode();
        return var1 - var2;
    };
    public static final Comparator<AutoCraftingTask> AUTO_CRAFTING_SESSION_COMPARATOR = (o1, o2) -> {
        int hash1 = o1.hashCode();
        int hash2 = o2.hashCode();
        return hash1 - hash2;
    };
}
