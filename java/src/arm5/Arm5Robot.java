package arm5;

import javax.vecmath.Vector3f;
import javax.media.opengl.GL2;

import java.awt.event.KeyEvent;
import java.nio.FloatBuffer;


public class Arm5Robot 
extends RobotWithSerialConnection {
	//math constants
	final float RAD2DEG = 180.0f/(float)Math.PI;

	private Model anchor = new Model();
	private Model shoulder = new Model();
	private Model shoulder_pinion = new Model();
	private Model boom = new Model();
	private Model stick = new Model();
	private Model wristBone = new Model();
	private Model wristEnd = new Model();
	private Model wristInterior = new Model();
	private Model wristPinion = new Model();
	
	//machine dimensions
	final float BASE_TO_SHOULDER_X   =(5.37f);  // measured in solidworks
	final float BASE_TO_SHOULDER_Z   =(9.55f);  // measured in solidworks
	final float SHOULDER_TO_ELBOW    =(25.0f);
	final float ELBOW_TO_WRIST       =(25.0f);
	final float WRIST_TO_FINGER      =(4.0f);
	final float BASE_TO_SHOULDER_MINIMUM_LIMIT = 7.5f;
	
	final double ANCHOR_ADJUST_Y = 0.64;
	final double ANCHOR_TO_SHOULDER_Y = 3.27;
	final double SHOULDER_TO_PINION_X = -15;
	final double SHOULDER_TO_PINION_Y = -2.28;
	final double SHOULDER_TO_BOOM_X = 8;
	final double SHOULDER_TO_BOOM_Y = 7;
	final double BOOM_TO_STICK_Y = 37;
	final double STICK_TO_WRIST_X = -40.0;
	final double WRIST_TO_PINION_X = 5;
	final double WRIST_TO_PINION_Z = 1.43;
	final float WRIST_TO_TOOL_X = -6.29f;
	final float WRIST_TO_TOOL_Y = 1.0f;
	
	Cylinder [] volumes = new Cylinder[6];
	
	protected Arm5MotionState motionNow = new Arm5MotionState();
	protected Arm5MotionState motionFuture = new Arm5MotionState();
	
	boolean homed = false;
	boolean homing = false;
	boolean follow_mode = false;
	boolean armMoved = false;
	
	// keyboard history
	float aDir = 0.0f;
	float bDir = 0.0f;
	float cDir = 0.0f;
	float dDir = 0.0f;
	float eDir = 0.0f;

	float xDir = 0.0f;
	float yDir = 0.0f;
	float zDir = 0.0f;

	boolean pWasOn=false;
	boolean moveMode=false;
	
	boolean isLoaded=false;
		
	
	public Arm5Robot(String name) {
		super(name);
		
		// set up bounding volumes
		for(int i=0;i<volumes.length;++i) {
			volumes[i] = new Cylinder();
		}
		volumes[0].radius=3.2f;
		volumes[1].radius=3.0f*0.575f;
		volumes[2].radius=2.2f;
		volumes[3].radius=1.15f;
		volumes[4].radius=1.2f;
		volumes[5].radius=1.0f*0.575f;
		
		RotateBase(0,0);
		inverseKinematics(motionNow);
		inverseKinematics(motionFuture);
	}

	
	private void enableFK() {		
		xDir=0;
		yDir=0;
		zDir=0;
	}
	
	private void disableFK() {	
		aDir=0;
		bDir=0;
		cDir=0;
		dDir=0;
		eDir=0;
	}

	public void moveA(float dir) {
		aDir=dir;
		enableFK();
	}

	public void moveB(float dir) {
		bDir=dir;
		enableFK();
	}

	public void moveC(float dir) {
		cDir=dir;
		enableFK();
	}

	public void moveD(float dir) {
		dDir=dir;
		enableFK();
	}

	public void moveE(float dir) {
		eDir=dir;
		enableFK();
	}

	public void moveX(float dir) {
		xDir=dir;
		disableFK();
	}

	public void moveY(float dir) {
		yDir=dir;
		disableFK();
	}

	public void moveZ(float dir) {
		zDir=dir;
		disableFK();
	}
	
	
	// TODO check for collisions with http://geomalgorithms.com/a07-_distance.html#dist3D_Segment_to_Segment ?
	public boolean movePermitted(Arm5MotionState state) {
		// don't hit floor
		if(state.fingerTip.z<0.25f) {
			return false;
		}
		// don't hit ceiling
		if(state.fingerTip.z>50.0f) {
			return false;
		}

		// check far limit
		Vector3f temp = new Vector3f(state.fingerTip);
		temp.sub(state.shoulder);
		if(temp.length() > 50) return false;
		// check near limit
		if(temp.length() < BASE_TO_SHOULDER_MINIMUM_LIMIT) return false;

		// seems doable
		if(inverseKinematics(state)==false) return false;
		// angle are good?
		if(checkAngleLimits(state)==false) return false;

		// OK
		return true;
	}
	
	
	protected boolean checkAngleLimits(Arm5MotionState state) {
		// machine specific limits
		//a
		//if (state.angle_4 < -180) return false;
		//if (state.angle_4 >  180) return false;
		//b
		if (state.angleB <      72.90) return false;
		if (state.angleB >  360-72.90) return false;
		//c
		if (state.angleC <   50.57) return false;
		if (state.angleC >  160.31) return false;
		//d
		if (state.angleD <   87.85) return false;
		if (state.angleD >  173.60) return false;
		//e
		//if (state.angle_0 < 180-165) return false;
		//if (state.angle_0 > 180+165) return false;

		
		return true;
	}
	
	
	/**
	 * Find the arm joint angles that would put the finger at the desired location.
	 * @return 0 if successful, 1 if the IK solution cannot be found.
	 */
	protected boolean inverseKinematics(Arm5MotionState state) {
		float a0,a1,a2,a3,a4;
		// if we know the position of the wrist relative to the shoulder
		// we can use intersection of circles to find the elbow.
		// once we know the elbow position we can find the angle of each joint.
		// each angle can be converted to motor steps.

	    // the finger (attachment point for the tool) is a short distance in "front" of the wrist joint
	    Vector3f finger = new Vector3f(state.fingerTip);
		state.wrist.set(state.fingerForward);
		state.wrist.scale(-WRIST_TO_FINGER);
		state.wrist.add(finger);
				
	    // use intersection of circles to find two possible elbow points.
	    // the two circles are the bicep (shoulder-elbow) and the ulna (elbow-wrist)
	    // the distance between circle centers is d  
	    Vector3f arm_plane = new Vector3f(state.wrist.x,state.wrist.y,0);
	    arm_plane.normalize();
	
	    state.shoulder.set(arm_plane);
	    state.shoulder.scale(BASE_TO_SHOULDER_X);
	    state.shoulder.z = BASE_TO_SHOULDER_Z;
	    
	    // use intersection of circles to find elbow
	    Vector3f es = new Vector3f(state.wrist);
	    es.sub(state.shoulder);
	    float d = es.length();
	    float r1=ELBOW_TO_WRIST;  // circle 1 centers on wrist
	    float r0=SHOULDER_TO_ELBOW;  // circle 0 centers on shoulder
	    if( d > ELBOW_TO_WRIST + SHOULDER_TO_ELBOW ) {
	      // The points are impossibly far apart, no solution can be found.
	      return false;  // should this throw an error because it's called from the constructor?
	    }
	    float a = ( r0 * r0 - r1 * r1 + d*d ) / ( 2.0f*d );
	    // find the midpoint
	    Vector3f mid=new Vector3f(es);
	    mid.scale(a/d);
	    mid.add(state.shoulder);

	    // with a and r0 we can find h, the distance from midpoint to the intersections.
	    float h=(float)Math.sqrt(r0*r0-a*a);
	    // the distance h on a line orthogonal to n and plane_normal gives us the two intersections.
		Vector3f n = new Vector3f(-arm_plane.y,arm_plane.x,0);
		n.normalize();
		Vector3f r = new Vector3f();
		r.cross(n, es);  // check this!
		r.normalize();
		r.scale(h);

		state.elbow.set(mid);
		state.elbow.sub(r);
		//Vector3f.add(mid, s, elbow);

		
		// find the angle between elbow-shoulder and the horizontal
		Vector3f bicep_forward = new Vector3f(state.elbow);
		bicep_forward.sub(state.shoulder);		  
		bicep_forward.normalize();
		float ax = bicep_forward.dot(arm_plane);
		float ay = bicep_forward.z;
		a1 = (float) -Math.atan2(ay,ax);

		// find the angle between elbow-wrist and the horizontal
		Vector3f ulna_forward = new Vector3f(state.elbow);
		ulna_forward.sub(state.wrist);
		ulna_forward.normalize();
		float bx = ulna_forward.dot(arm_plane);
		float by = ulna_forward.z;
		a2 = (float) Math.atan2(by,bx);

		// find the angle of the base
		a0 = (float) Math.atan2(state.wrist.y,state.wrist.x);
		

		Vector3f right_uprotated;
		Vector3f forward = new Vector3f(0,0,1);
		Vector3f right = new Vector3f(1,0,0);
		Vector3f up = new Vector3f();
		
		up.cross(forward,right);
		
		//Vector3f of = new Vector3f(forward);
		Vector3f or = new Vector3f(right);
		Vector3f ou = new Vector3f(up);
		
		//result = RotateAroundAxis(right,of,motion_now.iku);
		right_uprotated = rotateAroundAxis(right,or,motionNow.ikv);
		right_uprotated = rotateAroundAxis(right_uprotated,ou,motionNow.ikw);

		Vector3f ulna_normal = new Vector3f();
		ulna_normal.cross(state.fingerForward,right_uprotated);
		ulna_normal.normalize();
		
		Vector3f ulna_up = new Vector3f();
		ulna_up.cross(ulna_forward,ulna_normal);
		
		Vector3f ffn = new Vector3f(state.fingerForward);
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
			float df= motionNow.fingerForward.dot(ffn2);
			if(Math.abs(df)<0.999999) {
				Vector3f ulna_up_unrotated = new Vector3f();
				ulna_up_unrotated.cross(ulna_forward,arm_plane_normal);
				
				Vector3f finger_on_ulna = new Vector3f(motionNow.fingerForward);
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
		state.angleE=a0 * RAD2DEG;
		state.angleD=a1 * RAD2DEG;
		state.angleC=a2 * RAD2DEG;
		state.angleB=a3 * RAD2DEG;
		state.angleA=a4 * RAD2DEG;

		return true;
	}

	
	
	/**
	 * update the desired finger location
	 * @param delta
	 */
	protected void updateFingerForInverseKinematics(float delta) {
		boolean changed=false;
		motionFuture.fingerTip.set(motionNow.fingerTip);
		final float vel=5.0f;
		float dp = vel * delta;

		if (xDir!=0) {
			motionFuture.fingerTip.x += xDir * dp;
			changed=true;
			xDir=0;
		}		
		if (yDir!=0) {
			motionFuture.fingerTip.y += yDir * dp;
			changed=true;
			yDir=0;
		}
		if (zDir!=0) {
			motionFuture.fingerTip.z += zDir * dp;
			changed=true;
			zDir=0;
		}

		// rotations
		float ru=0,rv=0,rw=0;
		//if(uDown) rw= 0.1f;
		//if(jDown) rw=-0.1f;
		//if(aPos) rv=0.1f;
		//if(aNeg) rv=-0.1f;
		//if(bPos) ru=0.1f;
		//if(bNeg) ru=-0.1f;

		//if(rw!=0 || rv!=0 || ru!=0 )
		{
			// On a 3-axis robot when homed the forward axis of the finger tip is pointing downward.
			// More complex arms start from the same assumption.
			Vector3f forward = new Vector3f(0,0,1);
			Vector3f right = new Vector3f(1,0,0);
			Vector3f up = new Vector3f();
			
			up.cross(forward,right);
			
			Vector3f of = new Vector3f(forward);
			Vector3f or = new Vector3f(right);
			Vector3f ou = new Vector3f(up);
			
			motionFuture.iku+=ru*dp;
			motionFuture.ikv+=rv*dp;
			motionFuture.ikw+=rw*dp;
			
			Vector3f result;

			result = rotateAroundAxis(forward,of,motionFuture.iku);  // TODO rotating around itself has no effect.
			result = rotateAroundAxis(result,or,motionFuture.ikv);
			result = rotateAroundAxis(result,ou,motionFuture.ikw);
			motionFuture.fingerForward.set(result);

			result = rotateAroundAxis(right,of,motionFuture.iku);
			result = rotateAroundAxis(result,or,motionFuture.ikv);
			result = rotateAroundAxis(result,ou,motionFuture.ikw);
			motionFuture.fingerRight.set(result);
			
			changed=true;
		}
		
		if(changed==true && movePermitted(motionFuture)) {
			if(motionNow.fingerTip.epsilonEquals(motionFuture.fingerTip,0.1f)) {
				armMoved=true;
			}
		} else {
			motionFuture.fingerTip.set(motionNow.fingerTip);
		}
	}
	
		
	/**
	 * Rotate the point xyz around the line passing through abc with direction uvw
	 * http://inside.mines.edu/~gmurray/ArbitraryAxisRotation/ArbitraryAxisRotation.html
	 * Special case where abc=0
	 * @param vec
	 * @param axis
	 * @param angle
	 * @return
	 */
	protected Vector3f rotateAroundAxis(Vector3f vec,Vector3f axis,float angle) {
		float C = (float)Math.cos(angle);
		float S = (float)Math.sin(angle);
		float x = vec.x;
		float y = vec.y;
		float z = vec.z;
		float u = axis.x;
		float v = axis.y;
		float w = axis.z;
		
		// (a*( v*v + w*w) - u*(b*v + c*w - u*x - v*y - w*z))(1.0-C)+x*C+(-c*v + b*w - w*y + v*z)*S
		// (b*( u*u + w*w) - v*(a*v + c*w - u*x - v*y - w*z))(1.0-C)+y*C+( c*u - a*w + w*x - u*z)*S
		// (c*( u*u + v*v) - w*(a*v + b*v - u*x - v*y - w*z))(1.0-C)+z*C+(-b*u + a*v - v*x + u*y)*S
		// but a=b=c=0 so
		// x' = ( -u*(- u*x - v*y - w*z)) * (1.0-C) + x*C + ( - w*y + v*z)*S
		// y' = ( -v*(- u*x - v*y - w*z)) * (1.0-C) + y*C + ( + w*x - u*z)*S
		// z' = ( -w*(- u*x - v*y - w*z)) * (1.0-C) + z*C + ( - v*x + u*y)*S
		
		float a = (-u*x - v*y - w*z);

		return new Vector3f( (-u*a) * (1.0f-C) + x*C + ( -w*y + v*z)*S,
							 (-v*a) * (1.0f-C) + y*C + (  w*x - u*z)*S,
							 (-w*a) * (1.0f-C) + z*C + ( -v*x + u*y)*S);
	}
	
	
	/**
	 * Calculate the finger location from the angles at each joint
	 * @param state
	 */
	protected void forwardKinematics(Arm5MotionState state) {
		Vector3f arm_plane = new Vector3f((float)Math.cos(state.angleE/RAD2DEG),
					  					  (float)Math.sin(state.angleE/RAD2DEG),
					  					  0);
		state.shoulder.set(arm_plane.x*BASE_TO_SHOULDER_X,
						   arm_plane.y*BASE_TO_SHOULDER_X,
						               BASE_TO_SHOULDER_Z);
		
		state.elbow.set(arm_plane.x*(float)Math.cos(-state.angleD/RAD2DEG)*SHOULDER_TO_ELBOW,
						arm_plane.y*(float)Math.cos(-state.angleD/RAD2DEG)*SHOULDER_TO_ELBOW,
									(float)Math.sin(-state.angleD/RAD2DEG)*SHOULDER_TO_ELBOW);
		state.elbow.add(state.shoulder);

		state.wrist.set(arm_plane.x*(float)Math.cos(state.angleC/RAD2DEG)*-ELBOW_TO_WRIST,
				 		arm_plane.y*(float)Math.cos(state.angleC/RAD2DEG)*-ELBOW_TO_WRIST,
				 					(float)Math.sin(state.angleC/RAD2DEG)*-ELBOW_TO_WRIST);
		state.wrist.add(state.elbow);
		
		// build the axies around which we will rotate the tip
		Vector3f fn = new Vector3f();
		Vector3f up = new Vector3f(0,0,1);
		fn.cross(arm_plane,up);
		Vector3f axis = new Vector3f(state.wrist);
		axis.sub(state.elbow);
		axis.normalize();
		fn = rotateAroundAxis(fn, axis, -state.angleB/RAD2DEG);
		up = rotateAroundAxis(up, axis, -state.angleB/RAD2DEG);

		state.fingerTip.set(arm_plane);
		state.fingerTip = rotateAroundAxis(state.fingerTip, axis,-state.angleB/RAD2DEG); 
		state.fingerTip = rotateAroundAxis(state.fingerTip, fn,-state.angleA/RAD2DEG);
		state.fingerTip.scale(WRIST_TO_FINGER);
		state.fingerTip.add(state.wrist);

		state.fingerForward.set(state.fingerTip);
		state.fingerForward.sub(state.wrist);
		state.fingerForward.normalize();
		
		state.fingerRight.set(up); 
		state.fingerRight.scale(-1);
		state.fingerRight = rotateAroundAxis(state.fingerRight, fn,-state.angleA/RAD2DEG); 
	}
	
	
	
	protected void updateFKAngles(float delta) {
		boolean changed=false;
		float velcd=1.0f;
		float velabe=1.0f;
		
		float dE = motionFuture.angleE;
		float dD = motionFuture.angleD;
		float dC = motionFuture.angleC;
		float dB = motionFuture.angleB;
		float dA = motionFuture.angleA;

		if (eDir!=0) {
			dE += velabe * eDir;
			changed=true;
			eDir=0;
		}
		
		if (dDir!=0) {
			dD += velcd * dDir;
			changed=true;
			dDir=0;
		}

		if (cDir!=0) {
			dC += velcd * cDir;
			changed=true;
			cDir=0;
		}
		
		if(bDir!=0) {
			dB += velabe * bDir;
			changed=true;
			bDir=0;
		}
		
		if(aDir!=0) {
			dA += velabe * aDir;
			changed=true;
			aDir=0;
		}
		

		if(changed==true) {
			//if(CheckAngleLimits(motion_future)) 
			{
				this.sendCommand("R0"
								+(dA==motionFuture.angleA?"":" A"+dA)
								+(dB==motionFuture.angleB?"":" B"+dB)
								+(dC==motionFuture.angleC?"":" C"+dC)
								+(dD==motionFuture.angleD?"":" D"+dD)
								+(dE==motionFuture.angleE?"":" E"+dE)
								);
				
				forwardKinematics(motionFuture);
				armMoved=true;
			}
		}
	}

	
	protected void keyAction(KeyEvent e,boolean state) {
		/*
		switch(e.getKeyCode()) {
		case KeyEvent.VK_R: rDown=state;  break;
		case KeyEvent.VK_F: fDown=state;  break;
		case KeyEvent.VK_T: tDown=state;  break;
		case KeyEvent.VK_G: gDown=state;  break;
		case KeyEvent.VK_Y: yDown=state;  break;
		case KeyEvent.VK_H: hDown=state;  break;
		case KeyEvent.VK_U: uDown=state;  break;
		case KeyEvent.VK_J: jDown=state;  break;
		case KeyEvent.VK_I: iDown=state;  break;
		case KeyEvent.VK_K: kDown=state;  break;
		case KeyEvent.VK_O: oDown=state;  break;
		case KeyEvent.VK_L: lDown=state;  break;
		case KeyEvent.VK_P: pDown=state;  break;
		}*/
	}

	
	
	public void keyPressed(KeyEvent e) {
		keyAction(e,true);
   	}
	
	
	public void keyReleased(KeyEvent e) {
		keyAction(e,false);
	}
	
	
	public void PrepareMove(float delta) {
		if(moveMode) updateFingerForInverseKinematics(delta);
		else		 updateFKAngles(delta);
	}
	
	
	public void finalizeMove() {
		// copy motion_future to motion_now
		motionNow.set(motionFuture);
		
		if(armMoved) {
			if(homed && follow_mode && this.readyForCommands() ) {
				armMoved=false;
				this.deleteAllQueuedCommands();
				//this.SendCommand("G0 X"+motion_now.finger_tip.x+" Y"+motion_now.finger_tip.y+" Z"+motion_now.finger_tip.z);
			}
		}
	}
	

	protected void setColor(GL2 gl2,float r,float g,float b,float a) {
		float [] mat_diffuse = { r,g,b,a};
		gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, mat_diffuse,0);

	    gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, mat_diffuse,0);
	    float[] emission={0.01f,0.01f,0.01f,1f};
	    gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION, emission,0);
	    
	    gl2.glMaterialf(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, 50.0f);

	    gl2.glColor4f(r,g,b,a);
	}
	
	
	public void loadModels(GL2 gl2) {
		anchor.loadFromZip(gl2,"ArmParts.zip","anchor.STL");
		shoulder.loadFromZip(gl2,"ArmParts.zip","shoulder1.STL");
		shoulder_pinion.loadFromZip(gl2,"ArmParts.zip","shoulder_pinion.STL");
		boom.loadFromZip(gl2,"ArmParts.zip","boom.STL");
		stick.loadFromZip(gl2,"ArmParts.zip","stick.STL");
		wristBone.loadFromZip(gl2,"ArmParts.zip","wrist_bone.STL");
		wristEnd.loadFromZip(gl2,"ArmParts.zip","wrist_end.STL");
		wristInterior.loadFromZip(gl2,"ArmParts.zip","wrist_interior.STL");
		wristPinion.loadFromZip(gl2,"ArmParts.zip","wrist_pinion.STL");
	}
	
	
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
		renderFKModels(gl2);
		gl2.glPopMatrix();
	}
	
	
	protected void renderFKModels(GL2 gl2) {
		if(isLoaded==false) {
			loadModels(gl2);
			isLoaded=true;
		}

		// anchor
		setColor(gl2,1,1,1,1);
		gl2.glRotated(90, 1, 0, 0);
		gl2.glTranslated(0, ANCHOR_ADJUST_Y, 0);
		anchor.render(gl2);

		// shoulder (E)
		setColor(gl2,1,0,0,1);
		gl2.glTranslated(0, ANCHOR_TO_SHOULDER_Y, 0);
		gl2.glRotated(155-motionNow.angleE,0,1,0);
//		gl2.glRotated(motion_now.base_pan,1,0,0);
		shoulder.render(gl2);

		// shoulder pinion
		setColor(gl2,0,1,0,1);
		gl2.glPushMatrix();
		gl2.glTranslated(SHOULDER_TO_PINION_X, SHOULDER_TO_PINION_Y, 0);
		double anchor_gear_ratio = 80.0/8.0;
		gl2.glRotated(motionNow.angleE*anchor_gear_ratio,0,1,0);
		shoulder_pinion.render(gl2);
		gl2.glPopMatrix();

		// boom (D)
		setColor(gl2,0,0,1,1);
		gl2.glTranslated(SHOULDER_TO_BOOM_X,SHOULDER_TO_BOOM_Y, 0);
		gl2.glRotated(90-motionNow.angleD-90,0,0,1);
		gl2.glPushMatrix();
		gl2.glScaled(-1,1,1);
		boom.render(gl2);
		gl2.glPopMatrix();

		// stick (C)
		setColor(gl2,1,0,1,1);
		gl2.glTranslated(0.0, BOOM_TO_STICK_Y, 0);
		gl2.glRotated(90+motionNow.angleC-90,0,0,1);
		gl2.glPushMatrix();
		gl2.glScaled(1,-1,1);
		stick.render(gl2);
		gl2.glPopMatrix();

		// to center of wrist
		gl2.glTranslated(STICK_TO_WRIST_X, 0.0, 0);

		// Gear A
		setColor(gl2,1,1,0,1);
		gl2.glPushMatrix();
		gl2.glRotated(180+motionNow.angleA,0,0,1);
		gl2.glRotated(90, 1, 0, 0);
		wristInterior.render(gl2);
		gl2.glPopMatrix();

		// Gear B
		setColor(gl2,0,0,1,1);
		gl2.glPushMatrix();
		gl2.glRotated(180-motionNow.angleB*2.0-motionNow.angleA,0,0,1);
		gl2.glRotated(-90, 1, 0, 0);
		wristInterior.render(gl2);
		gl2.glPopMatrix();

		gl2.glPushMatrix();  // wrist

			gl2.glRotated(-motionNow.angleB+180,0,0,1);
			
			// wrist bone
			setColor(gl2,0.5f,1,0,1);
			wristBone.render(gl2);
				
			// tool holder
			gl2.glRotated(motionNow.angleB+motionNow.angleA-78,1,0,0);  // Why is this -78 here?
			setColor(gl2,0,1,0,1);
			gl2.glPushMatrix();
			wristEnd.render(gl2);
			gl2.glPopMatrix();

			// finger tip
			boolean lightOn= gl2.glIsEnabled(GL2.GL_LIGHTING);
			boolean matCoOn= gl2.glIsEnabled(GL2.GL_COLOR_MATERIAL);
			gl2.glDisable(GL2.GL_LIGHTING);
			gl2.glDisable(GL2.GL_COLOR_MATERIAL);
			setColor(gl2,1,1,1,1);
			PrimitiveSolids.drawStar(gl2, new Vector3f(WRIST_TO_TOOL_X,0,0));
			PrimitiveSolids.drawStar(gl2, new Vector3f(WRIST_TO_TOOL_X,WRIST_TO_TOOL_Y,0));
			if(lightOn) gl2.glEnable(GL2.GL_LIGHTING);
			if(matCoOn) gl2.glEnable(GL2.GL_COLOR_MATERIAL);
		
		gl2.glPopMatrix();  // wrist

		// pinions
		setColor(gl2,0,0,1,1);
		gl2.glPushMatrix();
		gl2.glTranslated(WRIST_TO_PINION_X, 0, -WRIST_TO_PINION_Z);
		gl2.glRotated((motionNow.angleB*2+motionNow.angleA)*24.0/8.0, 0,0,1);
		wristPinion.render(gl2);
		gl2.glPopMatrix();

		setColor(gl2,1,1,0,1);
		gl2.glPushMatrix();
		gl2.glTranslated(WRIST_TO_PINION_X, 0, WRIST_TO_PINION_Z);
		gl2.glScaled(1,1,-1);
		gl2.glRotated((-motionNow.angleA)*24.0/8.0, 0,0,1);
		wristPinion.render(gl2);
		gl2.glPopMatrix();
	}
	
	
	protected void drawMatrix(GL2 gl2,Vector3f p,Vector3f u,Vector3f v,Vector3f w) {
		drawMatrix(gl2,p,u,v,w,1);
	}
	
	
	protected void drawMatrix(GL2 gl2,Vector3f p,Vector3f u,Vector3f v,Vector3f w,float scale) {
		gl2.glPushMatrix();
		gl2.glDisable(GL2.GL_DEPTH_TEST);
		gl2.glTranslatef(p.x, p.y, p.z);
		gl2.glScalef(scale, scale, scale);
		
		gl2.glBegin(GL2.GL_LINES);
		gl2.glColor3f(1,1,0);		gl2.glVertex3f(0,0,0);		gl2.glVertex3f(u.x,u.y,u.z);
		gl2.glColor3f(0,1,1);		gl2.glVertex3f(0,0,0);		gl2.glVertex3f(v.x,v.y,v.z);
		gl2.glColor3f(1,0,1);		gl2.glVertex3f(0,0,0);		gl2.glVertex3f(w.x,w.y,w.z);
		gl2.glEnd();
		
		gl2.glEnable(GL2.GL_DEPTH_TEST);
		gl2.glPopMatrix();
	}
	
	
	protected void drawBounds(GL2 gl2) {
		// base
		
		gl2.glPushMatrix();
		gl2.glTranslatef(motionNow.base.x, motionNow.base.y, motionNow.base.z);
		gl2.glRotatef(motionNow.angleE,0,0,1);
		gl2.glColor3f(0,0,1);
		PrimitiveSolids.drawBox(gl2,4,BASE_TO_SHOULDER_X*2,BASE_TO_SHOULDER_Z);
		gl2.glPopMatrix();
		
		//gl2.glDisable(GL2.GL_LIGHTING);

		gl2.glColor3f(0,1,0);	PrimitiveSolids.drawCylinder(gl2,volumes[0]);  // shoulder
		gl2.glColor3f(0,0,1);	PrimitiveSolids.drawCylinder(gl2,volumes[1]);  // bicep
		gl2.glColor3f(0,1,0);	PrimitiveSolids.drawCylinder(gl2,volumes[2]);  // elbow
		gl2.glColor3f(0,0,1);	PrimitiveSolids.drawCylinder(gl2,volumes[3]);  // ulna
		gl2.glColor3f(0,1,0);	PrimitiveSolids.drawCylinder(gl2,volumes[4]);  // wrist
		gl2.glColor3f(0,0,1);	PrimitiveSolids.drawCylinder(gl2,volumes[5]);  // elbow

		//gl2.glEnable(GL2.GL_LIGHTING);
		
		// TODO draw tool here
	}
	
	
	
	private double parseNumber(String str) {
		return (Math.floor(Float.parseFloat(str)*5.0)/5.0);
	}
	
	
	@Override
	// override this method to check that the software is connected to the right type of robot.
	public boolean confirmPort(String preamble) {
		if(!portOpened) return false;
		
		if(preamble.contains("HELLO WORLD! I AM MINION")) {
			portConfirmed=true;

			motionFuture.fingerTip.set(0,0,0);  // HOME_* should match values in robot firmware.
			inverseKinematics(motionFuture);
			finalizeMove();
			this.sendCommand("G91");
			this.sendCommand("R1");
			homing=false;
			homed=true;
			follow_mode=true;
		}
		
		if( portConfirmed == true ) {
			if(preamble.startsWith("A")) {
				String items[] = preamble.split(" ");
				if(items.length >= 5) {
					if(items[0].startsWith("A")) motionFuture.angleA = (float)parseNumber(items[0].substring(1));
					if(items[1].startsWith("B")) motionFuture.angleB = (float)parseNumber(items[1].substring(1));
					if(items[2].startsWith("C")) motionFuture.angleC = (float)parseNumber(items[2].substring(1));
					if(items[3].startsWith("D")) motionFuture.angleD = (float)parseNumber(items[3].substring(1));
					if(items[4].startsWith("E")) motionFuture.angleE = (float)parseNumber(items[4].substring(1));
					motionNow.set(motionFuture);
				}
			}
		}
		return portConfirmed;
	}
	

	public void MoveBase(Vector3f dp) {
		motionFuture.base.set(dp);
	}
	
	
	public void RotateBase(float pan,float tilt) {
		motionFuture.base_pan=pan;
		motionFuture.base_tilt=tilt;
		
		motionFuture.baseForward.y = (float)Math.sin(pan * Math.PI/180.0) * (float)Math.cos(tilt * Math.PI/180.0);
		motionFuture.baseForward.x = (float)Math.cos(pan * Math.PI/180.0) * (float)Math.cos(tilt * Math.PI/180.0);
		motionFuture.baseForward.z =                                        (float)Math.sin(tilt * Math.PI/180.0);
		motionFuture.baseForward.normalize();
		
		motionFuture.baseUp.set(0,0,1);
	
		motionFuture.baseRight.cross(motionFuture.baseForward, motionFuture.baseUp);
		motionFuture.baseRight.normalize();
		motionFuture.baseUp.cross(motionFuture.baseRight, motionFuture.baseForward);
		motionFuture.baseUp.normalize();
	}
	
	
	public BoundingVolume [] GetBoundingVolumes() {
		// shoulder joint
		Vector3f t1=new Vector3f(motionFuture.baseRight);
		t1.scale(volumes[0].radius/2);
		t1.add(motionFuture.shoulder);
		Vector3f t2=new Vector3f(motionFuture.baseRight);
		t2.scale(-volumes[0].radius/2);
		t2.add(motionFuture.shoulder);
		volumes[0].SetP1(GetWorldCoordinatesFor(t1));
		volumes[0].SetP2(GetWorldCoordinatesFor(t2));
		// bicep
		volumes[1].SetP1(GetWorldCoordinatesFor(motionFuture.shoulder));
		volumes[1].SetP2(GetWorldCoordinatesFor(motionFuture.elbow));
		// elbow
		t1.set(motionFuture.baseRight);
		t1.scale(volumes[0].radius/2);
		t1.add(motionFuture.elbow);
		t2.set(motionFuture.baseRight);
		t2.scale(-volumes[0].radius/2);
		t2.add(motionFuture.elbow);
		volumes[2].SetP1(GetWorldCoordinatesFor(t1));
		volumes[2].SetP2(GetWorldCoordinatesFor(t2));
		// ulna
		volumes[3].SetP1(GetWorldCoordinatesFor(motionFuture.elbow));
		volumes[3].SetP2(GetWorldCoordinatesFor(motionFuture.wrist));
		// wrist
		t1.set(motionFuture.baseRight);
		t1.scale(volumes[0].radius/2);
		t1.add(motionFuture.wrist);
		t2.set(motionFuture.baseRight);
		t2.scale(-volumes[0].radius/2);
		t2.add(motionFuture.wrist);
		volumes[4].SetP1(GetWorldCoordinatesFor(t1));
		volumes[4].SetP2(GetWorldCoordinatesFor(t2));
		// finger
		volumes[5].SetP1(GetWorldCoordinatesFor(motionFuture.wrist));
		volumes[5].SetP2(GetWorldCoordinatesFor(motionFuture.fingerTip));
		
		return volumes;
	}
	
	
	Vector3f GetWorldCoordinatesFor(Vector3f in) {
		Vector3f out = new Vector3f(motionFuture.base);
		
		Vector3f tempx = new Vector3f(motionFuture.baseForward);
		tempx.scale(in.x);
		out.add(tempx);

		Vector3f tempy = new Vector3f(motionFuture.baseRight);
		tempy.scale(-in.y);
		out.add(tempy);

		Vector3f tempz = new Vector3f(motionFuture.baseUp);
		tempz.scale(in.z);
		out.add(tempz);
				
		return out;
	}
}
