package com.marginallyclever.robotoverlord.systems;

import javax.swing.*;
import java.awt.*;

/**
 * A collection of static methods for working with {@link EntitySystem}s.
 *
 * @author Dan Royer
 * @since 2.5.6
 */
public class EntitySystemUtils {
    public static JDialog makePanel(JPanel panel, Component parent, String title) {
        JFrame parentFrame = (parent instanceof JFrame)
                            ? (JFrame)parent
                            : (JFrame)SwingUtilities.getWindowAncestor(parent);

        try {
            JDialog dialog = new JDialog(parentFrame, title);
            dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            dialog.add(panel);
            dialog.pack();
            dialog.setLocationRelativeTo(parentFrame);
            dialog.setVisible(true);
            return dialog;
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showConfirmDialog(parentFrame, ex.getMessage(), "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}
