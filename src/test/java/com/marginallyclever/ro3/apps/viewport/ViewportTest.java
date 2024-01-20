package com.marginallyclever.ro3.apps.viewport;

import com.marginallyclever.ro3.Registry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import java.util.List;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
public class ViewportTest {
    @BeforeAll
    public static void setUp() {
        Registry.start();
    }

    @Test
    public void test() {
        Viewport panel = new Viewport();

        // create and display a frame
        javax.swing.JFrame frame = new javax.swing.JFrame("Viewport Test");
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // version 1, pause for 0.5 seconds
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}

        // version 2, test specific behavior by simulating click and drag.


        frame.dispose();
    }

    @Test
    public void testPanel() {
        ViewportSettingsPanel panel = new ViewportSettingsPanel();

        // create and display a frame
        javax.swing.JFrame frame = new javax.swing.JFrame("Viewport Panel Test");
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // version 1, pause for 0.5 seconds
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}

        // version 2, test specific behavior by simulating click and drag.

        frame.dispose();
    }
}
