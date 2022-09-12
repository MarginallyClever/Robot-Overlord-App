package com.marginallyclever.robotoverlord.demos;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.components.LightComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.entities.BoxEntity;
import com.marginallyclever.robotoverlord.entities.ShapeEntity;
import com.marginallyclever.robotoverlord.entities.TrayCabinet;
import com.marginallyclever.robotoverlord.robots.stewartplatform.linear.LinearStewartPlatformCore;
import com.marginallyclever.robotoverlord.robots.stewartplatform.rotary.RotaryStewartPlatform;

import javax.vecmath.Vector3d;

public class StewartPlatformDemo implements Demo {
	@Override
	public String getName() {
		return "Stewart Platforms";
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
    	
		// add some collision bounds
		BoxEntity box;
		
		sc.addEntity(box = new BoxEntity());
		box.setName("Front wall");
		box.setSize(233.5,1,100);
		box.setPosition(new Vector3d(69.75,65,50));
		box.getMaterial().setDiffuseColor(0f/255f,169f/255f,255f/255f,1f);
		
		sc.addEntity(box = new BoxEntity());
		box.setName("Back wall");
		box.setSize(180,1,100);
		box.setPosition(new Vector3d(-47.5,-25.5,50));
		box.setRotation(new Vector3d(0, 0, Math.toRadians(-90)));
		box.getMaterial().setDiffuseColor(0f/255f,169f/255f,255f/255f,1f);

		ShapeEntity table = new ShapeEntity("Table","/table.stl");
		sc.addEntity(table);
		table.setPosition(new Vector3d(0,0,-0.75));

		TrayCabinet trayCabinet;
		sc.addEntity(trayCabinet=new TrayCabinet());
		trayCabinet.setPosition(new Vector3d(35,49.5,0));
		sc.addEntity(trayCabinet=new TrayCabinet());
		trayCabinet.setPosition(new Vector3d(35,49.5,21.75));

		RotaryStewartPlatform rsp = new RotaryStewartPlatform();
		sc.addEntity(rsp);
		LinearStewartPlatformCore lsp = new LinearStewartPlatformCore();
		sc.addEntity(lsp);
		lsp.setPosition(new Vector3d(50,0,0));
	}
}
