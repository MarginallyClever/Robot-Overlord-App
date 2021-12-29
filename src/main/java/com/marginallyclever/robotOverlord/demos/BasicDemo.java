package com.marginallyclever.robotOverlord.demos;

import javax.vecmath.Vector3d;

import com.marginallyclever.robotOverlord.Camera;
import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.sceneElements.Grid;
import com.marginallyclever.robotOverlord.sceneElements.Light;

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
    	light.setDiffuse(0.8f,0.8f,1,1);
    	light.setSpecular(0.5f, 0.5f, 0.5f, 1.0f);

		sc.addChild(light = new Light());
		light.setName("Light");
    	light.setPosition(new Vector3d(-60,60,-160));
    	light.setDiffuse(1,0.8f,0.8f,1);
    	light.setSpecular(0.5f, 0.5f, 0.5f, 1.0f);

    	Grid grid = new Grid();
		sc.addChild(grid);
		grid.setName("Floor");
	}
}
