package dev.yash.mc.v26_2.hud;

import dev.yash.core.animation.AnimationConfig;
import dev.yash.core.animation.AnimationEngine;
import dev.yash.core.animation.AnimationFrame;
import dev.yash.core.config.EffectiveConfig;
import dev.yash.core.config.YashConfig;
import java.util.Objects;

/**
 * Owns selector animation state and invalidates it when the HUD coordinate space or player
 * lifecycle changes. Input devices are deliberately absent: selected-slot state is the source of
 * truth.
 */
public final class HotbarAnimationController {
    static final double MAX_FRAME_GAP_SECONDS = 1.0;

    private YashConfig storedConfig;
    private EffectiveConfig effectiveConfig;
    private AnimationEngine engine;
    private boolean resetPending = true;
    private boolean lastUpdateReset;
    private Object levelIdentity;
    private Object playerIdentity;
    private boolean playerAlive;
    private int guiWidth;
    private int guiHeight;
    private int guiScale;
    private double lastTimestamp;

    public HotbarAnimationController(YashConfig config) {
        setConfig(config);
    }

    public YashConfig config() {
        return storedConfig;
    }

    /** Re-resolves runtime accessibility policy only when the saved configuration changes. */
    public void setConfig(YashConfig config) {
        YashConfig replacement = Objects.requireNonNull(config, "config");
        if (replacement.equals(storedConfig)) {
            return;
        }
        storedConfig = replacement;
        effectiveConfig = replacement.effective();
        if (!effectiveConfig.active()) {
            resetPending = true;
        }
    }

    /** Forces the next visible selector frame to align to the current slot. */
    public void resetForLifecycle() {
        resetPending = true;
    }

    public boolean lastUpdateReset() {
        return lastUpdateReset;
    }

    /**
     * Returns an animated frame when YASH owns selector rendering, or {@code null} when vanilla
     * must draw its selector.
     */
    public AnimationFrame update(
            int selectedSlot,
            double nowSeconds,
            Object currentLevelIdentity,
            Object currentPlayerIdentity,
            boolean currentPlayerAlive,
            int currentGuiWidth,
            int currentGuiHeight,
            int currentGuiScale) {
        validateContext(
                selectedSlot, nowSeconds, currentGuiWidth, currentGuiHeight, currentGuiScale);
        lastUpdateReset = false;
        if (!effectiveConfig.active()) {
            engine = null;
            resetPending = true;
            return null;
        }

        boolean contextChanged = engine == null
                || levelIdentity != currentLevelIdentity
                || playerIdentity != currentPlayerIdentity
                || playerAlive != currentPlayerAlive
                || guiWidth != currentGuiWidth
                || guiHeight != currentGuiHeight
                || guiScale != currentGuiScale;
        boolean unsafeTime = engine != null
                && (nowSeconds < lastTimestamp
                        || nowSeconds - lastTimestamp > MAX_FRAME_GAP_SECONDS);
        AnimationConfig animationConfig = effectiveConfig.animation();

        AnimationFrame frame;
        if (resetPending || contextChanged || unsafeTime) {
            engine = new AnimationEngine(animationConfig);
            frame = engine.reset(selectedSlot, nowSeconds);
            resetPending = false;
            lastUpdateReset = true;
        } else {
            if (!engine.config().equals(animationConfig)) {
                engine.setConfig(animationConfig, nowSeconds);
            }
            frame = engine.update(selectedSlot, nowSeconds);
        }

        levelIdentity = currentLevelIdentity;
        playerIdentity = currentPlayerIdentity;
        playerAlive = currentPlayerAlive;
        guiWidth = currentGuiWidth;
        guiHeight = currentGuiHeight;
        guiScale = currentGuiScale;
        lastTimestamp = nowSeconds;
        return frame;
    }

    private static void validateContext(
            int selectedSlot,
            double nowSeconds,
            int guiWidth,
            int guiHeight,
            int guiScale) {
        if (selectedSlot < 0 || selectedSlot >= AnimationEngine.HOTBAR_SLOT_COUNT) {
            throw new IllegalArgumentException("Selected slot must be in range [0, 8]");
        }
        if (!Double.isFinite(nowSeconds)) {
            throw new IllegalArgumentException("HUD timestamp must be finite");
        }
        if (guiWidth <= 0 || guiHeight <= 0 || guiScale <= 0) {
            throw new IllegalArgumentException("HUD dimensions and GUI scale must be positive");
        }
    }
}
