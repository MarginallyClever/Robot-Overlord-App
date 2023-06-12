package com.marginallyclever.robotoverlord.systems.render;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.OpenGLHelper;
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

	public void render(GL3 gl,CameraComponent camera,ShaderProgram program) {
		PoseComponent cameraPose = camera.getEntity().getComponent(PoseComponent.class);
/*
		program.set1i(gl,"useTexture",1);
		program.set1i(gl,"useLighting",0);
		program.set1i(gl,"useVertexColor",0);

		Matrix4d m1 = cameraPose.getWorld();
		m1.transpose();
		program.setMatrix4d(gl,"modelMatrix",m1);
*/
		gl.glUseProgram(0);
		boolean lit = OpenGLHelper.disableLightingStart(gl);

		gl.glPushMatrix();
		Matrix4d m = cameraPose.getWorld();
		m.setTranslation(new Vector3d(0,0,0));
		gl.glLoadIdentity();
		m.invert();
		MatrixHelper.applyMatrix(gl, m);


		gl.glColor3f(1, 1, 1);

		textureXPos.render(gl);
		gl.glBegin(GL3.GL_TRIANGLE_FAN);
			gl.glTexCoord2d(0,1);  gl.glVertex3d(10, 10, 10);
			gl.glTexCoord2d(0,0);  gl.glVertex3d(10, 10, -10);
			gl.glTexCoord2d(1,0);  gl.glVertex3d(10, -10, -10);
			gl.glTexCoord2d(1,1);  gl.glVertex3d(10, -10, 10);
		gl.glEnd();

		textureXNeg.render(gl);
		gl.glBegin(GL3.GL_TRIANGLE_FAN);
			gl.glTexCoord2d(0,1);  gl.glVertex3d(-10, -10, 10);
			gl.glTexCoord2d(0,0);  gl.glVertex3d(-10, -10, -10);
			gl.glTexCoord2d(1,0);  gl.glVertex3d(-10, 10, -10);
			gl.glTexCoord2d(1,1);  gl.glVertex3d(-10, 10, 10);
		gl.glEnd();

		textureYPos.render(gl);
		gl.glBegin(GL3.GL_TRIANGLE_FAN);
			gl.glTexCoord2d(0,1);  gl.glVertex3d(-10, 10, 10);
			gl.glTexCoord2d(0,0);  gl.glVertex3d(-10, 10, -10);
			gl.glTexCoord2d(1,0);  gl.glVertex3d(10, 10, -10);
			gl.glTexCoord2d(1,1);  gl.glVertex3d(10, 10, 10);
		gl.glEnd();

		textureYNeg.render(gl);
		gl.glBegin(GL3.GL_TRIANGLE_FAN);
			gl.glTexCoord2d(0,1);  gl.glVertex3d(10, -10, 10);
			gl.glTexCoord2d(0,0);  gl.glVertex3d(10, -10, -10);
			gl.glTexCoord2d(1,0);  gl.glVertex3d(-10, -10, -10);
			gl.glTexCoord2d(1,1);  gl.glVertex3d(-10, -10, 10);
		gl.glEnd();

		textureZPos.render(gl);
		gl.glBegin(GL3.GL_TRIANGLE_FAN);
			gl.glTexCoord2d(0,0);  gl.glVertex3d(-10, 10, 10);
			gl.glTexCoord2d(1,0);  gl.glVertex3d( 10, 10, 10);
			gl.glTexCoord2d(1,1);  gl.glVertex3d( 10,-10, 10);
			gl.glTexCoord2d(0,1);  gl.glVertex3d(-10,-10, 10);
		gl.glEnd();

		textureZNeg.render(gl);
		gl.glBegin(GL3.GL_TRIANGLE_FAN);
			gl.glTexCoord2d(0,0);  gl.glVertex3d(-10,-10, -10);
			gl.glTexCoord2d(1,0);  gl.glVertex3d( 10,-10, -10);
			gl.glTexCoord2d(1,1);  gl.glVertex3d( 10, 10, -10);
			gl.glTexCoord2d(0,1);  gl.glVertex3d(-10, 10, -10);
		gl.glEnd();

		gl.glPopMatrix();
		// Clear the depth buffer
        gl.glClear(GL3.GL_DEPTH_BUFFER_BIT);

		program.use(gl);
		OpenGLHelper.disableLightingEnd(gl,lit);
	}
}
