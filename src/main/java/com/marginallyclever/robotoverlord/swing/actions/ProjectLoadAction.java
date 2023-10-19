package com.marginallyclever.robotoverlord.swing.actions;

import com.marginallyclever.robotoverlord.Project;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swing.UnicodeIcon;
import com.marginallyclever.robotoverlord.swing.UndoSystem;
import com.marginallyclever.robotoverlord.swing.translator.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.*;

/**
 * Load a scene from a file.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class ProjectLoadAction extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(ProjectLoadAction.class);
    public static final String UP_ARROW_FROM_BAR = "\u21A5";
    private final Project project;

    /**
     * The file chooser remembers the last path.
     */
    private static final JFileChooser fc = new JFileChooser();

    public ProjectLoadAction(Project project) {
        super(Translator.get("SceneLoadAction.name"));
        this.project = project;
        fc.setFileFilter(RobotOverlord.FILE_FILTER);
        putValue(SMALL_ICON,new UnicodeIcon(UP_ARROW_FROM_BAR));
        putValue(SHORT_DESCRIPTION, Translator.get("SceneLoadAction.shortDescription"));
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK) );
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

        if (fc.showOpenDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
            loadIntoScene(fc.getSelectedFile(),parentFrame);
        }
    }

    /**
     * Load the file into the current Scene.
     * @param file the file to load
     */
    public void loadIntoScene(File file,Component parentFrame) {
        try {
            Project projectToAdd = new Project();
            projectToAdd.load(file);

            ProjectClearAction clear = new ProjectClearAction(project);
            clear.clearScene();

            project.setPath(projectToAdd.getPath());
            project.addProject(projectToAdd);

            UndoSystem.reset();
        } catch(Exception e1) {
            logger.error("Failed to load. ",e1);
            JOptionPane.showMessageDialog(parentFrame,e1.getLocalizedMessage());
            e1.printStackTrace();
        }
    }

}
