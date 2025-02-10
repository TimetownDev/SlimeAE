package me.ddggdd135.slimeae.utils;

public class InfinityLibUtils {
    public static boolean isCraftingBlock(Object o) {
        Class<?> clazz = o.getClass();

        while (true) {
            if (clazz.equals(Object.class)) return false;
            if (clazz.getSimpleName().equals("CraftingBlock")) return true;

            clazz = clazz.getSuperclass();
        }
    }

    public static boolean isCraftingBlockRecipe(Object o) {
        Class<?> clazz = o.getClass();

        while (true) {
            if (clazz.equals(Object.class)) return false;
            if (clazz.getSimpleName().equals("CraftingBlockRecipe")) return true;

            clazz = clazz.getSuperclass();
        }
    }
}
