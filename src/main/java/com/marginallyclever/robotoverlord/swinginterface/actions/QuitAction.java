package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Quit the application. This action is not an undoable action.
 * @author Admin
 *
 */
public class QuitAction extends AbstractAction implements ActionListener {
	protected RobotOverlord ro;
	
	public QuitAction(RobotOverlord ro) {
		super(Translator.get("Quit"));
        putValue(SHORT_DESCRIPTION, Translator.get("Gracefully terminate this app"));
        
        // on Windows
        //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK) );
        // on OSX
        //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.ALT_MASK | ActionEvent.SHIFT_MASK) );
        
		this.ro = ro;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ro.confirmClose();
	}
}
