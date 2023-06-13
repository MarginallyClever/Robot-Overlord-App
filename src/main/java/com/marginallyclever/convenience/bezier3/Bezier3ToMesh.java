package com.marginallyclever.convenience.bezier3;

import com.jogamp.opengl.GL3;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;

import javax.vecmath.Vector3d;

/**
 * 3D Bezier curve implementation
 * See <a href="https://en.wikipedia.org/wiki/B%C3%A9zier_curve">Wikipedia</a>
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class Bezier3ToMesh {
	public Bezier3 position = new Bezier3();
	
	/**
	 * visualize the line in opengl
	 * @param gl
	 */
	public Mesh generate() {
		Mesh mesh = new Mesh();
		mesh.setRenderStyle(GL3.GL_LINE_STRIP);
		mesh.addColor(0,0,1,1);		mesh.addVertex( (float)position.p0.x, (float)position.p0.y, (float)position.p0.z );
		mesh.addColor(0,0,1,1);		mesh.addVertex( (float)position.p1.x, (float)position.p1.y, (float)position.p1.z );
		mesh.addColor(0,0,1,1);		mesh.addVertex( (float)position.p2.x, (float)position.p2.y, (float)position.p2.z );
		mesh.addColor(0,0,1,1);		mesh.addVertex( (float)position.p3.x, (float)position.p3.y, (float)position.p3.z );


		final float NUM_STEPS=20;
		for(float i=NUM_STEPS;i>=0;--i) {
			Vector3d ipos = position.interpolate(i/NUM_STEPS);
			mesh.addColor(0,1,0,1);
			mesh.addVertex( (float)ipos.x, (float)ipos.y, (float)ipos.z );
		}
		return mesh;
	}
}
