package com.marginallyclever.robotOverlord.swingInterface.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.swingInterface.translator.Translator;

/**
 * Quit the application. This action is not an undoable action.
 * @author Admin
 *
 */
public class CommandQuit extends AbstractAction implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected RobotOverlord ro;
	
	public CommandQuit(RobotOverlord ro) {
		super(Translator.get("Quit"));
        putValue(SHORT_DESCRIPTION, Translator.get("Gracefully terminate this app"));
        
        // on windows
        //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK) );
        // on osx
        //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.ALT_MASK | ActionEvent.SHIFT_MASK) );
        
		this.ro = ro;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ro.confirmClose();
	}
}
