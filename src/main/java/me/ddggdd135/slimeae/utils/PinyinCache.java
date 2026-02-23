package me.ddggdd135.slimeae.utils;

import com.github.houbb.pinyin.constant.enums.PinyinStyleEnum;
import com.github.houbb.pinyin.util.PinyinHelper;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;

/**
 * 拼音转换结果全局缓存
 * 物品名称 → 拼音结果映射是确定性的，可以永久缓存。
 * 在 Minecraft 场景下，物品名称是有限集合，缓存命中率接近 100%。
 */
public final class PinyinCache {
    private static final ConcurrentHashMap<String, String> pinyinFullCache = new ConcurrentHashMap<>(512);
    private static final ConcurrentHashMap<String, String> pinyinFirstLetterCache = new ConcurrentHashMap<>(512);

    private PinyinCache() {}

    /**
     * 获取完整拼音（带缓存）
     *
     * @param text 要转换的文本
     * @return 完整拼音字符串
     */
    @Nonnull
    public static String toPinyinFull(@Nonnull String text) {
        return pinyinFullCache.computeIfAbsent(text,
            k -> PinyinHelper.toPinyin(k, PinyinStyleEnum.INPUT, ""));
    }

    /**
     * 获取拼音首字母（带缓存）
     *
     * @param text 要转换的文本
     * @return 拼音首字母字符串
     */
    @Nonnull
    public static String toPinyinFirstLetter(@Nonnull String text) {
        return pinyinFirstLetterCache.computeIfAbsent(text,
            k -> PinyinHelper.toPinyin(k, PinyinStyleEnum.FIRST_LETTER, ""));
    }

    /**
     * 清除所有缓存
     */
    public static void clearCache() {
        pinyinFullCache.clear();
        pinyinFirstLetterCache.clear();
    }
}
