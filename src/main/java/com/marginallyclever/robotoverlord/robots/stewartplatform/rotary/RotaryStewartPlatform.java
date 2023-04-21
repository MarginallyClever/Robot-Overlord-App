package com.marginallyclever.robotoverlord.robots.stewartplatform.rotary;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.OpenGLHelper;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.entities.PoseEntity;
import com.marginallyclever.robotoverlord.parameters.BooleanEntity;
import com.marginallyclever.robotoverlord.parameters.DoubleEntity;
import com.marginallyclever.robotoverlord.parameters.RemoteEntity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.beans.PropertyChangeEvent;

/**
 * Generic rotary stewart platform.  6 Rotating biceps move forearms connected to the top plate.
 * @author Dan Royer
 * @since 2015?
 */
@Deprecated
public class RotaryStewartPlatform extends PoseEntity {
	private static final Logger logger = LoggerFactory.getLogger(RotaryStewartPlatform.class);


	public final String hello = "HELLO WORLD! I AM STEWART PLATFORM V4.2";
	// machine dimensions
	public DoubleEntity BASE_X         = new DoubleEntity("BASE_X",8.093f);
	public DoubleEntity BASE_Y         = new DoubleEntity("BASE_Y",2.150f);
	public DoubleEntity BASE_Z         = new DoubleEntity("BASE_Z",6.610f);
	public DoubleEntity EE_X           = new DoubleEntity("EE_X",7.635f);
	public DoubleEntity EE_Y           = new DoubleEntity("EE_Y",0.553f);
	public DoubleEntity EE_Z           = new DoubleEntity("EE_Z",-0.870f);
	public DoubleEntity BICEP_LENGTH   = new DoubleEntity("BICEP_LENGTH",5.000f);
	public DoubleEntity ARM_LENGTH     = new DoubleEntity("ARM_LENGTH",16.750f);

	protected BooleanEntity debugElbows = new BooleanEntity("debugElbows",false);
	protected BooleanEntity debugEEPoints = new BooleanEntity("debugEEPoints",false);
	protected BooleanEntity debugArms = new BooleanEntity("debugArms",false);

	private final PoseEntity ee = new PoseEntity("ee");

	protected final RotaryStewartPlatformArm [] arms = {
			new RotaryStewartPlatformArm(),
			new RotaryStewartPlatformArm(),
			new RotaryStewartPlatformArm(),
			new RotaryStewartPlatformArm(),
			new RotaryStewartPlatformArm(),
			new RotaryStewartPlatformArm()
	};

	private final RemoteEntity connection = new RemoteEntity();
	private final DoubleEntity velocity = new DoubleEntity("velocity",5);
	private final DoubleEntity acceleration = new DoubleEntity("acceleration",200);

	protected final MaterialComponent material = new MaterialComponent();

	private final int [] indexes = {0,5,2,1,4,3};

	public RotaryStewartPlatform() {
		this("Rotary Stewart Platform");
	}

	public RotaryStewartPlatform(String name) {
		super(name);
		addEntity(ee);

		connection.addPropertyChangeListener(this);

		// apply some default materials.
		material.setAmbientColor(0, 0, 0, 1);
		material.setDiffuseColor(1,1,1,1);
		material.setEmissionColor(0, 0, 0, 1);
		material.setLit(true);
		material.setShininess(0);

		calculateEndEffectorPointsOneTime();
		calculateMotorAxlePointsOneTime();
		
		ee.setPosition(new Vector3d(0,0,BASE_Z.get()+Math.abs(EE_Z.get())+ARM_LENGTH.get()));
	}

	/**
	 * calculate the center of each ball joint on the top plate, relative to the end effector.
	 * The points are ordered counter clockwise, looking down on the machine.
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
			double r = Math.toRadians(120.0*i/2.0);
			double c = Math.cos(r);
			double s = Math.sin(r);
			vx.set(c,s,0);
			vy.set(-s,c,0);
			tx.scale( EE_X.get(),vx);
			ty.scale( EE_Y.get(),vy);
			arms[indexes[i]].pEE.add(tx,ty);
			arms[indexes[i]].pEE.z=EE_Z.get();
			++i;
			tx.scale( EE_X.get(),vx);
			ty.scale(-EE_Y.get(),vy);
			arms[indexes[i]].pEE.add(tx,ty);
			arms[indexes[i]].pEE.z=EE_Z.get();
		}
	}

	/**
	 * Calculate base of linear slides.
	 * linear slides are ordered counter-clockwise, looking down on the machine.
	 *     1
 	 *  2       0 <-- first
	 *      x     <-- center
	 *  3       5 <-- last
	 *     4
	 */
	protected void calculateMotorAxlePointsOneTime() {
		Vector3d vx = new Vector3d();
		Vector3d vy = new Vector3d();
		Vector3d tx = new Vector3d();
		Vector3d ty = new Vector3d();

		for(int i=0;i<arms.length;++i) {
			double r = Math.toRadians(120.0*i/2.0);
			double c = Math.cos(r);
			double s = Math.sin(r);
			vx.set(c,s,0);
			vy.set(-s,c,0);
			tx.scale( BASE_X.get(),vx);
			ty.scale( BASE_Y.get(),vy);
			arms[indexes[i]].pShoulder.add(tx,ty);
			arms[indexes[i]].pShoulder.z=BASE_Z.get();
			++i;
			tx.scale( BASE_X.get(),vx);
			ty.scale(-BASE_Y.get(),vy);
			arms[indexes[i]].pShoulder.add(tx,ty);
			arms[indexes[i]].pShoulder.z=BASE_Z.get();
		}
	}


	@Override
	public void update(double dt) {
		connection.update(dt);
		super.update(dt);

		Matrix4d eeMatrix = ee.getPose();

		// use calculated end effector points to find same points after EE moves.
		for (RotaryStewartPlatformArm arm : arms) {
			eeMatrix.transform(arm.pEE, arm.pEE2);
		}
		
		findElbowPositions();
	}

	private void findElbowPositions() {
		Vector3d wrist = new Vector3d();
		Vector3d projectedWrist = new Vector3d();
		Vector3d temp = new Vector3d();
		Vector3d r = new Vector3d();
		double b,d,r1,r0,hh,y,x;

		int i;
		for(i=0;i<arms.length;++i) {
			int j = (i+arms.length-1)%arms.length;
			int k = ((j/2)+1);
			RotaryStewartPlatformArm arm = arms[i];
			
			double angle = Math.toRadians(k*120.0);
			double c= Math.cos(angle);
			double s= Math.sin(angle);
			Vector3d normal = new Vector3d(c,s,0);
			Vector3d ortho = new Vector3d(-s,c,0);
			
			// projectedWrist = project pEE2 onto plane of bicep

			wrist.set(arm.pEE2);
			wrist.sub(arm.pShoulder);

			double a = wrist.dot(normal);
			//projectedWrist = wrist - (normal * a);
			temp.set(normal);
			temp.scale(a);
			projectedWrist.set(wrist);
			projectedWrist.sub(temp);

			// we need to find projectedWrist-elbow to calculate the angle at the shoulder.
			// projectedWrist-elbow is not the same as wrist-elbow.
			b=Math.sqrt(ARM_LENGTH.get()*ARM_LENGTH.get()-a*a);
			if(Double.isNaN(b)) throw new AssertionError();

			// use intersection of circles to find elbow point.
			//a = (r0r0 - r1r1 + d*d ) / (2*d) 
			r1=b;  // circle 1 centers on wrist
			r0=BICEP_LENGTH.get();  // circle 0 centers on shoulder
			d=projectedWrist.length();	
			// distance along projectedWrist to the midpoint between the two possible intersections
			a = ( r0 * r0 - r1 * r1 + d*d ) / ( 2.0f*d );

			// now find the midpoint
			// normalize projectedWrist (projectedWrist /= d)
			projectedWrist.scale(1.0f/d);
			//temp=arm.shoulder+(projectedWrist*a);
			temp.set(projectedWrist);
			temp.scale(a);
			temp.add(arm.pShoulder);
			// with a and r0 we can find h, the distance from midpoint to intersections.
			hh=Math.sqrt(r0*r0-a*a);
			if(!Double.isNaN(hh)) {
				// get a line orthogonal to projectedWrist in the plane of normal.
				r.cross(normal,projectedWrist);
				r.scale(hh);
				arm.pElbow.set(temp);
				if(j%2==0) arm.pElbow.add(r);
				else       arm.pElbow.sub(r);
	
				temp.sub(arm.pElbow,arm.pShoulder);
				y=-temp.z;
				temp.z=0;
				x=temp.length();
				// use atan2 to find theta
				if( ortho.dot(temp) < 0 ) x=-x;
				arm.angle=(float)Math.toDegrees(Math.atan2(-y,x))%360;
			}
		}
	}

	@Override
	public void render(GL2 gl2) {
		super.render(gl2);

		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, myPose);

			// draw the end effector
			gl2.glPushMatrix();
			MatrixHelper.drawMatrix(gl2, ee.getPose(),5);
			gl2.glPopMatrix();

			drawBiceps(gl2);
			drawForearms(gl2);
			
			// debug info
			if(debugElbows.get()) drawDebugElbows(gl2);
			if(debugEEPoints.get()) drawDebugEEPoints(gl2);
			if(debugArms.get()) drawDebugArms(gl2);
		gl2.glPopMatrix();
	}
	
	protected void drawDebugElbows(GL2 gl2) {
		boolean wasLit = OpenGLHelper.disableLightingStart(gl2);

		for (RotaryStewartPlatformArm arm : arms) {
			gl2.glPushMatrix();
			gl2.glTranslated(
					arm.pElbow.x,
					arm.pElbow.y,
					arm.pElbow.z);
			MatrixHelper.drawMatrix(gl2, 3);
			gl2.glPopMatrix();
		}
		

		gl2.glBegin(GL2.GL_LINES);
		int i;
		for(i=0;i<arms.length;++i) {
			int j = (i+arms.length-1)%arms.length;
			
			// project wrist position onto plane of bicep (wop)
			double angle = Math.toRadians(((int)(j/2)+1)*120.0);
			double c= Math.cos(angle);
			double s= Math.sin(angle);
			Vector3d normal = new Vector3d(c,s,0);
			Vector3d ortho = new Vector3d(-s,c,0);
			gl2.glColor3d(1, 0, 0);
			gl2.glVertex3d(0, 0, 0);
			gl2.glVertex3d(
					normal.x*10,
					normal.y*10,
					normal.z*10);
			gl2.glColor3d(0, 1, 0);
			gl2.glVertex3d(0, 0, 0);
			gl2.glVertex3d(
					ortho.x*10,
					ortho.y*10,
					ortho.z*10);
		}
		gl2.glEnd();

		OpenGLHelper.disableLightingEnd(gl2,wasLit);
	}

	protected void drawBiceps(GL2 gl2) {
		boolean wasLit = OpenGLHelper.disableLightingStart(gl2);

		gl2.glBegin(GL2.GL_LINES);
		for(int i=0;i<arms.length;++i) {
			int k = (i+arms.length-1)%arms.length;
			gl2.glVertex3d(arms[i].pShoulder.x,arms[i].pShoulder.y, arms[i].pShoulder.z);
			gl2.glVertex3d(arms[i].pElbow.x,arms[i].pElbow.y,arms[i].pElbow.z);
		}
		gl2.glEnd();

		OpenGLHelper.disableLightingEnd(gl2,wasLit);
	}

	protected void drawForearms(GL2 gl2) {
		boolean wasLit = OpenGLHelper.disableLightingStart(gl2);

		gl2.glColor3d(1, 0, 0);
		gl2.glBegin(GL2.GL_LINES);
		for (RotaryStewartPlatformArm arm : arms) {
			gl2.glVertex3d(arm.pEE2.x,
					arm.pEE2.y,
					arm.pEE2.z);
			gl2.glVertex3d(arm.pElbow.x,
					arm.pElbow.y,
					arm.pElbow.z);
			gl2.glColor3d(0, 0, 0);
		}
		gl2.glEnd();
		OpenGLHelper.disableLightingEnd(gl2,wasLit);
	}

	protected void drawDebugArms(GL2 gl2) {
		gl2.glColor3d(1, 0, 0);
		gl2.glBegin(GL2.GL_LINES);
		for (RotaryStewartPlatformArm arm : arms) {
			gl2.glVertex3d(arm.pElbow.x,
					arm.pElbow.y,
					arm.pElbow.z);
			gl2.glVertex3d(arm.pShoulder.x,
					arm.pShoulder.y,
					arm.pShoulder.z);
			gl2.glColor3d(0, 0, 0);
		}
		gl2.glEnd();
	}

	protected void drawDebugEEPoints(GL2 gl2) {
		Vector3d eeCenter = ee.getPosition();
		gl2.glColor3d(1, 0, 0);
		gl2.glBegin(GL2.GL_LINES);
		for (RotaryStewartPlatformArm arm : arms) {
			gl2.glVertex3d(eeCenter.x, eeCenter.y, eeCenter.z);
			gl2.glVertex3d(arm.pEE2.x,
					arm.pEE2.y,
					arm.pEE2.z);
			gl2.glColor3d(0, 0, 0);
		}
		gl2.glEnd();
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Rotary Stewart Platform",true);
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
				+" X"+StringHelper.formatDouble(arms[0].angle*scale)
				+" Y"+StringHelper.formatDouble(arms[1].angle*scale)
				+" Z"+StringHelper.formatDouble(arms[2].angle*scale)
				+" U"+StringHelper.formatDouble(arms[3].angle*scale)
				+" V"+StringHelper.formatDouble(arms[4].angle*scale)
				+" W"+StringHelper.formatDouble(arms[5].angle*scale);
		logger.info(message);
		connection.sendMessage(message);
	}

	public Matrix4d getEndEffectorPose() {
		return ee.getPose();
	}
}
