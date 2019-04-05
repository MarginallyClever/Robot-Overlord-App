package com.marginallyclever.robotOverlord.arm5;

import javax.swing.JPanel;
import javax.vecmath.Vector3d;
import com.jogamp.opengl.GL2;
import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.*;
import com.marginallyclever.robotOverlord.arm5.tool.*;
import com.marginallyclever.robotOverlord.material.Material;
import com.marginallyclever.robotOverlord.model.Model;
import com.marginallyclever.robotOverlord.model.ModelFactory;
import com.marginallyclever.robotOverlord.robot.Robot;
import com.marginallyclever.robotOverlord.robot.RobotKeyframe;

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
extends Robot {
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
	private transient Model anchorModel = null;
	private transient Model shoulderModel = null;
	private transient Model shoulderPinionModel = null;
	private transient Model boomModel = null;
	private transient Model stickModel = null;
	private transient Model wristBoneModel = null;
	private transient Model handModel = null;
	private transient Model wristInteriorModel = null;
	private transient Model wristPinionModel = null;

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
	protected EvilMinionKeyframe motionNow = new EvilMinionKeyframe();
	protected EvilMinionKeyframe motionFuture = new EvilMinionKeyframe();
	
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
		checkAngleLimits(motionNow);
		checkAngleLimits(motionFuture);
		forwardKinematics(motionNow);
		forwardKinematics(motionFuture);
		inverseKinematics(motionNow);
		inverseKinematics(motionFuture);

		matAnchor.setDiffuseColor(0,0,0,1);
		matShoulder.setDiffuseColor(1,0,0,1);
		matBoom.setDiffuseColor(0,0,1,1);
		matStick.setDiffuseColor(1,0,1,1);
		matWrist.setDiffuseColor(0,1,0,1);
		matHand.setDiffuseColor(0.5f,0.5f,0.5f,1);
		
		tool = new EvilMinionToolGripper();
		tool.attachTo(this);
	}
	

	@Override
	protected void loadModels(GL2 gl2) {
		try {
			anchorModel = ModelFactory.createModelFromFilename("/ArmParts.zip:anchor.STL",0.1f);
			shoulderModel = ModelFactory.createModelFromFilename("/ArmParts.zip:shoulder1.STL",0.1f);
			shoulderPinionModel = ModelFactory.createModelFromFilename("/ArmParts.zip:shoulder_pinion.STL",0.1f);
			boomModel = ModelFactory.createModelFromFilename("/ArmParts.zip:boom.STL",0.1f);
			stickModel = ModelFactory.createModelFromFilename("/ArmParts.zip:stick.STL",0.1f);
			wristBoneModel = ModelFactory.createModelFromFilename("/ArmParts.zip:wrist_bone.STL",0.1f);
			handModel = ModelFactory.createModelFromFilename("/ArmParts.zip:wrist_end.STL",0.1f);
			wristInteriorModel = ModelFactory.createModelFromFilename("/ArmParts.zip:wrist_interior.STL",0.1f);
			wristPinionModel = ModelFactory.createModelFromFilename("/ArmParts.zip:wrist_pinion.STL",0.1f);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

    private void readObject(ObjectInputStream inputStream)
            throws IOException, ClassNotFoundException
    {
        inputStream.defaultReadObject();
    }

	
	@Override
	public ArrayList<JPanel> getContextPanel(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getContextPanel(gui);
		
		if(list==null) list = new ArrayList<JPanel>();
		
		arm5Panel = new EvilMinionRobotControlPanel(gui,this);
		list.add(arm5Panel);
		updateGUI();

		ArrayList<JPanel> toolList = tool.getContextPanel(gui);
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
	 * @param delta time since the last update.  usually ~1/30s.
	 */
	protected void updateIK(double delta) {
		boolean changed=false;
		motionFuture.fingerPosition.set(motionNow.fingerPosition);
		final double vel=speed;
		double dp = vel;// * delta;

		double dX=motionFuture.fingerPosition.x;
		double dY=motionFuture.fingerPosition.y;
		double dZ=motionFuture.fingerPosition.z;
		
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
			Vector3d forward = new Vector3d(0,0,1);
			Vector3d right = new Vector3d(1,0,0);
			Vector3d up = new Vector3d();
			
			up.cross(forward,right);
			
			Vector3d of = new Vector3d(forward);
			Vector3d or = new Vector3d(right);
			Vector3d ou = new Vector3d(up);
			
			motionFuture.iku+=ru*dp;
			motionFuture.ikv+=rv*dp;
			motionFuture.ikw+=rw*dp;
			
			Vector3d result;

			result = MathHelper.rotateAroundAxis(forward,of,motionFuture.iku);  // TODO rotating around itself has no effect.
			result = MathHelper.rotateAroundAxis(result,or,motionFuture.ikv);
			result = MathHelper.rotateAroundAxis(result,ou,motionFuture.ikw);
			motionFuture.fingerForward.set(result);

			result = MathHelper.rotateAroundAxis(right,of,motionFuture.iku);
			result = MathHelper.rotateAroundAxis(result,or,motionFuture.ikv);
			result = MathHelper.rotateAroundAxis(result,ou,motionFuture.ikw);
			motionFuture.fingerRight.set(result);
			
			//changed=true;
		}
		
		//if(changed==true && motionFuture.movePermitted()) {
		if(changed) {
			motionFuture.fingerPosition.x = dX;
			motionFuture.fingerPosition.y = dY;
			motionFuture.fingerPosition.z = dZ;
			if(!inverseKinematics(motionFuture)) return;
			if(checkAngleLimits(motionFuture)) {
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
	
	
	protected void updateFK(double delta) {
		boolean changed=false;
		double velcd=speed; // * delta
		double velabe=speed; // * delta

		motionFuture.set(motionNow);
		
		double dE = motionFuture.angleE;
		double dD = motionFuture.angleD;
		double dC = motionFuture.angleC;
		double dB = motionFuture.angleB;
		double dA = motionFuture.angleA;

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
			if(checkAngleLimits(motionFuture)) {
				forwardKinematics(motionFuture);
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

	
	public void updateGUI() {
		Vector3d v = new Vector3d();
		v.set(motionNow.fingerPosition);
		// TODO rotate fingerPosition before adding position
		v.add(getPosition());
		arm5Panel.xPos.setText(Double.toString(MathHelper.roundOff3(v.x)));
		arm5Panel.yPos.setText(Double.toString(MathHelper.roundOff3(v.y)));
		arm5Panel.zPos.setText(Double.toString(MathHelper.roundOff3(v.z)));

		arm5Panel.a1.setText(Double.toString(MathHelper.roundOff3(motionNow.angleA)));
		arm5Panel.b1.setText(Double.toString(MathHelper.roundOff3(motionNow.angleB)));
		arm5Panel.c1.setText(Double.toString(MathHelper.roundOff3(motionNow.angleC)));
		arm5Panel.d1.setText(Double.toString(MathHelper.roundOff3(motionNow.angleD)));
		arm5Panel.e1.setText(Double.toString(MathHelper.roundOff3(motionNow.angleE)));
		
		arm5Panel.a2.setText(Double.toString(MathHelper.roundOff3(motionNow.ik_angleA)));
		arm5Panel.b2.setText(Double.toString(MathHelper.roundOff3(motionNow.ik_angleB)));
		arm5Panel.c2.setText(Double.toString(MathHelper.roundOff3(motionNow.ik_angleC)));
		arm5Panel.d2.setText(Double.toString(MathHelper.roundOff3(motionNow.ik_angleD)));
		arm5Panel.e2.setText(Double.toString(MathHelper.roundOff3(motionNow.ik_angleE)));

		if( tool != null ) tool.updateGUI();
	}
	
	
	protected void sendChangeToRealMachine() {
		if(!isPortConfirmed) return;
		
		
		String str="";
		if(motionFuture.angleA!=motionNow.angleA) {
			str+=" A"+MathHelper.roundOff3(motionFuture.angleA);
		}
		if(motionFuture.angleB!=motionNow.angleB) {
			str+=" B"+MathHelper.roundOff3(motionFuture.angleB);
		}
		if(motionFuture.angleC!=motionNow.angleC) {
			str+=" C"+MathHelper.roundOff3(motionFuture.angleC);
		}
		if(motionFuture.angleD!=motionNow.angleD) {
			str+=" D"+MathHelper.roundOff3(motionFuture.angleD);
		}
		if(motionFuture.angleE!=motionNow.angleE) {
			str+=" E"+MathHelper.roundOff3(motionFuture.angleE);
		}
		
		if(str.length()>0) {
			this.sendLineToRobot("R0"+str);
		}
	}
	
	@Override
	public void prepareMove(double delta) {
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
		super.render(gl2);
		
		gl2.glPushMatrix();
			// TODO rotate model
			
			gl2.glPushMatrix();
				Vector3d p = getPosition();
				gl2.glTranslated(p.x, p.y, p.z);
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
	 * @param gl2 openGL render context
	 */
	protected void renderIK(GL2 gl2) {
		boolean lightOn= gl2.glIsEnabled(GL2.GL_LIGHTING);
		boolean matCoOn= gl2.glIsEnabled(GL2.GL_COLOR_MATERIAL);
		gl2.glDisable(GL2.GL_LIGHTING);
		
		Vector3d ff = new Vector3d();
		ff.set(motionNow.fingerPosition);
		ff.add(motionNow.fingerForward);
		Vector3d fr = new Vector3d();
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
	 * @param gl2 openGL render context
	 */
	protected void renderFK(GL2 gl2) {
		boolean lightOn= gl2.glIsEnabled(GL2.GL_LIGHTING);
		boolean matCoOn= gl2.glIsEnabled(GL2.GL_COLOR_MATERIAL);
		gl2.glDisable(GL2.GL_LIGHTING);

		Vector3d ff = new Vector3d();
		ff.set(motionNow.fingerPosition);
		ff.add(motionNow.fingerForward);
		Vector3d fr = new Vector3d();
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
	 * @param gl2 openGL render context
	 */
	protected void renderModels(GL2 gl2) {
		// anchor
		matAnchor.render(gl2);
		// this rotation is here because the anchor model was built facing the wrong way.
		gl2.glRotated(90, 1, 0, 0);
		
		gl2.glTranslated(0, ANCHOR_ADJUST_Y, 0);
		anchorModel.render(gl2);

		// shoulder (E)
		matShoulder.render(gl2);
		gl2.glTranslated(0, ANCHOR_TO_SHOULDER_Y, 0);
		gl2.glRotated(motionNow.angleE,0,1,0);
		shoulderModel.render(gl2);

		// shoulder pinion
		gl2.glPushMatrix();
			gl2.glTranslated(SHOULDER_TO_PINION_X, SHOULDER_TO_PINION_Y, 0);
			double anchor_gear_ratio = 80.0/8.0;
			gl2.glRotated(motionNow.angleE*anchor_gear_ratio,0,1,0);
			shoulderPinionModel.render(gl2);
		gl2.glPopMatrix();

		// boom (D)
		matBoom.render(gl2);
		gl2.glTranslated(SHOULDER_TO_BOOM_X,SHOULDER_TO_BOOM_Y, 0);
		gl2.glRotated(90-motionNow.angleD,0,0,1);
		gl2.glPushMatrix();
			gl2.glScaled(-1,1,1);
			boomModel.render(gl2);
		gl2.glPopMatrix();

		// stick (C)
		matStick.render(gl2);
		gl2.glTranslated(0.0, BOOM_TO_STICK_Y, 0);
		gl2.glRotated(90+motionNow.angleC,0,0,1);
		gl2.glPushMatrix();
			gl2.glScaled(1,-1,1);
			stickModel.render(gl2);
		gl2.glPopMatrix();

		// to center of wrist
		gl2.glTranslated(STICK_TO_WRIST_X, 0.0, 0);

		// Gear A
		
		gl2.glPushMatrix();
			gl2.glRotated(180+motionNow.angleA-motionNow.angleB*2.0,0,0,1);
			gl2.glRotated(90, 1, 0, 0);
			wristInteriorModel.render(gl2);
		gl2.glPopMatrix();

		// Gear B
		gl2.glPushMatrix();
			gl2.glRotated(180-motionNow.angleB*2.0-motionNow.angleA,0,0,1);
			gl2.glRotated(-90, 1, 0, 0);
			wristInteriorModel.render(gl2);
		gl2.glPopMatrix();

		gl2.glPushMatrix();  // wrist

			gl2.glRotated(-motionNow.angleB+180,0,0,1);

			matWrist.render(gl2);
			// wrist bone
			wristBoneModel.render(gl2);
			
			// tool holder
			gl2.glRotated(motionNow.angleA,1,0,0);

			gl2.glPushMatrix();
				matHand.render(gl2);
				handModel.render(gl2);
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
			wristPinionModel.render(gl2);
		gl2.glPopMatrix();

		// pinion A
		gl2.glPushMatrix();
			gl2.glTranslated(WRIST_TO_PINION_X, 0, WRIST_TO_PINION_Z);
			gl2.glScaled(1,1,-1);
			gl2.glRotated((-motionNow.angleA+motionNow.angleB*2.0)*24.0/8.0, 0,0,1);
			wristPinionModel.render(gl2);
		gl2.glPopMatrix();
	}
	
	
	protected void drawBounds(GL2 gl2) {
		throw new UnsupportedOperationException();
	}
	

	public void setModeAbsolute() {
		if(connection!=null) this.sendLineToRobot("G90");
	}
	
	
	public void setModeRelative() {
		if(connection!=null) this.sendLineToRobot("G91");
	}
	
	
	@Override
	// override this method to check that the software is connected to the right type of robot.
	public void dataAvailable(NetworkConnection arg0,String line) {
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
							double v = StringHelper.parseNumber(items[i].substring(1));
							if(motionFuture.angleA != v) {
								motionFuture.angleA = v;
								arm5Panel.a1.setText(Double.toString(MathHelper.roundOff3(v)));
							}
						} else if(items[i].startsWith("B")) {
							double v = StringHelper.parseNumber(items[i].substring(1));
							if(motionFuture.angleB != v) {
								motionFuture.angleB = v;
								arm5Panel.b1.setText(Double.toString(MathHelper.roundOff3(v)));
							}
						} else if(items[i].startsWith("C")) {
							double v = StringHelper.parseNumber(items[i].substring(1));
							if(motionFuture.angleC != v) {
								motionFuture.angleC = v;
								arm5Panel.c1.setText(Double.toString(MathHelper.roundOff3(v)));
							}
						} else if(items[i].startsWith("D")) {
							double v = StringHelper.parseNumber(items[i].substring(1));
							if(motionFuture.angleD != v) {
								motionFuture.angleD = v;
								arm5Panel.d1.setText(Double.toString(MathHelper.roundOff3(v)));
							}
						} else if(items[i].startsWith("E")) {
							double v = StringHelper.parseNumber(items[i].substring(1));
							if(motionFuture.angleE != v) {
								motionFuture.angleE = v;
								arm5Panel.e1.setText(Double.toString(MathHelper.roundOff3(v)));
							}
						}
					}
					
					forwardKinematics(motionFuture);
					motionNow.set(motionFuture);
					updateGUI();
				}
			} else {
				System.out.print("*** "+line);
			}
		}
	}
	

	public void moveBase(Vector3d dp) {
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
		Vector3d t1=new Vector3d(motionFuture.baseRight);
		t1.scale(volumes[0].getRadius()/2);
		t1.add(motionFuture.shoulder);
		Vector3d t2=new Vector3d(motionFuture.baseRight);
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
	
	
	Vector3d getWorldCoordinatesFor(Vector3d in) {
		Vector3d out = new Vector3d(motionFuture.anchorPosition);
		
		Vector3d tempx = new Vector3d(motionFuture.baseForward);
		tempx.scale(in.x);
		out.add(tempx);

		Vector3d tempy = new Vector3d(motionFuture.baseRight);
		tempy.scale(-in.y);
		out.add(tempy);

		Vector3d tempz = new Vector3d(motionFuture.baseUp);
		tempz.scale(in.z);
		out.add(tempz);
				
		return out;
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
	
	
	// TODO check for collisions with http://geomalgorithms.com/a07-_distance.html#dist3D_Segment_to_Segment ?
	public boolean movePermitted(EvilMinionKeyframe keyframe) {
		// don't hit floor?
		// don't hit ceiling?

		// check far limit
		// seems doable
		if(!inverseKinematics(keyframe)) return false;
		// angle are good?
		if(!checkAngleLimits(keyframe)) return false;

		// OK
		return true;
	}
	
	
	protected boolean checkAngleLimits(EvilMinionKeyframe keyframe) {
		// machine specific limits
		//a
		//if (angleA < -180) return false;
		//if (angleA >  180) return false;
		//b
		if (keyframe.angleB <      72.90) keyframe.angleB = 72.90f;
		if (keyframe.angleB >  360-72.90) keyframe.angleB = 360-72.90f;
		//c
		if (keyframe.angleC <   50.57) keyframe.angleC = 50.57f;
		if (keyframe.angleC >  160.31) keyframe.angleC = 160.31f;
		//d
		if (keyframe.angleD <   87.85) keyframe.angleD = 87.85f;
		if (keyframe.angleD >  173.60) keyframe.angleD = 173.60f;
		//e
		//if (angleE < 180-165) return false;
		//if (angleE > 180+165) return false;

		return true;
	}
	
	
	/**
	 * Find the arm joint angles that would put the finger at the desired location.
	 * @return false if successful, true if the IK solution cannot be found.
	 */
	protected boolean inverseKinematics(EvilMinionKeyframe keyframe) {
		double aa,bb,cc,dd,ee;
		
		Vector3d v0 = new Vector3d();
		Vector3d v1 = new Vector3d();
		Vector3d v2 = new Vector3d();
		Vector3d planar = new Vector3d();
		Vector3d planeNormal = new Vector3d();
		Vector3d planeRight = new Vector3d(0,0,1);

		// Finger position is never on x=y=0 line, so this is safe.
		planar.set(keyframe.fingerPosition);
		planar.z=0;
		planar.normalize();
		planeNormal.set(-planar.y,planar.x,0);
		planeNormal.normalize();
		
		// Find E
		ee = Math.atan2(planar.y, planar.x);
		ee = MathHelper.capRotationRadians(ee);
		keyframe.ik_angleE = (float)Math.toDegrees(ee);

		keyframe.ik_shoulder.set(0,0,(float)(EvilMinionRobot.ANCHOR_ADJUST_Y+EvilMinionRobot.ANCHOR_TO_SHOULDER_Y));
		keyframe.ik_boom.set((float)EvilMinionRobot.SHOULDER_TO_BOOM_X*(float)Math.cos(ee),
							 (float)EvilMinionRobot.SHOULDER_TO_BOOM_X*(float)Math.sin(ee),
							 (float)EvilMinionRobot.SHOULDER_TO_BOOM_Y);
		keyframe.ik_boom.add(keyframe.ik_shoulder);
		
		// Find wrist 
		keyframe.ik_wrist.set(keyframe.fingerForward);
		keyframe.ik_wrist.scale(EvilMinionRobot.WRIST_TO_TOOL_X);
		keyframe.ik_wrist.add(keyframe.fingerPosition);
				
		// Find elbow by using intersection of circles.
		// http://mathworld.wolfram.com/Circle-CircleIntersection.html
		// x = (dd-rr+RR) / (2d)
		v0.set(keyframe.ik_wrist);
		v0.sub(keyframe.ik_boom);
		double d = v0.length();
		double R = (float)Math.abs(EvilMinionRobot.BOOM_TO_STICK_Y);
		double r = (float)Math.abs(EvilMinionRobot.STICK_TO_WRIST_X);
		if( d > R+r ) {
			// impossibly far away
			return false;
		}/*
		if( d < Math.abs(R-r) ) {
			// impossibly close?
			return false;
		}*/
		
		double x = (d*d - r*r + R*R ) / (2*d);
		if( x > R ) {
			// would cause Math.sqrt(a negative number)
			return false;
		}
		v0.normalize();
		keyframe.ik_elbow.set(v0);
		keyframe.ik_elbow.scale(x);
		keyframe.ik_elbow.add(keyframe.ik_boom);
		// v1 is now at the intersection point between ik_wrist and ik_boom
		float a = (float)( Math.sqrt( R*R - x*x ) );
		v1.cross(planeNormal, v0);
		v1.scale(-a);
		keyframe.ik_elbow.add(v1);

		// find boom angle (D)
		v0.set(keyframe.ik_elbow);
		v0.sub(keyframe.ik_boom);
		x = -planar.dot(v0);
		double y = planeRight.dot(v0);
		dd = Math.atan2(y,x);
		dd = MathHelper.capRotationRadians(dd);
		keyframe.ik_angleD = (float)Math.toDegrees(dd);
		
		// find elbow angle (C)
		planar.set(v0);
		planar.normalize();
		planeRight.cross(planeNormal,v0);
		planeRight.normalize();
		v0.set(keyframe.ik_wrist);
		v0.sub(keyframe.ik_elbow);
		x = -planar.dot(v0);
		y = planeRight.dot(v0);
		cc = Math.atan2(y,x);
		cc = MathHelper.capRotationRadians(cc);
		keyframe.ik_angleC = (float)Math.toDegrees(cc);
		
		// find wrist angle (B)
		planar.set(keyframe.ik_wrist);
		planar.sub(keyframe.ik_elbow);
		planar.normalize();
		planeRight.cross(planeNormal,v0);
		planeRight.normalize();
		v0.set(keyframe.fingerPosition);
		v0.sub(keyframe.ik_wrist);
		x = -planar.dot(v0);
		y = -planeRight.dot(v0);
		bb = Math.atan2(y,x);
		bb = MathHelper.capRotationRadians(bb);
		keyframe.ik_angleB = (float)Math.toDegrees(bb);
		
		// find wrist rotation (A)
		v0.set(keyframe.fingerPosition);
		v0.sub(keyframe.ik_wrist);
		v0.normalize();
		v1.set(planeNormal);
		v2.cross(planeNormal,v0);
		v0.set(keyframe.fingerRight);
		
		x = v2.dot(v0);
		y = -v1.dot(v0);
		aa = Math.atan2(y,x)-bb-Math.PI/2.0;
		aa = MathHelper.capRotationRadians(aa);
		keyframe.ik_angleA = (float)Math.toDegrees(aa);
		
		keyframe.angleA=keyframe.ik_angleA;
		keyframe.angleB=keyframe.ik_angleB;
		keyframe.angleC=keyframe.ik_angleC;
		keyframe.angleD=keyframe.ik_angleD;
		keyframe.angleE=keyframe.ik_angleE;

		return true;
	}
	
	/**
	 * Calculate the finger location from the angles at each joint
	 * @param state
	 */
	protected void forwardKinematics(EvilMinionKeyframe keyframe) {
		double e = Math.toRadians(keyframe.angleE);
		double d = Math.toRadians(180-keyframe.angleD);
		double c = Math.toRadians(keyframe.angleC+180);
		double b = Math.toRadians(180-keyframe.angleB);
		double a = Math.toRadians(keyframe.angleA);
		
		Vector3d v0 = new Vector3d(0,0,(float)(EvilMinionRobot.ANCHOR_ADJUST_Y+EvilMinionRobot.ANCHOR_TO_SHOULDER_Y));
		Vector3d v1 = new Vector3d((float)EvilMinionRobot.SHOULDER_TO_BOOM_X*(float)Math.cos(e),
									(float)EvilMinionRobot.SHOULDER_TO_BOOM_X*(float)Math.sin(e),
									(float)EvilMinionRobot.SHOULDER_TO_BOOM_Y);
		Vector3d planar = new Vector3d((float)Math.cos(e),(float)Math.sin(e),0);
		planar.normalize();
		Vector3d planeNormal = new Vector3d(-v1.y,v1.x,0);
		planeNormal.normalize();
		Vector3d planarRight = new Vector3d();
		planarRight.cross(planar, planeNormal);
		planarRight.normalize();

		// anchor to shoulder
		keyframe.shoulder.set(v0);
		
		// shoulder to boom
		v1.add(v0);
		keyframe.boom.set(v1);
		
		// boom to elbow
		v0.set(v1);
		v1.set(planar);
		v1.scale( (float)( EvilMinionRobot.BOOM_TO_STICK_Y * Math.cos(d) ) );
		Vector3d v2 = new Vector3d();
		v2.set(planarRight);
		v2.scale( (float)( EvilMinionRobot.BOOM_TO_STICK_Y * Math.sin(d) ) );
		v1.add(v2);
		v1.add(v0);
		
		keyframe.elbow.set(v1);
		
		// elbow to wrist
		planar.set(v0);
		planar.sub(v1);
		planar.normalize();
		planarRight.cross(planar, planeNormal);
		planarRight.normalize();
		v0.set(v1);

		v1.set(planar);
		v1.scale( (float)( EvilMinionRobot.STICK_TO_WRIST_X * Math.cos(c) ) );
		v2.set(planarRight);
		v2.scale( (float)( EvilMinionRobot.STICK_TO_WRIST_X * Math.sin(c) ) );
		v1.add(v2);
		v1.add(v0);
		
		keyframe.wrist.set(v1);

		// wrist to finger
		planar.set(v0);
		planar.sub(v1);
		planar.normalize();
		planarRight.cross(planar, planeNormal);
		planarRight.normalize();
		v0.set(v1);

		v1.set(planar);
		v1.scale( (float)( EvilMinionRobot.WRIST_TO_TOOL_X * Math.cos(b) ) );
		v2.set(planarRight);
		v2.scale( (float)( EvilMinionRobot.WRIST_TO_TOOL_X * Math.sin(b) ) );
		v1.add(v2);
		v1.add(v0);

		keyframe.fingerPosition.set(v1);

		// finger rotation
		planarRight.set(planeNormal);
		planeNormal.set(v1);
		planeNormal.sub(v0);
		planeNormal.normalize();
		planar.cross(planeNormal,planarRight);
		v0.set(v1);

		v1.set(planar);
		v1.scale( (float)( EvilMinionRobot.WRIST_TO_TOOL_Y * Math.cos(a-b) ) );
		v2.set(planarRight);
		v2.scale( (float)( EvilMinionRobot.WRIST_TO_TOOL_Y * Math.sin(a-b) ) );
		v1.add(v2);
		v1.normalize();
		
		v0.set(keyframe.fingerPosition);
		v0.sub(keyframe.wrist);

		keyframe.fingerForward.set(planeNormal);
		
		keyframe.fingerRight.cross(v1, planeNormal);
		keyframe.fingerRight.normalize();
	}


	@Override
	public RobotKeyframe createKeyframe() {
		return new EvilMinionKeyframe();
	}
}
