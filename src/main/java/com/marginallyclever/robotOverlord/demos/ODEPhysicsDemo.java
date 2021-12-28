package com.marginallyclever.robotOverlord.demos;

import javax.vecmath.Vector3d;

import org.ode4j.math.DQuaternion;
import org.ode4j.math.DVector3C;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DBox;
import org.ode4j.ode.DHinge2Joint;
import org.ode4j.ode.DMass;
import org.ode4j.ode.DSpace;
import org.ode4j.ode.DSphere;
import org.ode4j.ode.OdeHelper;
import org.ode4j.ode.OdeMath;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.physics.ode.ODEPhysicsEngine;
import com.marginallyclever.robotOverlord.physics.ode.ODEPhysicsEntity;
import com.marginallyclever.robotOverlord.sceneElements.Light;

/**
 * See https://github.com/tzaeschke/ode4j/blob/master/demo/src/main/java/org/ode4j/demo/DemoBuggy.java
 * @author Dan Royer
 *
 */
public class ODEPhysicsDemo implements Demo {
	private final double CHASIS_LENGTH = 0.7;
	private final double CHASIS_WIDTH = 0.5;
	private final double CHASIS_HEIGHT = 0.2;
	private final float WHEEL_RADIUS = 0.18f;
	private final double CHASIS_Z_AT_START = 0.5;
	private final double CHASIS_MASS = 1.0;
	private final double WHEEL_MASS = 0.2;

	private ODEPhysicsEngine engine;

	// dynamics and collision objects (chassis, 3 wheels, environment)
	private static DBody [] body = new DBody[4];
	private static DHinge2Joint [] joint = new DHinge2Joint[3];	// joint[0] is the front wheel
	private static DSpace car_space;
	private static DBox [] box = new DBox[1];
	private static DSphere [] sphere = new DSphere[3];
	
	@Override
	public void execute(RobotOverlord ro) {
		// add some lights
    	Light light = new Light();

    	ro.getScene().addChild(light);
		light.setName("Light");
    	light.setPosition(new Vector3d(60,-60,160));
    	light.setDiffuse(1,1,1,1);
    	light.setSpecular(0.5f, 0.5f, 0.5f, 1.0f);
    	light.setAttenuationLinear(0.0014);
    	light.setAttenuationQuadratic(7*1e-6);
    	light.setDirectional(true);
    	
    	// start physics
		engine = new ODEPhysicsEngine();
		ro.addChild(engine);
		ro.getScene().addChild(new ODEPhysicsEntity(engine.getGroundBox()));
		ro.getScene().addChild(new ODEPhysicsEntity(engine.getGroundPlane()));
		
		DMass mass = OdeHelper.createMass();
		
		// chassis
		body[0] = engine.createBody();
		body[0].setPosition(0, 0, CHASIS_Z_AT_START);
		mass.setBox(1, CHASIS_LENGTH, CHASIS_WIDTH, CHASIS_HEIGHT);
		mass.adjust(CHASIS_MASS);
		body[0].setMass(mass);
		box[0] = OdeHelper.createBox(null,CHASIS_LENGTH,CHASIS_WIDTH,CHASIS_HEIGHT);
		box[0].setBody(body[0]);
		
		ro.getScene().addChild(new ODEPhysicsEntity(box[0]));

		// wheels
		int i;
		for (i=1; i<=sphere.length; i++) {
			body[i] = engine.createBody();
			DQuaternion q = new DQuaternion();
			OdeMath.dQFromAxisAndAngle (q,1,0,0,Math.PI*0.5);
			body[i].setQuaternion(q);
			mass.setSphere(1,WHEEL_RADIUS);
			mass.adjust(WHEEL_MASS);
			body[i].setMass(mass);
			sphere[i-1] = OdeHelper.createSphere(null,WHEEL_RADIUS);
			sphere[i-1].setBody(body[i]);
			ro.getScene().addChild(new ODEPhysicsEntity(sphere[i-1]));
		}
		body[1].setPosition(0.5*CHASIS_LENGTH,0,CHASIS_Z_AT_START-CHASIS_HEIGHT*0.5);
		body[2].setPosition(-0.5*CHASIS_LENGTH, CHASIS_WIDTH*0.5,CHASIS_Z_AT_START-CHASIS_HEIGHT*0.5);
		body[3].setPosition(-0.5*CHASIS_LENGTH,-CHASIS_WIDTH*0.5,CHASIS_Z_AT_START-CHASIS_HEIGHT*0.5);
				
		// front and back wheel hinges
		for (i=0; i<joint.length; i++) {
			joint[i] = engine.createHinge2Joint();
			joint[i].attach(body[0],body[i+1]);
			final DVector3C a = body[i+1].getPosition();
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

		joint[0].setParamVel (0.05);
		joint[0].setParamVel2 (-1.5);
		joint[0].setParamFMax2 (0.1);
		joint[0].setParamFMax (0.2);
		joint[0].setParamLoStop (-0.75);
		joint[0].setParamHiStop (0.75);
		joint[0].setParamFudgeFactor (0.1);
		
		// create car space and add it to the top level space
		car_space = engine.createSimpleSpace();
		car_space.setCleanup(false);
		car_space.add (box[0]);
		car_space.add (sphere[0]);
		car_space.add (sphere[1]);
		car_space.add (sphere[2]);
		
		// all objects need a draw hook and an update hook via PhysicsEntity
	}
	
	private void endDemo() {
		box[0].destroy();
		sphere[0].destroy();
		sphere[1].destroy();
		sphere[2].destroy();
	}

	@Override
	public String getName() {
		return "ODE Physics Demo";
	}
}
