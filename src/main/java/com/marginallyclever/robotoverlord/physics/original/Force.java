package com.marginallyclever.robotoverlord.physics.original;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;

class Force {
	public Point3d p = new Point3d();
	public Vector3d f = new Vector3d();
	public double r, g, b;

	public Force(Point3d p0, Vector3d f0, double rr, double gg, double bb) {
		p.set(p0);
		f.set(f0);
		r=rr;
		g=gg;
		b=bb;
	}
	
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
			gl2.glBegin(GL2.GL_LINES);
			gl2.glColor3d(r,g,b);
			gl2.glVertex3d(p.x    ,p.y    ,p.z    );
			gl2.glVertex3d(p.x+f.x,p.y+f.y,p.z+f.z);
			gl2.glEnd();
		gl2.glPopMatrix();
	}
}