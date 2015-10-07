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
extends RobotWithConnection
implements PropertyChangeListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1816224308642132316L;
	// machine ID
	protected long robotUID;
	protected final static String hello = "HELLO WORLD! I AM STEWART PLATFORM V4.2";

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

	protected Model modelTop = Model.loadModel("/StewartPlatform.zip:top.STL",0.1f);
	protected Model modelArm = Model.loadModel("/StewartPlatform.zip:arm.STL",0.1f);
	protected Model modelBase = Model.loadModel("/StewartPlatform.zip:base.STL",0.1f);
	
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
		motion_future.rotateBase(0f,0f);
		motion_future.moveBase(new Vector3f(0,0,0));
		moveIfAble();
	}


	public RotaryStewartPlatform2(EvilOverlord gui) {
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

		motion_now.rotateBase(0,0);
		motion_now.updateIKEndEffector();
		motion_now.rebuildShoulders();
		motion_now.updateIKWrists();

		motion_future.set(motion_now);

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
		moveIfAble();
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
			if(motion_future.checkAngleLimits()) {
				motion_future.updateForwardKinematics();
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
	
	@Override
	public void prepareMove(float delta) {
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
					motion_future.updateInverseKinematics();
					finalizeMove();
					this.sendLineToRobot("G92 X"+HOME_X+" Y"+HOME_Y+" Z"+HOME_Z);
					homing=false;
					homed=true;
					follow_mode=true;
				}
			}
		}
	}

	@Override
	public void finalizeMove() {
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
				float f = Float.parseFloat(viewPx.getField().getText());
				if(!Float.isNaN(f)) {
					this.motion_future.finger_tip.x = f;
					moveIfAble();
				}
			}
			if(subject == viewPy ) {
				float f = Float.parseFloat(viewPy.getField().getText());
				if(!Float.isNaN(f)) {
					this.motion_future.finger_tip.y = f;
					moveIfAble();
				}
			}
			if(subject == viewPz ) {
				float f = Float.parseFloat(viewPz.getField().getText());
				if(!Float.isNaN(f)) {
					this.motion_future.finger_tip.z = f;
					moveIfAble();
				}
			}
			
			if(subject == viewRx ) {
				float f = Float.parseFloat(viewRx.getField().getText());
				if(!Float.isNaN(f)) {
					this.motion_future.iku = f;
					moveIfAble();
				}
			}
			if(subject == viewRy ) {
				float f = Float.parseFloat(viewRy.getField().getText());
				if(!Float.isNaN(f)) {
					this.motion_future.ikv = f;
					moveIfAble();
				}
			}
			if(subject == viewRz ) {
				float f = Float.parseFloat(viewRz.getField().getText());
				if(!Float.isNaN(f)) {
					this.motion_future.ikw = f;
					moveIfAble();
				}
			}		
		} catch(NumberFormatException e2) {}
	}
}
