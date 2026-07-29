package dev.yash.core.config;

/** One-shot scale pulse emitted when the latest selector transition arrives. */
public record PulseSettings(boolean enabled, double strength) {
    public PulseSettings {
        SettingRanges.require(strength, 0.0, 1.0, "pulse strength");
    }

    public static PulseSettings defaults() {
        return new PulseSettings(false, 0.08);
    }

    public PulseSettings disabled() {
        return enabled ? new PulseSettings(false, strength) : this;
    }
}
