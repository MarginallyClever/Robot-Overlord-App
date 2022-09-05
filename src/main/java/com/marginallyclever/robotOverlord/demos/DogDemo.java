package com.marginallyclever.robotOverlord.demos;

import javax.vecmath.Vector3d;

import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.components.*;
import com.marginallyclever.robotOverlord.robots.dog.DogRobot;
import com.marginallyclever.robotOverlord.components.sceneElements.GridEntity;

public class DogDemo implements Demo {
	@Override
	public String getName() {
		return "Dog";
	}
	
	@Override
	public void execute(RobotOverlord ro) {
		ro.newScene();
		Entity sc = ro.getScene();
		
		// adjust default camera
		CameraComponent camera = ro.getCamera();
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
    	light.setDiffuse(1,1,1,1);
    	light.setSpecular(0.5f, 0.5f, 0.5f, 1.0f);
    	light.setAttenuationLinear(0.0014);
    	light.setAttenuationQuadratic(7*1e-6);
    	light.setDirectional(true);

		DogRobot dog = new DogRobot();
		sc.addChild(dog);
		dog.setPosition(new Vector3d(0,0,17));
		dog.setRotation(new Vector3d(Math.toRadians(90),0,0));

		Entity gridEntity = new Entity("Floor");
		MaterialComponent mat = new MaterialComponent();
		gridEntity.addComponent(pose = new PoseComponent());
		gridEntity.addComponent(mat);
		GridComponent grid = new GridComponent();
		gridEntity.addComponent(grid);
		dog.addChild(gridEntity);
		mat.setDiffuseColor(0.5,0.5,0.5,1);
		mat.setLit(false);
	}
}
