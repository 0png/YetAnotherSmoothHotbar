package dev.yash.mc.v26_2.screen;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

/** Small reusable range slider whose label and formatter remain fully translatable. */
final class YashSlider extends AbstractSliderButton {
    private final double minimum;
    private final double maximum;
    private final DoubleFunction<Component> messageFactory;
    private final DoubleConsumer responder;

    YashSlider(
            int x,
            int y,
            int width,
            int height,
            double minimum,
            double maximum,
            double initialValue,
            DoubleFunction<Component> messageFactory,
            DoubleConsumer responder) {
        super(
                x,
                y,
                width,
                height,
                Component.empty(),
                normalize(initialValue, minimum, maximum));
        if (!Double.isFinite(minimum) || !Double.isFinite(maximum) || minimum >= maximum) {
            throw new IllegalArgumentException("Slider range must be finite and increasing");
        }
        this.minimum = minimum;
        this.maximum = maximum;
        this.messageFactory = messageFactory;
        this.responder = responder;
        updateMessage();
    }

    double actualValue() {
        return minimum + value * (maximum - minimum);
    }

    @Override
    protected void updateMessage() {
        if (messageFactory != null) {
            setMessage(messageFactory.apply(actualValue()));
        }
    }

    @Override
    protected void applyValue() {
        responder.accept(actualValue());
    }

    private static double normalize(double value, double minimum, double maximum) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Slider value must be finite");
        }
        return Math.clamp((value - minimum) / (maximum - minimum), 0.0, 1.0);
    }
}
