package com.marginallyclever.robotOverlord.makelangeloRobot.settings.hardwareProperties;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.makelangeloRobot.MakelangeloRobot;
import com.marginallyclever.robotOverlord.makelangeloRobot.settings.MakelangeloRobotSettings;
import com.marginallyclever.robotOverlord.HTMLDialogBox;

public class Makelangelo3Properties extends Makelangelo2Properties {
	@Override
	public int getVersion() {
		return 3;
	}

	@Override
	public String getName() {
		return "Makelangelo 3+";
	}

	@Override
	public boolean canChangeMachineSize() {
		return true;
	}

	@Override
	public boolean canAccelerate() {
		return true;
	}

	@Override
	public void render(GL2 gl2,MakelangeloRobot robot) {
		MakelangeloRobotSettings settings = robot.getSettings();

		super.paintCalibrationPoint(gl2,settings);
		super.paintMotors(gl2,settings);
		paintControlBox(gl2,settings);
		super.paintPenHolderAndCounterweights(gl2,robot);		
	}

	/**
	 * paint the controller and the LCD panel
	 * @param gl2 the opengl context to call when rendering this hardware
	 * @param settings device specific settings 
	 */
	protected void paintControlBox(GL2 gl2,MakelangeloRobotSettings settings) {
		double cy = settings.getLimitTop();
		double left = settings.getLimitLeft();
		double right = settings.getLimitRight();
		double cx = 0;

		gl2.glPushMatrix();
		gl2.glTranslated(cx, cy, 0.25);
		
		// mounting plate for PCB
		gl2.glColor3f(1,0.8f,0.5f);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(-8, 5);
		gl2.glVertex2d(+8, 5);
		gl2.glVertex2d(+8, -5);
		gl2.glVertex2d(-8, -5);
		gl2.glEnd();

		// wires to each motor
		gl2.glTranslated(0,0,0.25);
		gl2.glBegin(GL2.GL_LINES);
		gl2.glColor3f(1,0,0); 	gl2.glVertex2d(0,-0.3);	gl2.glVertex2d(left+2.1f,-0.3);
		gl2.glColor3f(0,1,0); 	gl2.glVertex2d(0,-0.1);	gl2.glVertex2d(left+2.1f,-0.1);
		gl2.glColor3f(0,0,1); 	gl2.glVertex2d(0, 0.1);	gl2.glVertex2d(left+2.1f, 0.1);
		gl2.glColor3f(1,1,0); 	gl2.glVertex2d(0, 0.3);	gl2.glVertex2d(left+2.1f, 0.3);

		gl2.glColor3f(1,0,0); 	gl2.glVertex2d(0, 0.3);	gl2.glVertex2d(right-2.1f, 0.3);
		gl2.glColor3f(0,1,0); 	gl2.glVertex2d(0, 0.1);	gl2.glVertex2d(right-2.1f, 0.1);
		gl2.glColor3f(0,0,1); 	gl2.glVertex2d(0,-0.1);	gl2.glVertex2d(right-2.1f,-0.1);
		gl2.glColor3f(1,1,0); 	gl2.glVertex2d(0,-0.3);	gl2.glVertex2d(right-2.1f,-0.3);
		gl2.glEnd();
		
		// RUMBA in v3 (135mm*75mm)
		gl2.glTranslated(0,0,0.25);
		float h = 7.5f/2;
		float w = 13.5f/2;
		gl2.glColor3d(0.9,0.9,0.9);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(-w, h);
		gl2.glVertex2d(+w, h);
		gl2.glVertex2d(+w, -h);
		gl2.glVertex2d(-w, -h);
		gl2.glEnd();

		renderLCD(gl2);

		gl2.glPopMatrix();
	}
	
	protected void renderLCD(GL2 gl2) {
		// position
		gl2.glPushMatrix();
		gl2.glTranslated(-18, 0, 0);
		
		// mounting plate for LCD
		gl2.glColor3f(1,0.8f,0.5f);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(-8, 5);
		gl2.glVertex2d(+8, 5);
		gl2.glVertex2d(+8, -5);
		gl2.glVertex2d(-8, -5);
		gl2.glEnd();

		// LCD red
		gl2.glTranslated(0, 0, 0.1);
		float w = 15.0f/2;
		float h = 5.6f/2;
		gl2.glColor3f(0.8f,0.0f,0.0f);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(-w, h);
		gl2.glVertex2d(+w, h);
		gl2.glVertex2d(+w, -h);
		gl2.glVertex2d(-w, -h);
		gl2.glEnd();

		// LCD green
		gl2.glPushMatrix();
		gl2.glTranslated(-(2.6)/2, -0.771, 0.1);
		
		w = 9.8f/2;
		h = 6.0f/2;
		gl2.glColor3f(0,0.6f,0.0f);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(-w, h);
		gl2.glVertex2d(+w, h);
		gl2.glVertex2d(+w, -h);
		gl2.glVertex2d(-w, -h);
		gl2.glEnd();

		// LCD black
		gl2.glTranslated(0, 0, 0.1);
		h = 4.0f/2;
		gl2.glColor3f(0,0,0);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(-w, h);
		gl2.glVertex2d(+w, h);
		gl2.glVertex2d(+w, -h);
		gl2.glVertex2d(-w, -h);
		gl2.glEnd();

		// LCD blue
		gl2.glTranslated(0, 0, 0.1);
		h = 2.5f/2;
		w = 7.5f/2;
		gl2.glColor3f(0,0,0.7f);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(-w, h);
		gl2.glVertex2d(+w, h);
		gl2.glVertex2d(+w, -h);
		gl2.glVertex2d(-w, -h);
		gl2.glEnd();
		
		gl2.glPopMatrix();

		// clean up
		gl2.glPopMatrix();
	}

	@Override
	public void doAbout() {
		HTMLDialogBox box = new HTMLDialogBox();
		box.display(null, "<html><body>"
				+"<h1>"+this.getName()+"</h1>"
				+"<p>Created by Dan Royer (dan@marginallyclever.com).</p><br>"
				+"<p>A wall hanging polargraph art robot.</p><br>"
				+"<p><a href='https://www.marginallyclever.com/product/makelangelo-3.2'>Click here for more details</a>.</p>"
				+"</body></html>", "About "+this.getName());
	}
}
