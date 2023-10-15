package com.marginallyclever.robotoverlord.preferences;

import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.parameters.swing.ComponentSwingViewFactory;
import com.marginallyclever.robotoverlord.swing.translator.Translator;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.BackingStoreException;

/**
 * Panel to display graphics preferences
 * @since 2.7.0
 * @author Dan Royer
 */
public class GraphicsPreferencesPanel extends JPanel {
    public GraphicsPreferencesPanel() {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        GraphicsPreferences.load();

        ComponentSwingViewFactory factory = new ComponentSwingViewFactory(new EntityManager());
        factory.add(GraphicsPreferences.verticalSync);
        factory.add(GraphicsPreferences.glDebug);
        factory.add(GraphicsPreferences.glTrace);
        factory.add(GraphicsPreferences.hardwareAccelerated);
        factory.add(GraphicsPreferences.backgroundOpaque);
        factory.add(GraphicsPreferences.doubleBuffered);
        factory.addComboBox(GraphicsPreferences.fsaaSamples,GraphicsPreferences.FSAA_NAMES);
        factory.addRange(GraphicsPreferences.framesPerSecond,120,1);
        factory.addRange(GraphicsPreferences.outlineWidth,20,1);
        factory.add(GraphicsPreferences.outlineColor);
        factory.add(GraphicsPreferences.backgroundColor);

        this.add(factory.getResult(),BorderLayout.NORTH);
    }

    public static void main(String[] args) throws BackingStoreException {
        Translator.start();
        JFrame frame = new JFrame(Translator.get("GraphicsPreferencesPanel.title"));
        frame.setContentPane(new GraphicsPreferencesPanel());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
