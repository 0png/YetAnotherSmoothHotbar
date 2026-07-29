package dev.yash.core.config;

/** Horizontal selector stretch driven by movement speed. */
public record StretchSettings(boolean enabled, double strength) {
    public StretchSettings {
        SettingRanges.require(strength, 0.0, 1.0, "stretch strength");
    }

    public static StretchSettings defaults() {
        return new StretchSettings(false, 0.15);
    }

    public StretchSettings disabled() {
        return enabled ? new StretchSettings(false, strength) : this;
    }
}
