package me.ddggdd135.slimeae.core.slimefun;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class MECellWorkbenchTest {

    @Test
    void filterDisplayLoreHasWorkbenchMarker() {
        List<String> lore = MECellWorkbench.createFilterDisplayLore(List.of("原始描述"));

        assertIterableEquals(List.of("原始描述", MECellWorkbench.FILTER_DISPLAY_LORE), lore);
    }

    @Test
    void filterDisplayLoreKeepsSingleWorkbenchMarker() {
        List<String> lore = MECellWorkbench.createFilterDisplayLore(List.of(MECellWorkbench.FILTER_DISPLAY_LORE));

        assertIterableEquals(List.of(MECellWorkbench.FILTER_DISPLAY_LORE), lore);
        assertTrue(lore.contains(MECellWorkbench.FILTER_DISPLAY_LORE));
    }
}
