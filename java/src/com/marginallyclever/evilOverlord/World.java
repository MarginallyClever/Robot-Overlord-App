package com.marginallyclever.evilOverlord;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import javax.media.opengl.GL2;
import javax.media.opengl.GLProfile;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.vecmath.Vector3f;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.marginallyclever.evilOverlord.Camera.Camera;
import com.marginallyclever.evilOverlord.EvilMinion.EvilMinionRobot;
import com.marginallyclever.evilOverlord.RotaryStewartPlatform2.RotaryStewartPlatform2;
import com.marginallyclever.evilOverlord.Spidee.Spidee;
import com.marginallyclever.evilOverlord.communications.MarginallyCleverConnectionManager;
import com.marginallyclever.evilOverlord.communications.SerialConnectionManager;

/**
 * Container for all the visible objects in the world.
 * @author danroyer
 *
 */
public class World
implements ActionListener, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2405142728731535038L;

	protected transient MarginallyCleverConnectionManager connectionManager = new SerialConnectionManager();
	
	protected transient JMenu worldMenu;
	protected transient JMenuItem buttonAddArm5Robot;
	protected transient JMenuItem buttonAddRSP2;
	protected transient JMenuItem buttonAddSpidee;
	protected transient boolean areTexturesLoaded=false;
	
	// world contents
	protected ArrayList<ObjectInWorld> objects = new ArrayList<ObjectInWorld>();
	protected Camera camera = null;
	protected LightObject light0;
	protected LightObject light1;
	protected transient Texture t0,t1,t2,t3,t4,t5;

	protected transient Vector3f pickForward = null;
	protected transient Vector3f pickRight = null;
	protected transient Vector3f pickUp = null;
	protected transient Vector3f pickRay = null;
	protected transient ObjectInWorld lastPickedObject = null;
	protected transient boolean isSetup = false;
	

	public World() {
		camera = new Camera();		
		light0 = new LightObject();
		light1 = new LightObject();
		areTexturesLoaded=false;

		pickForward=new Vector3f();
		pickRight=new Vector3f();
		pickUp=new Vector3f();
		pickRay=new Vector3f();
	}
	
	protected void addArm5Robot() {
		EvilMinionRobot r = new EvilMinionRobot();
		r.setConnectionManager(connectionManager);
		objects.add(r);
	}
	
	protected void addRSP2() {
		RotaryStewartPlatform2 r = new RotaryStewartPlatform2();
		r.setConnectionManager(connectionManager);
		objects.add(r);
	}
	
	protected void addSpidee() {
		Spidee r = new Spidee();
		r.setConnectionManager(connectionManager);
		objects.add(r);
	}

    protected void setup( GL2 gl2 ) {
		gl2.glDepthFunc(GL2.GL_LESS);
		gl2.glEnable(GL2.GL_DEPTH_TEST);
		gl2.glDepthMask(true);
		
		setupLights();
		loadTextures(gl2);
    }
    

    protected void setupLights() {
    	light1.index=1;
	    light1.position=new float[]{-1.0f,-1.0f,1.0f,0.0f};
	    light1.ambient =new float[]{ 0.0f, 0.0f,0.0f,1.0f};
	    light1.diffuse =new float[]{ 2.0f, 2.0f,2.0f,1.0f};
	    light1.specular=new float[]{ 1.0f, 1.0f,1.0f,1.0f};
    }
    
	
	void loadTextures( GL2 gl2 ) {
		if(areTexturesLoaded) return;
		
		// World background texture
		try {/*
			t0 = TextureIO.newTexture(getClass().getResourceAsStream("/images/cube-x-pos.png"), true,null);
			t1 = TextureIO.newTexture(getClass().getResourceAsStream("/images/cube-x-neg.png"), true,null);
			t2 = TextureIO.newTexture(getClass().getResourceAsStream("/images/cube-y-pos.png"), true,null);
			t3 = TextureIO.newTexture(getClass().getResourceAsStream("/images/cube-y-neg.png"), true,null);
			t4 = TextureIO.newTexture(getClass().getResourceAsStream("/images/cube-z-pos.png"), true,null);
			t5 = TextureIO.newTexture(getClass().getResourceAsStream("/images/cube-z-neg.png"), true,null);
			*/
			t0 = TextureIO.newTexture(new File("java/src/images/cube-x-pos.png"), true);
			t1 = TextureIO.newTexture(new File("java/src/images/cube-x-neg.png"), true);
			t2 = TextureIO.newTexture(new File("java/src/images/cube-y-pos.png"), true);
			t3 = TextureIO.newTexture(new File("java/src/images/cube-y-neg.png"), true);
			t4 = TextureIO.newTexture(new File("java/src/images/cube-z-pos.png"), true);
			t5 = TextureIO.newTexture(new File("java/src/images/cube-z-neg.png"), true);
			System.out.println(">>> All textures loaded OK");
			areTexturesLoaded=true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();
		
		if(subject == buttonAddArm5Robot) {
			addArm5Robot();
			return;
		}
		if(subject == buttonAddRSP2) {
			addRSP2();
			return;
		}
		if(subject == buttonAddSpidee) {
			addSpidee();
			return;
		}
	}
	
	
    public JMenu updateMenu() {
    	worldMenu = new JMenu("World");
    	buttonAddArm5Robot = new JMenuItem("Add Evil Minion");
    	worldMenu.add(buttonAddArm5Robot);
    	buttonAddArm5Robot.addActionListener(this);
    	
    	buttonAddRSP2 = new JMenuItem("Add Rotary Stewart Platform 2");
    	worldMenu.add(buttonAddRSP2);
    	buttonAddRSP2.addActionListener(this);

    	buttonAddSpidee = new JMenuItem("Add Spidee");
    	worldMenu.add(buttonAddSpidee);
    	buttonAddSpidee.addActionListener(this);
    	
    	return worldMenu;
    }
    
	
	public void render(GL2 gl2, float delta ) {
		if(isSetup==false) {
			setup(gl2);
			isSetup=true;
		}
		
		Iterator<ObjectInWorld> io = objects.iterator();
		while(io.hasNext()) {
			ObjectInWorld obj = io.next();
			if(obj instanceof PhysicalObject) {
				PhysicalObject po = (PhysicalObject)obj;
				po.prepareMove(delta);
			}
		}
		
		//TODO do collision test here
		
		// Finalize the moves that don't collide
		io = objects.iterator();
		while(io.hasNext()) {
			ObjectInWorld obj = io.next();
			if(obj instanceof PhysicalObject) {
				PhysicalObject po = (PhysicalObject)obj;
				po.finalizeMove();
			}
		}

        gl2.glMatrixMode(GL2.GL_MODELVIEW);
		gl2.glLoadIdentity();

		// background color
    	gl2.glClearColor(212.0f/255.0f, 233.0f/255.0f, 255.0f/255.0f, 0.0f);
        // Special handling for the case where the GLJPanel is translucent
        // and wants to be composited with other Java 2D content
        if (GLProfile.isAWTAvailable() &&
            (gl2 instanceof javax.media.opengl.awt.GLJPanel) &&
            !((javax.media.opengl.awt.GLJPanel) gl2).isOpaque() &&
            ((javax.media.opengl.awt.GLJPanel) gl2).shouldPreserveColorBufferIfTranslucent()) {
          gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT);
        } else {
          gl2.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        }
        
        gl2.glDisable(GL2.GL_CULL_FACE);
		//gl2.glEnable(GL2.GL_CULL_FACE);
		//gl2.glCullFace(GL2.GL_BACK);

			
		gl2.glPushMatrix();
			camera.update(delta);
			camera.render(gl2);
			
			drawSkyCube(gl2);
			
			 // Enable lighting
			gl2.glShadeModel(GL2.GL_SMOOTH);
			gl2.glEnable(GL2.GL_LIGHTING);
			gl2.glEnable(GL2.GL_COLOR_MATERIAL);
			gl2.glColorMaterial( GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE );
			
			light0.render(gl2);
			light1.render(gl2);
			
		    // draw grid
			gl2.glDisable(GL2.GL_LIGHTING);
			PrimitiveSolids.drawGrid(gl2,50,5);
			gl2.glEnable(GL2.GL_LIGHTING);
			
			// draw!
			io = objects.iterator();
			while(io.hasNext()) {
				ObjectInWorld obj = io.next();
				gl2.glPushName(obj.getPickName());
				obj.render(gl2);
				gl2.glPopName();
			}
	
			showPickingTest(gl2);
			
		gl2.glPopMatrix();
	}

	
	protected void showPickingTest(GL2 gl2) {
		if(pickForward == null) return;
		
		gl2.glPushMatrix();
		gl2.glDisable(GL2.GL_LIGHTING);

		Vector3f forward = new Vector3f();
		forward.set(pickForward);
		forward.scale(10);
		forward.sub(camera.position);
		gl2.glColor3f(1,0,0);
		PrimitiveSolids.drawStar(gl2, forward);
		
		forward.set(pickForward);
		forward.scale(10);
		forward.add(pickRight);
		forward.sub(camera.position);
		gl2.glColor3f(0,1,0);
		PrimitiveSolids.drawStar(gl2, forward);
		
		forward.set(pickForward);
		forward.scale(10);
		forward.add(pickUp);
		forward.sub(camera.position);
		gl2.glColor3f(0,0,1);
		PrimitiveSolids.drawStar(gl2, forward);
		
		forward.set(pickRay);
		forward.scale(10);
		forward.sub(camera.position);
		gl2.glColor3f(1,1,0);
		PrimitiveSolids.drawStar(gl2, forward);
		
		gl2.glEnable(GL2.GL_LIGHTING);
		gl2.glPopMatrix();
	}
	

	// Draw background
	protected void drawSkyCube(GL2 gl2) {
		if(!areTexturesLoaded) return;
		
		gl2.glDisable(GL2.GL_DEPTH_TEST);
		gl2.glDisable(GL2.GL_LIGHTING);
		gl2.glDisable(GL2.GL_COLOR_MATERIAL);
		gl2.glEnable(GL2.GL_TEXTURE_2D);
		gl2.glPushMatrix();
			gl2.glColor3f(1, 1, 1);
			gl2.glTranslated(-camera.position.x,-camera.position.y,-camera.position.z);

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
	}

	
	boolean willCollide(EvilMinionRobot a,EvilMinionRobot b) {
		// TODO complete me
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

	
	public void pickObjectWithName(int pickName,EvilOverlord gui) {
		ObjectInWorld newObject=null;
		if(pickName==0) {
			// Hit nothing!  Default to camera controls
			newObject=camera;
		} else {
			// scan all objects in world to find the one with the pickName.
			Iterator<ObjectInWorld> iter = objects.iterator();
			while(iter.hasNext()) {
				ObjectInWorld obj = iter.next();
				if( obj.getPickName()==pickName ) {
					// found!
					newObject=obj;
					break;
				}
			}
		}
		
		if(newObject != lastPickedObject) {
			// only change the menu if the selected object has changed.
			lastPickedObject = newObject;
			gui.setContextMenu(lastPickedObject.buildPanel(gui),lastPickedObject.getDisplayName());
		}
	}
	
	public void pickCamera(EvilOverlord gui) {
		lastPickedObject = camera;
		gui.setContextMenu(lastPickedObject.buildPanel(gui),lastPickedObject.getDisplayName());
	}
}
