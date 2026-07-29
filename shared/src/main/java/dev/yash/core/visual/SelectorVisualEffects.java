package dev.yash.core.visual;

import dev.yash.core.config.RgbColor;
import java.util.Objects;

/** Renderer-neutral selector dimensions and color effects for one animation frame. */
public record SelectorVisualEffects(
        double scaleX,
        double scaleY,
        double opacity,
        RgbColor textureMultiplier,
        boolean glowEnabled,
        RgbColor glowColor,
        double glowOpacity,
        double glowRadius) {
    public SelectorVisualEffects {
        requirePositive(scaleX, "scaleX");
        requirePositive(scaleY, "scaleY");
        requireRange(opacity, 0.2, 1.0, "opacity");
        Objects.requireNonNull(textureMultiplier, "textureMultiplier");
        Objects.requireNonNull(glowColor, "glowColor");
        requireRange(glowOpacity, 0.0, 1.0, "glowOpacity");
        requireRange(glowRadius, 0.0, 16.0, "glowRadius");
    }

    private static void requirePositive(double value, String name) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(name + " must be finite and positive");
        }
    }

    private static void requireRange(double value, double minimum, double maximum, String name) {
        if (!Double.isFinite(value) || value < minimum || value > maximum) {
            throw new IllegalArgumentException(
                    name + " must be finite and in range [" + minimum + ", " + maximum + "]");
        }
    }
}
