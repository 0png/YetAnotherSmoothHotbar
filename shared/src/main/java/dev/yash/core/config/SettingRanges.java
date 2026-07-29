package dev.yash.core.config;

final class SettingRanges {
    private SettingRanges() {
    }

    static double require(double value, double minimum, double maximum, String name) {
        if (!Double.isFinite(value) || value < minimum || value > maximum) {
            throw new IllegalArgumentException(
                    name + " must be finite and in range [" + minimum + ", " + maximum + "]");
        }
        return value;
    }
}
