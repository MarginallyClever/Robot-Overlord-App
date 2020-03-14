package com.marginallyclever.robotOverlord.engine;


import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.marginallyclever.convenience.FileAccess;
import com.marginallyclever.robotOverlord.entity.cameraEntity.CameraEntity;

public class ViewCube {
	protected transient boolean isSetup = false;
	protected transient boolean areTexturesLoaded = false;
	protected transient Texture t0,t1,t2,t3,t4,t5,shadow;
	
    public ViewCube() {
		areTexturesLoaded=false;
    }
	
	void loadTextures() {
		if(areTexturesLoaded) return;
		
		// World background skybox texture
		try {
			t0 = TextureIO.newTexture(FileAccess.open("/images/cube-x-pos.png"), false, "png");
			t1 = TextureIO.newTexture(FileAccess.open("/images/cube-x-neg.png"), false, "png");
			t2 = TextureIO.newTexture(FileAccess.open("/images/cube-y-pos.png"), false, "png");
			t3 = TextureIO.newTexture(FileAccess.open("/images/cube-y-neg.png"), false, "png");
			t4 = TextureIO.newTexture(FileAccess.open("/images/cube-z-pos.png"), false, "png");
			t5 = TextureIO.newTexture(FileAccess.open("/images/cube-z-neg.png"), false, "png");
			shadow = TextureIO.newTexture(FileAccess.open("/images/shadow.png"), false, "png");
			//System.out.println(">>> ViewCube textures loaded OK");
			areTexturesLoaded=true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	public void render(GL2 gl2,CameraEntity cam) {
		if(!isSetup) {
			loadTextures();
			isSetup=true;
		}


		gl2.glEnable(GL2.GL_DEPTH_TEST);
		
		gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glPushMatrix();
			gl2.glLoadIdentity();
			gl2.glOrtho(0, cam.getCanvasWidth(), cam.getCanvasHeight(), 0, -1, 100);
			gl2.glMatrixMode(GL2.GL_MODELVIEW);
			gl2.glPushMatrix();
				gl2.glLoadIdentity();
		
				gl2.glDisable(GL2.GL_LIGHTING);
				gl2.glDisable(GL2.GL_COLOR_MATERIAL);
	
				gl2.glColor3f(1, 1, 1);
				//Vector3f p = camera.getPosition();
				//gl2.glTranslated(-p.x,-p.y,-p.z);
	
				gl2.glTranslated(cam.getCanvasWidth()-55,+55,-50);
				gl2.glRotated(-cam.getTilt(), -1, 0, 0);
				gl2.glRotated(-cam.getPan(),0,0,1);
				gl2.glScalef(3, -3, 3);
			
				gl2.glPushMatrix();
					gl2.glTranslated(-10.4,-10.4,-10.4);
					gl2.glBegin(GL2.GL_LINES);
					gl2.glColor3d(1, 0, 0);		gl2.glVertex3d(0, 0, 0);		gl2.glVertex3d(25, 0, 0);
					gl2.glColor3d(0, 1, 0);		gl2.glVertex3d(0, 0, 0);		gl2.glVertex3d(0, 25, 0);
					gl2.glColor3d(0, 0, 1);		gl2.glVertex3d(0, 0, 0);		gl2.glVertex3d(0, 0, 25);
					gl2.glEnd();
				gl2.glPopMatrix();
	
				gl2.glEnable(GL2.GL_TEXTURE_2D);
				gl2.glColor4d(1,1,1,1);
				t0.bind(gl2);
				gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(-10, 10, 10);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d(-10, -10, 10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d(-10, -10, -10);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(-10, 10, -10);
				gl2.glEnd();
	
				t1.bind(gl2);
				gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(10, -10, 10);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d(10, 10, 10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d(10, 10, -10);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(10, -10, -10);
				gl2.glEnd();
	
				t2.bind(gl2);
				gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d(-10, 10, 10);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(10, 10, 10);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(10, 10, -10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d(-10, 10, -10);
				gl2.glEnd();
	
				t3.bind(gl2);
				gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d(10, -10, 10);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(-10, -10, 10);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(-10, -10, -10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d(10, -10, -10);
				gl2.glEnd();
	
				t4.bind(gl2);
				gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(-10,-10, 10);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(-10, 10, 10);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d( 10, 10, 10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d( 10,-10, 10);
				gl2.glEnd();
	
				t5.bind(gl2);
				gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(-10, 10, -10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d( 10, 10, -10);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d( 10,-10, -10);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(-10,-10, -10);
				gl2.glEnd();
	
				gl2.glEnable(GL2.GL_CULL_FACE);
				gl2.glCullFace(GL2.GL_BACK);
				gl2.glBlendFunc(GL2.GL_SRC_ALPHA,GL2.GL_ONE_MINUS_SRC_ALPHA);
				shadow.bind(gl2);
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
				// Still in MODELVIEW mode
			gl2.glPopMatrix();
			
		gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glPopMatrix();
		
		gl2.glMatrixMode(GL2.GL_MODELVIEW);
	}
}
