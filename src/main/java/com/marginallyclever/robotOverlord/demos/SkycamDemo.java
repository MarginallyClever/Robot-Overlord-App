package com.marginallyclever.robotOverlord.demos;

import javax.vecmath.Vector3d;

import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.components.CameraComponent;
import com.marginallyclever.robotOverlord.components.LightComponent;
import com.marginallyclever.robotOverlord.components.PoseComponent;
import com.marginallyclever.robotOverlord.robots.skycam.Skycam;
import com.marginallyclever.robotOverlord.components.sceneElements.GridEntity;

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
    	
		// adjust grid
		GridEntity grid = new GridEntity();
		sc.addChild(grid);
		grid.width.set(140);
		grid.height.set(90);
		grid.setPosition(new Vector3d(60.0,0,-0.5));
		
    	// add a sixi robot
		Skycam skycam=new Skycam();
		sc.addChild(skycam);
	}
}
