package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.Project;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.UnicodeIcon;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.*;

public class ProjectLoadAction extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(ProjectLoadAction.class);
    private final Project project;

    /**
     * The file chooser remembers the last path.
     */
    private static final JFileChooser fc = new JFileChooser();

    public ProjectLoadAction(Project project) {
        super(Translator.get("SceneLoadAction.name"));
        this.project = project;
        fc.setFileFilter(RobotOverlord.FILE_FILTER);
        putValue(SMALL_ICON,new UnicodeIcon("🗁"));
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
    public void loadIntoScene(File file,JFrame parentFrame) {
        try {
            Project source = new Project();
            source.load(file);

            ProjectClearAction clear = new ProjectClearAction(project);
            clear.clearScene();

            project.setPath(source.getPath());
            project.addProject(source);

            UndoSystem.reset();
        } catch(Exception e1) {
            logger.error("Failed to load. ",e1);
            JOptionPane.showMessageDialog(parentFrame,e1.getLocalizedMessage());
            e1.printStackTrace();
        }
    }

}
