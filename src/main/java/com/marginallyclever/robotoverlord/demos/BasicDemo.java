package com.marginallyclever.robotoverlord.demos;

import javax.vecmath.Vector3d;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.components.*;
import com.marginallyclever.robotoverlord.components.shapes.Box;
import com.marginallyclever.robotoverlord.components.shapes.Grid;
import com.marginallyclever.robotoverlord.components.shapes.Sphere;

public class BasicDemo implements Demo {
	@Override
	public String getName() {
		return "Basic";
	}
	
	@Override
	public void execute(RobotOverlord ro) {
		ro.newScene();
		Entity sc = ro.getScene();
		
		// adjust default camera
		CameraComponent camera = sc.findFirstComponent(CameraComponent.class);
		PoseComponent pose = camera.getEntity().getComponent(PoseComponent.class);
		pose.setPosition(new Vector3d(40,-91,106));
		camera.setPan(-16);
		camera.setTilt(53);
		camera.setZoom(100);
		
		// add some lights
    	LightComponent light;
		Entity light0 = new Entity();
		sc.addChild(light0);
		light0.addComponent(pose = new PoseComponent());
		light0.addComponent(light = new LightComponent());
    	pose.setPosition(new Vector3d(60,-60,160));
    	light.setDiffuse(0.8f,0.8f,1,1);
    	light.setSpecular(0.5f, 0.5f, 0.5f, 1.0f);

		light0 = new Entity();
		sc.addChild(light0);
		light0.addComponent(pose = new PoseComponent());
		light0.addComponent(light = new LightComponent());
    	pose.setPosition(new Vector3d(-60,60,-160));
    	light.setDiffuse(1,0.8f,0.8f,1);
    	light.setSpecular(0.5f, 0.5f, 0.5f, 1.0f);

		Entity boxEntity = new Entity("Box");
		boxEntity.addComponent(pose = new PoseComponent());
		Box box = new Box();
		boxEntity.addComponent(box);
		boxEntity.addComponent(new MaterialComponent());
		sc.addChild(boxEntity);
		pose.setPosition(new Vector3d(-10,0,0));

		Entity sphereEntity = new Entity("Sphere");
		sphereEntity.addComponent(pose = new PoseComponent());
		Sphere sphere = new Sphere();
		sphereEntity.addComponent(sphere);
		sphereEntity.addComponent(new MaterialComponent());
		sc.addChild(sphereEntity);
		pose.setPosition(new Vector3d(10,0,0));

		Entity gridEntity = new Entity("Floor");
		MaterialComponent mat = new MaterialComponent();
		gridEntity.addComponent(pose = new PoseComponent());
		gridEntity.addComponent(mat);
    	Grid grid = new Grid();
		gridEntity.addComponent(grid);
		sc.addChild(gridEntity);
		mat.setDiffuseColor(0.5,0.5,0.5,1);
		mat.setLit(false);
	}
}
