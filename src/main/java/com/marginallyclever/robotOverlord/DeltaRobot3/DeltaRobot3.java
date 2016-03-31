package com.marginallyclever.robotOverlord.DeltaRobot3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.media.opengl.GL2;
import javax.swing.JPanel;
import javax.vecmath.Vector3f;

import com.marginallyclever.robotOverlord.*;
import com.marginallyclever.robotOverlord.communications.AbstractConnection;

public class DeltaRobot3
extends RobotWithConnection {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7173262402738411169L;
	
	// machine ID
	protected long robotUID;
	protected final static String hello = "HELLO WORLD! I AM DELTA ROBOT V3-";
	public final static String ROBOT_NAME = "Delta Robot 3";
	
	//machine dimensions
	static final float BASE_TO_SHOULDER_X   =( 0.0f);  // measured in solidworks, relative to base origin
	static final float BASE_TO_SHOULDER_Y   =( 3.77f);
	static final float BASE_TO_SHOULDER_Z   =(18.9f);
	static final float BICEP_LENGTH         =( 5.000f);
	static final float FOREARM_LENGTH       =(16.50f);
	static final float WRIST_TO_FINGER_X    =( 0.0f);
	static final float WRIST_TO_FINGER_Y    =( 1.724f);
	static final float WRIST_TO_FINGER_Z    =( 0.5f);  // measured in solidworks, relative to finger origin
	
	protected float HOME_X = 0.0f;
	protected float HOME_Y = 0.0f;
	protected float HOME_Z = 3.98f;
	
	static final float LIMIT_U=15;
	static final float LIMIT_V=15;
	static final float LIMIT_W=15;

	protected boolean HOME_AUTOMATICALLY_ON_STARTUP = true;
	
	protected Cylinder [] volumes = new Cylinder[DeltaRobot3MotionState.NUM_ARMS];

	protected transient Model modelTop = null;
	protected transient Model modelArm = null;
	protected transient Model modelBase = null;
	
	protected DeltaRobot3MotionState motion_now = new DeltaRobot3MotionState();
	protected DeltaRobot3MotionState motion_future = new DeltaRobot3MotionState();
	protected transient DeltaRobot3ControlPanel controlPanel;
	
	boolean homed = false;
	boolean arm_moved = false;
	
	// keyboard history
	protected float aDir = 0.0f;
	protected float bDir = 0.0f;
	protected float cDir = 0.0f;

	protected float xDir = 0.0f;
	protected float yDir = 0.0f;
	protected float zDir = 0.0f;

	
	boolean pDown=false;
	boolean pWasOn=false;
	boolean moveMode=true;
	protected boolean isPortConfirmed=false;
	
	protected double speed=2;

	
	
	public Vector3f getHome() {  return new Vector3f(HOME_X,HOME_Y,HOME_Z);  }
	
	
	public void setHome(Vector3f newhome) {
		HOME_X=newhome.x;
		HOME_Y=newhome.y;
		HOME_Z=newhome.z;
		motion_future.rotateBase(0f,0f);
		motion_future.moveBase(new Vector3f(0,0,0));
		moveIfAble();
	}


	public DeltaRobot3() {
		super();
		
		setupModels();
		
		setDisplayName(ROBOT_NAME);

		/*
		// set up bounding volumes
		for(int i=0;i<volumes.length;++i) {
			volumes[i] = new Cylinder();
		}
		volumes[0].radius=3.2f;
		volumes[1].radius=3.0f*0.575f;
		volumes[2].radius=2.2f;
		*/

		motion_now.rotateBase(0,0);
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
		motion_now.finger_tip.set(0,0,BASE_TO_SHOULDER_Z-bb-WRIST_TO_FINGER_Z);

		motion_future.finger_tip.set(motion_now.finger_tip);
		moveIfAble();
	}
	
	
	private void setupModels() {
		modelTop = Model.loadModel("/DeltaRobot3.zip:top.STL",0.1f);
		modelArm = Model.loadModel("/DeltaRobot3.zip:arm.STL",0.1f);
		modelBase = Model.loadModel("/DeltaRobot3.zip:base.STL",0.1f);
	}

    private void readObject(ObjectInputStream inputStream)
            throws IOException, ClassNotFoundException
    {
    	setupModels();
        inputStream.defaultReadObject();
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
	}


	protected void update_ik(float delta) {
		boolean changed=false;
		motion_future.set(motion_now);
		
		// movement 
		float dv = (float)getSpeed();
		// if continuous, adjust speed over time
		//* delta;
		
		// lateral moves
		if (xDir!=0) {  motion_future.finger_tip.x += dv * xDir;	}
		if (yDir!=0) {  motion_future.finger_tip.y += dv * yDir;	}
		if (zDir!=0) {  motion_future.finger_tip.z += dv * zDir;	}

		// if not continuous, reset abc to zero
		xDir=0;
		yDir=0;
		zDir=0;
		
		if(!motion_now.finger_tip.epsilonEquals(motion_future.finger_tip,dv/2.0f)) {
			changed=true;
		}

		if(changed==true) {
			moveIfAble();
		}
	}
	
	public void moveIfAble() {
		if(motion_future.movePermitted()) {
			arm_moved=true;
			finalizeMove();
		} else {
			motion_future.set(motion_now);
		}
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
		int i;
		
		for(i=0;i<DeltaRobot3MotionState.NUM_ARMS;++i) {
			motion_future.arms[i].angle = motion_now.arms[i].angle;
		}
		
		// movement
		float dv=(float)getSpeed();
		// if continuous, adjust speed over time
		//float dv *= delta;
		
		if (aDir!=0) {
			motion_future.arms[0].angle -= dv * aDir;
			changed=true;
		}
		if (bDir!=0) {
			motion_future.arms[1].angle -= dv * bDir;
			changed=true;
		}
		if (cDir!=0) {
			motion_future.arms[2].angle += dv * cDir;
			changed=true;
		}
		
		// if not continuous, reset abc to zero
		aDir=0;
		bDir=0;
		cDir=0;
		
		if(changed==true) {
			if(motion_future.checkAngleLimits()) {
				motion_future.updateForwardKinematics();
				arm_moved=true;
			}
		}
	}
	
	@Override
	public void prepareMove(float delta) {
		update_ik(delta);
		update_fk(delta);
	}

	
	@Override
	public void finalizeMove() {
		// copy motion_future to motion_now
		motion_now.set(motion_future);
		
		if(arm_moved) {
			if(homed) {
				arm_moved=false;
				this.sendLineToRobot("G0 X"+motion_now.finger_tip.x
						          +" Y"+motion_now.finger_tip.y
						          +" Z"+motion_now.finger_tip.z
						          );
				updateGUI();
			}
		}
	}
	

	protected void setColor(GL2 gl2,float r,float g,float b,float a) {
		float [] diffuse = {r,g,b,a};
		gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, diffuse,0);
		float[] specular={0.85f,0.85f,0.85f,1.0f};
	    gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, specular,0);
	    float[] emission={0.01f,0.01f,0.01f,1f};
	    gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION, emission,0);
	    
	    gl2.glMaterialf(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, 50.0f);

	    gl2.glColor4f(r,g,b,a);
	}
	
	
	public void render(GL2 gl2) {
		int i;

		boolean draw_finger_star=true;
		boolean draw_base_star=true;
		boolean draw_shoulder_to_elbow=true;
		boolean draw_shoulder_star=true;
		boolean draw_elbow_star=true;
		boolean draw_wrist_star=true;
		boolean draw_stl=true;

		//RebuildShoulders(motion_now);
		
		gl2.glPushMatrix();
		gl2.glTranslated(position.x, position.y, position.z+2);
		
		//motion_now.moveBase(new Vector3f(0,0,0));
		
		if(draw_stl) {
			// base
			gl2.glPushMatrix();
			setColor(gl2,
					247.0f/255.0f,
					233.0f/255.0f,
					215.0f/255.0f,
					1.0f);
			setColor(gl2,1,0.8f,0.6f,1);
			modelBase.render(gl2);
			gl2.glPopMatrix();

			//gl2.glTranslatef(0, 0, BASE_TO_SHOULDER_Z);
			
			// arms
			for(i=0;i<DeltaRobot3MotionState.NUM_ARMS;++i) {
				setColor(gl2,255.0f/255.0f, 249.0f/255.0f, 242.0f/255.0f,1);
				gl2.glPushMatrix();
				gl2.glTranslatef(motion_now.arms[i].shoulder.x,
						         motion_now.arms[i].shoulder.y,
						         motion_now.arms[i].shoulder.z);
				gl2.glRotatef(90,0,1,0);  // model oriented wrong direction
				gl2.glRotatef(60-i*(360.0f/DeltaRobot3MotionState.NUM_ARMS), 1, 0, 0);
				gl2.glTranslatef(0, 0, 0.125f*2.54f);  // model origin wrong
				gl2.glRotatef(180-motion_now.arms[i].angle,0,0,1);
				modelArm.render(gl2);
				gl2.glPopMatrix();
			}
			//top
			gl2.glPushMatrix();
			setColor(gl2,255.0f/255.0f, 249.0f/255.0f, 242.0f/255.0f,1);
			gl2.glTranslatef(motion_now.finger_tip.x,motion_now.finger_tip.y,motion_now.finger_tip.z);
			modelTop.render(gl2);
			gl2.glPopMatrix();
		}
		
		// draw the forearms
		
		Cylinder tube = new Cylinder();
		gl2.glColor3f(0.8f,0.8f,0.8f);
		tube.setRadius(0.15f);
		Vector3f a = new Vector3f();
		Vector3f b = new Vector3f();
		Vector3f ortho = new Vector3f();
		
		for(i=0;i<DeltaRobot3MotionState.NUM_ARMS;++i) {
			//gl2.glBegin(GL2.GL_LINES);
			//gl2.glColor3f(1,0,0);
			//gl2.glVertex3f(motion_now.arms[i].wrist.x,motion_now.arms[i].wrist.y,motion_now.arms[i].wrist.z);
			//gl2.glColor3f(0,1,0);
			//gl2.glVertex3f(motion_now.arms[i].elbow.x,motion_now.arms[i].elbow.y,motion_now.arms[i].elbow.z);
			//gl2.glEnd();
			ortho.x=(float)Math.cos((float)i*Math.PI*2.0/3.0f);
			ortho.y=(float)Math.sin((float)i*Math.PI*2.0/3.0f);
			ortho.z=0;
			a.set(motion_now.arms[i].wrist);
			b.set(ortho);
			b.scale(1);
			a.add(b);
			tube.SetP1(a);
			a.set(motion_now.arms[i].elbow);
			b.set(ortho);
			b.scale(1);
			a.add(b);
			tube.SetP2(a);
			PrimitiveSolids.drawCylinder(gl2, tube);

			a.set(motion_now.arms[i].wrist);
			b.set(ortho);
			b.scale(-1);
			a.add(b);
			tube.SetP1(a);
			a.set(motion_now.arms[i].elbow);
			b.set(ortho);
			b.scale(-1);
			a.add(b);
			tube.SetP2(a);

			PrimitiveSolids.drawCylinder(gl2, tube);
		}

		gl2.glDisable(GL2.GL_LIGHTING);
		// debug info
		gl2.glPushMatrix();
		for(i=0;i<DeltaRobot3MotionState.NUM_ARMS;++i) {
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
			gl2.glVertex3f(motion_now.finger_tip.x+motion_now.base_forward.x*s,
					       motion_now.finger_tip.y+motion_now.base_forward.y*s,
					       motion_now.finger_tip.z+motion_now.base_forward.z*s);
			gl2.glVertex3f(motion_now.finger_tip.x, motion_now.finger_tip.y, motion_now.finger_tip.z);
			gl2.glVertex3f(motion_now.finger_tip.x+motion_now.base_up.x*s,
				       motion_now.finger_tip.y+motion_now.base_up.y*s,
				       motion_now.finger_tip.z+motion_now.base_up.z*s);
			gl2.glVertex3f(motion_now.finger_tip.x, motion_now.finger_tip.y, motion_now.finger_tip.z);
			gl2.glVertex3f(motion_now.finger_tip.x+motion_now.base_right.x*s,
				       motion_now.finger_tip.y+motion_now.base_right.y*s,
				       motion_now.finger_tip.z+motion_now.base_right.z*s);
			
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
			gl2.glVertex3f(motion_now.base.x+motion_now.base_right.x*s,
				       motion_now.base.y+motion_now.base_right.y*s,
				       motion_now.base.z+motion_now.base_right.z*s);
			
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

	
	public void goHome() {
		this.sendLineToRobot("G28");
		motion_future.finger_tip.set(HOME_X,HOME_Y,HOME_Z);  // HOME_* should match values in robot firmware.
		motion_future.updateInverseKinematics();
		arm_moved=true;
		finalizeMove();
		homed=true;
	}
	

	@Override
	// override this method to check that the software is connected to the right type of robot.
	public void dataAvailable(AbstractConnection arg0,String line) {
		if(line.contains(hello)) {
			isPortConfirmed=true;
			//finalizeMove();
			setModeAbsolute();
			
			String uidString=line.substring(hello.length()).trim();
			uidString = uidString.substring(uidString.indexOf('#')+1);
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
		
		System.out.print("*** "+line);
	}
	

	/**
	 * based on http://www.exampledepot.com/egs/java.net/Post.html
	 */
	private long getNewRobotUID() {
		long new_uid = 0;

		try {
			// Send data
			URL url = new URL("https://marginallyclever.com/deltarobot_getuid.php");
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
		
		controlPanel = new DeltaRobot3ControlPanel(this);
		list.add(controlPanel);
		updateGUI();
/*
		ArrayList<JPanel> toolList = tool.getControlPanels();
		Iterator<JPanel> iter = toolList.iterator();
		while(iter.hasNext()) {
			list.add(iter.next());
		}
*/
		return list;
	}

	
	protected float roundOff(float v) {
		float SCALE = 1000.0f;
		
		return Math.round(v*SCALE)/SCALE;
	}
	
	
	public void updateGUI() {
		Vector3f v = new Vector3f();
		v.set(motion_now.finger_tip);
		// TODO rotate fingerPosition before adding position
		//v.add(position);
		
		controlPanel.xPos.setText(Float.toString(roundOff(v.x)));
		controlPanel.yPos.setText(Float.toString(roundOff(v.y)));
		controlPanel.zPos.setText(Float.toString(roundOff(v.z)));
	}
	
	
	public void setSpeed(double newSpeed) {
		speed=newSpeed;
	}
	public double getSpeed() {
		return speed;
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
}
