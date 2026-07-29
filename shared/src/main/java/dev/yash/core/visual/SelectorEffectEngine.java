package dev.yash.core.visual;

import dev.yash.core.animation.AnimationFrame;
import dev.yash.core.config.EffectiveConfig;
import dev.yash.core.config.GlowSettings;
import dev.yash.core.config.RgbColor;
import dev.yash.core.config.TintSettings;
import java.util.Objects;

/** Stateful, renderer-independent visual effect sampler. */
public final class SelectorEffectEngine {
    public static final double PULSE_DURATION_SECONDS = 0.160;
    private static final double FULL_STRETCH_SPEED = 40.0;
    private static final double MAX_HORIZONTAL_STRETCH = 0.35;

    private long lastArrivalId;
    private double pulseStartSeconds = Double.NaN;

    public void reset() {
        lastArrivalId = 0L;
        pulseStartSeconds = Double.NaN;
    }

    public SelectorVisualEffects sample(
            AnimationFrame frame, EffectiveConfig config, double nowSeconds) {
        Objects.requireNonNull(frame, "frame");
        Objects.requireNonNull(config, "config");
        if (!Double.isFinite(nowSeconds)) {
            throw new IllegalArgumentException("Effect timestamp must be finite");
        }

        if (frame.arrivalId() != lastArrivalId) {
            pulseStartSeconds = frame.arrived() && config.pulse().enabled()
                    ? nowSeconds
                    : Double.NaN;
            lastArrivalId = frame.arrivalId();
        }
        if (!config.pulse().enabled()) {
            pulseStartSeconds = Double.NaN;
        }

        double pulse = pulseAmount(config, nowSeconds);
        double speedActivity = Math.min(1.0, Math.abs(frame.velocity()) / FULL_STRETCH_SPEED);
        double stretch = config.stretch().enabled()
                ? config.stretch().strength() * MAX_HORIZONTAL_STRETCH * speedActivity
                : 0.0;
        double pulseScale = 1.0 + pulse;

        GlowSettings glow = config.glow();
        double glowActivity = glow.animated()
                ? 0.65 + 0.35 * Math.max(speedActivity, normalizedPulse(config, pulse))
                : 1.0;
        double glowOpacity = glow.enabled() ? glow.strength() * glowActivity : 0.0;

        return new SelectorVisualEffects(
                config.scale() * pulseScale * (1.0 + stretch),
                config.scale() * pulseScale,
                config.opacity(),
                tintMultiplier(config.tint()),
                glow.enabled() && glowOpacity > 0.0 && glow.radius() > 0.0,
                glow.color(),
                glowOpacity,
                glow.radius());
    }

    private double pulseAmount(EffectiveConfig config, double nowSeconds) {
        if (!Double.isFinite(pulseStartSeconds)) {
            return 0.0;
        }
        double elapsed = Math.max(0.0, nowSeconds - pulseStartSeconds);
        if (elapsed >= PULSE_DURATION_SECONDS) {
            pulseStartSeconds = Double.NaN;
            return 0.0;
        }
        double remaining = 1.0 - elapsed / PULSE_DURATION_SECONDS;
        return config.pulse().strength() * remaining * remaining;
    }

    private static double normalizedPulse(EffectiveConfig config, double pulse) {
        return config.pulse().strength() > 0.0
                ? Math.min(1.0, pulse / config.pulse().strength())
                : 0.0;
    }

    private static RgbColor tintMultiplier(TintSettings tint) {
        if (!tint.enabled() || tint.strength() == 0.0) {
            return RgbColor.WHITE;
        }
        int source = tint.color().value();
        int red = blendChannel((source >>> 16) & 0xFF, tint.strength());
        int green = blendChannel((source >>> 8) & 0xFF, tint.strength());
        int blue = blendChannel(source & 0xFF, tint.strength());
        return new RgbColor((red << 16) | (green << 8) | blue);
    }

    private static int blendChannel(int tintChannel, double strength) {
        return (int) Math.round(255.0 * (1.0 - strength) + tintChannel * strength);
    }
}
