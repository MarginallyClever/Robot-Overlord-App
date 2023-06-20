package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.preferences.GraphicsPreferences;
import com.marginallyclever.robotoverlord.preferences.GraphicsPreferencesPanel;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class EditPreferencesAction extends AbstractAction implements ActionListener {
    private final Component parent;

    public EditPreferencesAction(Component parent) {
        super(Translator.get("EditPreferencesAction.name"));
        this.parent = parent;
        putValue(SHORT_DESCRIPTION, Translator.get("EditPreferencesAction.shortDescription"));
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        Window w = SwingUtilities.getWindowAncestor(parent);

        GraphicsPreferencesPanel panel = new GraphicsPreferencesPanel();

        int result = JOptionPane.showConfirmDialog(w, panel, "Preferences", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if(result == JOptionPane.OK_OPTION) {
            GraphicsPreferences.save();
        } else {
            GraphicsPreferences.load();
        }
    }
}
