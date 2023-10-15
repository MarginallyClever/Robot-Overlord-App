package com.marginallyclever.robotoverlord.swing.translator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.PropertyPermission;

public class TranslatorTest {
    static boolean canRun=false;
    @BeforeAll
    public static void setUp() {
        SecurityManager securityManager = System.getSecurityManager();
        if(securityManager != null) {
            try {
                securityManager.checkPermission(new PropertyPermission("user.language", "write"));
            } catch (SecurityException e) {
                System.out.println("Cannot run TranslatorTest cases: " + e.getMessage());
                canRun = false;
            }
        }
    }


    @Test
    public void testChangeLocale()  {
        if(!canRun) return;
        Locale original = Locale.getDefault();
        Locale.setDefault(Locale.forLanguageTag("es-ES"));
        Locale.setDefault(original);
    }

    @Test
    public void testBadLocaleFallbackToEnglish() {
        if(!canRun) return;
        testBadLocaleFallbackToEnglish("es-ES");
        testBadLocaleFallbackToEnglish("en-US");
    }

    public void testBadLocaleFallbackToEnglish(String languageTag) {
        System.out.println("Testing locale: " + languageTag);

        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag(languageTag));
            Translator.start();
        }
        finally {
            Locale.setDefault(original);
        }
    }
}
