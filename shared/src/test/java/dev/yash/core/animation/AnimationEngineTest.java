package dev.yash.core.animation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

final class AnimationEngineTest {
    private static final double EPSILON = 1.0e-9;

    @Test
    void firstUpdateInitializesWithoutAnimatingOrPulsing() {
        AnimationEngine engine = engine(AnimationPreset.BEDROCK_INSPIRED, WrapMode.ACROSS);

        AnimationFrame frame = engine.update(3, 10.0);

        assertEquals(3.0, frame.position(), EPSILON);
        assertEquals(3, frame.targetSlot());
        assertFalse(frame.animating());
        assertFalse(frame.arrived());
        assertEquals(0L, frame.transitionId());
        assertEquals(0L, frame.arrivalId());
    }

    @ParameterizedTest
    @ValueSource(ints = {30, 60, 144, 240})
    void completionTimeIsIndependentOfFrameRate(int framesPerSecond) {
        AnimationConfig config = AnimationPreset.BEDROCK_INSPIRED.config(WrapMode.ACROSS);
        AnimationEngine engine = new AnimationEngine(config);
        engine.reset(0, 0.0);
        engine.update(8, 0.0);

        double frameDuration = 1.0 / framesPerSecond;
        double now = 0.0;
        AnimationFrame frame;
        do {
            now += frameDuration;
            frame = engine.update(8, now);
        } while (frame.animating());

        assertTrue(now >= config.durationSeconds());
        assertTrue(now <= config.durationSeconds() + frameDuration + EPSILON);
        assertEquals(8.0, frame.position(), EPSILON);
        assertTrue(frame.arrived());
    }

    @Test
    void rapidRetargetingStartsAtTheCurrentVisualPositionAndDropsOldTargets() {
        AnimationEngine engine = engine(AnimationPreset.BEDROCK_INSPIRED, WrapMode.ACROSS);
        engine.reset(0, 0.0);
        engine.update(4, 0.0);

        AnimationFrame towardFive = engine.update(4, 0.030);
        AnimationFrame towardEight = engine.update(7, 0.030);
        assertEquals(towardFive.position(), towardEight.position(), EPSILON);
        assertEquals(7, towardEight.targetSlot());
        assertEquals(2L, towardEight.transitionId());
        assertFalse(towardEight.arrived());

        AnimationFrame movingAgain = engine.update(7, 0.055);
        AnimationFrame towardThree = engine.update(2, 0.055);
        assertEquals(movingAgain.position(), towardThree.position(), EPSILON);
        assertEquals(2, towardThree.targetSlot());
        assertEquals(3L, towardThree.transitionId());

        AnimationFrame arrived = engine.update(2, 0.200);
        assertFalse(arrived.animating());
        assertTrue(arrived.arrived());
        assertEquals(2.0, arrived.position(), EPSILON);
        assertEquals(1L, arrived.arrivalId());
    }

    @Test
    void acrossTravelsThroughTheVisibleHotbar() {
        AnimationEngine engine = new AnimationEngine(linearConfig(WrapMode.ACROSS));
        engine.reset(8, 0.0);
        engine.update(0, 0.0);

        AnimationFrame halfway = engine.update(0, 0.100);

        assertEquals(4.0, halfway.position(), EPSILON);
        assertEquals(4.0, halfway.wrappedPosition(), EPSILON);
    }

    @Test
    void wrapUsesContinuousVirtualCoordinatesInBothDirections() {
        AnimationEngine forward = new AnimationEngine(linearConfig(WrapMode.WRAP));
        forward.reset(8, 0.0);
        forward.update(0, 0.0);
        AnimationFrame forwardHalfway = forward.update(0, 0.100);
        AnimationFrame forwardEnd = forward.update(0, 0.200);

        assertEquals(8.5, forwardHalfway.position(), EPSILON);
        assertEquals(9.0, forwardEnd.position(), EPSILON);
        assertEquals(0.0, forwardEnd.wrappedPosition(), EPSILON);

        AnimationEngine reverse = new AnimationEngine(linearConfig(WrapMode.WRAP));
        reverse.reset(0, 0.0);
        reverse.update(8, 0.0);
        AnimationFrame reverseHalfway = reverse.update(8, 0.100);
        AnimationFrame reverseEnd = reverse.update(8, 0.200);

        assertEquals(-0.5, reverseHalfway.position(), EPSILON);
        assertEquals(-1.0, reverseEnd.position(), EPSILON);
        assertEquals(8.0, reverseEnd.wrappedPosition(), EPSILON);
    }

    @Test
    void wrapChoosesTheShortestCircularPathForLongJumps() {
        AnimationEngine engine = new AnimationEngine(linearConfig(WrapMode.WRAP));
        engine.reset(0, 0.0);
        engine.update(5, 0.0);

        AnimationFrame halfway = engine.update(5, 0.100);

        assertEquals(-2.0, halfway.position(), EPSILON);
        assertEquals(7.0, halfway.wrappedPosition(), EPSILON);
    }

    @Test
    void springOvershootsWithinItsConfiguredLimitAndSettlesAtDuration() {
        AnimationConfig config = new AnimationConfig(
                AnimationPreset.CUSTOM,
                0.500,
                EasingType.SPRING,
                WrapMode.ACROSS,
                20.0,
                0.25,
                0.10);
        AnimationEngine engine = new AnimationEngine(config);
        engine.reset(0, 0.0);
        engine.update(4, 0.0);

        double maximum = Double.NEGATIVE_INFINITY;
        for (int step = 1; step < 500; step++) {
            maximum = Math.max(maximum, engine.update(4, step / 1_000.0).position());
        }
        AnimationFrame arrived = engine.update(4, 0.500);
        double measuredMaximum = maximum;

        assertTrue(
                measuredMaximum > 4.05,
                () -> "Expected visible overshoot, maximum was " + measuredMaximum);
        assertTrue(
                measuredMaximum <= 4.4 + EPSILON,
                () -> "Overshoot exceeded limit: " + measuredMaximum);
        assertTrue(arrived.arrived());
        assertFalse(arrived.animating());
        assertEquals(4.0, arrived.position(), EPSILON);
    }

    @Test
    void zeroOvershootProducesMonotonicSpringMotion() {
        AnimationConfig config = new AnimationConfig(
                AnimationPreset.CUSTOM,
                0.400,
                EasingType.SPRING,
                WrapMode.ACROSS,
                20.0,
                0.20,
                0.0);
        AnimationEngine engine = new AnimationEngine(config);
        engine.reset(0, 0.0);
        engine.update(4, 0.0);

        double previous = 0.0;
        for (int step = 1; step <= 400; step++) {
            double position = engine.update(4, step / 1_000.0).position();
            assertTrue(position >= previous - EPSILON);
            assertTrue(position <= 4.0 + EPSILON);
            previous = position;
        }
    }

    @Test
    void arrivalEventIsEmittedOnceForOnlyTheLatestCompletedTransition() {
        AnimationEngine engine = engine(AnimationPreset.SNAPPY, WrapMode.ACROSS);
        engine.reset(0, 0.0);
        engine.update(4, 0.0);
        engine.update(7, 0.020);
        engine.update(2, 0.040);

        int arrivalEvents = 0;
        AnimationFrame frame = null;
        for (int step = 5; step <= 30; step++) {
            frame = engine.update(2, step / 100.0);
            if (frame.arrived()) {
                arrivalEvents++;
            }
        }

        assertEquals(1, arrivalEvents);
        assertEquals(1L, frame.arrivalId());
        assertEquals(3L, frame.transitionId());
        assertFalse(frame.arrived());
    }

    @Test
    void disabledPresetMovesImmediatelyWithoutArrivalEvent() {
        AnimationEngine engine = engine(AnimationPreset.DISABLED, WrapMode.ACROSS);
        engine.reset(0, 0.0);

        AnimationFrame frame = engine.update(8, 0.010);

        assertEquals(8.0, frame.position(), EPSILON);
        assertFalse(frame.animating());
        assertFalse(frame.arrived());
        assertEquals(0L, frame.arrivalId());
    }

    @Test
    void changingConfigWhileMovingDoesNotJumpBackToAnIntegerOrigin() {
        AnimationEngine engine = engine(AnimationPreset.BEDROCK_INSPIRED, WrapMode.ACROSS);
        engine.reset(0, 0.0);
        engine.update(8, 0.0);
        AnimationFrame before = engine.update(8, 0.040);

        engine.setConfig(AnimationPreset.SMOOTH.config(WrapMode.ACROSS), 0.040);
        AnimationFrame after = engine.currentFrame();

        assertEquals(before.position(), after.position(), EPSILON);
        assertTrue(after.animating());
    }

    @Test
    void changingWrapModeWhileSettledKeepsTheCanonicalSlot() {
        AnimationEngine engine = engine(AnimationPreset.SMOOTH, WrapMode.WRAP);
        engine.reset(8, 0.0);
        engine.update(0, 0.0);
        AnimationFrame settledFrame = engine.update(0, 0.175);
        assertEquals(9.0, settledFrame.position(), EPSILON);

        engine.setConfig(AnimationPreset.SMOOTH.config(WrapMode.ACROSS), 0.175);

        AnimationFrame rebased = engine.currentFrame();
        assertEquals(0.0, rebased.position(), EPSILON);
        assertEquals(0.0, rebased.wrappedPosition(), EPSILON);
        assertFalse(rebased.animating());
    }

    @Test
    void resetAndValidationProtectEngineState() {
        AnimationEngine engine = engine(AnimationPreset.SMOOTH, WrapMode.ACROSS);
        assertThrows(IllegalStateException.class, engine::currentFrame);
        assertThrows(IllegalArgumentException.class, () -> engine.update(-1, 0.0));
        assertThrows(IllegalArgumentException.class, () -> engine.update(9, 0.0));
        assertThrows(IllegalArgumentException.class, () -> engine.update(0, Double.NaN));

        engine.reset(1, 2.0);
        assertThrows(IllegalArgumentException.class, () -> engine.update(2, 1.999));
        AnimationFrame reset = engine.reset(6, 3.0);
        assertEquals(6.0, reset.position(), EPSILON);
        assertEquals(0L, reset.transitionId());
        assertEquals(0L, reset.arrivalId());
    }

    private static AnimationEngine engine(AnimationPreset preset, WrapMode wrapMode) {
        return new AnimationEngine(preset.config(wrapMode));
    }

    private static AnimationConfig linearConfig(WrapMode wrapMode) {
        return new AnimationConfig(
                AnimationPreset.CUSTOM,
                0.200,
                EasingType.LINEAR,
                wrapMode,
                20.0,
                1.0,
                0.0);
    }
}
