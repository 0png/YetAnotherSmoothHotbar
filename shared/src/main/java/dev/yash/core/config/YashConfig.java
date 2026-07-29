package dev.yash.core.config;

import dev.yash.core.animation.AnimationConfig;
import dev.yash.core.animation.AnimationPreset;
import dev.yash.core.animation.EasingType;
import dev.yash.core.animation.WrapMode;
import java.util.Objects;

/** Complete immutable player preference model. */
public record YashConfig(
        boolean enabled,
        AnimationConfig animation,
        SelectorTheme theme,
        TintSettings tint,
        double opacity,
        double scale,
        GlowSettings glow,
        StretchSettings stretch,
        PulseSettings pulse,
        boolean reducedMotion) {
    public static final double REDUCED_MOTION_DURATION_SECONDS = 0.050;

    public YashConfig {
        Objects.requireNonNull(animation, "animation");
        Objects.requireNonNull(theme, "theme");
        Objects.requireNonNull(tint, "tint");
        Objects.requireNonNull(glow, "glow");
        Objects.requireNonNull(stretch, "stretch");
        Objects.requireNonNull(pulse, "pulse");
        SettingRanges.require(opacity, 0.2, 1.0, "opacity");
        SettingRanges.require(scale, 0.75, 1.5, "scale");
    }

    public static YashConfig defaults() {
        return new YashConfig(
                true,
                AnimationPreset.BEDROCK_INSPIRED.config(WrapMode.ACROSS),
                SelectorTheme.VANILLA_RESOURCE_PACK,
                TintSettings.defaults(),
                1.0,
                1.0,
                GlowSettings.defaults(),
                StretchSettings.defaults(),
                PulseSettings.defaults(),
                false);
    }

    /** Applies every animation parameter owned by an official preset. */
    public YashConfig withPreset(AnimationPreset preset) {
        AnimationConfig replacement = Objects.requireNonNull(preset, "preset")
                .config(animation.wrapMode());
        return withAnimation(replacement);
    }

    public YashConfig withAnimationDurationSeconds(double durationSeconds) {
        return withAnimation(animation.withDurationSeconds(durationSeconds));
    }

    public YashConfig withEasing(EasingType easing) {
        return withAnimation(animation.withEasing(easing));
    }

    public YashConfig withSpringParameters(double strength, double damping, double overshoot) {
        return withAnimation(animation.withSpringParameters(strength, damping, overshoot));
    }

    public YashConfig withWrapMode(WrapMode wrapMode) {
        return withAnimation(animation.withWrapMode(wrapMode));
    }

    public YashConfig withEnabled(boolean newEnabled) {
        return new YashConfig(
                newEnabled, animation, theme, tint, opacity, scale,
                glow, stretch, pulse, reducedMotion);
    }

    public YashConfig withTheme(SelectorTheme newTheme) {
        return new YashConfig(
                enabled,
                animation,
                Objects.requireNonNull(newTheme, "newTheme"),
                tint,
                opacity,
                scale,
                glow,
                stretch,
                pulse,
                reducedMotion);
    }

    public YashConfig withTint(TintSettings newTint) {
        return new YashConfig(
                enabled,
                animation,
                theme,
                Objects.requireNonNull(newTint, "newTint"),
                opacity,
                scale,
                glow,
                stretch,
                pulse,
                reducedMotion);
    }

    public YashConfig withOpacity(double newOpacity) {
        return new YashConfig(
                enabled, animation, theme, tint, newOpacity, scale,
                glow, stretch, pulse, reducedMotion);
    }

    public YashConfig withScale(double newScale) {
        return new YashConfig(
                enabled, animation, theme, tint, opacity, newScale,
                glow, stretch, pulse, reducedMotion);
    }

    public YashConfig withGlow(GlowSettings newGlow) {
        return new YashConfig(
                enabled,
                animation,
                theme,
                tint,
                opacity,
                scale,
                Objects.requireNonNull(newGlow, "newGlow"),
                stretch,
                pulse,
                reducedMotion);
    }

    public YashConfig withStretch(StretchSettings newStretch) {
        return new YashConfig(
                enabled,
                animation,
                theme,
                tint,
                opacity,
                scale,
                glow,
                Objects.requireNonNull(newStretch, "newStretch"),
                pulse,
                reducedMotion);
    }

    public YashConfig withPulse(PulseSettings newPulse) {
        return new YashConfig(
                enabled,
                animation,
                theme,
                tint,
                opacity,
                scale,
                glow,
                stretch,
                Objects.requireNonNull(newPulse, "newPulse"),
                reducedMotion);
    }

    public YashConfig withReducedMotion(boolean newReducedMotion) {
        return new YashConfig(
                enabled, animation, theme, tint, opacity, scale,
                glow, stretch, pulse, newReducedMotion);
    }

    /** Resolves runtime behavior without changing any saved player preference. */
    public EffectiveConfig effective() {
        boolean active = enabled && animation.animated();
        AnimationConfig effectiveAnimation = active
                ? animation
                : AnimationPreset.DISABLED.config(animation.wrapMode());
        GlowSettings effectiveGlow = glow;
        StretchSettings effectiveStretch = stretch;
        PulseSettings effectivePulse = pulse;

        if (active && reducedMotion) {
            effectiveAnimation = new AnimationConfig(
                    AnimationPreset.CUSTOM,
                    Math.min(animation.durationSeconds(), REDUCED_MOTION_DURATION_SECONDS),
                    EasingType.EASE_OUT_QUAD,
                    animation.wrapMode(),
                    animation.springStrength(),
                    Math.max(1.0, animation.springDampingRatio()),
                    0.0);
            effectiveGlow = glow.withoutAnimation();
            effectiveStretch = stretch.disabled();
            effectivePulse = pulse.disabled();
        }

        return new EffectiveConfig(
                active,
                effectiveAnimation,
                theme,
                tint,
                opacity,
                scale,
                effectiveGlow,
                effectiveStretch,
                effectivePulse,
                reducedMotion);
    }

    private YashConfig withAnimation(AnimationConfig newAnimation) {
        return new YashConfig(
                enabled,
                Objects.requireNonNull(newAnimation, "newAnimation"),
                theme,
                tint,
                opacity,
                scale,
                glow,
                stretch,
                pulse,
                reducedMotion);
    }
}
