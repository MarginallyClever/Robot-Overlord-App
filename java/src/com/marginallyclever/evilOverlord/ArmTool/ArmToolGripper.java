package com.marginallyclever.evilOverlord.ArmTool;

import java.util.ArrayList;

import javax.media.opengl.GL2;
import javax.swing.JPanel;


/**
 * special tool that renders the gripper at the right open/close state.
 * @author danroyer
 *
 */
public class ArmToolGripper extends ArmTool {
	// The servo angle
	protected float angleMax=160;
	protected float angleMin=120;
	protected float angle = angleMin;
	protected float servoDir = 0.0f;
	protected ArmToolGripperControlPanel armToolGripperControlPanel=null;

	
	public void render(GL2 gl2) {
		gl2.glPushMatrix();

		double c = Math.cos(Math.toRadians(180-angle));
		double s = Math.sin(Math.toRadians(180-angle));
		double y,z;

		boolean isLit= gl2.glIsEnabled(GL2.GL_LIGHTING);
		gl2.glDisable(GL2.GL_LIGHTING);
//		gl2.glDisable(GL2.GL_DEPTH_TEST);
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
//		gl2.glEnable(GL2.GL_DEPTH_TEST);

		gl2.glPopMatrix();
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
		
			if(armToolGripperControlPanel!=null) {
				armToolGripperControlPanel.updateGUI();
			}
		}
	}
	
	@Override
	public ArrayList<JPanel> getControlPanels() {
		ArrayList<JPanel> list = super.getControlPanels();
		
		if(list==null) list = new ArrayList<JPanel>();
		
		armToolGripperControlPanel = new ArmToolGripperControlPanel(this);
		list.add(armToolGripperControlPanel);
		
		return list;
	}
}
