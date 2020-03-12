package com.marginallyclever.robotOverlord.engine;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.marginallyclever.convenience.FileAccess;
import com.marginallyclever.robotOverlord.entity.camera.Camera;

public class SkyBox {
	protected transient boolean areSkyboxTexturesLoaded=false;
	protected transient Texture skyboxtextureZPos;
	protected transient Texture skyboxtextureXPos;
	protected transient Texture skyboxtextureXNeg;
	protected transient Texture skyboxtextureYPos;
	protected transient Texture skyboxtextureYNeg;
	protected transient Texture skyboxtextureZNeg;

	
	private void loadSkyboxTexturesOnce() {
		if(areSkyboxTexturesLoaded) return;
		try {
			skyboxtextureXPos = TextureIO.newTexture(FileAccess.open("/images/cube-x-pos.png"), false, "png");
			skyboxtextureXNeg = TextureIO.newTexture(FileAccess.open("/images/cube-x-neg.png"), false, "png");
			skyboxtextureYPos = TextureIO.newTexture(FileAccess.open("/images/cube-y-pos.png"), false, "png");
			skyboxtextureYNeg = TextureIO.newTexture(FileAccess.open("/images/cube-y-neg.png"), false, "png");
			skyboxtextureZPos = TextureIO.newTexture(FileAccess.open("/images/cube-z-pos.png"), false, "png");
			skyboxtextureZNeg = TextureIO.newTexture(FileAccess.open("/images/cube-z-neg.png"), false, "png");
			//System.out.println(">>> All textures loaded OK");
			areSkyboxTexturesLoaded=true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	// Draw background
	protected void render(GL2 gl2,Camera camera) {
		loadSkyboxTexturesOnce();
		if(!areSkyboxTexturesLoaded) return;

        //gl2.glDisable(GL2.GL_CULL_FACE);
		
		gl2.glDisable(GL2.GL_DEPTH_TEST);
		gl2.glDisable(GL2.GL_LIGHTING);
		gl2.glDisable(GL2.GL_COLOR_MATERIAL);
		gl2.glEnable(GL2.GL_TEXTURE_2D);
		gl2.glPushMatrix();
			gl2.glColor3f(1, 1, 1);
			Vector3d p = camera.getPosition();
			gl2.glTranslated(-p.x,-p.y,-p.z);

			skyboxtextureXPos.bind(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(10, 10, 10);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d(10, -10, 10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d(10, -10, -10);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(10, 10, -10);
			gl2.glEnd();

			skyboxtextureXNeg.bind(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(-10, -10, 10);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d(-10, 10, 10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d(-10, 10, -10);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(-10, -10, -10);
			gl2.glEnd();

			skyboxtextureYPos.bind(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(-10, 10, 10);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d(10, 10, 10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d(10, 10, -10);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(-10, 10, -10);
			gl2.glEnd();

			skyboxtextureYNeg.bind(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(10, -10, 10);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d(-10, -10, 10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d(-10, -10, -10);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(10, -10, -10);
			gl2.glEnd();

			skyboxtextureZPos.bind(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(-10, 10, 10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d( 10, 10, 10);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d( 10,-10, 10);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(-10,-10, 10);
			gl2.glEnd();

			skyboxtextureZNeg.bind(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(-10,-10, -10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d( 10,-10, -10);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d( 10, 10, -10);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(-10, 10, -10);
			gl2.glEnd();
			
		gl2.glPopMatrix();
		gl2.glEnable(GL2.GL_DEPTH_TEST);
        //gl2.glEnable(GL2.GL_CULL_FACE);
	}
}
