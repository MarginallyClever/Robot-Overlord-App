package com.marginallyclever.robotOverlord.swingInterface;


import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.primitives.PhysicalEntity;
import com.marginallyclever.robotOverlord.entity.primitives.TextureEntity;

public class ViewCubeEntity extends Entity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected transient TextureEntity tRight;
	protected transient TextureEntity tLeft;
	protected transient TextureEntity t2;
	protected transient TextureEntity t3;
	protected transient TextureEntity tTop;
	protected transient TextureEntity tBottom;
	protected transient TextureEntity tShadow;
	
    public ViewCubeEntity() {
    	super();
    	setName("ViewCube");
    	tRight = new TextureEntity("/images/cube-x-pos.png");
    	tLeft = new TextureEntity("/images/cube-x-neg.png");
    	t2 = new TextureEntity("/images/cube-y-pos.png");
    	t3 = new TextureEntity("/images/cube-y-neg.png");
    	tTop = new TextureEntity("/images/cube-z-pos.png");
    	tBottom = new TextureEntity("/images/cube-z-neg.png");
    	tShadow = new TextureEntity("/images/shadow.png");
    }
		
	public void render(GL2 gl2) {
		RobotOverlord ro = (RobotOverlord)getRoot();
		CameraViewEntity cameraView = ro.cameraView;
		
		gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT);

		gl2.glEnable(GL2.GL_DEPTH_TEST);
		gl2.glEnable(GL2.GL_CULL_FACE);
		
		gl2.glDisable(GL2.GL_LIGHTING);
		gl2.glDisable(GL2.GL_COLOR_MATERIAL);
		gl2.glColor4d(1,1,1,1);
		
		gl2.glBlendFunc(GL2.GL_SRC_ALPHA,GL2.GL_ONE_MINUS_SRC_ALPHA);
		
		gl2.glMatrixMode(GL2.GL_MODELVIEW);
		gl2.glPushMatrix();
			gl2.glLoadIdentity();
			cameraView.renderShared(gl2);
			
			PhysicalEntity camera = cameraView.getAttachedTo();
			Matrix4d m = camera.getPoseWorld();
			Vector3d p = camera.getPosition();
			Vector3d vx = MatrixHelper.getXAxis(m);
			Vector3d vy = MatrixHelper.getYAxis(m);
			Vector3d vz = MatrixHelper.getZAxis(m);
			double fovRadians = Math.toRadians(cameraView.fieldOfView.get());
			//System.out.println(Math.sin(fovRadians)+"\t"+Math.cos(fovRadians));
			double ar = cameraView.getAspectRatio();
			vz.scale((-cameraView.canvasHeight*2)*Math.cos(fovRadians));
			vx.scale((cameraView.canvasWidth/2-20)*ar);
			vy.scale((cameraView.canvasHeight/2-20));
			p.add(vx);
			p.add(vy);
			p.add(vz);
			
			gl2.glTranslated(p.x, p.y, p.z);
			gl2.glScaled(3, 3, 3);

			tRight.render(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
			gl2.glTexCoord2d(0,1);  gl2.glVertex3d(-10, 10, 10);
			gl2.glTexCoord2d(0,0);  gl2.glVertex3d(-10, 10, -10);
			gl2.glTexCoord2d(1,0);  gl2.glVertex3d(-10, -10, -10);
			gl2.glTexCoord2d(1,1);  gl2.glVertex3d(-10, -10, 10);
			gl2.glEnd();

			tLeft.render(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
			gl2.glTexCoord2d(0,1);  gl2.glVertex3d(10, -10, 10);
			gl2.glTexCoord2d(0,0);  gl2.glVertex3d(10, -10, -10);
			gl2.glTexCoord2d(1,0);  gl2.glVertex3d(10, 10, -10);
			gl2.glTexCoord2d(1,1);  gl2.glVertex3d(10, 10, 10);
			gl2.glEnd();

			t2.render(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
			gl2.glTexCoord2d(1,1);  gl2.glVertex3d(-10, 10, 10);
			gl2.glTexCoord2d(0,1);  gl2.glVertex3d(10, 10, 10);
			gl2.glTexCoord2d(0,0);  gl2.glVertex3d(10, 10, -10);
			gl2.glTexCoord2d(1,0);  gl2.glVertex3d(-10, 10, -10);
			gl2.glEnd();

			t3.render(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
			gl2.glTexCoord2d(1,1);  gl2.glVertex3d(10, -10, 10);
			gl2.glTexCoord2d(0,1);  gl2.glVertex3d(-10, -10, 10);
			gl2.glTexCoord2d(0,0);  gl2.glVertex3d(-10, -10, -10);
			gl2.glTexCoord2d(1,0);  gl2.glVertex3d(10, -10, -10);
			gl2.glEnd();

			tTop.render(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
			gl2.glTexCoord2d(0,0);  gl2.glVertex3d(-10,-10, 10);
			gl2.glTexCoord2d(1,0);  gl2.glVertex3d( 10,-10, 10);
			gl2.glTexCoord2d(1,1);  gl2.glVertex3d( 10, 10, 10);
			gl2.glTexCoord2d(0,1);  gl2.glVertex3d(-10, 10, 10);
			gl2.glEnd();

			tBottom.render(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
			gl2.glTexCoord2d(0,0);  gl2.glVertex3d(-10, 10, -10);
			gl2.glTexCoord2d(1,0);  gl2.glVertex3d( 10, 10, -10);
			gl2.glTexCoord2d(1,1);  gl2.glVertex3d( 10,-10, -10);
			gl2.glTexCoord2d(0,1);  gl2.glVertex3d(-10,-10, -10);
			gl2.glEnd();
			

			gl2.glDisable(GL2.GL_TEXTURE_2D);
			
			// draw the edges
			gl2.glColor4d(0,0,0,0.5);
			
			gl2.glBegin(GL2.GL_LINE_LOOP);
			gl2.glVertex3d(-10, 10, 10);
			gl2.glVertex3d(-10, -10, 10);
			gl2.glVertex3d(-10, -10, -10);
			gl2.glVertex3d(-10, 10, -10);
			gl2.glEnd();

			gl2.glBegin(GL2.GL_LINE_LOOP);
			gl2.glVertex3d(10, -10, 10);
			gl2.glVertex3d(10, 10, 10);
			gl2.glVertex3d(10, 10, -10);
			gl2.glVertex3d(10, -10, -10);
			gl2.glEnd();

			gl2.glBegin(GL2.GL_LINE_LOOP);
			gl2.glVertex3d(-10, 10, 10);
			gl2.glVertex3d(10, 10, 10);
			gl2.glVertex3d(10, 10, -10);
			gl2.glVertex3d(-10, 10, -10);
			gl2.glEnd();

			gl2.glBegin(GL2.GL_LINE_LOOP);
			gl2.glVertex3d(10, -10, 10);
			gl2.glVertex3d(-10, -10, 10);
			gl2.glVertex3d(-10, -10, -10);
			gl2.glVertex3d(10, -10, -10);
			gl2.glEnd();

			gl2.glBegin(GL2.GL_LINE_LOOP);
			gl2.glVertex3d(-10,-10, 10);
			gl2.glVertex3d(-10, 10, 10);
			gl2.glVertex3d( 10, 10, 10);
			gl2.glVertex3d( 10,-10, 10);
			gl2.glEnd();

			gl2.glBegin(GL2.GL_LINE_LOOP);
			gl2.glVertex3d(-10, 10, -10);
			gl2.glVertex3d( 10, 10, -10);
			gl2.glVertex3d( 10,-10, -10);
			gl2.glVertex3d(-10,-10, -10);
			gl2.glEnd();

			// the big lines
			gl2.glLineWidth(4);
			gl2.glPushMatrix();
				gl2.glTranslated(-10.4,-10.4,-10.4);
				gl2.glBegin(GL2.GL_LINES);
				gl2.glColor3d(1, 0, 0);		gl2.glVertex3d(0, 0, 0);		gl2.glVertex3d(25, 0, 0);
				gl2.glColor3d(0, 1, 0);		gl2.glVertex3d(0, 0, 0);		gl2.glVertex3d(0, 25, 0);
				gl2.glColor3d(0, 0, 1);		gl2.glVertex3d(0, 0, 0);		gl2.glVertex3d(0, 0, 25);
				gl2.glEnd();
			gl2.glPopMatrix();
			gl2.glLineWidth(1);
			
			tShadow.render(gl2);
			gl2.glColor4d(1,1,1,1);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
			gl2.glNormal3d(0,0,-1);
			gl2.glTexCoord2d(0,0);  gl2.glVertex3d(-15, 15, -10.01);
			gl2.glTexCoord2d(0,1);  gl2.glVertex3d(-15,-15, -10.01);
			gl2.glTexCoord2d(1,1);  gl2.glVertex3d( 15,-15, -10.01);
			gl2.glTexCoord2d(1,0);  gl2.glVertex3d( 15, 15, -10.01);
			gl2.glEnd();
			gl2.glDisable(GL2.GL_CULL_FACE);
			gl2.glCullFace(GL2.GL_NONE);

			gl2.glDisable(GL2.GL_TEXTURE_2D);
			
		gl2.glPopMatrix();
	}
}
