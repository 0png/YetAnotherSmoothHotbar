package dev.yash.core.config;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/** Safe configuration load result with recovery provenance. */
public record ConfigLoadResult(
        YashConfig config,
        ConfigLoadSource source,
        Optional<Path> backupPath) {
    public ConfigLoadResult {
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(backupPath, "backupPath");
    }
}
