package com.marginallyclever.robotOverlord.demos;

import javax.vecmath.Vector3d;

import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.components.CameraComponent;
import com.marginallyclever.robotOverlord.robots.dog.DogRobot;
import com.marginallyclever.robotOverlord.components.sceneElements.GridEntity;
import com.marginallyclever.robotOverlord.components.sceneElements.LightEntity;

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
		camera.setPosition(new Vector3d(40,-91,106));
		camera.setPan(-16);
		camera.setTilt(53);
		camera.setZoom(100);
		
		// add some lights
    	LightEntity light;

		sc.addChild(light = new LightEntity());
		light.setName("Light");
    	light.setPosition(new Vector3d(60,-60,160));
    	light.setDiffuse(1,1,1,1);
    	light.setSpecular(0.5f, 0.5f, 0.5f, 1.0f);
    	light.setAttenuationLinear(0.0014);
    	light.setAttenuationQuadratic(7*1e-6);
    	light.setDirectional(true);

    	GridEntity grid = new GridEntity();
		sc.addChild(grid);
		grid.setName("Floor");
		
		DogRobot dog = new DogRobot();
		sc.addChild(dog);
		dog.setPosition(new Vector3d(0,0,17));
		dog.setRotation(new Vector3d(Math.toRadians(90),0,0));
		
		grid.shadow(dog);
	}
}
