package com.marginallyclever.robotoverlord.preferences;

import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.parameters.swing.ViewPanelFactory;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.BackingStoreException;

/**
 * Panel to display interaction preferences
 * @since 2.7.0
 * @author Dan Royer
 */
public class InteractionPreferencesPanel extends JPanel {
    public InteractionPreferencesPanel() {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        InteractionPreferences.load();

        ViewPanelFactory factory = new ViewPanelFactory(new EntityManager());
        factory.addRange(InteractionPreferences.cursorSize,20,3);
        factory.add(InteractionPreferences.toolScale);
        factory.addRange(InteractionPreferences.compassSize,50,5);

        this.add(factory.getFinalView(),BorderLayout.NORTH);
    }

    public static void main(String[] args) throws BackingStoreException {
        Translator.start();
        JFrame frame = new JFrame(Translator.get("InteractionPreferencesPanel.title"));
        frame.setContentPane(new InteractionPreferencesPanel());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
