package com.marginallyclever.evilOverlord;

import javax.vecmath.Vector3f;

/**
 * A snapshot in time of the robot in a given position.  Can run forward or inverse kinematics to calcualte the joint angles and/or the tool position.
 * @author danroyer
 *
 */
class Arm5MotionState {
	// angle of rotation
	float angleE = 0;
	float angleD = 0;
	float angleC = 0;
	float angleB = 0;
	float angleA = 0;

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
	
	
	void set(Arm5MotionState other) {
		angleE = other.angleE;
		angleD = other.angleD;
		angleC = other.angleC;
		angleB = other.angleB;
		angleA = other.angleA;
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
	}
	
	
	// TODO check for collisions with http://geomalgorithms.com/a07-_distance.html#dist3D_Segment_to_Segment ?
	public boolean movePermitted() {
		// don't hit floor
		if(fingerPosition.z<0.25f) {
			return false;
		}
		// don't hit ceiling
		if(fingerPosition.z>50.0f) {
			return false;
		}

		// check far limit
		Vector3f temp = new Vector3f(fingerPosition);
		temp.sub(shoulder);
		if(temp.length() > 50) return false;
		// check near limit
		if(temp.length() < Arm5Robot.BASE_TO_SHOULDER_MINIMUM_LIMIT) return false;

		// seems doable
		if(inverseKinematics()==false) return false;
		// angle are good?
		if(checkAngleLimits()==false) return false;

		// OK
		return true;
	}
	
	
	protected boolean checkAngleLimits() {
		// machine specific limits
		//a
		//if (angleA < -180) return false;
		//if (angleA >  180) return false;
		//b
		if (angleB <      72.90) return false;
		if (angleB >  360-72.90) return false;
		//c
		if (angleC <   50.57) return false;
		if (angleC >  160.31) return false;
		//d
		if (angleD <   87.85) return false;
		if (angleD >  173.60) return false;
		//e
		//if (angleE < 180-165) return false;
		//if (angleE > 180+165) return false;

		return true;
	}
	
	
	/**
	 * Find the arm joint angles that would put the finger at the desired location.
	 * @return 0 if successful, 1 if the IK solution cannot be found.
	 */
	protected boolean inverseKinematics() {
		float a0,a1,a2,a3,a4;
		// if we know the position of the wrist relative to the shoulder
		// we can use intersection of circles to find the elbow.
		// once we know the elbow position we can find the angle of each joint.
		// each angle can be converted to motor steps.

	    // the finger (attachment point for the tool) is a short distance in "front" of the wrist joint
	    Vector3f finger = new Vector3f(fingerPosition);
		wrist.set(fingerForward);
		wrist.scale(-Arm5Robot.WRIST_TO_FINGER);
		wrist.add(finger);
				
	    // use intersection of circles to find two possible elbow points.
	    // the two circles are the bicep (shoulder-elbow) and the ulna (elbow-wrist)
	    // the distance between circle centers is d  
	    Vector3f arm_plane = new Vector3f(wrist.x,wrist.y,0);
	    arm_plane.normalize();
	
	    shoulder.set(arm_plane);
	    shoulder.scale(Arm5Robot.BASE_TO_SHOULDER_X);
	    shoulder.z = Arm5Robot.BASE_TO_SHOULDER_Z;
	    
	    // use intersection of circles to find elbow
	    Vector3f es = new Vector3f(wrist);
	    es.sub(shoulder);
	    float d = es.length();
	    float r1=Arm5Robot.ELBOW_TO_WRIST;  // circle 1 centers on wrist
	    float r0=Arm5Robot.SHOULDER_TO_ELBOW;  // circle 0 centers on shoulder
	    if( d > Arm5Robot.ELBOW_TO_WRIST + Arm5Robot.SHOULDER_TO_ELBOW ) {
	      // The points are impossibly far apart, no solution can be found.
	      return false;  // should this throw an error because it's called from the constructor?
	    }
	    float a = ( r0 * r0 - r1 * r1 + d*d ) / ( 2.0f*d );
	    // find the midpoint
	    Vector3f mid=new Vector3f(es);
	    mid.scale(a/d);
	    mid.add(shoulder);

	    // with a and r0 we can find h, the distance from midpoint to the intersections.
	    float h=(float)Math.sqrt(r0*r0-a*a);
	    // the distance h on a line orthogonal to n and plane_normal gives us the two intersections.
		Vector3f n = new Vector3f(-arm_plane.y,arm_plane.x,0);
		n.normalize();
		Vector3f r = new Vector3f();
		r.cross(n, es);  // check this!
		r.normalize();
		r.scale(h);

		elbow.set(mid);
		elbow.sub(r);
		//Vector3f.add(mid, s, elbow);

		
		// find the angle between elbow-shoulder and the horizontal
		Vector3f bicep_forward = new Vector3f(elbow);
		bicep_forward.sub(shoulder);		  
		bicep_forward.normalize();
		float ax = bicep_forward.dot(arm_plane);
		float ay = bicep_forward.z;
		a1 = (float) -Math.atan2(ay,ax);

		// find the angle between elbow-wrist and the horizontal
		Vector3f ulna_forward = new Vector3f(elbow);
		ulna_forward.sub(wrist);
		ulna_forward.normalize();
		float bx = ulna_forward.dot(arm_plane);
		float by = ulna_forward.z;
		a2 = (float) Math.atan2(by,bx);

		// find the angle of the base
		a0 = (float) Math.atan2(wrist.y,wrist.x);
		

		Vector3f right_uprotated;
		Vector3f forward = new Vector3f(0,0,1);
		Vector3f right = new Vector3f(1,0,0);
		Vector3f up = new Vector3f();
		
		up.cross(forward,right);
		
		//Vector3f of = new Vector3f(forward);
		Vector3f or = new Vector3f(right);
		Vector3f ou = new Vector3f(up);
		
		//result = RotateAroundAxis(right,of,motion_now.iku);
		right_uprotated = Arm5Robot.rotateAroundAxis(right,or,ikv);
		right_uprotated = Arm5Robot.rotateAroundAxis(right_uprotated,ou,ikw);

		Vector3f ulna_normal = new Vector3f();
		ulna_normal.cross(fingerForward,right_uprotated);
		ulna_normal.normalize();
		
		Vector3f ulna_up = new Vector3f();
		ulna_up.cross(ulna_forward,ulna_normal);
		
		Vector3f ffn = new Vector3f(fingerForward);
		ffn.normalize();
		
		// find the angle of the wrist bend
		{
			float dx = -ffn.dot(ulna_forward);
			float dy = ffn.dot(ulna_up);
			a4=(float) Math.atan2(dy,dx);
		}
		
		// find the angle of the ulna rotation
		{
	    
			Vector3f arm_plane_normal = new Vector3f();
			Vector3f arm_up = new Vector3f(0,0,1);
			arm_plane_normal.cross(arm_plane,arm_up);
			
			Vector3f ffn2 = new Vector3f(ulna_forward);
			ffn2.normalize();
			float df= fingerForward.dot(ffn2);
			if(Math.abs(df)<0.999999) {
				Vector3f ulna_up_unrotated = new Vector3f();
				ulna_up_unrotated.cross(ulna_forward,arm_plane_normal);
				
				Vector3f finger_on_ulna = new Vector3f(fingerForward);
				Vector3f temp = new Vector3f(ffn2);
				temp.scale(df);
				finger_on_ulna.sub(temp);
				finger_on_ulna.normalize();
				
				float cy = ulna_normal.dot(finger_on_ulna);
				float cx = ulna_up.dot(finger_on_ulna);
				a3 = (float) -Math.atan2(cy,cx);
			} else {
				a3 = 0;
			}
		}
		
		// all angles are in radians, I want degrees
		angleE=(float) Math.toDegrees(a0);
		angleD=(float) Math.toDegrees(a1);
		angleC=(float) Math.toDegrees(a2);
		angleB=(float) Math.toDegrees(a3);
		angleA=(float) Math.toDegrees(a4);

		return true;
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
		
		Vector3f v0 = new Vector3f(0,0,(float)(Arm5Robot.ANCHOR_ADJUST_Y+Arm5Robot.ANCHOR_TO_SHOULDER_Y));
		Vector3f v1 = new Vector3f((float)Arm5Robot.SHOULDER_TO_BOOM_X*(float)Math.cos(e),
									(float)Arm5Robot.SHOULDER_TO_BOOM_X*(float)Math.sin(e),
									(float)Arm5Robot.SHOULDER_TO_BOOM_Y);
		Vector3f planar = new Vector3f((float)Math.cos(e),(float)Math.sin(e),0);
		planar.normalize();
		Vector3f planarNormal = new Vector3f(-v1.y,v1.x,0);
		planarNormal.normalize();
		Vector3f planarRight = new Vector3f();
		planarRight.cross(planar, planarNormal);
		planarRight.normalize();

		// anchor to shoulder
		shoulder.set(v0);
		
		// shoulder to boom
		v1.add(v0);
		boom.set(v1);
		
		// boom to elbow
		v0.set(v1);
		v1.set(planar);
		v1.scale( (float)( Arm5Robot.BOOM_TO_STICK_Y * Math.cos(d) ) );
		Vector3f v2 = new Vector3f();
		v2.set(planarRight);
		v2.scale( (float)( Arm5Robot.BOOM_TO_STICK_Y * Math.sin(d) ) );
		v1.add(v2);
		v1.add(v0);
		
		elbow.set(v1);
		
		// elbow to wrist
		planar.set(v0);
		planar.sub(v1);
		planar.normalize();
		planarRight.cross(planar, planarNormal);
		planarRight.normalize();
		v0.set(v1);

		v1.set(planar);
		v1.scale( (float)( Arm5Robot.STICK_TO_WRIST_X * Math.cos(c) ) );
		v2.set(planarRight);
		v2.scale( (float)( Arm5Robot.STICK_TO_WRIST_X * Math.sin(c) ) );
		v1.add(v2);
		v1.add(v0);
		
		wrist.set(v1);

		// wrist to finger
		planar.set(v0);
		planar.sub(v1);
		planar.normalize();
		planarRight.cross(planar, planarNormal);
		planarRight.normalize();
		v0.set(v1);

		v1.set(planar);
		v1.scale( (float)( Arm5Robot.WRIST_TO_TOOL_X * Math.cos(b) ) );
		v2.set(planarRight);
		v2.scale( (float)( Arm5Robot.WRIST_TO_TOOL_X * Math.sin(b) ) );
		v1.add(v2);
		v1.add(v0);

		fingerPosition.set(v1);

		// finger rotation
		planarRight.set(planarNormal);
		planarNormal.set(v1);
		planarNormal.sub(v0);
		planarNormal.normalize();
		planar.cross(planarNormal,planarRight);
		v0.set(v1);

		v1.set(planar);
		v1.scale( (float)( Arm5Robot.WRIST_TO_TOOL_Y * Math.cos(a-b) ) );
		v2.set(planarRight);
		v2.scale( (float)( Arm5Robot.WRIST_TO_TOOL_Y * Math.sin(a-b) ) );
		v1.add(v2);
		v1.add(v0);

		fingerForward.set(v1);
		fingerRight.cross(v1, planarNormal);
	}
}