package dev.yash.core.visual;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.yash.core.animation.AnimationFrame;
import dev.yash.core.animation.WrapMode;
import dev.yash.core.config.GlowSettings;
import dev.yash.core.config.PulseSettings;
import dev.yash.core.config.RgbColor;
import dev.yash.core.config.StretchSettings;
import dev.yash.core.config.TintSettings;
import dev.yash.core.config.YashConfig;
import org.junit.jupiter.api.Test;

final class SelectorEffectEngineTest {
    private static final double EPSILON = 1.0e-9;

    @Test
    void defaultsLeaveThemeArtworkUntouched() {
        SelectorVisualEffects effects = new SelectorEffectEngine().sample(
                frame(0.0, false, 0L), YashConfig.defaults().effective(), 10.0);

        assertEquals(1.0, effects.scaleX(), EPSILON);
        assertEquals(1.0, effects.scaleY(), EPSILON);
        assertEquals(1.0, effects.opacity(), EPSILON);
        assertEquals(RgbColor.WHITE, effects.textureMultiplier());
        assertFalse(effects.glowEnabled());
    }

    @Test
    void tintBlendsFromWhiteAndPreservesConfiguredOpacityAndScale() {
        YashConfig config = YashConfig.defaults()
                .withTint(new TintSettings(true, new RgbColor(0xFF0000), 0.5))
                .withOpacity(0.6)
                .withScale(1.25);
        SelectorVisualEffects effects = new SelectorEffectEngine().sample(
                frame(0.0, false, 0L), config.effective(), 10.0);

        assertEquals(new RgbColor(0xFF8080), effects.textureMultiplier());
        assertEquals(0.6, effects.opacity(), EPSILON);
        assertEquals(1.25, effects.scaleX(), EPSILON);
        assertEquals(1.25, effects.scaleY(), EPSILON);
    }

    @Test
    void stretchTracksHorizontalVelocityAndNeverChangesHeight() {
        YashConfig config = YashConfig.defaults()
                .withStretch(new StretchSettings(true, 1.0));
        SelectorVisualEffects effects = new SelectorEffectEngine().sample(
                frame(40.0, false, 0L), config.effective(), 10.0);

        assertEquals(1.35, effects.scaleX(), EPSILON);
        assertEquals(1.0, effects.scaleY(), EPSILON);
    }

    @Test
    void arrivalPulseDecaysAndRepeatedSamplingDoesNotStack() {
        YashConfig config = YashConfig.defaults().withPulse(new PulseSettings(true, 0.2));
        SelectorEffectEngine engine = new SelectorEffectEngine();

        SelectorVisualEffects arrival = engine.sample(
                frame(0.0, true, 1L), config.effective(), 20.0);
        SelectorVisualEffects halfway = engine.sample(
                frame(0.0, false, 1L), config.effective(), 20.080);
        SelectorVisualEffects repeated = engine.sample(
                frame(0.0, false, 1L), config.effective(), 20.080);
        SelectorVisualEffects settled = engine.sample(
                frame(0.0, false, 1L), config.effective(), 20.200);

        assertEquals(1.2, arrival.scaleX(), EPSILON);
        assertEquals(1.05, halfway.scaleX(), EPSILON);
        assertEquals(halfway.scaleX(), repeated.scaleX(), EPSILON);
        assertEquals(1.0, settled.scaleX(), EPSILON);
    }

    @Test
    void latestArrivalReplacesRatherThanAddsToThePreviousPulse() {
        YashConfig config = YashConfig.defaults().withPulse(new PulseSettings(true, 0.2));
        SelectorEffectEngine engine = new SelectorEffectEngine();
        engine.sample(frame(0.0, true, 1L), config.effective(), 1.0);

        SelectorVisualEffects replacement = engine.sample(
                frame(0.0, true, 2L), config.effective(), 1.050);

        assertEquals(1.2, replacement.scaleX(), EPSILON);
    }

    @Test
    void glowUsesConfiguredColorAndReducedMotionDisablesDynamicEffects() {
        YashConfig config = YashConfig.defaults()
                .withGlow(new GlowSettings(
                        true, new RgbColor(0xAA44FF), 0.8, 6.0, true))
                .withStretch(new StretchSettings(true, 1.0))
                .withPulse(new PulseSettings(true, 0.2));
        SelectorVisualEffects animated = new SelectorEffectEngine().sample(
                frame(40.0, false, 0L), config.effective(), 1.0);
        SelectorVisualEffects reduced = new SelectorEffectEngine().sample(
                frame(40.0, true, 1L), config.withReducedMotion(true).effective(), 1.0);

        assertTrue(animated.glowEnabled());
        assertEquals(new RgbColor(0xAA44FF), animated.glowColor());
        assertEquals(0.8, animated.glowOpacity(), EPSILON);
        assertEquals(1.0, reduced.scaleX(), EPSILON);
        assertEquals(1.0, reduced.scaleY(), EPSILON);
        assertTrue(reduced.glowEnabled());
        assertEquals(0.8, reduced.glowOpacity(), EPSILON);
    }

    private static AnimationFrame frame(double velocity, boolean arrived, long arrivalId) {
        return new AnimationFrame(
                4.0,
                velocity,
                4,
                false,
                arrived,
                arrivalId,
                arrivalId,
                WrapMode.ACROSS);
    }
}
