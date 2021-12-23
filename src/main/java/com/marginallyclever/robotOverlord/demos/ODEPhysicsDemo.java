package com.marginallyclever.robotOverlord.demos;

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

/**
 * See https://github.com/tzaeschke/ode4j/blob/master/demo/src/main/java/org/ode4j/demo/DemoBuggy.java
 * @author Dan Royer
 *
 */
public class ODEPhysicsDemo implements Demo {
	// some constants
	public static int DS_VERSION = 0x0002;

	private final double LENGTH = 0.7;	// chassis length
	private final double WIDTH = 0.5;	// chassis width
	private final double HEIGHT = 0.2;	// chassis height
	private final float RADIUS = 0.18f;	// wheel radius
	private final double STARTZ = 0.5;	// starting height of chassis
	private final double CMASS = 1;		// chassis mass
	private final double WMASS = 0.2;	// wheel mass

	// dynamics and collision objects (chassis, 3 wheels, environment)

	ODEPhysicsEngine engine;
	
	private static DBody [] body = new DBody[4];
	private static DHinge2Joint [] joint = new DHinge2Joint[3];	// joint[0] is the front wheel
	private static DSpace car_space;
	private static DBox [] box = new DBox[1];
	private static DSphere [] sphere = new DSphere[3];
	
	@Override
	public void execute(RobotOverlord ro) {
		engine = new ODEPhysicsEngine();
		ro.addChild(engine);
		
		DMass mass = OdeHelper.createMass();
		
		// chassis body
		body[0] = engine.createBody();
		body[0].setPosition(0, 0, STARTZ);
		mass.setBox(1, LENGTH, WIDTH, HEIGHT);
		mass.adjust(CMASS);
		body[0].setMass(mass);
		box[0] = OdeHelper.createBox(null,LENGTH,WIDTH,HEIGHT);
		box[0].setBody(body[0]);

		// wheel bodies
		int i;
		for (i=1; i<=3; i++) {
			body[i] = engine.createBody();
			DQuaternion q = new DQuaternion();
			OdeMath.dQFromAxisAndAngle (q,1,0,0,Math.PI*0.5);
			body[i].setQuaternion(q);
			mass.setSphere(1,RADIUS);
			mass.adjust(WMASS);
			body[i].setMass(mass);
			sphere[i-1] = OdeHelper.createSphere (null,RADIUS);
			sphere[i-1].setBody(body[i]);
		}
		body[1].setPosition(0.5*LENGTH,0,STARTZ-HEIGHT*0.5);
		body[2].setPosition(-0.5*LENGTH, WIDTH*0.5,STARTZ-HEIGHT*0.5);
		body[3].setPosition(-0.5*LENGTH,-WIDTH*0.5,STARTZ-HEIGHT*0.5);

		// front and back wheel hinges
		for (i=0; i<3; i++) {
			joint[i] = engine.createHinge2Joint();
			joint[i].attach(body[0],body[i+1]);
			final DVector3C a = body[i+1].getPosition();
			DHinge2Joint h2 = joint[i];
			h2.setAnchor (a);
			h2.setAxis1 (0,0,1);
			h2.setAxis2 (0,1,0);
		}

		// set joint suspension
		for (i=0; i<3; i++) {
			joint[i].setParamSuspensionERP (0.4);
			joint[i].setParamSuspensionCFM (0.8);
		}

		// lock back wheels along the steering axis
		for (i=1; i<3; i++) {
			// set stops to make sure wheels always stay in alignment
			joint[i].setParamLoStop (0);
			joint[i].setParamHiStop (0);
			// the following alternative method is no good as the wheels may get out
			// of alignment:
			//   dJointSetHinge2Param (joint[i],dParamVel,0);
			//   dJointSetHinge2Param (joint[i],dParamFMax,dInfinity);
		}

		// create car space and add it to the top level space
		car_space = engine.createSimpleSpace();
		car_space.setCleanup(false);
		car_space.add (box[0]);
		car_space.add (sphere[0]);
		car_space.add (sphere[1]);
		car_space.add (sphere[2]);

		box[0].destroy();
		sphere[0].destroy();
		sphere[1].destroy();
		sphere[2].destroy();
		
		// all objects need a draw hook to RobotOverlord.render()
		// all objects need an update hook to RobotOverlord.update()
	}

	@Override
	public String getName() {
		return "ODE Physics Demo";
	}
}
