package dev.yash.core.config;

/** Built-in selector art choices. Animation behavior is configured separately. */
public enum SelectorTheme {
    VANILLA_RESOURCE_PACK(
            "vanilla_resource_pack",
            new ThemeMetadata("", 24, 23, 0, 0, RgbColor.WHITE, 0.0, 0.0)),
    CLEAN(
            "clean",
            new ThemeMetadata(
                    "hud/hotbar_selector/clean", 24, 24, 0, 0,
                    RgbColor.WHITE, 0.04, 0.05)),
    CREEPER(
            "creeper",
            new ThemeMetadata(
                    "hud/hotbar_selector/creeper", 26, 26, 0, 0,
                    new RgbColor(0x70E64A), 0.06, 0.08)),
    ENDERMAN(
            "enderman",
            new ThemeMetadata(
                    "hud/hotbar_selector/enderman", 26, 26, 0, 0,
                    new RgbColor(0xC14CFF), 0.08, 0.08)),
    WOLF(
            "wolf",
            new ThemeMetadata(
                    "hud/hotbar_selector/wolf", 28, 30, 0, -2,
                    new RgbColor(0xE8E5DE), 0.10, 0.06)),
    BEE(
            "bee",
            new ThemeMetadata(
                    "hud/hotbar_selector/bee", 32, 26, 0, 0,
                    new RgbColor(0xFFD24A), 0.08, 0.12));

    private final String id;
    private final ThemeMetadata metadata;

    SelectorTheme(String id, ThemeMetadata metadata) {
        this.id = id;
        this.metadata = metadata;
    }

    /** Stable value used by configuration files; independent of the Java enum name. */
    public String id() {
        return id;
    }

    public ThemeMetadata metadata() {
        return metadata;
    }

    /** Translation identifiers are deliberately separate from persisted configuration IDs. */
    public String translationKey() {
        return switch (this) {
            case VANILLA_RESOURCE_PACK -> "theme.yash.vanilla_resource_pack";
            case CLEAN -> "theme.yash.clean";
            case CREEPER -> "theme.yash.creeper";
            case ENDERMAN -> "theme.yash.enderman";
            case WOLF -> "theme.yash.wolf";
            case BEE -> "theme.yash.bee";
        };
    }

    public static SelectorTheme fromId(String id) {
        for (SelectorTheme value : values()) {
            if (value.id.equals(id)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown selector theme id: " + id);
    }
}
