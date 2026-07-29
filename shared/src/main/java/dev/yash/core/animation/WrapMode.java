package dev.yash.core.animation;

/** Defines how a selector travels between hotbar slots. */
public enum WrapMode {
    /** Travel directly across the visible hotbar. */
    ACROSS,

    /** Travel along the shortest path on a nine-slot circular hotbar. */
    WRAP
}
