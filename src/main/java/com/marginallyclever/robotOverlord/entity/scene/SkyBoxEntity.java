package com.marginallyclever.robotOverlord.entity.scene;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.TextureEntity;

public class SkyBoxEntity extends Entity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7218495889495845836L;
	protected transient boolean areSkyboxTexturesLoaded=false;
	protected transient TextureEntity skyboxtextureZPos = new TextureEntity("/images/cube-x-pos.png");
	protected transient TextureEntity skyboxtextureXPos = new TextureEntity("/images/cube-x-neg.png");
	protected transient TextureEntity skyboxtextureXNeg = new TextureEntity("/images/cube-y-pos.png");
	protected transient TextureEntity skyboxtextureYPos = new TextureEntity("/images/cube-y-neg.png");
	protected transient TextureEntity skyboxtextureYNeg = new TextureEntity("/images/cube-z-pos.png");
	protected transient TextureEntity skyboxtextureZNeg = new TextureEntity("/images/cube-z-neg.png");

	public SkyBoxEntity() {
		super();
		setName("Skybox");
		
		skyboxtextureXPos.setName("XPos");
		skyboxtextureXNeg.setName("XNeg");
		skyboxtextureYPos.setName("YPos");
		skyboxtextureYNeg.setName("YNeg");
		skyboxtextureZPos.setName("ZPos");
		skyboxtextureZNeg.setName("ZNeg");
		
		addChild(skyboxtextureXPos);
		addChild(skyboxtextureXNeg);
		addChild(skyboxtextureYPos);
		addChild(skyboxtextureYNeg);
		addChild(skyboxtextureZPos);
		addChild(skyboxtextureZNeg);
	}
	
	// Draw background
	public void render(GL2 gl2,CameraEntity camera) {		
		//gl2.glDisable(GL2.GL_DEPTH_TEST);
		gl2.glDisable(GL2.GL_LIGHTING);
		gl2.glDisable(GL2.GL_COLOR_MATERIAL);
		gl2.glEnable(GL2.GL_TEXTURE_2D);
		gl2.glPushMatrix();
			gl2.glColor3f(1, 1, 1);
			Vector3d p = camera.getPosition();
			gl2.glTranslated(-p.x,-p.y,-p.z);

			skyboxtextureXPos.render(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(10, 10, 10);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d(10, -10, 10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d(10, -10, -10);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(10, 10, -10);
			gl2.glEnd();

			skyboxtextureXNeg.render(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(-10, -10, 10);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d(-10, 10, 10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d(-10, 10, -10);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(-10, -10, -10);
			gl2.glEnd();

			skyboxtextureYPos.render(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(-10, 10, 10);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d(10, 10, 10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d(10, 10, -10);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(-10, 10, -10);
			gl2.glEnd();

			skyboxtextureYNeg.render(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(10, -10, 10);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d(-10, -10, 10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d(-10, -10, -10);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(10, -10, -10);
			gl2.glEnd();

			skyboxtextureZPos.render(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(-10, 10, 10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d( 10, 10, 10);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d( 10,-10, 10);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(-10,-10, 10);
			gl2.glEnd();

			skyboxtextureZNeg.render(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(-10,-10, -10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d( 10,-10, -10);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d( 10, 10, -10);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(-10, 10, -10);
			gl2.glEnd();
			
		gl2.glPopMatrix();
		gl2.glEnable(GL2.GL_DEPTH_TEST);
	}
}
