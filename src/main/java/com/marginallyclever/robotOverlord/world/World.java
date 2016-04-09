package com.marginallyclever.robotOverlord.world;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLProfile;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.vecmath.Vector3f;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.marginallyclever.robotOverlord.BoundingVolume;
import com.marginallyclever.robotOverlord.Cylinder;
import com.marginallyclever.robotOverlord.IntersectionTester;
import com.marginallyclever.robotOverlord.LightObject;
import com.marginallyclever.robotOverlord.ModelInWorld;
import com.marginallyclever.robotOverlord.ObjectInWorld;
import com.marginallyclever.robotOverlord.PhysicalObject;
import com.marginallyclever.robotOverlord.PrimitiveSolids;
import com.marginallyclever.robotOverlord.RobotWithConnection;
import com.marginallyclever.robotOverlord.Camera.Camera;
import com.marginallyclever.robotOverlord.EvilMinion.EvilMinionRobot;
import com.marginallyclever.robotOverlord.communications.AbstractConnectionManager;
import com.marginallyclever.robotOverlord.communications.SerialConnectionManager;

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

	protected transient AbstractConnectionManager connectionManager = new SerialConnectionManager();
	
	protected transient JMenu worldMenu;
	protected transient JMenuItem buttonAddModel;
	protected transient boolean areTexturesLoaded=false;
	
	// world contents
	protected ArrayList<ObjectInWorld> objects = new ArrayList<ObjectInWorld>();
	protected Camera camera = null;
	protected LightObject light0;
	protected LightObject light1;
	protected LightObject light2;
	protected transient Texture t0,t1,t2,t3,t4,t5;

	protected transient Vector3f pickForward = null;
	protected transient Vector3f pickRight = null;
	protected transient Vector3f pickUp = null;
	protected transient Vector3f pickRay = null;
	protected transient boolean isSetup = false;
	
	protected List<AddRobotToWorldButton> addRobotButtons = null;

	public World() {
		camera = new Camera();		
		light0 = new LightObject();
		light1 = new LightObject();
		light2 = new LightObject();
		areTexturesLoaded=false;

		pickForward=new Vector3f();
		pickRight=new Vector3f();
		pickUp=new Vector3f();
		pickRay=new Vector3f();
	}

	
	protected void addModel(JFrame mainFrame) {
		JFileChooser fc = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("STL files", "STL", "abc");
		fc.setFileFilter(filter);
		int returnVal = fc.showOpenDialog(mainFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String filename = fc.getSelectedFile().getAbsolutePath();
    		ModelInWorld m = new ModelInWorld();
    		m.setFilename( filename );
    		objects.add(m);
		}
	}

    public void setup( GL2 gl2 ) {
		gl2.glDepthFunc(GL2.GL_LESS);
		gl2.glEnable(GL2.GL_DEPTH_TEST);
		gl2.glDepthMask(true);

    	gl2.glEnable(GL2.GL_LINE_SMOOTH);      
        //gl2.glEnable(GL2.GL_POLYGON_SMOOTH);
        gl2.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST);
        
        gl2.glEnable(GL2.GL_BLEND);
        gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

        // add a settings toggle for this option, it really slows down older machines.
        gl2.glEnable(GL2.GL_MULTISAMPLE);
        
        int buf[] = new int[1];
        int sbuf[] = new int[1];
        gl2.glGetIntegerv(GL2.GL_SAMPLES, buf, 0);
        gl2.glGetIntegerv(GL2.GL_SAMPLE_BUFFERS, sbuf, 0);
        
		setupLights();
		loadTextures(gl2);
    }
    

    protected void setupLights() {
    	light0.index=0;
	    light0.position=new float[]{  0.0f,0.0f,1.0f,0.0f};
	    light0.ambient =new float[]{  0.0f, 0.0f,0.0f,1.0f};
    	light0.diffuse =new float[]{255.0f/255.0f, 255.0f/255.0f, 251.0f/255.0f, 1.0f};  // noon
	    light0.specular=new float[]{  0.0f, 0.0f,0.0f,1.0f};
    	
    	light1.index=1;
	    light1.position=new float[]{-1.0f,-1.0f,1.0f,0.0f};
	    light1.ambient =new float[]{ 0.0f, 0.0f,0.0f,1.0f};
	    light1.diffuse =new float[]{ 1.0f, 1.0f,1.0f,1.0f};
	    light1.specular=new float[]{ 1.0f, 1.0f,1.0f,1.0f};
	    
    	light2.index=2;
	    light2.position=new float[]{  1.0f, 1.0f,1.0f,0.0f};
	    light2.ambient =new float[]{   0.0f, 0.0f,0.0f,1.0f};
	    light2.diffuse =new float[]{ 242.0f/255.0f, 252.0f/255.0f, 255.0f/255.0f,1.0f};  // metal halide
	    light2.specular=new float[]{   1.0f, 1.0f,1.0f,1.0f};
    }
    
	
	void loadTextures( GL2 gl2 ) {
		if(areTexturesLoaded) return;
		
		// World background texture
		try {
			t0 = TextureIO.newTexture(this.getClass().getResource("/images/cube-x-pos.png"), true, "png");
			t1 = TextureIO.newTexture(this.getClass().getResource("/images/cube-x-neg.png"), true, "png");
			t2 = TextureIO.newTexture(this.getClass().getResource("/images/cube-y-pos.png"), true, "png");
			t3 = TextureIO.newTexture(this.getClass().getResource("/images/cube-y-neg.png"), true, "png");
			t4 = TextureIO.newTexture(this.getClass().getResource("/images/cube-z-pos.png"), true, "png");
			t5 = TextureIO.newTexture(this.getClass().getResource("/images/cube-z-neg.png"), true, "png");
			//System.out.println(">>> All textures loaded OK");
			areTexturesLoaded=true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();

		if( subject == buttonAddModel ) {
			JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(buttonAddModel);
			addModel(topFrame);
			return;
		}
	}
	
	
    public JMenu updateMenu() {
    	worldMenu = new JMenu("World");
		
		// service load the robot types available.
		addRobotButtons = new ArrayList<AddRobotToWorldButton>();
		ServiceLoader<RobotWithConnection> loaders = ServiceLoader.load(RobotWithConnection.class);
		Iterator<RobotWithConnection> i = loaders.iterator();
		while(i.hasNext()) {
			RobotWithConnection lft = i.next();
			AddRobotToWorldButton button = new AddRobotToWorldButton(this,lft,"Add "+lft.getDisplayName());
			addRobotButtons.add(button);
			worldMenu.add(button);
		}

    	worldMenu.addSeparator();
    	
    	buttonAddModel = new JMenuItem("Add Model");
    	worldMenu.add(buttonAddModel);
    	buttonAddModel.addActionListener(this);
    	
    	return worldMenu;
    }
    
	
	public void render(GL2 gl2, float delta ) {
		if(isSetup==false) {
			setup(gl2);
			isSetup=true;
		}
		
		setupLights();
		
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
            (gl2 instanceof com.jogamp.opengl.awt.GLJPanel) &&
            !((com.jogamp.opengl.awt.GLJPanel) gl2).isOpaque() &&
            ((com.jogamp.opengl.awt.GLJPanel) gl2).shouldPreserveColorBufferIfTranslucent()) {
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
			light2.render(gl2);
			
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

	
	public ObjectInWorld pickObjectWithName(int pickName) {
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
		
		return newObject;
	}


	
	public void addRobot(RobotWithConnection r) {
		r.setConnectionManager(connectionManager);
		objects.add(r);
	}
	
	public Camera getCamera() {
		return camera;
	}
}
