package com.marginallyclever.robotOverlord.pTTPTPRobot;

import javax.swing.JPanel;
import javax.vecmath.Vector3f;
import com.jogamp.opengl.GL2;
import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.*;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.model.Model;
import com.marginallyclever.robotOverlord.model.ModelFactory;
import com.marginallyclever.robotOverlord.pTTPTPRobot.tool.*;
import com.marginallyclever.robotOverlord.robot.Robot;

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


/**
 * Robot Overlord simulation of Sixi 6DOF robot arm.
 * 
 * @author Dan Royer <dan @ marinallyclever.com>
 */
public class PTTPTPRobot
extends Robot {
	/**
	 * 
	 */
	private final static long serialVersionUID = -3644731265897692399L;
	private final static int NUM_SEGMENTS = 7;
	private final static String hello = "HELLO WORLD! I AM PTTPTP #";
	private final static String ROBOT_NAME = "PTTPTP 6DOF arm";
		
	public class Segment extends Entity {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public transient Model model = null;
		public Material mat = new Material();
		public Cylinder collision = new Cylinder();
		public float angleMin,angleMax,angleAdjust=0,angleNow;
		public Vector3f axisOfRotation = new Vector3f();
		public Vector3f offsetFromParent = new Vector3f();
		
		Segment() {
			super();
			mat.setDiffuseColor(1,1,1,1);
		}
		
		double offsetLength() {
			return offsetFromParent.length();
		}
	};

	public Material matFaded = new Material();
	public Material matSelected = new Material();
	public final static float EPSILON = 0.00001f;
	public float partScale=0.1f;
	
	// machine ID
	private long robotUID;

	private Segment [] segments = new Segment[NUM_SEGMENTS];
	
	// currently attached tool
	private PTTPTPTool tool = null;
	
	// motion states
	private PTTPTPRobotKeyframe motionNow = new PTTPTPRobotKeyframe();
	private PTTPTPRobotKeyframe motionFuture = new PTTPTPRobotKeyframe();
	
	// keyboard history
	private float aDir = 0.0f;
	private float bDir = 0.0f;
	private float cDir = 0.0f;
	private float dDir = 0.0f;
	private float eDir = 0.0f;
	private float fDir = 0.0f;

	private float xDir = 0.0f;
	private float yDir = 0.0f;
	private float zDir = 0.0f;
	private float uDir = 0.0f;
	private float vDir = 0.0f;
	private float wDir = 0.0f;

	// machine logic states
	private boolean armMoved = false;
	private boolean isPortConfirmed=false;
	private double speed=2;

	// visual debugging
	private boolean isRenderFKOn=true;
	private boolean isRenderIKOn=true;
	private boolean isRenderDebugOn=true;
	private int pickHighlight=NUM_SEGMENTS;

	// gui
	protected transient PTTPTPRobotControlPanel armPanel=null;
	
	public PTTPTPRobot() {
		super();
		
		for(int i=0;i<NUM_SEGMENTS;++i) {
			segments[i] = new Segment();
		}

		matFaded.setDiffuseColor(1,1,1, 0.5f);
		matSelected.setDiffuseColor(1,0.5f,0.5f, 1);

		segments[0].offsetFromParent.set(0, 0, 0);
		segments[1].offsetFromParent.set(0, 0,25);
		segments[2].offsetFromParent.set(0,-5,25);
		segments[3].offsetFromParent.set(0, 5,10);
		segments[4].offsetFromParent.set(0, 0,10);
		segments[5].offsetFromParent.set(0, 0, 5);
		segments[6].offsetFromParent.set(0, 0, 0);

		segments[0].axisOfRotation.set(0,0,1);
		segments[1].axisOfRotation.set(1,0,0);
		segments[2].axisOfRotation.set(1,0,0);
		segments[3].axisOfRotation.set(0,0,1);
		segments[4].axisOfRotation.set(1,0,0);
		segments[5].axisOfRotation.set(0,0,1);
		segments[6].axisOfRotation.set(0,0,1);
		
		setDisplayName(ROBOT_NAME);
		
		rotateBase(0,0);/*
		checkAngleLimits(motionNow);
		checkAngleLimits(motionFuture);
		forwardKinematics(motionNow);
		forwardKinematics(motionFuture);
		inverseKinematics(motionNow);
		inverseKinematics(motionFuture);*/

		tool = new PTTPTPToolGripper();
		tool.attachTo(this);
	}
	
	@Override
	public boolean hasPickName(int name) {
		for(int i=0;i<NUM_SEGMENTS;++i) {
			if(segments[i].hasPickName(name)) {
				pickHighlight=i;
				return true;
			}
		}
		if(this.getPickName()==name) return true;
		return false;
	}
	
	public void unPick() {
		pickHighlight = NUM_SEGMENTS;
	}
	
	public void setPart(int index,String arg0) {
		if(index>=0 && index<NUM_SEGMENTS) {
			try {
				segments[index].model = ModelFactory.createModelFromFilename(arg0);
				segments[index].model.setScale(partScale);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public String getPart(int index) {
		if(segments[index].model!=null) return segments[index].model.getSourceName();
		return null;
	}
	
	public void setPartScale(float scale) {
		partScale=scale;
		for(int i=0;i<NUM_SEGMENTS;++i) {
			if(segments[i].model!=null) {
				segments[i].model.setScale(scale);
			}
		}
	}
	public float getPartScale() {
		return partScale;
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
		
		armPanel = new PTTPTPRobotControlPanel(gui,this);
		list.add(armPanel);
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
		uDir=0;
		vDir=0;
		wDir=0;
	}
	
	private void disableFK() {	
		aDir=0;
		bDir=0;
		cDir=0;
		dDir=0;
		eDir=0;
		fDir=0;
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

	public void moveF(float dir) {
		fDir=dir;
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

	public void moveU(float dir) {
		uDir=dir;
		disableFK();
	}

	public void moveV(float dir) {
		vDir=dir;
		disableFK();
	}

	public void moveW(float dir) {
		wDir=dir;
		disableFK();
	}

	/**
	 * update the desired finger location
	 * @param delta the time since the last update.  Typically ~1/30s
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
		float ru=motionFuture.ikU;
		float rv=motionFuture.ikV;
		float rw=motionFuture.ikW;
		boolean hasTurned=false;

		if (uDir!=0) {
			ru += uDir * dp;
			changed=true;
			hasTurned=true;
			uDir=0;
		}
		if (vDir!=0) {
			rv += vDir * dp;
			changed=true;
			hasTurned=true;
			vDir=0;
		}
		if (wDir!=0) {
			rw += wDir * dp;
			changed=true;
			hasTurned=true;
			wDir=0;
		}


		if(hasTurned) {
			// On a 3-axis robot when homed the forward axis of the finger tip is pointing downward.
			// More complex arms start from the same assumption.
			Vector3f forward = new Vector3f(0,0,1);
			Vector3f right = new Vector3f(1,0,0);
			Vector3f up = new Vector3f();
			
			up.cross(forward,right);
			
			Vector3f of = new Vector3f(forward);
			Vector3f or = new Vector3f(right);
			Vector3f ou = new Vector3f(up);
			
			motionFuture.ikU=ru;
			motionFuture.ikV=rv;
			motionFuture.ikW=rw;
			
			Vector3f result;

			result = MathHelper.rotateAroundAxis(forward,of,(float)Math.toRadians(motionFuture.ikU));  // TODO rotating around itself has no effect.
			result = MathHelper.rotateAroundAxis(result ,or,(float)Math.toRadians(motionFuture.ikV));
			result = MathHelper.rotateAroundAxis(result ,ou,(float)Math.toRadians(motionFuture.ikW));
			motionFuture.fingerForward.set(result);

			result = MathHelper.rotateAroundAxis(right ,of,(float)Math.toRadians(motionFuture.ikU));
			result = MathHelper.rotateAroundAxis(result,or,(float)Math.toRadians(motionFuture.ikV));
			result = MathHelper.rotateAroundAxis(result,ou,(float)Math.toRadians(motionFuture.ikW));
			motionFuture.fingerRight.set(result);
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
	
	protected void updateFK(float delta) {
		boolean changed=false;
		float velcd=(float)speed; // * delta
		float velabe=(float)speed; // * delta

		motionFuture.set(motionNow);
		
		float dF = motionFuture.angleF;
		float dE = motionFuture.angleE;
		float dD = motionFuture.angleD;
		float dC = motionFuture.angleC;
		float dB = motionFuture.angleB;
		float dA = motionFuture.angleA;

		if (fDir!=0) {
			dF += velabe * fDir;
			changed=true;
			fDir=0;
		}
		
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
			motionFuture.angleF=dF;
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

	protected float roundOff(float v) {
		float SCALE = 1000.0f;
		
		return Math.round(v*SCALE)/SCALE;
	}
	
	public void updateGUI() {
		Vector3f v = new Vector3f();
		v.set(motionNow.fingerPosition);
		v.add(getPosition());
		armPanel.xPos.setText(Float.toString(roundOff(v.x)));
		armPanel.yPos.setText(Float.toString(roundOff(v.y)));
		armPanel.zPos.setText(Float.toString(roundOff(v.z)));
		armPanel.uPos.setText(Float.toString(roundOff(motionNow.ikU)));
		armPanel.vPos.setText(Float.toString(roundOff(motionNow.ikV)));
		armPanel.wPos.setText(Float.toString(roundOff(motionNow.ikW)));

		armPanel.a1.setText(Float.toString(roundOff(motionNow.angleA)));
		armPanel.b1.setText(Float.toString(roundOff(motionNow.angleB)));
		armPanel.c1.setText(Float.toString(roundOff(motionNow.angleC)));
		armPanel.d1.setText(Float.toString(roundOff(motionNow.angleD)));
		armPanel.e1.setText(Float.toString(roundOff(motionNow.angleE)));
		armPanel.f1.setText(Float.toString(roundOff(motionNow.angleF)));

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
		if(motionFuture.angleF!=motionNow.angleF) {
			str+=" F"+roundOff(motionFuture.angleF);
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
		
		segments[0].angleNow = motionNow.angleF;
		segments[1].angleNow = motionNow.angleE;
		segments[2].angleNow = motionNow.angleD;
		segments[3].angleNow = motionNow.angleC;
		segments[4].angleNow = motionNow.angleB;
		segments[5].angleNow = motionNow.angleA;

		if(armMoved) {
			if( this.isReadyToReceive ) {
				armMoved=false;
			}
		}
	}
	
	@Override
	public void render(GL2 gl2) {
		super.render(gl2);
		
		gl2.glPushMatrix();
			// TODO rotate model
			Vector3f p = getPosition();
			gl2.glTranslatef(p.x, p.y, p.z);
			
			gl2.glPushMatrix();
				renderModels(gl2);
			gl2.glPopMatrix();

			isRenderDebugOn=false;
			if(isRenderDebugOn) {
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
			}
		gl2.glPopMatrix();
	}
	
	/**
	 * Visualize the inverse kinematics calculations
	 * @param gl2 the OpenGL render context
	 */
	protected void renderIK(GL2 gl2) {
		boolean lightOn= gl2.glIsEnabled(GL2.GL_LIGHTING);
		boolean matCoOn= gl2.glIsEnabled(GL2.GL_COLOR_MATERIAL);
		gl2.glDisable(GL2.GL_LIGHTING);
		
		Vector3f ff = new Vector3f();
		ff.set(motionNow.fingerForward);
		ff.scale(5);
		ff.add(motionNow.fingerPosition);
		Vector3f fr = new Vector3f();
		fr.set(motionNow.fingerRight);
		fr.scale(15);
		fr.add(motionNow.fingerPosition);
		
		gl2.glColor4f(1,0,0,1);

		gl2.glBegin(GL2.GL_LINE_STRIP);
		gl2.glVertex3d(0,0,0);
		gl2.glVertex3d(motionNow.base.x,motionNow.base.y,motionNow.base.z);
		gl2.glVertex3d(motionNow.shoulder.x,motionNow.shoulder.y,motionNow.shoulder.z);
		gl2.glVertex3d(motionNow.elbow.x,motionNow.elbow.y,motionNow.elbow.z);
		gl2.glVertex3d(motionNow.wrist.x,motionNow.wrist.y,motionNow.wrist.z);
		gl2.glVertex3d(motionNow.fingerPosition.x,motionNow.fingerPosition.y,motionNow.fingerPosition.z);
		gl2.glEnd();

		gl2.glBegin(GL2.GL_LINES);
		gl2.glColor4f(0,0.8f,1,1);
		gl2.glVertex3d(motionNow.fingerPosition.x,motionNow.fingerPosition.y,motionNow.fingerPosition.z);
		gl2.glVertex3d(ff.x,ff.y,ff.z);

		gl2.glColor4f(0,0,1,1);
		gl2.glVertex3d(motionNow.fingerPosition.x,motionNow.fingerPosition.y,motionNow.fingerPosition.z);
		gl2.glVertex3d(fr.x,fr.y,fr.z);
		gl2.glEnd();
		/*
		// finger tip
		setColor(gl2,1,0.8f,0,1);
		PrimitiveSolids.drawStar(gl2, motionNow.fingerPosition );
		setColor(gl2,0,0.8f,1,1);
		PrimitiveSolids.drawStar(gl2, ff );
		setColor(gl2,0,0,1,1);
		PrimitiveSolids.drawStar(gl2, fr );
		

		Vector3f towardsElbow = new Vector3f(motionNow.ikElbow);
		towardsElbow.sub(motionNow.ikShoulder);
		towardsElbow.normalize();
		
		Vector3f v0 = new Vector3f();
		Vector3f v1 = new Vector3f();

		Vector3f facingDirection = new Vector3f(motionNow.ikWrist.x,motionNow.ikWrist.y,0);
		facingDirection.normalize();
		Vector3f up = new Vector3f(0,0,1);
		Vector3f planarRight = new Vector3f();
		planarRight.cross(facingDirection, up);
		planarRight.normalize();
		// angleC is the ulna rotation
		Vector3f towardsWrist = new Vector3f(motionNow.ikWrist);
		towardsWrist.sub(motionNow.ikElbow);
		
		v0.set(towardsWrist);
		v0.normalize();
		v1.cross(planarRight,v0);
		v1.normalize();
		Vector3f towardsFinger = new Vector3f(motionNow.fingerForward);
		Vector3f towardsFingerAdj = new Vector3f(motionNow.fingerForward);
		towardsFingerAdj.normalize();
		float tf = v0.dot(towardsFingerAdj);
		// can calculate angle
		v0.scale(tf);
		towardsFingerAdj.sub(v0);
		towardsFingerAdj.normalize();
		
		// angleA is the hand rotation
		v0.cross(towardsFingerAdj,towardsWrist);
		v0.normalize();
		v1.cross(v0, towardsFinger);
		
		towardsWrist.sub(motionNow.ikElbow);
		towardsWrist.normalize();

		v0.cross(towardsFingerAdj,towardsWrist);
		v0.normalize();
		v1.cross(v0, towardsFinger);

		gl2.glBegin(GL2.GL_LINES);
		gl2.glColor3f(0,0.5f,1);
		gl2.glVertex3f(	motionNow.ikWrist.x,
						motionNow.ikWrist.y,
						motionNow.ikWrist.z);
		gl2.glVertex3f(	motionNow.ikWrist.x+v0.x*10,
						motionNow.ikWrist.y+v0.y*10,
						motionNow.ikWrist.z+v0.z*10);

		gl2.glColor3f(1,0.5f,0);
		gl2.glVertex3f(	motionNow.ikWrist.x,
						motionNow.ikWrist.y,
						motionNow.ikWrist.z);
		gl2.glVertex3f(	motionNow.ikWrist.x+v1.x*10,
						motionNow.ikWrist.y+v1.y*10,
						motionNow.ikWrist.z+v1.z*10);

		gl2.glEnd();*/
		/*
		gl2.glBegin(GL2.GL_LINES);
		gl2.glColor3f(0,1,1);
		gl2.glVertex3f(	motionNow.ikWrist.x,
						motionNow.ikWrist.y,
						motionNow.ikWrist.z);
		gl2.glVertex3f(	motionNow.ikWrist.x+planarRight.x*10,
						motionNow.ikWrist.y+planarRight.y*10,
						motionNow.ikWrist.z+planarRight.z*10);

		gl2.glColor3f(1,0,1);
		gl2.glVertex3f(	motionNow.ikWrist.x,
						motionNow.ikWrist.y,
						motionNow.ikWrist.z);
		gl2.glVertex3f(	motionNow.ikWrist.x+v1.x*10,
						motionNow.ikWrist.y+v1.y*10,
						motionNow.ikWrist.z+v1.z*10);
		gl2.glColor3f(1,1,1);
		gl2.glVertex3f(	motionNow.ikWrist.x,
						motionNow.ikWrist.y,
						motionNow.ikWrist.z);
		gl2.glVertex3f(	motionNow.ikWrist.x+towardsFingerAdj.x*10,
						motionNow.ikWrist.y+towardsFingerAdj.y*10,
						motionNow.ikWrist.z+towardsFingerAdj.z*10);
		gl2.glColor3f(0.6f,0.6f,0.6f);
		gl2.glVertex3f(	motionNow.ikWrist.x,
						motionNow.ikWrist.y,
						motionNow.ikWrist.z);
		gl2.glVertex3f(	motionNow.ikWrist.x+motionNow.fingerForward.x*10,
						motionNow.ikWrist.y+motionNow.fingerForward.y*10,
						motionNow.ikWrist.z+motionNow.fingerForward.z*10);
		gl2.glEnd();
		*/
		if(lightOn) gl2.glEnable(GL2.GL_LIGHTING);
		if(matCoOn) gl2.glEnable(GL2.GL_COLOR_MATERIAL);
	}
	
	/**
	 * Draw the arm without calling glRotate to prove forward kinematics are correct.
	 * @param gl2 the OpenGL render context
	 */
	protected void renderFK(GL2 gl2) {
		boolean lightOn= gl2.glIsEnabled(GL2.GL_LIGHTING);
		boolean matCoOn= gl2.glIsEnabled(GL2.GL_COLOR_MATERIAL);
		gl2.glDisable(GL2.GL_LIGHTING);

		gl2.glPushMatrix();
		gl2.glTranslated(motionNow.base.x,motionNow.base.y,motionNow.base.z);
		
		Vector3f ff = new Vector3f();
		ff.set(motionNow.fingerForward);
		ff.scale(5);
		ff.add(motionNow.fingerPosition);
		Vector3f fr = new Vector3f();
		fr.set(motionNow.fingerRight);
		fr.scale(15);
		fr.add(motionNow.fingerPosition);
		
		gl2.glColor4f(0,0,0,1);
		gl2.glBegin(GL2.GL_LINE_STRIP);
		gl2.glVertex3d(0,0,0);
		gl2.glVertex3d(motionNow.shoulder.x,motionNow.shoulder.y,motionNow.shoulder.z);
		//gl2.glVertex3d(motionNow.bicep.x,motionNow.bicep.y,motionNow.bicep.z);
		gl2.glVertex3d(motionNow.elbow.x,motionNow.elbow.y,motionNow.elbow.z);
		gl2.glVertex3d(motionNow.wrist.x,motionNow.wrist.y,motionNow.wrist.z);
		gl2.glVertex3d(motionNow.fingerPosition.x,motionNow.fingerPosition.y,motionNow.fingerPosition.z);
		gl2.glEnd();

		gl2.glBegin(GL2.GL_LINES);
		gl2.glColor4f(0,0.8f,1,1);
		gl2.glVertex3d(motionNow.fingerPosition.x,motionNow.fingerPosition.y,motionNow.fingerPosition.z);
		gl2.glVertex3d(ff.x,ff.y,ff.z);

		gl2.glColor4f(0,0,1,1);
		gl2.glVertex3d(motionNow.fingerPosition.x,motionNow.fingerPosition.y,motionNow.fingerPosition.z);
		gl2.glVertex3d(fr.x,fr.y,fr.z);
		gl2.glEnd();

		// finger tip
		gl2.glColor4f(1,0.8f,0,1);		PrimitiveSolids.drawStar(gl2, motionNow.fingerPosition );
		gl2.glColor4f(0,0.8f,1,1);		PrimitiveSolids.drawStar(gl2, ff );
		gl2.glColor4f(0,0,1,1);			PrimitiveSolids.drawStar(gl2, fr );
	
		if(lightOn) gl2.glEnable(GL2.GL_LIGHTING);
		if(matCoOn) gl2.glEnable(GL2.GL_COLOR_MATERIAL);

		gl2.glPopMatrix();
	}
		
	/**
	 * Draw the physical model according to the angle values in the motionNow state.
	 * @param gl2 the openGL render context
	 */
	protected void renderModels(GL2 gl2) {
		
		Vector3f offset = new Vector3f();
		int i;
		for(i=0;i<NUM_SEGMENTS;++i) {
			PrimitiveSolids.drawStar(gl2,new Vector3f(0,0,0),10);
			if(pickHighlight<NUM_SEGMENTS) {
				if(pickHighlight==i) {
					matSelected.render(gl2);
				} else {
					matFaded.render(gl2);
				}
			} else {
				segments[i].mat.render(gl2);
			}
			if(segments[i].model!=null) {
				gl2.glPushName(segments[i].getPickName());
				segments[i].model.adjustOrigin(offset);
				segments[i].model.render(gl2);
				gl2.glPopName();
			}
			gl2.glTranslatef(segments[i].offsetFromParent.x,
							 segments[i].offsetFromParent.y,
							 segments[i].offsetFromParent.z);
			offset.sub(segments[i].offsetFromParent);
			if( !Float.isNaN(segments[i].angleNow)) {
				gl2.glRotated(segments[i].angleAdjust+segments[i].angleNow,
						segments[i].axisOfRotation.x,
						segments[i].axisOfRotation.y,
						segments[i].axisOfRotation.z);  // pan
			}
		}
		
		if(tool!=null) {
			gl2.glRotated(90, 0, 1, 0);
			tool.render(gl2);
		}
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
				armPanel.setUID(robotUID);
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
								armPanel.a1.setText(Float.toString(roundOff(v)));
							}
						} else if(items[i].startsWith("B")) {
							float v = (float)parseNumber(items[i].substring(1));
							if(motionFuture.angleB != v) {
								motionFuture.angleB = v;
								armPanel.b1.setText(Float.toString(roundOff(v)));
							}
						} else if(items[i].startsWith("C")) {
							float v = (float)parseNumber(items[i].substring(1));
							if(motionFuture.angleC != v) {
								motionFuture.angleC = v;
								armPanel.c1.setText(Float.toString(roundOff(v)));
							}
						} else if(items[i].startsWith("D")) {
							float v = (float)parseNumber(items[i].substring(1));
							if(motionFuture.angleD != v) {
								motionFuture.angleD = v;
								armPanel.d1.setText(Float.toString(roundOff(v)));
							}
						} else if(items[i].startsWith("E")) {
							float v = (float)parseNumber(items[i].substring(1));
							if(motionFuture.angleE != v) {
								motionFuture.angleE = v;
								armPanel.e1.setText(Float.toString(roundOff(v)));
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

	public void moveBase(Vector3f dp) {
		motionFuture.anchorPosition.set(dp);
	}
	
	public void rotateBase(float pan,float tilt) {
		motionFuture.basePan=pan;
		motionFuture.baseTilt=tilt;
		
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
		BoundingVolume[] volumes = new BoundingVolume[6];
		for(int i=0;i<6;++i) volumes[i]=new BoundingVolume();
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
	 * Query the web server for a new robot UID.  
	 * @return the new UID if successful.  0 on failure.
	 * @see <a href='http://www.exampledepot.com/egs/java.net/Post.html'>http://www.exampledepot.com/egs/java.net/Post.html</a>
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
	public boolean movePermitted(PTTPTPRobotKeyframe keyframe) {
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
	
	protected boolean checkAngleLimits(PTTPTPRobotKeyframe keyframe) {/*
		// machine specific limits
		//a
		//if (angleA < -180) return false;
		//if (angleA >  180) return false;
		//b
		if (angleB <      72.90) angleB = 72.90f;
		if (angleB >  360-72.90) angleB = 360-72.90f;
		//c
		if (angleC <   50.57) angleC = 50.57f;
		if (angleC >  160.31) angleC = 160.31f;
		//d
		if (angleD <   87.85) angleD = 87.85f;
		if (angleD >  173.60) angleD = 173.60f;
		//e
		//if (angleE < 180-165) return false;
		//if (angleE > 180+165) return false;
*/
		return true;
	}
	
	/**
	 * Find the arm joint angles that would put the finger at the desired location.
	 * @return false if successful, true if the IK solution cannot be found.
	 */
	protected boolean inverseKinematics(PTTPTPRobotKeyframe keyframe) {
		double ee;
		float xx,yy;
		
		// rotation at finger, bend at wrist, rotation between wrist and elbow, then bends down to base.
		
		// find the wrist position
		keyframe.wrist = new Vector3f(keyframe.fingerPosition);
		keyframe.wrist.sub(segments[5].offsetFromParent);
		// find the shoulder position
		keyframe.base = new Vector3f(0,0,0);
		keyframe.shoulder = new Vector3f(segments[0].offsetFromParent);

		// Find the facingDirection and planeNormal vectors.
		Vector3f facingDirection = new Vector3f(keyframe.wrist.x,keyframe.wrist.y,0);
		if(Math.abs(keyframe.wrist.x)<EPSILON && Math.abs(keyframe.wrist.y)<EPSILON) {
			// Wrist is directly above shoulder, makes calculations hard.
			// TODO figure this out.  Use previous state to guess elbow?
			return false;
		}
		facingDirection.normalize();
		Vector3f up = new Vector3f(0,0,1);
		Vector3f planarRight = new Vector3f();
		planarRight.cross(facingDirection, up);
		planarRight.normalize();
		
		// Find elbow by using intersection of circles.
		// http://mathworld.wolfram.com/Circle-CircleIntersection.html
		// x = (dd-rr+RR) / (2d)
		Vector3f v0 = new Vector3f(keyframe.wrist);
		v0.sub(keyframe.shoulder);
		float d = v0.length();
		float R = (float)segments[2].offsetLength();  // bicep
		float r = (float)(segments[3].offsetLength());  // elbow + forearm
		if( d > R+r ) {
			// impossibly far away
			return false;
		}
		float x = (d*d - r*r + R*R ) / (2*d);
		if( x > R ) {
			// would cause Math.sqrt(a negative number)
			return false;
		}
		v0.normalize();
		keyframe.elbow.set(v0);
		keyframe.elbow.scale(x);
		keyframe.elbow.add(keyframe.shoulder);
		// v1 is now at the intersection point between ik_wrist and ik_boom
		Vector3f v1 = new Vector3f();
		float a = (float)( Math.sqrt( R*R - x*x ) );
		v1.cross(planarRight, v0);
		v1.scale(a);
		keyframe.elbow.add(v1);

		// angleF is the base
		// all the joint locations are now known.  find the angles.
		ee = Math.atan2(facingDirection.y, facingDirection.x);
		ee = MathHelper.capRotationRadians(ee);
		keyframe.angleF = (float)Math.toDegrees(ee);

		// angleE is the shoulder
		Vector3f towardsElbow = new Vector3f(keyframe.elbow);
		towardsElbow.sub(keyframe.shoulder);
		towardsElbow.normalize();
		xx = (float)towardsElbow.z;
		yy = facingDirection.dot(towardsElbow);
		ee = Math.atan2(yy, xx);
		ee = MathHelper.capRotationRadians(ee);
		keyframe.angleE = 90+(float)Math.toDegrees(ee);
		
		// angleD is the elbow
		Vector3f towardsWrist = new Vector3f(keyframe.wrist);
		towardsWrist.sub(keyframe.elbow);
		towardsWrist.normalize();
		xx = (float)towardsElbow.dot(towardsWrist);
		v1.cross(planarRight,towardsElbow);
		yy = towardsWrist.dot(v1);
		ee = Math.atan2(yy, xx);
		ee = MathHelper.capRotationRadians(ee);
		keyframe.angleD = 180+(float)Math.toDegrees(ee);
		
		// angleC is the ulna rotation
		v0.set(towardsWrist);
		v0.normalize();
		v1.cross(v0,planarRight);
		v1.normalize();
		Vector3f towardsFingerAdj = new Vector3f(keyframe.fingerForward);
		float tf = v0.dot(towardsFingerAdj);
		if(tf>=1-EPSILON) {
			// cannot calculate angle, leave as was
			return false;
		}
		// can calculate angle
		v0.scale(tf);
		towardsFingerAdj.sub(v0);
		towardsFingerAdj.normalize();
		xx = planarRight.dot(towardsFingerAdj);
		yy = v1.dot(towardsFingerAdj);
		ee = Math.atan2(yy, xx);
		ee = MathHelper.capRotationRadians(ee);
		keyframe.angleC = (float)Math.toDegrees(ee)-90;
		
		// angleB is the wrist bend
		v0.set(towardsWrist);
		v0.normalize();
		xx = v0.dot(keyframe.fingerForward);
		yy = towardsFingerAdj.dot(keyframe.fingerForward);
		ee = Math.atan2(yy, xx);
		ee = MathHelper.capRotationRadians(ee);
		keyframe.angleB = (float)(Math.toDegrees(ee));
		
		// angleA is the hand rotation
		v0.cross(towardsFingerAdj,towardsWrist);
		v0.normalize();
		v1.cross(v0, keyframe.fingerForward);
		v1.normalize();
		
		xx = v0.dot(keyframe.fingerRight);
		yy = v1.dot(keyframe.fingerRight);
		ee = Math.atan2(yy, xx);
		ee = MathHelper.capRotationRadians(ee);
		keyframe.angleA = (float)Math.toDegrees(ee);

		return true;
	}
	
	/**
	 * Calculate the finger location from the angles at each joint
	 * @param state
	 */
	protected void forwardKinematics(PTTPTPRobotKeyframe keyframe) {
		double f = Math.toRadians(keyframe.angleF);
		double e = Math.toRadians(keyframe.angleE);
		double d = Math.toRadians(keyframe.angleD);
		double c = Math.toRadians(keyframe.angleC);
		double b = Math.toRadians(keyframe.angleB);
		double a = Math.toRadians(keyframe.angleA);
		
		Vector3f facingDirection = new Vector3f((float)Math.cos(f),(float)Math.sin(f),0);
		Vector3f up = new Vector3f(0,0,1);
		Vector3f planarRight = new Vector3f();
		planarRight.cross(facingDirection, up);
		planarRight.normalize();

		keyframe.shoulder.set(segments[1].offsetFromParent);
		keyframe.shoulder.add(segments[0].offsetFromParent);
		keyframe.bicep.set(segments[2].offsetFromParent);
		keyframe.bicep.add(segments[1].offsetFromParent);
		keyframe.bicep.add(segments[0].offsetFromParent);
		
		// shoulder to elbow
		Vector3f toElbow = new Vector3f(facingDirection);
		toElbow.scale( -(float)Math.cos(-e) );
		Vector3f v2 = new Vector3f(up);
		v2.scale( -(float)Math.sin(-e) );
		toElbow.add(v2);
		float n = (float)segments[1].offsetLength();
		toElbow.scale(n);
		
		keyframe.elbow.set(toElbow);
		keyframe.elbow.add(keyframe.shoulder);
		
		// elbow to wrist
		Vector3f towardsElbowOrtho = new Vector3f();
		towardsElbowOrtho.cross(toElbow, planarRight);
		towardsElbowOrtho.normalize();

		Vector3f elbowToWrist = new Vector3f(toElbow);
		elbowToWrist.normalize();
		elbowToWrist.scale( (float)Math.cos(d) );
		v2.set(towardsElbowOrtho);
		v2.scale( (float)Math.sin(d) );
		elbowToWrist.add(v2);
		n = (float)segments[3].offsetLength();
		elbowToWrist.scale(n);
		
		keyframe.wrist.set(elbowToWrist);
		keyframe.wrist.add(keyframe.elbow);

		// wrist to finger
		Vector3f wristOrthoBeforeUlnaRotation = new Vector3f();
		wristOrthoBeforeUlnaRotation.cross(elbowToWrist, planarRight);
		wristOrthoBeforeUlnaRotation.normalize();
		Vector3f wristOrthoAfterRotation = new Vector3f(wristOrthoBeforeUlnaRotation);
		
		wristOrthoAfterRotation.scale( (float)Math.cos(-c) );
		v2.set(planarRight);
		v2.scale( (float)Math.sin(-c) );
		wristOrthoAfterRotation.add(v2);
		wristOrthoAfterRotation.normalize();

		Vector3f towardsFinger = new Vector3f();

		towardsFinger.set(elbowToWrist);
		towardsFinger.normalize();
		towardsFinger.scale( (float)( Math.cos(-b) ) );
		v2.set(wristOrthoAfterRotation);
		v2.scale( (float)( Math.sin(-b) ) );
		towardsFinger.add(v2);
		towardsFinger.normalize();

		keyframe.fingerPosition.set(towardsFinger);
		n = (float)segments[6].offsetLength();
		keyframe.fingerPosition.scale(n);
		keyframe.fingerPosition.add(keyframe.wrist);

		// finger rotation
		Vector3f v0 = new Vector3f();
		Vector3f v1 = new Vector3f();
		v0.cross(towardsFinger,wristOrthoAfterRotation);
		v0.normalize();
		v1.cross(v0,towardsFinger);
		v1.normalize();
		
		keyframe.fingerRight.set(v0);
		keyframe.fingerRight.scale((float)Math.cos(a));
		v2.set(v1);
		v2.scale((float)Math.sin(a));
		keyframe.fingerRight.add(v2);

		keyframe.fingerForward.set(towardsFinger);
		keyframe.fingerForward.normalize();
	}
}
