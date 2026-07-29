package dev.yash.core.animation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class AnimationPresetTest {
    @Test
    void officialPresetDurationsMatchTheProductRanges() {
        assertDurationMillis(AnimationPreset.BEDROCK_INSPIRED, 100, 130);
        assertDurationMillis(AnimationPreset.SMOOTH, 160, 190);
        assertDurationMillis(AnimationPreset.SNAPPY, 60, 90);
        assertDurationMillis(AnimationPreset.MINIMAL, 40, 60);
    }

    @Test
    void springAndDisabledPresetsExposeTheirSpecialBehavior() {
        AnimationConfig spring = AnimationConfig.forPreset(AnimationPreset.SPRING, WrapMode.ACROSS);
        AnimationConfig disabled = AnimationConfig.forPreset(AnimationPreset.DISABLED, WrapMode.WRAP);

        assertTrue(spring.animated());
        assertTrue(spring.spring());
        assertTrue(spring.springOvershootLimit() > 0.0);
        assertFalse(disabled.animated());
        assertFalse(disabled.spring());
        assertEquals(0.0, disabled.durationSeconds());
    }

    @Test
    void configRejectsInvalidOrInconsistentParameters() {
        assertThrows(NullPointerException.class, () -> AnimationConfig.forPreset(null, WrapMode.ACROSS));
        assertThrows(NullPointerException.class, () -> AnimationConfig.forPreset(
                AnimationPreset.SMOOTH, null));
        assertThrows(IllegalArgumentException.class, () -> new AnimationConfig(
                AnimationPreset.CUSTOM,
                0.0,
                EasingType.LINEAR,
                WrapMode.ACROSS,
                20.0,
                1.0,
                0.0));
        assertThrows(IllegalArgumentException.class, () -> new AnimationConfig(
                AnimationPreset.CUSTOM,
                0.1,
                EasingType.LINEAR,
                WrapMode.ACROSS,
                Double.NaN,
                1.0,
                0.0));
        assertThrows(IllegalArgumentException.class, () -> new AnimationConfig(
                AnimationPreset.CUSTOM,
                0.1,
                EasingType.LINEAR,
                WrapMode.ACROSS,
                20.0,
                1.0,
                1.01));
    }

    @Test
    void directConstructionCannotMislabelChangedOfficialParameters() {
        AnimationConfig edited = new AnimationConfig(
                AnimationPreset.SMOOTH,
                0.250,
                EasingType.EASE_IN_OUT_CUBIC,
                WrapMode.ACROSS,
                22.0,
                1.0,
                0.0);

        assertEquals(AnimationPreset.CUSTOM, edited.preset());
    }

    private static void assertDurationMillis(AnimationPreset preset, int minimum, int maximum) {
        double durationMillis = preset.config(WrapMode.ACROSS).durationSeconds() * 1_000.0;
        assertTrue(durationMillis >= minimum && durationMillis <= maximum,
                () -> preset + " duration was " + durationMillis + " ms");
    }
}
