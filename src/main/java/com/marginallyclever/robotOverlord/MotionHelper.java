package com.marginallyclever.robotOverlord;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.physicalObject.PhysicalObject;

@Deprecated
public class MotionHelper extends PhysicalObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MotionHelper() {
		setDisplayName("MotionHelper");
	}
	
	@Override
	public void render(GL2 gl2) {
		Vector3d p = this.getPosition();
		
		gl2.glPushMatrix();
			gl2.glTranslated(p.x, p.y, p.z);
			//gl2.glScalef(1, 1, 1);

			
			boolean isLit = gl2.glIsEnabled(GL2.GL_LIGHTING);
			gl2.glDisable(GL2.GL_LIGHTING);
			boolean isDepth = gl2.glIsEnabled(GL2.GL_DEPTH_TEST);
			gl2.glDisable(GL2.GL_DEPTH_TEST);
			boolean isCull = gl2.glIsEnabled(GL2.GL_CULL_FACE);
			
			gl2.glEnable(GL2.GL_CULL_FACE);
			renderRotation(gl2);
			
			gl2.glDisable(GL2.GL_CULL_FACE);
			renderTranslation(gl2);
			
			if(isCull) gl2.glEnable(GL2.GL_CULL_FACE);
			else gl2.glDisable(GL2.GL_CULL_FACE);

			if(isDepth) gl2.glEnable(GL2.GL_DEPTH_TEST);
			if(isLit) gl2.glEnable(GL2.GL_LIGHTING);
			
		gl2.glPopMatrix();	
	}

	public void renderRotation(GL2 gl2) {
		final double res=40;
		final double radius=10;
		final double ht=0.1;  // half thickness
		double i,x,y;
		
		// z
		gl2.glPushName(7);
		gl2.glColor3d(1,0,0);
		gl2.glBegin(GL2.GL_TRIANGLE_STRIP);
		for(i=0;i<=res;++i) {
			x = Math.cos(Math.PI * 2.0 * i/res);
			y = Math.sin(Math.PI * 2.0 * i/res);
			gl2.glVertex3d(x*radius, y*radius,  ht);
			gl2.glVertex3d(x*radius, y*radius, -ht);
		}
		gl2.glEnd();
		gl2.glPopName();
		
		// y
		gl2.glPushName(8);
		gl2.glColor3d(0,1,0);
		gl2.glBegin(GL2.GL_TRIANGLE_STRIP);
		for(i=0;i<=res;++i) {
			x = Math.cos(Math.PI * 2.0 * i/res);
			y = Math.sin(Math.PI * 2.0 * i/res);
			gl2.glVertex3d(x*radius, -ht, y*radius);
			gl2.glVertex3d(x*radius,  ht, y*radius);
		}
		gl2.glEnd();
		gl2.glPopName();
		
		// x
		gl2.glPushName(9);
		gl2.glColor3d(0,0,1);
		gl2.glBegin(GL2.GL_TRIANGLE_STRIP);
		for(i=0;i<=res;++i) {
			x = Math.cos(Math.PI * 2.0 * i/res);
			y = Math.sin(Math.PI * 2.0 * i/res);
			gl2.glVertex3d( ht, x*radius, y*radius);
			gl2.glVertex3d(-ht, x*radius, y*radius);
		}
		gl2.glEnd();
		gl2.glPopName();
	}
	
	public void renderTranslation(GL2 gl2) {

		// Z
		gl2.glPushName(3);
		gl2.glColor3d(1,0,0);  drawArrow(gl2);
		gl2.glPopName();

		// Y?
		gl2.glPushName(2);
		gl2.glPushMatrix();
		gl2.glRotated(-90, 1, 0, 0);
		gl2.glColor3d(0,1,0);  drawArrow(gl2);
		gl2.glPopMatrix();
		gl2.glPopName();
		
		// X?
		gl2.glPushName(1);
		gl2.glPushMatrix();
		gl2.glRotated(90, 0, 1, 0);
		gl2.glColor3d(0,0,1);  drawArrow(gl2);
		gl2.glPopMatrix();	
		gl2.glPopName();

		// planes
		final double q=3;
		
		// xz
		gl2.glPushName(4);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glColor4d(1,0,1,0.5);
		gl2.glVertex3d(0,0,0);
		gl2.glVertex3d(q,0,0);
		gl2.glVertex3d(q,0,q);
		gl2.glVertex3d(0,0,q);
		gl2.glEnd();
		gl2.glPopName();
		// xy
		gl2.glPushName(5);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glColor4d(1,1,0,0.5);
		gl2.glVertex3d(0,0,0);
		gl2.glVertex3d(q,0,0);
		gl2.glVertex3d(q,q,0);
		gl2.glVertex3d(0,q,0);
		gl2.glEnd();
		gl2.glPopName();
		// yz
		gl2.glPushName(6);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glColor4d(0,1,1,0.5);
		gl2.glVertex3d(0,0,0);
		gl2.glVertex3d(0,q,0);
		gl2.glVertex3d(0,q,q);
		gl2.glVertex3d(0,0,q);
		gl2.glEnd();
		gl2.glPopName();

	}
	
	public void drawArrow(GL2 gl2) {
		final double arrowLength=10;
		final double tipLength=2;
		final double tipRadius=0.5;
		final double res=10;

		gl2.glBegin(GL2.GL_LINES);
		gl2.glVertex3d(0,0,0);
		gl2.glVertex3d(0,0,arrowLength);
		gl2.glEnd();
		// cone
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glVertex3d(0, 0, arrowLength);
		for(double i=0;i<=res;++i) {
			double x = Math.cos(Math.PI * 2.0 * i/res);
			double y = Math.sin(Math.PI * 2.0 * i/res);
			gl2.glVertex3d(x*tipRadius, y*tipRadius, arrowLength-tipLength);
		}
		gl2.glEnd();
	}
}
