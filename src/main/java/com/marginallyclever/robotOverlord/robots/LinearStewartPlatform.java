package com.marginallyclever.robotOverlord.robots;


import java.beans.PropertyChangeEvent;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.IntersectionHelper;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.OpenGLHelper;
import com.marginallyclever.convenience.Ray;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.PoseEntity;
import com.marginallyclever.robotOverlord.shape.Shape;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;
import com.marginallyclever.robotOverlord.uiExposedTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.uiExposedTypes.MaterialEntity;
import com.marginallyclever.robotOverlord.uiExposedTypes.RemoteEntity;

public class LinearStewartPlatform  extends PoseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final double SLIDE_TRAVEL = 10.0;  // cm

	public static final double BASE_X=6.0968;  // cm
	public static final double BASE_Y=1.6000;  // cm
	public static final double BASE_Z=7.8383;  // cm

	public static final double EE_X= 3.6742;  // cm
	public static final double EE_Y= 0.7500;  // cm
	public static final double EE_Z=-2.4000;  // cm

	public static final double ARM_LENGTH=15.0362;  // cm

	private Shape baseModel;
	private Shape eeModel;
	private Shape armModel;
	
	private PoseEntity ee = new PoseEntity("ee");

	private class Arm {
		// lowest point that the magnetic ball can travel.
		// they can only move up from this point.
		public Point3d pBase = new Point3d();
		// center of each magnetic ball at the end effector, before being transformed by ee.pose
		public Point3d pEE = new Point3d();
		// pEE after transform by ee.pose.  will be same coordinate system as base.
		public Point3d pEE2 = new Point3d();
		// point where arm is connected to slider after EE has moved.
		public Point3d pSlide = new Point3d();
		// value to remember to send to robot.
		public double linearPosition;
		
		public Arm() {
			linearPosition=0;
		}
	};

	private Arm [] arms = { new Arm(), new Arm(), new Arm(), new Arm(), new Arm(), new Arm() };
	
	private MaterialEntity me = new MaterialEntity();
	private RemoteEntity connection = new RemoteEntity();
	private DoubleEntity velocity = new DoubleEntity("velocity",5);
	private DoubleEntity acceleration = new DoubleEntity("acceleration",200);

	public LinearStewartPlatform() {
		super("Linear Stewart Platform");
		addChild(ee);

		connection.addPropertyChangeListener(this);
		
		// load models and fix scale/orientation.
		baseModel = new Shape("Base","/linearStewartPlatform/base.stl");
		baseModel.setShapeScale(0.1);
		eeModel = new Shape("ee","/linearStewartPlatform/endEffector.stl");
		eeModel.setShapeScale(0.1);
		eeModel.setShapeRotation(new Vector3d(0,0,-30));
		armModel = new Shape("arm","/linearStewartPlatform/arm.stl");
		armModel.setShapeScale(0.1);

		// apply some default materials.
		me.setAmbientColor(0, 0, 0, 1);
		me.setDiffuseColor(1f,1f,1f,1);
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
			double r = Math.toRadians(60.0+120.0*i/2.0);
			vx.set(Math.cos(r),Math.sin(r),0);
			vy.set(-vx.y,vx.x,0);
			tx.scale( EE_X,vx);
			ty.scale(-EE_Y,vy);
			arms[i].pEE.add(tx,ty);
			arms[i].pEE.z=EE_Z;
			++i;
			tx.scale( EE_X,vx);
			ty.scale( EE_Y,vy);
			arms[i].pEE.add(tx,ty);
			arms[i].pEE.z=EE_Z;
		}

		// calculate base of linear slides.
		// linear slides are ordered counter clockwise, looking down on the machine.
		//     1
		//  2       0 <-- first
		//      x     <-- center
		//  3       5 <-- last
		//     4
		int [] indexes = {0,5,2,1,4,3};

		for(int i=0;i<arms.length;++i) {
			double r = Math.toRadians(120.0*i/2.0);
			vx.set(Math.cos(r),Math.sin(r),0);
			vy.set(-vx.y,vx.x,0);
			tx.scale( BASE_X,vx);
			ty.scale( BASE_Y,vy);
			arms[indexes[i]].pBase.add(tx,ty);
			++i;
			tx.scale( BASE_X,vx);
			ty.scale(-BASE_Y,vy);
			arms[indexes[i]].pBase.add(tx,ty);
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

		// We have pEE2 and pBase.  one end of the rod is at pEE2[n].  
		// The sphere formed by pDD2[n] and ARM_LENGTH intersects the vertical line at bBase[n] twice.
		// The first intersection traveling up is the one we want.
		Ray ray = new Ray();
		ray.direction.set(0,0,1);
		for(int i=0;i<arms.length;++i) {
			ray.start.set(arms[i].pBase);
			arms[i].linearPosition = IntersectionHelper.raySphere(ray, arms[i].pEE2, ARM_LENGTH)-BASE_Z;
			arms[i].pSlide.set(arms[i].pBase);
			arms[i].pSlide.z += arms[i].linearPosition+BASE_Z;
		}
	}
	
	
	@Override
	public void render(GL2 gl2) {
		super.render(gl2);

		gl2.glPushMatrix();
			// draw the base
			MatrixHelper.applyMatrix(gl2, myPose);
			baseModel.render(gl2);
			
			// draw the end effector
			gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, ee.getPose());
			eeModel.render(gl2);
			gl2.glPopMatrix();

			// draw the arms (some work to get each matrix...)
			Matrix4d m = new Matrix4d();
			for(int i=0;i<arms.length;++i) {
				// we need the pose of each bone to draw the mesh.
				// a matrix is 3 orthogonal (right angle) vectors and a position. 
				// z (up) is from one ball to the next
				Vector3d z = new Vector3d(
						arms[i].pEE2.x-arms[i].pSlide.x,
						arms[i].pEE2.y-arms[i].pSlide.y,
						arms[i].pEE2.z-arms[i].pSlide.z);
				z.normalize();
				// x is a vector that is guaranteed not parallel to z.
				Vector3d x = new Vector3d(
						arms[i].pSlide.x,
						arms[i].pSlide.y,
						arms[i].pSlide.z);
				x.normalize();
				// y is orthogonal to x and z.  
				Vector3d y = new Vector3d();
				y.cross(z, x);
				y.normalize();
				// x was not orthogonal to z.
				// y and z are orthogonal, so use them. 
				x.cross(y, z);
				x.normalize();
				
				// fill in the matrix
				m.m00=x.x;
				m.m10=x.y;
				m.m20=x.z;
                    
				m.m01=y.x;
				m.m11=y.y;
				m.m21=y.z;
				    
				m.m02=z.x;
				m.m12=z.y;
				m.m22=z.z;
				
				m.m03=arms[i].pSlide.x;
				m.m13=arms[i].pSlide.y;
				m.m23=arms[i].pSlide.z;
				m.m33=1;
						
				gl2.glPushMatrix();
				MatrixHelper.applyMatrix(gl2, m);
				armModel.render(gl2);
				gl2.glPopMatrix();
			}
			
			// debug info
			boolean wasLit = OpenGLHelper.disableLightingStart(gl2);
			boolean debugEEPoints=false;
			boolean debugSlides=true;
			boolean debugArms=false;
			if(debugEEPoints) drawDebugEEPoints(gl2);
			if(debugSlides) drawDebugSlides(gl2);	
			if(debugArms) drawDebugArms(gl2);
			OpenGLHelper.disableLightingEnd(gl2,wasLit);
			
		gl2.glPopMatrix();

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

	private void drawDebugSlides(GL2 gl2)  {
		for(int i=0;i<arms.length;++i) {
			renderOneLinearSlide(gl2,
					arms[i].pSlide,
					BASE_Z,
					BASE_Z+SLIDE_TRAVEL,
					i==0);
		}
	}

	private void drawDebugArms(GL2 gl2) {
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
	}

	private void renderOneLinearSlide(GL2 gl2,Point3d p,double min,double max,boolean first) {
		gl2.glBegin(GL2.GL_LINES);
		if(first) gl2.glColor3d(1, 0, 0);
		else      gl2.glColor3d(1, 1, 1);
		gl2.glVertex3d(p.x, p.y, min);
		gl2.glVertex3d(p.x, p.y, p.z);
		gl2.glColor3d(0, 0, 1);
		gl2.glVertex3d(p.x, p.y, p.z);
		gl2.glVertex3d(p.x, p.y, max);
		gl2.glEnd();
	}
	
	@Override
	public void getView(ViewPanel view) {

		view.pushStack("LSP", "Linear Stewart Platform");
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
			Log.message(message);
			connection.sendMessage(message);
			Matrix4d ident = new Matrix4d();
			ident.setIdentity();
			ee.setPose(ident);
			ee.setPosition(new Vector3d(0,0,BASE_Z+Math.abs(EE_Z)+ARM_LENGTH));
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
				+" X"+StringHelper.formatDouble(arms[0].linearPosition*scale)
				+" Y"+StringHelper.formatDouble(arms[1].linearPosition*scale)
				+" Z"+StringHelper.formatDouble(arms[2].linearPosition*scale)
				+" U"+StringHelper.formatDouble(arms[3].linearPosition*scale)
				+" V"+StringHelper.formatDouble(arms[4].linearPosition*scale)
				+" W"+StringHelper.formatDouble(arms[5].linearPosition*scale);
		Log.message(message);
		connection.sendMessage(message);
	}
}
