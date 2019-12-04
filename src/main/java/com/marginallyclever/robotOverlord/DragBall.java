package com.marginallyclever.robotOverlord;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.physicalObject.PhysicalObject;

public class DragBall extends PhysicalObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2233212276793302070L;
	
	
	public void render(GL2 gl2) {
		float scale=15;
		Vector3d v=new Vector3d();
		Vector3d v1=new Vector3d();
		Matrix4d cm=new Matrix4d(getWorld().getCamera().getMatrix());
		
		gl2.glDisable(GL2.GL_LIGHTING);
		
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, this.matrix);
			gl2.glScalef(scale, scale, scale);

			cm.invert();
			cm.mul(matrix);
			cm.setTranslation(new Vector3d(0,0,0));
/*
			//grey
			gl2.glColor3d(0.5,0.5,0.5);
			gl2.glBegin(GL2.GL_LINE_LOOP);
			for(double n=0;n<Math.PI*2;n+=Math.PI/20) {
				//v.set(0,);
				//cm.transform(v, v1);
				//if(v1.z>=0) {
					gl2.glVertex3d(Math.cos(n),Math.sin(n),0);
				//}
			}
			gl2.glEnd();
*/
			
			int inOutin;
			
			//x
			inOutin=0;
			gl2.glColor3d(1, 0, 0);
			for(double n=0;n<Math.PI*4;n+=Math.PI/20) {
				v.set(0,Math.cos(n),Math.sin(n));
				cm.transform(v, v1);
				if(v1.z<0 ) {
					if(inOutin==0) inOutin=1;
					if(inOutin==2) break;
				}
				if(v1.z>=0) {
					if(inOutin==1) {
						inOutin=2;
						gl2.glBegin(GL2.GL_LINE_STRIP);
					}
					if(inOutin==2) {
						gl2.glVertex3d(v.x,v.y,v.z);
					}
				}
			}
			gl2.glEnd();
			
			//y
			inOutin=0;
			gl2.glColor3d(0, 1, 0);
			for(double n=0;n<Math.PI*4;n+=Math.PI/20) {
				v.set(Math.cos(n), 0, Math.sin(n));
				cm.transform(v, v1);
				if(v1.z<0 ) {
					if(inOutin==0) inOutin=1;
					if(inOutin==2) break;
				}
				if(v1.z>=0) {
					if(inOutin==1) {
						inOutin=2;
						gl2.glBegin(GL2.GL_LINE_STRIP);
					}
					if(inOutin==2) {
						gl2.glVertex3d(v.x,v.y,v.z);
					}
				}
			}
			gl2.glEnd();
			
			//z
			inOutin=0;
			gl2.glColor3d(0, 0, 1);
			for(double n=0;n<Math.PI*4;n+=Math.PI/20) {
				v.set(Math.cos(n), Math.sin(n),0);
				cm.transform(v, v1);
				if(v1.z<0 ) {
					if(inOutin==0) inOutin=1;
					if(inOutin==2) break;
				}
				if(v1.z>=0) {
					if(inOutin==1) {
						inOutin=2;
						gl2.glBegin(GL2.GL_LINE_STRIP);
					}
					if(inOutin==2) {
						gl2.glVertex3d(v.x,v.y,v.z);
					}
				}
			}
			
			gl2.glEnd();
		gl2.glPopMatrix();
	}
}
