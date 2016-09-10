package com.marginallyclever.makelangeloRobot.settings.hardwareProperties;

import com.jogamp.opengl.GL2;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettings;
import com.marginallyclever.robotOverlord.HTMLDialogBox;
import com.marginallyclever.robotOverlord.PrimitiveSolids;

public class Makelangelo2Properties implements MakelangeloHardwareProperties {
	public final static float PEN_HOLDER_RADIUS=6; //cm

	@Override
	public int getVersion() {
		return 2;
	}

	@Override
	public String getName() {
		return "Makelangelo 2+";
	}

	@Override
	public boolean canInvertMotors() {
		return true;
	}
	
	@Override
	public boolean canChangeMachineSize() {
		return true;
	}

	@Override
	public boolean canAccelerate() {
		return false;
	}

	@Override
	public boolean canChangePulleySize() {
		return false;
	}

	@Override
	public boolean canAutoHome() {
		return false;
	}

	public float getWidth() { return 0; }
	public float getHeight() { return 0; }
	
	@Override
	public void render(GL2 gl2,MakelangeloRobot robot) {
		MakelangeloRobotSettings settings = robot.getSettings();

		paintCalibrationPoint(gl2,settings);
		paintMotors(gl2,settings);
		paintControlBox(gl2,settings);
		paintPenHolderAndCounterweights(gl2,robot);		
	}
	

	// draw left & right motor
	protected void paintMotors( GL2 gl2,MakelangeloRobotSettings settings ) {
		double top = settings.getLimitTop();
		double right = settings.getLimitRight();
		double left = settings.getLimitLeft();

		
		gl2.glColor3f(1,0.8f,0.5f);
		// left frame
		gl2.glPushMatrix();
		gl2.glTranslated(left,top,0.25);
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glVertex2d(-5f, +5f);
		gl2.glVertex2d(+5f, +5f);
		gl2.glVertex2d(+5f,  0f);
		gl2.glVertex2d( 0f, -5f);
		gl2.glVertex2d(-5f, -5f);
		gl2.glEnd();
		gl2.glPopMatrix();

		// right frame
		gl2.glPushMatrix();
		gl2.glTranslated(right,top,0.25);
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glVertex2d(+5f, +5f);
		gl2.glVertex2d(-5f, +5f);
		gl2.glVertex2d(-5f,  0f);
		gl2.glVertex2d( 0f, -5f);
		gl2.glVertex2d(+5f, -5f);
		gl2.glEnd();
		gl2.glPopMatrix();

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
		gl2.glColor3f(1,0,0); 	gl2.glVertex2d(0,-0.3);	gl2.glVertex2d(left,-0.3);
		gl2.glColor3f(0,1,0); 	gl2.glVertex2d(0,-0.1);	gl2.glVertex2d(left,-0.1);
		gl2.glColor3f(0,0,1); 	gl2.glVertex2d(0, 0.1);	gl2.glVertex2d(left, 0.1);
		gl2.glColor3f(1,1,0); 	gl2.glVertex2d(0, 0.3);	gl2.glVertex2d(left, 0.3);
		

		gl2.glColor3f(1,0,0); 	gl2.glVertex2d(0, 0.3);	gl2.glVertex2d(right, 0.3);
		gl2.glColor3f(0,1,0); 	gl2.glVertex2d(0, 0.1);	gl2.glVertex2d(right, 0.1);
		gl2.glColor3f(0,0,1); 	gl2.glVertex2d(0,-0.1);	gl2.glVertex2d(right,-0.1);
		gl2.glColor3f(1,1,0); 	gl2.glVertex2d(0,-0.3);	gl2.glVertex2d(right,-0.3);
		gl2.glEnd();

		gl2.glTranslated(0,0,0.25);
		
		// UNO
		gl2.glColor3d(0,0,0.6);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(-4, 3);
		gl2.glVertex2d(+4, 3);
		gl2.glVertex2d(+4, -3);
		gl2.glVertex2d(-4, -3);
		gl2.glEnd();

		gl2.glPopMatrix();
	}
	
	protected void paintPenHolderAndCounterweights( GL2 gl2, MakelangeloRobot robot ) {
		MakelangeloRobotSettings settings = robot.getSettings();
		double dx,dy;
		double gx = robot.getGondolaX() / 10;
		double gy = robot.getGondolaY() / 10;
		
		double top = settings.getLimitTop();
		double bottom = settings.getLimitBottom();
		double left = settings.getLimitLeft();
		double right = settings.getLimitRight();
		
		double mw = right-left;
		double mh = top-settings.getLimitBottom();
		double suggestedLength = Math.sqrt(mw*mw+mh*mh)+5;

		dx = gx - left;
		dy = gy - top;
		double left_a = Math.sqrt(dx*dx+dy*dy);
		double left_b = (suggestedLength - left_a)/2;

		dx = gx - right;
		double right_a = Math.sqrt(dx*dx+dy*dy);
		double right_b = (suggestedLength - right_a)/2;

		if(gx<left) return;
		if(gx>right) return;
		if(gy>top) return;
		if(gy<bottom) return;
		
		gl2.glPushMatrix();
		gl2.glTranslated(0,0,0.5);
		
		
		gl2.glBegin(GL2.GL_LINES);
		gl2.glColor3d(0.2,0.2,0.2);
		
		// belt from motor to pen holder left
		gl2.glVertex2d(left, top);
		gl2.glVertex2d(gx,gy);
		// belt from motor to pen holder right
		gl2.glVertex2d(right, top);
		gl2.glVertex2d(gx,gy);
		
		float bottleCenter = 0.8f+0.75f;
		
		// belt from motor to counterweight left
		gl2.glVertex2d(left-bottleCenter-0.2, top);
		gl2.glVertex2d(left-bottleCenter-0.2, top-left_b);
		gl2.glVertex2d(left-bottleCenter+0.2, top);
		gl2.glVertex2d(left-bottleCenter+0.2, top-left_b);
		// belt from motor to counterweight right
		gl2.glVertex2d(right+bottleCenter-0.2, top);
		gl2.glVertex2d(right+bottleCenter-0.2, top-right_b);
		gl2.glVertex2d(right+bottleCenter+0.2, top);
		gl2.glVertex2d(right+bottleCenter+0.2, top-right_b);
		gl2.glEnd();

		// counterweight left
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glColor3f(0, 0, 1);
		gl2.glVertex2d(left-bottleCenter-1.5,top-left_b);
		gl2.glVertex2d(left-bottleCenter+1.5,top-left_b);
		gl2.glVertex2d(left-bottleCenter+1.5,top-left_b-15);
		gl2.glVertex2d(left-bottleCenter-1.5,top-left_b-15);
		gl2.glEnd();
		
		// counterweight right
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glColor3f(0, 0, 1);
		gl2.glVertex2d(right+bottleCenter-1.5,top-right_b);
		gl2.glVertex2d(right+bottleCenter+1.5,top-right_b);
		gl2.glVertex2d(right+bottleCenter+1.5,top-right_b-15);
		gl2.glVertex2d(right+bottleCenter-1.5,top-right_b-15);
		gl2.glEnd();
		
		gl2.glPopMatrix();
		
		// gondola
		gl2.glPushMatrix();
		gl2.glTranslated(0,0,0.25);
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glColor3f(0, 0, 1);
		float f;
		for(f=0;f<2.0*Math.PI;f+=0.3f) {
			gl2.glVertex2d(gx+Math.cos(f)*PEN_HOLDER_RADIUS,gy+Math.sin(f)*PEN_HOLDER_RADIUS);
		}
		gl2.glEnd();
		gl2.glPopMatrix();
	}


	/**
	 * draw calibration point
	 * @param gl2
	 */
	protected void paintCalibrationPoint(GL2 gl2,MakelangeloRobotSettings settings) {
		gl2.glColor3f(0.8f,0.8f,0.8f);
		gl2.glPushMatrix();
		gl2.glTranslated(settings.getHomeX(), settings.getHomeY(), 0.1);

		// pen holder
		gl2.glBegin(GL2.GL_LINE_LOOP);
		float f;
		for(f=0;f<2.0*Math.PI;f+=0.3f) {
			gl2.glVertex2d(	Math.cos(f)*(PEN_HOLDER_RADIUS+0.1),
							Math.sin(f)*(PEN_HOLDER_RADIUS+0.1)
							);
		}
		gl2.glEnd();

		gl2.glBegin(GL2.GL_LINES);
		gl2.glVertex2f(-0.25f,0.0f);
		gl2.glVertex2f( 0.25f,0.0f);
		gl2.glVertex2f(0.0f,-0.25f);
		gl2.glVertex2f(0.0f, 0.25f);
		gl2.glEnd();
		
		gl2.glPopMatrix();
	}

	@Override
	public void doAbout() {
		HTMLDialogBox box = new HTMLDialogBox();
		box.display(null, "<html><body>"
				+"<h1>"+this.getName()+"</h1>"
				+"<p>Created by Dan Royer (dan@marginallyclever.com).</p><br>"
				+"<p>A wall hanging polargraph art robot.</p><br>"
				+"<p><a href='https://www.marginallyclever.com/product/makelangelo-2.5'>Click here for more details</a>.</p>"
				+"</body></html>", "About "+this.getName());
	}
}
