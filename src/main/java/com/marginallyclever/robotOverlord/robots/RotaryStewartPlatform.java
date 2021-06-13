package com.marginallyclever.robotOverlord.robots;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.OpenGLHelper;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.PoseEntity;
import com.marginallyclever.robotOverlord.shape.Shape;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;
import com.marginallyclever.robotOverlord.uiExposedTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.uiExposedTypes.MaterialEntity;
import com.marginallyclever.robotOverlord.uiExposedTypes.RemoteEntity;

public class RotaryStewartPlatform  extends PoseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public final String hello = "HELLO WORLD! I AM STEWART PLATFORM V4.2";
	// machine dimensions
	public final double BASE_X         = 8.093f;
	public final double BASE_Y         = 2.150f;
	public final double BASE_Z         = 6.610f;
	public final double EE_X           = 7.635f;
	public final double EE_Y           = 0.553f;
	public final double EE_Z           =-0.870f;
	public final double BICEP_LENGTH   = 5.000f;
	public final double ARM_LENGTH     =16.750f;

	private Shape baseModel;
	private Shape eeModel;
	private Shape armModel;
	
	private PoseEntity ee = new PoseEntity("ee");

	private class Arm {
		// lowest point that the magnetic ball can travel.
		// they can only move up from this point.
		public Point3d pShoulder = new Point3d();
		// center of each magnetic ball at the end effector, before being transformed by ee.pose
		public Point3d pEE = new Point3d();
		// pEE after transform by ee.pose.  will be same coordinate system as base.
		public Point3d pEE2 = new Point3d();
		// point where arm is connected to slider after EE has moved.
		public Point3d pElbow = new Point3d();
		// value to remember to send to robot.
		public double angle;
		
		public Arm() {
			angle=0;
		}
	};

	private Arm [] arms = { new Arm(), new Arm(), new Arm(), new Arm(), new Arm(), new Arm() };
	
	private MaterialEntity me = new MaterialEntity();
	private RemoteEntity connection = new RemoteEntity();
	private DoubleEntity velocity = new DoubleEntity("velocity",5);
	private DoubleEntity acceleration = new DoubleEntity("acceleration",200);
	
	private int [] indexes = {0,5,2,1,4,3};

	public RotaryStewartPlatform() {
		super("Rotary Stewart Platform");
		addChild(ee);

		connection.addPropertyChangeListener(this);
		
		// load models and fix scale/orientation.
		baseModel = new Shape("/rotaryStewartPlatform/base.stl");
		baseModel.setShapeScale(0.1);
		eeModel = new Shape("/rotaryStewartPlatform/endEffector.stl");
		eeModel.setShapeScale(0.1);
		eeModel.setShapeRotation(new Vector3d(0,0,-30));
		armModel = new Shape("/rotaryStewartPlatform/arm.stl");
		armModel.setShapeScale(0.1);

		eeModel.setShapeRotation(180,0,30);
		baseModel.setShapeRotation(0,90,90);
		baseModel.setShapeOrigin(0,0,BASE_Z + 0.6);
		
		// apply some default materials.
		me.setAmbientColor(0, 0, 0, 1);
		me.setDiffuseColor(1,1,1,1);
		me.setEmissionColor(0, 0, 0, 1);
		me.setLit(true);
		me.setShininess(0);
		baseModel.setMaterial(me);
		eeModel.setMaterial(me);
		armModel.setMaterial(me);

		Vector3d vx = new Vector3d();
		Vector3d vy = new Vector3d();
		Vector3d tx = new Vector3d();
		Vector3d ty = new Vector3d();

		// calculate end effector points - the center of each magnetic ball at the end effector
		// end effector points are ordered counter clockwise, looking down on the machine.
		//       1
		//  2       0 <-- first
		//      x     <-- center
		//  3       5 <-- last
		//       4
		for(int i=0;i<arms.length;++i) {
			double r = Math.toRadians(120.0*i/2.0);
			vx.set(Math.cos(r),Math.sin(r),0);
			vy.set(-vx.y,vx.x,0);
			tx.scale( EE_X,vx);
			ty.scale( EE_Y,vy);
			arms[indexes[i]].pEE.add(tx,ty);
			arms[indexes[i]].pEE.z=EE_Z;
			++i;
			tx.scale( EE_X,vx);
			ty.scale(-EE_Y,vy);
			arms[indexes[i]].pEE.add(tx,ty);
			arms[indexes[i]].pEE.z=EE_Z;
		}

		// calculate base of linear slides.
		// linear slides are ordered counter clockwise, looking down on the machine.
		//     1
		//  2       0 <-- first
		//      x     <-- center
		//  3       5 <-- last
		//     4
		for(int i=0;i<arms.length;++i) {
			double r = Math.toRadians(120.0*i/2.0);
			vx.set(Math.cos(r),Math.sin(r),0);
			vy.set(-vx.y,vx.x,0);
			tx.scale( BASE_X,vx);
			ty.scale( BASE_Y,vy);
			arms[indexes[i]].pShoulder.add(tx,ty);
			arms[indexes[i]].pShoulder.z=BASE_Z;
			++i;
			tx.scale( BASE_X,vx);
			ty.scale(-BASE_Y,vy);
			arms[indexes[i]].pShoulder.add(tx,ty);
			arms[indexes[i]].pShoulder.z=BASE_Z;
		}
		
		ee.setPosition(new Vector3d(0,0,BASE_Z+Math.abs(EE_Z)+ARM_LENGTH));
	}
	
	
	@Override
	public void update(double dt) {
		connection.update(dt);
		super.update(dt);

		Matrix4d eeMatrix = ee.getPose();

		// use calculated end effector points to find same points after EE moves.
		for(int i=0;i<arms.length;++i) {
			eeMatrix.transform(arms[i].pEE, arms[i].pEE2);
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
			Arm arm = arms[i];
			
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
			b=Math.sqrt(ARM_LENGTH*ARM_LENGTH-a*a);
			if(Double.isNaN(b)) throw new AssertionError();

			// use intersection of circles to find elbow point.
			//a = (r0r0 - r1r1 + d*d ) / (2*d) 
			r1=b;  // circle 1 centers on wrist
			r0=BICEP_LENGTH;  // circle 0 centers on shoulder
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
			MatrixHelper.applyMatrix(gl2, pose);
			
			baseModel.render(gl2);
			
			// draw the end effector
			gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, ee.getPose());
			eeModel.render(gl2);
			gl2.glPopMatrix();

			drawBiceps(gl2);
			drawForearms(gl2);
			
			// debug info
			boolean wasLit = OpenGLHelper.disableLightingStart(gl2);
			boolean debugElbows=false;
			boolean debugEEPoints=false;
			boolean debugArms=false;
			if(debugElbows) drawDebugElbows(gl2);
			if(debugEEPoints) drawDebugEEPoints(gl2);
			if(debugArms) drawDebugArms(gl2);
			OpenGLHelper.disableLightingEnd(gl2,wasLit);
		gl2.glPopMatrix();
	}
	
	private void drawDebugElbows(GL2 gl2) {
		for(int i=0;i<arms.length;++i) {
			gl2.glPushMatrix();
			gl2.glTranslated(
					arms[i].pElbow.x,
					arms[i].pElbow.y,
					arms[i].pElbow.z);
			MatrixHelper.drawMatrix(gl2,3);
			gl2.glPopMatrix();
		}
		

		gl2.glBegin(GL2.GL_LINES);
		int i;
		for(i=0;i<arms.length;++i) {
			int j = (i+arms.length-1)%arms.length;
			
			// project wrist position onto plane of bicep (wop)
			double angle = Math.toRadians(((j/2)+1)*120.0);
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
	}

	private void drawBiceps(GL2 gl2) {
		for(int i=0;i<arms.length;++i) {
			gl2.glPushMatrix();
			int k = (i+arms.length-1)%arms.length;
			gl2.glTranslated(arms[i].pShoulder.x,arms[i].pShoulder.y, arms[i].pShoulder.z);
			double j = Math.floor(k/2)+1;
			gl2.glRotated(j*120, 0, 0, 1);
			gl2.glRotated(90, 0, 1, 0);
			gl2.glTranslated(0,0,-1);
			gl2.glRotated(arms[i].angle, 0, 0, 1);
			armModel.render(gl2);
			gl2.glPopMatrix();
		}
	}

	private void drawForearms(GL2 gl2) {
		boolean wasLit = OpenGLHelper.disableLightingStart(gl2);
		
		gl2.glColor3d(1, 0, 0);
		gl2.glBegin(GL2.GL_LINES);
		for(int i=0;i<arms.length;++i) {
			gl2.glVertex3d(arms[i].pEE2.x,
						   arms[i].pEE2.y,
						   arms[i].pEE2.z);
			gl2.glVertex3d(arms[i].pElbow.x,
						   arms[i].pElbow.y,
						   arms[i].pElbow.z);
			gl2.glColor3d(0, 0, 0);
		}
		gl2.glEnd();
		OpenGLHelper.disableLightingEnd(gl2,wasLit);
	}

	private void drawDebugArms(GL2 gl2) {
		gl2.glColor3d(1, 0, 0);
		gl2.glBegin(GL2.GL_LINES);
		for(int i=0;i<arms.length;++i) {
			gl2.glVertex3d(arms[i].pElbow.x,
						   arms[i].pElbow.y,
						   arms[i].pElbow.z);
			gl2.glVertex3d( arms[i].pShoulder.x,
							arms[i].pShoulder.y,
							arms[i].pShoulder.z);
			gl2.glColor3d(0, 0, 0);
		}
		gl2.glEnd();
	}

	private void drawDebugEEPoints(GL2 gl2) {
		Vector3d eeCenter = ee.getPosition();
		gl2.glColor3d(1, 0, 0);
		gl2.glBegin(GL2.GL_LINES);
		for(int i=0;i<arms.length;++i) {
			gl2.glVertex3d(eeCenter.x,eeCenter.y,eeCenter.z);
			gl2.glVertex3d(arms[i].pEE2.x,
						   arms[i].pEE2.y,
						   arms[i].pEE2.z);
			gl2.glColor3d(0, 0, 0);
		}
		gl2.glEnd();
	}
	
	@Override
	public void getView(ViewPanel view) {

		view.pushStack("LSP", "Linear Stewart Platform");
		view.add(connection);
		view.addButton("GOTO EE").addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				gotoPose();
			}
		});
		view.addButton("GOTO ZERO").addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				String message = "G0"
						+" F"+StringHelper.formatDouble(velocity.get())
						+" A"+StringHelper.formatDouble(acceleration.get())
						+" X0"
						+" Y0"
						+" Z0"
						+" U0"
						+" V0"
						+" W0";
				System.out.println(message);
				connection.sendMessage(message);
				Matrix4d ident = new Matrix4d();
				ident.setIdentity();
				ee.setPose(ident);
				ee.setPosition(new Vector3d(0,0,BASE_Z+Math.abs(EE_Z)+ARM_LENGTH));
			}
		});
		view.addButton("Factory Reset").addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				for(int i=0;i<6;++i) {
					connection.sendMessage("M101 A"+i+" B-1000 T0");
					// wait while it saves...
					try {
						Thread.sleep(2500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		view.addRange(velocity, 20, 1);
		view.addRange(acceleration, 1000, 0);
		view.popStack();
		
		me.getView(view);
		
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
		System.out.println(message);
		connection.sendMessage(message);
	}
}
