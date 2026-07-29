package dev.yash.mc.v26_2.hud;

import dev.yash.core.animation.AnimationFrame;
import dev.yash.core.animation.WrapMode;

/** Minecraft 26.2 vanilla hotbar geometry in GUI-scaled pixels. */
public final class HotbarLayout {
    public static final int SLOT_SPACING = 20;
    public static final int SELECTOR_TRAVEL_DISTANCE = 8 * SLOT_SPACING;

    private HotbarLayout() {
    }

    /** Derives the slot-zero selector X from vanilla's selected-slot X argument. */
    public static int selectorBaseX(int vanillaSelectorX, int selectedSlot) {
        if (selectedSlot < 0 || selectedSlot >= 9) {
            throw new IllegalArgumentException("Selected slot must be in range [0, 8]");
        }
        return vanillaSelectorX - selectedSlot * SLOT_SPACING;
    }

    /** Converts an animation position measured in slots to GUI-scaled pixel coordinates. */
    public static int selectorX(int selectorBaseX, double slotPosition) {
        if (!Double.isFinite(slotPosition)) {
            throw new IllegalArgumentException("Slot position must be finite");
        }
        return selectorBaseX + (int) Math.round(slotPosition * SLOT_SPACING);
    }

    /**
     * Returns the second clipped copy needed while crossing the 9↔1 seam, or {@link Double#NaN}
     * when the canonical selector is the only draw.
     */
    public static double wrapSeamPosition(AnimationFrame frame) {
        if (frame.wrapMode() != WrapMode.WRAP || !frame.animating()) {
            return Double.NaN;
        }
        if (frame.targetSlot() == 8 && frame.position() < 0.0) {
            return frame.position();
        }
        if (frame.targetSlot() == 0 && frame.position() > 8.0) {
            return frame.position() < 9.0 ? frame.position() - 9.0 : frame.position();
        }
        return Double.NaN;
    }
}
