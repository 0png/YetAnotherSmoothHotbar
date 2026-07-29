package dev.yash.core.config;

/** Indicates that a configuration document cannot be safely interpreted. */
public final class ConfigFormatException extends Exception {
    public ConfigFormatException(String message) {
        super(message);
    }

    public ConfigFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
