package com.marginallyclever.robotOverlord.demos;

import javax.vecmath.Vector3d;

import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.components.CameraComponent;
import com.marginallyclever.robotOverlord.components.Pose;
import com.marginallyclever.robotOverlord.components.sceneElements.GridEntity;
import com.marginallyclever.robotOverlord.components.sceneElements.LightEntity;

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
		Pose pose = camera.getEntity().getComponent(Pose.class);
		pose.setPosition(new Vector3d(40,-91,106));
		camera.setPan(-16);
		camera.setTilt(53);
		camera.setZoom(100);
		
		// add some lights
    	LightEntity light;

		sc.addChild(light = new LightEntity());
		light.setName("Light");
    	light.setPosition(new Vector3d(60,-60,160));
    	light.setDiffuse(0.8f,0.8f,1,1);
    	light.setSpecular(0.5f, 0.5f, 0.5f, 1.0f);

		sc.addChild(light = new LightEntity());
		light.setName("Light");
    	light.setPosition(new Vector3d(-60,60,-160));
    	light.setDiffuse(1,0.8f,0.8f,1);
    	light.setSpecular(0.5f, 0.5f, 0.5f, 1.0f);

    	GridEntity grid = new GridEntity();
		sc.addChild(grid);
		grid.setName("Floor");
	}
}
