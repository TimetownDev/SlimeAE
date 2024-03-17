package me.ddggdd135.slimeae.utils;

import net.Zrips.CMILib.Colors.CMIChatColor;

import java.util.ArrayList;
import java.util.List;

public class ColorUtils {
    public static String[] translateAll(String... strings) {
        List<String> result = new ArrayList<>();
        for (String string : strings) {
            result.add(CMIChatColor.translate(string));
        }

        return result.toArray(new String[0]);
    }
}
