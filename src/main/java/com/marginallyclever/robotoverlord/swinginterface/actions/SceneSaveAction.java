package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.EntityManager;
import com.marginallyclever.robotoverlord.swinginterface.UnicodeIcon;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Save the world state to a file.  This action is not an undoable action.
 * @author Admin
 *
 */
public class SceneSaveAction extends AbstractAction implements ActionListener {
	private static final Logger logger = LoggerFactory.getLogger(SceneSaveAction.class);
	private final EntityManager entityManager;

	/**
	 * The file chooser remembers the last path.
	 */
	private static final JFileChooser fc = new JFileChooser();
	
	public SceneSaveAction(EntityManager entityManager) {
		super(Translator.get("SceneSaveAction.name"));
		this.entityManager = entityManager;
		fc.setFileFilter(RobotOverlord.FILE_FILTER);
		putValue(SMALL_ICON,new UnicodeIcon("💾"));
		putValue(SHORT_DESCRIPTION, Translator.get("SceneSaveAction.shortDescription"));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK) );
	}

	public static void setLastDirectory(String s) {
		fc.setCurrentDirectory(new File(s));
	}

	public static String getLastDirectory() {
		return fc.getCurrentDirectory().getAbsolutePath();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Component source = (Component) e.getSource();
		JFrame parentFrame = (JFrame)SwingUtilities.getWindowAncestor(source);

		if (fc.showSaveDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
			String name = addExtensionIfNeeded(fc.getSelectedFile().getAbsolutePath());
			try {
				saveModelToFile(name);
			} catch(Exception ex) {
				logger.error("Error saving file: ",ex);
				JOptionPane.showMessageDialog(parentFrame,ex.getLocalizedMessage());
				ex.printStackTrace();
			}
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

	private void saveModelToFile(String absolutePath) throws IOException {
		// try-with-resources will close the file for us.
		try(BufferedWriter w = new BufferedWriter(new FileWriter(absolutePath))) {
			w.write(entityManager.toJSON().toString());
		}
	}
}
