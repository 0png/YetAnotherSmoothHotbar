package dev.yash.core.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;

final class LanguageParityTest {
    private static final Gson GSON = new Gson();
    private static final Type LANGUAGE_TYPE = new TypeToken<Map<String, String>>() { }.getType();

    @Test
    void traditionalChineseContainsExactlyTheCanonicalEnglishKeys() {
        Map<String, String> english = loadLanguage("en_us");
        Map<String, String> traditionalChinese = loadLanguage("zh_tw");

        assertEquals(english.keySet(), traditionalChinese.keySet());
    }

    @Test
    void requiredLanguagesContainNoBlankKeysOrValues() {
        for (String language : new String[] {"en_us", "zh_tw"}) {
            loadLanguage(language).forEach((key, value) -> {
                assertFalse(key.isBlank(), () -> language + " contains a blank translation key");
                assertFalse(value.isBlank(), () -> language + " contains a blank value for " + key);
            });
        }
    }

    private static Map<String, String> loadLanguage(String language) {
        String resourcePath = "/assets/yash/lang/" + language + ".json";
        var stream = LanguageParityTest.class.getResourceAsStream(resourcePath);
        assertNotNull(stream, () -> "Missing language resource " + resourcePath);

        try (Reader reader = new java.io.InputStreamReader(stream, StandardCharsets.UTF_8)) {
            Map<String, String> translations = GSON.fromJson(reader, LANGUAGE_TYPE);
            assertNotNull(translations, () -> "Invalid language resource " + resourcePath);
            return translations;
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to read " + resourcePath, exception);
        }
    }
}
