package com.marginallyclever.robotOverlord.physics.ode;

import org.ode4j.math.DMatrix3;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DBox;
import org.ode4j.ode.DContact;
import org.ode4j.ode.DContactBuffer;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DGeom.DNearCallback;
import org.ode4j.ode.DHinge2Joint;
import org.ode4j.ode.DJoint;
import org.ode4j.ode.DJointGroup;
import org.ode4j.ode.DPlane;
import org.ode4j.ode.DSpace;
import org.ode4j.ode.DWorld;
import org.ode4j.ode.OdeConstants;
import org.ode4j.ode.OdeHelper;
import org.ode4j.ode.OdeMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.Entity;

public class ODEPhysicsEngine extends Entity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(ODEPhysicsEngine.class);
	
	private static DWorld world;
	private static DSpace space;
	private static DJointGroup contactgroup;
	private DNearCallback callback;
	
	public ODEPhysicsEngine() {
		super(ODEPhysicsEngine.class.getSimpleName());
		logger.debug("start ODEPhysicsEngine");
		
		if(OdeHelper.initODE2(0)==0) {
			logger.error("init failed.");
		}
		
		world = OdeHelper.createWorld();
		world.setGravity(0,0,-0.5);
		space = OdeHelper.createHashSpace(null);
		contactgroup = OdeHelper.createJointGroup();
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
		
		if(callback!=null) space.collide(null,callback);
		world.step(dt);

		// remove all contact joints
		contactgroup.empty();
	}
	
	public DSpace getSpace() {
		return space;
	}

	public DWorld getWorld() {
		return world;
	}

	public DJointGroup getContactGroup() {
		return contactgroup;
	}
	
	public void setCallback(DNearCallback arg0) {
		callback=arg0;
	}
}
