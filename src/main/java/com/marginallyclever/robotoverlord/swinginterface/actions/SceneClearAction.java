package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.EntityManager;
import com.marginallyclever.robotoverlord.components.shapes.Box;
import com.marginallyclever.robotoverlord.swinginterface.UnicodeIcon;
import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.components.LightComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.*;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Clear the world and start anew. This action is not an undoable action.
 * @author Dan Royer
 *
 */
public class SceneClearAction extends AbstractAction {
	private final EntityManager entityManager;

	public SceneClearAction(EntityManager entityManager) {
		super(Translator.get("SceneClearAction.name"));
		this.entityManager = entityManager;
		putValue(SMALL_ICON,new UnicodeIcon("🌱"));
		putValue(SHORT_DESCRIPTION, Translator.get("SceneClearAction.shortDescription"));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK) );
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Component source = (Component) e.getSource();
		JFrame parentFrame = (JFrame)SwingUtilities.getWindowAncestor(source);

        int result = JOptionPane.showConfirmDialog(
                parentFrame,
                Translator.get("Are you sure?"),
                (String)this.getValue(AbstractAction.NAME),
                JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
			clearScene();
			UndoSystem.reset();
			addDefaultEntities();
        }
	}

	public void clearScene() {
		entityManager.clear();
	}

	public void addDefaultEntities() {
		Entity mainCamera = new Entity("Main Camera");
		CameraComponent camera = new CameraComponent();
		mainCamera.addComponent(camera);
		PoseComponent pose = mainCamera.findFirstComponent(PoseComponent.class);

		entityManager.addEntityToParent(mainCamera, entityManager.getRoot());
		pose.setPosition(new Vector3d(25,20,15));
		camera.lookAt(new Vector3d(0,0,0));

		Entity light0 = new Entity("Light");
		light0.addComponent(pose = new PoseComponent());
		light0.addComponent(new LightComponent());
		entityManager.addEntityToParent(light0,mainCamera);
		pose.setPosition(new Vector3d(0,0,50));

		Entity box = new Entity("box");
		box.addComponent(new Box());
		entityManager.addEntityToParent(box, entityManager.getRoot());
	}
}
