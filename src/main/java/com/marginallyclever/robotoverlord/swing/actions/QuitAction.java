package com.marginallyclever.robotoverlord.swing.actions;

import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swing.translator.Translator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Quit the application. This action is not an undoable action.
 * @author Admin
 *
 */
public class QuitAction extends AbstractAction implements ActionListener {
	protected RobotOverlord app;
	
	public QuitAction(RobotOverlord app) {
		super(Translator.get("QuitAction.name"));
        putValue(SHORT_DESCRIPTION, Translator.get("QuitAction.shortDescription"));
        
        // on Windows
        //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK) );
        // on OSX
        //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.ALT_MASK | ActionEvent.SHIFT_MASK) );
        
		this.app = app;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		app.confirmClose();
	}
}
