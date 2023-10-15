package com.marginallyclever.robotoverlord.swing.actions;

import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.preferences.*;
import com.marginallyclever.robotoverlord.swing.translator.Translator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class EditPreferencesAction extends AbstractAction implements ActionListener {
    private final Component parent;
    private final RobotOverlord app;

    public EditPreferencesAction(RobotOverlord app,Component parent) {
        super(Translator.get("EditPreferencesAction.name"));
        this.app = app;
        this.parent = parent;
        putValue(SHORT_DESCRIPTION, Translator.get("EditPreferencesAction.shortDescription"));
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        Window w = SwingUtilities.getWindowAncestor(parent);

        PreferencesPanel preferencesPanel = new PreferencesPanel();
        int result = JOptionPane.showConfirmDialog(w, preferencesPanel, Translator.get("PreferencesPanel.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if(result == JOptionPane.OK_OPTION) {
            GraphicsPreferences.save();
            InteractionPreferences.save();
            app.buildRenderPanel();
            app.layoutComponents();
        } else {
            GraphicsPreferences.load();
            InteractionPreferences.load();
        }
    }
}
