package dev.yash.core.animation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class EasingTypeTest {
    private static final double EPSILON = 1.0e-12;

    @Test
    void nonSpringEasingsAreBoundedAndMonotonic() {
        for (EasingType easing : EasingType.values()) {
            if (easing == EasingType.SPRING) {
                continue;
            }

            assertEquals(0.0, easing.transform(0.0), EPSILON, easing.name());
            assertEquals(1.0, easing.transform(1.0), EPSILON, easing.name());
            double previous = 0.0;
            for (int step = 1; step <= 100; step++) {
                double value = easing.transform(step / 100.0);
                assertTrue(value >= previous - EPSILON, easing.name());
                assertTrue(value >= 0.0 && value <= 1.0, easing.name());
                assertTrue(easing.derivative(step / 100.0) >= -EPSILON, easing.name());
                previous = value;
            }
        }
    }

    @Test
    void progressIsClampedButMustBeFinite() {
        assertEquals(0.0, EasingType.LINEAR.transform(-10.0), EPSILON);
        assertEquals(1.0, EasingType.LINEAR.transform(10.0), EPSILON);
        assertThrows(IllegalArgumentException.class, () -> EasingType.LINEAR.transform(Double.NaN));
        assertThrows(UnsupportedOperationException.class, () -> EasingType.SPRING.transform(0.5));
    }
}
