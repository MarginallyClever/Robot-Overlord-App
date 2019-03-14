package com.marginallyclever.robotOverlord.lines;

import javax.vecmath.Vector3f;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.math.Quaternion;
import com.marginallyclever.convenience.MatrixHelper;

/**
 * 3D Bezier curve implementation
 * @author Dan Royer
 * @see https://en.wikipedia.org/wiki/B%C3%A9zier_curve
 *
 */
public class LineControlPoint {
	public LineBezier position;
	//public LineBezier4 angle;
	
	//Quaternion q = new Quaternion;
	
	/**
	 * visualize the line in opengl
	 * @param gl2
	 */
	void render(GL2 gl2) {
		Vector3f u,v,w;
		
		//MatrixHelper.drawMatrix(gl2, position.interpolate(0), u, v, w);
		//MatrixHelper.drawMatrix(gl2, position.interpolate(1), u, v, w);
		
		gl2.glBegin(GL2.GL_LINES);
		final float NUM_STEPS=10;
		for(float i=0;i<=NUM_STEPS;++i) {
			Vector3f ipos = position.interpolate(i/NUM_STEPS);
			gl2.glVertex3f(ipos.x,ipos.y,ipos.z);
		}
		gl2.glEnd();
	}
}
