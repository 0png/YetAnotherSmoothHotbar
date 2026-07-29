package dev.yash.core.config;

/** How a configuration became available for this session. */
public enum ConfigLoadSource {
    DEFAULTS,
    FILE,
    MIGRATED,
    RECOVERED
}
