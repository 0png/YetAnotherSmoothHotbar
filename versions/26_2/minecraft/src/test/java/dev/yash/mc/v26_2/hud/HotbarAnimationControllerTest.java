package dev.yash.mc.v26_2.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.yash.core.animation.AnimationFrame;
import dev.yash.core.animation.AnimationPreset;
import dev.yash.core.animation.WrapMode;
import dev.yash.core.config.YashConfig;
import org.junit.jupiter.api.Test;

final class HotbarAnimationControllerTest {
    private static final double EPSILON = 1.0e-9;

    private final Object level = new Object();
    private final Object player = new Object();

    @Test
    void selectedSlotStateAloneDrivesEveryTransition() {
        HotbarAnimationController controller = controller();
        AnimationFrame initial = update(controller, 0, 0.0);

        AnimationFrame towardFive = update(controller, 4, 0.010);
        AnimationFrame moving = update(controller, 4, 0.040);
        AnimationFrame towardEight = update(controller, 7, 0.040);

        assertTrue(controller.config().enabled());
        assertFalse(initial.animating());
        assertTrue(towardFive.animating());
        assertEquals(moving.position(), towardEight.position(), EPSILON);
        assertEquals(7, towardEight.targetSlot());
        assertEquals(2L, towardEight.transitionId());
        assertFalse(controller.lastUpdateReset());
    }

    @Test
    void disablingImmediatelyHandsRenderingBackToVanillaAndReenableResets() {
        HotbarAnimationController controller = controller();
        update(controller, 0, 0.0);
        update(controller, 5, 0.020);

        controller.setConfig(controller.config().withEnabled(false));
        assertNull(update(controller, 5, 0.030));

        controller.setConfig(controller.config().withEnabled(true));
        AnimationFrame restored = update(controller, 5, 0.040);
        assertEquals(5.0, restored.position(), EPSILON);
        assertFalse(restored.animating());
        assertTrue(controller.lastUpdateReset());
    }

    @Test
    void changingAnimationPolicyWhileMovingKeepsTheVisiblePosition() {
        HotbarAnimationController controller = controller();
        update(controller, 0, 0.0);
        update(controller, 8, 0.0);
        AnimationFrame before = update(controller, 8, 0.040);

        controller.setConfig(controller.config().withPreset(AnimationPreset.SMOOTH));
        AnimationFrame after = update(controller, 8, 0.040);

        assertEquals(before.position(), after.position(), EPSILON);
        assertTrue(after.animating());
        assertFalse(controller.lastUpdateReset());
    }

    @Test
    void worldPlayerDeathAndCoordinateSpaceChangesResetSafely() {
        HotbarAnimationController controller = controller();
        update(controller, 3, 0.0);

        assertReset(controller, controller.update(
                3, 0.010, new Object(), player, true, 320, 180, 2));
        assertReset(controller, controller.update(
                3, 0.020, level, new Object(), true, 320, 180, 2));
        assertReset(controller, controller.update(
                3, 0.030, level, player, false, 320, 180, 2));
        assertReset(controller, controller.update(
                3, 0.040, level, player, true, 640, 180, 2));
        assertReset(controller, controller.update(
                3, 0.050, level, player, true, 320, 360, 2));
        assertReset(controller, controller.update(
                3, 0.060, level, player, true, 320, 180, 3));
    }

    @Test
    void explicitLifecycleGapAndClockRollbackResetToTheCurrentSlot() {
        HotbarAnimationController controller = controller();
        update(controller, 0, 5.0);
        update(controller, 8, 5.010);

        controller.resetForLifecycle();
        assertReset(controller, update(controller, 8, 5.020));
        assertReset(controller, update(controller, 2, 7.000));
        assertReset(controller, update(controller, 6, 6.000));
    }

    @Test
    void layoutUsesGuiScaledPixelsAndOnlyDuplicatesDuringWrapSeams() {
        int vanillaSlotSixX = 100 + 6 * HotbarLayout.SLOT_SPACING;
        int baseX = HotbarLayout.selectorBaseX(vanillaSlotSixX, 6);

        assertEquals(100, baseX);
        assertEquals(150, HotbarLayout.selectorX(baseX, 2.5));

        AnimationFrame forwardSeam = frame(8.5, 0, true);
        AnimationFrame reverseSeam = frame(-0.5, 8, true);
        AnimationFrame settled = frame(9.0, 0, false);
        assertEquals(-0.5, HotbarLayout.wrapSeamPosition(forwardSeam), EPSILON);
        assertEquals(-0.5, HotbarLayout.wrapSeamPosition(reverseSeam), EPSILON);
        assertTrue(Double.isNaN(HotbarLayout.wrapSeamPosition(settled)));
    }

    private static HotbarAnimationController controller() {
        return new HotbarAnimationController(YashConfig.defaults());
    }

    private AnimationFrame update(HotbarAnimationController controller, int selectedSlot, double now) {
        return controller.update(selectedSlot, now, level, player, true, 320, 180, 2);
    }

    private static void assertReset(
            HotbarAnimationController controller,
            AnimationFrame frame) {
        assertTrue(controller.lastUpdateReset());
        assertFalse(frame.animating());
        assertEquals(frame.targetSlot(), frame.position(), EPSILON);
    }

    private static AnimationFrame frame(double position, int targetSlot, boolean animating) {
        return new AnimationFrame(
                position,
                0.0,
                targetSlot,
                animating,
                false,
                1L,
                0L,
                WrapMode.WRAP);
    }
}
