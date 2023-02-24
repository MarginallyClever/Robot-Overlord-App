package com.marginallyclever.robotoverlord.swinginterface.translator;

import org.junit.jupiter.api.Test;

import java.util.Locale;

public class TranslatorTest {
    @Test
    public void testBadLocaleFallbackToEnglish() {
        testBadLocaleFallbackToEnglish("es-ES");
        testBadLocaleFallbackToEnglish("en-US");
    }

    public void testBadLocaleFallbackToEnglish(String languageTag) {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag(languageTag));
            Translator.start();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            Locale.setDefault(original);
        }
    }
}
