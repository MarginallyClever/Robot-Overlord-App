package com.marginallyclever.robotoverlord;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.parameters.TextureParameter;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

/**
 * A skybox is a cube with 6 textures on it.  The camera is inside the cube, looking out.
 * @author Dan Royer
 */
public class SkyBox {
	private transient final TextureParameter textureXPos = new TextureParameter("XPos","/skybox/cube-x-pos.png");
	private transient final TextureParameter textureXNeg = new TextureParameter("XNeg","/skybox/cube-x-neg.png");
	private transient final TextureParameter textureYPos = new TextureParameter("YPos","/skybox/cube-y-pos.png");
	private transient final TextureParameter textureYNeg = new TextureParameter("YNeg","/skybox/cube-y-neg.png");
	private transient final TextureParameter textureZPos = new TextureParameter("ZPos","/skybox/cube-z-pos.png");
	private transient final TextureParameter textureZNeg = new TextureParameter("ZNeg","/skybox/cube-z-neg.png");

	public SkyBox() {
		super();
	}

	public void render(GL2 gl2,CameraComponent camera) {
		PoseComponent pose = camera.getEntity().findFirstComponent(PoseComponent.class);

		gl2.glDisable(GL2.GL_LIGHTING);
		gl2.glDisable(GL2.GL_COLOR_MATERIAL);
		gl2.glEnable(GL2.GL_TEXTURE_2D);
		gl2.glPushMatrix();
			Matrix4d m = pose.getWorld();
			m.setTranslation(new Vector3d(0,0,0));
			gl2.glLoadIdentity();
			m.invert();
			MatrixHelper.applyMatrix(gl2, m);
		
			gl2.glColor3f(1, 1, 1);

			textureXPos.render(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(10, 10, 10);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(10, 10, -10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d(10, -10, -10);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d(10, -10, 10);
			gl2.glEnd();

			textureXNeg.render(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(-10, -10, 10);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(-10, -10, -10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d(-10, 10, -10);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d(-10, 10, 10);
			gl2.glEnd();

			textureYPos.render(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(-10, 10, 10);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(-10, 10, -10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d(10, 10, -10);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d(10, 10, 10);
			gl2.glEnd();

			textureYNeg.render(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(10, -10, 10);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(10, -10, -10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d(-10, -10, -10);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d(-10, -10, 10);
			gl2.glEnd();

			textureZPos.render(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(-10, 10, 10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d( 10, 10, 10);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d( 10,-10, 10);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(-10,-10, 10);
			gl2.glEnd();

			textureZNeg.render(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(-10,-10, -10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d( 10,-10, -10);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d( 10, 10, -10);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(-10, 10, -10);
			gl2.glEnd();
			
		gl2.glPopMatrix();

		// Clear the depth buffer
        gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT);
	}
}
