package me.ddggdd135.slimeae.utils;

import java.util.Map;
import javax.annotation.Nonnull;

public class MapUtils {
    public static <K, V> boolean areMapsEqual(@Nonnull Map<K, V> map1, @Nonnull Map<K, V> map2) {
        if (map1.size() != map2.size()) {
            return false;
        }

        for (K key : map1.keySet()) {
            if (!map2.containsKey(key) || !map1.get(key).equals(map2.get(key))) {
                return false;
            }
        }

        return true;
    }
}
