package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;

/**
 * Save the world state to a file.  This action is not an undoable action.
 * @author Admin
 *
 */
public class SceneSaveAction extends AbstractAction implements ActionListener {
	private final RobotOverlord ro;

	/**
	 * The file chooser remembers the last path.
	 */
	private static final JFileChooser fc = new JFileChooser();
	
	public SceneSaveAction(String name, RobotOverlord ro) {
		super(name);
		this.ro = ro;
		fc.setFileFilter(RobotOverlord.FILE_FILTER);
	}

	public static void setLastDirectory(String s) {
		fc.setCurrentDirectory(new File(s));
	}

	public static String getLastDirectory() {
		return fc.getCurrentDirectory().getAbsolutePath();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
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
