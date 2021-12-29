package com.marginallyclever.robotOverlord.demos;

import javax.vecmath.Vector3d;

import com.marginallyclever.robotOverlord.Camera;
import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.robots.dog.DogRobot;
import com.marginallyclever.robotOverlord.sceneElements.Grid;
import com.marginallyclever.robotOverlord.sceneElements.Light;

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
		Camera camera = ro.getCamera();
		camera.setPosition(new Vector3d(40,-91,106));
		camera.setPan(-16);
		camera.setTilt(53);
		camera.setZoom(100);
		camera.update(0);
		
		// add some lights
    	Light light;

		sc.addChild(light = new Light());
		light.setName("Light");
    	light.setPosition(new Vector3d(60,-60,160));
    	light.setDiffuse(1,1,1,1);
    	light.setSpecular(0.5f, 0.5f, 0.5f, 1.0f);
    	light.setAttenuationLinear(0.0014);
    	light.setAttenuationQuadratic(7*1e-6);
    	light.setDirectional(true);

    	Grid grid = new Grid();
		sc.addChild(grid);
		grid.setName("Floor");
		
		DogRobot dog = new DogRobot();
		sc.addChild(dog);
		dog.setPosition(new Vector3d(0,0,17));
		dog.setRotation(new Vector3d(Math.toRadians(90),0,0));
		
		grid.shadow(dog);
	}
}
