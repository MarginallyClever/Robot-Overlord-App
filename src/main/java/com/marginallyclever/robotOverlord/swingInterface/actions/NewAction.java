package com.marginallyclever.robotOverlord.swingInterface.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.swingInterface.translator.Translator;

/**
 * Clear the world and start anew. This action is not an undoable action.
 * @author Dan Royer
 *
 */
public class NewAction extends AbstractAction implements ActionListener {
	private static final long serialVersionUID = 1L;
	protected RobotOverlord ro;
	
	public NewAction(RobotOverlord ro) {
		super(Translator.get("New"));
        putValue(SHORT_DESCRIPTION, "Remove everything from the world.");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK | ActionEvent.SHIFT_MASK) );
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
        	ro.newScene();
        }
	}
}
