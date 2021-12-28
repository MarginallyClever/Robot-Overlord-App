package com.marginallyclever.convenience;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;
import com.jogamp.opengl.GL2;


public class PrimitiveSolids {
	static public void drawCircleYZ(GL2 gl2,double radius,int steps) {
		double stepSize = Math.PI*2 / (double)(steps+1);
		
		gl2.glBegin(GL2.GL_LINE_LOOP);
		for(double n=0;n<Math.PI*2;n+=stepSize) {
			double c = Math.cos(n);
			double s = Math.sin(n);
			gl2.glVertex3d(0,c*radius, s*radius);
		}
		gl2.glEnd();
	}
	
	static public void drawCircleXZ(GL2 gl2,double radius,int steps) {
		double stepSize = Math.PI*2 / (double)(steps+1);
		
		gl2.glBegin(GL2.GL_LINE_LOOP);
		for(double n=0;n<Math.PI*2;n+=stepSize) {
			double c = Math.cos(n);
			double s = Math.sin(n);
			gl2.glVertex3d(c*radius,0, s*radius);
		}
		gl2.glEnd();
	}
	
	static public void drawCircleXY(GL2 gl2,double radius,int steps) {
		double stepSize = Math.PI*2 / (double)(steps+1);
		
		gl2.glBegin(GL2.GL_LINE_LOOP);
		for(double n=0;n<Math.PI*2;n+=stepSize) {
			double c = Math.cos(n);
			double s = Math.sin(n);
			gl2.glVertex3d(c*radius, s*radius,0);
		}
		gl2.glEnd();
	}
	
	/**
	 * draw a sphere with a given radius.
	 * TODO expose quality parameters?
	 * TODO generate a sphere once as a shape, return that.
	 * See https://www.gamedev.net/forums/topic/537269-procedural-sphere-creation/4469427/
	 * @param gl2
	 * @param radius
	 */
	static public void drawSphere(GL2 gl2,double radius) {
		int width = 32;
		int height = 16;
		
		double theta, phi;
		int i, j, t;

		int nvec = (height-2)* width + 2;
		int ntri = (height-2)*(width-1)*2;

		FloatBuffer vertices = FloatBuffer.allocate(nvec * 3);
		IntBuffer indexes = IntBuffer.allocate(ntri * 3);

		float [] dat = vertices.array();
		int   [] idx = indexes.array();
		
		for( t=0, j=1; j<height-1; j++ ) {
			for(i=0; i<width; i++ )  {
				theta = (double)(j)/(double)(height-1) * Math.PI;
				phi   = (double)(i)/(double)(width-1 ) * Math.PI*2;

				dat[t++] = (float)( Math.sin(theta) * Math.cos(phi));
				dat[t++] = (float)( Math.cos(theta));
				dat[t++] = (float)(-Math.sin(theta) * Math.sin(phi));
			}
		}
		dat[t++]= 0;
		dat[t++]= 1;
		dat[t++]= 0;
		dat[t++]= 0;
		dat[t++]=-1;
		dat[t++]= 0;
		
		for( t=0, j=0; j<height-3; j++ ) {
			for(      i=0; i<width-1; i++ )  {
				idx[t++] = (j  )*width + i  ;
				idx[t++] = (j+1)*width + i+1;
				idx[t++] = (j  )*width + i+1;
				idx[t++] = (j  )*width + i  ;
				idx[t++] = (j+1)*width + i  ;
				idx[t++] = (j+1)*width + i+1;
			}
		}
		for( i=0; i<width-1; i++ )  {
			idx[t++] = (height-2)*width;
			idx[t++] = i;
			idx[t++] = i+1;
			idx[t++] = (height-2)*width+1;
			idx[t++] = (height-3)*width + i+1;
			idx[t++] = (height-3)*width + i;
		}

		int NUM_BUFFERS=1;
		int[] VBO = new int[NUM_BUFFERS];
		gl2.glGenBuffers(NUM_BUFFERS, VBO, 0);
		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, VBO[0]);
	    // Write out vertex buffer to the currently bound VBO.
		int s=(Float.SIZE/8);  // bits per float / bits per byte = bytes per float
	    gl2.glBufferData(GL2.GL_ARRAY_BUFFER, dat.length*s, vertices, GL2.GL_STATIC_DRAW);
	    
	    
		gl2.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl2.glVertexPointer(3,GL2.GL_FLOAT,0,0);
		
		gl2.glEnableClientState(GL2.GL_NORMAL_ARRAY);
		gl2.glNormalPointer(GL2.GL_FLOAT,0,0);

		gl2.glPushMatrix();
		gl2.glScaled(radius,radius,radius);
		gl2.glDrawElements(GL2.GL_TRIANGLES, ntri*3, GL2.GL_UNSIGNED_INT, indexes );
		gl2.glPopMatrix();
		
		gl2.glDisableClientState(GL2.GL_NORMAL_ARRAY);
		gl2.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		
		gl2.glDeleteBuffers(NUM_BUFFERS, VBO, 0);
	}

	static public void drawCylinder(GL2 gl2,float thicknessY,float radiusXZ) {
		int i;
		int c=36;
		
		// left
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glNormal3f(0,1,0);
		for(i=0;i<=c;++i) {
			float ratio= (float)Math.PI * 2.0f * (float)i/(float)c;
			gl2.glVertex3d((float)Math.sin(ratio)*radiusXZ,
							thicknessY,
							(float)Math.cos(ratio)*radiusXZ);
		}
		gl2.glEnd();
		// right
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glNormal3f(0,-1,0);
		for(i=0;i<=c;++i) {
			float ratio= (float)Math.PI * 2.0f * (float)i/(float)c;
			gl2.glVertex3d((float)Math.cos(ratio)*radiusXZ,
							-thicknessY,
							(float)Math.sin(ratio)*radiusXZ);
		}
		gl2.glEnd();

		// edge
		gl2.glBegin(GL2.GL_TRIANGLE_STRIP);
		for(i=0;i<=c;++i) {
			float ratio= (float)Math.PI * 2.0f * (float)i/(float)c;
			float a=(float)Math.sin(ratio)*radiusXZ;
			float b=thicknessY;
			float d=(float)Math.cos(ratio)*radiusXZ;
			gl2.glNormal3f(a,0,d);
			gl2.glVertex3d(a,b,d);
			gl2.glVertex3d(a,-b,d);
		}
		gl2.glEnd();
	}
	
	/**
	 * draw box based on depth,width, and height with the origin in the bottom center.
	 * @param gl2
	 * @param depth
	 * @param width
	 * @param height
	 */
	static public void drawBox(GL2 gl2,double depth,double width,double height) {
		width/=2;
		depth/=2;

		gl2.glPushMatrix();
		gl2.glBegin(GL2.GL_QUADS);
		// bottom
		gl2.glNormal3f( 0, 0,-1);
		gl2.glVertex3d(-width, depth,0);
		gl2.glVertex3d( width, depth,0);
		gl2.glVertex3d( width,-depth,0);
		gl2.glVertex3d(-width,-depth,0);

		// top
		gl2.glNormal3f( 0, 0, 1);
		gl2.glVertex3d( width, depth,height);
		gl2.glVertex3d(-width, depth,height);
		gl2.glVertex3d(-width,-depth,height);
		gl2.glVertex3d( width,-depth,height);

		
		// side
		gl2.glNormal3f( 0, 1, 0);
		gl2.glVertex3d(-width, depth,height);
		gl2.glVertex3d( width, depth,height);
		gl2.glVertex3d( width, depth,0);
		gl2.glVertex3d(-width, depth,0);
		
		gl2.glNormal3f( 0,-1, 0);
		gl2.glVertex3d( width,-depth,height);
		gl2.glVertex3d(-width,-depth,height);
		gl2.glVertex3d(-width,-depth,0);
		gl2.glVertex3d( width,-depth,0);

		gl2.glNormal3f( 1, 0, 0);
		gl2.glVertex3d( width, depth,0);
		gl2.glVertex3d( width, depth,height);
		gl2.glVertex3d( width,-depth,height);
		gl2.glVertex3d( width,-depth,0);
	
		gl2.glNormal3f(-1, 0, 0);
		gl2.glVertex3d(-width,-depth,height);
		gl2.glVertex3d(-width, depth,height);
		gl2.glVertex3d(-width, depth,0);
		gl2.glVertex3d(-width,-depth,0);

		gl2.glEnd();
		
		gl2.glPopMatrix();
	}

	/**
	 * draw box based on two corners
	 * @param gl2
	 * @param bottom minimum bounds
	 * @param top maximum bounds
	 */
	static public void drawBox(GL2 gl2,Tuple3d bottom,Tuple3d top) {
		double x0=bottom.x;
		double y0=bottom.y;
		double z0=bottom.z;
		double x1=top.x;
		double y1=top.y;
		double z1=top.z;

		gl2.glBegin(GL2.GL_QUADS);
			gl2.glNormal3f( 0, 0,-1);	gl2.glVertex3d(x0,y1,z0);	gl2.glVertex3d(x1,y1,z0);	gl2.glVertex3d(x1,y0,z0);	gl2.glVertex3d(x0,y0,z0);  // bottom
			gl2.glNormal3f( 0, 0, 1);	gl2.glVertex3d(x1,y1,z1);	gl2.glVertex3d(x0,y1,z1);	gl2.glVertex3d(x0,y0,z1);	gl2.glVertex3d(x1,y0,z1);  // top
			gl2.glNormal3f( 0, 1, 0);	gl2.glVertex3d(x0,y1,z1);	gl2.glVertex3d(x1,y1,z1);	gl2.glVertex3d(x1,y1,z0);	gl2.glVertex3d(x0,y1,z0);  // side
			gl2.glNormal3f( 0,-1, 0);	gl2.glVertex3d(x1,y0,z1);	gl2.glVertex3d(x0,y0,z1);	gl2.glVertex3d(x0,y0,z0);	gl2.glVertex3d(x1,y0,z0);
			gl2.glNormal3f( 1, 0, 0);	gl2.glVertex3d(x1,y1,z0);	gl2.glVertex3d(x1,y1,z1);	gl2.glVertex3d(x1,y0,z1);	gl2.glVertex3d(x1,y0,z0);
			gl2.glNormal3f(-1, 0, 0);	gl2.glVertex3d(x0,y0,z1);	gl2.glVertex3d(x0,y1,z1);	gl2.glVertex3d(x0,y1,z0);	gl2.glVertex3d(x0,y0,z0);
		gl2.glEnd();
	}

	public static Point3d[] get8PointsOfBox(Point3d bottom, Point3d top) {
		double x0=bottom.x;
		double y0=bottom.y;
		double z0=bottom.z;
		double x1=top.x;
		double y1=top.y;
		double z1=top.z;
		
		return new Point3d[] {
			new Point3d(x0,y1,z0),
			new Point3d(x0,y1,z1),
			new Point3d(x0,y0,z0),
			new Point3d(x0,y0,z1),
			new Point3d(x1,y1,z1),
			new Point3d(x1,y1,z0),
			new Point3d(x1,y0,z1),
			new Point3d(x1,y0,z0)
		};
	}

	/**
	 * draw box based on two corners
	 * @param gl2
	 * @param bottom minimum bounds
	 * @param top maximum bounds
	 */
	static public void drawBoxWireframe(GL2 gl2,Tuple3d bottom,Tuple3d top) {
		gl2.glDisable(GL2.GL_TEXTURE_2D);
		boolean lightWasOn = OpenGLHelper.disableLightingStart(gl2);
		
		double x0=bottom.x;
		double y0=bottom.y;
		double z0=bottom.z;
		double x1=top.x;
		double y1=top.y;
		double z1=top.z;

		gl2.glBegin(GL2.GL_LINE_LOOP);	gl2.glNormal3f( 0, 0,-1);	gl2.glVertex3d(x0,y1,z0);	gl2.glVertex3d(x1,y1,z0);	gl2.glVertex3d(x1,y0,z0);	gl2.glVertex3d(x0,y0,z0);	gl2.glEnd();  // bottom	
		gl2.glBegin(GL2.GL_LINE_LOOP);	gl2.glNormal3f( 0, 0, 1);	gl2.glVertex3d(x1,y1,z1);	gl2.glVertex3d(x0,y1,z1);	gl2.glVertex3d(x0,y0,z1);	gl2.glVertex3d(x1,y0,z1);	gl2.glEnd();  // top
		gl2.glBegin(GL2.GL_LINE_LOOP);	gl2.glNormal3f( 0, 1, 0);	gl2.glVertex3d(x0,y1,z1);	gl2.glVertex3d(x1,y1,z1);	gl2.glVertex3d(x1,y1,z0);	gl2.glVertex3d(x0,y1,z0);	gl2.glEnd();  // side
		gl2.glBegin(GL2.GL_LINE_LOOP);	gl2.glNormal3f( 0,-1, 0);	gl2.glVertex3d(x1,y0,z1);	gl2.glVertex3d(x0,y0,z1);	gl2.glVertex3d(x0,y0,z0);	gl2.glVertex3d(x1,y0,z0);	gl2.glEnd();
		gl2.glBegin(GL2.GL_LINE_LOOP);	gl2.glNormal3f( 1, 0, 0);	gl2.glVertex3d(x1,y1,z0);	gl2.glVertex3d(x1,y1,z1);	gl2.glVertex3d(x1,y0,z1);	gl2.glVertex3d(x1,y0,z0);	gl2.glEnd();
		gl2.glBegin(GL2.GL_LINE_LOOP);	gl2.glNormal3f(-1, 0, 0);	gl2.glVertex3d(x0,y0,z1);	gl2.glVertex3d(x0,y1,z1);	gl2.glVertex3d(x0,y1,z0);	gl2.glVertex3d(x0,y0,z0);	gl2.glEnd();

		OpenGLHelper.disableLightingEnd(gl2,lightWasOn);
	}
	
	static public void drawStar(GL2 gl2,double size) {
		drawStar(gl2,new Vector3d(0,0,0),size);
	}
	
	static public void drawStar(GL2 gl2,Tuple3d p) {
		drawStar(gl2,p,1.0f);
	}
	
	static public void drawStar(GL2 gl2,Tuple3d p,double size) {
		// save the current color
		double [] params = new double[4];
		gl2.glGetDoublev(GL2.GL_CURRENT_COLOR, params, 0);
		
		boolean lightWasOn = OpenGLHelper.disableLightingStart(gl2);
		//int depth = OpenGLHelper.drawAtopEverythingStart(gl2);

		size/=2.0f;
		
		gl2.glPushMatrix();
		gl2.glTranslated(p.x, p.y, p.z);
		gl2.glBegin(GL2.GL_LINES);
		gl2.glColor3d(1, 0, 0);		gl2.glVertex3d(0, 0, 0);		gl2.glVertex3d(size, 0, 0);
		gl2.glColor3d(0, 1, 0);		gl2.glVertex3d(0, 0, 0);		gl2.glVertex3d(0, size, 0);
		gl2.glColor3d(0, 0, 1);		gl2.glVertex3d(0, 0, 0);		gl2.glVertex3d(0, 0, size);
		gl2.glEnd();
		gl2.glPopMatrix();

		//OpenGLHelper.drawAtopEverythingEnd(gl2,depth);
		OpenGLHelper.disableLightingEnd(gl2,lightWasOn);
		
		// restore color
		gl2.glColor4dv(params,0);
	}	

	public static void drawSphere(GL2 gl2, double radius, Vector3d p) {
		gl2.glPushMatrix();
		gl2.glTranslated(p.x,p.y,p.z);
		drawSphere(gl2,radius);
		gl2.glPopMatrix();
	}
	
	/** draw square billboard facing the camera.
	 * @param gl2 render context
	 * @param p center of billboard
	 * @param c camera
	 * @param w width of square
	 * @param h height of square
	 */
	public static void drawBillboard(GL2 gl2, Tuple3d p,double w,double h) {
		Matrix4d m = OpenGLHelper.getModelviewMatrix(gl2);
		Vector3d up = MatrixHelper.getYAxis(m);
		Vector3d left = MatrixHelper.getXAxis(m);
		//Vector3d forward = MatrixHelper.getZAxis(m);
		up.scale(h);
		left.scale(w);
		Vector3d a0 = new Vector3d();
		Vector3d a1 = new Vector3d();
		Vector3d a2 = new Vector3d();
		Vector3d a3 = new Vector3d();
		a0.set(p);		a0.add(up);		a0.sub(left);
		a1.set(p);		a1.add(up);		a1.add(left);
		a2.set(p);		a2.sub(up);		a2.add(left);
		a3.set(p);		a3.sub(up);		a3.sub(left);

		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		//gl2.glColor3d(0, 0, 1);
		gl2.glTexCoord2d(0, 1);		gl2.glVertex3d(a3.x,a3.y,a3.z);
		//gl2.glColor3d(0, 1, 0);
		gl2.glTexCoord2d(1, 1);		gl2.glVertex3d(a2.x,a2.y,a2.z);
		//gl2.glColor3d(1, 0, 0);
		gl2.glTexCoord2d(1, 0);		gl2.glVertex3d(a1.x,a1.y,a1.z);
		//gl2.glColor3d(1, 1, 1);
		gl2.glTexCoord2d(0, 0);		gl2.glVertex3d(a0.x,a0.y,a0.z);
		gl2.glEnd();
	}
}
