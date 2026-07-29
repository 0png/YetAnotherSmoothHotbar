package dev.yash.core.config;

import java.util.Locale;

/** Immutable 24-bit RGB color serialized as an uppercase {@code #RRGGBB} string. */
public record RgbColor(int value) {
    public static final RgbColor WHITE = new RgbColor(0xFFFFFF);

    public RgbColor {
        if (value < 0 || value > 0xFFFFFF) {
            throw new IllegalArgumentException("RGB value must be in range [0x000000, 0xFFFFFF]");
        }
    }

    public static RgbColor parse(String hex) {
        if (hex == null || !hex.matches("#[0-9a-fA-F]{6}")) {
            throw new IllegalArgumentException("RGB color must use #RRGGBB format: " + hex);
        }
        return new RgbColor(Integer.parseInt(hex.substring(1), 16));
    }

    public String hex() {
        return String.format(Locale.ROOT, "#%06X", value);
    }
}
