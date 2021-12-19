package com.marginallyclever.robotOverlord.physics.ode;

import org.ode4j.math.DMatrix3;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DBox;
import org.ode4j.ode.DContact;
import org.ode4j.ode.DContactBuffer;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DHinge2Joint;
import org.ode4j.ode.DJoint;
import org.ode4j.ode.DJointGroup;
import org.ode4j.ode.DPlane;
import org.ode4j.ode.DSpace;
import org.ode4j.ode.DWorld;
import org.ode4j.ode.OdeConstants;
import org.ode4j.ode.OdeHelper;
import org.ode4j.ode.OdeMath;
import org.ode4j.ode.DGeom.DNearCallback;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.Entity;

public class ODEPhysicsEngine extends Entity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static DWorld world;
	private static DSpace space;
	private static DJointGroup contactgroup;
	private static DPlane ground;
	private static DBox ground_box;
	
	public ODEPhysicsEngine() {
		super(ODEPhysicsEngine.class.getSimpleName());
		
		OdeHelper.initODE2(0);
		world = OdeHelper.createWorld();
		world.setGravity (0,0,-0.5);
		space = OdeHelper.createHashSpace(null);
		
		// create world		
		contactgroup = OdeHelper.createJointGroup();
		ground = OdeHelper.createPlane(space,0,0,1,0);

		// environment
		ground_box = createBox(2,1.5,1);
		DMatrix3 R = new DMatrix3();
		OdeMath.dRFromAxisAndAngle (R,0,1,0,-0.15);
		ground_box.setPosition(2,0,-0.34);
		ground_box.setRotation(R);
	}
	
	public DBody createBody() {
		return OdeHelper.createBody(world);
	}
	
	public void onFinish() {
		contactgroup.destroy();
		space.destroy();
		world.destroy();
		OdeHelper.closeODE();
	}

	public DHinge2Joint createHinge2Joint() {
		return OdeHelper.createHinge2Joint(world,null);
	}

	public DSpace createSimpleSpace() {
		return OdeHelper.createSimpleSpace(space);
	}

	public DBox createBox(double x, double y, double z) {
		return OdeHelper.createBox(space,x,y,z);
	}
	
	@Override
	public void update(double dt) {
		super.update(dt);
		System.out.println("update");

		space.collide(null,new DNearCallback() {
			@Override
			public void call(Object data, DGeom o1, DGeom o2) {
				nearCallback(data, o1, o2);
			}
		});
		world.step(dt);

		// remove all contact joints
		contactgroup.empty();
	}

	private void nearCallback(Object data, DGeom o1, DGeom o2) {
		int i,n;

		// only collide things with the ground
		boolean g1 = (o1 == ground || o1 == ground_box);
		boolean g2 = (o2 == ground || o2 == ground_box);
		if (!(g1 ^ g2)) return;

		final int N = 10;
		//dContact contact[N];
		DContactBuffer contacts = new DContactBuffer(N);
//		n = dCollide (o1,o2,N,&contact[0].geom,sizeof(dContact));
		n = OdeHelper.collide (o1,o2,N,contacts.getGeomBuffer());//[0].geom,sizeof(dContact));
		if (n > 0) {
			for (i=0; i<n; i++) {
				DContact contact = contacts.get(i);
				contact.surface.mode = OdeConstants.dContactSlip1 | OdeConstants.dContactSlip2 |
				OdeConstants.dContactSoftERP | OdeConstants.dContactSoftCFM | OdeConstants.dContactApprox1;
				contact.surface.mu = OdeConstants.dInfinity;
				contact.surface.slip1 = 0.1;
				contact.surface.slip2 = 0.1;
				contact.surface.soft_erp = 0.5;
				contact.surface.soft_cfm = 0.3;
				DJoint c = OdeHelper.createContactJoint (world,contactgroup,contact);
				c.attach(
						contact.geom.g1.getBody(),
						contact.geom.g2.getBody());
			}
		}
	}
	
	@Override
	public void render(GL2 gl2) {
		super.render(gl2);
		
		System.out.println("rendering");
	}
}
