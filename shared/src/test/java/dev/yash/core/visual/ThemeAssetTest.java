package dev.yash.core.visual;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.yash.core.config.SelectorTheme;
import dev.yash.core.config.ThemeMetadata;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

final class ThemeAssetTest {
    @Test
    void everyBuiltInThemeHasAValidTransparentGuiSprite() throws IOException {
        for (SelectorTheme theme : SelectorTheme.values()) {
            ThemeMetadata metadata = theme.metadata();
            if (metadata.resourcePackBacked()) {
                continue;
            }
            String resource = "/assets/yash/textures/gui/sprites/"
                    + metadata.spritePath()
                    + ".png";
            try (InputStream stream = ThemeAssetTest.class.getResourceAsStream(resource)) {
                assertNotNull(stream, () -> "Missing theme sprite " + resource);
                BufferedImage image = ImageIO.read(stream);
                assertNotNull(image, () -> "Unreadable theme sprite " + resource);
                assertEquals(metadata.nativeWidth(), image.getWidth());
                assertEquals(metadata.nativeHeight(), image.getHeight());
                assertTrue(image.getColorModel().hasAlpha());
                assertTrue(visiblePixels(image) > 0);
                assertTrue(transparentPixels(image) > 0);
                assertTrue(centerIsTransparent(image),
                        () -> "Theme center must not cover the item: " + theme);
            }
        }
    }

    private static int visiblePixels(BufferedImage image) {
        int count = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (((image.getRGB(x, y) >>> 24) & 0xFF) != 0) {
                    count++;
                }
            }
        }
        return count;
    }

    private static int transparentPixels(BufferedImage image) {
        return image.getWidth() * image.getHeight() - visiblePixels(image);
    }

    private static boolean centerIsTransparent(BufferedImage image) {
        int centerX = image.getWidth() / 2;
        int centerY = image.getHeight() / 2;
        for (int y = centerY - 2; y <= centerY + 2; y++) {
            for (int x = centerX - 2; x <= centerX + 2; x++) {
                if (((image.getRGB(x, y) >>> 24) & 0xFF) != 0) {
                    return false;
                }
            }
        }
        return true;
    }
}
