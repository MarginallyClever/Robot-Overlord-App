package com.marginallyclever.robotoverlord.demos;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.components.LightComponent;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.shapes.Grid;
import com.marginallyclever.robotoverlord.robots.skycam.Skycam;

import javax.vecmath.Vector3d;

public class SkycamDemo implements Demo {
	@Override
	public String getName() {
		return "Skycam";
	}	

	@Override
	public void execute(RobotOverlord ro) {
		Entity sc = ro.getScene();
		
		// adjust default camera
		CameraComponent camera = ro.getCamera();
		PoseComponent pose = camera.getEntity().findFirstComponent(PoseComponent.class);
		pose.setPosition(new Vector3d(40,-91,106));
		camera.setPan(-16);
		camera.setTilt(53);
		camera.setZoom(100);
		
		// add some lights
		LightComponent light;
		Entity light0 = new Entity();
		sc.addEntity(light0);
		light0.addComponent(pose = new PoseComponent());
		light0.addComponent(light = new LightComponent());
    	pose.setPosition(new Vector3d(60,-60,160));
    	light.setDiffuse(1,1,1,1);
    	light.setSpecular(0.5f, 0.5f, 0.5f, 1.0f);
    	light.setAttenuationLinear(0.0014);
    	light.setAttenuationQuadratic(7*1e-6);
    	light.setDirectional(true);
    	
		// adjust grid

		Entity gridEntity = new Entity("Floor");
		MaterialComponent mat = new MaterialComponent();
		gridEntity.addComponent(pose = new PoseComponent());
		gridEntity.addComponent(mat);
		Grid grid = new Grid();
		gridEntity.addComponent(grid);
		sc.addEntity(gridEntity);
		mat.setDiffuseColor(0.5,0.5,0.5,1);
		mat.setLit(false);

		pose.setPosition(new Vector3d(60.0,0,-0.5));
		
    	// add a sixi robot
		Skycam skycam=new Skycam();
		sc.addEntity(skycam);
	}
}
