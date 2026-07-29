package dev.yash.mc.v26_2.screen;

import dev.yash.core.config.YashConfig;
import dev.yash.mc.v26_2.hud.HotbarSelectorRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

/** Clickable nine-slot preview that uses the production selector renderer. */
final class HotbarPreviewWidget extends AbstractWidget {
    static final int SLOT_SIZE = 20;
    static final int PREVIEW_WIDTH = SLOT_SIZE * 9 + 4;
    static final int PREVIEW_HEIGHT = 28;

    private static final double NANOS_TO_SECONDS = 1.0e-9;
    private static final Identifier VANILLA_SELECTOR =
            Identifier.withDefaultNamespace("hud/hotbar_selection");

    private final Font font;
    private final HotbarPreviewController controller;
    private final HotbarSelectorRenderer renderer = new HotbarSelectorRenderer();

    HotbarPreviewWidget(int x, int y, Font font, HotbarPreviewController controller) {
        super(x, y, PREVIEW_WIDTH, PREVIEW_HEIGHT, Component.translatable("preview.yash.hotbar"));
        this.font = font;
        this.controller = controller;
    }

    void applyConfig(YashConfig config) {
        controller.setConfig(config, nowSeconds());
    }

    void setMode(PreviewMode mode) {
        controller.setMode(mode, nowSeconds());
    }

    PreviewMode mode() {
        return controller.mode();
    }

    @Override
    protected void extractWidgetRenderState(
            GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int slotsX = getX() + 2;
        int slotsY = getY() + 4;
        graphics.fill(getX(), getY() + 2, getRight(), getBottom() - 2, 0xCC101014);
        for (int slot = 0; slot < 9; slot++) {
            int x = slotsX + slot * SLOT_SIZE;
            int fill = slot == controller.selectedSlot() ? 0xCC4A4A55 : 0xCC24242B;
            graphics.fill(x, slotsY, x + SLOT_SIZE, slotsY + SLOT_SIZE, fill);
            graphics.outline(x, slotsY, SLOT_SIZE, SLOT_SIZE, 0xFF8B8B95);
            graphics.centeredText(font, Integer.toString(slot + 1), x + SLOT_SIZE / 2, slotsY + 6, 0xFFE6E6EA);
        }

        double now = nowSeconds();
        HotbarPreviewController.PreviewSample sample = controller.sample(now);
        renderer.render(
                graphics,
                RenderPipelines.GUI_TEXTURED,
                VANILLA_SELECTOR,
                slotsX - 2,
                slotsY - 2,
                24,
                24,
                sample.frame(),
                sample.config(),
                sample.effects());
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        int slot = (int) ((event.x() - getX() - 2) / SLOT_SIZE);
        if (slot >= 0 && slot < 9) {
            controller.selectSlot(slot, nowSeconds());
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }

    private static double nowSeconds() {
        return Util.getNanos() * NANOS_TO_SECONDS;
    }
}
