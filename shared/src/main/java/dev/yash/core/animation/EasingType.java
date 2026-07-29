package dev.yash.core.animation;

/** Time curves available to non-spring selector animations. */
public enum EasingType {
    LINEAR("linear") {
        @Override
        double transformUnit(double progress) {
            return progress;
        }

        @Override
        double derivativeUnit(double progress) {
            return 1.0;
        }
    },
    EASE_OUT_QUAD("ease_out_quad") {
        @Override
        double transformUnit(double progress) {
            double remaining = 1.0 - progress;
            return 1.0 - remaining * remaining;
        }

        @Override
        double derivativeUnit(double progress) {
            return 2.0 * (1.0 - progress);
        }
    },
    EASE_OUT_CUBIC("ease_out_cubic") {
        @Override
        double transformUnit(double progress) {
            double remaining = 1.0 - progress;
            return 1.0 - remaining * remaining * remaining;
        }

        @Override
        double derivativeUnit(double progress) {
            double remaining = 1.0 - progress;
            return 3.0 * remaining * remaining;
        }
    },
    EASE_OUT_QUINT("ease_out_quint") {
        @Override
        double transformUnit(double progress) {
            double remaining = 1.0 - progress;
            return 1.0 - Math.pow(remaining, 5.0);
        }

        @Override
        double derivativeUnit(double progress) {
            return 5.0 * Math.pow(1.0 - progress, 4.0);
        }
    },
    EASE_IN_OUT_CUBIC("ease_in_out_cubic") {
        @Override
        double transformUnit(double progress) {
            if (progress < 0.5) {
                return 4.0 * progress * progress * progress;
            }
            return 1.0 - Math.pow(-2.0 * progress + 2.0, 3.0) / 2.0;
        }

        @Override
        double derivativeUnit(double progress) {
            if (progress < 0.5) {
                return 12.0 * progress * progress;
            }
            double remaining = 1.0 - progress;
            return 12.0 * remaining * remaining;
        }
    },
    SMOOTHSTEP("smoothstep") {
        @Override
        double transformUnit(double progress) {
            return progress * progress * (3.0 - 2.0 * progress);
        }

        @Override
        double derivativeUnit(double progress) {
            return 6.0 * progress * (1.0 - progress);
        }
    },
    /** Marker for the analytic damped-spring path in {@link AnimationEngine}. */
    SPRING("spring") {
        @Override
        double transformUnit(double progress) {
            throw new UnsupportedOperationException("Spring motion is sampled by AnimationEngine");
        }

        @Override
        double derivativeUnit(double progress) {
            throw new UnsupportedOperationException("Spring motion is sampled by AnimationEngine");
        }
    };

    private final String id;

    EasingType(String id) {
        this.id = id;
    }

    /** Stable value used by configuration files; independent of the Java enum name. */
    public String id() {
        return id;
    }

    public static EasingType fromId(String id) {
        for (EasingType value : values()) {
            if (value.id.equals(id)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown easing id: " + id);
    }

    /** Applies this easing to a normalized progress value. */
    public double transform(double progress) {
        return transformUnit(clampUnit(progress));
    }

    /** Returns the slope of this easing at a normalized progress value. */
    public double derivative(double progress) {
        return derivativeUnit(clampUnit(progress));
    }

    abstract double transformUnit(double progress);

    abstract double derivativeUnit(double progress);

    private static double clampUnit(double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Progress must be finite");
        }
        return Math.max(0.0, Math.min(1.0, value));
    }
}
