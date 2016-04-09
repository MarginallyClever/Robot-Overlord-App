package com.marginallyclever.robotOverlord.RotaryStewartPlatform2;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import com.jogamp.opengl.GL2;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.vecmath.Vector3f;

import com.marginallyclever.robotOverlord.*;
import com.marginallyclever.robotOverlord.communications.AbstractConnection;

public class RotaryStewartPlatform2
extends RobotWithConnection
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1816224308642132316L;
	// machine ID
	protected long robotUID;
	protected final static String hello = "HELLO WORLD! I AM STEWART PLATFORM V4.2";
	public static final String ROBOT_NAME = "Stewart Platorm 2";
	
	//machine dimensions
	static final float BASE_TO_SHOULDER_X   =( 8.093f);  // measured in solidworks, relative to base origin
	static final float BASE_TO_SHOULDER_Y   =( 2.150f);
	static final float BASE_TO_SHOULDER_Z   =( 6.610f);
	static final float BICEP_LENGTH         =( 5.000f);
	static final float FOREARM_LENGTH       =(16.750f);
	static final float WRIST_TO_FINGER_X    =( 7.635f);
	static final float WRIST_TO_FINGER_Y    =( 0.553f);
	static final float WRIST_TO_FINGER_Z    =(-0.870f);  // measured in solidworks, relative to finger origin
	
	protected float HOME_X = 0.0f;
	protected float HOME_Y = 0.0f;
	protected float HOME_Z = 0.0f;
	protected float HOME_A = 0.0f;
	protected float HOME_B = 0.0f;
	protected float HOME_C = 0.0f;
	
	static final float LIMIT_U=15;
	static final float LIMIT_V=15;
	static final float LIMIT_W=15;
	
	protected float HOME_RIGHT_X = 0;
	protected float HOME_RIGHT_Y = 0;
	protected float HOME_RIGHT_Z = -1;

	protected float HOME_FORWARD_X = 1;
	protected float HOME_FORWARD_Y = 0;
	protected float HOME_FORWARD_Z = 0;

	protected boolean HOME_AUTOMATICALLY_ON_STARTUP = true;
	
	protected Cylinder [] volumes = new Cylinder[6];

	protected boolean isPortConfirmed=false;

	protected transient Model modelTop = null;
	protected transient Model modelArm = null;
	protected transient Model modelBase = null;
	
	protected RotaryStewartPlatform2MotionState motion_now = new RotaryStewartPlatform2MotionState();
	protected RotaryStewartPlatform2MotionState motion_future = new RotaryStewartPlatform2MotionState();
	
	boolean homed = false;
	boolean homing = false;
	boolean follow_mode = true;
	boolean arm_moved = false;
	
	// keyboard history
	protected float xDir = 0.0f;
	protected float yDir = 0.0f;
	protected float zDir = 0.0f;
	protected float uDir = 0.0f;
	protected float vDir = 0.0f;
	protected float wDir = 0.0f;
	protected double speed=2;
	
	boolean moveMode=true;

	private boolean just_testing_dont_get_uid=true;

	protected transient RotaryStewartPlatform2ControlPanel rspPanel;
	
	
	public Vector3f getHome() {  return new Vector3f(HOME_X,HOME_Y,HOME_Z);  }
	
	
	public void setHome(Vector3f newhome) {
		HOME_X=newhome.x;
		HOME_Y=newhome.y;
		HOME_Z=newhome.z;
		motion_future.rotateBase(0f,0f);
		motion_future.moveBase(new Vector3f(0,0,0));
		moveIfAble();
	}


	public RotaryStewartPlatform2() {
		super();
		setDisplayName(ROBOT_NAME);

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

		motion_now.rotateBase(0,0);
		motion_now.updateIKEndEffector();
		motion_now.rebuildShoulders();
		motion_now.updateIKWrists();

		motion_future.set(motion_now);
		setupModels();

		motion_future.finger_tip.set(motion_now.finger_tip);
		moveIfAble();
	}
	

	protected void setupModels() {
		modelTop = Model.loadModel("/StewartPlatform.zip:top.STL",0.1f);
		modelArm = Model.loadModel("/StewartPlatform.zip:arm.STL",0.1f);
		modelBase = Model.loadModel("/StewartPlatform.zip:base.STL",0.1f);
	}

    private void readObject(ObjectInputStream inputStream)
            throws IOException, ClassNotFoundException
    {
    	setupModels();
        inputStream.defaultReadObject();
    }   
	

	public void setSpeed(double newSpeed) {
		speed=newSpeed;
	}
	public double getSpeed() {
		return speed;
	}

	public void moveX(float dir) {
		xDir=dir;
	}

	public void moveY(float dir) {
		yDir=dir;
	}

	public void moveZ(float dir) {
		zDir=dir;
	}

	public void moveU(float dir) {
		uDir=dir;
	}

	public void moveV(float dir) {
		vDir=dir;
	}

	public void moveW(float dir) {
		wDir=dir;
	}


	protected void updateIK(float delta) {
		boolean changed=false;
		motion_future.set(motion_now);

		// lateral moves
		if (xDir!=0) {
			motion_future.finger_tip.x += xDir * (float)speed;
			changed=true;
			xDir=0;
		}		
		if (yDir!=0) {
			motion_future.finger_tip.y += yDir * (float)speed;
			changed=true;
			yDir=0;
		}
		if (zDir!=0) {
			motion_future.finger_tip.z += zDir * (float)speed;
			changed=true;
			zDir=0;
		}

		// rotation		
		if(uDir!=0) {	motion_future.iku = (float)speed * uDir;	changed=true; }
		if(vDir!=0) {	motion_future.ikv = (float)speed * vDir;	changed=true; }
		if(wDir!=0) {	motion_future.ikw = (float)speed * wDir;	changed=true; }

		if(changed==true) {
			moveIfAble();
		}
	}
	
	public void moveIfAble() {
		rotateFinger();	
		
		if(motion_future.movePermitted()) {
			arm_moved=true;
			finalizeMove();
		} else {
			motion_future.set(motion_now);
		}
	}
	
	
	public void rotateFinger() {
		Vector3f forward = new Vector3f(HOME_FORWARD_X,HOME_FORWARD_Y,HOME_FORWARD_Z);
		Vector3f right = new Vector3f(HOME_RIGHT_X,HOME_RIGHT_Y,HOME_RIGHT_Z);
		Vector3f up = new Vector3f();
		
		up.cross(forward,right);
		
		Vector3f of = new Vector3f(forward);
		Vector3f or = new Vector3f(right);
		Vector3f ou = new Vector3f(up);
		
		Vector3f result;

		result = rotateAroundAxis(forward,of,Math.toRadians(motion_future.iku));  // TODO rotating around itself has no effect.
		result = rotateAroundAxis(result,or,Math.toRadians(motion_future.ikv));
		result = rotateAroundAxis(result,ou,Math.toRadians(motion_future.ikw));
		motion_future.finger_forward.set(result);

		result = rotateAroundAxis(right,of,Math.toRadians(motion_future.iku));
		result = rotateAroundAxis(result,or,Math.toRadians(motion_future.ikv));
		result = rotateAroundAxis(result,ou,Math.toRadians(motion_future.ikw));
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
	static public Vector3f rotateAroundAxis(Vector3f vec,Vector3f axis,double angle_radians) {
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

	
	@Override
	public void prepareMove(float delta) {
		updateIK(delta);
	}

	@Override
	public void finalizeMove() {
		// copy motion_future to motion_now
		motion_now.set(motion_future);
		
		if(arm_moved) {
			if(homed && follow_mode ) {
				arm_moved=false;
				sendChangeToRealMachine();
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
			gl2.glColor3f(1, 0.8f, 0.6f);
			gl2.glTranslatef(0, 0, BASE_TO_SHOULDER_Z+0.6f);
			gl2.glRotatef(90, 0, 0, 1);
			gl2.glRotatef(90, 1, 0, 0);
			modelBase.render(gl2);
			gl2.glPopMatrix();
			
			// arms
			for(i=0;i<3;++i) {
				gl2.glColor3f(0.9f,0.9f,0.9f);
				gl2.glPushMatrix();
				gl2.glTranslatef(motion_now.arms[i*2+0].shoulder.x,
						         motion_now.arms[i*2+0].shoulder.y,
						         motion_now.arms[i*2+0].shoulder.z);
				gl2.glRotatef(120.0f*i, 0, 0, 1);
				gl2.glRotatef(90, 0, 1, 0);
				gl2.glRotatef(180-motion_now.arms[i*2+0].angle,0,0,1);
				modelArm.render(gl2);
				gl2.glPopMatrix();
	
				gl2.glColor3f(0.9f,0.9f,0.9f);
				gl2.glPushMatrix();
				gl2.glTranslatef(motion_now.arms[i*2+1].shoulder.x,
						         motion_now.arms[i*2+1].shoulder.y,
						         motion_now.arms[i*2+1].shoulder.z);
				gl2.glRotatef(120.0f*i, 0, 0, 1);
				gl2.glRotatef(90, 0, 1, 0);
				gl2.glRotatef(+motion_now.arms[i*2+1].angle,0,0,1);
				modelArm.render(gl2);
				gl2.glPopMatrix();
			}
			//top
			gl2.glPushMatrix();
			gl2.glColor3f(1, 0.8f, 0.6f);
			gl2.glTranslatef(motion_now.finger_tip.x,motion_now.finger_tip.y,motion_now.finger_tip.z+motion_now.relative.z);
			gl2.glRotatef(motion_now.iku, 1, 0, 0);
			gl2.glRotatef(motion_now.ikv, 0, 1, 0);
			gl2.glRotatef(motion_now.ikw, 0, 0, 1);
			gl2.glRotatef(90, 0, 0, 1);
			gl2.glRotatef(180, 1, 0, 0);
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
	public void dataAvailable(AbstractConnection arg0,String line) {
		if(line.contains(hello)) {
			isPortConfirmed=true;
			//finalizeMove();
			setModeAbsolute();
			this.sendLineToRobot("R1");
			
			String ending = line.substring(hello.length());
			String uidString=ending.substring(ending.indexOf('#')+1).trim();
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

			setDisplayName(ROBOT_NAME+" #"+robotUID);
		}
	}
	

	/**
	 * based on http://www.exampledepot.com/egs/java.net/Post.html
	 */
	private long getNewRobotUID() {
		long new_uid = 0;

		if(just_testing_dont_get_uid==true) {
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
		}
		return new_uid;
	}
	
	
	public boolean isPortConfirmed() {
		return isPortConfirmed;
	}
	
	
	public BoundingVolume [] getBoundingVolumes() {
		// TODO finish me
		return volumes;
	}
	
	
	Vector3f getWorldCoordinatesFor(Vector3f in) {
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

		rspPanel = new RotaryStewartPlatform2ControlPanel(this);
		
		list.add(rspPanel);
		updateGUI();
		
		return list;
	}
	
	
	public void updateGUI() {
		
	}
	
	private void sendChangeToRealMachine() {
		if(isPortConfirmed()==false) return;
		
		this.sendLineToRobot("G0 X"+motion_now.finger_tip.x
		          +" Y"+motion_now.finger_tip.y
		          +" Z"+motion_now.finger_tip.z
		          +" U"+motion_now.iku
		          +" V"+motion_now.ikv
		          +" W"+motion_now.ikw
		          );
	}
	
	
	public void goHome() {
		homed=false;
		this.sendLineToRobot("G28");
		motion_future.finger_tip.set(HOME_X,HOME_Y,HOME_Z);  // HOME_* should match values in robot firmware.
		motion_future.iku=0;
		motion_future.iku=0;
		motion_future.iku=0;
		motion_future.updateInverseKinematics();
		motion_now.set(motion_future);
		
		//finalizeMove();
		//this.sendLineToRobot("G92 X"+HOME_X+" Y"+HOME_Y+" Z"+HOME_Z);
		homed=true;
	}
}
