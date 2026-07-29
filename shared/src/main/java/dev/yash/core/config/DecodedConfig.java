package dev.yash.core.config;

import java.util.Objects;

/** Result of decoding, including whether an older schema was upgraded in memory. */
public record DecodedConfig(YashConfig config, boolean migrated) {
    public DecodedConfig {
        Objects.requireNonNull(config, "config");
    }
}
