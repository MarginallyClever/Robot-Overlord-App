package com.marginallyclever.robotOverlord.demos;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.Light;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.demoAssets.Box;
import com.marginallyclever.robotOverlord.demoAssets.Grid;
import com.marginallyclever.robotOverlord.physics.RigidBody;

public class PhysicsDemo implements Demo {
	@Override
	public String getName() {
		return "Physics";
	}
	
	@Override
	public void execute(RobotOverlord ro) {
		ro.newScene();
		Entity sc = ro.getScene();
		
		// adjust default camera
		ro.camera.setPosition(new Vector3d(40/4,-91/4,106/4));
		ro.camera.lookAt(new Vector3d(0,0,0));
		ro.camera.setZoom(30);
		ro.camera.update(0);
		
		// add some lights
    	Light light;

		sc.addChild(light = new Light());
		light.setName("Light");
    	light.lightIndex=1;
    	light.setPosition(new Vector3d(60,-60,160));
    	light.setDiffuse(1,1,1,1);
    	light.setSpecular(0.5f, 0.5f, 0.5f, 1.0f);
    	light.attenuationLinear.set(0.0014);
    	light.attenuationQuadratic.set(7*1e-6);
    	light.setDirectional(true);
    	
		// add some collision bounds
		// adjust grid
		Grid grid = new Grid();
		sc.addChild(grid);

		int count=1;
		int countSq = (int)Math.sqrt(count);
		for(int i=0;i<count;++i) {
			Box box = new Box();
			box.setSize(10,//+Math.random()*5,
					    10,//+Math.random()*5,
					    10//+Math.random()*5
					    );
			RigidBody rigidBody = new RigidBody();
			double x = 0;//20*(i/countSq) - 10*countSq;
			double y = 0;//20*(i%countSq) - 10*countSq;
			double z = 10;
			
			rigidBody.setPosition(new Vector3d(x,y,z));
			//rigidBody.setRotation(new Vector3d(15,30,0));
			rigidBody.setShape(box);
			rigidBody.setMass(1);//Math.random()*5);
			sc.addChild(rigidBody);
			//rigidBody.setLinearVelocity(new Vector3d(Math.random()*2-1,Math.random()*2-1,Math.random()*2-1));
			//rigidBody.setAngularVelocity(new Vector3d(randomRotation(),randomRotation(),randomRotation()));
			
			rigidBody.applyForceAtPoint(new Vector3d(10,0,-10), new Point3d(-10,0,5));
		}
	}
	
	private double randomRotation() {
		return (Math.random()*4-2)*Math.PI;
	}
}
