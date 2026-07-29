package dev.yash.mc.v26_2.hud;

import dev.yash.core.config.ThemeMetadata;
import dev.yash.core.visual.SelectorVisualEffects;
import java.util.Objects;

/** Centers custom theme geometry on the vanilla selector's animated slot position. */
public final class SelectorRenderGeometry {
    private SelectorRenderGeometry() {
    }

    public static SelectorPlacement place(
            int selectorBaseX,
            int vanillaY,
            int vanillaWidth,
            int vanillaHeight,
            double slotPosition,
            ThemeMetadata theme,
            SelectorVisualEffects effects) {
        Objects.requireNonNull(theme, "theme");
        Objects.requireNonNull(effects, "effects");
        if (!Double.isFinite(slotPosition)) {
            throw new IllegalArgumentException("Slot position must be finite");
        }
        if (vanillaWidth <= 0 || vanillaHeight <= 0) {
            throw new IllegalArgumentException("Vanilla selector dimensions must be positive");
        }

        int width = Math.max(1, (int) Math.round(theme.nativeWidth() * effects.scaleX()));
        int height = Math.max(1, (int) Math.round(theme.nativeHeight() * effects.scaleY()));
        double centerX = selectorBaseX
                + vanillaWidth / 2.0
                + slotPosition * HotbarLayout.SLOT_SPACING
                + theme.offsetX();
        double centerY = vanillaY + vanillaHeight / 2.0 + theme.offsetY();
        return new SelectorPlacement(
                (int) Math.round(centerX - width / 2.0),
                (int) Math.round(centerY - height / 2.0),
                width,
                height);
    }
}
