package com.marginallyclever.robotoverlord.demos;

import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.components.LightComponent;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.systems.physics.ode.ODEPhysicsComponent;
import com.marginallyclever.robotoverlord.systems.physics.ode.ODEPhysicsEngine;
import org.ode4j.math.DMatrix3;
import org.ode4j.math.DQuaternion;
import org.ode4j.math.DVector3C;
import org.ode4j.ode.*;

import javax.vecmath.Vector3d;

/**
 * See https://github.com/tzaeschke/ode4j/blob/master/demo/src/main/java/org/ode4j/demo/DemoBuggy.java
 * @author Dan Royer
 *
 */
public class DemoODEPhysics implements Demo {
	private static final double CHASIS_LENGTH = 0.7;
	private static final double CHASIS_WIDTH = 0.5;
	private static final double CHASIS_HEIGHT = 0.2;
	private static final float WHEEL_RADIUS = 0.18f;
	private static final double CHASIS_Z_AT_START = 0.5;
	private static final double CHASIS_MASS = 1.0;
	private static final double WHEEL_MASS = 0.2;

	private ODEPhysicsEngine engine;

	// dynamics and collision objects (chassis, 3 wheels, environment)
	private DBody chassisBody;
	private final DBody [] wheelBodies = new DBody[3];
	private final DHinge2Joint [] joint = new DHinge2Joint[3];	// joint[0] is the front wheel
	private DSpace car_space;
	private DBox box;
	private final DSphere [] sphere = new DSphere[3];
	private DPlane ground;
	private DBox ramp;


	@Override
	public String getName() {
		return "ODE Physics";
	}
	
	@Override
	public void execute(EntityManager entityManager) {
		// adjust default camera
		CameraComponent camera = entityManager.getCamera();
		PoseComponent pose = camera.getEntity().getComponent(PoseComponent.class);
		pose.setPosition(new Vector3d(40/4f,-91/4f,106/4f));
		camera.lookAt(new Vector3d(0,0,0));
		camera.setOrbitDistance(20);
		
		// add some lights
		LightComponent light;
		Entity light0 = new Entity();
		entityManager.addEntityToParent(light0,camera.getEntity());
		light0.addComponent(pose = new PoseComponent());
		light0.addComponent(light = new LightComponent());
    	pose.setPosition(new Vector3d(60,-60,160));
    	light.setDiffuse(1,1,1,1);
    	light.setSpecular(0.5f, 0.5f, 0.5f, 1.0f);
    	light.setAttenuationLinear(0.0014);
    	light.setAttenuationQuadratic(7*1e-6);
    	light.setDirectional(true);
    	
    	// start physics
		engine = new ODEPhysicsEngine();
		
		engine.setCallback(this::nearCallback);

		// environment
		ground = OdeHelper.createPlane(engine.getSpace(),0,0,1,0);
		ramp = engine.createBox(2,1.5,1);
		DMatrix3 R = new DMatrix3();
		OdeMath.dRFromAxisAndAngle (R,0,1,0,-0.15);
		ramp.setPosition(2,0,-0.34);
		ramp.setRotation(R);
		entityManager.addEntityToParent(createEntity(new ODEPhysicsComponent(ramp)), entityManager.getRoot());
		entityManager.addEntityToParent(createEntity(new ODEPhysicsComponent(ground)), entityManager.getRoot());
		
		DMass mass = OdeHelper.createMass();
		
		// chassis
		chassisBody = engine.createBody();
		mass.setBox(1, CHASIS_LENGTH, CHASIS_WIDTH, CHASIS_HEIGHT);
		mass.adjust(CHASIS_MASS);
		chassisBody.setMass(mass);
		box = OdeHelper.createBox(null,CHASIS_LENGTH,CHASIS_WIDTH,CHASIS_HEIGHT);
		box.setBody(chassisBody);
		box.setPosition(0, 0, CHASIS_Z_AT_START);
		
		entityManager.addEntityToParent(createEntity(new ODEPhysicsComponent(box)), entityManager.getRoot());

		// wheels
		int i;
		for (i=0; i<sphere.length; i++) {
			wheelBodies[i] = engine.createBody();
			mass = OdeHelper.createMass();
			mass.setSphere(1,WHEEL_RADIUS);
			mass.adjust(WHEEL_MASS);
			wheelBodies[i].setMass(mass);
			sphere[i] = OdeHelper.createSphere(null,WHEEL_RADIUS);
			sphere[i].setBody(wheelBodies[i]);
			DQuaternion q = new DQuaternion();
			OdeMath.dQFromAxisAndAngle (q,1,0,0,Math.PI*0.5);
			sphere[i].setQuaternion(q);
			entityManager.addEntityToParent(createEntity(new ODEPhysicsComponent(sphere[i])), entityManager.getRoot());
		}
		wheelBodies[0].setPosition(0.5*CHASIS_LENGTH,0,CHASIS_Z_AT_START-CHASIS_HEIGHT*0.5);
		wheelBodies[1].setPosition(-0.5*CHASIS_LENGTH, CHASIS_WIDTH*0.5,CHASIS_Z_AT_START-CHASIS_HEIGHT*0.5);
		wheelBodies[2].setPosition(-0.5*CHASIS_LENGTH,-CHASIS_WIDTH*0.5,CHASIS_Z_AT_START-CHASIS_HEIGHT*0.5);
				
		// front and back wheel hinges
		for (i=0; i<joint.length; i++) {
			joint[i] = engine.createHinge2Joint();
			joint[i].attach(chassisBody,wheelBodies[i]);
			DVector3C a = wheelBodies[i].getPosition();
			DHinge2Joint h2 = joint[i];
			h2.setAnchor (a);
			h2.setAxis1 (0,0,1);
			h2.setAxis2 (0,1,0);
		}

		// set joint suspension
		for (i=0; i<joint.length; i++) {
			joint[i].setParamSuspensionERP (0.4);
			joint[i].setParamSuspensionCFM (0.8);
		}

		// lock back wheels along the steering axis
		for (i=1; i<joint.length; i++) {
			// set stops to make sure wheels always stay in alignment
			joint[i].setParamLoStop (0);
			joint[i].setParamHiStop (0);
			// the following alternative method is no good as the wheels may get out
			// of alignment:
			//   dJointSetHinge2Param (joint[i],dParamVel,0);
			//   dJointSetHinge2Param (joint[i],dParamFMax,dInfinity);
		}

		joint[0].setParamVel (0.025);
		joint[0].setParamVel2 (-2.5);
		joint[0].setParamFMax2 (0.1);
		joint[0].setParamFMax (0.2);
		joint[0].setParamLoStop (-0.75);
		joint[0].setParamHiStop (0.75);
		joint[0].setParamFudgeFactor (0.1);
		
		// create car space and add it to the top level space
		car_space = engine.createSimpleSpace();
		car_space.setCleanup(false);
		car_space.add(box);
		car_space.add(sphere[0]);
		car_space.add(sphere[1]);
		car_space.add(sphere[2]);
		
		// all objects need a draw hook and an update hook via PhysicsEntity
	}
	
	@SuppressWarnings("unused")
	private void endDemo() {
		box.destroy();
		sphere[0].destroy();
		sphere[1].destroy();
		sphere[2].destroy();
	}

	private void nearCallback(Object data, DGeom o1, DGeom o2) {
		int i,n;

		// only collide things with the ground
		boolean g1 = (o1 == ground || o1 == ramp);
		boolean g2 = (o2 == ground || o2 == ramp);
		if (!(g1 ^ g2)) return;

		final int N = 10;
		//dContact contact[N];
		DContactBuffer contacts = new DContactBuffer(N);
		n = OdeHelper.collide (o1,o2,N,contacts.getGeomBuffer());
		if (n > 0) {
			for (i=0; i<n; i++) {
				DContact contact = contacts.get(i);
				contact.surface.mode = OdeConstants.dContactSlip1 
									| OdeConstants.dContactSlip2 
									| OdeConstants.dContactSoftERP
									| OdeConstants.dContactSoftCFM
									| OdeConstants.dContactApprox1;
				contact.surface.mu = OdeConstants.dInfinity;
				contact.surface.slip1 = 0.1;
				contact.surface.slip2 = 0.1;
				contact.surface.soft_erp = 0.5;
				contact.surface.soft_cfm = 0.3;
				DJoint c = OdeHelper.createContactJoint (engine.getWorld(),engine.getContactGroup(),contact);
				c.attach(
						contact.geom.g1.getBody(),
						contact.geom.g2.getBody());
			}
		}
	}

	private Entity createEntity(ODEPhysicsComponent odePhysicsComponent) {
		Entity e = new Entity();
		e.addComponent(new PoseComponent());
		e.addComponent(new MaterialComponent());
		e.addComponent(odePhysicsComponent);
		return e;
	}
}
