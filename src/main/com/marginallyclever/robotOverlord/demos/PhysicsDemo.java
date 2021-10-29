package com.marginallyclever.robotOverlord.demos;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.Light;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.demos.demoAssets.Box;
import com.marginallyclever.robotOverlord.demos.demoAssets.Grid;
import com.marginallyclever.robotOverlord.demos.demoAssets.Sphere;
import com.marginallyclever.robotOverlord.physics.RigidBody;
import com.marginallyclever.robotOverlord.physics.RigidBodyBox;
import com.marginallyclever.robotOverlord.physics.RigidBodySphere;
import com.marginallyclever.robotOverlord.uiExposedTypes.MaterialEntity;

@SuppressWarnings("unused")
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
    	light.setPosition(new Vector3d(60,-60,160));
    	light.setDiffuse(1,1,1,1);
    	light.setSpecular(0.5f, 0.5f, 0.5f, 1.0f);
    	light.setAttenuationLinear(0.0014);
    	light.setAttenuationQuadratic(7*1e-6);
    	light.setDirectional(true);
    	
		// add some collision bounds
		// adjust grid
		Grid grid = new Grid();
		sc.addChild(grid);
		
		MakeRigidBody op;
		double x=0;
		//op = ()->makeOneSphere(sc);
		//testList(x,op);
		
		x+=5;
		op = ()->makeOneCube(sc);
		testList(x,op);
		
		//manyCubeDemo(sc);
	}
	
	interface MakeRigidBody {
		public RigidBody operation();
	}
	
	private void testList(double x,MakeRigidBody op) {
		oneFallingNoSpin     (op.operation(),new Vector3d(x,0,5));
		oneFallingWithSpin   (op.operation(),new Vector3d(x,5,5));
		oneFallingWithLinear (op.operation(),new Vector3d(x,10,5));
		//oneFallingWithBoth   (op.operation(),new Vector3d(x,15,5));
		//oneFalling45         (op.operation(),new Vector3d(x,20,5));
		//oneSitting           (op.operation(),new Vector3d(x,25,1));
		//oneSittingWithImpulse(op.operation(),new Vector3d(x,30,1));
		//oneSlidingNoFall     (op.operation(),new Vector3d(x,35,1));
	}

	private void oneSlidingNoFall(RigidBody body, Vector3d p) {
		body.setPosition(p);
		body.setLinearVelocity(new Vector3d(1,0,0));
	}

	private void oneSittingWithImpulse(RigidBody body, Vector3d p) {
		body.setPosition(p);
		double f= 100.0*body.getMass();
		body.applyForceAtPoint(new Vector3d(f,0,f),new Point3d(p.x,p.y,p.z-1));
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

	private void oneSitting(RigidBody body, Vector3d p) {
		body.setPosition(p);
	}
	
	private void oneFallingWithBoth(RigidBody body,Vector3d p) {
		body.setPosition(p);
		body.setLinearVelocity(new Vector3d(1,0,0));
		body.setAngularVelocity(new Vector3d(5,0,0));
		body.setPaused(true);
	}
	
	private void oneFallingWithLinear(RigidBody rigidBody,Vector3d p) {
		rigidBody.setPosition(p);
		rigidBody.setLinearVelocity(new Vector3d(1,0,0));
	}
	
	private void oneFallingWithSpin(RigidBody body,Vector3d p) {
		body.setPosition(p);
		body.setAngularVelocity(new Vector3d(5,0,0));
		body.setLinearVelocity(new Vector3d(0,0,0));
	}
	
	private void oneFalling45(RigidBody body,Vector3d p) {
		body.setPosition(p);
		body.setRotation(new Vector3d(45,0,0));
	}
	
	private void oneFallingNoSpin(RigidBody body,Vector3d p) {
		body.setPosition(p);
	}
	
	private RigidBody makeOneCube(Entity sc) {
		RigidBodyBox body = new RigidBodyBox();
		Box b = new Box();
		b.setSize(2, 2, 2);
		body.setShape(b);
		body.setMass(1);
		sc.addChild(body);
		return body;
	}
	
	private RigidBody makeOneSphere(Entity sc) {
		RigidBodySphere body = new RigidBodySphere();
		Sphere s = new Sphere();
		s.getMaterial().setTextureFilename("/grid.png");
		s.setDiameter(2);
		body.setShape(s);
		body.setMass(1);
		sc.addChild(body);
		return body;
	}
	
	private double randomRotation() {
		return (Math.random()*4-2)*Math.PI;
	}
}
