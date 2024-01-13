package com.marginallyclever.ro3;

import com.marginallyclever.ro3.apps.RO3Frame;

import javax.swing.*;
import java.awt.*;

/**
 * <p><b>RO3 (Robot Overlord 3)</b> is a robot simulation and control program.</p>
 * You can find the friendly user manual at <a href="mcr.dozuki.com">mcr.dozuki.com</a>.
 */
public class RO3 {
    public static void main(String[] args) {
        Registry.start();

        if(!GraphicsEnvironment.isHeadless()) {
            SwingUtilities.invokeLater(() -> (new RO3Frame()).setVisible(true));
        }
    }
}
