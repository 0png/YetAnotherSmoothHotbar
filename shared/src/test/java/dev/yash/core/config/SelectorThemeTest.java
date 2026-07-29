package dev.yash.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

final class SelectorThemeTest {
    @Test
    void vanillaAloneUsesTheCurrentResourcePackSprite() {
        assertTrue(SelectorTheme.VANILLA_RESOURCE_PACK.metadata().resourcePackBacked());
        for (SelectorTheme theme : SelectorTheme.values()) {
            if (theme != SelectorTheme.VANILLA_RESOURCE_PACK) {
                assertFalse(theme.metadata().resourcePackBacked());
            }
        }
    }

    @Test
    void builtInSpritePathsAreUniqueAndMatchVanillaSelectorFootprint() {
        Set<String> paths = Arrays.stream(SelectorTheme.values())
                .map(SelectorTheme::metadata)
                .filter(metadata -> !metadata.resourcePackBacked())
                .map(ThemeMetadata::spritePath)
                .collect(Collectors.toSet());

        assertEquals(SelectorTheme.values().length - 1, paths.size());
        for (SelectorTheme theme : SelectorTheme.values()) {
            ThemeMetadata metadata = theme.metadata();
            assertEquals(ThemeMetadata.VANILLA_SELECTOR_WIDTH, metadata.nativeWidth());
            assertEquals(ThemeMetadata.VANILLA_SELECTOR_HEIGHT, metadata.nativeHeight());
            assertEquals(0, metadata.offsetX());
            assertEquals(0, metadata.offsetY());
        }
    }

    @Test
    void translationKeysAreIndependentAndUnique() {
        Set<String> keys = Arrays.stream(SelectorTheme.values())
                .map(SelectorTheme::translationKey)
                .collect(Collectors.toSet());

        assertEquals(SelectorTheme.values().length, keys.size());
        assertTrue(keys.stream().allMatch(key -> key.startsWith("theme.yash.")));
    }
}
