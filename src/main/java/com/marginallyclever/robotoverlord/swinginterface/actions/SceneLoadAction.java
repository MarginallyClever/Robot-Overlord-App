package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.Scene;
import com.marginallyclever.robotoverlord.UnicodeIcon;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class SceneLoadAction extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(SceneLoadAction.class);
    private final RobotOverlord ro;

    /**
     * The file chooser remembers the last path.
     */
    private static final JFileChooser fc = new JFileChooser();

    public SceneLoadAction(RobotOverlord ro) {
        super(Translator.get("SceneLoadAction.name"));
        this.ro=ro;
        fc.setFileFilter(RobotOverlord.FILE_FILTER);
        putValue(Action.SMALL_ICON,new UnicodeIcon("🗁"));
        putValue(Action.SHORT_DESCRIPTION, Translator.get("SceneLoadAction.shortDescription"));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK) );
    }

    public static void setLastDirectory(String s) {
        fc.setCurrentDirectory(new File(s));
    }

    public static String getLastDirectory() {
        return fc.getCurrentDirectory().getAbsolutePath();
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (fc.showOpenDialog(ro.getMainFrame()) == JFileChooser.APPROVE_OPTION) {
            try {
                Scene source = loadScene(fc.getSelectedFile());

                SceneClearAction clear = new SceneClearAction(ro);
                clear.clearScene();

                Scene destination = ro.getScene();
                destination.setScenePath(source.getScenePath());
                // when entities are added to destination they will automatically be removed from source.
                // to prevent concurrent modification exception we have to have a copy of the list.
                List<Entity> entities = new LinkedList<>(source.getChildren());
                // now do the move safely.
                for(Entity e : entities) {
                    destination.addEntity(e);
                }

                UndoSystem.reset();
            } catch(Exception e1) {
                logger.error(e1.getMessage());
                JOptionPane.showMessageDialog(ro.getMainFrame(),e1.getLocalizedMessage());
                e1.printStackTrace();
            }
        }
    }

    public Scene loadScene(File file) throws IOException {
        StringBuilder responseStrBuilder = new StringBuilder();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String inputStr;
            while ((inputStr = reader.readLine()) != null) {
                responseStrBuilder.append(inputStr);
            }
        }

        logger.debug("Loading scene from {}", file.getAbsolutePath());

        String pathName = (Paths.get(file.getAbsolutePath())).getParent().toString();
        Scene nextScene = new Scene(pathName);
        try {
            nextScene.parseJSON(new JSONObject(responseStrBuilder.toString()));
        } catch(Exception e1) {
            logger.error(e1.getMessage());
            JOptionPane.showMessageDialog(ro.getMainFrame(),e1.getLocalizedMessage());
            e1.printStackTrace();
        }

        return nextScene;
    }

}
