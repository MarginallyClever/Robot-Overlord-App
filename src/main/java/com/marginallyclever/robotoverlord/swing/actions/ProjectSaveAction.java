package com.marginallyclever.robotoverlord.swing.actions;

import com.marginallyclever.convenience.helpers.PathHelper;
import com.marginallyclever.robotoverlord.Project;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swing.UnicodeIcon;
import com.marginallyclever.robotoverlord.swing.translator.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

/**
 * Save the world state to a file.  This action is not an undoable action.
 * @author Admin
 *
 */
public class ProjectSaveAction extends AbstractAction implements ActionListener {
	private static final Logger logger = LoggerFactory.getLogger(ProjectSaveAction.class);
	private final Project project;

	/**
	 * The file chooser remembers the last path.
	 */
	private static final JFileChooser fc = new JFileChooser();
	
	public ProjectSaveAction(Project project) {
		super(Translator.get("SceneSaveAction.name"));
		this.project = project;
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
			String name = PathHelper.addExtensionIfNeeded(
					fc.getSelectedFile().getAbsolutePath(),
					RobotOverlord.FILE_FILTER.getExtensions());
			try {
				project.save(name);
			} catch(Exception ex) {
				logger.error("Error saving file: ",ex);
				JOptionPane.showMessageDialog(parentFrame,ex.getLocalizedMessage());
				ex.printStackTrace();
			}
		}
	}
}
