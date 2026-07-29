package dev.yash.core.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.yash.core.animation.AnimationConfig;
import dev.yash.core.animation.AnimationPreset;
import dev.yash.core.animation.WrapMode;

/** Ordered schema migration pipeline. Version 0 represents the pre-schema flat prototype. */
final class ConfigMigrator {
    private ConfigMigrator() {
    }

    static MigrationResult migrate(JsonObject source, JsonObject defaults)
            throws ConfigFormatException {
        int version = schemaVersion(source);
        if (version > ConfigCodec.CURRENT_SCHEMA_VERSION) {
            throw new ConfigFormatException(
                    "Configuration schema " + version + " is newer than supported schema "
                            + ConfigCodec.CURRENT_SCHEMA_VERSION);
        }
        if (version < 0) {
            throw new ConfigFormatException("Configuration schema version cannot be negative");
        }

        JsonObject migrated = source.deepCopy();
        boolean changed = false;
        while (version < ConfigCodec.CURRENT_SCHEMA_VERSION) {
            if (version == 0) {
                migrated = migrateVersionZero(migrated, defaults);
                version = 1;
                changed = true;
            } else {
                throw new ConfigFormatException("No migration registered for schema " + version);
            }
        }
        return new MigrationResult(migrated, changed);
    }

    private static int schemaVersion(JsonObject source) throws ConfigFormatException {
        JsonElement element = source.get("schemaVersion");
        if (element == null) {
            if (source.has("animationPreset") || source.has("preset") || source.has("durationSeconds")) {
                return 0;
            }
            throw new ConfigFormatException("Missing required field: schemaVersion");
        }
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw new ConfigFormatException("schemaVersion must be an integer");
        }
        double numeric = element.getAsDouble();
        if (!Double.isFinite(numeric)
                || numeric != Math.rint(numeric)
                || numeric < Integer.MIN_VALUE
                || numeric > Integer.MAX_VALUE) {
            throw new ConfigFormatException("schemaVersion must be an integer");
        }
        return element.getAsInt();
    }

    private static JsonObject migrateVersionZero(JsonObject source, JsonObject defaults) {
        JsonObject result = defaults.deepCopy();
        copy(source, "enabled", result, "enabled");
        copy(source, "theme", result, "theme");
        copy(source, "reducedMotion", result, "reducedMotion");

        JsonObject animation = result.getAsJsonObject("animation");
        applyLegacyPresetDefaults(source, animation);
        copyFirst(source, "animationPreset", "preset", animation, "preset");
        copy(source, "easing", animation, "easing");
        copyFirst(source, "wrapMode", "wrap", animation, "wrapMode");
        copy(source, "springStrength", animation, "springStrength");
        copy(source, "springDampingRatio", animation, "springDampingRatio");
        copy(source, "springOvershootLimit", animation, "springOvershootLimit");
        if (source.has("durationMillis")) {
            animation.add("durationMillis", source.get("durationMillis").deepCopy());
        } else if (source.has("durationSeconds")) {
            JsonElement duration = source.get("durationSeconds");
            if (duration.isJsonPrimitive() && duration.getAsJsonPrimitive().isNumber()) {
                animation.addProperty("durationMillis", duration.getAsDouble() * 1_000.0);
            } else {
                animation.add("durationMillis", duration.deepCopy());
            }
        }

        JsonObject effects = result.getAsJsonObject("effects");
        copy(source, "opacity", effects, "opacity");
        copy(source, "scale", effects, "scale");
        migrateEffect(source, effects.getAsJsonObject("tint"), "tint", true);
        migrateEffect(source, effects.getAsJsonObject("glow"), "glow", true);
        migrateEffect(source, effects.getAsJsonObject("stretch"), "stretch", false);
        migrateEffect(source, effects.getAsJsonObject("pulse"), "pulse", false);
        result.addProperty("schemaVersion", ConfigCodec.CURRENT_SCHEMA_VERSION);
        return result;
    }

    private static void applyLegacyPresetDefaults(JsonObject source, JsonObject animation) {
        JsonElement presetElement = source.has("animationPreset")
                ? source.get("animationPreset")
                : source.get("preset");
        if (presetElement == null
                || !presetElement.isJsonPrimitive()
                || !presetElement.getAsJsonPrimitive().isString()) {
            return;
        }

        try {
            AnimationPreset preset = AnimationPreset.fromId(presetElement.getAsString());
            WrapMode wrapMode = legacyWrapMode(source);
            AnimationConfig config = preset.config(wrapMode);
            animation.addProperty("preset", preset.id());
            animation.addProperty("durationMillis", config.durationSeconds() * 1_000.0);
            animation.addProperty("easing", config.easing().id());
            animation.addProperty("wrapMode", config.wrapMode().id());
            animation.addProperty("springStrength", config.springStrength());
            animation.addProperty("springDampingRatio", config.springDampingRatio());
            animation.addProperty("springOvershootLimit", config.springOvershootLimit());
        } catch (IllegalArgumentException ignored) {
            // Preserve unknown source values so strict decoding can reject and back them up.
        }
    }

    private static WrapMode legacyWrapMode(JsonObject source) {
        JsonElement wrapElement = source.has("wrapMode") ? source.get("wrapMode") : source.get("wrap");
        if (wrapElement != null
                && wrapElement.isJsonPrimitive()
                && wrapElement.getAsJsonPrimitive().isString()) {
            try {
                return WrapMode.fromId(wrapElement.getAsString());
            } catch (IllegalArgumentException ignored) {
                // Preserve the safe default here; strict decoding later rejects the copied value.
            }
        }
        return WrapMode.ACROSS;
    }

    private static void migrateEffect(
            JsonObject source,
            JsonObject destination,
            String prefix,
            boolean hasColor) {
        copy(source, prefix + "Enabled", destination, "enabled");
        copy(source, prefix + "Strength", destination, "strength");
        if (hasColor) {
            copy(source, prefix + "Color", destination, "color");
        }
        if (prefix.equals("glow")) {
            copy(source, "glowRadius", destination, "radius");
            copy(source, "glowAnimated", destination, "animated");
        }
    }

    private static void copy(
            JsonObject source,
            String sourceName,
            JsonObject destination,
            String destinationName) {
        if (source.has(sourceName)) {
            destination.add(destinationName, source.get(sourceName).deepCopy());
        }
    }

    private static void copyFirst(
            JsonObject source,
            String firstName,
            String secondName,
            JsonObject destination,
            String destinationName) {
        if (source.has(firstName)) {
            destination.add(destinationName, source.get(firstName).deepCopy());
        } else {
            copy(source, secondName, destination, destinationName);
        }
    }

    record MigrationResult(JsonObject document, boolean migrated) {
    }
}
