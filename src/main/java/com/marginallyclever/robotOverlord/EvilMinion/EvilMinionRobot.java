package com.marginallyclever.robotOverlord.EvilMinion;

import javax.swing.JPanel;
import javax.vecmath.Vector3f;
import com.jogamp.opengl.GL2;

import com.marginallyclever.robotOverlord.*;
import com.marginallyclever.robotOverlord.EvilMinionTool.*;
import com.marginallyclever.robotOverlord.communications.AbstractConnection;
import com.marginallyclever.robotOverlord.model.Model;
import com.marginallyclever.robotOverlord.model.ModelFactory;
import com.marginallyclever.robotOverlord.robot.RobotWithConnection;

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
import java.util.Iterator;


public class EvilMinionRobot
extends RobotWithConnection {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3644731265897692399L;
	// machine ID
	protected long robotUID;
	protected final static String hello = "HELLO WORLD! I AM MINION #";
	public final static String ROBOT_NAME = "Evil Minion Arm";
	
	// machine dimensions from design software
	public final static double ANCHOR_ADJUST_Y = 0.64;
	public final static double ANCHOR_TO_SHOULDER_Y = 3.27;
	public final static double SHOULDER_TO_PINION_X = -15;
	public final static double SHOULDER_TO_PINION_Y = -2.28;
	public final static double SHOULDER_TO_BOOM_X = 8;
	public final static double SHOULDER_TO_BOOM_Y = 7;
	public final static double BOOM_TO_STICK_Y = 37;
	public final static double STICK_TO_WRIST_X = -40.0;
	public final static double WRIST_TO_PINION_X = 5;
	public final static double WRIST_TO_PINION_Z = 1.43;
	public final static float WRIST_TO_TOOL_X = -6.29f;
	public final static float WRIST_TO_TOOL_Y = 1.0f;
	
	// model files
	private transient Model anchor = null;
	private transient Model shoulder = null;
	private transient Model shoulderPinion = null;
	private transient Model boom = null;
	private transient Model stick = null;
	private transient Model wristBone = null;
	private transient Model hand = null;
	private transient Model wristInterior = null;
	private transient Model wristPinion = null;

	private Material matAnchor		= new Material();
	private Material matShoulder	= new Material();
	private Material matBoom		= new Material();
	private Material matStick		= new Material();
	private Material matWrist		= new Material();
	private Material matHand		= new Material();
	
	// currently attached tool
	private EvilMinionTool tool = null;
	
	// collision volumes
	Cylinder [] volumes = new Cylinder[6];

	// motion states
	protected EvilMinionRobotMotionState motionNow = new EvilMinionRobotMotionState();
	protected EvilMinionRobotMotionState motionFuture = new EvilMinionRobotMotionState();
	
	// keyboard history
	protected float aDir = 0.0f;
	protected float bDir = 0.0f;
	protected float cDir = 0.0f;
	protected float dDir = 0.0f;
	protected float eDir = 0.0f;

	protected float xDir = 0.0f;
	protected float yDir = 0.0f;
	protected float zDir = 0.0f;

	// machine logic states
	protected boolean armMoved = false;
	protected boolean isPortConfirmed=false;
	protected double speed=2;

	// visual debugging
	protected boolean isRenderFKOn=false;
	protected boolean isRenderIKOn=false;

	// gui
	protected transient EvilMinionRobotControlPanel arm5Panel=null;
	
	
	public EvilMinionRobot() {
		super();
		
		setupModels();
		
		setDisplayName(ROBOT_NAME);
		
		// set up bounding volumes
		for(int i=0;i<volumes.length;++i) {
			volumes[i] = new Cylinder();
		}
		volumes[0].setRadius(3.2f);
		volumes[1].setRadius(3.0f*0.575f);
		volumes[2].setRadius(2.2f);
		volumes[3].setRadius(1.15f);
		volumes[4].setRadius(1.2f);
		volumes[5].setRadius(1.0f*0.575f);
		
		rotateBase(0,0);
		motionNow.checkAngleLimits();
		motionFuture.checkAngleLimits();
		motionNow.forwardKinematics();
		motionFuture.forwardKinematics();
		motionNow.inverseKinematics();
		motionFuture.inverseKinematics();

		matAnchor.setDiffuseColor(0,0,0,1);
		matShoulder.setDiffuseColor(1,0,0,1);
		matBoom.setDiffuseColor(0,0,1,1);
		matStick.setDiffuseColor(1,0,1,1);
		matWrist.setDiffuseColor(0,1,0,1);
		matHand.setDiffuseColor(0.5f,0.5f,0.5f,1);
		
		tool = new EvilMinionToolGripper();
		tool.attachTo(this);
	}
	

	protected void setupModels() {
		anchor = ModelFactory.createModelFromFilename("/ArmParts.zip:anchor.STL",0.1f);
		shoulder = ModelFactory.createModelFromFilename("/ArmParts.zip:shoulder1.STL",0.1f);
		shoulderPinion = ModelFactory.createModelFromFilename("/ArmParts.zip:shoulder_pinion.STL",0.1f);
		boom = ModelFactory.createModelFromFilename("/ArmParts.zip:boom.STL",0.1f);
		stick = ModelFactory.createModelFromFilename("/ArmParts.zip:stick.STL",0.1f);
		wristBone = ModelFactory.createModelFromFilename("/ArmParts.zip:wrist_bone.STL",0.1f);
		hand = ModelFactory.createModelFromFilename("/ArmParts.zip:wrist_end.STL",0.1f);
		wristInterior = ModelFactory.createModelFromFilename("/ArmParts.zip:wrist_interior.STL",0.1f);
		wristPinion = ModelFactory.createModelFromFilename("/ArmParts.zip:wrist_pinion.STL",0.1f);
	}

    private void readObject(ObjectInputStream inputStream)
            throws IOException, ClassNotFoundException
    {
    	setupModels();
        inputStream.defaultReadObject();
    }

	
	@Override
	public ArrayList<JPanel> getControlPanels(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getControlPanels(gui);
		
		if(list==null) list = new ArrayList<JPanel>();
		
		arm5Panel = new EvilMinionRobotControlPanel(gui,this);
		list.add(arm5Panel);
		updateGUI();

		ArrayList<JPanel> toolList = tool.getControlPanels(gui);
		Iterator<JPanel> iter = toolList.iterator();
		while(iter.hasNext()) {
			list.add(iter.next());
		}
		
		return list;
	}
	
	
	public boolean isPortConfirmed() {
		return isPortConfirmed;
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

	
	
	/**
	 * update the desired finger location
	 * @param delta
	 */
	protected void updateIK(float delta) {
		boolean changed=false;
		motionFuture.fingerPosition.set(motionNow.fingerPosition);
		final float vel=(float)speed;
		float dp = vel;// * delta;

		float dX=motionFuture.fingerPosition.x;
		float dY=motionFuture.fingerPosition.y;
		float dZ=motionFuture.fingerPosition.z;
		
		if (xDir!=0) {
			dX += xDir * dp;
			changed=true;
			xDir=0;
		}		
		if (yDir!=0) {
			dY += yDir * dp;
			changed=true;
			yDir=0;
		}
		if (zDir!=0) {
			dZ += zDir * dp;
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

		if(rw!=0 || rv!=0 || ru!=0 )
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
			
			//changed=true;
		}
		
		//if(changed==true && motionFuture.movePermitted()) {
		if(changed) {
			motionFuture.fingerPosition.x = dX;
			motionFuture.fingerPosition.y = dY;
			motionFuture.fingerPosition.z = dZ;
			if(!motionFuture.inverseKinematics()) return;
			if(motionFuture.checkAngleLimits()) {
			//if(motionNow.fingerPosition.epsilonEquals(motionFuture.fingerPosition,0.1f) == false) {
				armMoved=true;
				isRenderIKOn=true;
				isRenderFKOn=false;

				sendChangeToRealMachine();
				if(!this.isPortConfirmed()) {
					// live data from the sensors will update motionNow, so only do this if we're unconnected.
					motionNow.set(motionFuture);
				}
				updateGUI();
			} else {
				motionFuture.set(motionNow);
			}
		}
	}
	
	
	protected void updateFK(float delta) {
		boolean changed=false;
		float velcd=(float)speed; // * delta
		float velabe=(float)speed; // * delta

		motionFuture.set(motionNow);
		
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
		

		if(changed) {
			motionFuture.angleA=dA;
			motionFuture.angleB=dB;
			motionFuture.angleC=dC;
			motionFuture.angleD=dD;
			motionFuture.angleE=dE;
			if(motionFuture.checkAngleLimits()) {
				motionFuture.forwardKinematics();
				isRenderIKOn=false;
				isRenderFKOn=true;
				armMoved=true;
				
				sendChangeToRealMachine();
				if(!this.isPortConfirmed()) {
					// live data from the sensors will update motionNow, so only do this if we're unconnected.
					motionNow.set(motionFuture);
				}
				updateGUI();
			} else {
				motionFuture.set(motionNow);
			}
		}
	}

	
	protected float roundOff(float v) {
		float SCALE = 1000.0f;
		
		return Math.round(v*SCALE)/SCALE;
	}
	

	
	public void updateGUI() {
		Vector3f v = new Vector3f();
		v.set(motionNow.fingerPosition);
		// TODO rotate fingerPosition before adding position
		v.add(getPosition());
		arm5Panel.xPos.setText(Float.toString(roundOff(v.x)));
		arm5Panel.yPos.setText(Float.toString(roundOff(v.y)));
		arm5Panel.zPos.setText(Float.toString(roundOff(v.z)));

		arm5Panel.a1.setText(Float.toString(roundOff(motionNow.angleA)));
		arm5Panel.b1.setText(Float.toString(roundOff(motionNow.angleB)));
		arm5Panel.c1.setText(Float.toString(roundOff(motionNow.angleC)));
		arm5Panel.d1.setText(Float.toString(roundOff(motionNow.angleD)));
		arm5Panel.e1.setText(Float.toString(roundOff(motionNow.angleE)));
		
		arm5Panel.a2.setText(Float.toString(roundOff(motionNow.ik_angleA)));
		arm5Panel.b2.setText(Float.toString(roundOff(motionNow.ik_angleB)));
		arm5Panel.c2.setText(Float.toString(roundOff(motionNow.ik_angleC)));
		arm5Panel.d2.setText(Float.toString(roundOff(motionNow.ik_angleD)));
		arm5Panel.e2.setText(Float.toString(roundOff(motionNow.ik_angleE)));

		if( tool != null ) tool.updateGUI();
	}
	
	
	protected void sendChangeToRealMachine() {
		if(!isPortConfirmed) return;
		
		
		String str="";
		if(motionFuture.angleA!=motionNow.angleA) {
			str+=" A"+roundOff(motionFuture.angleA);
		}
		if(motionFuture.angleB!=motionNow.angleB) {
			str+=" B"+roundOff(motionFuture.angleB);
		}
		if(motionFuture.angleC!=motionNow.angleC) {
			str+=" C"+roundOff(motionFuture.angleC);
		}
		if(motionFuture.angleD!=motionNow.angleD) {
			str+=" D"+roundOff(motionFuture.angleD);
		}
		if(motionFuture.angleE!=motionNow.angleE) {
			str+=" E"+roundOff(motionFuture.angleE);
		}
		
		if(str.length()>0) {
			this.sendLineToRobot("R0"+str);
		}
	}
	
	@Override
	public void prepareMove(float delta) {
		updateIK(delta);
		updateFK(delta);
		if(tool != null) tool.update(delta);
	}

	@Override
	public void finalizeMove() {
		// copy motion_future to motion_now
		motionNow.set(motionFuture);
		
		if(armMoved) {
			if( this.isReadyToReceive ) {
				armMoved=false;
			}
		}
	}
	
	
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
			// TODO rotate model
			
			gl2.glPushMatrix();
				Vector3f p = getPosition();
				gl2.glTranslatef(p.x, p.y, p.z);
				renderModels(gl2);
			gl2.glPopMatrix();

			if(isRenderFKOn) {
				gl2.glPushMatrix();
				gl2.glDisable(GL2.GL_DEPTH_TEST);
				renderFK(gl2);
				gl2.glEnable(GL2.GL_DEPTH_TEST);
				gl2.glPopMatrix();
			}
			
			if(isRenderIKOn) {
				gl2.glPushMatrix();
				gl2.glDisable(GL2.GL_DEPTH_TEST);
				renderIK(gl2);
				gl2.glEnable(GL2.GL_DEPTH_TEST);
				gl2.glPopMatrix();
			}
		gl2.glPopMatrix();
	}
	

	/**
	 * Visualize the inverse kinematics calculations
	 * @param gl2
	 */
	protected void renderIK(GL2 gl2) {
		boolean lightOn= gl2.glIsEnabled(GL2.GL_LIGHTING);
		boolean matCoOn= gl2.glIsEnabled(GL2.GL_COLOR_MATERIAL);
		gl2.glDisable(GL2.GL_LIGHTING);
		
		Vector3f ff = new Vector3f();
		ff.set(motionNow.fingerPosition);
		ff.add(motionNow.fingerForward);
		Vector3f fr = new Vector3f();
		fr.set(motionNow.fingerPosition);
		fr.add(motionNow.fingerRight);
		
		
		gl2.glColor4f(1,0,0,1);

		gl2.glBegin(GL2.GL_LINE_STRIP);
		gl2.glVertex3d(0,0,0);
		gl2.glVertex3d(motionNow.ik_shoulder.x,motionNow.ik_shoulder.y,motionNow.ik_shoulder.z);
		gl2.glVertex3d(motionNow.ik_boom.x,motionNow.ik_boom.y,motionNow.ik_boom.z);
		gl2.glVertex3d(motionNow.ik_elbow.x,motionNow.ik_elbow.y,motionNow.ik_elbow.z);
		gl2.glVertex3d(motionNow.ik_wrist.x,motionNow.ik_wrist.y,motionNow.ik_wrist.z);
		gl2.glVertex3d(motionNow.fingerPosition.x,motionNow.fingerPosition.y,motionNow.fingerPosition.z);
		gl2.glVertex3d(ff.x,ff.y,ff.z);		
		gl2.glVertex3d(motionNow.fingerPosition.x,motionNow.fingerPosition.y,motionNow.fingerPosition.z);
		gl2.glVertex3d(fr.x,fr.y,fr.z);
		gl2.glEnd();

		// finger tip
		gl2.glColor4f(1,0.8f,0,1);
		PrimitiveSolids.drawStar(gl2, motionNow.fingerPosition );
		PrimitiveSolids.drawStar(gl2, ff );
		PrimitiveSolids.drawStar(gl2, fr );
	
		if(lightOn) gl2.glEnable(GL2.GL_LIGHTING);
		if(matCoOn) gl2.glEnable(GL2.GL_COLOR_MATERIAL);
	}
	
	
	/**
	 * Draw the arm without calling glRotate to prove forward kinematics are correct.
	 * @param gl2
	 */
	protected void renderFK(GL2 gl2) {
		boolean lightOn= gl2.glIsEnabled(GL2.GL_LIGHTING);
		boolean matCoOn= gl2.glIsEnabled(GL2.GL_COLOR_MATERIAL);
		gl2.glDisable(GL2.GL_LIGHTING);

		Vector3f ff = new Vector3f();
		ff.set(motionNow.fingerPosition);
		ff.add(motionNow.fingerForward);
		Vector3f fr = new Vector3f();
		fr.set(motionNow.fingerPosition);
		fr.add(motionNow.fingerRight);
		
		gl2.glColor4f(1,1,1,1);
		gl2.glBegin(GL2.GL_LINE_STRIP);
		
		gl2.glVertex3d(0,0,0);
		gl2.glVertex3d(motionNow.shoulder.x,motionNow.shoulder.y,motionNow.shoulder.z);
		gl2.glVertex3d(motionNow.boom.x,motionNow.boom.y,motionNow.boom.z);
		gl2.glVertex3d(motionNow.elbow.x,motionNow.elbow.y,motionNow.elbow.z);
		gl2.glVertex3d(motionNow.wrist.x,motionNow.wrist.y,motionNow.wrist.z);
		gl2.glVertex3d(motionNow.fingerPosition.x,motionNow.fingerPosition.y,motionNow.fingerPosition.z);
		gl2.glVertex3d(ff.x,ff.y,ff.z);
		gl2.glVertex3d(motionNow.fingerPosition.x,motionNow.fingerPosition.y,motionNow.fingerPosition.z);
		gl2.glVertex3d(fr.x,fr.y,fr.z);

		gl2.glEnd();

		// finger tip
		gl2.glColor4f(1,0.8f,0,1);		PrimitiveSolids.drawStar(gl2, motionNow.fingerPosition );
		gl2.glColor4f(0,0.8f,1,1);		PrimitiveSolids.drawStar(gl2, ff );
		gl2.glColor4f(0,0,1,1);			PrimitiveSolids.drawStar(gl2, fr );
	
		if(lightOn) gl2.glEnable(GL2.GL_LIGHTING);
		if(matCoOn) gl2.glEnable(GL2.GL_COLOR_MATERIAL);
	}
	
	
	/**
	 * Draw the physical model according to the angle values in the motionNow state.
	 * @param gl2
	 */
	protected void renderModels(GL2 gl2) {
		// anchor
		matAnchor.render(gl2);
		// this rotation is here because the anchor model was built facing the wrong way.
		gl2.glRotated(90, 1, 0, 0);
		
		gl2.glTranslated(0, ANCHOR_ADJUST_Y, 0);
		anchor.render(gl2);

		// shoulder (E)
		matShoulder.render(gl2);
		gl2.glTranslated(0, ANCHOR_TO_SHOULDER_Y, 0);
		gl2.glRotated(motionNow.angleE,0,1,0);
		shoulder.render(gl2);

		// shoulder pinion
		gl2.glPushMatrix();
			gl2.glTranslated(SHOULDER_TO_PINION_X, SHOULDER_TO_PINION_Y, 0);
			double anchor_gear_ratio = 80.0/8.0;
			gl2.glRotated(motionNow.angleE*anchor_gear_ratio,0,1,0);
			shoulderPinion.render(gl2);
		gl2.glPopMatrix();

		// boom (D)
		matBoom.render(gl2);
		gl2.glTranslated(SHOULDER_TO_BOOM_X,SHOULDER_TO_BOOM_Y, 0);
		gl2.glRotated(90-motionNow.angleD,0,0,1);
		gl2.glPushMatrix();
			gl2.glScaled(-1,1,1);
			boom.render(gl2);
		gl2.glPopMatrix();

		// stick (C)
		matStick.render(gl2);
		gl2.glTranslated(0.0, BOOM_TO_STICK_Y, 0);
		gl2.glRotated(90+motionNow.angleC,0,0,1);
		gl2.glPushMatrix();
			gl2.glScaled(1,-1,1);
			stick.render(gl2);
		gl2.glPopMatrix();

		// to center of wrist
		gl2.glTranslated(STICK_TO_WRIST_X, 0.0, 0);

		// Gear A
		
		gl2.glPushMatrix();
			gl2.glRotated(180+motionNow.angleA-motionNow.angleB*2.0,0,0,1);
			gl2.glRotated(90, 1, 0, 0);
			wristInterior.render(gl2);
		gl2.glPopMatrix();

		// Gear B
		gl2.glPushMatrix();
			gl2.glRotated(180-motionNow.angleB*2.0-motionNow.angleA,0,0,1);
			gl2.glRotated(-90, 1, 0, 0);
			wristInterior.render(gl2);
		gl2.glPopMatrix();

		gl2.glPushMatrix();  // wrist

			gl2.glRotated(-motionNow.angleB+180,0,0,1);

			matWrist.render(gl2);
			// wrist bone
			wristBone.render(gl2);
			
			// tool holder
			gl2.glRotated(motionNow.angleA,1,0,0);

			gl2.glPushMatrix();
				matHand.render(gl2);
				hand.render(gl2);
			gl2.glPopMatrix();
			
			gl2.glTranslated(-6, 0, 0);
			if(tool!=null) {
				tool.render(gl2);
			}

		gl2.glPopMatrix();  // wrist

		// pinion B
		gl2.glPushMatrix();
			gl2.glTranslated(WRIST_TO_PINION_X, 0, -WRIST_TO_PINION_Z);
			gl2.glRotated((motionNow.angleB*2+motionNow.angleA)*24.0/8.0, 0,0,1);
			wristPinion.render(gl2);
		gl2.glPopMatrix();

		// pinion A
		gl2.glPushMatrix();
			gl2.glTranslated(WRIST_TO_PINION_X, 0, WRIST_TO_PINION_Z);
			gl2.glScaled(1,1,-1);
			gl2.glRotated((-motionNow.angleA+motionNow.angleB*2.0)*24.0/8.0, 0,0,1);
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
		throw new UnsupportedOperationException();
	}
	
	
	
	private double parseNumber(String str) {
		float f=0;
		try {
			f = Float.parseFloat(str);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return f;
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
			
			String uidString=line.substring(hello.length()).trim();
			System.out.println(">>> UID="+uidString);
			try {
				long uid = Long.parseLong(uidString);
				if(uid==0) {
					robotUID = getNewRobotUID();
				} else {
					robotUID = uid;
				}
				arm5Panel.setUID(robotUID);
			}
			catch(Exception e) {
				e.printStackTrace();
			}

			setDisplayName(ROBOT_NAME+" #"+robotUID);
		}
		
		if( isPortConfirmed ) {
			if(line.startsWith("A")) {
				String items[] = line.split(" ");
				if(items.length>=5) {
					for(int i=0;i<items.length;++i) {
						if(items[i].startsWith("A")) {
							float v = (float)parseNumber(items[i].substring(1));
							if(motionFuture.angleA != v) {
								motionFuture.angleA = v;
								arm5Panel.a1.setText(Float.toString(roundOff(v)));
							}
						} else if(items[i].startsWith("B")) {
							float v = (float)parseNumber(items[i].substring(1));
							if(motionFuture.angleB != v) {
								motionFuture.angleB = v;
								arm5Panel.b1.setText(Float.toString(roundOff(v)));
							}
						} else if(items[i].startsWith("C")) {
							float v = (float)parseNumber(items[i].substring(1));
							if(motionFuture.angleC != v) {
								motionFuture.angleC = v;
								arm5Panel.c1.setText(Float.toString(roundOff(v)));
							}
						} else if(items[i].startsWith("D")) {
							float v = (float)parseNumber(items[i].substring(1));
							if(motionFuture.angleD != v) {
								motionFuture.angleD = v;
								arm5Panel.d1.setText(Float.toString(roundOff(v)));
							}
						} else if(items[i].startsWith("E")) {
							float v = (float)parseNumber(items[i].substring(1));
							if(motionFuture.angleE != v) {
								motionFuture.angleE = v;
								arm5Panel.e1.setText(Float.toString(roundOff(v)));
							}
						}
					}
					
					motionFuture.forwardKinematics();
					motionNow.set(motionFuture);
					updateGUI();
				}
			} else {
				System.out.print("*** "+line);
			}
		}
	}
	

	public void moveBase(Vector3f dp) {
		motionFuture.anchorPosition.set(dp);
	}
	
	
	public void rotateBase(float pan,float tilt) {
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
	
	
	public BoundingVolume [] getBoundingVolumes() {
		// shoulder joint
		Vector3f t1=new Vector3f(motionFuture.baseRight);
		t1.scale(volumes[0].getRadius()/2);
		t1.add(motionFuture.shoulder);
		Vector3f t2=new Vector3f(motionFuture.baseRight);
		t2.scale(-volumes[0].getRadius()/2);
		t2.add(motionFuture.shoulder);
		volumes[0].SetP1(getWorldCoordinatesFor(t1));
		volumes[0].SetP2(getWorldCoordinatesFor(t2));
		// bicep
		volumes[1].SetP1(getWorldCoordinatesFor(motionFuture.shoulder));
		volumes[1].SetP2(getWorldCoordinatesFor(motionFuture.elbow));
		// elbow
		t1.set(motionFuture.baseRight);
		t1.scale(volumes[0].getRadius()/2);
		t1.add(motionFuture.elbow);
		t2.set(motionFuture.baseRight);
		t2.scale(-volumes[0].getRadius()/2);
		t2.add(motionFuture.elbow);
		volumes[2].SetP1(getWorldCoordinatesFor(t1));
		volumes[2].SetP2(getWorldCoordinatesFor(t2));
		// ulna
		volumes[3].SetP1(getWorldCoordinatesFor(motionFuture.elbow));
		volumes[3].SetP2(getWorldCoordinatesFor(motionFuture.wrist));
		// wrist
		t1.set(motionFuture.baseRight);
		t1.scale(volumes[0].getRadius()/2);
		t1.add(motionFuture.wrist);
		t2.set(motionFuture.baseRight);
		t2.scale(-volumes[0].getRadius()/2);
		t2.add(motionFuture.wrist);
		volumes[4].SetP1(getWorldCoordinatesFor(t1));
		volumes[4].SetP2(getWorldCoordinatesFor(t2));
		// finger
		volumes[5].SetP1(getWorldCoordinatesFor(motionFuture.wrist));
		volumes[5].SetP2(getWorldCoordinatesFor(motionFuture.fingerPosition));
		
		return volumes;
	}
	
	
	Vector3f getWorldCoordinatesFor(Vector3f in) {
		Vector3f out = new Vector3f(motionFuture.anchorPosition);
		
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
	
		
	/**
	 * Rotate the point xyz around the line passing through abc with direction uvw following the right hand rule for rotation
	 * http://inside.mines.edu/~gmurray/ArbitraryAxisRotation/ArbitraryAxisRotation.html
	 * Special case where abc=0
	 * @param vec
	 * @param axis
	 * @param angle
	 * @return
	 */
	public static Vector3f rotateAroundAxis(Vector3f vec,Vector3f axis,double angle) {
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
	 * based on http://www.exampledepot.com/egs/java.net/Post.html
	 */
	private long getNewRobotUID() {
		long new_uid = 0;

		try {
			// Send data
			URL url = new URL("https://marginallyclever.com/evil_minion_getuid.php");
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
}
