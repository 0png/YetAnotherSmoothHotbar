package dev.yash.mc.v26_2;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.yash.core.animation.AnimationFrame;
import dev.yash.core.animation.WrapMode;
import dev.yash.core.config.ConfigLoadResult;
import dev.yash.core.config.ConfigStore;
import dev.yash.core.config.YashConfig;
import dev.yash.mc.v26_2.hud.HotbarAnimationController;
import dev.yash.mc.v26_2.hud.HotbarLayout;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

/** Minecraft 26.2 client integration, independent of any loader API. */
public final class YashMinecraftClient {
    private static final double NANOS_TO_SECONDS = 1.0e-9;

    private static YashMinecraftClient instance;

    private final ConfigStore configStore;
    private final HotbarAnimationController animationController;

    private YashMinecraftClient(ConfigStore configStore, YashConfig config) {
        this.configStore = configStore;
        this.animationController = new HotbarAnimationController(config);
    }

    /** Initializes the version layer from a loader-provided configuration path. */
    public static ConfigLoadResult initialize(Path configPath, Consumer<String> warningSink) {
        ConfigStore store = new ConfigStore(
                Objects.requireNonNull(configPath, "configPath"),
                Objects.requireNonNull(warningSink, "warningSink"));
        ConfigLoadResult loaded = store.load();
        instance = new YashMinecraftClient(store, loaded.config());
        return loaded;
    }

    public static boolean initialized() {
        return instance != null;
    }

    public static YashConfig config() {
        return requireInstance().animationController.config();
    }

    /** Applies settings immediately; persistence is optional for live previews. */
    public static boolean applyConfig(YashConfig config, boolean persist) {
        YashMinecraftClient client = requireInstance();
        client.animationController.setConfig(Objects.requireNonNull(config, "config"));
        return !persist || client.configStore.save(config);
    }

    /** Invalidates coordinates after HUD, world, window, or resource lifecycle changes. */
    public static void resetAnimation() {
        if (instance != null) {
            instance.animationController.resetForLifecycle();
        }
    }

    /**
     * Replaces exactly one vanilla selector draw. Returns false when the wrapped vanilla call must
     * proceed unchanged.
     */
    public static boolean renderHotbarSelector(
            GuiGraphicsExtractor graphics,
            RenderPipeline pipeline,
            Identifier sprite,
            int vanillaX,
            int y,
            int width,
            int height) {
        YashMinecraftClient client = instance;
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (client == null || player == null || minecraft.level == null) {
            return false;
        }

        int selectedSlot = player.getInventory().getSelectedSlot();
        AnimationFrame frame = client.animationController.update(
                selectedSlot,
                Util.getNanos() * NANOS_TO_SECONDS,
                minecraft.level,
                player,
                player.isAlive(),
                graphics.guiWidth(),
                graphics.guiHeight(),
                minecraft.getWindow().getGuiScale());
        if (frame == null) {
            return false;
        }

        int selectorBaseX = HotbarLayout.selectorBaseX(vanillaX, selectedSlot);
        if (frame.wrapMode() == WrapMode.WRAP) {
            renderWrappedSelector(
                    graphics, pipeline, sprite, selectorBaseX, y, width, height, frame);
        } else {
            graphics.blitSprite(
                    pipeline,
                    sprite,
                    HotbarLayout.selectorX(selectorBaseX, frame.position()),
                    y,
                    width,
                    height);
        }
        return true;
    }

    private static void renderWrappedSelector(
            GuiGraphicsExtractor graphics,
            RenderPipeline pipeline,
            Identifier sprite,
            int selectorBaseX,
            int y,
            int width,
            int height,
            AnimationFrame frame) {
        double canonicalPosition = frame.wrappedPosition();
        graphics.enableScissor(
                selectorBaseX,
                y,
                selectorBaseX + HotbarLayout.SELECTOR_TRAVEL_DISTANCE + width,
                y + height);
        try {
            graphics.blitSprite(
                    pipeline,
                    sprite,
                    HotbarLayout.selectorX(selectorBaseX, canonicalPosition),
                    y,
                    width,
                    height);
            double seamPosition = HotbarLayout.wrapSeamPosition(frame);
            if (Double.isFinite(seamPosition)) {
                graphics.blitSprite(
                        pipeline,
                        sprite,
                        HotbarLayout.selectorX(selectorBaseX, seamPosition),
                        y,
                        width,
                        height);
            }
        } finally {
            graphics.disableScissor();
        }
    }

    private static YashMinecraftClient requireInstance() {
        if (instance == null) {
            throw new IllegalStateException("YASH Minecraft client has not been initialized");
        }
        return instance;
    }
}
