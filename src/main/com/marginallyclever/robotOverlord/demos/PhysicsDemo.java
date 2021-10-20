package com.marginallyclever.robotOverlord.demos;

import javax.vecmath.Vector3d;

import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.Light;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.demoAssets.Box;
import com.marginallyclever.robotOverlord.demoAssets.Grid;
import com.marginallyclever.robotOverlord.demoAssets.Sphere;
import com.marginallyclever.robotOverlord.physics.RigidBodyBox;
import com.marginallyclever.robotOverlord.physics.RigidBodySphere;
import com.marginallyclever.robotOverlord.uiExposedTypes.MaterialEntity;

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

		//oneSphereFallingNoSpin(sc,new Vector3d(0,0,5));
		oneCubeFalling45(sc,new Vector3d(0,0,5));
		oneCubeFallingWithSpin(sc,new Vector3d(0,5,5));
		oneCubeFallingNoSpin(sc,new Vector3d(0,10,5));
		oneCubeFallingWithLinear(sc,new Vector3d(5,0,5));
		oneCubeFallingWithBoth(sc,new Vector3d(10,0,5));
		//manyCubeDemo(sc);
	}
	
	private void oneSphereFallingNoSpin(Entity sc, Vector3d p) {
		RigidBodySphere rigidBody = makeOneSphere(sc);
		rigidBody.setPosition(p);
		rigidBody.setLinearVelocity(new Vector3d(0,0,0));
		rigidBody.setAngularVelocity(new Vector3d(0,0,0));
	}

	private void manyCubeDemo(Entity sc) {
		int count=10;
		int countSq = (int)Math.sqrt(count);
		for(int i=0;i<count;++i) {
			Box box = new Box();
			box.setSize(1+Math.random()*5,
					    1+Math.random()*5,
					    1+Math.random()*5
					    );
			RigidBodyBox rigidBody = new RigidBodyBox();
			double x = 20*(i/countSq) - 10*countSq;
			double y = 20*(i%countSq) - 10*countSq;
			double z = 10;
			
			double mass = box.getWidth() * box.getHeight() * box.getDepth();
			MaterialEntity m = new MaterialEntity();
			double massScaled = mass/16.0;
			m.setDiffuseColor(massScaled,massScaled,massScaled, 1.0);
			box.setMaterial(m);
			
			rigidBody.setShape(box);
			rigidBody.setMass(mass);
			rigidBody.setPosition(new Vector3d(x,y,z));
			rigidBody.setLinearVelocity(new Vector3d(Math.random()*10-1,0,0));//Math.random()*2-1,Math.random()*2-1));
			rigidBody.setAngularVelocity(new Vector3d(randomRotation(),randomRotation(),randomRotation()));
			//rigidBody.applyForceAtPoint(new Vector3d(10,0,40), new Point3d(-10,0,10));
			//rigidBody.applyForceAtPoint(new Vector3d(-1,0,-4), new Point3d(-10,0,10));
			sc.addChild(rigidBody);
		}
	}
	
	private void oneCubeFallingWithBoth(Entity sc,Vector3d p) {
		RigidBodyBox rigidBody = makeOneCube(sc);
		rigidBody.setPosition(p);
		rigidBody.setLinearVelocity(new Vector3d(1,0,0));
		rigidBody.setAngularVelocity(new Vector3d(5,0,0));
	}
	
	private void oneCubeFallingWithLinear(Entity sc,Vector3d p) {
		RigidBodyBox rigidBody = makeOneCube(sc);
		rigidBody.setPosition(p);
		rigidBody.setLinearVelocity(new Vector3d(1,0,0));
	}
	
	private void oneCubeFallingWithSpin(Entity sc,Vector3d p) {
		RigidBodyBox rigidBody = makeOneCube(sc);
		rigidBody.setPosition(p);
		rigidBody.setAngularVelocity(new Vector3d(5,0,0));
		rigidBody.setLinearVelocity(new Vector3d(0,0,0));
	}
	
	private void oneCubeFalling45(Entity sc,Vector3d p) {
		RigidBodyBox rigidBody = makeOneCube(sc);
		rigidBody.setPosition(p);
		rigidBody.setRotation(new Vector3d(45,0,0));
	}
	
	private void oneCubeFallingNoSpin(Entity sc,Vector3d p) {
		RigidBodyBox rigidBody = makeOneCube(sc);
		rigidBody.setPosition(p);
	}
	
	private RigidBodyBox makeOneCube(Entity sc) {
		Box box = new Box();
		box.setSize(1,1,1);
		RigidBodyBox rigidBody = new RigidBodyBox();
		rigidBody.setShape(box);
		rigidBody.setMass(1);
		sc.addChild(rigidBody);
		return rigidBody;
	}
	
	private RigidBodySphere makeOneSphere(Entity sc) {
		Sphere box = new Sphere();
		box.setDiameter(10);
		RigidBodySphere rigidBody = new RigidBodySphere();
		rigidBody.setShape(box);
		rigidBody.setMass(1);
		sc.addChild(rigidBody);
		return rigidBody;
	}
	
	private double randomRotation() {
		return (Math.random()*4-2)*Math.PI;
	}
}
