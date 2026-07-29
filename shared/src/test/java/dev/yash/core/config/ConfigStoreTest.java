package dev.yash.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.yash.core.animation.AnimationConfig;
import dev.yash.core.animation.AnimationPreset;
import dev.yash.core.animation.EasingType;
import dev.yash.core.animation.WrapMode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class ConfigStoreTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void firstLaunchUsesSafeDefaultsWithoutWarning() {
        List<String> warnings = new ArrayList<>();
        ConfigStore store = store(temporaryDirectory.resolve("config/yash.json"), warnings);

        ConfigLoadResult result = store.load();

        assertEquals(YashConfig.defaults(), result.config());
        assertEquals(ConfigLoadSource.DEFAULTS, result.source());
        assertTrue(result.backupPath().isEmpty());
        assertTrue(warnings.isEmpty());
    }

    @Test
    void atomicSavePreservesEveryFieldAcrossRestart() throws IOException {
        Path path = temporaryDirectory.resolve("config/yash.json");
        List<String> warnings = new ArrayList<>();
        ConfigStore store = store(path, warnings);
        YashConfig expected = completeCustomConfig();

        assertTrue(store.save(expected));
        ConfigLoadResult reloaded = store(path, warnings).load();

        assertEquals(expected, reloaded.config());
        assertEquals(ConfigLoadSource.FILE, reloaded.source());
        assertTrue(warnings.isEmpty());
        try (var entries = Files.list(path.getParent())) {
            assertTrue(entries.noneMatch(file -> file.getFileName().toString().endsWith(".tmp")));
        }
    }

    @Test
    void malformedFileIsBackedUpWarnedAndReplacedWithDefaults() throws Exception {
        Path path = temporaryDirectory.resolve("yash.json");
        String corrupt = "{ definitely not valid json";
        Files.writeString(path, corrupt, StandardCharsets.UTF_8);
        List<String> warnings = new ArrayList<>();

        ConfigLoadResult result = store(path, warnings).load();

        assertEquals(ConfigLoadSource.RECOVERED, result.source());
        assertEquals(YashConfig.defaults(), result.config());
        assertTrue(result.backupPath().isPresent());
        assertEquals(corrupt, Files.readString(result.backupPath().orElseThrow()));
        assertEquals(YashConfig.defaults(), new ConfigCodec().decode(Files.readString(path)).config());
        assertTrue(warnings.stream().anyMatch(message -> message.contains("safe defaults")));
    }

    @Test
    void missingVersionOneFieldUsesTheSameSafeRecoveryPath() throws Exception {
        Path path = temporaryDirectory.resolve("yash.json");
        String incomplete = new ConfigCodec().encode(YashConfig.defaults())
                .replace("  \"theme\": \"vanilla_resource_pack\",\n", "");
        Files.writeString(path, incomplete, StandardCharsets.UTF_8);

        ConfigLoadResult result = store(path, new ArrayList<>()).load();

        assertEquals(ConfigLoadSource.RECOVERED, result.source());
        assertTrue(result.backupPath().isPresent());
        assertEquals(incomplete, Files.readString(result.backupPath().orElseThrow()));
    }

    @Test
    void legacyFileIsMigratedAndRewrittenAsCurrentSchema() throws IOException {
        Path path = temporaryDirectory.resolve("yash.json");
        Files.writeString(path, """
                {
                  "animationPreset": "smooth",
                  "durationSeconds": 0.175,
                  "easing": "ease_in_out_cubic",
                  "wrapMode": "wrap"
                }
                """, StandardCharsets.UTF_8);

        ConfigLoadResult result = store(path, new ArrayList<>()).load();

        assertEquals(ConfigLoadSource.MIGRATED, result.source());
        assertEquals(AnimationPreset.SMOOTH, result.config().animation().preset());
        assertEquals(WrapMode.WRAP, result.config().animation().wrapMode());
        assertTrue(Files.readString(path).contains("\"schemaVersion\": 1"));
    }

    @Test
    void filesystemFailureReturnsFalseAndReportsWarning() throws IOException {
        Path parentFile = temporaryDirectory.resolve("not-a-directory");
        Files.writeString(parentFile, "occupied");
        List<String> warnings = new ArrayList<>();
        ConfigStore store = store(parentFile.resolve("yash.json"), warnings);

        assertFalse(store.save(YashConfig.defaults()));
        assertFalse(warnings.isEmpty());
    }

    private static ConfigStore store(Path path, List<String> warnings) {
        return new ConfigStore(path, warnings::add);
    }

    private static YashConfig completeCustomConfig() {
        return new YashConfig(
                false,
                new AnimationConfig(
                        AnimationPreset.CUSTOM,
                        0.287,
                        EasingType.SPRING,
                        WrapMode.WRAP,
                        28.0,
                        0.61,
                        0.21),
                SelectorTheme.ENDERMAN,
                new TintSettings(true, new RgbColor(0x7A31CC), 0.42),
                0.67,
                1.18,
                new GlowSettings(true, new RgbColor(0xAA44FF), 0.73, 7.0, true),
                new StretchSettings(true, 0.37),
                new PulseSettings(true, 0.29),
                true);
    }
}
