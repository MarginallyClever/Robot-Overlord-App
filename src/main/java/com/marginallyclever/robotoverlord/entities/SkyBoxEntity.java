package com.marginallyclever.robotoverlord.entities;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import com.marginallyclever.robotoverlord.uiexposedtypes.TextureEntity;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.io.Serial;

public class SkyBoxEntity extends Entity {
	@Serial
	private static final long serialVersionUID = 7218495889495845836L;
	protected transient boolean areSkyboxTexturesLoaded=false;
	protected transient final TextureEntity skyboxTextureZPos = new TextureEntity("/skybox/cube-z-pos.png");
	protected transient final TextureEntity skyboxTextureXPos = new TextureEntity("/skybox/cube-x-pos.png");
	protected transient final TextureEntity skyboxTextureXNeg = new TextureEntity("/skybox/cube-x-neg.png");
	protected transient final TextureEntity skyboxTextureYPos = new TextureEntity("/skybox/cube-y-pos.png");
	protected transient final TextureEntity skyboxTextureYNeg = new TextureEntity("/skybox/cube-y-neg.png");
	protected transient final TextureEntity skyboxTextureZNeg = new TextureEntity("/skybox/cube-z-neg.png");

	public SkyBoxEntity() {
		super();
		setName("Skybox");
		
		skyboxTextureXPos.setName("XPos");
		skyboxTextureXNeg.setName("XNeg");
		skyboxTextureYPos.setName("YPos");
		skyboxTextureYNeg.setName("YNeg");
		skyboxTextureZPos.setName("ZPos");
		skyboxTextureZNeg.setName("ZNeg");
	}

	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Skybox",true);
		view.add(skyboxTextureXPos);
		view.add(skyboxTextureXNeg);
		view.add(skyboxTextureYPos);
		view.add(skyboxTextureYNeg);
		view.add(skyboxTextureZPos);
		view.add(skyboxTextureZNeg);
		view.popStack();
		super.getView(view);
	}

	// Draw background
	@Override
	public void render(GL2 gl2) {
		RobotOverlord ro =(RobotOverlord)this.getRoot();
		CameraComponent camera = ro.getCamera();
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

			skyboxTextureXPos.render(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(10, 10, 10);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(10, 10, -10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d(10, -10, -10);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d(10, -10, 10);
			gl2.glEnd();

			skyboxTextureXNeg.render(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(-10, -10, 10);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(-10, -10, -10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d(-10, 10, -10);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d(-10, 10, 10);
			gl2.glEnd();

			skyboxTextureYPos.render(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(-10, 10, 10);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(-10, 10, -10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d(10, 10, -10);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d(10, 10, 10);
			gl2.glEnd();

			skyboxTextureYNeg.render(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(10, -10, 10);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(10, -10, -10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d(-10, -10, -10);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d(-10, -10, 10);
			gl2.glEnd();

			skyboxTextureZPos.render(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(-10, 10, 10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d( 10, 10, 10);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d( 10,-10, 10);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(-10,-10, 10);
			gl2.glEnd();

			skyboxTextureZNeg.render(gl2);
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
