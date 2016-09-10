package com.marginallyclever.makelangeloRobot.settings.hardwareProperties;

import com.jogamp.opengl.GL2;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettings;
import com.marginallyclever.robotOverlord.HTMLDialogBox;
import com.marginallyclever.robotOverlord.PrimitiveSolids;

public class Makelangelo5Properties extends Makelangelo3Properties {
	@Override
	public int getVersion() {
		return 5;
	}
	
	@Override
	public String getName() {
		return "Makelangelo 5+";
	}
	
	@Override
	public boolean canInvertMotors() {
		return false;
	}

	@Override
	public boolean canChangeMachineSize() {
		return false;
	}
	public float getWidth() { return 835; }
	public float getHeight() { return 1200; }

	@Override
	public boolean canAutoHome() {
		return true;
	}

	@Override
	public void render(GL2 gl2,MakelangeloRobot robot) {
		MakelangeloRobotSettings settings = robot.getSettings();

		super.paintCalibrationPoint(gl2,settings);
		paintMotors(gl2,settings);
		paintControlBox(gl2,settings);
		super.paintPenHolderAndCounterweights(gl2,robot);		
	}
	

	// draw left & right motor
	protected void paintMotors( GL2 gl2,MakelangeloRobotSettings settings ) {
		double top = settings.getLimitTop();
		double right = settings.getLimitRight();
		double left = settings.getLimitLeft();

		gl2.glTranslated(0, 0, 0.1);
		
		gl2.glColor3f(1,0.8f,0.5f);
		// frame
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(left-5f, top+5f);
		gl2.glVertex2d(right+5f, top+5f);
		gl2.glVertex2d(right+5f, top-5f);
		gl2.glVertex2d(left-5f, top-5f);
		gl2.glEnd();

		gl2.glColor3f(0,0,0);
		// left motor
		gl2.glPushMatrix();
		gl2.glTranslated(left,top, 0.75);
		PrimitiveSolids.drawBox(gl2, 4.2f,4.2f,4.2f);
		gl2.glPopMatrix();
		
		// right motor
		gl2.glPushMatrix();
		gl2.glTranslated(right,top, 0.75);
		PrimitiveSolids.drawBox(gl2, 4.2f,4.2f,4.2f);
		gl2.glPopMatrix();
	}

	@Override
	public void doAbout() {
		HTMLDialogBox box = new HTMLDialogBox();
		box.display(null, "<html><body>"
				+"<h1>"+this.getName()+"</h1>"
				+"<p>Created by Dan Royer (dan@marginallyclever.com).</p><br>"
				+"<p>A wall hanging polargraph art robot.</p><br>"
				+"<p><a href='https://www.marginallyclever.com/product/makelangelo-5.0'>Click here for more details</a>.</p>"
				+"</body></html>", "About "+this.getName());
	}
}
