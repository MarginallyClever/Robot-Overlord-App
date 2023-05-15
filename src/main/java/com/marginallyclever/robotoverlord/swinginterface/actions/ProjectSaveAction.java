package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.PathUtils;
import com.marginallyclever.robotoverlord.Project;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.UnicodeIcon;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
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
			String name = PathUtils.addExtensionIfNeeded(
					fc.getSelectedFile().getAbsolutePath(),
					RobotOverlord.FILE_FILTER.getExtensions());

			if(!assetsOutOfProjectApproved(parentFrame)) return;

			try {
				project.save(name);
			} catch(Exception ex) {
				logger.error("Error saving file: ",ex);
				JOptionPane.showMessageDialog(parentFrame,ex.getLocalizedMessage());
				ex.printStackTrace();
			}
		}
	}

	private boolean assetsOutOfProjectApproved(JFrame parentFrame) {
		List<String> list = project.getAllAssetsNotInProject();
		if(!list.isEmpty()) {
			logger.warn("Project does not contain all assets");

			JPanel container = new JPanel(new BorderLayout());
			container.add(new JLabel(Translator.get("ProjectSaveAction.doesNotContainAllAssets")),BorderLayout.NORTH);
			JList<String> listBox = new JList<>(list.toArray(new String[0]));
			container.add(new JScrollPane(listBox),BorderLayout.CENTER);
			int result = JOptionPane.showConfirmDialog(parentFrame,container,Translator.get("Warning"),JOptionPane.OK_CANCEL_OPTION);
			if(result == JOptionPane.CANCEL_OPTION) {
				logger.warn("Save cancelled by user.");
				return false;
			}
			logger.warn("Save approved by user.");
		}
		return true;
	}
}
