package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.Scene;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.EntityPasteEdit;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SceneLoadAction extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(SceneLoadAction.class);
    private final RobotOverlord ro;

    /**
     * The file chooser remembers the last path.
     */
    private static final JFileChooser fc = new JFileChooser();

    public SceneLoadAction(String name, RobotOverlord ro) {
        super(name);
        this.ro=ro;
        fc.setFileFilter(RobotOverlord.FILE_FILTER);
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (fc.showOpenDialog(ro.getMainFrame()) == JFileChooser.APPROVE_OPTION) {
            try {
                Scene scene = loadScene(fc.getSelectedFile());

                SceneClearAction clear = new SceneClearAction("Clear Scene",ro);
                clear.clearScene();
                UndoSystem.reset();

                List<Entity> dest = new ArrayList<>();
                dest.add(ro.getScene());
                UndoSystem.addEvent(this,new EntityPasteEdit((String)this.getValue(Action.NAME),ro,scene,dest));
                ro.getScene().setScenePath(fc.getSelectedFile().getAbsolutePath());
            } catch(Exception e1) {
                logger.error(e1.getMessage());
                JOptionPane.showMessageDialog(ro.getMainFrame(),e1.getLocalizedMessage());
                e1.printStackTrace();
            }
        }
    }

    private Scene loadScene(File file) throws IOException {
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
