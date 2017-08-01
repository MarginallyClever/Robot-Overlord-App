package com.marginallyclever.robotOverlord.rotaryStewartPlatform;

import javax.vecmath.Vector3f;

import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.robotOverlord.Log;
import com.marginallyclever.robotOverlord.robot.RobotKeyframe;


public class RotaryStewartPlatformKeyframe implements RobotKeyframe {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// angle of rotation
	public RotaryStewartPlatformArm arms[];

	// Relative to base unless otherwise noted.
	public Vector3f relative = new Vector3f();
	public Vector3f fingerPosition = new Vector3f(0,0,0);
	public Vector3f finger_forward = new Vector3f();
	public Vector3f finger_up = new Vector3f();
	public Vector3f finger_left = new Vector3f();
	// rotating the finger tip
	public float rotationAngleU=0;
	public float rotationAngleV=0;
	public float rotationAngleW=0;
	
	public Vector3f base = new Vector3f();  // relative to world
	// base orientation, affects entire arm
	public Vector3f baseForward = new Vector3f();
	public Vector3f baseUp = new Vector3f();
	public Vector3f baseRight = new Vector3f();
	
	// rotating entire robot
	public float basePan=0;
	public float baseTilt=0;

	public boolean isHomed;
	public boolean isHoming;
	public boolean isFollowMode;

	public float speed;

	public RotaryStewartPlatform2Dimensions dimensions;
	
	public RotaryStewartPlatformKeyframe(RotaryStewartPlatform2Dimensions arg0) {
		dimensions = arg0;
		arms = new RotaryStewartPlatformArm[6];
		int i;
		for(i=0;i<6;++i) {
			arms[i] = new RotaryStewartPlatformArm();
		}
		
		// find the starting height of the end effector at home position
		// @TODO: project wrist-on-bicep to get more accurate distance
		float aa=dimensions.BICEP_LENGTH-(dimensions.BASE_TO_SHOULDER_Y-dimensions.WRIST_TO_FINGER_Y);
		float cc=dimensions.FOREARM_LENGTH;
		float bb=(float)Math.sqrt((cc*cc)-(aa*aa));
		aa=this.arms[0].elbow.x-this.arms[0].wrist.x;
		cc=bb;
		bb=(float)Math.sqrt((cc*cc)-(aa*aa));
		this.relative.set(0,0,bb+dimensions.BASE_TO_SHOULDER_Z-dimensions.WRIST_TO_FINGER_Z);
		this.fingerPosition.set(0,0,0);
		this.isHomed = false;
		this.isHoming = false;
		this.isFollowMode = true;
	}
	
	
	public void set(RotaryStewartPlatformKeyframe other) {
		dimensions = other.dimensions;
		
		int i;
		for(i=0;i<6;++i) {
			arms[i].set(other.arms[i]);
		}
		
		relative.set(other.relative);
		fingerPosition.set(other.fingerPosition);
		finger_forward.set(other.finger_forward);
		finger_left.set(other.finger_left);
		finger_up.set(other.finger_up);
		rotationAngleU=other.rotationAngleU;
		rotationAngleV=other.rotationAngleV;
		rotationAngleW=other.rotationAngleW;
		
		base.set(other.base);
		baseForward.set(other.baseForward);
		baseUp.set(other.baseUp);
		baseRight.set(other.baseRight);
		basePan = other.basePan;
		baseTilt = other.baseTilt;
		
		this.isHomed = other.isHomed;
		this.isFollowMode = other.isFollowMode;
		this.isHoming = other.isHoming;
		this.speed = 2;
	}
	 
	

	public void setSpeed(float newSpeed) {
		speed=newSpeed;
	}
	public float getSpeed() {
		return speed;
	}
	
	public void updateFK() {
		Log.error("Forward Kinematics are not implemented yet");
	}
	
	
	/**
	 * Convert cartesian XYZ to robot motor steps.
	 * @return true if successful, false if the IK solution cannot be found.
	 */
	public boolean updateIK() {
		try {
			updateIKEndEffector();
			updateIKWrists();
			updateIKShoulderAngles();
		}
		catch(AssertionError e) {
			return false;
		}
		
		return true;
	}

	protected void updateIKEndEffector() {
		this.finger_forward.set(1,0,0);
		this.finger_left   .set(0,1,0);
		this.finger_up     .set(0,0,1);

		// roll, pitch, then yaw
		this.finger_forward = MathHelper.rotateAroundAxis(this.finger_forward,new Vector3f(1,0,0),(float)Math.toRadians(this.rotationAngleU));
		this.finger_forward = MathHelper.rotateAroundAxis(this.finger_forward,new Vector3f(0,1,0),(float)Math.toRadians(this.rotationAngleV));
		this.finger_forward = MathHelper.rotateAroundAxis(this.finger_forward,new Vector3f(0,0,1),(float)Math.toRadians(this.rotationAngleW));

		this.finger_up      = MathHelper.rotateAroundAxis(this.finger_up,     new Vector3f(1,0,0),(float)Math.toRadians(this.rotationAngleU));
		this.finger_up      = MathHelper.rotateAroundAxis(this.finger_up,     new Vector3f(0,1,0),(float)Math.toRadians(this.rotationAngleV));
		this.finger_up      = MathHelper.rotateAroundAxis(this.finger_up,     new Vector3f(0,0,1),(float)Math.toRadians(this.rotationAngleW));

		this.finger_left    = MathHelper.rotateAroundAxis(this.finger_left,   new Vector3f(1,0,0),(float)Math.toRadians(this.rotationAngleU));
		this.finger_left    = MathHelper.rotateAroundAxis(this.finger_left,   new Vector3f(0,1,0),(float)Math.toRadians(this.rotationAngleV));
		this.finger_left    = MathHelper.rotateAroundAxis(this.finger_left,   new Vector3f(0,0,1),(float)Math.toRadians(this.rotationAngleW));
	}

	protected void updateIKWrists() {
		  Vector3f n1 = new Vector3f(),o1 = new Vector3f(),temp = new Vector3f();
		  float c,s;
		  int i;
		  for(i=0;i<3;++i) {
		    RotaryStewartPlatformArm arma=this.arms[i*2+0];
		    RotaryStewartPlatformArm armb=this.arms[i*2+1];

		    c=(float)Math.cos(i*Math.PI*2.0f/3.0f);
		    s=(float)Math.sin(i*Math.PI*2.0f/3.0f);

		    //n1 = n* c + o*s;
		    n1.set(this.finger_forward);
		    n1.scale(c);
		    temp.set(this.finger_left);
		    temp.scale(s);
		    n1.add(temp);
		    //o1 = n*-s + o*c;
		    o1.set(this.finger_forward);
		    o1.scale(-s);
		    temp.set(this.finger_left);
		    temp.scale(c);
		    o1.add(temp);

		    //arma.wrist = this.finger_tip + n1*T2W_X + this.finger_up*T2W_Z - o1*T2W_Y;
		    //armb.wrist = this.finger_tip + n1*T2W_X + this.finger_up*T2W_Z + o1*T2W_Y;
		    arma.wrist.set(n1);
		    arma.wrist.scale(dimensions.WRIST_TO_FINGER_X);
		    arma.wrist.add(this.fingerPosition);
		    temp.set(this.finger_up);
		    temp.scale(dimensions.WRIST_TO_FINGER_Z);
		    arma.wrist.add(temp);
		    armb.wrist.set(arma.wrist);
		    temp.set(o1);
		    temp.scale(dimensions.WRIST_TO_FINGER_Y);
		    
		    arma.wrist.sub(temp);
		    armb.wrist.add(temp);
		    arma.wrist.z+=relative.z;
		    armb.wrist.z+=relative.z;
		  }
	}
	
	protected void updateIKShoulderAngles() throws AssertionError {
		Vector3f ortho = new Vector3f(),w = new Vector3f(),wop = new Vector3f(),temp = new Vector3f(),r = new Vector3f();
		  float a,b,d,r1,r0,hh,y,x;
		  
		  int i;
		  for(i=0;i<6;++i) {
		    RotaryStewartPlatformArm arm = this.arms[i];
		    
		    // project wrist position onto plane of bicep (wop)
		    ortho.x=(float)Math.cos((i/2)*Math.PI*2.0f/3.0f);
		    ortho.y=(float)Math.sin((i/2)*Math.PI*2.0f/3.0f);
		    ortho.z=0;
		    
		    //w = arm.wrist - arm.shoulder
		    w.set(arm.wrist);
		    w.sub(arm.shoulder);
		    
		    //a=w | ortho;
		    a = w.dot( ortho );
		    //wop = w - (ortho * a);
		    temp.set(ortho);
		    temp.scale(a);
		    wop.set(w);
		    wop.sub(temp);

		    // we need to find wop-elbow to calculate the angle at the shoulder.
		    // wop-elbow is not the same as wrist-elbow.
		    b=(float)Math.sqrt(dimensions.FOREARM_LENGTH*dimensions.FOREARM_LENGTH-a*a);
		    if(Float.isNaN(b)) throw new AssertionError();

		    // use intersection of circles to find elbow point.
		    //a = (r0r0 - r1r1 + d*d ) / (2*d) 
		    r1=b;  // circle 1 centers on wrist
		    r0=dimensions.BICEP_LENGTH;  // circle 0 centers on shoulder
		    d=wop.length();
		    // distance along wop to the midpoint between the two possible intersections
		    a = ( r0 * r0 - r1 * r1 + d*d ) / ( 2.0f*d );

		    // now find the midpoint
		    // normalize wop
		    //wop /= d;
		    wop.scale(1.0f/d);
		    //temp=arm.shoulder+(wop*a);
		    temp.set(wop);
		    temp.scale(a);
		    temp.add(arm.shoulder);
		    // with a and r0 we can find h, the distance from midpoint to intersections.
		    hh=(float)Math.sqrt(r0*r0-a*a);
		    if(Float.isNaN(hh)) throw new AssertionError();
		    // get a normal to the line wop in the plane orthogonal to ortho
		    r.cross(ortho,wop);
		    r.scale(hh);
		    arm.elbow.set(temp);
		    if(i%2==0) arm.elbow.add(r);
		    else       arm.elbow.sub(r);

		    temp.sub(arm.elbow,arm.shoulder);
		    y=-temp.z;
		    temp.z=0;
		    x=temp.length();
		    // use atan2 to find theta
		    if( ( arm.shoulderToElbow.dot( temp ) ) < 0 ) x=-x;
		    arm.angle= (float)Math.toDegrees(Math.atan2(-y,x));
		  }
	}


	public void moveBase(Vector3f dp) {
		base.set(dp);
		rebuildShoulders();
	}
	
	/**
	 * tilt (around x) and then pan (around z) the base of this robot in world space. 
	 * @param pan degrees
	 * @param tilt degrees
	 */
	public void rotateBase(float pan,float tilt) {
		this.basePan=pan;
		this.baseTilt=tilt;
		
		pan = (float)Math.toRadians(pan);
		tilt = (float)Math.toRadians(tilt);
		this.baseForward.y = (float)Math.sin(pan) * (float)Math.cos(tilt);
		this.baseForward.x = (float)Math.cos(pan) * (float)Math.cos(tilt);
		this.baseForward.z =                                 (float)Math.sin(tilt);
		this.baseForward.normalize();
		
		this.baseUp.set(0,0,1);
	
		this.baseRight.cross(this.baseUp,this.baseForward);
		this.baseRight.normalize();
		this.baseUp.cross(this.baseForward,this.baseRight);
		this.baseUp.normalize();
		
		rebuildShoulders();
	}
	
	protected void rebuildShoulders() {
		  Vector3f n1=new Vector3f(),o1=new Vector3f(),temp=new Vector3f();
		  float c,s;
		  int i;
		  for(i=0;i<3;++i) {
		    RotaryStewartPlatformArm arma=this.arms[i*2+0];
		    RotaryStewartPlatformArm armb=this.arms[i*2+1];

		    c=(float)Math.cos(i*(float)Math.PI*2.0f/3.0f);
		    s=(float)Math.sin(i*(float)Math.PI*2.0f/3.0f);

		    //n1 = n* c + o*s;
		    n1.set(this.baseForward);
		    n1.scale(c);
		    temp.set(this.baseRight);
		    temp.scale(s);
		    n1.add(temp);
		    n1.normalize();
		    //o1 = n*-s + o*c;
		    o1.set(this.baseForward);
		    o1.scale(-s);
		    temp.set(this.baseRight);
		    temp.scale(c);
		    o1.add(temp);
		    o1.normalize();
		    //n1.scale(-1);

		    
//		    arma.shoulder = n1*BASE_TO_SHOULDER_X + motion_future.base_up*BASE_TO_SHOULDER_Z - o1*BASE_TO_SHOULDER_Y;
//		    armb.shoulder = n1*BASE_TO_SHOULDER_X + motion_future.base_up*BASE_TO_SHOULDER_Z + o1*BASE_TO_SHOULDER_Y;
		    arma.shoulder.set(n1);
		    arma.shoulder.scale(dimensions.BASE_TO_SHOULDER_X);
		    temp.set(this.baseUp);
		    temp.scale(dimensions.BASE_TO_SHOULDER_Z);
		    arma.shoulder.add(temp);
		    armb.shoulder.set(arma.shoulder);
		    temp.set(o1);
		    temp.scale(dimensions.BASE_TO_SHOULDER_Y);
		    arma.shoulder.sub(temp);
		    armb.shoulder.add(temp);
		    //arma.shoulder.add(this.base);
		    //armb.shoulder.add(this.base);

//		    arma.elbow = n1*BASE_TO_SHOULDER_X + motion_future.base_up*BASE_TO_SHOULDER_Z - o1*(BASE_TO_SHOULDER_Y+BICEP_LENGTH);
//		    armb.elbow = n1*BASE_TO_SHOULDER_X + motion_future.base_up*BASE_TO_SHOULDER_Z + o1*(BASE_TO_SHOULDER_Y+BICEP_LENGTH);
		    arma.elbow.set(n1);
		    arma.elbow.scale(dimensions.BASE_TO_SHOULDER_X);
		    temp.set(this.baseUp);
		    temp.scale(dimensions.BASE_TO_SHOULDER_Z);
		    arma.elbow.add(temp);
		    armb.elbow.set(arma.elbow);
		    temp.set(o1);
		    temp.scale(dimensions.BASE_TO_SHOULDER_Y+dimensions.BICEP_LENGTH);
		    arma.elbow.sub(temp);
		    armb.elbow.add(temp);
		    //arma.shoulder.add(this.base);
		    //armb.shoulder.add(this.base);		    
		    
		    arma.shoulderToElbow.set(o1);
		    arma.shoulderToElbow.scale(-1);
		    armb.shoulderToElbow.set(o1);
		  }
	}
	
	
	//TODO check for collisions with http://geomalgorithms.com/a07-_distance.html#dist3D_Segment_to_Segment ?
	public boolean movePermitted() {/*
		// don't hit floor
		if(state.finger_tip.z<0.25f) {
			return false;
		}
		// don't hit ceiling
		if(state.finger_tip.z>50.0f) {
			return false;
		}

		// check far limit
		Vector3f temp = new Vector3f(state.finger_tip);
		temp.sub(state.shoulder);
		if(temp.length() > 50) return false;
		// check near limit
		if(temp.length() < BASE_TO_SHOULDER_MINIMUM_LIMIT) return false;
*/
		// angle are good?
		if(!checkAngleLimits()) return false;
		// seems doable
		if(!updateIK()) return false;

		// OK
		return true;
	}
	

	public boolean checkAngleLimits() {
		// machine specific limits
		/*
		if (state.angle_0 < -180) return false;
		if (state.angle_0 >  180) return false;
		if (state.angle_2 <  -20) return false;
		if (state.angle_2 >  180) return false;
		if (state.angle_1 < -150) return false;
		if (state.angle_1 >   80) return false;
		if (state.angle_1 < -state.angle_2+ 10) return false;
		if (state.angle_1 > -state.angle_2+170) return false;

		if (state.angle_3 < -180) return false;
		if (state.angle_3 >  180) return false;
		if (state.angle_4 < -180) return false;
		if (state.angle_4 >  180) return false;
		if (state.angle_5 < -180) return false;
		if (state.angle_5 >  180) return false;
		*/
		if(Math.abs(rotationAngleU)>RotaryStewartPlatform.LIMIT_U) return false;
		if(Math.abs(rotationAngleV)>RotaryStewartPlatform.LIMIT_V) return false;
		if(Math.abs(rotationAngleW)>RotaryStewartPlatform.LIMIT_W) return false;
	
		return true;
	}
};
