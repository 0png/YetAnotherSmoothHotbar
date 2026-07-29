package dev.yash.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.yash.core.animation.AnimationPreset;
import dev.yash.core.animation.EasingType;
import dev.yash.core.animation.WrapMode;
import org.junit.jupiter.api.Test;

final class YashConfigTest {
    private static final double EPSILON = 1.0e-12;

    @Test
    void safeDefaultsMatchTheProductSpecification() {
        YashConfig config = YashConfig.defaults();

        assertTrue(config.enabled());
        assertEquals(AnimationPreset.BEDROCK_INSPIRED, config.animation().preset());
        assertEquals(WrapMode.ACROSS, config.animation().wrapMode());
        assertEquals(SelectorTheme.VANILLA_RESOURCE_PACK, config.theme());
        assertFalse(config.glow().enabled());
        assertFalse(config.stretch().enabled());
        assertFalse(config.pulse().enabled());
        assertFalse(config.tint().enabled());
        assertEquals(1.0, config.opacity(), EPSILON);
        assertEquals(1.0, config.scale(), EPSILON);
        assertFalse(config.reducedMotion());
        assertTrue(config.effective().active());
    }

    @Test
    void selectingAnOfficialPresetAppliesAllOwnedParameters() {
        YashConfig customized = YashConfig.defaults()
                .withWrapMode(WrapMode.WRAP)
                .withAnimationDurationSeconds(0.333)
                .withEasing(EasingType.SPRING)
                .withSpringParameters(41.0, 0.25, 0.3);

        YashConfig spring = customized.withPreset(AnimationPreset.SPRING);

        assertEquals(AnimationPreset.SPRING.config(WrapMode.WRAP), spring.animation());
    }

    @Test
    void editingPresetOwnedParametersSwitchesToCustomButWrapDoesNot() {
        YashConfig smooth = YashConfig.defaults().withPreset(AnimationPreset.SMOOTH);

        assertEquals(AnimationPreset.CUSTOM,
                smooth.withAnimationDurationSeconds(0.250).animation().preset());
        assertEquals(AnimationPreset.CUSTOM,
                smooth.withEasing(EasingType.LINEAR).animation().preset());
        assertEquals(AnimationPreset.CUSTOM,
                smooth.withSpringParameters(30.0, 0.8, 0.1).animation().preset());
        assertEquals(AnimationPreset.SMOOTH,
                smooth.withWrapMode(WrapMode.WRAP).animation().preset());
    }

    @Test
    void reducedMotionChangesOnlyTheEffectiveRuntimeView() {
        YashConfig stored = new YashConfig(
                true,
                AnimationPreset.SPRING.config(WrapMode.WRAP),
                SelectorTheme.WOLF,
                new TintSettings(true, new RgbColor(0xAABBCC), 0.4),
                0.75,
                1.25,
                new GlowSettings(true, new RgbColor(0xFFAA00), 0.8, 6.0, true),
                new StretchSettings(true, 0.7),
                new PulseSettings(true, 0.6),
                true);

        EffectiveConfig effective = stored.effective();

        assertNotSame(stored.animation(), effective.animation());
        assertEquals(AnimationPreset.SPRING, stored.animation().preset());
        assertEquals(EasingType.EASE_OUT_QUAD, effective.animation().easing());
        assertFalse(effective.animation().spring());
        assertTrue(effective.animation().durationSeconds()
                <= YashConfig.REDUCED_MOTION_DURATION_SECONDS);
        assertEquals(0.0, effective.animation().springOvershootLimit(), EPSILON);
        assertFalse(effective.glow().animated());
        assertTrue(effective.glow().enabled());
        assertFalse(effective.stretch().enabled());
        assertFalse(effective.pulse().enabled());
        assertEquals(SelectorTheme.WOLF, effective.theme());
        assertEquals(stored.tint(), effective.tint());
        assertEquals(stored.opacity(), effective.opacity(), EPSILON);
        assertEquals(stored.scale(), effective.scale(), EPSILON);

        assertTrue(stored.glow().animated());
        assertTrue(stored.stretch().enabled());
        assertTrue(stored.pulse().enabled());
    }

    @Test
    void eitherDisableControlImmediatelyResolvesToVanillaBehavior() {
        EffectiveConfig globallyDisabled = YashConfig.defaults().withEnabled(false).effective();
        EffectiveConfig presetDisabled = YashConfig.defaults()
                .withPreset(AnimationPreset.DISABLED)
                .effective();

        assertFalse(globallyDisabled.active());
        assertFalse(globallyDisabled.animation().animated());
        assertFalse(presetDisabled.active());
        assertFalse(presetDisabled.animation().animated());
    }

    @Test
    void visualRangesAndRgbFormatAreValidated() {
        assertEquals("#00A1FF", RgbColor.parse("#00a1ff").hex());
        assertThrows(IllegalArgumentException.class, () -> RgbColor.parse("00A1FF"));
        assertThrows(IllegalArgumentException.class, () -> new RgbColor(0x1000000));
        assertThrows(IllegalArgumentException.class, () -> new TintSettings(true, RgbColor.WHITE, 1.1));
        assertThrows(IllegalArgumentException.class,
                () -> new GlowSettings(true, RgbColor.WHITE, 0.5, 16.1, false));
        assertThrows(IllegalArgumentException.class, () -> new StretchSettings(true, -0.1));
        assertThrows(IllegalArgumentException.class, () -> new PulseSettings(true, Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new YashConfig(
                true,
                AnimationPreset.SMOOTH.config(WrapMode.ACROSS),
                SelectorTheme.CLEAN,
                TintSettings.defaults(),
                0.19,
                1.0,
                GlowSettings.defaults(),
                StretchSettings.defaults(),
                PulseSettings.defaults(),
                false));
    }
}
