package dev.yash.mc.v26_2.screen;

import dev.yash.core.animation.AnimationEngine;
import dev.yash.core.animation.AnimationFrame;
import dev.yash.core.config.EffectiveConfig;
import dev.yash.core.config.YashConfig;
import dev.yash.core.visual.SelectorEffectEngine;
import dev.yash.core.visual.SelectorVisualEffects;
import java.util.Objects;

/** Deterministic state machine for the settings screen's interactive hotbar preview. */
public final class HotbarPreviewController {
    private static final double CYCLE_INTERVAL_SECONDS = 0.65;
    private static final double RAPID_INTERVAL_SECONDS = 0.12;
    private static final int[] RAPID_SEQUENCE = {0, 4, 7, 2};

    private final AnimationEngine animationEngine;
    private final SelectorEffectEngine effectEngine = new SelectorEffectEngine();

    private YashConfig config;
    private PreviewMode mode = PreviewMode.MANUAL;
    private int selectedSlot = 4;
    private int sequenceIndex;
    private double nextSwitchSeconds = Double.POSITIVE_INFINITY;
    private double lastTimestamp;

    public HotbarPreviewController(YashConfig config, double nowSeconds) {
        validateTimestamp(nowSeconds);
        this.config = Objects.requireNonNull(config, "config");
        this.animationEngine = new AnimationEngine(config.effective().animation());
        this.animationEngine.reset(selectedSlot, nowSeconds);
        this.lastTimestamp = nowSeconds;
    }

    public PreviewMode mode() {
        return mode;
    }

    public int selectedSlot() {
        return selectedSlot;
    }

    /** Keeps the current visual position while applying a newly edited configuration. */
    public void setConfig(YashConfig newConfig, double nowSeconds) {
        ensureTimestamp(nowSeconds);
        config = Objects.requireNonNull(newConfig, "newConfig");
        animationEngine.setConfig(config.effective().animation(), nowSeconds);
        effectEngine.reset();
        lastTimestamp = nowSeconds;
    }

    public void selectSlot(int slot, double nowSeconds) {
        ensureTimestamp(nowSeconds);
        if (slot < 0 || slot >= AnimationEngine.HOTBAR_SLOT_COUNT) {
            throw new IllegalArgumentException("Preview slot must be in range [0, 8]");
        }
        mode = PreviewMode.MANUAL;
        selectedSlot = slot;
        nextSwitchSeconds = Double.POSITIVE_INFINITY;
        animationEngine.update(selectedSlot, nowSeconds);
        lastTimestamp = nowSeconds;
    }

    public void setMode(PreviewMode newMode, double nowSeconds) {
        ensureTimestamp(nowSeconds);
        mode = Objects.requireNonNull(newMode, "newMode");
        sequenceIndex = 0;
        switch (mode) {
            case MANUAL -> nextSwitchSeconds = Double.POSITIVE_INFINITY;
            case CYCLE -> nextSwitchSeconds = nowSeconds + CYCLE_INTERVAL_SECONDS;
            case EDGES -> {
                selectedSlot = selectedSlot == 0 ? 8 : 0;
                animationEngine.update(selectedSlot, nowSeconds);
                nextSwitchSeconds = nowSeconds + CYCLE_INTERVAL_SECONDS;
            }
            case RAPID -> {
                selectedSlot = RAPID_SEQUENCE[sequenceIndex++];
                animationEngine.update(selectedSlot, nowSeconds);
                nextSwitchSeconds = nowSeconds + RAPID_INTERVAL_SECONDS;
            }
        }
        lastTimestamp = nowSeconds;
    }

    public PreviewSample sample(double nowSeconds) {
        ensureTimestamp(nowSeconds);
        advanceScenario(nowSeconds);
        AnimationFrame frame = animationEngine.update(selectedSlot, nowSeconds);
        EffectiveConfig effective = config.effective();
        SelectorVisualEffects effects = effectEngine.sample(frame, effective, nowSeconds);
        lastTimestamp = nowSeconds;
        return new PreviewSample(frame, effective, effects);
    }

    private void advanceScenario(double nowSeconds) {
        while (nowSeconds >= nextSwitchSeconds) {
            double eventTime = nextSwitchSeconds;
            animationEngine.update(selectedSlot, eventTime);
            switch (mode) {
                case MANUAL -> nextSwitchSeconds = Double.POSITIVE_INFINITY;
                case CYCLE -> {
                    selectedSlot = (selectedSlot + 1) % AnimationEngine.HOTBAR_SLOT_COUNT;
                    animationEngine.update(selectedSlot, eventTime);
                    nextSwitchSeconds += CYCLE_INTERVAL_SECONDS;
                }
                case EDGES -> {
                    selectedSlot = selectedSlot == 0 ? 8 : 0;
                    animationEngine.update(selectedSlot, eventTime);
                    nextSwitchSeconds += CYCLE_INTERVAL_SECONDS;
                }
                case RAPID -> {
                    selectedSlot = RAPID_SEQUENCE[sequenceIndex++ % RAPID_SEQUENCE.length];
                    animationEngine.update(selectedSlot, eventTime);
                    nextSwitchSeconds += RAPID_INTERVAL_SECONDS;
                }
            }
        }
    }

    private void ensureTimestamp(double timestamp) {
        validateTimestamp(timestamp);
        if (timestamp < lastTimestamp) {
            throw new IllegalArgumentException("Preview timestamp cannot move backwards");
        }
    }

    private static void validateTimestamp(double timestamp) {
        if (!Double.isFinite(timestamp)) {
            throw new IllegalArgumentException("Preview timestamp must be finite");
        }
    }

    public record PreviewSample(
            AnimationFrame frame,
            EffectiveConfig config,
            SelectorVisualEffects effects) {
        public PreviewSample {
            Objects.requireNonNull(frame, "frame");
            Objects.requireNonNull(config, "config");
            Objects.requireNonNull(effects, "effects");
        }
    }
}
