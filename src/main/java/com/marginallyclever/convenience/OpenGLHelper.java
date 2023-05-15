package com.marginallyclever.convenience;

import com.jogamp.opengl.GL2;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * A collection of static methods to help with OpenGL.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class OpenGLHelper {
	static public int drawAtopEverythingStart(GL2 gl2) {
		IntBuffer depthFunc = IntBuffer.allocate(1);
		gl2.glGetIntegerv(GL2.GL_DEPTH_FUNC, depthFunc);
		gl2.glDepthFunc(GL2.GL_ALWAYS);
		return depthFunc.get();
	}
	
	static public void drawAtopEverythingEnd(GL2 gl2, int previousState) {
		gl2.glDepthFunc(previousState);
	}
	
	static public boolean disableLightingStart(GL2 gl2) {
		boolean lightWasOn = gl2.glIsEnabled(GL2.GL_LIGHTING);
		gl2.glDisable(GL2.GL_LIGHTING);
		return lightWasOn;
	}

	static public void disableLightingEnd(GL2 gl2,boolean lightWasOn) {
		if(lightWasOn) gl2.glEnable(GL2.GL_LIGHTING);
	}

	static public float setLineWidth(GL2 gl2,float newWidth) {
		FloatBuffer lineWidth = FloatBuffer.allocate(1);
		gl2.glGetFloatv(GL2.GL_LINE_WIDTH, lineWidth);
		gl2.glLineWidth(newWidth);
		return lineWidth.get(0);
	}
	
	static public double [] getCurrentColor(GL2 gl2) {
		double [] rgba = new double[4];
		gl2.glGetDoublev(GL2.GL_CURRENT_COLOR, rgba, 0);
		return rgba;
	}

	// draw line from origin to v.
	public static void drawVector3d(GL2 gl2,Vector3d v) {
		gl2.glBegin(GL2.GL_LINES);
		gl2.glVertex3d(0, 0, 0);
		gl2.glVertex3d(v.x,v.y,v.z);
		gl2.glEnd();
	}

	// draw line from start to end.
	public static void drawLine(GL2 gl2, Vector3d start, Vector3d end) {
		gl2.glBegin(GL2.GL_LINES);
		//gl2.glColor3d(0, 0, 0);
		gl2.glVertex3d(start.x,start.y,start.z);
		//gl2.glColor3d(1, 1, 1);
		gl2.glVertex3d(end.x,end.y,end.z);
		gl2.glEnd();
	}

	// draw vector v at position p.
	public static void drawVector3dFrom(GL2 gl2, Vector3d v, Vector3d p) {
		gl2.glBegin(GL2.GL_LINES);
		//gl2.glColor3d(0, 0, 0);
		gl2.glVertex3d(p.x,p.y,p.z);
		//gl2.glColor3d(1, 1, 1);
		gl2.glVertex3d(p.x+v.x,p.y+v.y,p.z+v.z);
		gl2.glEnd();
	}

	public static Matrix4d getModelviewMatrix(GL2 gl2) {
		FloatBuffer ptr = FloatBuffer.allocate(16);
		gl2.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, ptr);
		float [] fArray = ptr.array();
		double [] dArray = new double[fArray.length];
		for(int i=0;i<fArray.length;++i) dArray[i]=fArray[i];
		return new Matrix4d(dArray);
	}

	public static Matrix4d getProjectionMatrix(GL2 gl2) {
		FloatBuffer ptr = FloatBuffer.allocate(16);
		gl2.glGetFloatv(GL2.GL_PROJECTION_MATRIX, ptr);
		float [] fArray = ptr.array();
		double [] dArray = new double[fArray.length];
		for(int i=0;i<fArray.length;++i) dArray[i]=fArray[i];
		return new Matrix4d(dArray);
	}

	public static boolean disableTextureStart(GL2 gl2) {
		boolean b = gl2.glIsEnabled(GL2.GL_TEXTURE_2D);
		gl2.glDisable(GL2.GL_TEXTURE_2D);
		return b;
	}
	
	public static void disableTextureEnd(GL2 gl2,boolean oldState) {
		if(oldState) gl2.glEnable(GL2.GL_TEXTURE_2D);
	}
}
