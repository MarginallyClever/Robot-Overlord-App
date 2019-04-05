package com.marginallyclever.robotOverlord.sixi2Robot;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.lines.LineControlPoint;
import com.marginallyclever.robotOverlord.robot.RobotKeyframe;
import com.marginallyclever.robotOverlord.world.World;

/**
 * A snapshot in time of the robot in a given position.  Can run forward or inverse kinematics to calcualte the joint angles and/or the tool position.
 * @author danroyer
 *
 */
class Sixi2RobotKeyframe implements RobotKeyframe {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1012199745425607761L;
	
	// angle of rotation
	double angle0;
	double angle1;
	double angle2;
	double angle3;
	double angle4;
	double angle5;
	double angleServo;
	
	// robot arm coordinates.  Relative to base unless otherwise noted.
	public Vector3d fingerPosition = new Vector3d();
	public Vector3d fingerForward = new Vector3d();
	public Vector3d fingerRight = new Vector3d();
	// finger rotation, in degrees
	public double ikU=0;
	public double ikV=0;
	public double ikW=0;
	// joint locations relative to base
	public Vector3d wrist = new Vector3d();
	public Vector3d elbow = new Vector3d();
	public Vector3d bicep = new Vector3d();
	public Vector3d shoulder = new Vector3d();
	public Vector3d base = new Vector3d();
	
	public Vector3d anchorPosition = new Vector3d();  // relative to world
	// base orientation, affects entire arm
	public Vector3d baseForward = new Vector3d();
	public Vector3d baseUp = new Vector3d();
	public Vector3d baseRight = new Vector3d();
	
	// rotating entire robot
	public double basePan=0;
	public double baseTilt=0;
	
	public String additionalInstructions;
	
	
	public Sixi2RobotKeyframe() {
		super();

		angle0 = 0;
		angle1 = 0;
		angle2 = 0;
		angle3 = 0;
		angle4 = 0;
		angle5 = 0;
		angleServo = 120;
	}
	
	
	void set(Sixi2RobotKeyframe other) {
		angle0 = other.angle0;
		angle1 = other.angle1;
		angle2 = other.angle2;
		angle3 = other.angle3;
		angle4 = other.angle4;
		angle5 = other.angle5;
		angleServo = other.angleServo;
		ikU=other.ikU;
		ikV=other.ikV;
		ikW=other.ikW;
		fingerForward.set(other.fingerForward);
		fingerRight.set(other.fingerRight);
		fingerPosition.set(other.fingerPosition);
		wrist.set(other.wrist);
		elbow.set(other.elbow);
		bicep.set(other.bicep);
		shoulder.set(other.shoulder);
		anchorPosition.set(other.anchorPosition);
		baseForward.set(other.baseForward);
		baseUp.set(other.baseUp);
		baseRight.set(other.baseRight);
		basePan = other.basePan;
		baseTilt = other.baseTilt;

		wrist.set(other.wrist);
		elbow.set(other.elbow);
		shoulder.set(other.shoulder);
		base.set(other.base);
	}
	
	/**
	 * Fill this instance with the interpolated value of (b-a)*t+a, where t={0..1}.  
	 *  The valid values are: {@fingerPosition}, {@fingerForward}, {@fingerRight}, {@ikU}, {@ikV}, {@ikW},
	 *  and {@angleServo}.  No other values should be treated as valid until after running forward or inverse kinematics.
	 * @param a The starting at t=0.
	 * @param b The keyframe at t=1.
	 * @param t {0..1}
	 */
	public void interpolate(RobotKeyframe arg0,RobotKeyframe arg1,double t) {
		Sixi2RobotKeyframe a = (Sixi2RobotKeyframe)arg0;
		Sixi2RobotKeyframe b = (Sixi2RobotKeyframe)arg1;
		
		if(t==0) {
			this.set(a);
			return;
		} else if(t==1) {
			this.set(b);
			return;
		}
		
		angleServo = MathHelper.interpolate(a.angleServo,b.angleServo,t);
		
		fingerPosition.set(MathHelper.interpolate(a.fingerPosition,b.fingerPosition,t));
		fingerForward.set(MathHelper.slerp(a.fingerForward,b.fingerForward,t));
		fingerRight.set(MathHelper.slerp(a.fingerRight,b.fingerRight,t));

		// finger rotation, in degrees
		//ikU = MathHelper.interpolate(a.ikU,b.ikU,t);
		//ikV = MathHelper.interpolate(a.ikV,b.ikV,t);
		//ikW = MathHelper.interpolate(a.ikW,b.ikW,t);
		
		anchorPosition = MathHelper.interpolate(a.anchorPosition,b.anchorPosition,t);
		baseForward = MathHelper.slerp(a.baseForward,b.baseForward,t);
		baseRight = MathHelper.slerp(a.baseRight,b.baseRight,t);
		baseUp.cross(baseRight, baseForward);

		// rotating entire robot
		basePan = MathHelper.interpolate(a.basePan,b.basePan,t);

		// A linear interpolation of the joint positions would be wrong.
		// they should be calculated from the inverse kinematics.
		inverseKinematics(false,null);
		//forwardKinematics(false,null);
	}
	
	public void rotateFinger(double u,double v,double w) {
		// On a 3-axis robot when homed the forward axis of the finger tip is pointing downward.
		// More complex arms start from the same assumption.
		this.ikU=u;
		this.ikV=v;
		this.ikW=w;

		// Rotating around itself has no effect, so just skip it
		//Vector3d result = MathHelper.rotateAroundAxis(World.forward,World.forward,(float)Math.toRadians(motionFuture.ikU));
		Vector3d result = new Vector3d(World.forward);

		result = MathHelper.rotateAroundAxis(result     ,World.right  ,(float)Math.toRadians(this.ikV));
		result = MathHelper.rotateAroundAxis(result     ,World.up     ,(float)Math.toRadians(this.ikW));
		this.fingerForward.set(result);

		result = MathHelper.rotateAroundAxis(World.right,World.forward,(float)Math.toRadians(this.ikU));
		result = MathHelper.rotateAroundAxis(result     ,World.right  ,(float)Math.toRadians(this.ikV));
		result = MathHelper.rotateAroundAxis(result     ,World.up     ,(float)Math.toRadians(this.ikW));
		this.fingerRight.set(result);
	}
	
	/**
	 * visualize this keyframe
	 */
	public void render(GL2 gl2) {
		Vector3d fingerUp = new Vector3d();
		fingerUp.cross(fingerRight,fingerForward);
		MatrixHelper.drawMatrix(gl2,fingerPosition,fingerForward,fingerRight,fingerUp);
		
		// draw the control handles?
	}
	
	/**
	 * visualize the change between this keyframe and another
	 */
	@Override
	public void renderInterpolation(GL2 gl2,RobotKeyframe arg1) {
		Sixi2RobotKeyframe b = (Sixi2RobotKeyframe)arg1;
		LineControlPoint lcp = new LineControlPoint();
		
		lcp.position.p0.set(this.fingerPosition);
		lcp.position.p1.set(this.fingerPosition);  // TODO add control handles!
		lcp.position.p2.set(b.fingerPosition);	// TODO add control handles!
		lcp.position.p3.set(b.fingerPosition);

		lcp.render(gl2);
	}
	
	
	/**
	 * Calculate the finger location from the angles at each joint
	 * @param keyframe
	 * @param renderMode don't apply math, just visualize the intermediate results
	 */
	protected void forwardKinematics(boolean renderMode,GL2 gl2) {
		double angle0rad = Math.toRadians(-angle0);
		double angle1rad = Math.toRadians(90+angle1);
		double angle2rad = Math.toRadians(-angle2);
		double angle3rad = Math.toRadians(angle3);
		double angle4rad = Math.toRadians(180-angle4);
		double angle5rad = Math.toRadians(-angle5);


		Vector3d shoulderPosition = new Vector3d(0,0,(float)(Sixi2Robot.FLOOR_TO_SHOULDER));
		Vector3d shoulderPlaneZ = new Vector3d(0,0,1);
		Vector3d shoulderPlaneX = new Vector3d((float)Math.cos(angle0rad),(float)Math.sin(angle0rad),0);
		Vector3d shoulderPlaneY = new Vector3d();
		shoulderPlaneY.cross(shoulderPlaneX, shoulderPlaneZ);
		shoulderPlaneY.normalize();

		// get rotation at bicep
		Vector3d nvx = new Vector3d(shoulderPlaneX);	nvx.scale((float)Math.cos(angle1rad));
		Vector3d nvz = new Vector3d(shoulderPlaneZ);	nvz.scale((float)Math.sin(angle1rad));

		Vector3d bicepPlaneY = new Vector3d(shoulderPlaneY);
		Vector3d bicepPlaneZ = new Vector3d(nvx);
		bicepPlaneZ.add(nvz);
		bicepPlaneZ.normalize();
		Vector3d bicepPlaneX = new Vector3d();
		bicepPlaneX.cross(bicepPlaneZ,bicepPlaneY);
		bicepPlaneX.normalize();

		// shoulder to elbow
		Vector3d vx = new Vector3d(bicepPlaneX);	vx.scale((float)Sixi2Robot.SHOULDER_TO_ELBOW_Y);
		Vector3d vz = new Vector3d(bicepPlaneZ);	vz.scale((float)Sixi2Robot.SHOULDER_TO_ELBOW_Z);
		Vector3d shoulderToElbow = new Vector3d();
		shoulderToElbow.add(vx);
		shoulderToElbow.add(vz);
		Vector3d elbowPosition = new Vector3d(shoulderPosition);
		elbowPosition.add(shoulderToElbow);

		if(gl2!=null) {
			gl2.glColor3f(0,0,0);
			
			gl2.glBegin(GL2.GL_LINE_STRIP);
			gl2.glVertex3d(0,0,0);
			gl2.glVertex3d(shoulderPosition.x, shoulderPosition.y, shoulderPosition.z);
			gl2.glEnd();
			
			// shoulder to elbow
			gl2.glPushMatrix();
			gl2.glTranslated(shoulderPosition.x, shoulderPosition.y, shoulderPosition.z);
			gl2.glBegin(GL2.GL_LINE_STRIP);
			gl2.glVertex3d(0,0,0);
			gl2.glVertex3d(vz.x,vz.y,vz.z);
			gl2.glVertex3d(vx.x+vz.x,vx.y+vz.y,vx.z+vz.z);
			gl2.glEnd();
			gl2.glPopMatrix();
		}

		// get the matrix at the elbow
		nvx.set(bicepPlaneZ);	nvx.scale((float)Math.cos(angle2rad));
		nvz.set(bicepPlaneX);	nvz.scale((float)Math.sin(angle2rad));

		Vector3d elbowPlaneY = new Vector3d(shoulderPlaneY);
		Vector3d elbowPlaneZ = new Vector3d(nvx);
		elbowPlaneZ.add(nvz);
		elbowPlaneZ.normalize();
		Vector3d elbowPlaneX = new Vector3d();
		elbowPlaneX.cross(elbowPlaneZ,elbowPlaneY);
		elbowPlaneX.normalize();

		// get elbow to ulna
		vx.set(elbowPlaneX);	vx.scale((float)Sixi2Robot.ELBOW_TO_ULNA_Y);
		vz.set(elbowPlaneZ);	vz.scale((float)Sixi2Robot.ELBOW_TO_ULNA_Z);
		Vector3d ulnaPosition = new Vector3d(elbowPosition);
		ulnaPosition.add(vx);
		ulnaPosition.add(vz);

		if(gl2!=null) {
			// elbow to ulna
			gl2.glPushMatrix();
			gl2.glTranslated(elbowPosition.x, elbowPosition.y, elbowPosition.z);
			gl2.glColor3f(0,0,0);
			gl2.glBegin(GL2.GL_LINE_STRIP);
			gl2.glVertex3d(0,0,0);
			gl2.glVertex3d(vz.x,vz.y,vz.z);
			gl2.glVertex3d(vx.x+vz.x,vx.y+vz.y,vx.z+vz.z);
			gl2.glEnd();
			gl2.glPopMatrix();
		}

		// get matrix of ulna rotation
		Vector3d ulnaPlaneZ = new Vector3d(elbowPlaneX);
		Vector3d ulnaPlaneX = new Vector3d();
		vx.set(elbowPlaneZ);	vx.scale((float)Math.cos(angle3rad));
		vz.set(elbowPlaneY);	vz.scale((float)Math.sin(angle3rad));
		ulnaPlaneX.add(vx);
		ulnaPlaneX.add(vz);
		ulnaPlaneX.normalize();
		Vector3d ulnaPlaneY = new Vector3d();
		ulnaPlaneY.cross(ulnaPlaneX, ulnaPlaneZ);
		ulnaPlaneY.normalize();

		Vector3d ulnaToWrist = new Vector3d(ulnaPlaneZ);
		ulnaToWrist.scale((float)Sixi2Robot.ULNA_TO_WRIST_Y);
		Vector3d wristPosition = new Vector3d(ulnaPosition);
		wristPosition.add(ulnaToWrist);

		// wrist to finger
		vx.set(ulnaPlaneZ);		vx.scale((float)Math.cos(angle4rad));
		vz.set(ulnaPlaneX);		vz.scale((float)Math.sin(angle4rad));
		Vector3d wristToFingerNormalized = new Vector3d();
		wristToFingerNormalized.add(vx);
		wristToFingerNormalized.add(vz);
		wristToFingerNormalized.normalize();
		Vector3d wristToFinger = new Vector3d(wristToFingerNormalized);
		wristToFinger.scale((float)Sixi2Robot.WRIST_TO_TOOL_Z);
		
		Vector3d wristPlaneY = new Vector3d(ulnaPlaneX);
		Vector3d wristPlaneZ = new Vector3d(wristToFingerNormalized);
		Vector3d wristPlaneX = new Vector3d();
		wristPlaneX.cross(wristPlaneY,wristPlaneZ);
		wristPlaneX.normalize();

		// finger rotation
		Vector3d fingerPlaneY = new Vector3d();
		Vector3d fingerPlaneZ = new Vector3d(wristPlaneZ);
		Vector3d fingerPlaneX = new Vector3d();
		vx.set(wristPlaneX);	vx.scale((float)Math.cos(angle5rad));
		vz.set(wristPlaneY);	vz.scale((float)Math.sin(angle5rad));
		fingerPlaneX.add(vx);
		fingerPlaneX.add(vz);
		fingerPlaneX.normalize();
		fingerPlaneY.cross(fingerPlaneZ, fingerPlaneX);
		Vector3d fingerPositionNew = new Vector3d(wristPosition);
		fingerPositionNew.add(wristToFinger);

		// find the UVW rotations for the finger direction
		// I know the fingerPlaneZ is some combination of rotations around World.up, World.forward, and World.right.
		// since we roll U, then V, then W... we have to solve backwards.  First find W, then V, then U.
		
		// Project fingerPlaneZ onto the XY plane (newForward) and find the rotation around World.up
		Vector3d newForward = new Vector3d(fingerPlaneZ);
		Vector3d newRight = new Vector3d(fingerPlaneY);
		double lenW;
		double ikUnew=ikU;
		double ikVnew=ikV;
		double ikWnew=ikW;

		lenW = World.up.dot(newForward);
		if(Math.abs(lenW)>1-MathHelper.EPSILON) {
			// TODO special case straight along the axis, one way or the other.
		} else {
			Vector3d planeOffsetW = new Vector3d(World.up);
			planeOffsetW.scale(lenW);
			Vector3d projectedForward = new Vector3d(newForward);
			projectedForward.sub(planeOffsetW);
			projectedForward.normalize();

			double dotX = World.right.dot(projectedForward); 
			double dotY = World.forward.dot(projectedForward);
			double ikWrad = Math.atan2(dotX, dotY);
			ikWnew = Math.toDegrees(MathHelper.capRotationRadians(ikWrad));

			// Turn the vectors to remove the effect of W rotation.
			// That will give us better results for V and U.
			newForward = MathHelper.rotateAroundAxis(newForward, World.up, (float)-ikWrad);
			newRight = MathHelper.rotateAroundAxis(newRight, World.up, (float)-ikWrad);
		}
		
		// now repeat, solving for V.
		lenW = World.right.dot(newForward);
		if(Math.abs(lenW)>1-MathHelper.EPSILON) {
			// TODO special case straight along the axis, one way or the other.
		} else {
			Vector3d planeOffsetW = new Vector3d(World.right);
			planeOffsetW.scale(lenW);
			Vector3d projectedForward = new Vector3d(newForward);
			projectedForward.sub(planeOffsetW);
			projectedForward.normalize();

			double dotX = World.up.dot(projectedForward); 
			double dotY = World.forward.dot(projectedForward);
			double ikVrad = Math.atan2(-dotX, dotY);
			ikVnew = Math.toDegrees(MathHelper.capRotationRadians(ikVrad));
			
			// Turn the vectors to remove the effect of V rotation.
			// That will give us better results for U.
			newForward = MathHelper.rotateAroundAxis(newForward, World.right, (float)-ikVrad);
			newRight = MathHelper.rotateAroundAxis(newRight, World.right, (float)-ikVrad);
		}
		
		// now repeat, solving for U.  Since newForward started pointing along World.forward, it's probably going to say 1.
		Vector3d projectedForward = new Vector3d(newRight);
		projectedForward.normalize();

		double dotX = World.right.dot(projectedForward); 
		double dotY = World.up.dot(projectedForward);
		double ikUrad = Math.atan2(dotX, -dotY);
		ikUnew = Math.toDegrees(MathHelper.capRotationRadians(ikUrad));
		
		//newForward = MathHelper.rotateAroundAxis(newForward, World.forward, (float)-ikUrad);
		//newRight = MathHelper.rotateAroundAxis(newRight, World.forward, (float)-ikUrad);

		// draw some helpful stuff for solving things.
		if(gl2!=null) {/*
			gl2.glBegin(GL2.GL_LINES);
			gl2.glColor3f(1,1,1);
			gl2.glVertex3d(fingerPositionNew.x, fingerPositionNew.y, fingerPositionNew.z);
			gl2.glVertex3d(	fingerPositionNew.x+newRight.x,
							fingerPositionNew.y+newRight.y,
							fingerPositionNew.z+newRight.z);

			gl2.glColor3f(0.5f,0.5f,0.5f);
			gl2.glVertex3d(fingerPositionNew.x, fingerPositionNew.y, fingerPositionNew.z);
			gl2.glVertex3d(	fingerPositionNew.x+World.right.x,
							fingerPositionNew.y+World.right.y,
							fingerPositionNew.z+World.right.z);

			gl2.glColor3f(0.25f,0.25f,0.25f);
			gl2.glVertex3d(fingerPositionNew.x, fingerPositionNew.y, fingerPositionNew.z);
			gl2.glVertex3d(	fingerPositionNew.x+World.up.x,
							fingerPositionNew.y+World.up.y,
							fingerPositionNew.z+World.up.z);
			gl2.glEnd();*/

			gl2.glBegin(GL2.GL_LINE_STRIP);
			gl2.glColor3f(0,0,0);
			gl2.glVertex3d(ulnaPosition.x, ulnaPosition.y, ulnaPosition.z);
			gl2.glVertex3d(wristPosition.x, wristPosition.y, wristPosition.z);
			gl2.glVertex3d(fingerPositionNew.x, fingerPositionNew.y, fingerPositionNew.z);
			gl2.glEnd();

			MatrixHelper.drawMatrix(gl2,shoulderPosition,bicepPlaneX,bicepPlaneY,bicepPlaneZ);
			MatrixHelper.drawMatrix(gl2,elbowPosition,elbowPlaneX,elbowPlaneY,elbowPlaneZ);
			MatrixHelper.drawMatrix(gl2,ulnaPosition,ulnaPlaneX,ulnaPlaneY,ulnaPlaneZ);
			MatrixHelper.drawMatrix(gl2,wristPosition,wristPlaneX,wristPlaneY,wristPlaneZ);
			MatrixHelper.drawMatrix(gl2,fingerPositionNew,fingerPlaneX,fingerPlaneY,fingerPlaneZ);
		}
		if(renderMode==false) {
			ikW = (float)ikWnew;
			ikV = (float)ikVnew;
			ikU = (float)ikUnew;
			shoulder.set(shoulderPosition);
			bicep.set(shoulderPosition);
			elbow.set(elbowPosition);
			wrist.set(wristPosition);
			fingerPosition.set(fingerPositionNew);  // xyz values used in inverse kinematics
			fingerRight.set(fingerPlaneX);
			fingerForward.set(fingerPlaneZ);
		}
	}
	
	/**
	 * Knowing the position and orientation of the finger, find the angles at each joint.
	 * @return false if successful, true if the IK solution cannot be found.
	 * @param renderMode don't apply math, just visualize the intermediate results
	 * @param gl2 render context must be supplied to visualize results
	 */
	public boolean inverseKinematics(boolean renderMode,GL2 gl2) {
		double ee;
		double xx, yy, angle0new,angle1new,angle2new,angle3new,angle4new,angle5new;
		
		// rotation at finger, bend at picasso box, rotation of tuning fork, then bends down to base.
		// get the finger position
		Vector3d fingerPlaneZ = new Vector3d(fingerForward);
		Vector3d fingerPlaneX = new Vector3d(fingerRight);
		Vector3d fingerPlaneY = new Vector3d();
		fingerPlaneY.cross(fingerPlaneZ, fingerPlaneX);

		// find the wrist position
		Vector3d wristToFinger = new Vector3d(fingerPlaneZ);
		wristToFinger.scale((float)Sixi2Robot.WRIST_TO_TOOL_Z);
		Vector3d wristPosition = new Vector3d(fingerPosition);
		wristPosition.sub(wristToFinger);

		// figure out the shoulder matrix
		Vector3d shoulderPosition = new Vector3d(0,0,(float)(Sixi2Robot.FLOOR_TO_SHOULDER));
		
		if(Math.abs(wristPosition.x)<MathHelper.EPSILON && Math.abs(wristPosition.y)<MathHelper.EPSILON) {
			// Wrist is directly above shoulder, makes calculations hard.
			// TODO figure this out.  Use previous state to guess elbow?
			return false;
		}
		Vector3d shoulderPlaneX = new Vector3d(wristPosition.x,wristPosition.y,0);
		shoulderPlaneX.normalize();
		Vector3d shoulderPlaneZ = new Vector3d(0,0,1);
		Vector3d shoulderPlaneY = new Vector3d();
		shoulderPlaneY.cross(shoulderPlaneX, shoulderPlaneZ);
		shoulderPlaneY.normalize();

		// Find elbow by using intersection of circles (http://mathworld.wolfram.com/Circle-CircleIntersection.html)
		// x = (dd-rr+RR) / (2d)
		Vector3d shoulderToWrist = new Vector3d(wristPosition);
		shoulderToWrist.sub(shoulderPosition);
		double d = shoulderToWrist.length();
		double R = Math.abs(Sixi2Robot.SHOULDER_TO_ELBOW);
		double r = Math.abs(Sixi2Robot.ELBOW_TO_WRIST);
		if( d > R+r ) {
			// impossibly far away
			return false;
		}
		double x = (d*d - r*r + R*R ) / (2*d);
		if( x > R ) {
			// would cause sqrt(-something)
			return false;
		}
		shoulderToWrist.normalize();
		
		Vector3d elbowMidpoint = new Vector3d(shoulderToWrist);
		elbowMidpoint.scale(x);
		elbowMidpoint.add(shoulderPosition);

		Vector3d v1 = new Vector3d();
		double a = Math.sqrt( R*R - x*x );
		v1.cross(shoulderPlaneY, shoulderToWrist);
		Vector3d v1neg = new Vector3d(v1);
		// find both possible intersections of circles
		v1.scale(a);
		v1neg.scale(-a);
		v1.add(elbowMidpoint);
		v1neg.add(elbowMidpoint);

		if(gl2!=null) {			
			//MatrixHelper.drawMatrix2(gl2,fingerPosition,fingerPlaneX,fingerPlaneY,fingerPlaneZ);
			//MatrixHelper.drawMatrix2(gl2,wristPosition,wristPlaneX,wristPlaneY,wristPlaneZ,20);
			
			//gl2.glTranslated(fingerPosition.x, fingerPosition.y, fingerPosition.z);
			gl2.glBegin(GL2.GL_LINES);
			gl2.glColor3f(1,0,1);
			gl2.glVertex3d(elbowMidpoint.x,elbowMidpoint.y,elbowMidpoint.z);
			gl2.glVertex3d(v1.x,v1.y,v1.z);

			gl2.glColor3f(0,1,1);
			gl2.glVertex3d(elbowMidpoint.x,elbowMidpoint.y,elbowMidpoint.z);
			gl2.glVertex3d(v1neg.x,v1neg.y,v1neg.z);
			gl2.glEnd();
		}

		Vector3d elbowPosition = new Vector3d();
		{
			if(v1.z<shoulderPosition.z) {
				elbowPosition.set(v1neg);
			} else if(v1neg.z<shoulderPosition.z) {
				elbowPosition.set(v1);
			} else if(v1neg.z<v1.z) {
				elbowPosition.set(v1);
			} else {
				elbowPosition.set(v1neg);
				/*
				// the closer of the two circles to the previous elbow position is probably the more desirable of the two.
				Vector3d test1 = new Vector3d(elbow);
				test1.sub(v1);
				float test1LenSquared = test1.lengthSquared();
	
				Vector3d test1neg = new Vector3d(elbow);
				test1neg.sub(v1neg);
				float test1negLenSquared = test1neg.lengthSquared();
				
				if(test1LenSquared < test1negLenSquared) {
					elbowPosition.set(v1);				
				} else {
					elbowPosition.set(v1neg);
				}*/
			}
		}

		//----------------------------------
		// PART TWO
		// We have the finger, wrist, elbow, shoulder, and anchor positions.
		// Now get the orientation of each joint as a matrix.
		// Then we'll have everything we need to calculate angles.
		//----------------------------------
		
		// The bone between elbow and wrist is L shaped.  
		// The angles of the triangle implied by the L are important.
		double aa=Math.atan(Math.abs(Sixi2Robot.ELBOW_TO_WRIST_Y/Sixi2Robot.ELBOW_TO_WRIST_Z));
		//double bb=(Math.PI/2)-aa;
		
		Vector3d shoulderToElbow = new Vector3d(elbowPosition);
		shoulderToElbow.sub(shoulderPosition);
		Vector3d bicepPlaneZ = new Vector3d(shoulderToElbow);
		bicepPlaneZ.normalize();

		if(gl2!=null) MatrixHelper.drawMatrix2(gl2,shoulderPosition,shoulderPlaneX,shoulderPlaneY,shoulderPlaneZ);
		
		Vector3d elbowToWrist = new Vector3d(wristPosition);
		elbowToWrist.sub(elbowPosition);
		elbowToWrist.normalize();

		// find the angles pt 1 

		// shoulder
		ee = Math.atan2(shoulderPlaneX.y, shoulderPlaneX.x);
		ee = MathHelper.capRotationRadians(ee);
		angle0new = (float)MathHelper.capRotationDegrees(Math.toDegrees(ee)+180);

		if( angle0new > 270 ) angle0new -= 360;
		if( angle0new <-270 ) angle0new += 360;
		
		// bicep
		xx = shoulderToElbow.z;
		yy = shoulderPlaneX.dot(shoulderToElbow);
		ee = Math.atan2(yy, xx);
		angle1new = (float)MathHelper.capRotationDegrees(Math.toDegrees(ee));

		if( angle1new > 270 ) angle1new -= 360;
		if( angle1new <-270 ) angle1new += 360;

		Vector3d bicepPlaneX = new Vector3d();
		Vector3d bicepPlaneY = new Vector3d(shoulderPlaneY);
		bicepPlaneX.cross(bicepPlaneZ,bicepPlaneY);

		if(gl2!=null) MatrixHelper.drawMatrix2(gl2,elbowPosition,bicepPlaneX,bicepPlaneY,bicepPlaneZ);
		
		// elbow
		xx = elbowToWrist.dot(bicepPlaneZ);
		yy = elbowToWrist.dot(bicepPlaneX);
		ee = Math.atan2(yy, xx);
		double angle2rad=-(ee-aa);
		angle2new = (float)MathHelper.capRotationDegrees(Math.toDegrees(angle2rad));
		
		if( angle2new > 270 ) angle2new -= 360;
		if( angle2new <-270 ) angle2new += 360;

		// the same code that was used in forward kinematics
		// get the matrix at the elbow
		Vector3d nvx = new Vector3d(bicepPlaneX);	nvx.scale((float)Math.cos(angle2rad));
		Vector3d nvz = new Vector3d(bicepPlaneZ);	nvz.scale((float)Math.sin(angle2rad));
		Vector3d elbowPlaneX = new Vector3d(nvx);
		elbowPlaneX.add(nvz);
		elbowPlaneX.normalize();

		Vector3d elbowPlaneZ = new Vector3d();
		Vector3d elbowPlaneY = new Vector3d(bicepPlaneY);
		elbowPlaneZ.cross(elbowPlaneY,elbowPlaneX);
		
		if(gl2!=null) MatrixHelper.drawMatrix2(gl2,elbowPosition,elbowPlaneX,elbowPlaneY,elbowPlaneZ);
		
		// get elbow to ulna
		nvx.set(elbowPlaneX);	nvx.scale(-(float)Sixi2Robot.ELBOW_TO_ULNA_Y);
		nvz.set(elbowPlaneZ);	nvz.scale((float)Sixi2Robot.ELBOW_TO_ULNA_Z);
		Vector3d mid = new Vector3d(elbowPosition);
		mid.add(nvz);
		Vector3d ulnaPosition = new Vector3d(mid);
		ulnaPosition.add(nvx);

		//if(gl2!=null) MatrixHelper.drawMatrix2(gl2,ulnaPosition,elbowPlaneX,elbowPlaneY,elbowPlaneZ,20);

		v1.cross(elbowToWrist,shoulderPlaneY);
		v1.normalize(); 
		Vector3d v2 = new Vector3d();
		v2.cross(shoulderPlaneY,v1);
		v2.normalize();  // normalized version of elbowToWrist 
		
		// ulna matrix
		Vector3d ulnaPlaneX = new Vector3d(elbowPlaneX);
		Vector3d ulnaPlaneY = new Vector3d();
		Vector3d ulnaPlaneZ = new Vector3d();
		
		// I have wristToFinger.  I need wristToFinger projected on the plane elbow-space XY to calculate the angle. 
		double tf = elbowPlaneZ.dot(fingerForward);
		// v0 and fingerForward are normal length.  if they dot to nearly 1, they are colinear.
		// if they are colinear then I have no reference to calculate the angle of the ulna rotation.
		if(tf>=1-MathHelper.EPSILON) {
			return false;
		}

		tf = elbowPlaneX.dot(wristToFinger);
		Vector3d projectionAmount = new Vector3d(elbowPlaneX);
		projectionAmount.scale(tf);
		ulnaPlaneZ.set(wristToFinger);
		ulnaPlaneZ.sub(projectionAmount);
		ulnaPlaneZ.normalize();
		ulnaPlaneY.cross(ulnaPlaneX,ulnaPlaneZ);
		ulnaPlaneY.normalize();

		if(gl2!=null) MatrixHelper.drawMatrix2(gl2,ulnaPosition,ulnaPlaneX,ulnaPlaneY,ulnaPlaneZ,20);

		// TODO wrist may be bending backward.  As it passes the middle a singularity can occur.
		// Compare projected vector to previous frame's projected vector. if the direction is reversed, flip it. 

		// wrist matrix
		Vector3d wristPlaneZ = new Vector3d(wristToFinger);
		Vector3d wristPlaneX = new Vector3d(ulnaPlaneY);
		Vector3d wristPlaneY = new Vector3d();
		wristPlaneZ.normalize();
		wristPlaneX.normalize();
		wristPlaneY.cross(wristPlaneZ,wristPlaneX);
		wristPlaneY.normalize();
		
		if(gl2!=null) MatrixHelper.drawMatrix2(gl2,wristPosition,wristPlaneX,wristPlaneY,wristPlaneZ,20);
		
		// find the angles pt 2
		
		// ulna rotation
		xx = shoulderPlaneY.dot(fingerPlaneZ);  // shoulderPlaneY is the same as elbowPlaneY
		yy = shoulderPlaneZ.dot(fingerPlaneZ);
		ee = Math.atan2(yy, xx);
		double ee1 = Math.atan2(yy, xx);
		double ee2 = Math.atan2(-yy, -xx);
		float angle3a = (float)MathHelper.capRotationDegrees(Math.toDegrees(ee1)-90);
		float angle3b = (float)MathHelper.capRotationDegrees(Math.toDegrees(ee2)-90);
		if(angle3a> 180) angle3a-=360;
		if(angle3a<-180) angle3a+=360;
		if(angle3b> 180) angle3b-=360;
		if(angle3b<-180) angle3b+=360;
		double ada = Math.abs(angle3a - angle3);
		double adb = Math.abs(angle3b - angle3);
		boolean flipWrist = false;
		if( ada < adb ) {
			angle3new = angle3a;
		} else {
			angle3new = angle3b;
			flipWrist=true;
		}
		
		//System.out.print(angle3a+"\t"+angle3b+"\t"+angle3+"\t"+angle3+"\n");
		
		// wrist
		xx = ulnaPlaneX.dot(fingerPlaneZ);
		yy = ulnaPlaneZ.dot(fingerPlaneZ);
		ee = Math.atan2(yy, xx);
		angle4new = (float)MathHelper.capRotationDegrees(Math.toDegrees(ee));

		//System.out.print(xx+"\t"+yy+"\t"+ee+"\t"+aa+"\t"+bb+"\t"+angle4+"\n");
		
		if(flipWrist) {
			angle4new = -angle4new;
		}
		if( angle4new > 270 ) angle4new -= 360;
		if( angle4new <-270 ) angle4new += 360;
		if(Math.abs(angle4new - angle4new)>90) {
			//System.out.println("angle4 jump "+angle4+" vs "+angle4);
		}
		
		// hand		
		xx = wristPlaneY.dot(fingerRight);
		yy = wristPlaneX.dot(fingerRight);
		ee = Math.atan2(yy, xx);

		angle5new = (float)MathHelper.capRotationDegrees(Math.toDegrees(ee));
		if( angle5new > 270 ) angle5new -= 360;
		if( angle5new <-270 ) angle5new += 360;
		
		if(gl2!=null) {			
			//MatrixHelper.drawMatrix2(gl2,fingerPosition,fingerPlaneX,fingerPlaneY,fingerPlaneZ);
			//MatrixHelper.drawMatrix2(gl2,wristPosition,wristPlaneX,wristPlaneY,wristPlaneZ,20);
			
			//gl2.glTranslated(fingerPosition.x, fingerPosition.y, fingerPosition.z);
			gl2.glBegin(GL2.GL_LINE_STRIP);
			gl2.glColor3f(1,1,1);
			gl2.glVertex3d(
					fingerPosition.x,
					fingerPosition.y,
					fingerPosition.z);
			gl2.glVertex3d(wristPosition.x,wristPosition.y,wristPosition.z);
			gl2.glVertex3d(ulnaPosition.x,ulnaPosition.y,ulnaPosition.z);
			gl2.glVertex3d(mid.x,mid.y,mid.z);
			gl2.glVertex3d(elbowPosition.x,elbowPosition.y,elbowPosition.z);
			gl2.glVertex3d(shoulderPosition.x,shoulderPosition.y,shoulderPosition.z);
			gl2.glEnd();
		}
		if(!renderMode) {
			/*
			System.out.print(angle0+"/"+angle0+"\t");
			System.out.print(angle1+"/"+angle1+"\t");
			System.out.print(angle2+"/"+angle2+"\t");
			System.out.print(angle3+"/"+angle3+"\t");
			System.out.print(angle4+"/"+angle4+"\t");
			System.out.print(angle5+"/"+angle5+"\n");
			//*/
			base = new Vector3d(0,0,0);
			shoulder.set(shoulderPosition);
			elbow.set(elbowPosition);
			wrist.set(wristPosition);
			angle0 = -angle0new;
			angle1 =  angle1new;
			angle2 = -angle2new;
			angle3 =  angle3new;
			angle4 =  angle4new;
			angle5 = -angle5new;
		}

		return true;
	}

	// machine specific limits
	public boolean checkAngleLimits() {
		if (angle0 <  Sixi2Robot.MIN_ANGLE_0) { System.out.println("angle0 top "   +angle0);	return false; }
		if (angle0 >  Sixi2Robot.MAX_ANGLE_0) { System.out.println("angle0 bottom "+angle0);	return false; }
		
		if (angle1 <  Sixi2Robot.MIN_ANGLE_1) { System.out.println("angle1 top "   +angle1);	return false; }
		if (angle1 >  Sixi2Robot.MAX_ANGLE_1) { System.out.println("angle1 bottom "+angle1);	return false; }
		
		if (angle2 <  Sixi2Robot.MIN_ANGLE_2) { System.out.println("angle2 top "   +angle2);	return false; }
		if (angle2 >  Sixi2Robot.MAX_ANGLE_2) { System.out.println("angle2 bottom "+angle2);	return false; }
		
		if (angle3 <  Sixi2Robot.MIN_ANGLE_3) { System.out.println("angle3 top "   +angle3);	return false; }
		if (angle3 >  Sixi2Robot.MAX_ANGLE_3) { System.out.println("angle3 bottom "+angle3);	return false; }
		
		if (angle4 <  Sixi2Robot.MIN_ANGLE_4) { System.out.println("angle4 top "   +angle4);	return false; }
		if (angle4 >  Sixi2Robot.MAX_ANGLE_4) { System.out.println("angle4 bottom "+angle4);	return false; }
		
		if (angle5 <  Sixi2Robot.MIN_ANGLE_5) { System.out.println("angle5 top "   +angle5);	return false; }
		if (angle5 >  Sixi2Robot.MAX_ANGLE_5) { System.out.println("angle5 bottom "+angle5);	return false; }

		return true;
	}
}