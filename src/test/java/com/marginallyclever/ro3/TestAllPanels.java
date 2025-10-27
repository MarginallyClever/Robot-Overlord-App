package com.marginallyclever.ro3;

import com.formdev.flatlaf.FlatLightLaf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * <p>Display a card layout of all the {@link JPanel}s in the project.  This would be handy for translators to see all
 * the panels in one place.</p>
 */
@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
public class TestAllPanels {
    private static final Logger logger = LoggerFactory.getLogger(TestAllPanels.class);

    @Test
    public void testAllPanels() {
        Registry.start();

        // print the classpath
        System.out.println("Classpath: "+ System.getProperty("java.class.path"));

        FlatLightLaf.setup();

        AllPanels allPanels = new AllPanels();
        var frame = allPanels.buildFrame();

        // version 1, pause for 0.5 seconds
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}

        frame.dispose();
    }
}
