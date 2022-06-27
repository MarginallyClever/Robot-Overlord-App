package com.marginallyclever.robotOverlord.demos;

import javax.vecmath.Vector3d;

import com.marginallyclever.robotOverlord.Camera;
import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.demos.demoAssets.TrayCabinet;
import com.marginallyclever.robotOverlord.robots.stewartplatform.LinearStewartPlatform;
import com.marginallyclever.robotOverlord.robots.stewartplatform.RotaryStewartPlatform;
import com.marginallyclever.robotOverlord.sceneElements.Box;
import com.marginallyclever.robotOverlord.sceneElements.Light;
import com.marginallyclever.robotOverlord.shape.Shape;

public class StewartPlatformDemo implements Demo {
	@Override
	public String getName() {
		return "Stewart Platforms";
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
    	
		// add some collision bounds
		Box box;
		
		sc.addChild(box = new Box());
		box.setName("Front wall");
		box.setSize(233.5,1,100);
		box.setPosition(new Vector3d(69.75,65,50));
		box.getMaterial().setDiffuseColor(0f/255f,169f/255f,255f/255f,1f);
		
		sc.addChild(box = new Box());
		box.setName("Back wall");
		box.setSize(180,1,100);
		box.setPosition(new Vector3d(-47.5,-25.5,50));
		box.setRotation(new Vector3d(0, 0, Math.toRadians(-90)));
		box.getMaterial().setDiffuseColor(0f/255f,169f/255f,255f/255f,1f);

		Shape table = new Shape("Table","/table.stl");
		sc.addChild(table);
		table.setPosition(new Vector3d(0,0,-0.75));

		TrayCabinet trayCabinet;
		sc.addChild(trayCabinet=new TrayCabinet());
		trayCabinet.setPosition(new Vector3d(35,49.5,0));
		sc.addChild(trayCabinet=new TrayCabinet());
		trayCabinet.setPosition(new Vector3d(35,49.5,21.75));

		RotaryStewartPlatform rsp = new RotaryStewartPlatform();
		sc.addChild(rsp);
		LinearStewartPlatform lsp = new LinearStewartPlatform();
		sc.addChild(lsp);
		lsp.setPosition(new Vector3d(50,0,0));
	}
}
