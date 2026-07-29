package dev.yash.core.config;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/** Atomic local persistence with corrupt-file backup and non-throwing recovery. */
public final class ConfigStore {
    private final Path configPath;
    private final ConfigCodec codec;
    private final Consumer<String> warningSink;

    public ConfigStore(Path configPath, Consumer<String> warningSink) {
        this(configPath, new ConfigCodec(), warningSink);
    }

    public ConfigStore(Path configPath, ConfigCodec codec, Consumer<String> warningSink) {
        this.configPath = Objects.requireNonNull(configPath, "configPath")
                .toAbsolutePath()
                .normalize();
        this.codec = Objects.requireNonNull(codec, "codec");
        this.warningSink = Objects.requireNonNull(warningSink, "warningSink");
    }

    public Path configPath() {
        return configPath;
    }

    /** Loads preferences without propagating malformed input or filesystem errors to the game. */
    public ConfigLoadResult load() {
        if (Files.notExists(configPath)) {
            return result(YashConfig.defaults(), ConfigLoadSource.DEFAULTS, Optional.empty());
        }

        try {
            DecodedConfig decoded = codec.decode(Files.readString(configPath, StandardCharsets.UTF_8));
            if (decoded.migrated()) {
                save(decoded.config());
                return result(decoded.config(), ConfigLoadSource.MIGRATED, Optional.empty());
            }
            return result(decoded.config(), ConfigLoadSource.FILE, Optional.empty());
        } catch (IOException | ConfigFormatException | RuntimeException exception) {
            return recover(exception);
        }
    }

    /** Writes preferences atomically where supported, returning false instead of crashing on failure. */
    public boolean save(YashConfig config) {
        Objects.requireNonNull(config, "config");
        try {
            writeAtomically(codec.encode(config));
            return true;
        } catch (IOException | RuntimeException exception) {
            warningSink.accept("Unable to save YASH configuration at " + configPath + ": "
                    + exception.getMessage());
            return false;
        }
    }

    private ConfigLoadResult recover(Exception cause) {
        Optional<Path> backup = backupCorruptFile();
        warningSink.accept("Invalid YASH configuration at " + configPath
                + "; using safe defaults. Cause: " + cause.getMessage());
        if (backup.isPresent()) {
            save(YashConfig.defaults());
        } else {
            warningSink.accept("The invalid configuration could not be backed up and was left unchanged");
        }
        return result(YashConfig.defaults(), ConfigLoadSource.RECOVERED, backup);
    }

    private Optional<Path> backupCorruptFile() {
        try {
            Path backup = nextBackupPath();
            Files.copy(configPath, backup, StandardCopyOption.COPY_ATTRIBUTES);
            return Optional.of(backup);
        } catch (IOException | RuntimeException exception) {
            warningSink.accept("Unable to back up invalid YASH configuration at " + configPath + ": "
                    + exception.getMessage());
            return Optional.empty();
        }
    }

    private Path nextBackupPath() {
        String baseName = configPath.getFileName() + ".corrupt-" + System.currentTimeMillis();
        Path candidate = configPath.resolveSibling(baseName + ".bak");
        int suffix = 1;
        while (Files.exists(candidate)) {
            candidate = configPath.resolveSibling(baseName + "-" + suffix + ".bak");
            suffix++;
        }
        return candidate;
    }

    private void writeAtomically(String json) throws IOException {
        Path parent = configPath.getParent();
        if (parent == null) {
            throw new IOException("Configuration path has no parent directory");
        }
        Files.createDirectories(parent);
        Path temporary = Files.createTempFile(parent, "." + configPath.getFileName(), ".tmp");
        try {
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            try (FileChannel channel = FileChannel.open(
                    temporary,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                while (buffer.hasRemaining()) {
                    channel.write(buffer);
                }
                channel.force(true);
            }
            try {
                Files.move(
                        temporary,
                        configPath,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, configPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static ConfigLoadResult result(
            YashConfig config,
            ConfigLoadSource source,
            Optional<Path> backup) {
        return new ConfigLoadResult(config, source, backup);
    }
}
