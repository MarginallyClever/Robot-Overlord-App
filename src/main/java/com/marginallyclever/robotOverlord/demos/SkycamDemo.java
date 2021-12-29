package com.marginallyclever.robotOverlord.demos;

import javax.vecmath.Vector3d;

import com.marginallyclever.robotOverlord.Camera;
import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.robots.skycam.Skycam;
import com.marginallyclever.robotOverlord.sceneElements.Grid;
import com.marginallyclever.robotOverlord.sceneElements.Light;

public class SkycamDemo implements Demo {
	@Override
	public String getName() {
		return "Skycam";
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
    	
		// adjust grid
		Grid grid = new Grid();
		sc.addChild(grid);
		grid.width.set(140);
		grid.height.set(90);
		grid.setPosition(new Vector3d(60.0,0,-0.5));
		
    	// add a sixi robot
		Skycam skycam=new Skycam();
		sc.addChild(skycam);
	}
}
