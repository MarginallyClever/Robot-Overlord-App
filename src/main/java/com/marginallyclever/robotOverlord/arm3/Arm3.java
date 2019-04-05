package com.marginallyclever.robotOverlord.arm3;

import javax.vecmath.Vector3d;

import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.BoundingVolume;
import com.marginallyclever.robotOverlord.Cylinder;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.robot.Robot;
import com.marginallyclever.robotOverlord.robot.RobotKeyframe;
import com.jogamp.opengl.GL2;
import javax.swing.JPanel;

import java.util.ArrayList;


public class Arm3 extends Robot {
	/**
	 * serial version id
	 */
	private static final long serialVersionUID = -5065086783049069206L;

	// Collision volumes
	protected Cylinder [] volumes = new Cylinder[6];
	
	// motion now and in the future for looking-ahead
	protected Arm3Keyframe motionNow;
	protected Arm3Keyframe motionFuture;
	protected Arm3Dimensions armSettings;
	
	// keyboard history
	protected float aDir = 0.0f;
	protected float bDir = 0.0f;
	protected float cDir = 0.0f;

	protected float xDir = 0.0f;
	protected float yDir = 0.0f;
	protected float zDir = 0.0f;
	
	protected double speed=2;
	boolean followMode = false;
	boolean armHasMoved = false;
	
	protected boolean isPortConfirmed=false;
	
	protected Arm3ControlPanel arm3Panel=null;
	protected boolean draw_simple=false;
	
	
	public Arm3() {
		super();
		setupDimensions(new Arm3Dimensions());
	}
	
	public Arm3(Arm3Dimensions arg0) {
		super();
		setupDimensions(arg0);
	}
	
	protected void setupDimensions(Arm3Dimensions arg0) {
		armSettings = arg0;

		motionNow = new Arm3Keyframe(arg0);
		motionFuture = new Arm3Keyframe(arg0);
		
		setDisplayName(armSettings.getName());
		
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
		
		RotateBase(0,0);
		IK(motionNow);
		IK(motionFuture);
	}
	

	public Vector3d getHome() {
		return armSettings.getHomePosition();
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

	
	protected void updateIK(double delta) {
		boolean changed=false;
		motionFuture.fingerPosition.set(motionNow.fingerPosition);
		float dp = (float)speed;// * delta;
		
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

		if(changed) {
			if(movePermitted(motionFuture)) {
				if(!motionNow.fingerPosition.epsilonEquals(motionFuture.fingerPosition,0.1f)) {
					armHasMoved=true;
					updateGUI();
				}
			} else {
				motionFuture.set(motionNow);
			}
		}
	}
	
	
	protected void updateFK(double delta) {
		boolean changed=false;

		if (cDir!=0) {
			motionFuture.angleElbow += speed * cDir;
			changed=true;
			cDir=0;
		}
		
		if(bDir!=0) {
			motionFuture.angleShoulder += speed * bDir;
			changed=true;
			bDir=0;
		}
		
		if(aDir!=0) {
			motionFuture.angleBase += speed * aDir;
			changed=true;
			aDir=0;
		}

		if(changed) {
			if(CheckAngleLimits(motionFuture)) {
				FK(motionFuture);
				armHasMoved=true;
				updateGUI();
			} else {
				motionFuture.set(motionNow);
			}
		}
	}

	@Override
	public void prepareMove(double delta) {
		updateIK(delta);
		updateFK(delta);
	}
	

	@Override
	public void finalizeMove() {
		// copy motionFuture to motionNow
		motionNow.set(motionFuture);
		
		if( isReadyToReceive && armHasMoved ) {
			String command = armSettings.reportMove(motionFuture);
			if(command.trim().length()>0) {
				sendLineToRobot(command);
			}

			armHasMoved=false;
		}
	}

	
	@Override
	public ArrayList<JPanel> getContextPanel(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getContextPanel(gui);
		
		if(list==null) list = new ArrayList<JPanel>();
		
		arm3Panel = createArm3ControlPanel(gui);
		list.add(arm3Panel);
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
	
	
	protected Arm3ControlPanel createArm3ControlPanel(RobotOverlord gui) {
		return new Arm3ControlPanel(gui,this);
	}
	
	public void updateGUI() {
		Vector3d v = new Vector3d();
		v.set(motionNow.fingerPosition);
		// TODO rotate fingerPosition before adding position
		v.add(getPosition());
		arm3Panel.xPos.setText(Double.toString(MathHelper.roundOff3(v.x)));
		arm3Panel.yPos.setText(Double.toString(MathHelper.roundOff3(v.y)));
		arm3Panel.zPos.setText(Double.toString(MathHelper.roundOff3(v.z)));

		arm3Panel.labelFK1.setText(Double.toString(MathHelper.roundOff3(motionNow.angleBase)));
		arm3Panel.labelFK2.setText(Double.toString(MathHelper.roundOff3(motionNow.angleShoulder)));
		arm3Panel.labelFK3.setText(Double.toString(MathHelper.roundOff3(motionNow.angleElbow)));
		
		arm3Panel.labelIK1.setText(Double.toString(MathHelper.roundOff3(motionNow.angleBase)));
		arm3Panel.labelIK2.setText(Double.toString(MathHelper.roundOff3(motionNow.angleShoulder)));
		arm3Panel.labelIK3.setText(Double.toString(MathHelper.roundOff3(motionNow.angleElbow)));

		//if( tool != null ) tool.updateGUI();
	}
	
	
	public void render(GL2 gl2) {
		super.render(gl2);
		
		if(this.getClass() == Arm3.class) {
			gl2.glPushMatrix();
			Vector3d p = this.getPosition();
			gl2.glTranslated(p.x,p.y,p.z);
	
	//		gl2.glTranslated(motionNow.base.x, motionNow.base.y, motionNow.base.z);
	//		gl2.glRotated(motionNow.base_pan, motionNow.base_up.x,motionNow.base_up.y,motionNow.base_up.z);
	 		// for debugging difference between FK and IK
	//		gl2.glDisable(GL2.GL_LIGHTING);
	//		gl2.glColor3d(1,1,1);
	//		PrimitiveSolids.drawStar(gl2,motionNow.finger_tip,5);
	//		PrimitiveSolids.drawStar(gl2,motionNow.elbow,10);
	//		PrimitiveSolids.drawStar(gl2,motionNow.shoulder,15);
	//		gl2.glEnable(GL2.GL_LIGHTING);
	
			//drawBounds(gl2);
			
			// these two should always match!
			drawFK(gl2);
			//drawIK(gl2);
			
			gl2.glPopMatrix();
		}
	}
	
	
	protected void drawIK(GL2 gl2) {
		gl2.glPushMatrix();
		gl2.glTranslated(motionNow.base.x, motionNow.base.y, motionNow.base.z);
		gl2.glRotated(motionNow.base_pan, motionNow.base_up.x,motionNow.base_up.y,motionNow.base_up.z);
		
		gl2.glDisable(GL2.GL_DEPTH_TEST);
		gl2.glDisable(GL2.GL_LIGHTING);
		
	    Vector3d base_u = new Vector3d(1,0,0);
	    Vector3d base_v = new Vector3d(0,1,0);
	    Vector3d base_w = new Vector3d(0,0,1);
	    MatrixHelper.drawMatrix(gl2,motionNow.base,base_u,base_v,base_w);

	    Vector3d arm_plane = new Vector3d(motionNow.wrist.x,motionNow.wrist.y,0);
	    arm_plane.normalize();
		Vector3d arm_plane_normal = new Vector3d();
		Vector3d arm_up = new Vector3d(0,0,1);
		arm_plane_normal.cross(arm_plane,arm_up);

		Vector3d shoulder_v = new Vector3d(arm_plane_normal);
		shoulder_v.scale(-1);
		Vector3d shoulder_w = new Vector3d(motionNow.elbow);
		shoulder_w.sub(motionNow.shoulder);
		shoulder_w.normalize();
		Vector3d shoulder_u = new Vector3d();
		shoulder_u.cross(shoulder_v,shoulder_w);
	    MatrixHelper.drawMatrix(gl2,motionNow.shoulder,shoulder_u,shoulder_v,shoulder_w);
		
		Vector3d elbow_v = new Vector3d(arm_plane_normal);
		elbow_v.scale(-1);
		Vector3d elbow_w = new Vector3d(motionNow.wrist);
		elbow_w.sub(motionNow.elbow);
		elbow_w.normalize();
		Vector3d elbow_u = new Vector3d();
		elbow_u.cross(elbow_v,elbow_w);
	    MatrixHelper.drawMatrix(gl2,motionNow.elbow,elbow_u,elbow_v,elbow_w);

	    
		Vector3d ulna_w = new Vector3d(motionNow.wrist);
		ulna_w.sub(motionNow.elbow);
		ulna_w.normalize();
		Vector3d ulna_p = new Vector3d(motionNow.wrist);
		ulna_p.add(motionNow.elbow);
		ulna_p.scale(0.5f);

		Vector3d ulna_v = new Vector3d(arm_plane_normal);
		ulna_v.scale(-1);
		Vector3d ulna_u = new Vector3d();
		ulna_u.cross(ulna_v,ulna_w);
	    MatrixHelper.drawMatrix(gl2,ulna_p,ulna_u,ulna_v,ulna_w);

	    Vector3d wrist_u = new Vector3d(ulna_u);
	    Vector3d wrist_v = new Vector3d(ulna_v);
	    Vector3d wrist_w = new Vector3d(ulna_w);
	    MatrixHelper.drawMatrix(gl2,motionNow.wrist,wrist_u,wrist_v,wrist_w);

	    Vector3d finger_up = new Vector3d();
	    finger_up.cross(motionNow.fingerForward,motionNow.fingerRight);
	    MatrixHelper.drawMatrix(gl2,motionNow.fingerPosition,motionNow.fingerForward,motionNow.fingerRight,finger_up);

		gl2.glDisable(GL2.GL_DEPTH_TEST);
		gl2.glBegin(GL2.GL_LINES);

		gl2.glColor3f(1,1,1);
		gl2.glVertex3d(0,0,0);
		gl2.glVertex3d(motionNow.shoulder.x,motionNow.shoulder.y,motionNow.shoulder.z);

		gl2.glVertex3d(motionNow.shoulder.x,motionNow.shoulder.y,motionNow.shoulder.z);
		gl2.glVertex3d(motionNow.elbow.x,motionNow.elbow.y,motionNow.elbow.z);

		gl2.glVertex3d(motionNow.elbow.x,motionNow.elbow.y,motionNow.elbow.z);
		gl2.glVertex3d(motionNow.wrist.x,motionNow.wrist.y,motionNow.wrist.z);

		gl2.glVertex3d(motionNow.wrist.x,motionNow.wrist.y,motionNow.wrist.z);
		gl2.glVertex3d(motionNow.fingerPosition.x,motionNow.fingerPosition.y,motionNow.fingerPosition.z);

		// DEBUG START
		Vector3d ulna_forward = new Vector3d(motionNow.elbow);
		ulna_forward.sub(motionNow.wrist);
		ulna_forward.normalize();
		Vector3d ffn = new Vector3d(ulna_forward);

		Vector3d right_unrotated;
		Vector3d forward = armSettings.getHomeForward();
		Vector3d right = armSettings.getHomeRight();
		Vector3d up = new Vector3d();
		
		up.cross(forward,right);
		
		//Vector3d of = new Vector3d(forward);
		Vector3d or = new Vector3d(right);
		Vector3d ou = new Vector3d(up);
		
		//result = RotateAroundAxis(right,of,motionNow.iku);
		right_unrotated = MathHelper.rotateAroundAxis(right,or,motionNow.ikv);
		right_unrotated = MathHelper.rotateAroundAxis(right_unrotated,ou,motionNow.ikw);

		Vector3d ulna_normal = new Vector3d();
		ulna_normal.cross(motionNow.fingerForward,right_unrotated);
		ulna_normal.normalize();
		
		Vector3d ulna_up = new Vector3d();
		ulna_up.cross(ulna_forward,ulna_normal);

		ffn.normalize();
		double df= motionNow.fingerForward.dot(ffn);
		if(Math.abs(df)<0.999999) {
			Vector3d ulna_up_unrotated = new Vector3d();
			ulna_up_unrotated.cross(ulna_forward,arm_plane_normal);
			
			Vector3d finger_on_ulna = new Vector3d(motionNow.fingerForward);
			Vector3d temp = new Vector3d(ffn);
			temp.scale(df);
			finger_on_ulna.sub(temp);
			finger_on_ulna.normalize();
			
			gl2.glColor3f(0,1,1);
			gl2.glVertex3d(motionNow.wrist.x,motionNow.wrist.y,motionNow.wrist.z);
			gl2.glVertex3d(motionNow.wrist.x+ulna_up.x*3,
							motionNow.wrist.y+ulna_up.y*3,
							motionNow.wrist.z+ulna_up.z*3);
			gl2.glColor3f(1,0,1);
			gl2.glVertex3d(motionNow.wrist.x,motionNow.wrist.y,motionNow.wrist.z);
			gl2.glVertex3d(motionNow.wrist.x+ulna_forward.x*-3,
							motionNow.wrist.y+ulna_forward.y*-3,
							motionNow.wrist.z+ulna_forward.z*-3);
			gl2.glColor3f(1,1,1);
			gl2.glVertex3d(motionNow.wrist.x,motionNow.wrist.y,motionNow.wrist.z);
			gl2.glVertex3d(motionNow.wrist.x+finger_on_ulna.x,
							motionNow.wrist.y+finger_on_ulna.y,
							motionNow.wrist.z+finger_on_ulna.z);

		}
		// DEBUG END
		
		gl2.glEnd();


		gl2.glEnable(GL2.GL_DEPTH_TEST);
		gl2.glEnable(GL2.GL_LIGHTING);
		
		gl2.glPopMatrix();
	}


	protected void drawFK(GL2 gl2) {
		Vector3d a0 = new Vector3d(armSettings.getBaseToShoulderX(),0,armSettings.getBaseToShoulderZ());
		Vector3d a1 = new Vector3d(0,0,armSettings.getShoulderToElbow());
		Vector3d a2 = new Vector3d(0,0,armSettings.getElbowToWrist());
		Vector3d a3 = new Vector3d(0,0,armSettings.getWristToFinger());

		// base 
		gl2.glPushMatrix();
		gl2.glTranslated(motionNow.base.x, motionNow.base.y, motionNow.base.z);
		gl2.glRotated(motionNow.base_pan, motionNow.base_up.x,motionNow.base_up.y,motionNow.base_up.z);
		
		gl2.glColor3f(1,1,1);
		gl2.glRotated(motionNow.angleBase,0,0,1);
		gl2.glColor3f(0,0,1);
		PrimitiveSolids.drawBox(gl2,4,armSettings.getBaseToShoulderX()*2,armSettings.getBaseToShoulderZ());

		// shoulder
		gl2.glTranslated(a0.x,a0.y,a0.z);
		gl2.glRotated(90+motionNow.angleShoulder,0,1,0);
		gl2.glColor3f(0,1,0);
		PrimitiveSolids.drawCylinder(gl2,3.2f,3.2f);
		
		// bicep
		gl2.glColor3f(0,0,1);
		//PrimitiveSolids.drawBox(gl2,3,3,SHOULDER_TO_ELBOW);
		gl2.glPushMatrix();
		gl2.glTranslated(a1.x/2,a1.y/2,a1.z/2);
		gl2.glRotated(90,1,0,0);
		PrimitiveSolids.drawCylinder(gl2, armSettings.getShoulderToElbow()/2.0f, 3.0f*0.575f);
		gl2.glPopMatrix();

		// elbow
		gl2.glTranslated(a1.x,a1.y,a1.z);
		gl2.glRotated(180-motionNow.angleElbow-motionNow.angleShoulder,0,1,0);
		gl2.glColor3f(0,1,0);
		PrimitiveSolids.drawCylinder(gl2,2.2f,2.2f);
		gl2.glColor3f(0,0,1);
		//PrimitiveSolids.drawBox(gl2,2,2,ELBOW_TO_WRIST);

		// ulna
		gl2.glPushMatrix();
		gl2.glTranslated(a2.x/2,a2.y/2,a2.z/2);
		gl2.glRotated(90,1,0,0);
		PrimitiveSolids.drawCylinder(gl2, armSettings.getElbowToWrist()/2.0f, 1.15f);
		gl2.glPopMatrix();

		// wrist
		gl2.glTranslated(a2.x,a2.y,a2.z);
		gl2.glRotated(-180+motionNow.angleElbow,0,1,0);
		gl2.glColor3f(0,1,0);
		PrimitiveSolids.drawCylinder(gl2,1.2f,1.2f);
		gl2.glColor3f(0,0,1);
		//PrimitiveSolids.drawBox(gl2,1,1,WRIST_TO_FINGER);

		// finger tip
		gl2.glPushMatrix();
		gl2.glTranslated(a3.x/2,a3.y/3,a3.z/2);
		gl2.glRotated(90,1,0,0);
		PrimitiveSolids.drawCylinder(gl2, armSettings.getWristToFinger()/2.0f, 1.0f*0.575f);
		gl2.glPopMatrix();

		gl2.glTranslated(a3.x,a3.y,a3.z);	
		//gl2.glRotated(-motionNow.angle_3,a2.x,a2.y,a2.z);
		//gl2.glRotated(motionNow.angle_4,0,1,0);
		//gl2.glRotated(-180+motionNow.angle_2,0,1,0);
		gl2.glRotated(-90,0,1,0);
		// draw finger tip orientation
		gl2.glDisable(GL2.GL_LIGHTING);
		gl2.glBegin(GL2.GL_LINES);
		gl2.glColor3f(1,0,1);  // magenta
		gl2.glVertex3d(0,0,0);
		Vector3d hf=armSettings.getHomeForward();
		Vector3d hr=armSettings.getHomeRight();
		gl2.glVertex3d(hr.x,hr.y,hr.z);
		gl2.glColor3f(0,1,0);  // green
		gl2.glVertex3d(0,0,0);
		gl2.glVertex3d(hf.x,hf.y,hf.z);
		gl2.glEnd();
		gl2.glEnable(GL2.GL_LIGHTING);
		// TODO draw tool here
		gl2.glPopMatrix();

		if(draw_simple) {
			gl2.glDisable(GL2.GL_DEPTH_TEST);
			gl2.glDisable(GL2.GL_LIGHTING);

			// base 
			gl2.glPushMatrix();
			gl2.glTranslated(motionNow.base.x, motionNow.base.y, motionNow.base.z);
			gl2.glRotated(motionNow.base_pan, motionNow.base_up.x,motionNow.base_up.y,motionNow.base_up.z);
			
			gl2.glColor3f(1,1,1);
			gl2.glRotated(motionNow.angleBase,0,0,1);

			gl2.glBegin(GL2.GL_LINES);
			gl2.glVertex3d(0,0,0);
			gl2.glVertex3d(a0.x,a0.y,a0.z);
			gl2.glEnd();

			// shoulder
			gl2.glTranslated(a0.x,a0.y,a0.z);
			gl2.glRotated(90+motionNow.angleShoulder,0,1,0);
			
			// bicep
			gl2.glColor3f(0,0,1);
			gl2.glBegin(GL2.GL_LINES);
			gl2.glVertex3d(0,0,0);
			gl2.glVertex3d(a1.x,a1.y,a1.z);
			gl2.glEnd();
	
			// elbow
			gl2.glTranslated(a1.x,a1.y,a1.z);
			gl2.glRotated(180-motionNow.angleElbow-motionNow.angleShoulder,0,1,0);
	
			// ulna
			gl2.glBegin(GL2.GL_LINES);
			gl2.glVertex3d(0,0,0);
			gl2.glVertex3d(a2.x,a2.y,a2.z);
			gl2.glEnd();
	
			// wrist
			gl2.glTranslated(a2.x,a2.y,a2.z);
			gl2.glRotated(-180+motionNow.angleElbow,0,1,0);
			
			// finger tip
			gl2.glBegin(GL2.GL_LINES);
			gl2.glVertex3d(0,0,0);
			gl2.glVertex3d(a3.x,a3.y,a3.z);
			gl2.glEnd();
			
			gl2.glTranslated(a3.x,a3.y,a3.z);	

			// draw finger tip orientation			
			gl2.glRotated(-90,0,1,0);
			gl2.glBegin(GL2.GL_LINES);
			gl2.glColor3f(1,0,1);  // magenta
			gl2.glVertex3d(0,0,0);
			gl2.glVertex3d(hr.x*5,hr.y*5,hr.z*5);
			gl2.glColor3f(0,1,0);  // green
			gl2.glVertex3d(0,0,0);
			gl2.glVertex3d(hf.x,hf.y,hf.z);
			gl2.glEnd();
			
			// TODO draw tool here
			
			gl2.glPopMatrix();

			gl2.glEnable(GL2.GL_LIGHTING);
			gl2.glEnable(GL2.GL_DEPTH_TEST);
		}
	}

	
	protected void drawBounds(GL2 gl2) {
		// base
		
		gl2.glPushMatrix();
		gl2.glTranslated(motionNow.base.x, motionNow.base.y, motionNow.base.z);
		gl2.glRotated(motionNow.angleBase,0,0,1);
		gl2.glColor3f(0,0,1);
		PrimitiveSolids.drawBox(gl2,4,armSettings.getBaseToShoulderX()*2,armSettings.getBaseToShoulderZ());
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
	
	
	public boolean isPortConfirmed() {
		return isPortConfirmed;
	}
	

	public void MoveBase(Vector3d dp) {
		motionFuture.base.set(dp);
	}
	
	
	public void RotateBase(float pan,float tilt) {
		motionFuture.base_pan=pan;
		motionFuture.base_tilt=tilt;
		
		motionFuture.base_forward.y = Math.sin(pan * Math.PI/180.0) * Math.cos(tilt * Math.PI/180.0);
		motionFuture.base_forward.x = Math.cos(pan * Math.PI/180.0) * Math.cos(tilt * Math.PI/180.0);
		motionFuture.base_forward.z =                                        Math.sin(tilt * Math.PI/180.0);
		motionFuture.base_forward.normalize();
		
		motionFuture.base_up.set(0,0,1);
	
		motionFuture.base_right.cross(motionFuture.base_forward, motionFuture.base_up);
		motionFuture.base_right.normalize();
		motionFuture.base_up.cross(motionFuture.base_right, motionFuture.base_forward);
		motionFuture.base_up.normalize();
	}
	
	
	public BoundingVolume [] GetBoundingVolumes() {
		// shoulder joint
		Vector3d t1=new Vector3d(motionFuture.base_right);
		t1.scale(volumes[0].getRadius()/2);
		t1.add(motionFuture.shoulder);
		Vector3d t2=new Vector3d(motionFuture.base_right);
		t2.scale(-volumes[0].getRadius()/2);
		t2.add(motionFuture.shoulder);
		volumes[0].SetP1(GetWorldCoordinatesFor(t1));
		volumes[0].SetP2(GetWorldCoordinatesFor(t2));
		// bicep
		volumes[1].SetP1(GetWorldCoordinatesFor(motionFuture.shoulder));
		volumes[1].SetP2(GetWorldCoordinatesFor(motionFuture.elbow));
		// elbow
		t1.set(motionFuture.base_right);
		t1.scale(volumes[0].getRadius()/2);
		t1.add(motionFuture.elbow);
		t2.set(motionFuture.base_right);
		t2.scale(-volumes[0].getRadius()/2);
		t2.add(motionFuture.elbow);
		volumes[2].SetP1(GetWorldCoordinatesFor(t1));
		volumes[2].SetP2(GetWorldCoordinatesFor(t2));
		// ulna
		volumes[3].SetP1(GetWorldCoordinatesFor(motionFuture.elbow));
		volumes[3].SetP2(GetWorldCoordinatesFor(motionFuture.wrist));
		// wrist
		t1.set(motionFuture.base_right);
		t1.scale(volumes[0].getRadius()/2);
		t1.add(motionFuture.wrist);
		t2.set(motionFuture.base_right);
		t2.scale(-volumes[0].getRadius()/2);
		t2.add(motionFuture.wrist);
		volumes[4].SetP1(GetWorldCoordinatesFor(t1));
		volumes[4].SetP2(GetWorldCoordinatesFor(t2));
		// finger
		volumes[5].SetP1(GetWorldCoordinatesFor(motionFuture.wrist));
		volumes[5].SetP2(GetWorldCoordinatesFor(motionFuture.fingerPosition));
		
		return volumes;
	}
	
	
	Vector3d GetWorldCoordinatesFor(Vector3d in) {
		Vector3d out = new Vector3d(motionFuture.base);
		
		Vector3d tempx = new Vector3d(motionFuture.base_forward);
		tempx.scale(in.x);
		out.add(tempx);

		Vector3d tempy = new Vector3d(motionFuture.base_right);
		tempy.scale(-in.y);
		out.add(tempy);

		Vector3d tempz = new Vector3d(motionFuture.base_up);
		tempz.scale(in.z);
		out.add(tempz);
				
		return out;
	}

	
	@Override
	public void dataAvailable(NetworkConnection arg0,String data) {
		super.dataAvailable(arg0, data);
		
		if(data.contains(armSettings.getHello())) {
			isPortConfirmed=true;
			
			// we are not homed and we have not begun to home
			if(armSettings.getHomeAutomaticallyOnStartup()) {
				// this should be sent by a human when they are ready
				sendLineToRobot("G28");
				motionFuture.fingerPosition.set(armSettings.getHomePosition());  // HOME_* should match values in robot firmware.
				IK(motionFuture);
				sendLineToRobot("G92 X"+motionFuture.fingerPosition.x
								+" Y"+motionFuture.fingerPosition.y
								+" Z"+motionFuture.fingerPosition.z);
				followMode=true;
			}
		}
	}
	
	public double getSpeed() {
		return speed;
	}
	
	public void setSpeed(double s) {
		speed=s;
	}
	
	public void doAbout() {
		armSettings.doAbout();
	}
	
	
	//TODO check for collisions with http://geomalgorithms.com/a07-_distance.html#dist3D_Segment_to_Segment ?
	public boolean movePermitted(Arm3Keyframe keyframe) {
		// don't hit floor
		if(keyframe.fingerPosition.z<0.25f) {
			return false;
		}
		// don't hit ceiling
		if(keyframe.fingerPosition.z>50.0f) {
			return false;
		}

		// check far limit
		Vector3d temp = new Vector3d(keyframe.fingerPosition);
		temp.sub(keyframe.shoulder);
		if(temp.length() > 50) return false;
		// check near limit
		if(temp.length() < keyframe.dimensions.getBaseToShoulderMinimumLimit()) return false;

		// seems doable
		if(!IK(keyframe)) return false;
		// angle are good?
		if(!CheckAngleLimits(keyframe)) return false;

		// OK
		return true;
	}
	
	
	protected boolean CheckAngleLimits(Arm3Keyframe keyframe) {/*
		// machine specific limits
		if (keyframe.angle_0 < -180) return false;
		if (keyframe.angle_0 >  180) return false;
		if (keyframe.angle_2 <  -20) return false;
		if (keyframe.angle_2 >  180) return false;
		if (keyframe.angle_1 < -150) return false;
		if (keyframe.angle_1 >   80) return false;
		if (keyframe.angle_1 < -keyframe.angle_2+ 10) return false;
		if (keyframe.angle_1 > -keyframe.angle_2+170) return false;

		if (keyframe.angle_3 < -180) return false;
		if (keyframe.angle_3 >  180) return false;
		if (keyframe.angle_4 < -180) return false;
		if (keyframe.angle_4 >  180) return false;
		if (keyframe.angle_5 < -180) return false;
		if (keyframe.angle_5 >  180) return false;*/
		
		return true;
	}
	
	
	/**
	 * Convert cartesian XYZ to robot motor steps.
	 * @return true if successful, false if the IK solution cannot be found.
	 */
	protected boolean IK(Arm3Keyframe keyframe) {
		float a0,a1,a2;
		// if we know the position of the wrist relative to the shoulder
		// we can use intersection of circles to find the elbow.
		// once we know the elbow position we can find the angle of each joint.
		// each angle can be converted to motor steps.

	    // the finger (attachment point for the tool) is a short distance in "front" of the wrist joint
	    Vector3d finger = new Vector3d(keyframe.fingerPosition);
		keyframe.wrist.set(keyframe.fingerForward);
		keyframe.wrist.scale(-keyframe.dimensions.getWristToFinger());
		keyframe.wrist.add(finger);
				
	    // use intersection of circles to find two possible elbow points.
	    // the two circles are the bicep (shoulder-elbow) and the ulna (elbow-wrist)
	    // the distance between circle centers is d  
	    Vector3d arm_plane = new Vector3d(keyframe.wrist.x,keyframe.wrist.y,0);
	    arm_plane.normalize();
	
	    keyframe.shoulder.set(arm_plane);
	    keyframe.shoulder.scale(keyframe.dimensions.getBaseToShoulderX());
	    keyframe.shoulder.z = keyframe.dimensions.getBaseToShoulderZ();
	    
	    // use intersection of circles to find elbow
	    Vector3d es = new Vector3d(keyframe.wrist);
	    es.sub(keyframe.shoulder);
	    double d = es.length();
	    double r1=keyframe.dimensions.getElbowToWrist();  // circle 1 centers on wrist
	    double r0=keyframe.dimensions.getShoulderToElbow();  // circle 0 centers on shoulder
	    if( d > keyframe.dimensions.getElbowToWrist() + keyframe.dimensions.getShoulderToElbow() ) {
	      // The points are impossibly far apart, no solution can be found.
	      return false;  // should this throw an error because it's called from the constructor?
	    }
	    double a = ( r0 * r0 - r1 * r1 + d*d ) / ( 2.0f*d );
	    // find the midpoint
	    Vector3d mid=new Vector3d(es);
	    mid.scale(a/d);
	    mid.add(keyframe.shoulder);

	    // with a and r0 we can find h, the distance from midpoint to the intersections.
	    double h=Math.sqrt(r0*r0-a*a);
	    // the distance h on a line orthogonal to n and plane_normal gives us the two intersections.
		Vector3d n = new Vector3d(-arm_plane.y,arm_plane.x,0);
		n.normalize();
		Vector3d r = new Vector3d();
		r.cross(n, es);  // check this!
		r.normalize();
		r.scale(h);

		keyframe.elbow.set(mid);
		keyframe.elbow.sub(r);
		//Vector3d.add(mid, s, elbow);

		
		// find the angle between elbow-shoulder and the horizontal
		Vector3d bicep_forward = new Vector3d(keyframe.elbow);
		bicep_forward.sub(keyframe.shoulder);		  
		bicep_forward.normalize();
		double ax = bicep_forward.dot(arm_plane);
		double ay = bicep_forward.z;
		a1 = (float) -Math.atan2(ay,ax);

		// find the angle between elbow-wrist and the horizontal
		Vector3d ulna_forward = new Vector3d(keyframe.elbow);
		ulna_forward.sub(keyframe.wrist);
		ulna_forward.normalize();
		double bx = ulna_forward.dot(arm_plane);
		double by = ulna_forward.z;
		a2 = (float) Math.atan2(by,bx);

		// find the angle of the base
		a0 = (float) Math.atan2(keyframe.wrist.y,keyframe.wrist.x);
		
		// all angles are in radians, I want degrees
		keyframe.angleBase=Math.toDegrees(a0);
		keyframe.angleShoulder=Math.toDegrees(a1);
		keyframe.angleElbow=Math.toDegrees(a2);

		return true;
	}
	
	
	protected void FK(Arm3Keyframe keyframe) {
		Vector3d arm_plane = new Vector3d(Math.cos(Math.toRadians(keyframe.angleBase)),
					  					  Math.sin(Math.toRadians(keyframe.angleBase)),
					  					  0);
		keyframe.shoulder.set(arm_plane.x*keyframe.dimensions.getBaseToShoulderX(),
						      arm_plane.y*keyframe.dimensions.getBaseToShoulderX(),
						      			  keyframe.dimensions.getBaseToShoulderZ());
		
		keyframe.elbow.set(arm_plane.x*Math.cos(-Math.toRadians(keyframe.angleShoulder))*keyframe.dimensions.getShoulderToElbow(),
						   arm_plane.y*Math.cos(-Math.toRadians(keyframe.angleShoulder))*keyframe.dimensions.getShoulderToElbow(),
									   Math.sin(-Math.toRadians(keyframe.angleShoulder))*keyframe.dimensions.getShoulderToElbow());
		keyframe.elbow.add(keyframe.shoulder);

		keyframe.wrist.set(arm_plane.x*Math.cos(Math.toRadians(keyframe.angleElbow))*-keyframe.dimensions.getElbowToWrist(),
				 		   arm_plane.y*Math.cos(Math.toRadians(keyframe.angleElbow))*-keyframe.dimensions.getElbowToWrist(),
				 					   Math.sin(Math.toRadians(keyframe.angleElbow))*-keyframe.dimensions.getElbowToWrist());
		keyframe.wrist.add(keyframe.elbow);
		
		// build the axies around which we will rotate the tip
		Vector3d fn = new Vector3d();
		Vector3d up = new Vector3d(0,0,1);
		fn.cross(arm_plane,up);
		Vector3d axis = new Vector3d(keyframe.wrist);
		axis.sub(keyframe.elbow);
		axis.normalize();

		keyframe.fingerPosition.set(arm_plane);
		keyframe.fingerPosition.scale(keyframe.dimensions.getWristToFinger());
		keyframe.fingerPosition.add(keyframe.wrist);

		keyframe.fingerForward.set(keyframe.fingerPosition);
		keyframe.fingerForward.sub(keyframe.wrist);
		keyframe.fingerForward.normalize();
		
		keyframe.fingerRight.set(up); 
		keyframe.fingerRight.scale(-1);
		//keyframe.finger_right = MathHelper.rotateAroundAxis(keyframe.finger_right, axis,-keyframe.angle_3/RAD2DEG);
	}

	@Override
	public RobotKeyframe createKeyframe() {
		// TODO Auto-generated method stub
		return new Arm3Keyframe(armSettings);
	}
}
