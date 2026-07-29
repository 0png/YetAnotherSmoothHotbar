package dev.yash.core.animation;

/** Built-in animation profiles. Themes and visual effects are configured separately. */
public enum AnimationPreset {
    BEDROCK_INSPIRED("bedrock_inspired", 0.115, EasingType.EASE_OUT_QUINT, 22.0, 1.0, 0.0),
    SMOOTH("smooth", 0.175, EasingType.EASE_IN_OUT_CUBIC, 22.0, 1.0, 0.0),
    SNAPPY("snappy", 0.075, EasingType.EASE_OUT_CUBIC, 22.0, 1.0, 0.0),
    SPRING("spring", 0.420, EasingType.SPRING, 22.0, 0.68, 0.12),
    MINIMAL("minimal", 0.050, EasingType.EASE_OUT_QUAD, 22.0, 1.0, 0.0),
    CUSTOM("custom", 0.115, EasingType.EASE_OUT_CUBIC, 22.0, 0.75, 0.10),
    DISABLED("disabled", 0.0, EasingType.LINEAR, 22.0, 1.0, 0.0);

    private final String id;
    private final double durationSeconds;
    private final EasingType easing;
    private final double springStrength;
    private final double springDampingRatio;
    private final double springOvershootLimit;

    AnimationPreset(
            String id,
            double durationSeconds,
            EasingType easing,
            double springStrength,
            double springDampingRatio,
            double springOvershootLimit) {
        this.id = id;
        this.durationSeconds = durationSeconds;
        this.easing = easing;
        this.springStrength = springStrength;
        this.springDampingRatio = springDampingRatio;
        this.springOvershootLimit = springOvershootLimit;
    }

    /** Stable value used by configuration files; independent of the Java enum name. */
    public String id() {
        return id;
    }

    public static AnimationPreset fromId(String id) {
        for (AnimationPreset value : values()) {
            if (value.id.equals(id)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown animation preset id: " + id);
    }

    boolean matchesParameters(
            double candidateDurationSeconds,
            EasingType candidateEasing,
            double candidateSpringStrength,
            double candidateDampingRatio,
            double candidateOvershootLimit) {
        return durationSeconds == candidateDurationSeconds
                && easing == candidateEasing
                && springStrength == candidateSpringStrength
                && springDampingRatio == candidateDampingRatio
                && springOvershootLimit == candidateOvershootLimit;
    }

    /** Creates an immutable config with this preset's complete default values. */
    public AnimationConfig config(WrapMode wrapMode) {
        return new AnimationConfig(
                this,
                durationSeconds,
                easing,
                wrapMode,
                springStrength,
                springDampingRatio,
                springOvershootLimit);
    }
}
