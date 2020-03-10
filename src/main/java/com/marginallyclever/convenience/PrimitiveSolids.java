package com.marginallyclever.convenience;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import com.jogamp.opengl.GL2;


public class PrimitiveSolids {
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
	
	static public void drawCylinder(GL2 gl2,Cylinder tube) {
		/*
		gl2.glBegin(GL2.GL_LINES);
		gl2.glVertex3d(tube.GetP1().x, tube.GetP1().y, tube.GetP1().z);
		gl2.glVertex3d(tube.GetP2().x, tube.GetP2().y, tube.GetP2().z);
		gl2.glEnd();
		*/

		Vector3d tx = new Vector3d();
		Vector3d ty = new Vector3d();
		Vector3d t1 = new Vector3d();
		Vector3d t2 = new Vector3d();
		Vector3d n = new Vector3d();
		
		int i;
		int c=10;
		
		// left
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glNormal3d(-tube.GetN().x,-tube.GetN().y,-tube.GetN().z);
		for(i=0;i<=c;++i) {
			tx.set(tube.GetR());
			ty.set(tube.GetF());

			float ratio= (float)Math.PI * 2.0f * (float)i/(float)c;
			tx.scale((float)Math.sin(ratio)*tube.getRadius());
			ty.scale((float)Math.cos(ratio)*tube.getRadius());
			t1.set(tube.GetP1());
			t1.add(tx);
			t1.add(ty);
			gl2.glVertex3d(t1.x,t1.y,t1.z);
		}
		gl2.glEnd();
		// right
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glNormal3d(tube.GetN().x,tube.GetN().y,tube.GetN().z);
		for(i=0;i<=c;++i) {
			tx.set(tube.GetR());
			ty.set(tube.GetF());

			float ratio= (float)Math.PI * 2.0f * (float)i/(float)c;
			tx.scale((float)Math.sin(ratio)*tube.getRadius());
			ty.scale((float)Math.cos(ratio)*tube.getRadius());
			t1.set(tube.GetP2());
			t1.add(tx);
			t1.add(ty);
			gl2.glVertex3d(t1.x,t1.y,t1.z);
		}
		gl2.glEnd();

		// edge
		gl2.glBegin(GL2.GL_TRIANGLE_STRIP);
		for(i=0;i<=c;++i) {
			tx.set(tube.GetR());
			ty.set(tube.GetF());

			float ratio= (float)Math.PI * 2.0f * (float)i/(float)c;
			tx.scale((float)Math.sin(ratio)*tube.getRadius());
			ty.scale((float)Math.cos(ratio)*tube.getRadius());
			t1.set(tube.GetP1());
			t1.add(tx);
			t1.add(ty);
			
			t2.set(tx);
			t2.add(ty);
			n.set(t2);
			n.normalize();
			gl2.glNormal3d(n.x,n.y,n.z);
			t2.add(tube.GetP2());
			gl2.glVertex3d(t1.x,t1.y,t1.z);
			gl2.glVertex3d(t2.x,t2.y,t2.z);
			
		}
		gl2.glEnd();
	}

	// TODO: move this to Cylinder?
	static public void drawCylinder(GL2 gl2,float thickness,float radius) {
		int i;
		int c=36;
		
		// left
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glNormal3f(0,1,0);
		for(i=0;i<=c;++i) {
			float ratio= (float)Math.PI * 2.0f * (float)i/(float)c;
			gl2.glVertex3d((float)Math.sin(ratio)*radius,
							thickness,
							(float)Math.cos(ratio)*radius);
		}
		gl2.glEnd();
		// right
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glNormal3f(0,-1,0);
		for(i=0;i<=c;++i) {
			float ratio= (float)Math.PI * 2.0f * (float)i/(float)c;
			gl2.glVertex3d((float)Math.cos(ratio)*radius,
							-thickness,
							(float)Math.sin(ratio)*radius);
		}
		gl2.glEnd();

		// edge
		gl2.glBegin(GL2.GL_TRIANGLE_STRIP);
		for(i=0;i<=c;++i) {
			float ratio= (float)Math.PI * 2.0f * (float)i/(float)c;
			float a=(float)Math.sin(ratio)*radius;
			float b=thickness;
			float d=(float)Math.cos(ratio)*radius;
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
	static public void drawBox(GL2 gl2,float depth,float width,float height) {
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
	static public void drawBox(GL2 gl2,Point3d bottom,Point3d top) {
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

	/**
	 * draw box based on two corners
	 * @param gl2
	 * @param bottom minimum bounds
	 * @param top maximum bounds
	 */
	static public void drawBoxWireframe(GL2 gl2,Point3d bottom,Point3d top) {
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
	}
	

	static public void drawStar(GL2 gl2,Vector3d p) {
		drawStar(gl2,p,1.0f);
	}
	
	
	static public void drawStar(GL2 gl2,Vector3d p,double size) {
		// save the current color
		double [] params = new double[4];
		gl2.glGetDoublev(GL2.GL_CURRENT_COLOR, params, 0);
		
		// save the lighting mode
		boolean lightWasOn = gl2.glIsEnabled(GL2.GL_LIGHTING);
		gl2.glDisable(GL2.GL_LIGHTING);

		IntBuffer depthFunc = IntBuffer.allocate(1);
		gl2.glGetIntegerv(GL2.GL_DEPTH_FUNC, depthFunc);
		gl2.glDepthFunc(GL2.GL_ALWAYS);
		//boolean depthWasOn = gl2.glIsEnabled(GL2.GL_DEPTH_TEST);
		//gl2.glDisable(GL2.GL_DEPTH_TEST);

		size/=2.0f;
		
		gl2.glPushMatrix();
		gl2.glTranslated(p.x, p.y, p.z);
		gl2.glBegin(GL2.GL_LINES);
		gl2.glColor3d(1, 0, 0);		gl2.glVertex3d(0, 0, 0);		gl2.glVertex3d(size, 0, 0);
		gl2.glColor3d(0, 1, 0);		gl2.glVertex3d(0, 0, 0);		gl2.glVertex3d(0, size, 0);
		gl2.glColor3d(0, 0, 1);		gl2.glVertex3d(0, 0, 0);		gl2.glVertex3d(0, 0, size);
		gl2.glEnd();
		gl2.glPopMatrix();

		//if(depthWasOn) gl2.glEnable(GL2.GL_DEPTH_TEST);
		gl2.glDepthFunc(depthFunc.get());
		
		// restore lighting
		if(lightWasOn) gl2.glEnable(GL2.GL_LIGHTING);
		
		
		// restore color
		gl2.glColor4dv(params,0);
	}

	
	/**
	 * Draw a gride of lines in the current color
	 * @param gl2 the render context
	 * @param grid_size the dimensions of the grid, from -grid_size to grid_size.
	 * @param grid_space the distance between lines on the grid.
	 */
	static public void drawGrid(GL2 gl2,int grid_size,int grid_space) {
		drawGrid(gl2,grid_size,grid_size,grid_space);
	}

	
	/**
	 * Draw a grid of lines in the current color
	 * @param gl2 the render context
	 * @param gridWidth the dimensions of the grid
	 * @param gridHeight the dimensions of the grid
	 * @param grid_space the distance between lines on the grid.
	 */
	static public void drawGrid(GL2 gl2,int gridWidth,int gridHeight,int grid_space) {
		gl2.glNormal3f(0,0,1);

		//boolean isBlend = gl2.glIsEnabled(GL2.GL_BLEND);
	    //gl2.glEnable(GL2.GL_BLEND);
	    //gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		gl2.glBegin(GL2.GL_LINES);
		gridWidth/=2;
		gridHeight/=2;
		
		// float start=0;
		float start=1;
		float end=1;
		for(int i=-gridWidth;i<=gridWidth;i+=grid_space) {
			//end = 0.5f-((float)Math.abs(i)/(float)(gridHeight))*0.5f;
			gl2.glColor4f(0.2f,0.2f,0.2f,start);	gl2.glVertex2f(i,-gridHeight);
			gl2.glColor4f(0.2f,0.2f,0.2f,end  );	gl2.glVertex2f(i, 0         );
			gl2.glColor4f(0.2f,0.2f,0.2f,end  );	gl2.glVertex2f(i, 0         );
			gl2.glColor4f(0.2f,0.2f,0.2f,start);	gl2.glVertex2f(i, gridHeight);
		}
		for(int i=-gridHeight;i<=gridHeight;i+=grid_space) {
			gl2.glColor4f(0.2f,0.2f,0.2f,start);	gl2.glVertex2f(-gridWidth,i);
			gl2.glColor4f(0.2f,0.2f,0.2f,end  );	gl2.glVertex2f( 0        ,i);
			gl2.glColor4f(0.2f,0.2f,0.2f,end  );	gl2.glVertex2f( 0        ,i);
			gl2.glColor4f(0.2f,0.2f,0.2f,start);	gl2.glVertex2f( gridWidth,i);
		}
		gl2.glEnd();
		
		//if(!isBlend) gl2.glDisable(GL2.GL_BLEND);
	}
	
	
	static public void drawGrid(GL2 gl2) {
		drawGrid(gl2,50,1);
	}
}
