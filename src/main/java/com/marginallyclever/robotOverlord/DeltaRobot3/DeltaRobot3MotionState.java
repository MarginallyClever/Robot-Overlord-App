package com.marginallyclever.robotOverlord.DeltaRobot3;

import javax.vecmath.Vector3f;


public class DeltaRobot3MotionState {
	// angle of rotation
	DeltaRobot3Arm arms[];

	public static final int NUM_ARMS = 3;

	// Relative to base
	public Vector3f fingerPosition = new Vector3f(0,0,0);

	// base orientation, affects entire arm
	public Vector3f base = new Vector3f();  // relative to world
	public Vector3f base_forward = new Vector3f();
	public Vector3f base_up = new Vector3f();
	public Vector3f base_right = new Vector3f();

	// rotating entire robot
	float basePan=0;
	float baseTilt=0;


	public class DeltaRobot3Arm {
		Vector3f shoulder = new Vector3f();
		Vector3f elbow = new Vector3f();
		Vector3f shoulderToElbow = new Vector3f();
		Vector3f wrist = new Vector3f();

		float angle=0;


		public void set(DeltaRobot3Arm other) {
			shoulder.set(other.shoulder);
			elbow.set(other.elbow);
			shoulderToElbow.set(other.shoulderToElbow);
			wrist.set(other.wrist);

			angle = other.angle;
		}
	};



	public DeltaRobot3MotionState() {
		arms = new DeltaRobot3Arm[NUM_ARMS];
		int i;
		for(i=0;i<NUM_ARMS;++i) {
			arms[i] = new DeltaRobot3Arm();
		}
	}

	public void set(DeltaRobot3MotionState other) {
		fingerPosition.set(other.fingerPosition);
		int i;
		for(i=0;i<NUM_ARMS;++i) {
			arms[i].set(other.arms[i]);
		}
		base.set(other.base);
		base_forward.set(other.base_forward);
		base_up.set(other.base_up);
		base_right.set(other.base_right);
		basePan = other.basePan;
		baseTilt = other.baseTilt;
	}


	public void updateFK() {
	}


	/**
	 * Convert cartesian XYZ to robot motor steps.
	 * @input cartesian coordinates relative to the base
	 * @input results where to put resulting angles after the IK calculation
	 * @return 0 if successful, 1 if the IK solution cannot be found.
	 */
	public boolean updateIK() {
		try {
			updateIKWrists();
			updateIKShoulderAngles();
		}
		catch(AssertionError e) {
			return false;
		}

		return true;
	}


	protected void updateIKWrists() {
		Vector3f n1 = new Vector3f(),o1 = new Vector3f(),temp = new Vector3f();
		float c,s;
		int i;
		for(i=0;i<NUM_ARMS;++i) {
			DeltaRobot3Arm arma=this.arms[i];

			c=(float)Math.cos( (float)Math.PI*2.0f * (i/3.0f - 60.0f/360.0f) );
			s=(float)Math.sin( (float)Math.PI*2.0f * (i/3.0f - 60.0f/360.0f) );

			//n1 = n* c + o*s;
			n1.set(this.base_forward);
			n1.scale(c);
			temp.set(this.base_right);
			temp.scale(s);
			n1.add(temp);
			n1.normalize();
			//o1 = n*-s + o*c;
			o1.set(this.base_forward);
			o1.scale(-s);
			temp.set(this.base_right);
			temp.scale(c);
			o1.add(temp);
			o1.normalize();
			//n1.scale(-1);


			//arma.wrist = this.finger_tip + n1*T2W_X + this.base_up*T2W_Z - o1*T2W_Y;
			//armb.wrist = this.finger_tip + n1*T2W_X + this.base_up*T2W_Z + o1*T2W_Y;
			arma.wrist.set(n1);
			arma.wrist.scale(DeltaRobot3.WRIST_TO_FINGER_X);
			arma.wrist.add(this.fingerPosition);
			temp.set(this.base_up);
			temp.scale(DeltaRobot3.WRIST_TO_FINGER_Z);
			arma.wrist.add(temp);
			temp.set(o1);
			temp.scale(DeltaRobot3.WRIST_TO_FINGER_Y);
			arma.wrist.sub(temp);
		}
	}
	

	protected void updateIKShoulderAngles() throws AssertionError {
		Vector3f ortho = new Vector3f(),w = new Vector3f(),wop = new Vector3f(),temp = new Vector3f(),r = new Vector3f();
		float a,b,d,r1,r0,hh,y,x;

		int i;
		for(i=0;i<NUM_ARMS;++i) {
			DeltaRobot3Arm arm = this.arms[i];

			// project wrist position onto plane of bicep (wop)
			ortho.x=(float)Math.cos( (float)Math.PI*2.0f * (i/3.0f - 60.0f/360.0f) );
			ortho.y=(float)Math.sin( (float)Math.PI*2.0f * (i/3.0f - 60.0f/360.0f) );
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
			b=(float)Math.sqrt(DeltaRobot3.FOREARM_LENGTH*DeltaRobot3.FOREARM_LENGTH-a*a);
			if(Float.isNaN(b)) throw new AssertionError();

			// use intersection of circles to find elbow point.
			//a = (r0r0 - r1r1 + d*d ) / (2*d) 
			r1=b;  // circle 1 centers on wrist
			r0=DeltaRobot3.BICEP_LENGTH;  // circle 0 centers on shoulder
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
			//if(i%2==0) arm.elbow.add(r);
			//else
				arm.elbow.sub(r);

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


	public void rotateBase(float pan,float tilt) {
		this.basePan=pan;
		this.baseTilt=tilt;

		pan = (float)Math.toRadians(pan);
		tilt = (float)Math.toRadians(tilt);
		this.base_forward.y = (float)Math.sin(pan) * (float)Math.cos(tilt);
		this.base_forward.x = (float)Math.cos(pan) * (float)Math.cos(tilt);
		this.base_forward.z =                        (float)Math.sin(tilt);
		this.base_forward.normalize();

		this.base_up.set(0,0,1);

		this.base_right.cross(this.base_up,this.base_forward);
		this.base_right.normalize();
		this.base_up.cross(this.base_forward,this.base_right);
		this.base_up.normalize();

		rebuildShoulders();
	}

	
	protected void rebuildShoulders() {
		Vector3f n1=new Vector3f(),o1=new Vector3f(),temp=new Vector3f();
		float c,s;
		int i;
		for(i=0;i<3;++i) {
			DeltaRobot3Arm arma=this.arms[i];

			c=(float)Math.cos( (float)Math.PI*2.0f * (i/3.0f - 60.0f/360.0f) );
			s=(float)Math.sin( (float)Math.PI*2.0f * (i/3.0f - 60.0f/360.0f) );

			//n1 = n* c + o*s;
			n1.set(this.base_forward);
			n1.scale(c);
			temp.set(this.base_right);
			temp.scale(s);
			n1.add(temp);
			n1.normalize();
			//o1 = n*-s + o*c;
			o1.set(this.base_forward);
			o1.scale(-s);
			temp.set(this.base_right);
			temp.scale(c);
			o1.add(temp);
			o1.normalize();
			//n1.scale(-1);


			//		    arma.shoulder = n1*BASE_TO_SHOULDER_X + motion_future.base_up*BASE_TO_SHOULDER_Z - o1*BASE_TO_SHOULDER_Y;
			arma.shoulder.set(n1);
			arma.shoulder.scale(DeltaRobot3.BASE_TO_SHOULDER_X);
			temp.set(this.base_up);
			temp.scale(DeltaRobot3.BASE_TO_SHOULDER_Z);
			arma.shoulder.add(temp);
			temp.set(o1);
			temp.scale(DeltaRobot3.BASE_TO_SHOULDER_Y);
			arma.shoulder.sub(temp);
			arma.shoulder.add(this.base);

			//		    arma.elbow = n1*BASE_TO_SHOULDER_X + motion_future.base_up*BASE_TO_SHOULDER_Z - o1*(BASE_TO_SHOULDER_Y+BICEP_LENGTH);
			arma.elbow.set(n1);
			arma.elbow.scale(DeltaRobot3.BASE_TO_SHOULDER_X);
			temp.set(this.base_up);
			temp.scale(DeltaRobot3.BASE_TO_SHOULDER_Z);
			arma.elbow.add(temp);
			temp.set(o1);
			temp.scale(DeltaRobot3.BASE_TO_SHOULDER_Y+DeltaRobot3.BICEP_LENGTH);
			arma.elbow.sub(temp);
			//arma.shoulder.add(this.base);		    

			arma.shoulderToElbow.set(o1);
			arma.shoulderToElbow.scale(-1);
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
		return true;
	}
};
