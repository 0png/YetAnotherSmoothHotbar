package dev.yash.mc.v26_2.hud;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.yash.core.YashCore;
import dev.yash.core.animation.AnimationFrame;
import dev.yash.core.animation.WrapMode;
import dev.yash.core.config.EffectiveConfig;
import dev.yash.core.config.RgbColor;
import dev.yash.core.config.SelectorTheme;
import dev.yash.core.config.ThemeMetadata;
import dev.yash.core.visual.SelectorVisualEffects;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

/** Draws resource-pack and built-in selector themes without loading resources in the render loop. */
public final class HotbarSelectorRenderer {
    private static final Identifier GLOW_SPRITE = Identifier.fromNamespaceAndPath(
            YashCore.MOD_ID, "hud/hotbar_selector/clean");
    private static final Map<SelectorTheme, Identifier> BUILT_IN_SPRITES =
            createBuiltInSprites();

    public void render(
            GuiGraphicsExtractor graphics,
            RenderPipeline pipeline,
            Identifier vanillaSprite,
            int selectorBaseX,
            int vanillaY,
            int vanillaWidth,
            int vanillaHeight,
            AnimationFrame frame,
            EffectiveConfig config,
            SelectorVisualEffects effects) {
        ThemeMetadata theme = config.theme().metadata();
        Identifier sprite = theme.resourcePackBacked()
                ? vanillaSprite
                : BUILT_IN_SPRITES.get(config.theme());

        if (frame.wrapMode() == WrapMode.WRAP) {
            renderWrapped(
                    graphics,
                    pipeline,
                    sprite,
                    selectorBaseX,
                    vanillaY,
                    vanillaWidth,
                    vanillaHeight,
                    frame,
                    theme,
                    effects);
            return;
        }
        drawSelector(
                graphics,
                pipeline,
                sprite,
                SelectorRenderGeometry.place(
                        selectorBaseX,
                        vanillaY,
                        vanillaWidth,
                        vanillaHeight,
                        frame.position(),
                        theme,
                        effects),
                effects);
    }

    private static void renderWrapped(
            GuiGraphicsExtractor graphics,
            RenderPipeline pipeline,
            Identifier sprite,
            int selectorBaseX,
            int vanillaY,
            int vanillaWidth,
            int vanillaHeight,
            AnimationFrame frame,
            ThemeMetadata theme,
            SelectorVisualEffects effects) {
        int horizontalOverflow = Math.max(
                0,
                (int) Math.ceil(
                        (theme.nativeWidth() * effects.scaleX() - vanillaWidth) / 2.0
                                + effects.glowRadius()));
        int verticalOverflow = Math.max(
                0,
                (int) Math.ceil(
                        (theme.nativeHeight() * effects.scaleY() - vanillaHeight) / 2.0
                                + effects.glowRadius()));
        graphics.enableScissor(
                selectorBaseX - horizontalOverflow,
                vanillaY - verticalOverflow,
                selectorBaseX
                        + HotbarLayout.SELECTOR_TRAVEL_DISTANCE
                        + vanillaWidth
                        + horizontalOverflow,
                vanillaY + vanillaHeight + verticalOverflow);
        try {
            drawSelector(
                    graphics,
                    pipeline,
                    sprite,
                    SelectorRenderGeometry.place(
                            selectorBaseX,
                            vanillaY,
                            vanillaWidth,
                            vanillaHeight,
                            frame.wrappedPosition(),
                            theme,
                            effects),
                    effects);
            double seamPosition = HotbarLayout.wrapSeamPosition(frame);
            if (Double.isFinite(seamPosition)) {
                drawSelector(
                        graphics,
                        pipeline,
                        sprite,
                        SelectorRenderGeometry.place(
                                selectorBaseX,
                                vanillaY,
                                vanillaWidth,
                                vanillaHeight,
                                seamPosition,
                                theme,
                                effects),
                        effects);
            }
        } finally {
            graphics.disableScissor();
        }
    }

    private static void drawSelector(
            GuiGraphicsExtractor graphics,
            RenderPipeline pipeline,
            Identifier sprite,
            SelectorPlacement placement,
            SelectorVisualEffects effects) {
        if (effects.glowEnabled()) {
            int outerRadius = Math.max(1, (int) Math.round(effects.glowRadius()));
            int innerRadius = Math.max(1, (int) Math.round(effects.glowRadius() * 0.5));
            drawGlow(graphics, pipeline, placement, effects, outerRadius, 0.35);
            drawGlow(graphics, pipeline, placement, effects, innerRadius, 0.55);
        }
        graphics.blitSprite(
                pipeline,
                sprite,
                placement.x(),
                placement.y(),
                placement.width(),
                placement.height(),
                argb(effects.opacity(), effects.textureMultiplier()));
    }

    private static void drawGlow(
            GuiGraphicsExtractor graphics,
            RenderPipeline pipeline,
            SelectorPlacement placement,
            SelectorVisualEffects effects,
            int radius,
            double weight) {
        graphics.blitSprite(
                pipeline,
                GLOW_SPRITE,
                placement.x() - radius,
                placement.y() - radius,
                placement.width() + radius * 2,
                placement.height() + radius * 2,
                argb(
                        effects.opacity() * effects.glowOpacity() * weight,
                        effects.glowColor()));
    }

    static int argb(double opacity, RgbColor color) {
        int alpha = (int) Math.round(Math.clamp(opacity, 0.0, 1.0) * 255.0);
        return (alpha << 24) | color.value();
    }

    private static Map<SelectorTheme, Identifier> createBuiltInSprites() {
        EnumMap<SelectorTheme, Identifier> sprites = new EnumMap<>(SelectorTheme.class);
        for (SelectorTheme theme : SelectorTheme.values()) {
            if (!theme.metadata().resourcePackBacked()) {
                sprites.put(
                        theme,
                        Identifier.fromNamespaceAndPath(
                                YashCore.MOD_ID, theme.metadata().spritePath()));
            }
        }
        return Map.copyOf(sprites);
    }
}
