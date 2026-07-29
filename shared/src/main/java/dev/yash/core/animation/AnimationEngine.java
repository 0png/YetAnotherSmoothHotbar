package dev.yash.core.animation;

import java.util.Objects;

/**
 * Frame-rate-independent state machine for a nine-slot hotbar selector.
 *
 * <p>Callers provide an absolute monotonic timestamp in seconds. Retargeting samples the current
 * visual position first, invalidates the old destination, and starts directly toward the newest
 * selected slot without queueing transitions.</p>
 */
public final class AnimationEngine {
    public static final int HOTBAR_SLOT_COUNT = 9;

    private static final double POSITION_EPSILON = 1.0e-9;
    private static final double CRITICAL_DAMPING_EPSILON = 1.0e-7;

    private AnimationConfig config;
    private boolean initialized;
    private boolean settled;
    private int targetSlot;
    private double startPosition;
    private double startVelocity;
    private double targetPosition;
    private double transitionStartTime;
    private double lastTimestamp;
    private long transitionId;
    private long arrivalId;
    private AnimationFrame currentFrame;

    public AnimationEngine(AnimationConfig config) {
        this.config = Objects.requireNonNull(config, "config");
    }

    public AnimationConfig config() {
        return config;
    }

    public boolean initialized() {
        return initialized;
    }

    /** Returns the latest frame, or fails if the engine has not been initialized. */
    public AnimationFrame currentFrame() {
        if (!initialized) {
            throw new IllegalStateException("Animation engine has not been initialized");
        }
        return currentFrame;
    }

    /** Immediately aligns the selector with a slot and clears all pending arrival state. */
    public AnimationFrame reset(int selectedSlot, double nowSeconds) {
        validateSlot(selectedSlot);
        validateTimestamp(nowSeconds);

        initialized = true;
        settled = true;
        targetSlot = selectedSlot;
        startPosition = selectedSlot;
        startVelocity = 0.0;
        targetPosition = selectedSlot;
        transitionStartTime = nowSeconds;
        lastTimestamp = nowSeconds;
        transitionId = 0L;
        arrivalId = 0L;
        currentFrame = frame(selectedSlot, 0.0, false, false);
        return currentFrame;
    }

    /**
     * Applies a new animation policy without moving back to the previous transition origin.
     * The currently displayed position is sampled using the old policy before the change.
     */
    public void setConfig(AnimationConfig newConfig, double nowSeconds) {
        Objects.requireNonNull(newConfig, "newConfig");
        validateTimestamp(nowSeconds);

        if (!initialized) {
            config = newConfig;
            return;
        }

        ensureMonotonic(nowSeconds);
        if (settled) {
            config = newConfig;
            startPosition = targetSlot;
            startVelocity = 0.0;
            targetPosition = targetSlot;
            transitionStartTime = nowSeconds;
            lastTimestamp = nowSeconds;
            currentFrame = frame(targetPosition, 0.0, false, false);
            return;
        }

        MotionSample current = sample(nowSeconds);
        config = newConfig;
        startPosition = current.position();
        startVelocity = current.velocity();
        targetPosition = virtualTarget(targetSlot, current.position(), newConfig.wrapMode());
        transitionStartTime = nowSeconds;
        lastTimestamp = nowSeconds;

        if (!newConfig.animated() || approximatelyEqual(startPosition, targetPosition)) {
            startPosition = targetPosition;
            startVelocity = 0.0;
            settled = true;
            currentFrame = frame(targetPosition, 0.0, false, false);
            return;
        }

        settled = false;
        currentFrame = frame(startPosition, startVelocity, true, false);
    }

    /** Samples the selector at an absolute monotonic timestamp. */
    public AnimationFrame update(int selectedSlot, double nowSeconds) {
        validateSlot(selectedSlot);
        validateTimestamp(nowSeconds);

        if (!initialized) {
            return reset(selectedSlot, nowSeconds);
        }

        ensureMonotonic(nowSeconds);
        MotionSample current = sample(nowSeconds);
        boolean targetChanged = selectedSlot != targetSlot;

        if (targetChanged) {
            beginTransition(selectedSlot, current, nowSeconds);
            current = sample(nowSeconds);
        }

        boolean arrived = false;
        if (!targetChanged && !settled && current.complete()) {
            settled = true;
            arrivalId++;
            arrived = true;
            current = new MotionSample(targetPosition, 0.0, true);
        }

        lastTimestamp = nowSeconds;
        currentFrame = frame(current.position(), current.velocity(), !settled, arrived);
        return currentFrame;
    }

    private void beginTransition(int selectedSlot, MotionSample current, double nowSeconds) {
        targetSlot = selectedSlot;
        startPosition = current.position();
        startVelocity = current.velocity();
        targetPosition = virtualTarget(selectedSlot, current.position(), config.wrapMode());
        transitionStartTime = nowSeconds;
        transitionId++;

        if (!config.animated() || approximatelyEqual(startPosition, targetPosition)) {
            startPosition = targetPosition;
            startVelocity = 0.0;
            settled = true;
            return;
        }

        settled = false;
    }

    private MotionSample sample(double nowSeconds) {
        if (settled) {
            return new MotionSample(targetPosition, 0.0, true);
        }

        double elapsed = Math.max(0.0, nowSeconds - transitionStartTime);
        if (!config.animated() || elapsed >= config.durationSeconds()) {
            return new MotionSample(targetPosition, 0.0, true);
        }

        if (config.spring()) {
            return sampleSpring(elapsed);
        }
        return sampleEasing(elapsed);
    }

    private MotionSample sampleEasing(double elapsed) {
        double progress = elapsed / config.durationSeconds();
        double distance = targetPosition - startPosition;
        double eased = config.easing().transform(progress);
        double velocity = distance * config.easing().derivative(progress) / config.durationSeconds();
        return new MotionSample(startPosition + distance * eased, velocity, false);
    }

    private MotionSample sampleSpring(double elapsed) {
        double displacement = startPosition - targetPosition;
        double dampingRatio = config.springOvershootLimit() == 0.0
                ? Math.max(1.0, config.springDampingRatio())
                : config.springDampingRatio();
        MotionSample raw = solveDampedSpring(
                displacement,
                startVelocity,
                config.springStrength(),
                dampingRatio,
                elapsed);

        double position = targetPosition + raw.position();
        double velocity = raw.velocity();
        double distance = targetPosition - startPosition;
        double distanceMagnitude = Math.abs(distance);
        if (distanceMagnitude > POSITION_EPSILON) {
            double direction = Math.signum(distance);
            double progress = (position - startPosition) * direction / distanceMagnitude;
            double maximumProgress = 1.0 + config.springOvershootLimit();
            if (progress > maximumProgress) {
                position = startPosition + direction * distanceMagnitude * maximumProgress;
                if (velocity * direction > 0.0) {
                    velocity = 0.0;
                }
            }
        }

        return new MotionSample(position, velocity, false);
    }

    private static MotionSample solveDampedSpring(
            double initialDisplacement,
            double initialVelocity,
            double angularFrequency,
            double dampingRatio,
            double elapsed) {
        if (dampingRatio < 1.0 - CRITICAL_DAMPING_EPSILON) {
            double dampedFrequency = angularFrequency * Math.sqrt(1.0 - dampingRatio * dampingRatio);
            double a = initialDisplacement;
            double b = (initialVelocity + dampingRatio * angularFrequency * a) / dampedFrequency;
            double decay = Math.exp(-dampingRatio * angularFrequency * elapsed);
            double cosine = Math.cos(dampedFrequency * elapsed);
            double sine = Math.sin(dampedFrequency * elapsed);
            double oscillation = a * cosine + b * sine;
            double displacement = decay * oscillation;
            double velocity = decay * (
                    -dampingRatio * angularFrequency * oscillation
                            - a * dampedFrequency * sine
                            + b * dampedFrequency * cosine);
            return new MotionSample(displacement, velocity, false);
        }

        if (Math.abs(dampingRatio - 1.0) <= CRITICAL_DAMPING_EPSILON) {
            double a = initialDisplacement;
            double b = initialVelocity + angularFrequency * a;
            double decay = Math.exp(-angularFrequency * elapsed);
            double displacement = (a + b * elapsed) * decay;
            double velocity = (b - angularFrequency * (a + b * elapsed)) * decay;
            return new MotionSample(displacement, velocity, false);
        }

        double root = Math.sqrt(dampingRatio * dampingRatio - 1.0);
        double firstRate = -angularFrequency * (dampingRatio - root);
        double secondRate = -angularFrequency * (dampingRatio + root);
        double firstCoefficient = (initialVelocity - secondRate * initialDisplacement)
                / (firstRate - secondRate);
        double secondCoefficient = initialDisplacement - firstCoefficient;
        double firstTerm = firstCoefficient * Math.exp(firstRate * elapsed);
        double secondTerm = secondCoefficient * Math.exp(secondRate * elapsed);
        double displacement = firstTerm + secondTerm;
        double velocity = firstRate * firstTerm + secondRate * secondTerm;
        return new MotionSample(displacement, velocity, false);
    }

    private AnimationFrame frame(double position, double velocity, boolean animating, boolean arrived) {
        return new AnimationFrame(
                position,
                velocity,
                targetSlot,
                animating,
                arrived,
                transitionId,
                arrivalId,
                config.wrapMode());
    }

    private static double virtualTarget(int selectedSlot, double currentPosition, WrapMode wrapMode) {
        if (wrapMode == WrapMode.ACROSS) {
            return selectedSlot;
        }

        double cycle = Math.rint((currentPosition - selectedSlot) / HOTBAR_SLOT_COUNT);
        double candidate = selectedSlot + cycle * HOTBAR_SLOT_COUNT;
        double previous = candidate - HOTBAR_SLOT_COUNT;
        double next = candidate + HOTBAR_SLOT_COUNT;
        if (Math.abs(previous - currentPosition) < Math.abs(candidate - currentPosition)) {
            candidate = previous;
        }
        if (Math.abs(next - currentPosition) < Math.abs(candidate - currentPosition)) {
            candidate = next;
        }
        return candidate;
    }

    static void validateSlot(int slot) {
        if (slot < 0 || slot >= HOTBAR_SLOT_COUNT) {
            throw new IllegalArgumentException("Slot must be in range [0, 8]: " + slot);
        }
    }

    private static void validateTimestamp(double timestamp) {
        if (!Double.isFinite(timestamp)) {
            throw new IllegalArgumentException("Timestamp must be finite");
        }
    }

    private void ensureMonotonic(double timestamp) {
        if (timestamp < lastTimestamp) {
            throw new IllegalArgumentException("Timestamp cannot move backwards");
        }
    }

    private static boolean approximatelyEqual(double first, double second) {
        return Math.abs(first - second) <= POSITION_EPSILON;
    }

    private record MotionSample(double position, double velocity, boolean complete) {
    }
}
