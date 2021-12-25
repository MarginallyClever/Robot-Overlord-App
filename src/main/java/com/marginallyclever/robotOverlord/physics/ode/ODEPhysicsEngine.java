package com.marginallyclever.robotOverlord.physics.ode;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import org.ode4j.math.DMatrix3;
import org.ode4j.math.DMatrix3C;
import org.ode4j.math.DQuaternion;
import org.ode4j.math.DQuaternionC;
import org.ode4j.math.DVector3;
import org.ode4j.math.DVector3C;
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
import org.ode4j.ode.DGeom.DNearCallback;

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
	private static DPlane groundPlane;
	private static DBox groundBox;
	
	public ODEPhysicsEngine() {
		super(ODEPhysicsEngine.class.getSimpleName());
		logger.debug("start ODEPhysicsEngine");
		
		if(OdeHelper.initODE2(0)==0) {
			logger.error("init failed.");
		}
		world = OdeHelper.createWorld();
		world.setGravity(0,0,-0.5);
		space = OdeHelper.createHashSpace(null);
		
		// create world		
		contactgroup = OdeHelper.createJointGroup();
		

		// environment
		groundPlane = OdeHelper.createPlane(space,0,0,1,0);

		groundBox = createBox(2,1.5,1);
		DMatrix3 R = new DMatrix3();
		OdeMath.dRFromAxisAndAngle (R,0,1,0,-0.15);
		groundBox.setPosition(2,0,-0.34);
		groundBox.setRotation(R);
	}
	
	public DGeom getGroundPlane() {
		return groundPlane;
	}
	
	public DGeom getGroundBox() {
		return groundBox;
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
		logger.debug("update");

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

		// only collide things with the groundPlane
		boolean g1 = (o1 == groundPlane || o1 == groundBox);
		boolean g2 = (o2 == groundPlane || o2 == groundBox);
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
	
	public static Matrix3d getMatrix3d(DMatrix3C in) {
		Matrix3d out = new Matrix3d();
		
		out.m00=in.get00();
		out.m01=in.get01();
		out.m02=in.get02();
		
		out.m10=in.get10();
		out.m11=in.get11();
		out.m12=in.get12();

		out.m20=in.get20();
		out.m21=in.get21();
		out.m22=in.get22();
		
		return out;
	}
	
	public static DMatrix3 getDMatrix3(Matrix3d in) {
		DMatrix3 out = new DMatrix3();
		
		out.set00(in.m00);
		out.set01(in.m01);
		out.set02(in.m02);
           
		out.set10(in.m10);
		out.set11(in.m11);
		out.set12(in.m12);
           
		out.set20(in.m20);
		out.set21(in.m21);
		out.set22(in.m22);
		
		return out;
	}


	public static Vector3d getVector3d(DVector3C v) {
		return new Vector3d(v.get0(),v.get1(),v.get2());
	}
	
	public static DVector3 getDVector3(Vector3d v) {
		return new DVector3(v.x,v.y,v.z);
	}


	public static Quat4d getQuat4d(DQuaternionC q) {
		return new Quat4d(q.get1(),q.get2(),q.get3(),q.get0());
	}
	
	public static DQuaternion getDQuaternion(Quat4d q) {
		return new DQuaternion(q.w,q.x,q.y,q.z);
	}

	public static Matrix4d getMatrix4d(DGeom geom) {
		Matrix4d m = new Matrix4d();
		m.set(ODEPhysicsEngine.getQuat4d(geom.getQuaternion()));
		m.setTranslation(ODEPhysicsEngine.getVector3d(geom.getPosition()));
		return m;
	}
}
