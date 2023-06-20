package com.marginallyclever.robotoverlord.preferences;

import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.parameters.swing.ViewElementFactory;
import com.marginallyclever.robotoverlord.parameters.swing.ViewPanelFactory;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class GraphicsPreferencesPanel extends JPanel {
    public GraphicsPreferencesPanel() {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        GraphicsPreferences.load();

        ViewPanelFactory factory = new ViewPanelFactory(new EntityManager());
        factory.add(GraphicsPreferences.framesPerSecond);
        factory.add(GraphicsPreferences.verticalSync);
        factory.add(GraphicsPreferences.glDebug);
        factory.add(GraphicsPreferences.glTrace);
        factory.add(GraphicsPreferences.hardwareAccelerated);
        factory.add(GraphicsPreferences.backgroundOpaque);
        factory.add(GraphicsPreferences.doubleBuffered);
        factory.addComboBox(GraphicsPreferences.fsaaSamples,GraphicsPreferences.FSAA_NAMES);

        this.add(factory.getFinalView(),BorderLayout.CENTER);
    }

    public static void main(String[] args) throws BackingStoreException {
        JFrame frame = new JFrame("GraphicsPreferencesPanel");
        frame.setContentPane(new GraphicsPreferencesPanel());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(350,220));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
