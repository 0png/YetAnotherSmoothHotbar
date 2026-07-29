package dev.yash.mc.v26_2.hud;

/** Pixel-aligned selector rectangle in Minecraft's GUI-scaled coordinate space. */
public record SelectorPlacement(int x, int y, int width, int height) {
    public SelectorPlacement {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Selector dimensions must be positive");
        }
    }
}
