package com.marginallyclever.evilOverlord.RotaryStewartPlatform2;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.media.opengl.GL2;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.vecmath.Vector3f;

import com.marginallyclever.evilOverlord.*;
import com.marginallyclever.evilOverlord.communications.MarginallyCleverConnection;

public class RotaryStewartPlatform2
extends RobotWithSerialConnection
implements PropertyChangeListener
{
	// machine ID
	protected long robotUID;
	protected final static String hello = "HELLO WORLD! I AM STEWART PLATFORM V4.2";
	
	//math constants
	final float RAD2DEG = 180.0f/(float)Math.PI;
	final float DEG2RAD = (float)Math.PI/180.0f;

	//machine dimensions
	final float BASE_TO_SHOULDER_X   =( 8.093f);  // measured in solidworks, relative to base origin
	final float BASE_TO_SHOULDER_Y   =( 2.150f);
	final float BASE_TO_SHOULDER_Z   =( 6.610f);
	final float BICEP_LENGTH         =( 5.000f);
	final float FOREARM_LENGTH       =(16.750f);
	final float WRIST_TO_FINGER_X    =( 7.635f);
	final float WRIST_TO_FINGER_Y    =( 0.553f);
	final float WRIST_TO_FINGER_Z    =(-0.870f);  // measured in solidworks, relative to finger origin
	
	protected float HOME_X = 0.0f;
	protected float HOME_Y = 0.0f;
	protected float HOME_Z = 0.0f;
	protected float HOME_A = 0.0f;
	protected float HOME_B = 0.0f;
	protected float HOME_C = 0.0f;
	
	protected float LIMIT_U=15;
	protected float LIMIT_V=15;
	protected float LIMIT_W=15;
	
	protected float HOME_RIGHT_X = 0;
	protected float HOME_RIGHT_Y = 0;
	protected float HOME_RIGHT_Z = -1;

	protected float HOME_FORWARD_X = 1;
	protected float HOME_FORWARD_Y = 0;
	protected float HOME_FORWARD_Z = 0;

	protected boolean HOME_AUTOMATICALLY_ON_STARTUP = true;
	
	protected Cylinder [] volumes = new Cylinder[6];

	protected boolean isPortConfirmed=false;

	protected Model modelTop = Model.loadModel("/StewartPlatform.zip:top.STL");
	protected Model modelArm = Model.loadModel("/StewartPlatform.zip:arm.STL");
	protected Model modelBase = Model.loadModel("/StewartPlatform.zip:base.STL");
	
	
	class Arm {
		  Vector3f shoulder = new Vector3f();
		  Vector3f elbow = new Vector3f();
		  Vector3f shoulderToElbow = new Vector3f();
		  Vector3f wrist = new Vector3f();

		  float angle=0;
		  

		  public void set(Arm other) {
			  shoulder.set(other.shoulder);
			  elbow.set(other.elbow);
			  shoulderToElbow.set(other.shoulderToElbow);
			  wrist.set(other.wrist);
			  
			  angle = other.angle;
		  }
	};
	
	class RotaryStewartPlatform2MotionState {
		// angle of rotation
		Arm arms[];

		// Relative to base unless otherwise noted.
		public Vector3f finger_tip = new Vector3f(HOME_X,HOME_Y,HOME_Z);
		public Vector3f finger_forward = new Vector3f();
		public Vector3f finger_up = new Vector3f();
		public Vector3f finger_left = new Vector3f();
		// rotating the finger tip
		public float iku=0;
		public float ikv=0;
		public float ikw=0;
		
		public Vector3f base = new Vector3f();  // relative to world
		// base orientation, affects entire arm
		public Vector3f base_forward = new Vector3f();
		public Vector3f base_up = new Vector3f();
		public Vector3f base_right = new Vector3f();
		
		// rotating entire robot
		float base_pan=0;
		float base_tilt=0;

		
		RotaryStewartPlatform2MotionState() {
			arms = new Arm[6];
			int i;
			for(i=0;i<6;++i) {
				arms[i] = new Arm();
			}
		}
		
		void set(RotaryStewartPlatform2MotionState other) {
			iku=other.iku;
			ikv=other.ikv;
			ikw=other.ikw;
			finger_tip.set(other.finger_tip);
			finger_forward.set(other.finger_forward);
			finger_left.set(other.finger_left);
			finger_up.set(other.finger_up);
			int i;
			for(i=0;i<6;++i) {
				arms[i].set(other.arms[i]);
			}
			base.set(other.base);
			base_forward.set(other.base_forward);
			base_up.set(other.base_up);
			base_right.set(other.base_right);
			base_pan = other.base_pan;
			base_tilt = other.base_tilt;
		}
	};
	
	protected RotaryStewartPlatform2MotionState motion_now = new RotaryStewartPlatform2MotionState();
	protected RotaryStewartPlatform2MotionState motion_future = new RotaryStewartPlatform2MotionState();
	
	boolean homed = false;
	boolean homing = false;
	boolean follow_mode = false;
	boolean arm_moved = false;
	
	// keyboard history
	boolean rDown=false;
	boolean fDown=false;
	boolean tDown=false;
	boolean gDown=false;
	boolean yDown=false;
	boolean hDown=false;
	boolean uDown=false;
	boolean jDown=false;
	boolean iDown=false;
	boolean kDown=false;
	boolean oDown=false;
	boolean lDown=false;
	
	boolean pDown=false;
	boolean pWasOn=false;
	boolean moveMode=true;
	

	protected JButton view_home=null, view_go=null;
	protected JLabelledTextField viewPx,viewPy,viewPz,viewRx,viewRy,viewRz;
	
	
	public Vector3f getHome() {  return new Vector3f(HOME_X,HOME_Y,HOME_Z);  }
	
	
	public void setHome(Vector3f newhome) {
		HOME_X=newhome.x;
		HOME_Y=newhome.y;
		HOME_Z=newhome.z;
		RotateBase(motion_future,0f,0f);
		MoveBase(motion_future,new Vector3f(0,0,0));
		moveIfAble();
	}
	
	
	//TODO check for collisions with http://geomalgorithms.com/a07-_distance.html#dist3D_Segment_to_Segment ?
	public boolean movePermitted(RotaryStewartPlatform2MotionState state) {/*
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
		if(CheckAngleLimits(state)==false) return false;
		// seems doable
		if(IK(state)==false) return false;

		// OK
		return true;
	}
	
	
	public RotaryStewartPlatform2(MainGUI gui) {
		super(gui);
		setDisplayName("Rotary Stewart Platform 2");
		
		/*
		// set up bounding volumes
		for(int i=0;i<volumes.length;++i) {
			volumes[i] = new Cylinder();
		}
		volumes[0].radius=3.2f;
		volumes[1].radius=3.0f*0.575f;
		volumes[2].radius=2.2f;
		volumes[3].radius=1.15f;
		volumes[4].radius=1.2f;
		volumes[5].radius=1.0f*0.575f;*/
		
		RotateBase(motion_now,0,0);
		RotateBase(motion_future,0,0);
		  
		IK_update_end_effector(motion_now);
		RebuildShoulders(motion_now);
		IK_update_wrists(motion_now);

		IK_update_end_effector(motion_future);
		RebuildShoulders(motion_future);
		IK_update_wrists(motion_future);
		

		  // find the starting height of the end effector at home position
		  // @TODO: project wrist-on-bicep to get more accurate distance
		  float aa=(motion_now.arms[0].elbow.y-motion_now.arms[0].wrist.y);
		  float cc=FOREARM_LENGTH;
		  float bb=(float)Math.sqrt((cc*cc)-(aa*aa));
		  aa=motion_now.arms[0].elbow.x-motion_now.arms[0].wrist.x;
		  cc=bb;
		  bb=(float)Math.sqrt((cc*cc)-(aa*aa));
		  motion_now.finger_tip.set(0,0,bb+BASE_TO_SHOULDER_Z-WRIST_TO_FINGER_Z);
		  
		  motion_future.finger_tip.set(motion_now.finger_tip);

	}
	

	protected boolean CheckAngleLimits(RotaryStewartPlatform2MotionState state) {
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
		if(Math.abs(state.iku)>LIMIT_U) return false;
		if(Math.abs(state.ikv)>LIMIT_V) return false;
		if(Math.abs(state.ikw)>LIMIT_W) return false;
	
		return true;
	}
	
	
	/**
	 * Convert cartesian XYZ to robot motor steps.
	 * @input cartesian coordinates relative to the base
	 * @input results where to put resulting angles after the IK calculation
	 * @return 0 if successful, 1 if the IK solution cannot be found.
	 */
	protected boolean IK(RotaryStewartPlatform2MotionState state) {
		try {
			IK_update_end_effector(state);
			IK_update_wrists(state);
			IK_update_shoulder_angles(state);
		}
		catch(AssertionError e) {
			return false;
		}
		
		return true;
	}
	
	protected void IK_update_end_effector(RotaryStewartPlatform2MotionState state) {
		  state.finger_forward.set(1,0,0);
		  state.finger_left   .set(0,1,0);
		  state.finger_up     .set(0,0,1);

		  // roll, pitch, then yaw
		  
		  state.finger_forward = RotateAroundAxis(state.finger_forward,new Vector3f(1,0,0),state.iku*DEG2RAD);
		  state.finger_forward = RotateAroundAxis(state.finger_forward,new Vector3f(0,1,0),state.ikv*DEG2RAD);
		  state.finger_forward = RotateAroundAxis(state.finger_forward,new Vector3f(0,0,1),state.ikw*DEG2RAD);

		  state.finger_up      = RotateAroundAxis(state.finger_up,     new Vector3f(1,0,0),state.iku*DEG2RAD);
		  state.finger_up      = RotateAroundAxis(state.finger_up,     new Vector3f(0,1,0),state.ikv*DEG2RAD);
		  state.finger_up      = RotateAroundAxis(state.finger_up,     new Vector3f(0,0,1),state.ikw*DEG2RAD);

		  state.finger_left    = RotateAroundAxis(state.finger_left,   new Vector3f(1,0,0),state.iku*DEG2RAD);
		  state.finger_left    = RotateAroundAxis(state.finger_left,   new Vector3f(0,1,0),state.ikv*DEG2RAD);
		  state.finger_left    = RotateAroundAxis(state.finger_left,   new Vector3f(0,0,1),state.ikw*DEG2RAD);
	}
	
	protected void IK_update_wrists(RotaryStewartPlatform2MotionState state) {
		  Vector3f n1 = new Vector3f(),o1 = new Vector3f(),temp = new Vector3f();
		  float c,s;
		  int i;
		  for(i=0;i<3;++i) {
		    Arm arma=state.arms[i*2+0];
		    Arm armb=state.arms[i*2+1];

		    c=(float)Math.cos(i*Math.PI*2.0f/3.0f);
		    s=(float)Math.sin(i*Math.PI*2.0f/3.0f);

		    //n1 = n* c + o*s;
		    n1.set(state.finger_forward);
		    n1.scale(c);
		    temp.set(state.finger_left);
		    temp.scale(s);
		    n1.add(temp);
		    //o1 = n*-s + o*c;
		    o1.set(state.finger_forward);
		    o1.scale(-s);
		    temp.set(state.finger_left);
		    temp.scale(c);
		    o1.add(temp);

		    //arma.wrist = state.finger_tip + n1*T2W_X + state.finger_up*T2W_Z - o1*T2W_Y;
		    //armb.wrist = state.finger_tip + n1*T2W_X + state.finger_up*T2W_Z + o1*T2W_Y;
		    arma.wrist.set(n1);
		    arma.wrist.scale(WRIST_TO_FINGER_X);
		    arma.wrist.add(state.finger_tip);
		    temp.set(state.finger_up);
		    temp.scale(WRIST_TO_FINGER_Z);
		    arma.wrist.add(temp);
		    armb.wrist.set(arma.wrist);
		    temp.set(o1);
		    temp.scale(WRIST_TO_FINGER_Y);
		    arma.wrist.sub(temp);
		    armb.wrist.add(temp);
		  }
	}
	
	protected void IK_update_shoulder_angles(RotaryStewartPlatform2MotionState state) throws AssertionError {
		Vector3f ortho = new Vector3f(),w = new Vector3f(),wop = new Vector3f(),temp = new Vector3f(),r = new Vector3f();
		  float a,b,d,r1,r0,hh,y,x;
		  
		  int i;
		  for(i=0;i<6;++i) {
		    Arm arm = state.arms[i];
		    
		    // project wrist position onto plane of bicep (wop)
		    ortho.x=(float)Math.cos((int)(i/2)*Math.PI*2.0f/3.0f);
		    ortho.y=(float)Math.sin((int)(i/2)*Math.PI*2.0f/3.0f);
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
		    b=(float)Math.sqrt(FOREARM_LENGTH*FOREARM_LENGTH-a*a);
		    if(Float.isNaN(b)) throw new AssertionError();

		    // use intersection of circles to find elbow point.
		    //a = (r0r0 - r1r1 + d*d ) / (2*d) 
		    r1=b;  // circle 1 centers on wrist
		    r0=BICEP_LENGTH;  // circle 0 centers on shoulder
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
		    arm.angle= (float)Math.atan2(-y,x) * RAD2DEG;
		  }
	}

	
	protected void update_ik(float delta) {
		boolean changed=false;
		motion_future.set(motion_now);
		
		// speeds
		final float vtranslate=10.0f * delta;
		final float vrotate=100.0f * delta;
		
		// lateral moves
		if (rDown) {	motion_future.finger_tip.x -= vtranslate;	}
		if (fDown) {	motion_future.finger_tip.x += vtranslate;	}
		if (tDown) {	motion_future.finger_tip.y += vtranslate;	}
		if (gDown) {	motion_future.finger_tip.y -= vtranslate;	}
		if (yDown) {	motion_future.finger_tip.z += vtranslate;	}
		if (hDown) {	motion_future.finger_tip.z -= vtranslate;	}
		if(!motion_now.finger_tip.epsilonEquals(motion_future.finger_tip,vtranslate/2.0f)) 
		{
			changed=true;
		}
		
		// rotation
		float ru=0,rv=0,rw=0;
		if(uDown) rw= 0.1f;
		if(jDown) rw=-0.1f;
		if(iDown) rv=0.1f;
		if(kDown) rv=-0.1f;
		if(oDown) ru=0.1f;
		if(lDown) ru=-0.1f;

		if(rw!=0 || rv!=0 || ru!=0 )
		{
			motion_future.iku+=ru*vrotate;
			motion_future.ikv+=rv*vrotate;
			motion_future.ikw+=rw*vrotate;
			
			changed=true;
		}

		if(changed==true) {
			moveIfAble();
		}
	}
	
	public void moveIfAble() {
		RotateFinger();	
		
		if(movePermitted(motion_future)) {
			arm_moved=true;
			FinalizeMove();
		} else {
			motion_future.set(motion_now);
		}
	}
	
	
	public void RotateFinger() {
		Vector3f forward = new Vector3f(HOME_FORWARD_X,HOME_FORWARD_Y,HOME_FORWARD_Z);
		Vector3f right = new Vector3f(HOME_RIGHT_X,HOME_RIGHT_Y,HOME_RIGHT_Z);
		Vector3f up = new Vector3f();
		
		up.cross(forward,right);
		
		Vector3f of = new Vector3f(forward);
		Vector3f or = new Vector3f(right);
		Vector3f ou = new Vector3f(up);
		
		Vector3f result;

		result = RotateAroundAxis(forward,of,motion_future.iku*DEG2RAD);  // TODO rotating around itself has no effect.
		result = RotateAroundAxis(result,or,motion_future.ikv*DEG2RAD);
		result = RotateAroundAxis(result,ou,motion_future.ikw*DEG2RAD);
		motion_future.finger_forward.set(result);

		result = RotateAroundAxis(right,of,motion_future.iku*DEG2RAD);
		result = RotateAroundAxis(result,or,motion_future.ikv*DEG2RAD);
		result = RotateAroundAxis(result,ou,motion_future.ikw*DEG2RAD);
		motion_future.finger_left.set(result);
		
		motion_future.finger_up.cross(motion_future.finger_forward,motion_future.finger_left);
	}
	
		
	/**
	 * Rotate the point xyz around the line passing through abc with direction uvw
	 * http://inside.mines.edu/~gmurray/ArbitraryAxisRotation/ArbitraryAxisRotation.html
	 * Special case where abc=0
	 * @param vec
	 * @param axis
	 * @param angle_radians in radians
	 * @return
	 */
	protected Vector3f RotateAroundAxis(Vector3f vec,Vector3f axis,float angle_radians) {
		float C = (float)Math.cos(angle_radians);
		float S = (float)Math.sin(angle_radians);
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
	
	
	protected void FK(RotaryStewartPlatform2MotionState state) {}
	
	
	protected void update_fk(float delta) {
		boolean changed=false;
		final float vel=10.0f;
		int i;
		
		for(i=0;i<6;++i) {
			motion_future.arms[i].angle = motion_now.arms[i].angle;
		}

		if (rDown) {
			motion_future.arms[1].angle -= vel * delta;
			changed=true;
		}
		if (fDown) {
			motion_future.arms[1].angle += vel * delta;
			changed=true;
		}
		if (tDown) {
			motion_future.arms[2].angle += vel * delta;
			changed=true;
		}
		if (gDown) {
			motion_future.arms[2].angle -= vel * delta;
			changed=true;
		}
		if (yDown) {
			motion_future.arms[0].angle += vel * delta;
			changed=true;
		}
		if (hDown) {
			motion_future.arms[0].angle -= vel * delta;
			changed=true;
		}
		if(uDown) {
			motion_future.arms[3].angle += vel * delta;
			changed=true;
		}
		if(jDown) {
			motion_future.arms[3].angle -= vel * delta;
			changed=true;
		}
		if(iDown) {
			motion_future.arms[4].angle += vel * delta;
			changed=true;
		}
		if(kDown) {
			motion_future.arms[4].angle -= vel * delta;
			changed=true;
		}
		
		if(oDown) {
			motion_future.arms[5].angle += vel * delta;
			changed=true;
		}
		if(lDown) {
			motion_future.arms[5].angle -= vel * delta;
			changed=true;
		}
		

		if(changed==true) {
			if(CheckAngleLimits(motion_future)) {
				FK(motion_future);
				arm_moved=true;
			}
		}
	}

	
	protected void keyAction(KeyEvent e,boolean state) {
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
		}
	}

	
	public void keyPressed(KeyEvent e) {
		keyAction(e,true);
   	}
	
	
	public void keyReleased(KeyEvent e) {
		keyAction(e,false);
	}
	
	
	public void PrepareMove(float delta) {
		if(pDown) pWasOn=true;
		if(!pDown && pWasOn) {
			pWasOn=false;
			moveMode=!moveMode;
		}
		if(moveMode) update_ik(delta);
		else		 update_fk(delta);
		
		// before the robot is allowed to do anything it has to be homed
		if( this.isReadyToReceive ) {
			if(!homed) {
				if(!homing) {
					// we are not homed and we have not begun to home
					if(HOME_AUTOMATICALLY_ON_STARTUP==true) {
						// this should be sent by a human when they are ready
						this.sendLineToRobot("G28");
						homing = true;
					}
				} else {
					motion_future.finger_tip.set(HOME_X,HOME_Y,HOME_Z);  // HOME_* should match values in robot firmware.
					IK(motion_future);
					FinalizeMove();
					this.sendLineToRobot("G92 X"+HOME_X+" Y"+HOME_Y+" Z"+HOME_Z);
					homing=false;
					homed=true;
					follow_mode=true;
				}
			}
		}
	}
	
	
	public void FinalizeMove() {
		// copy motion_future to motion_now
		motion_now.set(motion_future);
		
		if(arm_moved) {
			if(homed && follow_mode && this.isReadyToReceive ) {
				arm_moved=false;
				this.sendLineToRobot("G0 X"+motion_now.finger_tip.x
						          +" Y"+motion_now.finger_tip.y
						          +" Z"+motion_now.finger_tip.z
						          +" U"+motion_now.iku
						          +" V"+motion_now.ikv
						          +" W"+motion_now.ikw
						          );
			}
			
			// TODO: update text fields when the cursor moves.  right now this causes inter-thread damage and crashes the app
			/*
			if(view_px!=null) {	        
				view_px.getField().setText(Float.toString(motion_now.finger_tip.x));
				viewPy.getField().setText(Float.toString(motion_now.finger_tip.y));
				viewPz.getField().setText(Float.toString(motion_now.finger_tip.z));
				viewRx.getField().setText(Float.toString(motion_now.iku));
				viewRy.getField().setText(Float.toString(motion_now.ikv));
				viewRz.getField().setText(Float.toString(motion_now.ikw));
			}*/
		}
	}
	
	
	public void render(GL2 gl2) {
		int i;

		boolean draw_finger_star=false;
		boolean draw_base_star=false;
		boolean draw_shoulder_to_elbow=false;
		boolean draw_shoulder_star=false;
		boolean draw_elbow_star=false;
		boolean draw_wrist_star=false;
		boolean draw_stl=true;

		//RebuildShoulders(motion_now);
		
		gl2.glPushMatrix();
		gl2.glTranslated(position.x, position.y, position.z);
		
		if(draw_stl) {
			// base
			gl2.glPushMatrix();
			gl2.glColor3f(0, 0, 1);
			gl2.glTranslatef(0, 0, BASE_TO_SHOULDER_Z+0.6f);
			gl2.glRotatef(90, 0, 0, 1);
			gl2.glRotatef(90, 1, 0, 0);
			//gl2.glScalef(0.1f,0.1f,0.1f);
			modelBase.render(gl2);
			gl2.glPopMatrix();
			
			// arms
			for(i=0;i<3;++i) {
				gl2.glColor3f(1, 0, 1);
				gl2.glPushMatrix();
				gl2.glTranslatef(motion_now.arms[i*2+0].shoulder.x,
						         motion_now.arms[i*2+0].shoulder.y,
						         motion_now.arms[i*2+0].shoulder.z);
				gl2.glRotatef(120.0f*i, 0, 0, 1);
				gl2.glRotatef(90, 0, 1, 0);
				gl2.glRotatef(180-motion_now.arms[i*2+0].angle,0,0,1);
				//gl2.glScalef(0.1f,0.1f,0.1f);
				modelArm.render(gl2);
				gl2.glPopMatrix();
	
				gl2.glColor3f(1, 1, 0);
				gl2.glPushMatrix();
				gl2.glTranslatef(motion_now.arms[i*2+1].shoulder.x,
						         motion_now.arms[i*2+1].shoulder.y,
						         motion_now.arms[i*2+1].shoulder.z);
				gl2.glRotatef(120.0f*i, 0, 0, 1);
				gl2.glRotatef(90, 0, 1, 0);
				gl2.glRotatef(+motion_now.arms[i*2+1].angle,0,0,1);
				//gl2.glScalef(0.1f,0.1f,0.1f);
				modelArm.render(gl2);
				gl2.glPopMatrix();
			}
			//top
			gl2.glPushMatrix();
			gl2.glColor3f(0, 1, 0);
			gl2.glTranslatef(motion_now.finger_tip.x,motion_now.finger_tip.y,motion_now.finger_tip.z);
			gl2.glRotatef(motion_now.iku, 1, 0, 0);
			gl2.glRotatef(motion_now.ikv, 0, 1, 0);
			gl2.glRotatef(motion_now.ikw, 0, 0, 1);
			gl2.glRotatef(90, 0, 0, 1);
			gl2.glRotatef(180, 1, 0, 0);
			//gl2.glScalef(0.1f, 0.1f, 0.1f);
			modelTop.render(gl2);
			gl2.glPopMatrix();
		}
		
		
		// draw the forearms
		
		Cylinder tube = new Cylinder();
		gl2.glColor3f(0.8f,0.8f,0.8f);
		tube.setRadius(0.15f);
		for(i=0;i<6;++i) {
			//gl2.glBegin(GL2.GL_LINES);
			//gl2.glColor3f(1,0,0);
			//gl2.glVertex3f(motion_now.arms[i].wrist.x,motion_now.arms[i].wrist.y,motion_now.arms[i].wrist.z);
			//gl2.glColor3f(0,1,0);
			//gl2.glVertex3f(motion_now.arms[i].elbow.x,motion_now.arms[i].elbow.y,motion_now.arms[i].elbow.z);
			//gl2.glEnd();
			tube.SetP1(motion_now.arms[i].wrist);
			tube.SetP2(motion_now.arms[i].elbow);
			PrimitiveSolids.drawCylinder(gl2, tube);
		}

		gl2.glDisable(GL2.GL_LIGHTING);
		// debug info
		gl2.glPushMatrix();
		for(i=0;i<6;++i) {
			gl2.glColor3f(1,1,1);
			if(draw_shoulder_star) PrimitiveSolids.drawStar(gl2, motion_now.arms[i].shoulder,5);
			if(draw_elbow_star) PrimitiveSolids.drawStar(gl2, motion_now.arms[i].elbow,3);			
			if(draw_wrist_star) PrimitiveSolids.drawStar(gl2, motion_now.arms[i].wrist,1);

			if(draw_shoulder_to_elbow) {
				gl2.glBegin(GL2.GL_LINES);
				gl2.glColor3f(0,1,0);
				gl2.glVertex3f(motion_now.arms[i].elbow.x,motion_now.arms[i].elbow.y,motion_now.arms[i].elbow.z);
				gl2.glColor3f(0,0,1);
				gl2.glVertex3f(motion_now.arms[i].shoulder.x,motion_now.arms[i].shoulder.y,motion_now.arms[i].shoulder.z);
				gl2.glEnd();
			}
		}
		gl2.glPopMatrix();
		
		if(draw_finger_star) {
	 		// draw finger orientation
			float s=2;
			gl2.glBegin(GL2.GL_LINES);
			gl2.glColor3f(1,1,1);
			gl2.glVertex3f(motion_now.finger_tip.x, motion_now.finger_tip.y, motion_now.finger_tip.z);
			gl2.glVertex3f(motion_now.finger_tip.x+motion_now.finger_forward.x*s,
					       motion_now.finger_tip.y+motion_now.finger_forward.y*s,
					       motion_now.finger_tip.z+motion_now.finger_forward.z*s);
			gl2.glVertex3f(motion_now.finger_tip.x, motion_now.finger_tip.y, motion_now.finger_tip.z);
			gl2.glVertex3f(motion_now.finger_tip.x+motion_now.finger_up.x*s,
				       motion_now.finger_tip.y+motion_now.finger_up.y*s,
				       motion_now.finger_tip.z+motion_now.finger_up.z*s);
			gl2.glVertex3f(motion_now.finger_tip.x, motion_now.finger_tip.y, motion_now.finger_tip.z);
			gl2.glVertex3f(motion_now.finger_tip.x+motion_now.finger_left.x*s,
				       motion_now.finger_tip.y+motion_now.finger_left.y*s,
				       motion_now.finger_tip.z+motion_now.finger_left.z*s);
			
			gl2.glEnd();
		}

		if(draw_base_star) {
	 		// draw finger orientation
			float s=2;
			gl2.glDisable(GL2.GL_DEPTH_TEST);
			gl2.glBegin(GL2.GL_LINES);
			gl2.glColor3f(1,0,0);
			gl2.glVertex3f(motion_now.base.x, motion_now.base.y, motion_now.base.z);
			gl2.glVertex3f(motion_now.base.x+motion_now.base_forward.x*s,
					       motion_now.base.y+motion_now.base_forward.y*s,
					       motion_now.base.z+motion_now.base_forward.z*s);
			gl2.glColor3f(0,1,0);
			gl2.glVertex3f(motion_now.base.x, motion_now.base.y, motion_now.base.z);
			gl2.glVertex3f(motion_now.base.x+motion_now.base_up.x*s,
				       motion_now.base.y+motion_now.base_up.y*s,
				       motion_now.base.z+motion_now.base_up.z*s);
			gl2.glColor3f(0,0,1);
			gl2.glVertex3f(motion_now.base.x, motion_now.base.y, motion_now.base.z);
			gl2.glVertex3f(motion_now.base.x+motion_now.finger_left.x*s,
				       motion_now.base.y+motion_now.finger_left.y*s,
				       motion_now.base.z+motion_now.finger_left.z*s);
			
			gl2.glEnd();
			gl2.glEnable(GL2.GL_DEPTH_TEST);
		}
		
		gl2.glEnable(GL2.GL_LIGHTING);
		
		gl2.glPopMatrix();
	}
	

	public void setModeAbsolute() {
		if(connection!=null) this.sendLineToRobot("G90");
	}
	
	
	public void setModeRelative() {
		if(connection!=null) this.sendLineToRobot("G91");
	}
	

	@Override
	// override this method to check that the software is connected to the right type of robot.
	public void serialDataAvailable(MarginallyCleverConnection arg0,String line) {
		if(line.contains(hello)) {
			isPortConfirmed=true;
			//finalizeMove();
			setModeAbsolute();
			this.sendLineToRobot("R1");
			
			String uidString=line.substring(hello.length()).trim();
			System.out.println(">>> UID="+uidString);
			try {
				long uid = Long.parseLong(uidString);
				if(uid==0) {
					robotUID = getNewRobotUID();
				} else {
					robotUID = uid;
				}
				updateGUI();
			}
			catch(Exception e) {
				e.printStackTrace();
			}

			displayName="Rotary Stewart Platform 2 #"+robotUID;
		}
	}
	

	/**
	 * based on http://www.exampledepot.com/egs/java.net/Post.html
	 */
	private long getNewRobotUID() {
		long new_uid = 0;

		try {
			// Send data
			URL url = new URL("https://marginallyclever.com/stewart_platform_getuid.php");
			URLConnection conn = url.openConnection();
			try (
					final InputStream connectionInputStream = conn.getInputStream();
					final Reader inputStreamReader = new InputStreamReader(connectionInputStream);
					final BufferedReader rd = new BufferedReader(inputStreamReader)
					) {
				String line = rd.readLine();
				new_uid = Long.parseLong(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}

		// did read go ok?
		if (new_uid != 0) {
			// make sure a topLevelMachinesPreferenceNode node is created
			// tell the robot it's new UID.
			this.sendLineToRobot("UID " + new_uid);
		}
		return new_uid;
	}
	
	
	public boolean isPortConfirmed() {
		return isPortConfirmed;
	}
	

	public void MoveBase(RotaryStewartPlatform2MotionState state,Vector3f dp) {
		state.base.set(dp);
		RebuildShoulders(state);
	}
	
	
	public void RotateBase(RotaryStewartPlatform2MotionState state,float pan,float tilt) {
		state.base_pan=pan;
		state.base_tilt=tilt;
		
		state.base_forward.y = (float)Math.sin(pan *DEG2RAD) * (float)Math.cos(tilt *DEG2RAD);
		state.base_forward.x = (float)Math.cos(pan *DEG2RAD) * (float)Math.cos(tilt *DEG2RAD);
		state.base_forward.z =                                 (float)Math.sin(tilt *DEG2RAD);
		state.base_forward.normalize();
		
		state.base_up.set(0,0,1);
	
		state.base_right.cross(state.base_up,state.base_forward);
		state.base_right.normalize();
		state.base_up.cross(state.base_forward,state.base_right);
		state.base_up.normalize();
		
		RebuildShoulders(state);
	}
	
	protected void RebuildShoulders(RotaryStewartPlatform2MotionState state) {
		  Vector3f n1=new Vector3f(),o1=new Vector3f(),temp=new Vector3f();
		  float c,s;
		  int i;
		  for(i=0;i<3;++i) {
		    Arm arma=state.arms[i*2+0];
		    Arm armb=state.arms[i*2+1];

		    c=(float)Math.cos(i*(float)Math.PI*2.0f/3.0f);
		    s=(float)Math.sin(i*(float)Math.PI*2.0f/3.0f);

		    //n1 = n* c + o*s;
		    n1.set(state.base_forward);
		    n1.scale(c);
		    temp.set(state.base_right);
		    temp.scale(s);
		    n1.add(temp);
		    n1.normalize();
		    //o1 = n*-s + o*c;
		    o1.set(state.base_forward);
		    o1.scale(-s);
		    temp.set(state.base_right);
		    temp.scale(c);
		    o1.add(temp);
		    o1.normalize();
		    //n1.scale(-1);

		    
//		    arma.shoulder = n1*BASE_TO_SHOULDER_X + motion_future.base_up*BASE_TO_SHOULDER_Z - o1*BASE_TO_SHOULDER_Y;
//		    armb.shoulder = n1*BASE_TO_SHOULDER_X + motion_future.base_up*BASE_TO_SHOULDER_Z + o1*BASE_TO_SHOULDER_Y;
		    arma.shoulder.set(n1);
		    arma.shoulder.scale(BASE_TO_SHOULDER_X);
		    temp.set(state.base_up);
		    temp.scale(BASE_TO_SHOULDER_Z);
		    arma.shoulder.add(temp);
		    armb.shoulder.set(arma.shoulder);
		    temp.set(o1);
		    temp.scale(BASE_TO_SHOULDER_Y);
		    arma.shoulder.sub(temp);
		    armb.shoulder.add(temp);
		    //arma.shoulder.add(state.base);
		    //armb.shoulder.add(state.base);

//		    arma.elbow = n1*BASE_TO_SHOULDER_X + motion_future.base_up*BASE_TO_SHOULDER_Z - o1*(BASE_TO_SHOULDER_Y+BICEP_LENGTH);
//		    armb.elbow = n1*BASE_TO_SHOULDER_X + motion_future.base_up*BASE_TO_SHOULDER_Z + o1*(BASE_TO_SHOULDER_Y+BICEP_LENGTH);
		    arma.elbow.set(n1);
		    arma.elbow.scale(BASE_TO_SHOULDER_X);
		    temp.set(state.base_up);
		    temp.scale(BASE_TO_SHOULDER_Z);
		    arma.elbow.add(temp);
		    armb.elbow.set(arma.elbow);
		    temp.set(o1);
		    temp.scale(BASE_TO_SHOULDER_Y+BICEP_LENGTH);
		    arma.elbow.sub(temp);
		    armb.elbow.add(temp);
		    //arma.shoulder.add(state.base);
		    //armb.shoulder.add(state.base);		    
		    
		    arma.shoulderToElbow.set(o1);
		    arma.shoulderToElbow.scale(-1);
		    armb.shoulderToElbow.set(o1);
		  }
	}
	
	
	public BoundingVolume [] GetBoundingVolumes() {
		// TODO finish me
		return volumes;
	}
	
	
	Vector3f GetWorldCoordinatesFor(Vector3f in) {
		Vector3f out = new Vector3f(motion_future.base);
		
		Vector3f tempx = new Vector3f(motion_future.base_forward);
		tempx.scale(in.x);
		out.add(tempx);

		Vector3f tempy = new Vector3f(motion_future.base_right);
		tempy.scale(-in.y);
		out.add(tempy);

		Vector3f tempz = new Vector3f(motion_future.base_up);
		tempz.scale(in.z);
		out.add(tempz);
				
		return out;
	}

	
	@Override
	public ArrayList<JPanel> getControlPanels() {
		ArrayList<JPanel> list = super.getControlPanels();
		
		if(list==null) list = new ArrayList<JPanel>();

		CollapsiblePanel rspPanel = new CollapsiblePanel("Inverse Kinematics");
		rspPanel.getContentPane().setLayout(new GridLayout(0,1));
		
		view_home=new JButton("Home");
		viewPx=new JLabelledTextField(Float.toString(motion_now.finger_tip.x),"X");
		viewPy=new JLabelledTextField(Float.toString(motion_now.finger_tip.y),"Y");
		viewPz=new JLabelledTextField(Float.toString(motion_now.finger_tip.z),"Z");
		viewRx=new JLabelledTextField(Float.toString(motion_now.iku),"U");
		viewRy=new JLabelledTextField(Float.toString(motion_now.ikv),"V");
		viewRz=new JLabelledTextField(Float.toString(motion_now.ikw),"W");
        rspPanel.getContentPane().add(view_home);
		rspPanel.getContentPane().add(viewPx);
		rspPanel.getContentPane().add(viewPy);
		rspPanel.getContentPane().add(viewPz);
		rspPanel.getContentPane().add(viewRx);
		rspPanel.getContentPane().add(viewRy);
		rspPanel.getContentPane().add(viewRz);
		view_home.addActionListener(this);
		viewPx.addPropertyChangeListener("value",this);
		viewPy.addPropertyChangeListener("value",this);
		viewPz.addPropertyChangeListener("value",this);
		viewRx.addPropertyChangeListener("value",this);
		viewRy.addPropertyChangeListener("value",this);
		viewRz.addPropertyChangeListener("value",this);
		
		list.add(rspPanel);
		updateGUI();
		
		return list;
	}
	
	
	public void updateGUI() {
		
	}
	

	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();

		if(subject == view_home ) {
			JOptionPane.showMessageDialog(null, "Go Home","Click", JOptionPane.INFORMATION_MESSAGE);
		}
		
		
		super.actionPerformed(e);
	}
	
	public void propertyChange(PropertyChangeEvent e) {
		Object subject = e.getSource();

		try {
			if(subject == viewPx ) {
				String t=viewPx.getField().getText();
				float f = Float.parseFloat(t);
				if(!Float.isNaN(f)) {
					this.motion_future.finger_tip.x = f;
					moveIfAble();
				}
			}
			if(subject == viewPy ) {
				String t=viewPy.getField().getText();
				float f = Float.parseFloat(t);
				if(!Float.isNaN(f)) {
					this.motion_future.finger_tip.y = f;
					moveIfAble();
				}
			}
			if(subject == viewPz ) {
				String t=viewPz.getField().getText();
				float f = Float.parseFloat(t);
				if(!Float.isNaN(f)) {
					this.motion_future.finger_tip.z = f;
					moveIfAble();
				}
			}
			
			if(subject == viewRx ) {
				String t=viewRx.getField().getText();
				float f = Float.parseFloat(t);
				if(!Float.isNaN(f)) {
					this.motion_future.iku = f;
					moveIfAble();
				}
			}
			if(subject == viewRy ) {
				String t=viewRy.getField().getText();
				float f = Float.parseFloat(t);
				if(!Float.isNaN(f)) {
					this.motion_future.ikv = f;
					moveIfAble();
				}
			}
			if(subject == viewRz ) {
				String t=viewRz.getField().getText();
				float f = Float.parseFloat(t);
				if(!Float.isNaN(f)) {
					this.motion_future.ikw = f;
					moveIfAble();
				}
			}		
		} catch(NumberFormatException e2) {}
	}
}
