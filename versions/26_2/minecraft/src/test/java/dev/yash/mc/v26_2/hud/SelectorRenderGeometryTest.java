package dev.yash.mc.v26_2.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.yash.core.config.RgbColor;
import dev.yash.core.config.SelectorTheme;
import dev.yash.core.visual.SelectorVisualEffects;
import org.junit.jupiter.api.Test;

final class SelectorRenderGeometryTest {
    private static final SelectorVisualEffects NORMAL = effects(1.0, 1.0);

    @Test
    void allThemeSizesStayCenteredOnTheAnimatedSlot() {
        for (SelectorTheme theme : SelectorTheme.values()) {
            SelectorPlacement placement = SelectorRenderGeometry.place(
                    100, 50, 24, 23, 2.0, theme.metadata(), NORMAL);

            assertEquals(
                    152 + theme.metadata().offsetX(),
                    placement.x() + placement.width() / 2);
            assertEquals(theme.metadata().nativeWidth(), placement.width());
            assertEquals(theme.metadata().nativeHeight(), placement.height());
        }
    }

    @Test
    void scaleAndStretchExpandAroundTheSameCenter() {
        SelectorPlacement placement = SelectorRenderGeometry.place(
                100,
                50,
                24,
                23,
                2.0,
                SelectorTheme.BEE.metadata(),
                effects(1.5, 1.25));

        assertEquals(36, placement.width());
        assertEquals(29, placement.height());
        assertEquals(152, placement.x() + placement.width() / 2);
    }

    @Test
    void argbCombinesOpacityAndMultiplierWithoutPremultiplyingRgb() {
        assertEquals(
                0x80112233,
                HotbarSelectorRenderer.argb(0.5, new RgbColor(0x112233)));
    }

    private static SelectorVisualEffects effects(double scaleX, double scaleY) {
        return new SelectorVisualEffects(
                scaleX,
                scaleY,
                1.0,
                RgbColor.WHITE,
                false,
                RgbColor.WHITE,
                0.0,
                0.0);
    }
}
