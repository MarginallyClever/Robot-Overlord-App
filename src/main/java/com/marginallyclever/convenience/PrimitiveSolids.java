package com.marginallyclever.convenience;
import javax.vecmath.Vector3f;
import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.Cylinder;


public class PrimitiveSolids {
	static public void drawCylinder(GL2 gl2,Cylinder tube) {
		/*
		gl2.glBegin(GL2.GL_LINES);
		gl2.glVertex3f(tube.GetP1().x, tube.GetP1().y, tube.GetP1().z);
		gl2.glVertex3f(tube.GetP2().x, tube.GetP2().y, tube.GetP2().z);
		gl2.glEnd();
		*/

		Vector3f tx = new Vector3f();
		Vector3f ty = new Vector3f();
		Vector3f t1 = new Vector3f();
		Vector3f t2 = new Vector3f();
		Vector3f n = new Vector3f();
		
		int i;
		int c=10;
		
		// left
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glNormal3f(-tube.GetN().x,-tube.GetN().y,-tube.GetN().z);
		for(i=0;i<=c;++i) {
			tx.set(tube.GetR());
			ty.set(tube.GetF());

			float ratio= (float)Math.PI * 2.0f * (float)i/(float)c;
			tx.scale((float)Math.sin(ratio)*tube.getRadius());
			ty.scale((float)Math.cos(ratio)*tube.getRadius());
			t1.set(tube.GetP1());
			t1.add(tx);
			t1.add(ty);
			gl2.glVertex3f(t1.x,t1.y,t1.z);
		}
		gl2.glEnd();
		// right
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glNormal3f(tube.GetN().x,tube.GetN().y,tube.GetN().z);
		for(i=0;i<=c;++i) {
			tx.set(tube.GetR());
			ty.set(tube.GetF());

			float ratio= (float)Math.PI * 2.0f * (float)i/(float)c;
			tx.scale((float)Math.sin(ratio)*tube.getRadius());
			ty.scale((float)Math.cos(ratio)*tube.getRadius());
			t1.set(tube.GetP2());
			t1.add(tx);
			t1.add(ty);
			gl2.glVertex3f(t1.x,t1.y,t1.z);
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
			gl2.glNormal3f(n.x,n.y,n.z);
			t2.add(tube.GetP2());
			gl2.glVertex3f(t1.x,t1.y,t1.z);
			gl2.glVertex3f(t2.x,t2.y,t2.z);
			
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
			gl2.glVertex3f((float)Math.sin(ratio)*radius,
							thickness,
							(float)Math.cos(ratio)*radius);
		}
		gl2.glEnd();
		// right
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glNormal3f(0,-1,0);
		for(i=0;i<=c;++i) {
			float ratio= (float)Math.PI * 2.0f * (float)i/(float)c;
			gl2.glVertex3f((float)Math.cos(ratio)*radius,
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
			gl2.glVertex3f(a,b,d);
			gl2.glVertex3f(a,-b,d);
		}
		gl2.glEnd();
	}
	
	static public void drawBox(GL2 gl2,float depth,float width,float height) {
		width/=2;
		depth/=2;

		gl2.glPushMatrix();
		gl2.glBegin(GL2.GL_QUADS);
		// bottom
		gl2.glNormal3f( 0, 0,-1);
		gl2.glVertex3f(-width, depth,0);
		gl2.glVertex3f( width, depth,0);
		gl2.glVertex3f( width,-depth,0);
		gl2.glVertex3f(-width,-depth,0);

		// top
		gl2.glNormal3f( 0, 0, 1);
		gl2.glVertex3f( width, depth,height);
		gl2.glVertex3f(-width, depth,height);
		gl2.glVertex3f(-width,-depth,height);
		gl2.glVertex3f( width,-depth,height);

		
		// side
		gl2.glNormal3f( 0, 1, 0);
		gl2.glVertex3f(-width, depth,height);
		gl2.glVertex3f( width, depth,height);
		gl2.glVertex3f( width, depth,0);
		gl2.glVertex3f(-width, depth,0);
		
		gl2.glNormal3f( 0,-1, 0);
		gl2.glVertex3f( width,-depth,height);
		gl2.glVertex3f(-width,-depth,height);
		gl2.glVertex3f(-width,-depth,0);
		gl2.glVertex3f( width,-depth,0);

		gl2.glNormal3f( 1, 0, 0);
		gl2.glVertex3f( width, depth,0);
		gl2.glVertex3f( width, depth,height);
		gl2.glVertex3f( width,-depth,height);
		gl2.glVertex3f( width,-depth,0);
	
		gl2.glNormal3f(-1, 0, 0);
		gl2.glVertex3f(-width,-depth,height);
		gl2.glVertex3f(-width, depth,height);
		gl2.glVertex3f(-width, depth,0);
		gl2.glVertex3f(-width,-depth,0);

		gl2.glEnd();
		
		gl2.glPopMatrix();
	}
	

	static public void drawStar(GL2 gl2,Vector3f p) {
		drawStar(gl2,p,1.0f);
	}
	
	
	static public void drawStar(GL2 gl2,Vector3f p,float size) {
		// save the current color
		double [] params = new double[4];
		gl2.glGetDoublev(GL2.GL_CURRENT_COLOR, params, 0);
		
		// save the lighting mode
		byte [] data = new byte[1];
		gl2.glGetBooleanv(GL2.GL_LIGHTING, data, 0);
		gl2.glDisable(GL2.GL_LIGHTING);

		size/=2.0f;
		
		gl2.glPushMatrix();
		gl2.glTranslatef(p.x, p.y, p.z);
		gl2.glBegin(GL2.GL_LINES);
		gl2.glColor3d(1, 0, 0);		gl2.glVertex3f(-size, 0, 0);		gl2.glVertex3f(size, 0, 0);
		gl2.glColor3d(0, 1, 0);		gl2.glVertex3f(0, -size, 0);		gl2.glVertex3f(0, size, 0);
		gl2.glColor3d(0, 0, 1);		gl2.glVertex3f(0, 0, -size);		gl2.glVertex3f(0, 0, size);
		gl2.glEnd();
		gl2.glPopMatrix();
		
		// restore lighting
		if(data[0]!=0) gl2.glEnable(GL2.GL_LIGHTING);
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
		gl2.glNormal3f(0,0,0);

	    gl2.glEnable (GL2.GL_BLEND);
	    gl2.glBlendFunc (GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		gl2.glBegin(GL2.GL_LINES);
		
		for(int i=-grid_size;i<=grid_size;i+=grid_space) {
				float f = 0.5f-((float)Math.abs(i)/(float)(grid_size))*0.5f;
				gl2.glColor4f(0.2f,0.2f,0.2f,0f);	gl2.glVertex2f(i,-grid_size);
				gl2.glColor4f(0.2f,0.2f,0.2f,f);	gl2.glVertex2f(i, 0        );
				gl2.glColor4f(0.2f,0.2f,0.2f,f);	gl2.glVertex2f(i, 0        );
				gl2.glColor4f(0.2f,0.2f,0.2f,0f);	gl2.glVertex2f(i, grid_size);

				gl2.glColor4f(0.2f,0.2f,0.2f,0f);	gl2.glVertex2f(-grid_size,i);
				gl2.glColor4f(0.2f,0.2f,0.2f,f);	gl2.glVertex2f( 0        ,i);
				gl2.glColor4f(0.2f,0.2f,0.2f,f);	gl2.glVertex2f( 0        ,i);
				gl2.glColor4f(0.2f,0.2f,0.2f,0f);	gl2.glVertex2f( grid_size,i);
			//}
		}
		gl2.glEnd();
	}
	
	
	static public void drawGrid(GL2 gl2) {
		drawGrid(gl2,50,1);
	}
}
