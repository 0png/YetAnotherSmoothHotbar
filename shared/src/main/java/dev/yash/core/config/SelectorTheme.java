package dev.yash.core.config;

/** Built-in selector art choices. Animation behavior is configured separately. */
public enum SelectorTheme {
    VANILLA_RESOURCE_PACK("vanilla_resource_pack"),
    CLEAN("clean"),
    CREEPER("creeper"),
    ENDERMAN("enderman"),
    WOLF("wolf"),
    BEE("bee");

    private final String id;

    SelectorTheme(String id) {
        this.id = id;
    }

    /** Stable value used by configuration files; independent of the Java enum name. */
    public String id() {
        return id;
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
