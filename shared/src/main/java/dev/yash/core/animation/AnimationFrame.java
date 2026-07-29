package dev.yash.core.animation;

/** A renderer-facing snapshot of selector motion, measured in hotbar-slot units. */
public record AnimationFrame(
        double position,
        double velocity,
        int targetSlot,
        boolean animating,
        boolean arrived,
        long transitionId,
        long arrivalId,
        WrapMode wrapMode) {

    public AnimationFrame {
        if (!Double.isFinite(position) || !Double.isFinite(velocity)) {
            throw new IllegalArgumentException("Position and velocity must be finite");
        }
        AnimationEngine.validateSlot(targetSlot);
        if (transitionId < 0 || arrivalId < 0) {
            throw new IllegalArgumentException("Animation identifiers cannot be negative");
        }
        if (wrapMode == null) {
            throw new NullPointerException("wrapMode");
        }
    }

    /**
     * Maps an unwrapped animation coordinate to the canonical range [0, 9).
     * Renderers can additionally draw copies shifted by ±9 while crossing an edge.
     */
    public double wrappedPosition() {
        double wrapped = position % AnimationEngine.HOTBAR_SLOT_COUNT;
        return wrapped < 0.0 ? wrapped + AnimationEngine.HOTBAR_SLOT_COUNT : wrapped;
    }
}
