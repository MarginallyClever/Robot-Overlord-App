package com.marginallyclever.robotoverlord.systems;

import javax.swing.*;
import java.awt.*;

public class EntitySystemUtils {
    public static void makePanel(JPanel panel, JComponent parent, String title) {
        JFrame parentFrame = (JFrame)SwingUtilities.getWindowAncestor(parent);

        try {
            JDialog frame = new JDialog(parentFrame, title);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setPreferredSize(new Dimension(700,300));
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(parentFrame);
            frame.setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showConfirmDialog(parentFrame, ex.getMessage(), "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
        }
    }
}
