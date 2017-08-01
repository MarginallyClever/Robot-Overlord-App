package com.marginallyclever.robotOverlord.deltaRobot3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import com.jogamp.opengl.GL2;
import javax.swing.JPanel;
import javax.vecmath.Vector3f;

import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.robotOverlord.*;
import com.marginallyclever.robotOverlord.actions.UndoableActionRobotMove;
import com.marginallyclever.robotOverlord.model.Model;
import com.marginallyclever.robotOverlord.model.ModelFactory;
import com.marginallyclever.robotOverlord.robot.Robot;

public class DeltaRobot3
extends Robot {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7173262402738411169L;
	
	// machine ID
	protected long robotUID;
	protected final static String hello = "HELLO WORLD! I AM DELTA ROBOT V3-";
	public final static String ROBOT_NAME = "Delta Robot 3";
	
	//machine dimensions & calibration
	static final float BASE_TO_SHOULDER_X   =( 0.0f);  // measured in solidworks, relative to base origin
	static final float BASE_TO_SHOULDER_Y   =( 3.77f);
	static final float BASE_TO_SHOULDER_Z   =(18.9f);
	static final float BICEP_LENGTH         =( 5.000f);
	static final float FOREARM_LENGTH       =(16.50f);
	static final float WRIST_TO_FINGER_X    =( 0.0f);
	static final float WRIST_TO_FINGER_Y    =( 1.724f);
	static final float WRIST_TO_FINGER_Z    =( 0.5f);  // measured in solidworks, relative to finger origin

	public static final int NUM_ARMS = 3;
	
	protected float HOME_X = 0.0f;
	protected float HOME_Y = 0.0f;
	protected float HOME_Z = 3.98f;
	
	static final float LIMIT_U=15;
	static final float LIMIT_V=15;
	static final float LIMIT_W=15;

	// bounding volumes for collision testing
	protected Cylinder [] volumes;

	// models for 3d rendering
	protected transient Model modelTop;
	protected transient Model modelArm;
	protected transient Model modelBase;
	
	// motion state testing
	protected DeltaRobot3Keyframe motionNow;
	protected DeltaRobot3Keyframe motionFuture;

	// control panel
	protected transient DeltaRobot3ControlPanel controlPanel;
	
	// keyboard history
	private float aDir, bDir, cDir;
	private float xDir, yDir, zDir;

	// network info
	private  boolean isPortConfirmed;
	
	// misc
	private double speed;
	private boolean isHomed = false;
	private boolean haveArmsMoved = false;
	

	public DeltaRobot3() {
		super();
		setDisplayName(ROBOT_NAME);

		motionNow = new DeltaRobot3Keyframe();
		motionFuture = new DeltaRobot3Keyframe();
		
		setupBoundingVolumes();
		setHome(new Vector3f(0,0,0));
		
		isPortConfirmed=false;
		speed=2;
		aDir = 0.0f;
		bDir = 0.0f;
		cDir = 0.0f;
		xDir = 0.0f;
		yDir = 0.0f;
		zDir = 0.0f;
	}
	

	protected void setupBoundingVolumes() {
		// set up bounding volumes
		volumes = new Cylinder[NUM_ARMS];
		for(int i=0;i<volumes.length;++i) {
			volumes[i] = new Cylinder();
		}
		volumes[0].setRadius(3.2f);
		volumes[1].setRadius(3.0f*0.575f);
		volumes[2].setRadius(2.2f);
	}

	
	@Override
	protected void loadModels(GL2 gl2) {
		try {
			modelTop = ModelFactory.createModelFromFilename("/DeltaRobot3.zip:top.STL",0.1f);
			modelArm = ModelFactory.createModelFromFilename("/DeltaRobot3.zip:arm.STL",0.1f);
			modelBase = ModelFactory.createModelFromFilename("/DeltaRobot3.zip:base.STL",0.1f);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public Vector3f getHome() {  return new Vector3f(HOME_X,HOME_Y,HOME_Z);  }
	
	
	public void setHome(Vector3f newHome) {
		HOME_X=newHome.x;
		HOME_Y=newHome.y;
		HOME_Z=newHome.z;
		rotateBase(motionNow,0f,0f);
		moveBase(motionNow,newHome);
		rebuildShoulders(motionNow);
		updateIKWrists(motionNow);

		// find the starting height of the end effector at home position
		// @TODO: project wrist-on-bicep to get more accurate distance
		float aa=(motionNow.arms[0].elbow.y-motionNow.arms[0].wrist.y);
		float cc=FOREARM_LENGTH;
		float bb=(float)Math.sqrt((cc*cc)-(aa*aa));
		aa=motionNow.arms[0].elbow.x-motionNow.arms[0].wrist.x;
		cc=bb;
		bb=(float)Math.sqrt((cc*cc)-(aa*aa));
		motionNow.fingerPosition.set(0,0,BASE_TO_SHOULDER_Z-bb-WRIST_TO_FINGER_Z);
		motionFuture.set(motionNow);

		moveIfAble();
	}
	

    private void readObject(ObjectInputStream inputStream)
            throws IOException, ClassNotFoundException
    {
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


	protected void updateIK(float delta) {
		boolean changed=false;
		motionFuture.set(motionNow);
		
		float dv = (float)getSpeed();
		
		if (xDir!=0) {  motionFuture.fingerPosition.x += dv * xDir;  changed=true;  xDir=0;  }
		if (yDir!=0) {  motionFuture.fingerPosition.y += dv * yDir;	 changed=true;  yDir=0;  }
		if (zDir!=0) {  motionFuture.fingerPosition.z += dv * zDir;	 changed=true;  zDir=0;  }
		
		if(changed) {
			moveIfAble();
		}
	}
	
	
	public void moveIfAble() {
		if(movePermitted(motionFuture)) {
			haveArmsMoved=true;
			finalizeMove();
			if(controlPanel!=null) controlPanel.update();
		} else {
			motionFuture.set(motionNow);
		}
	}
	
	
	protected void updateFK(float delta) {
		boolean changed=false;
		int i;
		
		for(i=0;i<NUM_ARMS;++i) {
			motionFuture.arms[i].angle = motionNow.arms[i].angle;
		}
		
		// movement
		float dv=(float)getSpeed();

		// if continuous, adjust speed over time
		//float dv *= delta;
		
		if (aDir!=0) {  motionFuture.arms[0].angle -= dv * aDir;  changed=true;  aDir=0;  }
		if (bDir!=0) {  motionFuture.arms[1].angle -= dv * bDir;  changed=true;  bDir=0;  }
		if (cDir!=0) {  motionFuture.arms[2].angle += dv * cDir;  changed=true;  cDir=0;  }
		
		// if not continuous, set *Dir to zero.
		
		if(changed) {
			if(checkAngleLimits(motionFuture)) {
				updateFK(motionFuture);
				haveArmsMoved=true;
			}
		}
	}
	
	@Override
	public void prepareMove(float delta) {
		updateIK(delta);
		updateFK(delta);
	}

	
	@Override
	public void finalizeMove() {
		if(!haveArmsMoved) return;		

		haveArmsMoved=false;
		motionNow.set(motionFuture);
		this.sendLineToRobot("G0 X"+motionNow.fingerPosition.x
				          +" Y"+motionNow.fingerPosition.y
				          +" Z"+motionNow.fingerPosition.z
				          );
		if(controlPanel!=null) controlPanel.update();
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
		super.render(gl2);
		
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
		Vector3f p = getPosition();
		gl2.glTranslated(p.x, p.y, p.z+2);
		
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
			for(i=0;i<NUM_ARMS;++i) {
				setColor(gl2,255.0f/255.0f, 249.0f/255.0f, 242.0f/255.0f,1);
				gl2.glPushMatrix();
				gl2.glTranslatef(motionNow.arms[i].shoulder.x,
						         motionNow.arms[i].shoulder.y,
						         motionNow.arms[i].shoulder.z);
				gl2.glRotatef(90,0,1,0);  // model oriented wrong direction
				gl2.glRotatef(60-i*(360.0f/NUM_ARMS), 1, 0, 0);
				gl2.glTranslatef(0, 0, 0.125f*2.54f);  // model origin wrong
				gl2.glRotatef(180-motionNow.arms[i].angle,0,0,1);
				modelArm.render(gl2);
				gl2.glPopMatrix();
			}
			//top
			gl2.glPushMatrix();
			setColor(gl2,255.0f/255.0f, 249.0f/255.0f, 242.0f/255.0f,1);
			gl2.glTranslatef(motionNow.fingerPosition.x,motionNow.fingerPosition.y,motionNow.fingerPosition.z);
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
		
		for(i=0;i<NUM_ARMS;++i) {
			//gl2.glBegin(GL2.GL_LINES);
			//gl2.glColor3f(1,0,0);
			//gl2.glVertex3f(motion_now.arms[i].wrist.x,motion_now.arms[i].wrist.y,motion_now.arms[i].wrist.z);
			//gl2.glColor3f(0,1,0);
			//gl2.glVertex3f(motion_now.arms[i].elbow.x,motion_now.arms[i].elbow.y,motion_now.arms[i].elbow.z);
			//gl2.glEnd();
			ortho.x=(float)Math.cos((float)i*Math.PI*2.0/3.0f);
			ortho.y=(float)Math.sin((float)i*Math.PI*2.0/3.0f);
			ortho.z=0;
			a.set(motionNow.arms[i].wrist);
			b.set(ortho);
			b.scale(1);
			a.add(b);
			tube.SetP1(a);
			a.set(motionNow.arms[i].elbow);
			b.set(ortho);
			b.scale(1);
			a.add(b);
			tube.SetP2(a);
			PrimitiveSolids.drawCylinder(gl2, tube);

			a.set(motionNow.arms[i].wrist);
			b.set(ortho);
			b.scale(-1);
			a.add(b);
			tube.SetP1(a);
			a.set(motionNow.arms[i].elbow);
			b.set(ortho);
			b.scale(-1);
			a.add(b);
			tube.SetP2(a);

			PrimitiveSolids.drawCylinder(gl2, tube);
		}

		gl2.glDisable(GL2.GL_LIGHTING);
		// debug info
		gl2.glPushMatrix();
		for(i=0;i<NUM_ARMS;++i) {
			gl2.glColor3f(1,1,1);
			if(draw_shoulder_star) PrimitiveSolids.drawStar(gl2, motionNow.arms[i].shoulder,5);
			if(draw_elbow_star) PrimitiveSolids.drawStar(gl2, motionNow.arms[i].elbow,3);			
			if(draw_wrist_star) PrimitiveSolids.drawStar(gl2, motionNow.arms[i].wrist,1);

			if(draw_shoulder_to_elbow) {
				gl2.glBegin(GL2.GL_LINES);
				gl2.glColor3f(0,1,0);
				gl2.glVertex3f(motionNow.arms[i].elbow.x,motionNow.arms[i].elbow.y,motionNow.arms[i].elbow.z);
				gl2.glColor3f(0,0,1);
				gl2.glVertex3f(motionNow.arms[i].shoulder.x,motionNow.arms[i].shoulder.y,motionNow.arms[i].shoulder.z);
				gl2.glEnd();
			}
		}
		gl2.glPopMatrix();
		
		if(draw_finger_star) {
	 		// draw finger orientation
			float s=2;
			gl2.glBegin(GL2.GL_LINES);
			gl2.glColor3f(1,1,1);
			gl2.glVertex3f(motionNow.fingerPosition.x, motionNow.fingerPosition.y, motionNow.fingerPosition.z);
			gl2.glVertex3f(motionNow.fingerPosition.x+motionNow.base_forward.x*s,
					       motionNow.fingerPosition.y+motionNow.base_forward.y*s,
					       motionNow.fingerPosition.z+motionNow.base_forward.z*s);
			gl2.glVertex3f(motionNow.fingerPosition.x, motionNow.fingerPosition.y, motionNow.fingerPosition.z);
			gl2.glVertex3f(motionNow.fingerPosition.x+motionNow.base_up.x*s,
				       motionNow.fingerPosition.y+motionNow.base_up.y*s,
				       motionNow.fingerPosition.z+motionNow.base_up.z*s);
			gl2.glVertex3f(motionNow.fingerPosition.x, motionNow.fingerPosition.y, motionNow.fingerPosition.z);
			gl2.glVertex3f(motionNow.fingerPosition.x+motionNow.base_right.x*s,
				       motionNow.fingerPosition.y+motionNow.base_right.y*s,
				       motionNow.fingerPosition.z+motionNow.base_right.z*s);
			
			gl2.glEnd();
		}

		if(draw_base_star) {
	 		// draw finger orientation
			float s=2;
			gl2.glDisable(GL2.GL_DEPTH_TEST);
			gl2.glBegin(GL2.GL_LINES);
			gl2.glColor3f(1,0,0);
			gl2.glVertex3f(motionNow.base.x, motionNow.base.y, motionNow.base.z);
			gl2.glVertex3f(motionNow.base.x+motionNow.base_forward.x*s,
					       motionNow.base.y+motionNow.base_forward.y*s,
					       motionNow.base.z+motionNow.base_forward.z*s);
			gl2.glColor3f(0,1,0);
			gl2.glVertex3f(motionNow.base.x, motionNow.base.y, motionNow.base.z);
			gl2.glVertex3f(motionNow.base.x+motionNow.base_up.x*s,
				       motionNow.base.y+motionNow.base_up.y*s,
				       motionNow.base.z+motionNow.base_up.z*s);
			gl2.glColor3f(0,0,1);
			gl2.glVertex3f(motionNow.base.x, motionNow.base.y, motionNow.base.z);
			gl2.glVertex3f(motionNow.base.x+motionNow.base_right.x*s,
				       motionNow.base.y+motionNow.base_right.y*s,
				       motionNow.base.z+motionNow.base_right.z*s);
			
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
		isHomed=false;
		this.sendLineToRobot("G28");
		motionFuture.fingerPosition.set(HOME_X,HOME_Y,HOME_Z);  // HOME_* should match values in robot firmware.
		updateIK(motionFuture);
		motionNow.set(motionFuture);
		haveArmsMoved=true;
		finalizeMove();
		isHomed=true;
		
		if(controlPanel!=null) controlPanel.update();
	}
	

	@Override
	// override this method to check that the software is connected to the right type of robot.
	public void dataAvailable(NetworkConnection arg0,String line) {
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
				if(controlPanel!=null) controlPanel.update();
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
                    final Reader inputStreamReader = new InputStreamReader(connectionInputStream, StandardCharsets.UTF_8);
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
		Vector3f out = new Vector3f(motionFuture.base);
		
		Vector3f tempx = new Vector3f(motionFuture.base_forward);
		tempx.scale(in.x);
		out.add(tempx);

		Vector3f tempy = new Vector3f(motionFuture.base_right);
		tempy.scale(-in.y);
		out.add(tempy);

		Vector3f tempz = new Vector3f(motionFuture.base_up);
		tempz.scale(in.z);
		out.add(tempz);
				
		return out;
	}

	
	@Override
	public ArrayList<JPanel> getContextPanel(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getContextPanel(gui);
		
		if(list==null) list = new ArrayList<JPanel>();
		
		controlPanel = new DeltaRobot3ControlPanel(gui,this);
		list.add(controlPanel);
		if(controlPanel!=null) controlPanel.update();
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
	
	
	public void setSpeed(double newSpeed) {
		speed=newSpeed;
	}
	public double getSpeed() {
		return speed;
	}
	
	public void move(int axis,int direction) {
		switch(axis) {
		case UndoableActionRobotMove.AXIS_A: moveA(direction); break;
		case UndoableActionRobotMove.AXIS_B: moveB(direction); break;
		case UndoableActionRobotMove.AXIS_C: moveC(direction); break;
		case UndoableActionRobotMove.AXIS_X: moveX(direction); break;
		case UndoableActionRobotMove.AXIS_Y: moveY(direction); break;
		case UndoableActionRobotMove.AXIS_Z: moveZ(direction); break;
		}
	}
	
	private void moveA(float dir) {		aDir=dir;		enableFK();		}
	private void moveB(float dir) {		bDir=dir;		enableFK();		}
	private void moveC(float dir) {		cDir=dir;		enableFK();		}
	private void moveX(float dir) {		xDir=dir;		disableFK();	}
	private void moveY(float dir) {		yDir=dir;		disableFK();	}
	private void moveZ(float dir) {		zDir=dir;		disableFK();	}	

	public boolean isHomed() {
		return isHomed;
	}

	
	public boolean updateFK(DeltaRobot3Keyframe keyframe) {
		return true;
	}

	/**
	 * Convert cartesian XYZ to robot motor steps.
	 * @return true if successful, false if the IK solution cannot be found.
	 */
	public boolean updateIK(DeltaRobot3Keyframe keyframe) {
		try {
			updateIKWrists(keyframe);
			updateIKShoulderAngles(keyframe);
		}
		catch(AssertionError e) {
			return false;
		}

		return true;
	}


	protected void updateIKWrists(DeltaRobot3Keyframe keyframe) {
		Vector3f n1 = new Vector3f(),o1 = new Vector3f(),temp = new Vector3f();
		float c,s;
		int i;
		for(i=0;i<NUM_ARMS;++i) {
			DeltaRobot3Arm arma=keyframe.arms[i];

			c=(float)Math.cos( (float)Math.PI*2.0f * (i/3.0f - 60.0f/360.0f) );
			s=(float)Math.sin( (float)Math.PI*2.0f * (i/3.0f - 60.0f/360.0f) );

			//n1 = n* c + o*s;
			n1.set(keyframe.base_forward);
			n1.scale(c);
			temp.set(keyframe.base_right);
			temp.scale(s);
			n1.add(temp);
			n1.normalize();
			//o1 = n*-s + o*c;
			o1.set(keyframe.base_forward);
			o1.scale(-s);
			temp.set(keyframe.base_right);
			temp.scale(c);
			o1.add(temp);
			o1.normalize();
			//n1.scale(-1);


			//arma.wrist = this.finger_tip + n1*T2W_X + this.base_up*T2W_Z - o1*T2W_Y;
			//armb.wrist = this.finger_tip + n1*T2W_X + this.base_up*T2W_Z + o1*T2W_Y;
			arma.wrist.set(n1);
			arma.wrist.scale(DeltaRobot3.WRIST_TO_FINGER_X);
			arma.wrist.add(keyframe.fingerPosition);
			temp.set(keyframe.base_up);
			temp.scale(DeltaRobot3.WRIST_TO_FINGER_Z);
			arma.wrist.add(temp);
			temp.set(o1);
			temp.scale(DeltaRobot3.WRIST_TO_FINGER_Y);
			arma.wrist.sub(temp);
		}
	}
	

	protected void updateIKShoulderAngles(DeltaRobot3Keyframe keyframe) throws AssertionError {
		Vector3f ortho = new Vector3f(),w = new Vector3f(),wop = new Vector3f(),temp = new Vector3f(),r = new Vector3f();
		float a,b,d,r1,r0,hh,y,x;

		int i;
		for(i=0;i<NUM_ARMS;++i) {
			DeltaRobot3Arm arm = keyframe.arms[i];

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


	public void moveBase(DeltaRobot3Keyframe keyframe,Vector3f dp) {
		keyframe.base.set(dp);
		rebuildShoulders(keyframe);
	}


	public void rotateBase(DeltaRobot3Keyframe keyframe,float pan,float tilt) {
		keyframe.basePan=pan;
		keyframe.baseTilt=tilt;

		pan = (float)Math.toRadians(pan);
		tilt = (float)Math.toRadians(tilt);
		keyframe.base_forward.y = (float)Math.sin(pan) * (float)Math.cos(tilt);
		keyframe.base_forward.x = (float)Math.cos(pan) * (float)Math.cos(tilt);
		keyframe.base_forward.z =                        (float)Math.sin(tilt);
		keyframe.base_forward.normalize();

		keyframe.base_up.set(0,0,1);

		keyframe.base_right.cross(keyframe.base_up,keyframe.base_forward);
		keyframe.base_right.normalize();
		keyframe.base_up.cross(keyframe.base_forward,keyframe.base_right);
		keyframe.base_up.normalize();

		rebuildShoulders(keyframe);
	}

	
	protected void rebuildShoulders(DeltaRobot3Keyframe keyframe) {
		Vector3f n1=new Vector3f(),o1=new Vector3f(),temp=new Vector3f();
		float c,s;
		int i;
		for(i=0;i<3;++i) {
			DeltaRobot3Arm arma=keyframe.arms[i];

			c=(float)Math.cos( (float)Math.PI*2.0f * (i/3.0f - 60.0f/360.0f) );
			s=(float)Math.sin( (float)Math.PI*2.0f * (i/3.0f - 60.0f/360.0f) );

			//n1 = n* c + o*s;
			n1.set(keyframe.base_forward);
			n1.scale(c);
			temp.set(keyframe.base_right);
			temp.scale(s);
			n1.add(temp);
			n1.normalize();
			//o1 = n*-s + o*c;
			o1.set(keyframe.base_forward);
			o1.scale(-s);
			temp.set(keyframe.base_right);
			temp.scale(c);
			o1.add(temp);
			o1.normalize();
			//n1.scale(-1);


			//		    arma.shoulder = n1*BASE_TO_SHOULDER_X + motion_future.base_up*BASE_TO_SHOULDER_Z - o1*BASE_TO_SHOULDER_Y;
			arma.shoulder.set(n1);
			arma.shoulder.scale(DeltaRobot3.BASE_TO_SHOULDER_X);
			temp.set(keyframe.base_up);
			temp.scale(DeltaRobot3.BASE_TO_SHOULDER_Z);
			arma.shoulder.add(temp);
			temp.set(o1);
			temp.scale(DeltaRobot3.BASE_TO_SHOULDER_Y);
			arma.shoulder.sub(temp);
			arma.shoulder.add(keyframe.base);

			//		    arma.elbow = n1*BASE_TO_SHOULDER_X + motion_future.base_up*BASE_TO_SHOULDER_Z - o1*(BASE_TO_SHOULDER_Y+BICEP_LENGTH);
			arma.elbow.set(n1);
			arma.elbow.scale(DeltaRobot3.BASE_TO_SHOULDER_X);
			temp.set(keyframe.base_up);
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
	public boolean movePermitted(DeltaRobot3Keyframe keyframe) {/*
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
		if(!checkAngleLimits(keyframe)) return false;
		// seems doable
		if(!updateIK(keyframe)) return false;

		// OK
		return true;
	}


	public boolean checkAngleLimits(DeltaRobot3Keyframe keyframe) {
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
}
