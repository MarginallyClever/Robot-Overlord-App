package com.marginallyclever.robotoverlord.swinginterface.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

/**
 * Clear the world and start anew. This action is not an undoable action.
 * @author Dan Royer
 *
 */
public class NewSceneAction extends AbstractAction {
	private final RobotOverlord ro;
	
	public NewSceneAction(String name,RobotOverlord ro) {
		super(name);
		this.ro = ro;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
        int result = JOptionPane.showConfirmDialog(
                ro.getMainFrame(),
                Translator.get("Are you sure?"),
                (String)this.getValue(AbstractAction.NAME),
                JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {

        }
	}
}
