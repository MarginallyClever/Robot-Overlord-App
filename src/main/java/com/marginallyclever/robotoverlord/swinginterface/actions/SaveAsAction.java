package com.marginallyclever.robotoverlord.swinginterface.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

/**
 * Save the world state to a file.  This action is not an undoable action.
 * @author Admin
 *
 */
public class SaveAsAction extends AbstractAction implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected RobotOverlord ro;
	
	public SaveAsAction(RobotOverlord ro) {
		super(Translator.get("Save As..."));
        putValue(SHORT_DESCRIPTION, Translator.get("Save this project."));
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.SHIFT_MASK | ActionEvent.CTRL_MASK) );
		this.ro = ro;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("RO files", "RO");
		fc.setFileFilter(filter);
		//fc.setSelectedFile(new File(projectFilename));
		int returnVal = fc.showSaveDialog(ro.getMainFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String destination=fc.getSelectedFile().getAbsolutePath();
            ro.saveWorldToFile(destination);
			// TODO remember destination for CommandSave
            //projectFilename = destination;
		}
	}
}
