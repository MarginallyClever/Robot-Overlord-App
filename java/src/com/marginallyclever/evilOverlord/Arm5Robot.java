package com.marginallyclever.evilOverlord;

import javax.vecmath.Vector3f;
import javax.media.opengl.GL2;

import java.awt.event.KeyEvent;


public class Arm5Robot 
extends RobotWithSerialConnection {
	//machine dimensions
	
	// out of date
	public final static float BASE_TO_SHOULDER_X   =(5.37f);  // measured in solidworks
	public final static float BASE_TO_SHOULDER_Z   =(9.55f);  // measured in solidworks
	public final static float SHOULDER_TO_ELBOW    =(25.0f);
	public final static float ELBOW_TO_WRIST       =(25.0f);
	public final static float WRIST_TO_FINGER      =(4.0f);
	public final static float BASE_TO_SHOULDER_MINIMUM_LIMIT = 7.5f;
	
	// new model
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
	private Model anchor = new Model();
	private Model shoulder = new Model();
	private Model shoulder_pinion = new Model();
	private Model boom = new Model();
	private Model stick = new Model();
	private Model wristBone = new Model();
	private Model wristEnd = new Model();
	private Model wristInterior = new Model();
	private Model wristPinion = new Model();
	
	// collision volumes
	Cylinder [] volumes = new Cylinder[6];

	// motion states
	protected Arm5MotionState motionNow = new Arm5MotionState();
	protected Arm5MotionState motionFuture = new Arm5MotionState();
	
	// keyboard history
	float aDir = 0.0f;
	float bDir = 0.0f;
	float cDir = 0.0f;
	float dDir = 0.0f;
	float eDir = 0.0f;

	float xDir = 0.0f;
	float yDir = 0.0f;
	float zDir = 0.0f;

	// machine logic states
	
	boolean homed = false;
	boolean homing = false;
	boolean follow_mode = false;
	boolean armMoved = false;
	
	boolean pWasOn=false;
	boolean moveMode=false;
	
	boolean isLoaded=false;
	boolean isRenderFKOn=true;
		
	
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
		//motionNow.inverseKinematics();
		//motionFuture.inverseKinematics();
		motionNow.forwardKinematics();
		motionFuture.forwardKinematics();
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

	
	
	/**
	 * update the desired finger location
	 * @param delta
	 */
	protected void updateFingerForInverseKinematics(float delta) {
		boolean changed=false;
		motionFuture.fingerPosition.set(motionNow.fingerPosition);
		final float vel=5.0f;
		float dp = vel * delta;

		if (xDir!=0) {
			motionFuture.fingerPosition.x += xDir * dp;
			changed=true;
			xDir=0;
		}		
		if (yDir!=0) {
			motionFuture.fingerPosition.y += yDir * dp;
			changed=true;
			yDir=0;
		}
		if (zDir!=0) {
			motionFuture.fingerPosition.z += zDir * dp;
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
		
		if(changed==true && motionFuture.movePermitted()) {
			if(motionNow.fingerPosition.epsilonEquals(motionFuture.fingerPosition,0.1f)) {
				armMoved=true;
			}
		} else {
			motionFuture.fingerPosition.set(motionNow.fingerPosition);
		}
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
				
				motionFuture.forwardKinematics();
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
		motionNow.angleD=90;
		motionNow.angleC=50;
		motionNow.angleB=60;
		motionNow.angleA=230;
		motionNow.forwardKinematics();
		
		gl2.glPushMatrix();
		renderFKModels(gl2);
		gl2.glPopMatrix();
		if(isRenderFKOn) {
			gl2.glPushMatrix();
			gl2.glDisable(GL2.GL_DEPTH_TEST);
			renderFK(gl2);
			gl2.glEnable(GL2.GL_DEPTH_TEST);
		}
		gl2.glPopMatrix();
	}
	
	
	/**
	 * Draw the arm without calling glRotate to prove forward kinematics are correct.
	 * @param gl2
	 */
	protected void renderFK(GL2 gl2) {
		boolean lightOn= gl2.glIsEnabled(GL2.GL_LIGHTING);
		boolean matCoOn= gl2.glIsEnabled(GL2.GL_COLOR_MATERIAL);
		gl2.glDisable(GL2.GL_LIGHTING);
		
		setColor(gl2,1,1,1,1);
		gl2.glBegin(GL2.GL_LINE_STRIP);
		
		gl2.glVertex3d(0,0,0);
		gl2.glVertex3d(motionNow.shoulder.x,motionNow.shoulder.y,motionNow.shoulder.z);
		gl2.glVertex3d(motionNow.boom.x,motionNow.boom.y,motionNow.boom.z);
		gl2.glVertex3d(motionNow.elbow.x,motionNow.elbow.y,motionNow.elbow.z);
		gl2.glVertex3d(motionNow.wrist.x,motionNow.wrist.y,motionNow.wrist.z);
		gl2.glVertex3d(motionNow.fingerPosition.x,motionNow.fingerPosition.y,motionNow.fingerPosition.z);
		gl2.glVertex3d(motionNow.fingerForward.x,motionNow.fingerForward.y,motionNow.fingerForward.z);

		gl2.glEnd();


		// finger tip
		setColor(gl2,1,0.8f,0,1);
		PrimitiveSolids.drawStar(gl2, motionNow.fingerPosition );
		PrimitiveSolids.drawStar(gl2, motionNow.fingerForward );
	
		if(lightOn) gl2.glEnable(GL2.GL_LIGHTING);
		if(matCoOn) gl2.glEnable(GL2.GL_COLOR_MATERIAL);
	}
	
	
	/**
	 * Draw the physical model according to the angle values in the motionNow state.
	 * @param gl2
	 */
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
		//gl2.glRotated(155-motionNow.angleE,0,1,0);
		gl2.glRotated(motionNow.angleE,0,1,0);
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
		gl2.glRotated(90-motionNow.angleD,0,0,1);
		gl2.glPushMatrix();
		gl2.glScaled(-1,1,1);
		boom.render(gl2);
		gl2.glPopMatrix();

		// stick (C)
		setColor(gl2,1,0,1,1);
		gl2.glTranslated(0.0, BOOM_TO_STICK_Y, 0);
		gl2.glRotated(90+motionNow.angleC,0,0,1);
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
			//gl2.glRotated(motionNow.angleB+motionNow.angleA-78,1,0,0);  // Why is this -78 here?
			gl2.glRotated(motionNow.angleB+motionNow.angleA,1,0,0);
			setColor(gl2,0,1,0,1);
			gl2.glPushMatrix();
			wristEnd.render(gl2);
			gl2.glPopMatrix();
			
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
		gl2.glTranslatef(motionNow.anchorPosition.x, motionNow.anchorPosition.y, motionNow.anchorPosition.z);
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

			motionFuture.fingerPosition.set(0,0,0);  // HOME_* should match values in robot firmware.
			motionFuture.inverseKinematics();
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
		motionFuture.anchorPosition.set(dp);
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
		volumes[5].SetP2(GetWorldCoordinatesFor(motionFuture.fingerPosition));
		
		return volumes;
	}
	
	
	Vector3f GetWorldCoordinatesFor(Vector3f in) {
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
}
