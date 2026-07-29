package dev.yash.core.config;

import java.util.Objects;

/** Immutable built-in artwork geometry and optional theme suggestions. */
public record ThemeMetadata(
        String spritePath,
        int nativeWidth,
        int nativeHeight,
        int offsetX,
        int offsetY,
        RgbColor suggestedGlowColor,
        double suggestedPulseStrength,
        double suggestedStretchStrength) {
    public ThemeMetadata {
        Objects.requireNonNull(spritePath, "spritePath");
        Objects.requireNonNull(suggestedGlowColor, "suggestedGlowColor");
        if (nativeWidth <= 0 || nativeHeight <= 0) {
            throw new IllegalArgumentException("Theme dimensions must be positive");
        }
        SettingRanges.require(
                suggestedPulseStrength, 0.0, 1.0, "suggested pulse strength");
        SettingRanges.require(
                suggestedStretchStrength, 0.0, 1.0, "suggested stretch strength");
    }

    /** Vanilla has no YASH sprite path because the active resource pack supplies its artwork. */
    public boolean resourcePackBacked() {
        return spritePath.isEmpty();
    }
}
