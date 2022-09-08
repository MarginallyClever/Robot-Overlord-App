package com.marginallyclever.robotoverlord.swinginterface.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;

import javax.swing.*;

import com.marginallyclever.robotoverlord.RobotOverlord;

/**
 * Save the world state to a file.  This action is not an undoable action.
 * @author Admin
 *
 */
public class SaveSceneAction extends AbstractAction implements ActionListener {
	private final RobotOverlord ro;

	/**
	 * The file chooser remembers the last path.
	 */
	private final JFileChooser fc = new JFileChooser();
	
	public SaveSceneAction(String name, RobotOverlord ro) {
		super(name);
		this.ro = ro;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		fc.setFileFilter(RobotOverlord.FILE_FILTER);
		if (fc.showSaveDialog(ro.getMainFrame()) == JFileChooser.APPROVE_OPTION) {
			String name = addExtensionIfNeeded(fc.getSelectedFile().getAbsolutePath());
			saveModelToFile(name);
		}
	}

	public String addExtensionIfNeeded(String filename) {
		int last = filename.lastIndexOf(".");
		String[] extensions = RobotOverlord.FILE_FILTER.getExtensions();
		if(last == -1) {
			// no extension at all
			return filename + "." + extensions[0];
		}

		String end = filename.substring(last+1).toLowerCase();
		for(String ext : extensions) {
			// has valid extension
			if(end.equals(ext.toLowerCase())) return filename;
		}
		// no matching extension
		return filename + "." + extensions[0];
	}

	private void saveModelToFile(String absolutePath) {
		try(BufferedWriter w = new BufferedWriter(new FileWriter(absolutePath))) {
			w.write(ro.getScene().toJSON().toString());
		} catch(Exception e) {
			JOptionPane.showMessageDialog(ro.getMainFrame(),e.getLocalizedMessage());
			e.printStackTrace();
		}
	}
}
