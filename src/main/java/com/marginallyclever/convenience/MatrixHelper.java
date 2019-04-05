package com.marginallyclever.convenience;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;

public class MatrixHelper {

	
	/**
	 * @see drawMatrix(gl2,p,u,v,w,1)
	 * @param gl2
	 * @param p
	 * @param u
	 * @param v
	 * @param w
	 */
	public static void drawMatrix(GL2 gl2,Vector3d p,Vector3d u,Vector3d v,Vector3d w) {
		drawMatrix(gl2,p,u,v,w,1);
	}
	
	/**
	 * Draw the three vectors of a matrix at a point
	 * @param gl2 render context
	 * @param p position at which to draw
	 * @param u in yellow (1,1,0)
	 * @param v in teal (0,1,1)
	 * @param w in magenta (1,0,1)
	 * @param scale nominally 1
	 */
	public static void drawMatrix(GL2 gl2,Vector3d p,Vector3d u,Vector3d v,Vector3d w,float scale) {
		//boolean depthWasOn = gl2.glIsEnabled(GL2.GL_DEPTH_TEST);
		//gl2.glDisable(GL2.GL_DEPTH_TEST);
			
		gl2.glPushMatrix();
			gl2.glTranslated(p.x, p.y, p.z);
			gl2.glScalef(scale, scale, scale);
			
			gl2.glBegin(GL2.GL_LINES);
			gl2.glColor3f(1,1,0);		gl2.glVertex3f(0,0,0);		gl2.glVertex3d(u.x,u.y,u.z);  // 1,1,0 = yellow
			gl2.glColor3f(0,1,1);		gl2.glVertex3f(0,0,0);		gl2.glVertex3d(v.x,v.y,v.z);  // 0,1,1 = teal 
			gl2.glColor3f(1,0,1);		gl2.glVertex3f(0,0,0);		gl2.glVertex3d(w.x,w.y,w.z);  // 1,0,1 = magenta
			gl2.glEnd();

		gl2.glPopMatrix();
		
		//if(depthWasOn) gl2.glEnable(GL2.GL_DEPTH_TEST);
	}

	/**
	 * Same as drawMatrix, but with alternate colors
	 * @see drawMatrix(gl2,p,u,v,w,1)
	 * @param gl2
	 * @param p
	 * @param u
	 * @param v
	 * @param w
	 */
	public static void drawMatrix2(GL2 gl2,Vector3d p,Vector3d u,Vector3d v,Vector3d w) {
		drawMatrix2(gl2,p,u,v,w,1);
	}
	
	/**
	 * Same as drawMatrix, but with alternate colors
	 * Draw the three vectors of a matrix at a point
	 * @param gl2 render context
	 * @param p position at which to draw
	 * @param u in red
	 * @param v in green
	 * @param w in blue
	 * @param scale nominally 1
	 */
	public static void drawMatrix2(GL2 gl2,Vector3d p,Vector3d u,Vector3d v,Vector3d w,float scale) {
		boolean depthWasOn = gl2.glIsEnabled(GL2.GL_DEPTH_TEST);
		gl2.glDisable(GL2.GL_DEPTH_TEST);
			
		gl2.glPushMatrix();
			gl2.glTranslated(p.x, p.y, p.z);
			gl2.glScalef(scale, scale, scale);
			
			gl2.glBegin(GL2.GL_LINES);
			gl2.glColor3f(1,0,0);		gl2.glVertex3f(0,0,0);		gl2.glVertex3d(u.x,u.y,u.z);  // 1,0,0 = red
			gl2.glColor3f(0,1,0);		gl2.glVertex3f(0,0,0);		gl2.glVertex3d(v.x,v.y,v.z);  // 0,1,0 = green 
			gl2.glColor3f(0,0,1);		gl2.glVertex3f(0,0,0);		gl2.glVertex3d(w.x,w.y,w.z);  // 0,0,1 = blue
			gl2.glEnd();

		gl2.glPopMatrix();
		
		if(depthWasOn) gl2.glEnable(GL2.GL_DEPTH_TEST);
	}
}
