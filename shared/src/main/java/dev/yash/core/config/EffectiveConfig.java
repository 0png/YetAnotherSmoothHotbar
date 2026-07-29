package dev.yash.core.config;

import dev.yash.core.animation.AnimationConfig;
import java.util.Objects;

/** Runtime-only settings after accessibility and enablement policies are applied. */
public record EffectiveConfig(
        boolean active,
        AnimationConfig animation,
        SelectorTheme theme,
        TintSettings tint,
        double opacity,
        double scale,
        GlowSettings glow,
        StretchSettings stretch,
        PulseSettings pulse,
        boolean reducedMotion) {
    public EffectiveConfig {
        Objects.requireNonNull(animation, "animation");
        Objects.requireNonNull(theme, "theme");
        Objects.requireNonNull(tint, "tint");
        Objects.requireNonNull(glow, "glow");
        Objects.requireNonNull(stretch, "stretch");
        Objects.requireNonNull(pulse, "pulse");
        SettingRanges.require(opacity, 0.2, 1.0, "opacity");
        SettingRanges.require(scale, 0.75, 1.5, "scale");
    }
}
