package com.marginallyclever.robotOverlord.world;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.jogamp.opengl.GL2;

import javax.vecmath.Vector3f;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.marginallyclever.communications.NetworkConnectionManager;
import com.marginallyclever.convenience.FileAccess;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.BoundingVolume;
import com.marginallyclever.robotOverlord.Cylinder;
import com.marginallyclever.robotOverlord.IntersectionTester;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.arm5.EvilMinionRobot;
import com.marginallyclever.robotOverlord.camera.Camera;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.light.Light;
import com.marginallyclever.robotOverlord.physicalObject.PhysicalObject;
import com.marginallyclever.robotOverlord.viewCube.ViewCube;

/**
 * Container for all the visible objects in the world.
 * @author danroyer
 *
 */
public class World
implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2405142728731535038L;

	protected transient NetworkConnectionManager connectionManager = new NetworkConnectionManager();

	protected transient boolean areTexturesLoaded=false;

	public final static Vector3f forward = new Vector3f(0,0,1);
	public final static Vector3f right = new Vector3f(1,0,0);
	public final static Vector3f up = new Vector3f(0,1,0);
	
	// world contents
	protected ArrayList<Entity> entities;
	protected Camera camera;
	protected Light light0, light1, light2;
	protected transient Texture t0,t1,t2,t3,t4,t5;

	protected transient Vector3f pickForward = null;
	protected transient Vector3f pickRight = null;
	protected transient Vector3f pickUp = null;
	protected transient Vector3f pickRay = null;
	protected transient boolean isSetup = false;

	public int gridWidth, gridHeight;
	
	protected transient ViewCube viewCube;
	
	protected transient WorldControlPanel worldControlPanel;
	
	public World() {
		areTexturesLoaded=false;
		pickForward=new Vector3f();
		pickRight=new Vector3f();
		pickUp=new Vector3f();
		pickRay=new Vector3f();
		
		gridWidth = (int)(25.4*8);
		gridHeight = (int)(25.4*3);
		
		entities = new ArrayList<Entity>();
		addEntity(camera = new Camera());
		addEntity(light0 = new Light());
		addEntity(light1 = new Light());
		addEntity(light2 = new Light());
		
		viewCube = new ViewCube();
	}
	

	/**
	 * sets some render options at the
	 * @param gl2 the openGL render context
	 */
    protected void setup() {
		setupLights();
		loadTextures();
    }
    

    protected void setupLights() {
    	light0.index=0;
    	light0.setPosition(new Vector3f(0,0,30));
    	light0.setAmbient(         0.0f,          0.0f,          0.0f, 1.0f);
    	light0.setDiffuse(255.0f/255.0f, 255.0f/255.0f, 251.0f/255.0f, 1.0f);  // noon
	    light0.setSpecular(        1.0f,          1.0f,          1.0f, 1.0f);
    	
    	light1.index=1;
    	light0.setPosition(new Vector3f(-10,-10,10));
	    light1.setAmbient(  0.0f, 0.0f, 0.0f, 1.0f);
    	light1.setDiffuse(  1.0f, 1.0f, 1.0f, 1.0f);
	    light1.setSpecular( 0.0f, 0.0f, 0.0f, 1.0f);
	    
    	light2.index=2;
    	light2.setPosition(new Vector3f(30,30,30));
	    light2.setAmbient(          0.0f,          0.0f,          0.0f, 1.0f);
    	light2.setDiffuse( 242.0f/255.0f, 252.0f/255.0f, 255.0f/255.0f, 1.0f);  // metal halide
	    light2.setSpecular(         0.0f,          0.0f,          0.0f, 1.0f);
    	light2.setDirectional(true);
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
			//System.out.println(">>> All textures loaded OK");
			areTexturesLoaded=true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param gl2 render context
	 * @param delta ms since last render
	 */
	public void render(GL2 gl2, float delta) {
		if(!isSetup) {
			setup();
			setupLights();
			isSetup=true;
		}
		
		Iterator<Entity> io = entities.iterator();
		while(io.hasNext()) {
			Entity obj = io.next();
			if(obj instanceof PhysicalObject) {
				PhysicalObject po = (PhysicalObject)obj;
				po.prepareMove(delta);
			}
		}
		
		//TODO do collision test here
		
		// Finalize the moves that don't collide
		io = entities.iterator();
		while(io.hasNext()) {
			Entity obj = io.next();
			if(obj instanceof PhysicalObject) {
				PhysicalObject po = (PhysicalObject)obj;
				po.finalizeMove();
			}
		}

		// Clear the screen and depth buffer

		// background color
    	gl2.glClearColor(212.0f/255.0f, 233.0f/255.0f, 255.0f/255.0f, 1.0f);
    	// Special handling for the case where the GLJPanel is translucent
        // and wants to be composited with other Java 2D content
		/*
        if (GLProfile.isAWTAvailable() &&
            (gl2 instanceof com.jogamp.opengl.awt.GLJPanel) &&
            !((com.jogamp.opengl.awt.GLJPanel) gl2).isOpaque() &&
            ((com.jogamp.opengl.awt.GLJPanel) gl2).shouldPreserveColorBufferIfTranslucent()) {
          gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT);
        } else {
          gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT);
        }*/
    	gl2.glDrawBuffer(GL2.GL_BACK);
        gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_COLOR_BUFFER_BIT);
        
		gl2.glCullFace(GL2.GL_BACK);

		// DRAW THE WORLD
        gl2.glMatrixMode(GL2.GL_MODELVIEW);
		gl2.glLoadIdentity();
		
		gl2.glPushMatrix();
			camera.update(delta);  // this is ugly.  What if there is more than one camera?
			camera.render(gl2);
			
			gl2.glDisable(GL2.GL_LIGHTING);

			//drawSkyCube(gl2);
			
			PrimitiveSolids.drawGrid(gl2,gridWidth,gridHeight,1);

			// lights
			io = entities.iterator();
			while(io.hasNext()) {
				Entity obj = io.next();
				if(obj instanceof Light) {
					obj.render(gl2);
				}
			}

			// draw!
			io = entities.iterator();
			while(io.hasNext()) {
				Entity obj = io.next();
				if(obj instanceof Light) continue;
				if(obj instanceof Camera) continue;
				
				gl2.glPushName(obj.getPickName());
				obj.render(gl2);
				gl2.glPopName();
			}
	
			showPickingTest(gl2);
			
		gl2.glPopMatrix();
		
		// DRAW THE HUD

		gl2.glPushMatrix();
			camera.render(gl2);
			
	        gl2.glMatrixMode(GL2.GL_MODELVIEW);
			gl2.glLoadIdentity();
	
			viewCube.render(gl2,getCamera());
		gl2.glPopMatrix();
	}

	
	protected void showPickingTest(GL2 gl2) {
		if(pickForward == null) return;
		
		gl2.glPushMatrix();
		gl2.glDisable(GL2.GL_LIGHTING);

		Vector3f forward = new Vector3f();
		forward.set(pickForward);
		forward.scale(10);
		forward.sub(camera.getPosition());
		gl2.glColor3f(1,0,0);
		PrimitiveSolids.drawStar(gl2, forward);
		
		forward.set(pickForward);
		forward.scale(10);
		forward.add(pickRight);
		forward.sub(camera.getPosition());
		gl2.glColor3f(0,1,0);
		PrimitiveSolids.drawStar(gl2, forward);
		
		forward.set(pickForward);
		forward.scale(10);
		forward.add(pickUp);
		forward.sub(camera.getPosition());
		gl2.glColor3f(0,0,1);
		PrimitiveSolids.drawStar(gl2, forward);
		
		forward.set(pickRay);
		forward.scale(10);
		forward.sub(camera.getPosition());
		gl2.glColor3f(1,1,0);
		PrimitiveSolids.drawStar(gl2, forward);
		
		gl2.glEnable(GL2.GL_LIGHTING);
		gl2.glPopMatrix();
	}
	

	// Draw background
	protected void drawSkyCube(GL2 gl2) {
		if(!areTexturesLoaded) return;

        //gl2.glDisable(GL2.GL_CULL_FACE);
		
		gl2.glDisable(GL2.GL_DEPTH_TEST);
		gl2.glDisable(GL2.GL_LIGHTING);
		gl2.glDisable(GL2.GL_COLOR_MATERIAL);
		gl2.glEnable(GL2.GL_TEXTURE_2D);
		gl2.glPushMatrix();
			gl2.glColor3f(1, 1, 1);
			Vector3f p = camera.getPosition();
			gl2.glTranslated(-p.x,-p.y,-p.z);

			t0.bind(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(10, 10, 10);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d(10, -10, 10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d(10, -10, -10);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(10, 10, -10);
			gl2.glEnd();

			t1.bind(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(-10, -10, 10);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d(-10, 10, 10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d(-10, 10, -10);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(-10, -10, -10);
			gl2.glEnd();

			t2.bind(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(-10, 10, 10);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d(10, 10, 10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d(10, 10, -10);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(-10, 10, -10);
			gl2.glEnd();

			t3.bind(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(10, -10, 10);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d(-10, -10, 10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d(-10, -10, -10);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(10, -10, -10);
			gl2.glEnd();

			t4.bind(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glTexCoord2d(0,0);  gl2.glVertex3d(-10, 10, 10);
				gl2.glTexCoord2d(1,0);  gl2.glVertex3d( 10, 10, 10);
				gl2.glTexCoord2d(1,1);  gl2.glVertex3d( 10,-10, 10);
				gl2.glTexCoord2d(0,1);  gl2.glVertex3d(-10,-10, 10);
			gl2.glEnd();

			t5.bind(gl2);
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

	
	boolean willCollide(EvilMinionRobot a,EvilMinionRobot b) {
		//Get the cylinders for each robot
		BoundingVolume [] from = a.getBoundingVolumes();
		BoundingVolume [] to = b.getBoundingVolumes();
		// test cylinder/cylinder intersection
		for(int i=0;i<from.length;++i) {
			for(int j=0;j<to.length;++j) {
				if(IntersectionTester.CylinderCylinder((Cylinder)from[i],(Cylinder)to[i])) {
					return true;
				}
			}
		}
		// if there is any hit, return true.
		return false;
	}


	// reach out from the camera into the world and find the nearest object (if any) that the ray intersects.
	public void rayPick(double screenX, double screenY) {
		pickForward.set(camera.getForward());
		pickForward.scale(-1);
		pickRight.set(camera.getRight());
		pickRight.scale(-1);
		pickUp.set(camera.getUp());
		
		Vector3f vy = new Vector3f();
		vy.set(pickUp);
		vy.scale((float)-screenY*10);

		Vector3f vx = new Vector3f();
		vx.set(pickRight);
		vx.scale((float)screenX*10);

		pickRay.set(pickForward);
		pickRay.scale(10);
		pickRay.add(vx);
		pickRay.add(vy);
		pickRay.normalize();

		// TODO traverse the world and find the object that intersects the ray
	}

	
	public Entity pickObjectWithName(int pickName) {
		Entity newObject=null;
		if(pickName==0) {
			// Hit nothing!  Default to camera controls
			newObject=camera;
		} else {
			// scan all objects in world to find the one with the pickName.
			Iterator<Entity> iter = entities.iterator();
			while(iter.hasNext()) {
				Entity obj = iter.next();
				if( obj.hasPickName(pickName) ) {
					// found!
					newObject=obj;
					break;
				}
			}
		}
		
		return newObject;
	}

	
	public void addEntity(Entity o) {
		entities.add(o);
		worldControlPanel.updateEntityList();
	}
	
	public void removeEntity(Entity o) {
		entities.remove(o);
	}
	
	public boolean hasEntity(Entity o) {
		return entities.contains(o);
	}
	
	public List<String> namesOfAllObjects() {
		ArrayList<String> list = new ArrayList<String>();
		
		Iterator<Entity> i = this.entities.iterator();
		while(i.hasNext()) {
			String s = i.next().getDisplayName();
			list.add(s);
		}
		
		return list;
	}
	
	public Entity findObjectWithName(String name) {
		Iterator<Entity> i = this.entities.iterator();
		while(i.hasNext()) {
			Entity o = i.next();
			String objectName = o.getDisplayName();
			if(name.equals(objectName)) return o; 
		}
		
		return null;
	}
	
	
	public Camera getCamera() {
		return camera;
	}
	
	public WorldControlPanel getControlPanel(RobotOverlord gui) {
		if(worldControlPanel==null) worldControlPanel = new WorldControlPanel(gui,this); 
		return worldControlPanel;
	}
}
