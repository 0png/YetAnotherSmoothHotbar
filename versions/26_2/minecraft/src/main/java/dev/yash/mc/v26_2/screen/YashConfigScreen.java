package dev.yash.mc.v26_2.screen;

import dev.yash.core.animation.AnimationPreset;
import dev.yash.core.animation.EasingType;
import dev.yash.core.animation.WrapMode;
import dev.yash.core.config.GlowSettings;
import dev.yash.core.config.PulseSettings;
import dev.yash.core.config.RgbColor;
import dev.yash.core.config.SelectorTheme;
import dev.yash.core.config.StretchSettings;
import dev.yash.core.config.TintSettings;
import dev.yash.core.config.YashConfig;
import dev.yash.core.i18n.TranslationKeys;
import dev.yash.mc.v26_2.YashMinecraftClient;
import java.util.Locale;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

/** Three-page, live-preview settings screen shared by keybind and Mod Menu entrypoints. */
public final class YashConfigScreen extends Screen {
    private static final double NANOS_TO_SECONDS = 1.0e-9;
    private static final double DEFAULT_DURATION_SECONDS = 0.115;
    private static final int CONTROL_HEIGHT = 20;
    private static final int CONTROL_GAP = 2;
    private final Screen parent;
    private YashConfig draft;
    private Page page = Page.PRESETS;
    private boolean advancedMode;
    private boolean saved;
    private HotbarPreviewController previewController;
    private HotbarPreviewWidget previewWidget;

    public YashConfigScreen(Screen parent) {
        super(Component.translatable(TranslationKeys.SETTINGS_TITLE));
        this.parent = parent;
        this.draft = YashMinecraftClient.config();
    }

    @Override
    protected void init() {
        int contentWidth = Math.min(316, width - 12);
        int left = (width - contentWidth) / 2;
        int tabWidth = (contentWidth - 8) / 3;
        int tabY = 22;
        addPageButton(Page.PRESETS, left, tabY, tabWidth);
        addPageButton(Page.THEMES, left + tabWidth + 4, tabY, tabWidth);
        addPageButton(Page.ADVANCED, left + (tabWidth + 4) * 2, tabY, tabWidth);

        addRenderableWidget(Button.builder(
                        Component.translatable(advancedMode
                                ? "action.yash.mode.advanced"
                                : "action.yash.mode.basic"),
                        button -> {
                            advancedMode = !advancedMode;
                            rebuildWidgets();
                        })
                .bounds(left, 44, contentWidth, CONTROL_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("description.yash.mode")))
                .build());

        int controlsTop = 68;
        int columnWidth = (contentWidth - CONTROL_GAP) / 2;
        switch (page) {
            case PRESETS -> addPresetControls(left, controlsTop, columnWidth);
            case THEMES -> addThemeControls(left, controlsTop, columnWidth);
            case ADVANCED -> addAdvancedControls(left, controlsTop, columnWidth);
        }

        double now = nowSeconds();
        if (previewController == null) {
            previewController = new HotbarPreviewController(draft, now);
            previewWidget = new HotbarPreviewWidget(0, 0, font, previewController);
        }
        int footerY = Math.max(row(controlsTop, 5) + 34, height - 24);
        previewWidget.setX((width - HotbarPreviewWidget.PREVIEW_WIDTH) / 2);
        previewWidget.setY(footerY - HotbarPreviewWidget.PREVIEW_HEIGHT - 4);
        addRenderableWidget(previewWidget);

        int footerButtonWidth = Math.min(150, (contentWidth - CONTROL_GAP) / 2);
        addRenderableWidget(CycleButton.builder(
                        mode -> Component.translatable(mode.translationKey()),
                        previewWidget.mode())
                .withValues(PreviewMode.values())
                .create(
                        left,
                        footerY,
                        footerButtonWidth,
                        CONTROL_HEIGHT,
                        Component.translatable("preview.yash.mode"),
                        (button, mode) -> previewWidget.setMode(mode)));
        addRenderableWidget(Button.builder(
                        Component.translatable("action.yash.done"),
                        button -> onClose())
                .bounds(
                        left + contentWidth - footerButtonWidth,
                        footerY,
                        footerButtonWidth,
                        CONTROL_HEIGHT)
                .build());
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        extractBackground(graphics, mouseX, mouseY, partialTick);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, 8, 0xFFFFFFFF);
        if (page == Page.ADVANCED && !advancedMode) {
            graphics.centeredText(
                    font,
                    Component.translatable("description.yash.advanced_locked"),
                    width / 2,
                    101,
                    0xFFA0A0A8);
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreenAndShow(parent);
    }

    @Override
    public void removed() {
        if (!saved) {
            YashMinecraftClient.applyConfig(draft, true);
            saved = true;
        }
    }

    private void addPageButton(Page target, int x, int y, int width) {
        Button button = Button.builder(
                        Component.translatable(target.translationKey),
                        ignored -> {
                            page = target;
                            rebuildWidgets();
                        })
                .bounds(x, y, width, CONTROL_HEIGHT)
                .build();
        button.active = page != target;
        addRenderableWidget(button);
    }

    private void addPresetControls(int left, int top, int columnWidth) {
        addBooleanControl(
                left,
                top,
                columnWidth,
                "option.yash.enabled",
                draft.enabled(),
                enabled -> applyDraft(draft.withEnabled(enabled)));

        addRenderableWidget(CycleButton.builder(
                        preset -> Component.translatable(presetKey(preset)),
                        draft.animation().preset())
                .withValues(AnimationPreset.values())
                .withTooltip(preset -> Tooltip.create(Component.translatable(
                        "description.yash.preset", Component.translatable(presetKey(preset)))))
                .create(
                        rightColumn(left, columnWidth),
                        top,
                        columnWidth,
                        CONTROL_HEIGHT,
                        Component.translatable("option.yash.preset"),
                        (button, preset) -> applyDraft(draft.withPreset(preset))));

        double speed = Math.clamp(
                DEFAULT_DURATION_SECONDS / Math.max(0.001, draft.animation().durationSeconds()),
                0.25,
                3.0);
        addSlider(
                left,
                row(top, 1),
                columnWidth,
                0.25,
                3.0,
                speed,
                "option.yash.speed",
                value -> String.format(Locale.ROOT, "%.2fx", value),
                value -> applyDraft(draft.withAnimationDurationSeconds(
                        DEFAULT_DURATION_SECONDS / value)));

        addRenderableWidget(CycleButton.builder(
                        wrap -> Component.translatable(wrapKey(wrap)),
                        draft.animation().wrapMode())
                .withValues(WrapMode.values())
                .create(
                        rightColumn(left, columnWidth),
                        row(top, 1),
                        columnWidth,
                        CONTROL_HEIGHT,
                        Component.translatable("option.yash.wrap_mode"),
                        (button, wrap) -> applyDraft(draft.withWrapMode(wrap))));

        addRenderableWidget(Button.builder(
                        Component.translatable("action.yash.reset_defaults"),
                        button -> {
                            applyDraft(YashConfig.defaults());
                            rebuildWidgets();
                        })
                .bounds(left, row(top, 2), columnWidth * 2 + CONTROL_GAP, CONTROL_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("description.yash.reset_defaults")))
                .build());
    }

    private void addThemeControls(int left, int top, int columnWidth) {
        addRenderableWidget(CycleButton.builder(
                        theme -> Component.translatable(theme.translationKey()),
                        draft.theme())
                .withValues(SelectorTheme.values())
                .withTooltip(theme -> Tooltip.create(Component.translatable(
                        "description.yash.theme." + theme.id())))
                .create(
                        left,
                        top,
                        columnWidth,
                        CONTROL_HEIGHT,
                        Component.translatable("option.yash.theme"),
                        (button, theme) -> applyDraft(draft.withTheme(theme))));
        addBooleanControl(
                rightColumn(left, columnWidth),
                top,
                columnWidth,
                "option.yash.effect.tint",
                draft.tint().enabled(),
                enabled -> applyDraft(draft.withTint(new TintSettings(
                        enabled, draft.tint().color(), draft.tint().strength()))));

        addSlider(
                left,
                row(top, 1),
                columnWidth,
                0.2,
                1.0,
                draft.opacity(),
                "option.yash.effect.opacity",
                YashConfigScreen::percent,
                value -> applyDraft(draft.withOpacity(value)));
        addSlider(
                rightColumn(left, columnWidth),
                row(top, 1),
                columnWidth,
                0.75,
                1.5,
                draft.scale(),
                "option.yash.effect.scale",
                value -> String.format(Locale.ROOT, "%.2fx", value),
                value -> applyDraft(draft.withScale(value)));

        addBooleanControl(
                left,
                row(top, 2),
                columnWidth,
                "option.yash.effect.glow",
                draft.glow().enabled(),
                enabled -> applyDraft(draft.withGlow(new GlowSettings(
                        enabled,
                        draft.glow().color(),
                        draft.glow().strength(),
                        draft.glow().radius(),
                        draft.glow().animated()))));
        addBooleanControl(
                rightColumn(left, columnWidth),
                row(top, 2),
                columnWidth,
                "option.yash.effect.stretch",
                draft.stretch().enabled(),
                enabled -> applyDraft(draft.withStretch(new StretchSettings(
                        enabled, draft.stretch().strength()))));
        addBooleanControl(
                left,
                row(top, 3),
                columnWidth,
                "option.yash.effect.arrival_pulse",
                draft.pulse().enabled(),
                enabled -> applyDraft(draft.withPulse(new PulseSettings(
                        enabled, draft.pulse().strength()))));

        if (advancedMode) {
            addHexColorControl(
                    rightColumn(left, columnWidth),
                    row(top, 3),
                    columnWidth,
                    "option.yash.effect.tint_color",
                    draft.tint().color(),
                    color -> applyDraft(draft.withTint(new TintSettings(
                            draft.tint().enabled(), color, draft.tint().strength()))));
            addSlider(
                    left,
                    row(top, 4),
                    columnWidth,
                    0.0,
                    1.0,
                    draft.tint().strength(),
                    "option.yash.effect.tint_strength",
                    YashConfigScreen::percent,
                    value -> applyDraft(draft.withTint(new TintSettings(
                            draft.tint().enabled(), draft.tint().color(), value))));
            addHexColorControl(
                    rightColumn(left, columnWidth),
                    row(top, 4),
                    columnWidth,
                    "option.yash.effect.glow_color",
                    draft.glow().color(),
                    color -> applyDraft(draft.withGlow(new GlowSettings(
                            draft.glow().enabled(),
                            color,
                            draft.glow().strength(),
                            draft.glow().radius(),
                            draft.glow().animated()))));
        }
    }

    private void addAdvancedControls(int left, int top, int columnWidth) {
        if (!advancedMode) {
            addBooleanControl(
                    left,
                    top,
                    columnWidth * 2 + CONTROL_GAP,
                    "option.yash.reduced_motion",
                    draft.reducedMotion(),
                    enabled -> applyDraft(draft.withReducedMotion(enabled)));
            return;
        }

        addSlider(
                left,
                top,
                columnWidth,
                0.025,
                1.0,
                Math.max(0.025, draft.animation().durationSeconds()),
                "option.yash.duration",
                value -> String.format(Locale.ROOT, "%.0f ms", value * 1000.0),
                value -> applyDraft(draft.withAnimationDurationSeconds(value)));
        addRenderableWidget(CycleButton.builder(
                        easing -> Component.translatable(easingKey(easing)),
                        draft.animation().easing())
                .withValues(EasingType.values())
                .create(
                        rightColumn(left, columnWidth),
                        top,
                        columnWidth,
                        CONTROL_HEIGHT,
                        Component.translatable("option.yash.easing"),
                        (button, easing) -> applyDraft(draft.withEasing(easing))));

        addSlider(
                left,
                row(top, 1),
                columnWidth,
                1.0,
                60.0,
                draft.animation().springStrength(),
                "option.yash.spring_strength",
                YashConfigScreen::oneDecimal,
                value -> applySpring(value, draft.animation().springDampingRatio(),
                        draft.animation().springOvershootLimit()));
        addSlider(
                rightColumn(left, columnWidth),
                row(top, 1),
                columnWidth,
                0.0,
                2.0,
                draft.animation().springDampingRatio(),
                "option.yash.damping",
                YashConfigScreen::twoDecimals,
                value -> applySpring(draft.animation().springStrength(), value,
                        draft.animation().springOvershootLimit()));

        addSlider(
                left,
                row(top, 2),
                columnWidth,
                0.0,
                0.5,
                draft.animation().springOvershootLimit(),
                "option.yash.overshoot",
                YashConfigScreen::percent,
                value -> applySpring(draft.animation().springStrength(),
                        draft.animation().springDampingRatio(), value));
        addSlider(
                rightColumn(left, columnWidth),
                row(top, 2),
                columnWidth,
                0.0,
                1.0,
                draft.glow().strength(),
                "option.yash.effect.glow_strength",
                YashConfigScreen::percent,
                value -> applyDraft(draft.withGlow(new GlowSettings(
                        draft.glow().enabled(),
                        draft.glow().color(),
                        value,
                        draft.glow().radius(),
                        draft.glow().animated()))));

        addSlider(
                left,
                row(top, 3),
                columnWidth,
                0.0,
                1.0,
                draft.stretch().strength(),
                "option.yash.effect.stretch_strength",
                YashConfigScreen::percent,
                value -> applyDraft(draft.withStretch(new StretchSettings(
                        draft.stretch().enabled(), value))));
        addSlider(
                rightColumn(left, columnWidth),
                row(top, 3),
                columnWidth,
                0.0,
                1.0,
                draft.pulse().strength(),
                "option.yash.effect.pulse_strength",
                YashConfigScreen::percent,
                value -> applyDraft(draft.withPulse(new PulseSettings(
                        draft.pulse().enabled(), value))));

        addSlider(
                left,
                row(top, 4),
                columnWidth,
                0.0,
                16.0,
                draft.glow().radius(),
                "option.yash.effect.glow_radius",
                YashConfigScreen::oneDecimal,
                value -> applyDraft(draft.withGlow(new GlowSettings(
                        draft.glow().enabled(),
                        draft.glow().color(),
                        draft.glow().strength(),
                        value,
                        draft.glow().animated()))));
        addBooleanControl(
                rightColumn(left, columnWidth),
                row(top, 4),
                columnWidth,
                "option.yash.reduced_motion",
                draft.reducedMotion(),
                enabled -> applyDraft(draft.withReducedMotion(enabled)));
    }

    private void addBooleanControl(
            int x,
            int y,
            int width,
            String key,
            boolean initial,
            java.util.function.Consumer<Boolean> responder) {
        addRenderableWidget(CycleButton.onOffBuilder(initial)
                .create(
                        x,
                        y,
                        width,
                        CONTROL_HEIGHT,
                        Component.translatable(key),
                        (button, value) -> responder.accept(value)));
    }

    private void addHexColorControl(
            int x,
            int y,
            int width,
            String key,
            RgbColor initial,
            java.util.function.Consumer<RgbColor> responder) {
        int fieldWidth = Math.min(76, width / 2);
        int labelWidth = width - fieldWidth - CONTROL_GAP;
        addRenderableWidget(new StringWidget(
                        x,
                        y,
                        labelWidth,
                        CONTROL_HEIGHT,
                        Component.translatable(key),
                        font)
                .setMaxWidth(labelWidth));
        EditBox field = new EditBox(
                font,
                x + labelWidth + CONTROL_GAP,
                y,
                fieldWidth,
                CONTROL_HEIGHT,
                Component.translatable(key));
        field.setMaxLength(7);
        field.setValue(initial.hex());
        field.setTooltip(Tooltip.create(Component.translatable("description.yash.hex_color")));
        field.setResponder(value -> {
            try {
                responder.accept(RgbColor.parse(value));
                field.setTextColor(0xFFE0E0E0);
            } catch (IllegalArgumentException ignored) {
                field.setTextColor(0xFFFF5555);
            }
        });
        addRenderableWidget(field);
    }

    private void addSlider(
            int x,
            int y,
            int width,
            double minimum,
            double maximum,
            double initial,
            String key,
            Function<Double, String> formatter,
            DoubleConsumer responder) {
        addRenderableWidget(new YashSlider(
                x,
                y,
                width,
                CONTROL_HEIGHT,
                minimum,
                maximum,
                initial,
                value -> Component.translatable(key, formatter.apply(value)),
                responder));
    }

    private void applySpring(double strength, double damping, double overshoot) {
        applyDraft(draft.withSpringParameters(strength, damping, overshoot));
    }

    private void applyDraft(YashConfig newDraft) {
        draft = newDraft;
        saved = false;
        YashMinecraftClient.applyConfig(draft, false);
        if (previewWidget != null) {
            previewWidget.applyConfig(draft);
        }
    }

    private static int rightColumn(int left, int columnWidth) {
        return left + columnWidth + CONTROL_GAP;
    }

    private static int row(int top, int index) {
        return top + index * (CONTROL_HEIGHT + CONTROL_GAP);
    }

    private static String percent(double value) {
        return String.format(Locale.ROOT, "%.0f%%", value * 100.0);
    }

    private static String oneDecimal(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private static String twoDecimals(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private static String presetKey(AnimationPreset preset) {
        return "preset.yash." + preset.id();
    }

    private static String easingKey(EasingType easing) {
        return "easing.yash." + easing.id();
    }

    private static String wrapKey(WrapMode wrap) {
        return "wrap.yash." + wrap.id();
    }

    private static double nowSeconds() {
        return Util.getNanos() * NANOS_TO_SECONDS;
    }

    private enum Page {
        PRESETS("screen.yash.page.presets"),
        THEMES("screen.yash.page.themes"),
        ADVANCED("screen.yash.page.advanced");

        private final String translationKey;

        Page(String translationKey) {
            this.translationKey = translationKey;
        }
    }

}
