package com.marginallyclever.evilOverlord.EvilMinionTool;

import java.util.ArrayList;

import javax.media.opengl.GL2;
import javax.swing.JPanel;
import javax.vecmath.Vector3f;

import com.marginallyclever.evilOverlord.Model;
import com.marginallyclever.evilOverlord.PrimitiveSolids;


/**
 * special tool that renders the gripper at the right open/close state.
 * @author danroyer
 *
 */
public class EvilMinionToolGripper extends EvilMinionTool {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5290710988214905673L;
	// The servo angle
	protected float angleMax=160;
	protected float angleMin=120;
	protected float angle = angleMin;
	protected float servoDir = 0.0f;
	protected EvilMinionToolGripperControlPanel armToolGripperControlPanel=null;

	protected Model modelAdapter = Model.loadModelBinary("/Gripper/Adapter.stl",0.1f);
	protected Model modelMain = Model.loadModel("/Gripper/Main.stl",0.1f);
	protected Model modelRearbar = Model.loadModel("/Gripper/Rearbar.stl",0.1f);
	protected Model modelFrontbar = Model.loadModel("/Gripper/Frontbar.stl",0.1f);
	protected Model modelLeftGear = Model.loadModel("/Gripper/LeftGear.stl",0.1f);
	protected Model modelRightGear = Model.loadModel("/Gripper/RightGear.stl",0.1f);
	protected Model modelBase = Model.loadModel("/Gripper/Base.stl",0.1f);
	protected Model modelGripper = Model.loadModel("/Gripper/Gripper.stl",0.1f);
	protected Model modelServo = Model.loadModel("/Spidee.zip:SG5010.stl");
	
	
	public void render(GL2 gl2) {
		//gl2.glPushMatrix();
		//renderWires(gl2);
		//gl2.glPopMatrix();
		gl2.glPushMatrix();
		renderMeshes(gl2);
		gl2.glPopMatrix();
	}
	
	public void renderMeshes(GL2 gl2) {
		double angleAdjusted = angle+20;
		
		double c = Math.cos(Math.toRadians(angleAdjusted));
		double s = Math.sin(Math.toRadians(angleAdjusted));
		
		// frame
		gl2.glPushMatrix();
		
		gl2.glTranslated(-1,0,0);
		gl2.glPushMatrix();
		this.setColor(gl2, 1,0,1,1);
		gl2.glRotatef(90,0,0,1);
		gl2.glTranslated(-1.375,-0.725,-1.9);
		modelAdapter.render(gl2);
		gl2.glPopMatrix();

		this.setColor(gl2, 0.8f,0.8f,0.8f,1);
		gl2.glTranslated(-0.41,-0.45,-0.2);
		modelBase.render(gl2);
		gl2.glTranslated(-5.3,0,0);
		modelMain.render(gl2);

		// servo
		this.setColor(gl2, 0.2f,0.2f,0.2f,1);
		gl2.glPushMatrix();
		gl2.glRotatef(-90,1,0,0);
		gl2.glRotatef(-90,0,1,0);
		gl2.glTranslated(2.4,-2.25,-3.55);
		modelServo.render(gl2);
		gl2.glPopMatrix();

		this.setColor(gl2, 0.8f,0.8f,0.8f,1);
		// left
		//this.setColor(gl2, 1,0,0,1);
		// gear
		gl2.glPushMatrix();
		gl2.glTranslated(2.615,1.415,-0.5);
		gl2.glRotated(angleAdjusted,0,0,1);
		gl2.glTranslated(-2.615,1.415,0.5);
		modelLeftGear.render(gl2);
		gl2.glPopMatrix();
		// gripper
		gl2.glPushMatrix();
		gl2.glTranslated(5.575-2.935+5.55,1.395-1.38,0);
		gl2.glRotatef(180,0,0,1);
		double d = 2.965;
		gl2.glTranslated(c*-d,s*-d,0);
		modelGripper.render(gl2);
		gl2.glPopMatrix();
		// bar
		gl2.glPushMatrix();
		gl2.glTranslated(0.7, 0.51,0);
		PrimitiveSolids.drawStar(gl2,new Vector3f(0,0,0));
		gl2.glRotated(angleAdjusted+175,0,0,1);
		gl2.glTranslated(4.6, 0.5, 0);
		gl2.glScaled(1,1,-1);
		modelRearbar.render(gl2);
		gl2.glRotated(180,1,0,0);
		//modelFrontbar.render(gl2);
		gl2.glPopMatrix();
		

		// right
		// gear
		//this.setColor(gl2, 0,1,0,1);
		gl2.glPushMatrix();
		gl2.glTranslated(2.615,-1.415,-0.5);
		gl2.glRotated(-angleAdjusted,0,0,1);
		gl2.glTranslated(-2.615,-1.415,0.5);
		modelRightGear.render(gl2);
		gl2.glPopMatrix();
		// gripper
		gl2.glPushMatrix();
		gl2.glTranslated(5.575-2.935+5.55,(-1.395-1.38),0);
		gl2.glRotatef(180,0,0,1);
		d = 2.965;
		gl2.glTranslated(c*-d,-s*-d,0);
		gl2.glRotatef(180,1,0,0);
		gl2.glTranslated(0,2.775,0.2);
		modelGripper.render(gl2);
		gl2.glPopMatrix();
		// bar
		gl2.glPushMatrix();
		gl2.glTranslated(0.7, -0.51,0);
		PrimitiveSolids.drawStar(gl2,new Vector3f(0,0,0));
		gl2.glRotated(-angleAdjusted+175,0,0,1);
		gl2.glTranslated(4.6, 0.5, 0);
		gl2.glScaled(1,1,-1);
		modelRearbar.render(gl2);
		gl2.glRotated(180,1,0,0);
		//modelFrontbar.render(gl2);
		gl2.glPopMatrix();
		
		// done!
		gl2.glPopMatrix();
	}
	
	public void renderWires(GL2 gl2) {
		double c = Math.cos(Math.toRadians(180-angle));
		double s = Math.sin(Math.toRadians(180-angle));
		double y,z;

		boolean isLit= gl2.glIsEnabled(GL2.GL_LIGHTING);
		gl2.glDisable(GL2.GL_LIGHTING);
		gl2.glColor3d(0,0,0);
		
		gl2.glBegin(GL2.GL_LINE_STRIP);
		gl2.glVertex3d(0, 0, 0);
		y=0;
		z=-2;
		gl2.glVertex3d(z, y, 0);
		y+=s*3;
		z-=c*3;
		gl2.glVertex3d(z, y, 0);
		z-=2;
		gl2.glVertex3d(z, y, 0);
		y-=1;
		gl2.glVertex3d(z, y, 0);
		z-=1;
		gl2.glVertex3d(z, y, 0);
		gl2.glEnd();
		
		gl2.glBegin(GL2.GL_LINE_STRIP);
		gl2.glVertex3d(0, 0, 0);
		y=0;
		z=-2;
		gl2.glVertex3d(z, y, 0);
		y-=s*3;
		z-=c*3;
		gl2.glVertex3d(z, y, 0);
		z-=2;
		gl2.glVertex3d(z, y, 0);
		y+=1;
		gl2.glVertex3d(z, y, 0);
		z-=1;
		gl2.glVertex3d(z, y, 0);
		gl2.glEnd();
		
		if(isLit) gl2.glEnable(GL2.GL_LIGHTING);
	}
	

	public void moveServo(float dir) {
		servoDir=dir;
	}
	
	
	public void update(float dt) {
		if(getAttachedTo()==null) return;
		
		boolean changed=false;
		float vel=(float)getAttachedTo().getSpeed(); // * delta
		
		float dS = angle;
		
		if(servoDir!=0) {
			dS += vel * servoDir;
			changed=true;
			servoDir=0;
		}
		
		if(changed==true) {
			if(dS<angleMin) dS=angleMin;
			if(dS>angleMax) dS=angleMax;
			angle = dS;

			getAttachedTo().sendLineToRobot("R10 S"+Float.toString(angle));
		
			updateGUI();
		}
	}
	
	@Override
	public ArrayList<JPanel> getControlPanels() {
		ArrayList<JPanel> list=null;
		
		if(getAttachedTo()==null) {
			// only show position when not attached.
			list = super.getControlPanels();
		}
		if(list==null) list = new ArrayList<JPanel>();
		
		armToolGripperControlPanel = new EvilMinionToolGripperControlPanel(this);
		list.add(armToolGripperControlPanel);
		updateGUI();
		
		return list;
	}
	
	@Override
	public void updateGUI() {
		super.updateGUI();
		
		if(armToolGripperControlPanel==null) return;
		armToolGripperControlPanel.servo.setText(Float.toString(angle));
	}
}
