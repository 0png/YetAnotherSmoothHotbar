package dev.yash.core.config;

import java.util.Objects;

/** Optional color overlay applied without replacing a theme's source artwork. */
public record TintSettings(boolean enabled, RgbColor color, double strength) {
    public TintSettings {
        Objects.requireNonNull(color, "color");
        SettingRanges.require(strength, 0.0, 1.0, "tint strength");
    }

    public static TintSettings defaults() {
        return new TintSettings(false, RgbColor.WHITE, 0.5);
    }
}
