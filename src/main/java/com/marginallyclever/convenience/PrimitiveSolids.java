package com.marginallyclever.convenience;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.OpenGLHelper;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;

import javax.vecmath.Matrix4d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

/**
 * Static methods to help with rendering some simple shapes in OpenGL.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class PrimitiveSolids {
	static public Mesh drawCircleXY(GL3 gl, double radius, int steps) {
		double stepSize = Math.PI*2 / (double)(steps+1);

		Mesh mesh = new Mesh();
		mesh.setRenderStyle(GL3.GL_LINE_LOOP);
		for(double n=0;n<Math.PI*2;n+=stepSize) {
			double c = Math.cos(n);
			double s = Math.sin(n);
			mesh.addVertex((float)(c*radius), (float)(s*radius),0);
		}
		return mesh;
	}

	/**
	 * Draw box based on two corners
	 * @param gl systems context
	 * @param bottom minimum bounds
	 * @param top maximum bounds
	 * @return mesh
	 */
	static public Mesh drawBoxWireframe(GL3 gl,Tuple3d bottom,Tuple3d top) {
		boolean tex = OpenGLHelper.disableTextureStart(gl);
		
		float x0 = (float)bottom.x;
		float y0 = (float)bottom.y;
		float z0 = (float)bottom.z;
		float x1 = (float)top.x;
		float y1 = (float)top.y;
		float z1 = (float)top.z;

		Mesh mesh = new Mesh();
		mesh.setRenderStyle(GL3.GL_LINE_LOOP);

		// go around bottom
		mesh.addVertex(x0,y0,z0);

		// climb each side and back.
		mesh.addVertex(x0,y1,z0);
		mesh.addVertex(x0,y1,z1);
		mesh.addVertex(x0,y1,z0);

		mesh.addVertex(x1,y1,z0);
		mesh.addVertex(x1,y1,z1);
		mesh.addVertex(x1,y1,z0);

		mesh.addVertex(x1,y0,z0);
		mesh.addVertex(x1,y0,z1);
		mesh.addVertex(x1,y0,z0);

		mesh.addVertex(x0,y0,z0);

		// then go around top edge.
		mesh.addVertex(x0,y0,z1);
		mesh.addVertex(x1,y0,z1);
		mesh.addVertex(x1,y1,z1);
		mesh.addVertex(x0,y1,z1);

		return mesh;
	}
}
