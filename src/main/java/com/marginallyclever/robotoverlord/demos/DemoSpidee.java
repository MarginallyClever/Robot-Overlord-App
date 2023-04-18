package com.marginallyclever.robotoverlord.demos;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.components.*;
import com.marginallyclever.robotoverlord.components.demo.CrabRobotComponent;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.shapes.Grid;

import javax.vecmath.Vector3d;

/**
 * See https://github.com/tzaeschke/ode4j/blob/master/demo/src/main/java/org/ode4j/demo/DemoBuggy.java
 * @author Dan Royer
 *
 */
public class DemoSpidee implements Demo {

	@Override
	public String getName() {
		return "Crab robot SPIDEE-1";
	}

	@Override
	public void execute(RobotOverlord ro) {
		Entity sc = ro.getScene();

		// adjust default camera
		CameraComponent camera = ro.getCamera();
		PoseComponent pose = camera.getEntity().findFirstComponent(PoseComponent.class);
		pose.setPosition(new Vector3d(40/4f,-91/4f,106/4f));
		camera.lookAt(new Vector3d(0,0,0));
		camera.setOrbitDistance(50);

		// add a grid
		Entity gridEntity = new Entity("Grid");
		Grid grid = new Grid();
		gridEntity.addComponent(grid);
		sc.addEntity(gridEntity);
		grid.setWidth(100);
		grid.setLength(100);
		MaterialComponent mat = gridEntity.findFirstComponent(MaterialComponent.class);
		mat.setDiffuseColor(0,0,0,0);
		mat.setLit(false);

		// add spidee
		Entity spidee = new Entity("Spidee-1");
		spidee.addComponent(new CrabRobotComponent());
		sc.addEntity(spidee);
	}
}
