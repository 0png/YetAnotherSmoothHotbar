package dev.yash.core.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import dev.yash.core.animation.AnimationConfig;
import dev.yash.core.animation.AnimationPreset;
import dev.yash.core.animation.EasingType;
import dev.yash.core.animation.WrapMode;
import java.util.Objects;

/** Strict, versioned JSON codec for YASH player preferences. */
public final class ConfigCodec {
    public static final int CURRENT_SCHEMA_VERSION = 1;

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public String encode(YashConfig config) {
        return gson.toJson(encodeObject(Objects.requireNonNull(config, "config"))) + System.lineSeparator();
    }

    public DecodedConfig decode(String json) throws ConfigFormatException {
        Objects.requireNonNull(json, "json");
        try {
            JsonElement parsed = JsonParser.parseString(json);
            if (!parsed.isJsonObject()) {
                throw new ConfigFormatException("Configuration root must be a JSON object");
            }

            ConfigMigrator.MigrationResult migration = ConfigMigrator.migrate(
                    parsed.getAsJsonObject(), encodeObject(YashConfig.defaults()));
            return new DecodedConfig(decodeObject(migration.document()), migration.migrated());
        } catch (ConfigFormatException exception) {
            throw exception;
        } catch (JsonParseException | IllegalArgumentException | IllegalStateException exception) {
            throw new ConfigFormatException("Invalid YASH configuration: " + exception.getMessage(), exception);
        }
    }

    private JsonObject encodeObject(YashConfig config) {
        JsonObject root = new JsonObject();
        root.addProperty("schemaVersion", CURRENT_SCHEMA_VERSION);
        root.addProperty("enabled", config.enabled());

        AnimationConfig animationConfig = config.animation();
        JsonObject animation = new JsonObject();
        animation.addProperty("preset", animationConfig.preset().id());
        animation.addProperty("durationMillis", animationConfig.durationSeconds() * 1_000.0);
        animation.addProperty("easing", animationConfig.easing().id());
        animation.addProperty("wrapMode", animationConfig.wrapMode().id());
        animation.addProperty("springStrength", animationConfig.springStrength());
        animation.addProperty("springDampingRatio", animationConfig.springDampingRatio());
        animation.addProperty("springOvershootLimit", animationConfig.springOvershootLimit());
        root.add("animation", animation);

        root.addProperty("theme", config.theme().id());
        JsonObject effects = new JsonObject();
        effects.add("tint", encodeTint(config.tint()));
        effects.addProperty("opacity", config.opacity());
        effects.addProperty("scale", config.scale());
        effects.add("glow", encodeGlow(config.glow()));
        effects.add("stretch", encodeToggleStrength(config.stretch().enabled(), config.stretch().strength()));
        effects.add("pulse", encodeToggleStrength(config.pulse().enabled(), config.pulse().strength()));
        root.add("effects", effects);
        root.addProperty("reducedMotion", config.reducedMotion());
        return root;
    }

    private YashConfig decodeObject(JsonObject root) throws ConfigFormatException {
        int version = requireInteger(root, "schemaVersion");
        if (version != CURRENT_SCHEMA_VERSION) {
            throw new ConfigFormatException("Expected migrated schema " + CURRENT_SCHEMA_VERSION);
        }

        JsonObject animation = requireObject(root, "animation");
        AnimationConfig animationConfig = new AnimationConfig(
                AnimationPreset.fromId(requireString(animation, "preset")),
                requireDouble(animation, "durationMillis") / 1_000.0,
                EasingType.fromId(requireString(animation, "easing")),
                WrapMode.fromId(requireString(animation, "wrapMode")),
                requireDouble(animation, "springStrength"),
                requireDouble(animation, "springDampingRatio"),
                requireDouble(animation, "springOvershootLimit"));

        JsonObject effects = requireObject(root, "effects");
        JsonObject tint = requireObject(effects, "tint");
        JsonObject glow = requireObject(effects, "glow");
        JsonObject stretch = requireObject(effects, "stretch");
        JsonObject pulse = requireObject(effects, "pulse");
        return new YashConfig(
                requireBoolean(root, "enabled"),
                animationConfig,
                SelectorTheme.fromId(requireString(root, "theme")),
                new TintSettings(
                        requireBoolean(tint, "enabled"),
                        RgbColor.parse(requireString(tint, "color")),
                        requireDouble(tint, "strength")),
                requireDouble(effects, "opacity"),
                requireDouble(effects, "scale"),
                new GlowSettings(
                        requireBoolean(glow, "enabled"),
                        RgbColor.parse(requireString(glow, "color")),
                        requireDouble(glow, "strength"),
                        requireDouble(glow, "radius"),
                        requireBoolean(glow, "animated")),
                new StretchSettings(
                        requireBoolean(stretch, "enabled"),
                        requireDouble(stretch, "strength")),
                new PulseSettings(
                        requireBoolean(pulse, "enabled"),
                        requireDouble(pulse, "strength")),
                requireBoolean(root, "reducedMotion"));
    }

    private static JsonObject encodeTint(TintSettings tint) {
        JsonObject object = new JsonObject();
        object.addProperty("enabled", tint.enabled());
        object.addProperty("color", tint.color().hex());
        object.addProperty("strength", tint.strength());
        return object;
    }

    private static JsonObject encodeGlow(GlowSettings glow) {
        JsonObject object = new JsonObject();
        object.addProperty("enabled", glow.enabled());
        object.addProperty("color", glow.color().hex());
        object.addProperty("strength", glow.strength());
        object.addProperty("radius", glow.radius());
        object.addProperty("animated", glow.animated());
        return object;
    }

    private static JsonObject encodeToggleStrength(boolean enabled, double strength) {
        JsonObject object = new JsonObject();
        object.addProperty("enabled", enabled);
        object.addProperty("strength", strength);
        return object;
    }

    private static JsonElement require(JsonObject object, String name) throws ConfigFormatException {
        JsonElement element = object.get(name);
        if (element == null || element.isJsonNull()) {
            throw new ConfigFormatException("Missing required field: " + name);
        }
        return element;
    }

    private static JsonObject requireObject(JsonObject object, String name)
            throws ConfigFormatException {
        JsonElement element = require(object, name);
        if (!element.isJsonObject()) {
            throw new ConfigFormatException(name + " must be an object");
        }
        return element.getAsJsonObject();
    }

    private static String requireString(JsonObject object, String name)
            throws ConfigFormatException {
        JsonElement element = require(object, name);
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            throw new ConfigFormatException(name + " must be a string");
        }
        return element.getAsString();
    }

    private static boolean requireBoolean(JsonObject object, String name)
            throws ConfigFormatException {
        JsonElement element = require(object, name);
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isBoolean()) {
            throw new ConfigFormatException(name + " must be a boolean");
        }
        return element.getAsBoolean();
    }

    private static double requireDouble(JsonObject object, String name)
            throws ConfigFormatException {
        JsonElement element = require(object, name);
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw new ConfigFormatException(name + " must be a number");
        }
        double value = element.getAsDouble();
        if (!Double.isFinite(value)) {
            throw new ConfigFormatException(name + " must be finite");
        }
        return value;
    }

    private static int requireInteger(JsonObject object, String name)
            throws ConfigFormatException {
        double value = requireDouble(object, name);
        if (value != Math.rint(value) || value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new ConfigFormatException(name + " must be an integer");
        }
        return (int) value;
    }
}
