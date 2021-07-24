package com.marginallyclever.robotOverlord.robots;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.OpenGLHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.PoseEntity;
import com.marginallyclever.robotOverlord.robots.sixi3.Sixi3Bone;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;
import com.marginallyclever.robotOverlord.uiExposedTypes.MaterialEntity;

/**
 * Spot Micro simulation.  Robot faces +
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class DogRobot extends PoseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5916361555293772951L;

	private double BODY_WIDTH = 12;
	private double BODY_LENGTH = 8;
	private double BODY_HEIGHT = 32;
	private double STEP_LENGTH = 5;
	private double STEP_HEIGHT = 3;

	
	public class DogLeg {
		public Sixi3Bone shoulderA = new Sixi3Bone();
		public Sixi3Bone shoulderB = new Sixi3Bone();
		public Sixi3Bone elbow = new Sixi3Bone();
		public Sixi3Bone foot = new Sixi3Bone();
		
		// actual toe location
		public Vector3d toe = new Vector3d();
		// desired toe location
		public Vector3d toeTarget = new Vector3d();
		// desired toe location, second order
		public Vector3d toeTarget2 = new Vector3d();
		
		public double [] idealStandingAngles = new double[4];
		
		public DogLeg() {}

		public void captureAngles(double[] legAngles) {
			legAngles[0]=shoulderA.theta;
			legAngles[1]=shoulderB.theta;
			legAngles[2]=elbow.theta;
			legAngles[3]=foot.theta;
		}
		
		public void render(GL2 gl2) {
			gl2.glPushMatrix();
			drawLineTo(gl2,shoulderA.pose,255,  0,  0);
			drawLineTo(gl2,shoulderB.pose,  0,  0,  0);
			drawLineTo(gl2,elbow.pose    ,  0,255,  0);
			drawLineTo(gl2,foot.pose     ,  0,  0,255);
			gl2.glPopMatrix();
		}
	};
	
	private DogLeg [] legs = new DogLeg[4];
	private MaterialEntity mat = new MaterialEntity();
	private MaterialEntity mat2 = new MaterialEntity();

	private double leftForce = 0;
	private double forwardForce = 0;
	private double turnForce = 0;
	
	
	public DogRobot() {
		super();
		setName("Dog Robot");
		
		for(int i=0;i<4;++i) {
			legs[i] = new DogLeg();
		}

		setDHParameters();
		
		mat.setLit(true);
		mat2.setLit(true);
		mat2.setDiffuseColor(1,0,0,1);
	}
	
	private void setDHParameters() {
		// robot faces +Z
		// r d a t min max file
		int i=0;
		legs[i].shoulderA.set( BODY_WIDTH/2, BODY_HEIGHT/2, 0, 0, 360, -360, "");
		legs[i].shoulderB.set(0, 0, 90, -90, 360, -360, "");
		legs[i].elbow    .set(11.5, 0, 0, -45, 0, -360, "");
		legs[i].foot     .set(13, 0, 0,  90, 360, -360, "");
		i++;

		legs[i].shoulderA.set(-BODY_WIDTH/2, BODY_HEIGHT/2, 0, 0, 360, -360, "");
		legs[i].shoulderB.set(0, 0, 90, -90, 360, -360, "");
		legs[i].elbow    .set(11.5, 0, 0, -45, 0, -360, "");
		legs[i].foot     .set(13, 0, 0,  90, 360, -360, "");
		i++;

		legs[i].shoulderA.set(-BODY_WIDTH/2,-BODY_HEIGHT/2, 0, 0, 360, -360, "");
		legs[i].shoulderB.set(0, 0, 90, -90, 360, -360, "");
		legs[i].elbow    .set(11.5, 0, 0, -45, 0, -360, "");
		legs[i].foot     .set(13, 0, 0,  90, 360, -360, "");
		i++;

		legs[i].shoulderA.set( BODY_WIDTH/2,-BODY_HEIGHT/2, 0, 0, 360, -360, "");
		legs[i].shoulderB.set(0, 0, 90, -90, 360, -360, "");
		legs[i].elbow    .set(11.5, 0, 0, -45, 0, -360, "");
		legs[i].foot     .set(13, 0, 0,  90, 360, -360, "");
		
		for( DogLeg leg : legs ) {
			updateLegMatrixes(leg);
			//leg.toeTarget2.set(leg.toe);
			//leg.toeTarget.set(leg.toe);
			leg.captureAngles(leg.idealStandingAngles);
		}
	}
	
	
	@Override
	public void update(double dt) {
		super.update(dt);
	}
	
	
	@Override
	public void render(GL2 gl2) {
		super.render(gl2);

		lowerFeetToGround();
		relaxShoulders();

		//walk1();
		walk2();
		
		moveToeTargetsSmoothly(1);
		gradientDescent();

		drawCurrentDogPose(gl2);
		//drawAllShoulderFloorCircles(gl2);
		drawToeTarget(gl2);
		drawPointOnGroundUnderFeet(gl2);
	}
	
	
	private void lowerFeetToGround() {
		for( DogLeg leg : legs ) {
			leg.toeTarget2.z=0;
		}
	}

	private void relaxShoulders() {
		for( DogLeg leg : legs ) {
			double d = leg.idealStandingAngles[0] - leg.shoulderA.theta;
			//d*=1;
			leg.shoulderA.theta+=d;
		}
	}

	// Move the toes towards the toeTargets.
	private void gradientDescent() {
		double [] legAngles = new double[4];
		double EPSILON = 0.001;
		
		int k=0;
		for( DogLeg leg : legs ) {
			legAngles[0] = leg.shoulderA.theta;
			legAngles[1] = leg.shoulderB.theta;
			legAngles[2] = leg.elbow.theta;
			legAngles[3] = leg.foot.theta;
			
			double stepSize=10;
			for(int tries=0;tries<15;++tries) {
				int i;
				// work from toe to shoulder, seems to finish faster than shoulder to toe.
				for(i=legAngles.length-1;i>=0;--i) {
					if(partialDescent(leg,legAngles,i,stepSize,k)<EPSILON) break;
				}
				if(i>0) break;
				
				stepSize*=0.75;
			}
			k++;
			setLegToAngles(leg,legAngles);
		}
	}


	// Wiggle leg joint 'i' to see which way gets a better score.
	private double partialDescent(DogLeg leg,double[] legAngles,int i,double stepSize,int k) {
		double startAngle = legAngles[i];
		double bestAngle = startAngle;
		double startScore = scoreLeg(leg,legAngles);
		double bestScore = startScore;

		legAngles[i] = startAngle-stepSize;
		double scoreNeg = scoreLeg(leg,legAngles);
		if(bestScore>scoreNeg) {
			bestScore = scoreNeg;
			bestAngle = legAngles[i];
		}
		
		legAngles[i] = startAngle+stepSize;
		double scorePos = scoreLeg(leg,legAngles);
		if(bestScore>scorePos) {
			bestScore = scorePos;
			bestAngle = legAngles[i];
		}
		
		legAngles[i] = bestAngle;
		return bestScore;
	}


	// @return the distance from the {@code DogLeg.toe} to the {@code DogLeg.toeTarget}
	private double scoreLeg(DogLeg leg,double [] angles) {
		setLegToAngles(leg,angles);
		Vector3d fp = new Vector3d();
		fp.sub(leg.toe,leg.toeTarget);
		return fp.lengthSquared();
	}


	private void setLegToAngles(DogLeg leg, double[] angles) {
		leg.shoulderA.setAngleWRTLimits(angles[0]);
		leg.shoulderB.setAngleWRTLimits(angles[1]);
		leg.elbow.setAngleWRTLimits(angles[2]);
		leg.foot.setAngleWRTLimits(angles[3]);
		updateLegMatrixes(leg);
	}


	private void drawCurrentDogPose(GL2 gl2) {
		mat.render(gl2);
		
		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2, pose);
		drawTorso(gl2);
		drawLegs(gl2);
		gl2.glPopMatrix();
	}

	
	private void drawTorso(GL2 gl2) {
		double scale = 0.99;
		double width = (BODY_WIDTH/2)*scale;
		double height = (BODY_HEIGHT/2)*scale;
		double length = (BODY_LENGTH/2)*scale;
		PrimitiveSolids.drawBox(gl2, 
			new Point3d(-width,-length,-height),
			new Point3d( width, length, height));
	}

	
	private void drawLegs(GL2 gl2) {
		boolean flag = OpenGLHelper.disableLightingStart(gl2);	
		for( DogLeg leg : legs ) leg.render(gl2);
		OpenGLHelper.disableLightingEnd(gl2, flag);
	}
	

	private void updateLegMatrixes(DogLeg leg) {
		leg.shoulderA.updateMatrix();
		leg.shoulderB.updateMatrix();
		leg.elbow.updateMatrix();
		leg.foot.updateMatrix();
		leg.toe.set(MatrixHelper.getPosition(getWorldMatrixOfToe(leg)));
	}

	
	@SuppressWarnings("unused")
	private void drawPointOnGroundUnderFeet(GL2 gl2) {
		for( DogLeg leg : legs ) {
			Vector3d fp = leg.toe;
			
			gl2.glPushMatrix();
			gl2.glTranslated(fp.x,fp.y,0);
			PrimitiveSolids.drawSphere(gl2, 0.25);
			gl2.glPopMatrix();
		}
	}


	private Matrix4d getWorldMatrixOfToe(DogLeg leg) {
		Matrix4d m = getPose();
		m.mul(leg.shoulderA.pose);
		m.mul(leg.shoulderB.pose);
		m.mul(leg.elbow.pose);
		m.mul(leg.foot.pose);
		return m;
	}
	

	private void walk2() {
		double t = System.currentTimeMillis()*0.001;

		Vector3d toward = getDesiredDirectionOfBody();
		Vector3d push = new Vector3d();
		double feetOnFloor=0;

		int i=0;
		for( DogLeg leg : legs ) {
			Vector3d f2 = getDesiredDirectionOfOneLeg(toward,leg);
			
			// step in the desired direction
			double as = Math.max(0,-Math.sin(t+i*Math.PI/2));
			double ac = Math.cos(t+i*Math.PI/2);
			i++;
			
			//if(f2.lengthSquared()>0)
			{
				f2.scale(STEP_LENGTH*ac);
				f2.z+= as*STEP_HEIGHT;
			}
			//System.out.println(f2.toString());
			
			// remember the point for later
			Vector3d fp = getPointOnFloorUnderShoulder(leg);
			Vector3d target = new Vector3d();
			target.add(f2,fp);
			boolean applyFriction=false;
			if(applyFriction) {
				if(f2.z<=0.001 && leg.toeTarget2.z<0.001) {
					feetOnFloor++;
					//target.sub(leg.toeTarget2);
					target.scale(0.001);
					push.add(target);
				}
			} else {
				leg.toeTarget2.set(target);
			}
		}
		
		if(feetOnFloor>0) {
			push.scale(1.0/feetOnFloor);
			moveBodyToMatchFeet(push);
		}
	}
	
	private void moveBodyToMatchFeet(Vector3d push) {
		// move the body to match the feet
		Matrix4d wp = new Matrix4d();
		Matrix4d wp2 = new Matrix4d();
		getPoseWorld(wp);
		wp2.set(wp);
		
		Vector3d p = MatrixHelper.getPosition(wp);
		p.add(push);
		MatrixHelper.setPosition(wp2, new Vector3d(0,0,0));
		
		//Matrix4d rz = new Matrix4d();
		//rz.rotZ(-turnForce);
		//wp.mul(rz);
		
		MatrixHelper.setPosition(wp2, p);
		
		setPoseWorld(wp2);
	}
	
	
	private Vector3d getDesiredDirectionOfOneLeg(final Vector3d bodyToward,DogLeg leg) {
		Vector3d f2 = new Vector3d();
		
		Vector3d worldUp = new Vector3d(0,0,1);
		Vector3d v1 = getCenterToShoulderOnXYPlane(leg);
		Vector3d crossProduct = new Vector3d();
		crossProduct.cross(v1, worldUp);
		crossProduct.normalize();
		crossProduct.scale(turnForce);
		f2.add(bodyToward,crossProduct);

		if(f2.lengthSquared()>0) f2.normalize();
		
		return f2;
	}
	

	private Vector3d getDesiredDirectionOfBody() {
		Vector3d forward = new Vector3d();
		
		Matrix4d myPose = getPose();
		Vector3d wLeft = MatrixHelper.getXAxis(myPose);
		Vector3d wForward = MatrixHelper.getZAxis(myPose);
		wLeft.scale(leftForce);
		wForward.scale(forwardForce);
		forward.add(wLeft,wForward);
		if(leftForce!=0 && forwardForce!=0) forward.normalize();
		
		return forward;
	}

	
	private void moveToeTargetsSmoothly(double dt) {
		dt *= 0.25;
		dt = Math.max(Math.min(dt, 1), 0);

		Vector3d v = new Vector3d();
		for( DogLeg leg : legs ) {
			v.sub(leg.toeTarget2,leg.toeTarget);
			v.scale(dt);
			leg.toeTarget.add(v);
		}
	}

	
	private Vector3d getCenterToShoulderOnXYPlane(DogLeg leg) {
		Vector3d v1 = getPointOnFloorUnderShoulder(leg);
		Vector3d v0 = MatrixHelper.getPosition(getPose());
		v0.z=0;
		v1.sub(v0);
		v1.normalize();
		
		return v1;
	}

	
	private Vector3d getPointOnFloorUnderShoulder(DogLeg leg) {
		Matrix4d m = new Matrix4d();
		m.set(getPose());
		m.mul(leg.shoulderA.pose);
		m.mul(leg.shoulderB.pose);
		Vector3d fp = MatrixHelper.getPosition(m);
		fp.z=0;

		return fp;
	}

	
	@SuppressWarnings("unused")
	private void drawFloorCirclesUnderEachShoulder(GL2 gl2) {
		for( DogLeg leg : legs ) {
			Vector3d fp = getPointOnFloorUnderShoulder(leg);
			drawFloorCircleUnderOneShoulder(gl2,fp);
		}
	}


	private void drawFloorCircleUnderOneShoulder(GL2 gl2, Vector3d fp) {
		gl2.glPushMatrix();
		gl2.glTranslated(fp.x,fp.y,fp.z);
		PrimitiveSolids.drawCircleXY(gl2, 5, 20);
		gl2.glPopMatrix();
	}


	@SuppressWarnings("unused")
	private void drawToeTarget(GL2 gl2) {
		for( DogLeg leg : legs ) {
			gl2.glPushMatrix();
			gl2.glTranslated(leg.toeTarget.x, leg.toeTarget.y, leg.toeTarget.z);

			if(leg.toeTarget.z<0.1) mat2.render(gl2);
			else mat.render(gl2);
			
			PrimitiveSolids.drawSphere(gl2, 0.5);
			gl2.glPopMatrix();
		}
		mat.render(gl2);
	}


	@SuppressWarnings("unused")
	private void walk1() {
		setDHParameters();
		
		double t = System.currentTimeMillis()*0.0025;
		
		for(int i=0;i<4;++i) {
			double as = Math.toDegrees(Math.sin(t));
			double ac = Math.toDegrees(Math.cos(t));
			legs[i].elbow.theta += as/4;
			legs[i].foot.theta += ac/4;
			updateLegMatrixes(legs[i]);
			t+=Math.PI/2;
		}
	}


	private void drawLineTo(GL2 gl2,Matrix4d m,double r,double g,double b) {
		Vector3d v = new Vector3d();
		m.get(v);
		gl2.glLineWidth(5);
		gl2.glBegin(GL2.GL_LINES);
		gl2.glColor3d(r,g,b);
		gl2.glVertex3d(0, 0, 0);
		gl2.glVertex3d(v.x,v.y,v.z);
		gl2.glEnd();
		gl2.glLineWidth(1);

		//MatrixHelper.drawMatrix(gl2,1);
		MatrixHelper.applyMatrix(gl2, m);
	}
	
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("D","Driving");
		view.addButton("all stop").addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				forwardForce=0;
				leftForce=0;
				turnForce=0;
			}
		});
		view.addButton("forward").addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				forwardForce++;
			}
		});
		view.addButton("backward").addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				forwardForce--;
			}
		});
		view.addButton("strafe left").addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				leftForce++;
			}
		});
		view.addButton("strafe right").addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				leftForce--;
			}
		});
		view.addButton("turn left").addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				turnForce--;
			}
		});
		view.addButton("turn right").addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				turnForce++;
			}
		});
		view.popStack();
		super.getView(view);
	}
}
