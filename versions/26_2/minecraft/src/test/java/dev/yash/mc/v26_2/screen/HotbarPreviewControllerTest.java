package dev.yash.mc.v26_2.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.yash.core.animation.AnimationPreset;
import dev.yash.core.config.YashConfig;
import org.junit.jupiter.api.Test;

final class HotbarPreviewControllerTest {
    private static final double EPSILON = 1.0e-9;

    @Test
    void clickingAnySlotSwitchesToManualAndRetargetsImmediately() {
        HotbarPreviewController controller = new HotbarPreviewController(YashConfig.defaults(), 0.0);
        controller.setMode(PreviewMode.CYCLE, 0.0);

        controller.selectSlot(8, 0.01);
        var sample = controller.sample(0.01);

        assertEquals(PreviewMode.MANUAL, controller.mode());
        assertEquals(8, controller.selectedSlot());
        assertEquals(8, sample.frame().targetSlot());
        assertTrue(sample.frame().animating());
    }

    @Test
    void autoCycleAdvancesAtAStableTimeInterval() {
        HotbarPreviewController controller = new HotbarPreviewController(YashConfig.defaults(), 0.0);
        controller.setMode(PreviewMode.CYCLE, 0.0);

        assertEquals(4, controller.sample(0.649).frame().targetSlot());
        assertEquals(5, controller.sample(0.650).frame().targetSlot());
        assertEquals(6, controller.sample(1.300).frame().targetSlot());
    }

    @Test
    void edgeScenarioAlternatesFirstAndNinthSlots() {
        HotbarPreviewController controller = new HotbarPreviewController(YashConfig.defaults(), 0.0);

        controller.setMode(PreviewMode.EDGES, 0.0);
        assertEquals(0, controller.sample(0.0).frame().targetSlot());
        assertEquals(8, controller.sample(0.65).frame().targetSlot());
        assertEquals(0, controller.sample(1.30).frame().targetSlot());
    }

    @Test
    void rapidScenarioUsesDocumentedNonQueuedSequence() {
        HotbarPreviewController controller = new HotbarPreviewController(YashConfig.defaults(), 0.0);

        controller.setMode(PreviewMode.RAPID, 0.0);
        assertEquals(0, controller.sample(0.0).frame().targetSlot());
        assertEquals(4, controller.sample(0.12).frame().targetSlot());
        assertEquals(7, controller.sample(0.24).frame().targetSlot());
        assertEquals(2, controller.sample(0.36).frame().targetSlot());
    }

    @Test
    void liveConfigChangePreservesCurrentVisualPosition() {
        HotbarPreviewController controller = new HotbarPreviewController(YashConfig.defaults(), 0.0);
        controller.selectSlot(8, 0.0);
        double before = controller.sample(0.04).frame().position();

        YashConfig slower = YashConfig.defaults().withPreset(AnimationPreset.SMOOTH);
        controller.setConfig(slower, 0.04);
        double after = controller.sample(0.04).frame().position();

        assertEquals(before, after, EPSILON);
    }

    @Test
    void disabledPresetStillPreviewsTheSelectedThemeAtTheTargetSlot() {
        YashConfig disabled = YashConfig.defaults().withPreset(AnimationPreset.DISABLED);
        HotbarPreviewController controller = new HotbarPreviewController(disabled, 0.0);

        controller.selectSlot(1, 0.0);
        var sample = controller.sample(0.0);

        assertEquals(1.0, sample.frame().position(), EPSILON);
        assertEquals(disabled.theme(), sample.config().theme());
    }
}
