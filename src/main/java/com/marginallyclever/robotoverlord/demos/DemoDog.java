package com.marginallyclever.robotoverlord.demos;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.demo.CrabRobotComponent;
import com.marginallyclever.robotoverlord.components.demo.DogRobotComponent;
import com.marginallyclever.robotoverlord.components.shapes.Grid;

import javax.vecmath.Vector3d;

/**
 * @author Dan Royer
 */
public class DemoDog implements Demo {

	@Override
	public String getName() {
		return "Dog robot Spot Micro";
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

		// add dog
		Entity dog = new Entity("SpotMicro");
		sc.addEntity(dog);
		dog.addComponent(new DogRobotComponent());
	}
}
