package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.Scene;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.PasteEntityEdit;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LoadSceneAction extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(LoadSceneAction.class);
    private final RobotOverlord ro;

    /**
     * The file chooser remembers the last path.
     */
    private final JFileChooser fc = new JFileChooser();

    public LoadSceneAction(String name,RobotOverlord ro) {
        super(name);
        this.ro=ro;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        fc.setFileFilter(RobotOverlord.FILE_FILTER);
        if (fc.showOpenDialog(ro.getMainFrame()) == JFileChooser.APPROVE_OPTION) {
            try {
                Scene scene = loadScene(fc.getSelectedFile().getAbsolutePath());
                List<Entity> dest = new ArrayList<>();
                dest.add(ro.getScene());
                UndoSystem.addEvent(this,new PasteEntityEdit((String)this.getValue(Action.NAME),ro,scene,dest));
            } catch(Exception e1) {
                logger.error(e1.getMessage());
                JOptionPane.showMessageDialog(ro.getMainFrame(),e1.getLocalizedMessage());
                e1.printStackTrace();
            }
        }
    }

    private Scene loadScene(String absolutePath) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(absolutePath)));
        StringBuilder responseStrBuilder = new StringBuilder();
        String inputStr;
        while ((inputStr = reader.readLine()) != null) {
            responseStrBuilder.append(inputStr);
        }

        Scene nextScene = new Scene();
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
