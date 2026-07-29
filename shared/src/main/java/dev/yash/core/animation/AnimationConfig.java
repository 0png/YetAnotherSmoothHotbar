package dev.yash.core.animation;

import java.util.Objects;

/** Immutable parameters used for one selector animation policy. */
public record AnimationConfig(
        AnimationPreset preset,
        double durationSeconds,
        EasingType easing,
        WrapMode wrapMode,
        double springStrength,
        double springDampingRatio,
        double springOvershootLimit) {

    public AnimationConfig {
        Objects.requireNonNull(preset, "preset");
        Objects.requireNonNull(easing, "easing");
        Objects.requireNonNull(wrapMode, "wrapMode");
        requireFiniteInRange(durationSeconds, 0.0, 10.0, "durationSeconds");
        requireFiniteInRange(springStrength, 0.001, 200.0, "springStrength");
        requireFiniteInRange(springDampingRatio, 0.0, 10.0, "springDampingRatio");
        requireFiniteInRange(springOvershootLimit, 0.0, 1.0, "springOvershootLimit");

        if (preset == AnimationPreset.DISABLED && durationSeconds != 0.0) {
            throw new IllegalArgumentException("Disabled preset must have zero duration");
        }
        if (preset != AnimationPreset.DISABLED && durationSeconds == 0.0) {
            throw new IllegalArgumentException("Animated presets must have a positive duration");
        }
        if (preset != AnimationPreset.CUSTOM
                && preset != AnimationPreset.DISABLED
                && !preset.matchesParameters(
                        durationSeconds,
                        easing,
                        springStrength,
                        springDampingRatio,
                        springOvershootLimit)) {
            preset = AnimationPreset.CUSTOM;
        }
    }

    /** Returns the complete defaults for a built-in preset. */
    public static AnimationConfig forPreset(AnimationPreset preset, WrapMode wrapMode) {
        return Objects.requireNonNull(preset, "preset").config(wrapMode);
    }

    /** Whether selector movement should be animated. */
    public boolean animated() {
        return preset != AnimationPreset.DISABLED;
    }

    /** Whether this config uses analytic damped-spring motion. */
    public boolean spring() {
        return animated() && easing == EasingType.SPRING;
    }

    /** Changes a preset-owned value and marks the result as Custom. */
    public AnimationConfig withDurationSeconds(double newDurationSeconds) {
        AnimationConfig base = customizationBase();
        return new AnimationConfig(
                AnimationPreset.CUSTOM,
                newDurationSeconds,
                base.easing,
                base.wrapMode,
                base.springStrength,
                base.springDampingRatio,
                base.springOvershootLimit);
    }

    /** Changes a preset-owned value and marks the result as Custom. */
    public AnimationConfig withEasing(EasingType newEasing) {
        AnimationConfig base = customizationBase();
        return new AnimationConfig(
                AnimationPreset.CUSTOM,
                base.durationSeconds,
                Objects.requireNonNull(newEasing, "newEasing"),
                base.wrapMode,
                base.springStrength,
                base.springDampingRatio,
                base.springOvershootLimit);
    }

    /** Changes preset-owned spring values and marks the result as Custom. */
    public AnimationConfig withSpringParameters(
            double newStrength,
            double newDampingRatio,
            double newOvershootLimit) {
        AnimationConfig base = customizationBase();
        return new AnimationConfig(
                AnimationPreset.CUSTOM,
                base.durationSeconds,
                base.easing,
                base.wrapMode,
                newStrength,
                newDampingRatio,
                newOvershootLimit);
    }

    /** Wrap is an independent navigation preference and does not change the selected preset. */
    public AnimationConfig withWrapMode(WrapMode newWrapMode) {
        return new AnimationConfig(
                preset,
                durationSeconds,
                easing,
                Objects.requireNonNull(newWrapMode, "newWrapMode"),
                springStrength,
                springDampingRatio,
                springOvershootLimit);
    }

    private AnimationConfig customizationBase() {
        return preset == AnimationPreset.DISABLED
                ? AnimationPreset.CUSTOM.config(wrapMode)
                : this;
    }

    private static void requireFiniteInRange(double value, double minimum, double maximum, String name) {
        if (!Double.isFinite(value) || value < minimum || value > maximum) {
            throw new IllegalArgumentException(
                    name + " must be finite and in range [" + minimum + ", " + maximum + "]");
        }
    }
}
