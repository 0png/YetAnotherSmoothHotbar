package dev.yash.core.animation;

/** Built-in animation profiles. Themes and visual effects are configured separately. */
public enum AnimationPreset {
    BEDROCK_INSPIRED(0.115, EasingType.EASE_OUT_QUINT, 22.0, 1.0, 0.0),
    SMOOTH(0.175, EasingType.EASE_IN_OUT_CUBIC, 22.0, 1.0, 0.0),
    SNAPPY(0.075, EasingType.EASE_OUT_CUBIC, 22.0, 1.0, 0.0),
    SPRING(0.420, EasingType.SPRING, 22.0, 0.68, 0.12),
    MINIMAL(0.050, EasingType.EASE_OUT_QUAD, 22.0, 1.0, 0.0),
    CUSTOM(0.115, EasingType.EASE_OUT_CUBIC, 22.0, 0.75, 0.10),
    DISABLED(0.0, EasingType.LINEAR, 22.0, 1.0, 0.0);

    private final double durationSeconds;
    private final EasingType easing;
    private final double springStrength;
    private final double springDampingRatio;
    private final double springOvershootLimit;

    AnimationPreset(
            double durationSeconds,
            EasingType easing,
            double springStrength,
            double springDampingRatio,
            double springOvershootLimit) {
        this.durationSeconds = durationSeconds;
        this.easing = easing;
        this.springStrength = springStrength;
        this.springDampingRatio = springDampingRatio;
        this.springOvershootLimit = springOvershootLimit;
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
