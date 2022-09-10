package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.Scene;
import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.components.LightComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.*;
import javax.vecmath.Vector3d;
import java.awt.event.ActionEvent;

/**
 * Clear the world and start anew. This action is not an undoable action.
 * @author Dan Royer
 *
 */
public class NewSceneAction extends AbstractAction {
	private final RobotOverlord ro;
	
	public NewSceneAction(String name,RobotOverlord ro) {
		super(name);
		this.ro = ro;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
        int result = JOptionPane.showConfirmDialog(
                ro.getMainFrame(),
                Translator.get("Are you sure?"),
                (String)this.getValue(AbstractAction.NAME),
                JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
			resetScene();
        }
	}

	public void resetScene() {
		Scene scene = ro.getScene();
		scene.removeAllEntities();

		PoseComponent pose = new PoseComponent();
		CameraComponent camera = new CameraComponent();
		scene.addComponent(new PoseComponent());
		Entity mainCamera = new Entity("Main Camera");
		mainCamera.addComponent(pose);
		mainCamera.addComponent(camera);
		scene.addEntity(mainCamera);
		pose.setPosition(new Vector3d(0,-10,-5));
		camera.lookAt(new Vector3d(0,0,0));

		Entity light0 = new Entity("Light");
		light0.addComponent(pose = new PoseComponent());
		light0.addComponent(new LightComponent());
		scene.addEntity(light0);
		pose.setPosition(new Vector3d(-50,-50,50));
	}
}
