package dev.yash.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.yash.core.animation.AnimationConfig;
import dev.yash.core.animation.AnimationPreset;
import dev.yash.core.animation.EasingType;
import dev.yash.core.animation.WrapMode;
import org.junit.jupiter.api.Test;

final class ConfigCodecTest {
    private final ConfigCodec codec = new ConfigCodec();

    @Test
    void everyFieldRoundTripsWithStableIdentifiers() throws Exception {
        YashConfig expected = customConfig();

        String json = codec.encode(expected);
        DecodedConfig decoded = codec.decode(json);

        assertEquals(expected, decoded.config());
        assertFalse(decoded.migrated());
        assertTrue(json.contains("\"preset\": \"custom\""));
        assertTrue(json.contains("\"theme\": \"bee\""));
        assertTrue(json.contains("\"easing\": \"spring\""));
        assertFalse(json.contains("CUSTOM"));
        assertFalse(json.contains("BEE"));
    }

    @Test
    void versionOneRequiresEveryFieldAndKnownStableIds() {
        String missingTheme = codec.encode(YashConfig.defaults())
                .replace("  \"theme\": \"vanilla_resource_pack\",\n", "");
        String unknownPreset = codec.encode(YashConfig.defaults())
                .replace("bedrock_inspired", "renamed_java_constant");
        String invalidOpacity = codec.encode(YashConfig.defaults())
                .replace("\"opacity\": 1.0", "\"opacity\": 0.0");

        assertThrows(ConfigFormatException.class, () -> codec.decode(missingTheme));
        assertThrows(ConfigFormatException.class, () -> codec.decode(unknownPreset));
        assertThrows(ConfigFormatException.class, () -> codec.decode(invalidOpacity));
        assertThrows(ConfigFormatException.class,
                () -> codec.decode("{\"schemaVersion\":2}"));
        assertThrows(ConfigFormatException.class,
                () -> codec.decode("{\"schemaVersion\":1e100}"));
        assertThrows(ConfigFormatException.class, () -> codec.decode("{}"));
    }

    @Test
    void manuallyChangedOfficialParametersAreReclassifiedAsCustom() throws Exception {
        String edited = codec.encode(YashConfig.defaults())
                .replace("\"durationMillis\": 115.0", "\"durationMillis\": 222.0");

        YashConfig decoded = codec.decode(edited).config();

        assertEquals(AnimationPreset.CUSTOM, decoded.animation().preset());
        assertEquals(0.222, decoded.animation().durationSeconds(), 1.0e-12);
    }

    @Test
    void flatVersionZeroConfigurationMigratesThroughTheRegisteredEntry() throws Exception {
        String legacy = """
                {
                  "enabled": false,
                  "animationPreset": "snappy",
                  "durationSeconds": 0.075,
                  "easing": "ease_out_cubic",
                  "wrapMode": "wrap",
                  "theme": "creeper",
                  "opacity": 0.7,
                  "scale": 1.2,
                  "glowEnabled": true,
                  "glowColor": "#12AB34",
                  "glowStrength": 0.6,
                  "reducedMotion": true
                }
                """;

        DecodedConfig result = codec.decode(legacy);

        assertTrue(result.migrated());
        assertFalse(result.config().enabled());
        assertEquals(AnimationPreset.SNAPPY, result.config().animation().preset());
        assertEquals(WrapMode.WRAP, result.config().animation().wrapMode());
        assertEquals(SelectorTheme.CREEPER, result.config().theme());
        assertEquals(0.7, result.config().opacity());
        assertEquals(1.2, result.config().scale());
        assertTrue(result.config().glow().enabled());
        assertEquals("#12AB34", result.config().glow().color().hex());
        assertTrue(result.config().reducedMotion());
    }

    @Test
    void legacyPresetWithoutAdvancedFieldsReceivesTheCompleteOfficialDefaults() throws Exception {
        DecodedConfig result = codec.decode("""
                {
                  "animationPreset": "spring",
                  "wrapMode": "wrap"
                }
                """);

        assertTrue(result.migrated());
        assertEquals(AnimationPreset.SPRING.config(WrapMode.WRAP), result.config().animation());
    }

    @Test
    void allEnumIdsCanBeParsedWithoutDependingOnEnumNames() {
        for (AnimationPreset preset : AnimationPreset.values()) {
            assertEquals(preset, AnimationPreset.fromId(preset.id()));
        }
        for (EasingType easing : EasingType.values()) {
            assertEquals(easing, EasingType.fromId(easing.id()));
        }
        for (WrapMode wrapMode : WrapMode.values()) {
            assertEquals(wrapMode, WrapMode.fromId(wrapMode.id()));
        }
        for (SelectorTheme theme : SelectorTheme.values()) {
            assertEquals(theme, SelectorTheme.fromId(theme.id()));
        }
    }

    private static YashConfig customConfig() {
        return new YashConfig(
                true,
                new AnimationConfig(
                        AnimationPreset.CUSTOM,
                        0.234,
                        EasingType.SPRING,
                        WrapMode.WRAP,
                        31.0,
                        0.72,
                        0.18),
                SelectorTheme.BEE,
                new TintSettings(true, new RgbColor(0x123456), 0.65),
                0.8,
                1.3,
                new GlowSettings(true, new RgbColor(0xFEDCBA), 0.7, 5.5, true),
                new StretchSettings(true, 0.45),
                new PulseSettings(true, 0.35),
                true);
    }
}
