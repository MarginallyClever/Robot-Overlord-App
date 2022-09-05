package com.marginallyclever.robotOverlord.demos;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.components.CameraComponent;
import com.marginallyclever.robotOverlord.components.LightComponent;
import com.marginallyclever.robotOverlord.components.PoseComponent;
import com.marginallyclever.robotOverlord.physics.original.RigidBody;
import com.marginallyclever.robotOverlord.physics.original.RigidBodyBox;
import com.marginallyclever.robotOverlord.physics.original.RigidBodySphere;
import com.marginallyclever.robotOverlord.components.sceneElements.BoxEntity;
import com.marginallyclever.robotOverlord.components.sceneElements.GridEntity;
import com.marginallyclever.robotOverlord.components.sceneElements.SphereEntity;
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
		CameraComponent camera = ro.getCamera();
		PoseComponent pose = camera.getEntity().getComponent(PoseComponent.class);
		pose.setPosition(new Vector3d(40/4,-91/4,106/4));
		camera.lookAt(new Vector3d(0,0,0));
		camera.setZoom(30);
		
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
		
		MakeRigidBody op;
		double x=0;
		op = ()->makeOneSphere(sc);
		testList(x,op);
		
		x+=5;
		//op = ()->makeOneCube(sc);
		//testList(x,op);
		
		//manyCubeDemo(sc);
	}
	
	interface MakeRigidBody {
		public RigidBody operation();
	}
	
	private void testList(double x,MakeRigidBody op) {
		double y=0;
		double space=5;
		oneFallingNoSpin     (op.operation(),new Vector3d(x,y,5));  y+=space;
		oneFallingWithSpin   (op.operation(),new Vector3d(x,y,5));  y+=space;
		oneFallingWithLinear (op.operation(),new Vector3d(x,y,5));  y+=space;
		oneFallingWithBoth   (op.operation(),new Vector3d(x,y,5));  y+=space;
		oneFalling45         (op.operation(),new Vector3d(x,y,5));  y+=space;
		oneSitting           (op.operation(),new Vector3d(x,y,1));  y+=space;
		oneSittingWithImpulse(op.operation(),new Vector3d(x,y,1));  y+=space;
		oneSlidingNoFall     (op.operation(),new Vector3d(x,y,1));  y+=space;
		oneHitsAtCenter      (op,new Vector3d(x,y,10));  y+=space;
		oneHitsOffCenter     (op,new Vector3d(x,y,10));  y+=space;
	}

	private void oneHitsOffCenter(MakeRigidBody op, Vector3d p) {
		RigidBody a = op.operation();
		RigidBody b = op.operation();
		a.setPosition(p);
		a.setApplyGravity(false);
		a.applyForceAtPoint(new Vector3d(5,0,0), new Point3d(p));
		
		p.x+=5;
		p.y+=0.5;
		b.setPosition(p);
		b.setApplyGravity(false);
	}
	
	private void oneHitsAtCenter(MakeRigidBody op, Vector3d p) {
		RigidBody a = op.operation();
		RigidBody b = op.operation();
		a.setPosition(p);
		a.setApplyGravity(false);
		a.applyForceAtPoint(new Vector3d(5,0,0), new Point3d(p));
		
		p.x+=5;
		b.setPosition(p);
		b.setApplyGravity(false);
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
			BoxEntity box = new BoxEntity();
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
		BoxEntity b = new BoxEntity();
		b.setSize(2, 2, 2);
		//b.getMaterial().setTextureFilename("/grid.png");
		body.setPauseOnCollision(false);
		body.setShape(b);
		body.setMass(1);
		sc.addChild(body);
		return body;
	}
	
	private RigidBody makeOneSphere(Entity sc) {
		RigidBodySphere body = new RigidBodySphere();
		SphereEntity s = new SphereEntity();
		s.setDiameter(2);
		s.getMaterial().setTextureFilename("/grid.png");
		body.setPauseOnCollision(false);
		body.setShape(s);
		body.setMass(1);
		sc.addChild(body);
		return body;
	}
	
	private double randomRotation() {
		return (Math.random()*4-2)*Math.PI;
	}
}
