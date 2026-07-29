package dev.yash.core.animation;

/** Defines how a selector travels between hotbar slots. */
public enum WrapMode {
    /** Travel directly across the visible hotbar. */
    ACROSS("across"),

    /** Travel along the shortest path on a nine-slot circular hotbar. */
    WRAP("wrap");

    private final String id;

    WrapMode(String id) {
        this.id = id;
    }

    /** Stable value used by configuration files; independent of the Java enum name. */
    public String id() {
        return id;
    }

    public static WrapMode fromId(String id) {
        for (WrapMode value : values()) {
            if (value.id.equals(id)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown wrap mode id: " + id);
    }
}
