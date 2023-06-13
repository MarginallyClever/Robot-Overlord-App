package com.marginallyclever.robotoverlord.robots.deltarobot3;

import javax.vecmath.Vector3d;

/**
 * One limb of a rotary-style Delta robot.
 *
 * @author Dan Royer
 * @since 1.7.1
 */
@Deprecated
public class DeltaRobot3Arm {
	private final Vector3d ortho = new Vector3d();
	public final Vector3d shoulder = new Vector3d();
	public final Vector3d elbow = new Vector3d();
	public final Vector3d shoulderToElbow = new Vector3d();
	public final Vector3d wrist = new Vector3d();
	/**
	 * in degrees from the horizontal plane
	 */
	public double angle=0;

	public DeltaRobot3Arm(Vector3d ortho) {
		this.ortho.set(ortho);
	}

	public void updateWrist(Vector3d endEffector) {
		//n1 = n* c + o*s;
		//o1 = n*-s + o*c;
		Vector3d n1 = new Vector3d(ortho.x,ortho.y,0),
				o1 = new Vector3d(-ortho.y,ortho.x,0),
				up = new Vector3d(0,0,1),
				temp = new Vector3d();

		//this.wrist = endEffector + n1*T2W_X + up*T2W_Z + o1*T2W_Y;
		this.wrist.set(n1);
		this.wrist.scale(DeltaRobot3.WRIST_TO_FINGER_X);
		this.wrist.add(endEffector);
		temp.set(up);
		temp.scale(DeltaRobot3.WRIST_TO_FINGER_Z);
		this.wrist.add(temp);
		temp.set(o1);
		temp.scale(DeltaRobot3.WRIST_TO_FINGER_Y);
		this.wrist.sub(temp);
	}

	/**
	 * Recalculate the elbow position based on the wrist position.
	 * Find the angle of the shoulder based on the new elbow position.
	 */
	public void updateShoulderAngle() {
		Vector3d w = new Vector3d(),
				wop = new Vector3d(),
				temp = new Vector3d(),
				r = new Vector3d();

		// project Wrist position Onto Plane of bicep (wop)
		//w = this.wrist - this.shoulder
		w.set(this.wrist);
		w.sub(this.shoulder);

		//a=w | ortho;
		double a = w.dot( ortho );
		//wop = w - (ortho * a);
		temp.set(ortho);
		temp.scale(a);
		wop.set(w);
		wop.sub(temp);

		// we need to find wop-elbow to calculate the angle at the shoulder.
		// wop-elbow is not the same as wrist-elbow.
		double b=Math.sqrt(DeltaRobot3.FOREARM_LENGTH*DeltaRobot3.FOREARM_LENGTH - a*a);
		if(Double.isNaN(b)) throw new AssertionError("unreachable");

		// use intersection of circles to find elbow point.
		//a = (r0*r0 - r1*r1 + d*d ) / (2*d)
		double r1=b;  // circle 1 centers on wrist
		double r0=DeltaRobot3.BICEP_LENGTH;  // circle 0 centers on shoulder
		double d=wop.length();
		// distance along wop to the midpoint between the two possible intersections
		a = ( r0 * r0 - r1 * r1 + d*d ) / ( 2.0f*d );

		// now find the midpoint
		// normalize wop
		//wop /= d;
		wop.scale(1.0f/d);
		//temp=this.shoulder+(wop*a);
		temp.set(wop);
		temp.scale(a);
		temp.add(this.shoulder);
		// with a and r0 we can find h, the distance from midpoint to intersections.
		double hh=Math.sqrt(r0*r0-a*a);
		if(Double.isNaN(hh)) throw new AssertionError("no intersections, too far");
		// get a normal to the line wop in the plane orthogonal to ortho
		r.cross(ortho,wop);
		r.scale(hh);
		this.elbow.set(temp);
		//if(i%2==0) this.elbow.add(r);
		//else
		this.elbow.sub(r);

		temp.sub(this.elbow,this.shoulder);
		double y=-temp.z;
		temp.z=0;
		double x=temp.length();
		// use atan2 to find theta
		if( ( this.shoulderToElbow.dot( temp ) ) < 0 ) x=-x;
		this.angle = Math.toDegrees(Math.atan2(-y,x));
	}

	/**
	 * Build the shoulders and elbows of the arm as though the bicep is horizontal.
	 */
	public void rebuildShoulder() {
		//n1 = n* c + o*s;
		//o1 = n*-s + o*c;
		Vector3d n1 = new Vector3d(ortho.x,ortho.y,0),
				o1 = new Vector3d(-ortho.y,ortho.x,0),
				up = new Vector3d(0,0,1),
				temp = new Vector3d();

		// this.shoulder = n1*BASE_TO_SHOULDER_X + motion_future.base_up*BASE_TO_SHOULDER_Z - o1*BASE_TO_SHOULDER_Y;
		this.shoulder.set(n1);
		this.shoulder.scale(DeltaRobot3.BASE_TO_SHOULDER_X);
		temp.set(up);
		temp.scale(DeltaRobot3.BASE_TO_SHOULDER_Z);
		this.shoulder.add(temp);
		temp.set(o1);
		temp.scale(DeltaRobot3.BASE_TO_SHOULDER_Y);
		this.shoulder.sub(temp);

		// this.elbow = n1*BASE_TO_SHOULDER_X + motion_future.base_up*BASE_TO_SHOULDER_Z - o1*(BASE_TO_SHOULDER_Y+BICEP_LENGTH);
		this.elbow.set(n1);
		this.elbow.scale(DeltaRobot3.BASE_TO_SHOULDER_X);
		temp.set(up);
		temp.scale(DeltaRobot3.BASE_TO_SHOULDER_Z);
		this.elbow.add(temp);
		temp.set(o1);
		temp.scale(DeltaRobot3.BASE_TO_SHOULDER_Y+DeltaRobot3.BICEP_LENGTH);
		this.elbow.sub(temp);
		//this.shoulder.add(this.base);		    

		this.shoulderToElbow.set(o1);
		this.shoulderToElbow.scale(-1);
	}

	public void updateElbowFromAngle() {
		Vector3d bicep = new Vector3d(ortho.y,-ortho.x,0),
				up = new Vector3d(0,0,1);
		double radians = Math.toRadians(this.angle);

		bicep.scale(Math.cos(radians));
		up.scale(Math.sin(radians));
		bicep.add(up);
		bicep.scale(DeltaRobot3.BICEP_LENGTH);
		this.elbow.set(bicep);
		this.elbow.add(this.shoulder);
	}
}