package com.marginallyclever.convenience.bezier3;

import com.jogamp.opengl.GL3;

import javax.vecmath.Vector3d;

/**
 * 3D Bezier curve implementation
 * See <a href="https://en.wikipedia.org/wiki/B%C3%A9zier_curve">Wikipedia</a>
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class Bezier3ControlPoint {
	public Bezier3 position = new Bezier3();
	
	/**
	 * visualize the line in opengl
	 * @param gl
	 */
	public void render(GL3 gl) {
		//Vector3d u,v,w;
		
		//MatrixHelper.drawMatrix(gl, position.interpolate(0), u, v, w);
		//MatrixHelper.drawMatrix(gl, position.interpolate(1), u, v, w);
		boolean isLit = gl.glIsEnabled(GL3.GL_LIGHTING);
		boolean isCM =  gl.glIsEnabled(GL3.GL_COLOR_MATERIAL);
		boolean isDepth =  gl.glIsEnabled(GL3.GL_DEPTH_TEST);

		gl.glEnable(GL3.GL_DEPTH_TEST);
		gl.glDisable(GL3.GL_LIGHTING);
		gl.glDisable(GL3.GL_COLOR_MATERIAL);
		
		//*
		gl.glColor4f(0, 0, 1, 1);
		gl.glBegin(GL3.GL_LINES);
		gl.glVertex3d(position.p0.x,position.p0.y,position.p0.z);
		gl.glVertex3d(position.p1.x,position.p1.y,position.p1.z);
		
		gl.glVertex3d(position.p2.x,position.p2.y,position.p2.z);
		gl.glVertex3d(position.p3.x,position.p3.y,position.p3.z);
		gl.glEnd();
		//*/
		
		gl.glColor4f(0, 1, 0, 1);
		gl.glBegin(GL3.GL_LINE_STRIP);
		final float NUM_STEPS=20;
		for(float i=0;i<=NUM_STEPS;++i) {
			Vector3d ipos = position.interpolate(i/NUM_STEPS);
			gl.glVertex3d(ipos.x,ipos.y,ipos.z);
		}
		gl.glEnd();
		
		if(isLit) gl.glEnable(GL3.GL_LIGHTING);
		if(isCM) gl.glEnable(GL3.GL_COLOR_MATERIAL);
		if(!isDepth) gl.glDisable(GL3.GL_DEPTH_TEST);
	}
}
