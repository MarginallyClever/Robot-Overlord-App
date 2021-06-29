package com.marginallyclever.robotOverlord.swingInterface.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.swingInterface.translator.Translator;

/**
 * Load the world from a file. This action is not an undoable action.
 * @author Admin
 *
 */
public class OpenAction extends AbstractAction implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected RobotOverlord ro;
	
	public OpenAction(RobotOverlord ro) {
		super(Translator.get("Open"));
        putValue(SHORT_DESCRIPTION, Translator.get("Open a saved project."));
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK) );
		this.ro = ro;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("RO files", "RO");
		fc.setFileFilter(filter);
		int returnVal = fc.showOpenDialog(ro.getMainFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
            ro.loadWorldFromFile(fc.getSelectedFile().getAbsolutePath());
		}
	}
}
