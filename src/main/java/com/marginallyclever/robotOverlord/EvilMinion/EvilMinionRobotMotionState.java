package com.marginallyclever.robotOverlord.evilMinion;

import java.io.Serializable;

import javax.vecmath.Vector3f;

/**
 * A snapshot in time of the robot in a given position.  Can run forward or inverse kinematics to calcualte the joint angles and/or the tool position.
 * @author danroyer
 *
 */
class EvilMinionRobotMotionState implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1012199745425607761L;
	// angle of rotation
	float angleE = 0;
	float angleD = 0;
	float angleC = 0;
	float angleB = 0;
	float angleA = 0;
	float angleServo = 120;
	
	// robot arm coordinates.  Relative to base unless otherwise noted.
	public Vector3f fingerPosition = new Vector3f();
	public Vector3f fingerForward = new Vector3f();
	public Vector3f fingerRight = new Vector3f();
	// finger rotation
	public float iku=0;
	public float ikv=0;
	public float ikw=0;
	// joint locations relative to base
	Vector3f wrist = new Vector3f();
	Vector3f elbow = new Vector3f();
	Vector3f boom = new Vector3f();
	Vector3f shoulder = new Vector3f();
	
	public Vector3f anchorPosition = new Vector3f();  // relative to world
	// base orientation, affects entire arm
	public Vector3f baseForward = new Vector3f();
	public Vector3f baseUp = new Vector3f();
	public Vector3f baseRight = new Vector3f();
	
	// rotating entire robot
	float base_pan=0;
	float base_tilt=0;

	// inverse kinematics visualizations
	Vector3f ik_wrist = new Vector3f();
	Vector3f ik_elbow = new Vector3f();
	Vector3f ik_boom = new Vector3f();
	Vector3f ik_shoulder = new Vector3f();
	float ik_angleE = 0;
	float ik_angleD = 0;
	float ik_angleC = 0;
	float ik_angleB = 0;
	float ik_angleA = 0;
	
	
	void set(EvilMinionRobotMotionState other) {
		angleE = other.angleE;
		angleD = other.angleD;
		angleC = other.angleC;
		angleB = other.angleB;
		angleA = other.angleA;
		angleServo = other.angleServo;
		iku=other.iku;
		ikv=other.ikv;
		ikw=other.ikw;
		fingerForward.set(other.fingerForward);
		fingerRight.set(other.fingerRight);
		fingerPosition.set(other.fingerPosition);
		wrist.set(other.wrist);
		elbow.set(other.elbow);
		boom.set(other.boom);
		shoulder.set(other.shoulder);
		anchorPosition.set(other.anchorPosition);
		baseForward.set(other.baseForward);
		baseUp.set(other.baseUp);
		baseRight.set(other.baseRight);
		base_pan = other.base_pan;
		base_tilt = other.base_tilt;
		
		ik_angleA = other.ik_angleA;
		ik_angleB = other.ik_angleB;
		ik_angleC = other.ik_angleC;
		ik_angleD = other.ik_angleD;
		ik_angleE = other.ik_angleE;

		ik_wrist.set(other.ik_wrist);
		ik_elbow.set(other.ik_elbow);
		ik_boom.set(other.ik_boom);
		ik_shoulder.set(other.ik_shoulder);
	}
	
	
	// TODO check for collisions with http://geomalgorithms.com/a07-_distance.html#dist3D_Segment_to_Segment ?
	public boolean movePermitted() {
		// don't hit floor?
		// don't hit ceiling?

		// check far limit
		// seems doable
		if(!inverseKinematics()) return false;
		// angle are good?
		if(!checkAngleLimits()) return false;

		// OK
		return true;
	}
	
	
	protected boolean checkAngleLimits() {
		// machine specific limits
		//a
		//if (angleA < -180) return false;
		//if (angleA >  180) return false;
		//b
		if (angleB <      72.90) angleB = 72.90f;
		if (angleB >  360-72.90) angleB = 360-72.90f;
		//c
		if (angleC <   50.57) angleC = 50.57f;
		if (angleC >  160.31) angleC = 160.31f;
		//d
		if (angleD <   87.85) angleD = 87.85f;
		if (angleD >  173.60) angleD = 173.60f;
		//e
		//if (angleE < 180-165) return false;
		//if (angleE > 180+165) return false;

		return true;
	}
	
	
	/**
	 * Find the arm joint angles that would put the finger at the desired location.
	 * @return false if successful, true if the IK solution cannot be found.
	 */
	protected boolean inverseKinematics() {
		double aa,bb,cc,dd,ee;
		
		Vector3f v0 = new Vector3f();
		Vector3f v1 = new Vector3f();
		Vector3f v2 = new Vector3f();
		Vector3f planar = new Vector3f();
		Vector3f planeNormal = new Vector3f();
		Vector3f planeRight = new Vector3f(0,0,1);

		// Finger position is never on x=y=0 line, so this is safe.
		planar.set(fingerPosition);
		planar.z=0;
		planar.normalize();
		planeNormal.set(-planar.y,planar.x,0);
		planeNormal.normalize();
		
		// Find E
		ee = Math.atan2(planar.y, planar.x);
		ee = capRotation(ee);
		ik_angleE = (float)Math.toDegrees(ee);

		ik_shoulder.set(0,0,(float)(EvilMinionRobot.ANCHOR_ADJUST_Y+EvilMinionRobot.ANCHOR_TO_SHOULDER_Y));
		ik_boom.set((float)EvilMinionRobot.SHOULDER_TO_BOOM_X*(float)Math.cos(ee),
					(float)EvilMinionRobot.SHOULDER_TO_BOOM_X*(float)Math.sin(ee),
					(float)EvilMinionRobot.SHOULDER_TO_BOOM_Y);
		ik_boom.add(ik_shoulder);
		
		// Find wrist 
		ik_wrist.set(fingerForward);
		ik_wrist.scale(EvilMinionRobot.WRIST_TO_TOOL_X);
		ik_wrist.add(fingerPosition);
				
		// Find elbow by using intersection of circles.
		// http://mathworld.wolfram.com/Circle-CircleIntersection.html
		// x = (dd-rr+RR) / (2d)
		v0.set(ik_wrist);
		v0.sub(ik_boom);
		float d = v0.length();
		float R = (float)Math.abs(EvilMinionRobot.BOOM_TO_STICK_Y);
		float r = (float)Math.abs(EvilMinionRobot.STICK_TO_WRIST_X);
		if( d > R+r ) {
			// impossibly far away
			return false;
		}/*
		if( d < Math.abs(R-r) ) {
			// impossibly close?
			return false;
		}*/
		
		float x = (d*d - r*r + R*R ) / (2*d);
		if( x > R ) {
			// would cause Math.sqrt(a negative number)
			return false;
		}
		v0.normalize();
		ik_elbow.set(v0);
		ik_elbow.scale(x);
		ik_elbow.add(ik_boom);
		// v1 is now at the intersection point between ik_wrist and ik_boom
		float a = (float)( Math.sqrt( R*R - x*x ) );
		v1.cross(planeNormal, v0);
		v1.scale(-a);
		ik_elbow.add(v1);

		// find boom angle (D)
		v0.set(ik_elbow);
		v0.sub(ik_boom);
		x = -planar.dot(v0);
		float y = planeRight.dot(v0);
		dd = Math.atan2(y,x);
		dd = capRotation(dd);
		ik_angleD = (float)Math.toDegrees(dd);
		
		// find elbow angle (C)
		planar.set(v0);
		planar.normalize();
		planeRight.cross(planeNormal,v0);
		planeRight.normalize();
		v0.set(ik_wrist);
		v0.sub(ik_elbow);
		x = -planar.dot(v0);
		y = planeRight.dot(v0);
		cc = Math.atan2(y,x);
		cc = capRotation(cc);
		ik_angleC = (float)Math.toDegrees(cc);
		
		// find wrist angle (B)
		planar.set(ik_wrist);
		planar.sub(ik_elbow);
		planar.normalize();
		planeRight.cross(planeNormal,v0);
		planeRight.normalize();
		v0.set(fingerPosition);
		v0.sub(ik_wrist);
		x = -planar.dot(v0);
		y = -planeRight.dot(v0);
		bb = Math.atan2(y,x);
		bb = capRotation(bb);
		ik_angleB = (float)Math.toDegrees(bb);
		
		// find wrist rotation (A)
		v0.set(fingerPosition);
		v0.sub(ik_wrist);
		v0.normalize();
		v1.set(planeNormal);
		v2.cross(planeNormal,v0);
		v0.set(fingerRight);
		
		x = v2.dot(v0);
		y = -v1.dot(v0);
		aa = Math.atan2(y,x)-bb-Math.PI/2.0;
		aa = capRotation(aa);
		ik_angleA = (float)Math.toDegrees(aa);
		
		angleA=ik_angleA;
		angleB=ik_angleB;
		angleC=ik_angleC;
		angleD=ik_angleD;
		angleE=ik_angleE;

		return true;
	}
	
	double capRotation(double aa) {
		while(aa<0) aa += Math.PI*2;
		while(aa>Math.PI*2) aa -= Math.PI*2;
		return aa;
	}
	
	/**
	 * Calculate the finger location from the angles at each joint
	 * @param state
	 */
	protected void forwardKinematics() {
		double e = Math.toRadians(angleE);
		double d = Math.toRadians(180-angleD);
		double c = Math.toRadians(angleC+180);
		double b = Math.toRadians(180-angleB);
		double a = Math.toRadians(angleA);
		
		Vector3f v0 = new Vector3f(0,0,(float)(EvilMinionRobot.ANCHOR_ADJUST_Y+EvilMinionRobot.ANCHOR_TO_SHOULDER_Y));
		Vector3f v1 = new Vector3f((float)EvilMinionRobot.SHOULDER_TO_BOOM_X*(float)Math.cos(e),
									(float)EvilMinionRobot.SHOULDER_TO_BOOM_X*(float)Math.sin(e),
									(float)EvilMinionRobot.SHOULDER_TO_BOOM_Y);
		Vector3f planar = new Vector3f((float)Math.cos(e),(float)Math.sin(e),0);
		planar.normalize();
		Vector3f planeNormal = new Vector3f(-v1.y,v1.x,0);
		planeNormal.normalize();
		Vector3f planarRight = new Vector3f();
		planarRight.cross(planar, planeNormal);
		planarRight.normalize();

		// anchor to shoulder
		shoulder.set(v0);
		
		// shoulder to boom
		v1.add(v0);
		boom.set(v1);
		
		// boom to elbow
		v0.set(v1);
		v1.set(planar);
		v1.scale( (float)( EvilMinionRobot.BOOM_TO_STICK_Y * Math.cos(d) ) );
		Vector3f v2 = new Vector3f();
		v2.set(planarRight);
		v2.scale( (float)( EvilMinionRobot.BOOM_TO_STICK_Y * Math.sin(d) ) );
		v1.add(v2);
		v1.add(v0);
		
		elbow.set(v1);
		
		// elbow to wrist
		planar.set(v0);
		planar.sub(v1);
		planar.normalize();
		planarRight.cross(planar, planeNormal);
		planarRight.normalize();
		v0.set(v1);

		v1.set(planar);
		v1.scale( (float)( EvilMinionRobot.STICK_TO_WRIST_X * Math.cos(c) ) );
		v2.set(planarRight);
		v2.scale( (float)( EvilMinionRobot.STICK_TO_WRIST_X * Math.sin(c) ) );
		v1.add(v2);
		v1.add(v0);
		
		wrist.set(v1);

		// wrist to finger
		planar.set(v0);
		planar.sub(v1);
		planar.normalize();
		planarRight.cross(planar, planeNormal);
		planarRight.normalize();
		v0.set(v1);

		v1.set(planar);
		v1.scale( (float)( EvilMinionRobot.WRIST_TO_TOOL_X * Math.cos(b) ) );
		v2.set(planarRight);
		v2.scale( (float)( EvilMinionRobot.WRIST_TO_TOOL_X * Math.sin(b) ) );
		v1.add(v2);
		v1.add(v0);

		fingerPosition.set(v1);

		// finger rotation
		planarRight.set(planeNormal);
		planeNormal.set(v1);
		planeNormal.sub(v0);
		planeNormal.normalize();
		planar.cross(planeNormal,planarRight);
		v0.set(v1);

		v1.set(planar);
		v1.scale( (float)( EvilMinionRobot.WRIST_TO_TOOL_Y * Math.cos(a-b) ) );
		v2.set(planarRight);
		v2.scale( (float)( EvilMinionRobot.WRIST_TO_TOOL_Y * Math.sin(a-b) ) );
		v1.add(v2);
		v1.normalize();
		
		v0.set(fingerPosition);
		v0.sub(wrist);

		fingerForward.set(planeNormal);
		
		fingerRight.cross(v1, planeNormal);
		fingerRight.normalize();
	}
}