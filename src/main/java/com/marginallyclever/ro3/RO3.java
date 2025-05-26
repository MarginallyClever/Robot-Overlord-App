package com.marginallyclever.ro3;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

/**
 * <p><b>RO3 (Robot Overlord 3)</b> is a robot simulation and control program.</p>
 * You can find the friendly user manual at <a href="mcr.dozuki.com">mcr.dozuki.com</a>.
 */
public class RO3 {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RO3.class);

    public static void main(String[] args) {
        Registry.start();

        if(GraphicsEnvironment.isHeadless()) {
            System.out.println("This app must be run with a display, keyboard, and mouse.");
        }

        setLookAndFeel();
        SwingUtilities.invokeLater(() -> (new RO3Frame()).setVisible(true));
    }

    public static void setLookAndFeel() {
        logger.info("Setting look and feel...");
        FlatLaf.registerCustomDefaultsSource( RO3.class.getPackageName() );
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            // option 2: UIManager.setLookAndFeel(new FlatDarkLaf());
            // option 3: UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            logger.warn("failed to set native look and feel.", ex);
        }
    }
}
