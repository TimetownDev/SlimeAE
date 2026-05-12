package me.ddggdd135.slimeae.api.autocraft;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class AutoCraftingTaskProgressTest {

    @Test
    void waitingResultDoesNotCountAsGlobalFailure() {
        assertFalse(AutoCraftingTask.shouldIncrementGlobalFail(List.of(AutoCraftingTask.StepProcessResult.WAITING)));
    }

    @Test
    void blockedOnlyResultsCountAsGlobalFailure() {
        assertTrue(AutoCraftingTask.shouldIncrementGlobalFail(List.of(AutoCraftingTask.StepProcessResult.BLOCKED)));
    }
}
