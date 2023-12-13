package com.marginallyclever.ro3;

import javax.swing.*;
import java.awt.*;

/**
 * RO3 is a robot arm control program.
 */
public class RO3 {
    public static void main(String[] args) {
        Registry.start();

        if(!GraphicsEnvironment.isHeadless()) {
            SwingUtilities.invokeLater(() -> (new RO3Frame()).setVisible(true));
        }
    }
}
