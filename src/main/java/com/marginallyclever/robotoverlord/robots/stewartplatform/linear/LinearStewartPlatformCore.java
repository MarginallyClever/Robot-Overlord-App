package com.marginallyclever.robotoverlord.robots.stewartplatform.linear;


import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.*;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.entities.PoseEntity;
import com.marginallyclever.robotoverlord.parameters.BooleanEntity;
import com.marginallyclever.robotoverlord.parameters.DoubleEntity;
import com.marginallyclever.robotoverlord.parameters.RemoteEntity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.beans.PropertyChangeEvent;

@Deprecated
public class LinearStewartPlatformCore extends PoseEntity {
	private static final Logger logger = LoggerFactory.getLogger(LinearStewartPlatformCore.class);
	
	public final DoubleEntity SLIDE_TRAVEL = new DoubleEntity("SLIDE_TRAVEL", 10.0);  // cm
	public final DoubleEntity ARM_LENGTH = new DoubleEntity("ARM_LENGTH",15.0362);  // cm
	public final DoubleEntity BASE_X = new DoubleEntity("BASE_X",6.0968);  // cm
	public final DoubleEntity BASE_Y = new DoubleEntity("BASE_Y",1.6000);  // cm
	public final DoubleEntity BASE_Z = new DoubleEntity("BASE_Z",7.8383);  // cm
	public final DoubleEntity EE_X = new DoubleEntity("EE_X", 3.6742);  // cm
	public final DoubleEntity EE_Y = new DoubleEntity("EE_Y", 0.7500);  // cm
	public final DoubleEntity EE_Z = new DoubleEntity("EE_Z",-2.4000);  // cm

	private final BooleanEntity debugEEPoints = new BooleanEntity("debugEEPoints", false);
	private final BooleanEntity debugSlides = new BooleanEntity("debugSlides", true);
	private final BooleanEntity debugArms = new BooleanEntity("debugArms", false);
	
	private final PoseEntity ee = new PoseEntity("ee");

	protected LinearStewartPlatformArm[] arms = {
			new LinearStewartPlatformArm(),
			new LinearStewartPlatformArm(),
			new LinearStewartPlatformArm(),
			new LinearStewartPlatformArm(),
			new LinearStewartPlatformArm(),
			new LinearStewartPlatformArm()
	};

	private final RemoteEntity connection = new RemoteEntity();
	private final DoubleEntity velocity = new DoubleEntity("velocity",5);
	private final DoubleEntity acceleration = new DoubleEntity("acceleration",200);

	protected final MaterialComponent material = new MaterialComponent();

	public LinearStewartPlatformCore() {
		this("Linear Stewart Platform");
	}

	public LinearStewartPlatformCore(String name) {
		super(name);
		addEntity(ee);

		connection.addPropertyChangeListener(this);

		// apply some default materials.
		material.setAmbientColor(0, 0, 0, 1);
		material.setDiffuseColor(1f,1f,1f,1);
		material.setEmissionColor(0, 0, 0, 1);
		material.setLit(true);
		material.setShininess(0);

		calculateEndEffectorPointsOneTime();
		calculateBasePointsOneTime();
		
		ee.setPosition(new Vector3d(0,0,BASE_Z.get()+Math.abs(EE_Z.get())+ARM_LENGTH.get()));
	}

	/**
	 * Calculate end effector points - the center of each magnetic ball at the end effector
	 * end effector points are ordered counter clockwise, looking down on the machine.
	 *       1
	 *  2       0 <-- first
	 *      x     <-- center
	 *  3       5 <-- last
	 *       4
	 */
	protected void calculateEndEffectorPointsOneTime() {
		Vector3d vx = new Vector3d();
		Vector3d vy = new Vector3d();
		Vector3d tx = new Vector3d();
		Vector3d ty = new Vector3d();

		for(int i=0;i<arms.length;++i) {
			double r = Math.toRadians(60.0+120.0*i/2.0);
			double c = Math.cos(r);
			double s = Math.sin(r);
			vx.set(c,s,0);
			vy.set(-s,c,0);
			tx.scale( EE_X.get(),vx);
			ty.scale(-EE_Y.get(),vy);
			arms[i].pEE.add(tx,ty);
			arms[i].pEE.z=EE_Z.get();
			++i;
			tx.scale( EE_X.get(),vx);
			ty.scale( EE_Y.get(),vy);
			arms[i].pEE.add(tx,ty);
			arms[i].pEE.z=EE_Z.get();
		}

	}

	/**
	 * Calculate base of linear slides.
	 * linear slides are ordered counter clockwise, looking down on the machine.
	 *     1
	 *  2       0 <-- first
	 *      x     <-- center
	 *  3       5 <-- last
	 *     4
	 */
	protected void calculateBasePointsOneTime() {
		Vector3d vx = new Vector3d();
		Vector3d vy = new Vector3d();
		Vector3d tx = new Vector3d();
		Vector3d ty = new Vector3d();

		int [] indexes = {0,5,2,1,4,3};

		for(int i=0;i<arms.length;++i) {
			double r = Math.toRadians(120.0*i/2.0);
			double c = Math.cos(r);
			double s = Math.sin(r);
			vx.set(c,s,0);
			vy.set(-s,c,0);
			tx.scale( BASE_X.get(),vx);
			ty.scale( BASE_Y.get(),vy);
			arms[indexes[i]].pBase.add(tx,ty);
			++i;
			tx.scale( BASE_X.get(),vx);
			ty.scale(-BASE_Y.get(),vy);
			arms[indexes[i]].pBase.add(tx,ty);
		}
	}


	@Override
	public void update(double dt) {
		connection.update(dt);
		super.update(dt);

		Matrix4d eeMatrix = ee.getPose();

		// use calculated end effector points to find same points after EE moves.
		for (LinearStewartPlatformArm linearStewartPlatformArm : arms) {
			eeMatrix.transform(linearStewartPlatformArm.pEE, linearStewartPlatformArm.pEE2);
		}

		// We have pEE2 and pBase.  one end of the rod is at pEE2[n].  
		// The sphere formed by pDD2[n] and ARM_LENGTH intersects the vertical line at bBase[n] twice.
		// The first intersection traveling up is the one we want.
		Ray ray = new Ray();
		ray.setDirection(new Vector3d(0,0,1));
		for (LinearStewartPlatformArm arm : arms) {
			ray.setOrigin(arm.pBase);
			arm.linearPosition = IntersectionHelper.raySphere(ray, arm.pEE2, ARM_LENGTH.get()) - BASE_Z.get();
			arm.pSlide.set(arm.pBase);
			arm.pSlide.z += arm.linearPosition + BASE_Z.get();
		}
	}
	
	@Override
	public void render(GL2 gl2) {
		super.render(gl2);

		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, myPose);
			drawBase(gl2);
			drawTopPlate(gl2);
			if(debugEEPoints.get()) drawDebugEEPoints(gl2);
			if(debugSlides.get()) drawDebugSlides(gl2);
			if(debugArms.get()) drawDebugArms(gl2);
		gl2.glPopMatrix();
	}

	private void drawBase(GL2 gl2) {
	}

	private void drawTopPlate(GL2 gl2) {
		MatrixHelper.drawMatrix(gl2, ee.getPose(),5);
	}

	public Matrix4d getEndEffectorPose() {
		return ee.getPose();
	}

	private void drawDebugEEPoints(GL2 gl2) {
		boolean wasLit = OpenGLHelper.disableLightingStart(gl2);
		Vector3d eeCenter = ee.getPosition();
		gl2.glColor3d(1, 0, 0);
		gl2.glBegin(GL2.GL_LINES);
		for (LinearStewartPlatformArm arm : arms) {
			gl2.glVertex3d(eeCenter.x, eeCenter.y, eeCenter.z);
			gl2.glVertex3d(arm.pEE2.x,
					arm.pEE2.y,
					arm.pEE2.z);
			gl2.glColor3d(0, 0, 0);
		}
		gl2.glEnd();
		OpenGLHelper.disableLightingEnd(gl2,wasLit);
	}

	private void drawDebugSlides(GL2 gl2)  {
		boolean wasLit = OpenGLHelper.disableLightingStart(gl2);
		for(int i=0;i<arms.length;++i) {
			renderOneLinearSlide(gl2,
					arms[i].pSlide,
					BASE_Z.get(),
					BASE_Z.get()+SLIDE_TRAVEL.get(),
					i==0);
		}
		OpenGLHelper.disableLightingEnd(gl2,wasLit);
	}

	protected void drawDebugArms(GL2 gl2) {
		boolean wasLit = OpenGLHelper.disableLightingStart(gl2);
		gl2.glColor3d(1, 0, 0);
		gl2.glBegin(GL2.GL_LINES);
		for(int i=0;i<arms.length;++i) {
			gl2.glVertex3d(arms[i].pEE2.x,
						   arms[i].pEE2.y,
						   arms[i].pEE2.z);
			gl2.glVertex3d( arms[i].pSlide.x,
							arms[i].pSlide.y,
							arms[i].pSlide.z);
			gl2.glColor3d(0, 0, 0);
		}
		gl2.glEnd();
		OpenGLHelper.disableLightingEnd(gl2,wasLit);
	}

	private void renderOneLinearSlide(GL2 gl2,Point3d p,double min,double max,boolean first) {
		gl2.glBegin(GL2.GL_LINES);
		if(first) gl2.glColor3d(1, 0, 0);
		else      gl2.glColor3d(0, 1, 0);
		gl2.glVertex3d(p.x, p.y, min);
		gl2.glVertex3d(p.x, p.y, p.z);
		gl2.glColor3d(0, 0, 1);
		gl2.glVertex3d(p.x, p.y, p.z);
		gl2.glVertex3d(p.x, p.y, max);
		gl2.glEnd();
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Linear Stewart Platform",true);
		view.add(connection);
		view.addButton("GOTO EE").addActionEventListener((evt)->gotoPose());
		view.addButton("GOTO ZERO").addActionEventListener((evt)->{
			String message = "G0"
					+" F"+StringHelper.formatDouble(velocity.get())
					+" A"+StringHelper.formatDouble(acceleration.get())
					+" X0"
					+" Y0"
					+" Z0"
					+" U0"
					+" V0"
					+" W0";
			logger.info(message);
			connection.sendMessage(message);
			Matrix4d ident = new Matrix4d();
			ident.setIdentity();
			ee.setPose(ident);
			ee.setPosition(new Vector3d(0,0,BASE_Z.get()+Math.abs(EE_Z.get())+ARM_LENGTH.get()));
		});
		view.addButton("Factory Reset").addActionEventListener((evt)->{
			for(int i=0;i<6;++i) {
				connection.sendMessage("M101 A"+i+" B-1000 T0");
				// wait while it saves...
				try {
					Thread.sleep(2500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		view.addRange(velocity, 20, 1);
		view.addRange(acceleration, 1000, 0);
		view.popStack();
		
		super.getView(view);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		Object o = evt.getSource();
		if(o == connection) {
			//readConnectionData((String)evt.getNewValue());
		}
	}
	
	private void gotoPose() {
		float scale=-10;
		String message = "G0"
				+" F"+StringHelper.formatDouble(velocity.get())
				+" A"+StringHelper.formatDouble(acceleration.get())
				+" X"+StringHelper.formatDouble(arms[0].linearPosition*scale)
				+" Y"+StringHelper.formatDouble(arms[1].linearPosition*scale)
				+" Z"+StringHelper.formatDouble(arms[2].linearPosition*scale)
				+" U"+StringHelper.formatDouble(arms[3].linearPosition*scale)
				+" V"+StringHelper.formatDouble(arms[4].linearPosition*scale)
				+" W"+StringHelper.formatDouble(arms[5].linearPosition*scale);
		logger.info(message);
		connection.sendMessage(message);
	}
}
