package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.Scene;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.PasteEdit;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

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
                UndoSystem.addEvent(this,new PasteEdit((String)this.getValue(Action.NAME),ro,scene));
            } catch(Exception e1) {
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
        } catch(Exception e) {
            logger.error(e.getMessage());
            JOptionPane.showMessageDialog(ro.getMainFrame(),e.getLocalizedMessage());
        }

        return nextScene;
    }
}
