package dev.yash.mc.v26_2.screen;

/** Repeatable interaction scenarios available in the settings preview. */
public enum PreviewMode {
    MANUAL("preview.yash.mode.manual"),
    CYCLE("preview.yash.mode.cycle"),
    EDGES("preview.yash.mode.edges"),
    RAPID("preview.yash.mode.rapid");

    private final String translationKey;

    PreviewMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public String translationKey() {
        return translationKey;
    }
}
