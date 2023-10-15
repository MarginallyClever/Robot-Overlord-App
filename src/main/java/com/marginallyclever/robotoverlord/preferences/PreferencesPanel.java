package com.marginallyclever.robotoverlord.preferences;


import com.marginallyclever.robotoverlord.swing.translator.Translator;

import javax.swing.*;
import java.awt.*;

/**
 * Panel to display preferences
 * @since 2.7.0
 * @author Dan Royer
 */
public class PreferencesPanel extends JPanel {
    private final JTabbedPane tabbedPane = new JTabbedPane();

    public PreferencesPanel() {
        super(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);

        addTab(Translator.get("InteractionPreferencesPanel.title"), new InteractionPreferencesPanel());
        addTab(Translator.get("GraphicsPreferencesPanel.title"), new GraphicsPreferencesPanel());
    }

    public void addTab(String title, JPanel group) {
        tabbedPane.addTab(title, group);
    }

    public static void main(String[] args) {
        Translator.start();
        JFrame frame = new JFrame(Translator.get("PreferencesPanel.title"));
        frame.setContentPane(new PreferencesPanel());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}