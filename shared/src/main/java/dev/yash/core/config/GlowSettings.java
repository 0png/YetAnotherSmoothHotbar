package dev.yash.core.config;

import java.util.Objects;

/** Selector glow, including whether its intensity may animate. */
public record GlowSettings(
        boolean enabled,
        RgbColor color,
        double strength,
        double radius,
        boolean animated) {
    public GlowSettings {
        Objects.requireNonNull(color, "color");
        SettingRanges.require(strength, 0.0, 1.0, "glow strength");
        SettingRanges.require(radius, 0.0, 16.0, "glow radius");
    }

    public static GlowSettings defaults() {
        return new GlowSettings(false, RgbColor.WHITE, 0.35, 4.0, false);
    }

    public GlowSettings withoutAnimation() {
        return animated ? new GlowSettings(enabled, color, strength, radius, false) : this;
    }
}
