package com.marginallyclever.robotOverlord.demos;

import javax.vecmath.Vector3d;

import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.components.*;
import com.marginallyclever.robotOverlord.components.sceneElements.GridEntity;

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
		BoxComponent box = new BoxComponent();
		boxEntity.addComponent(box);
		sc.addChild(boxEntity);
		pose.setPosition(new Vector3d(-10,0,0));

		Entity gridEntity = new Entity("Floor");
		MaterialComponent mat = new MaterialComponent();
		gridEntity.addComponent(pose = new PoseComponent());
		gridEntity.addComponent(mat);
    	GridComponent grid = new GridComponent();
		gridEntity.addComponent(grid);
		sc.addChild(gridEntity);
		mat.setDiffuseColor(0.5,0.5,0.5,1);
		mat.setLit(false);
	}
}
