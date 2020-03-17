package com.marginallyclever.robotOverlord.swingInterface;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.StringEntity;
import com.marginallyclever.robotOverlord.entity.primitives.PhysicalEntity;

/**
 * Wrapper for all projection matrix stuff at the start of the render pipeline.
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class CameraViewEntity extends Entity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected int canvasWidth, canvasHeight;
	protected int cursorX,cursorY;
	protected boolean isPressed;
	protected GLU glu;
	
	public DoubleEntity nearZ=new DoubleEntity("Near Z",5.0);
	public DoubleEntity farZ=new DoubleEntity("Far Z",2000.0);
	public DoubleEntity fieldOfView=new DoubleEntity("FOV",60.0);
	public StringEntity attachedTo=new StringEntity("Attached to","");
	
	public CameraViewEntity() {
		super();
		
		setName("Camera View");
		addChild(farZ);
		addChild(nearZ);
		addChild(fieldOfView);
		addChild(attachedTo);
			
		isPressed=false;
	}
	
	// OpenGL camera: -Z=forward, +X=right, +Y=up
	public void render(GL2 gl2) {
        // set up the projection matrix
    	gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glLoadIdentity();

		// opengl rendering context
        if(glu==null) glu = GLU.createGLU(gl2);
		
        glu.gluPerspective(
        		fieldOfView.get(), 
        		(float)canvasWidth/(float)canvasHeight, 
        		nearZ.get(),
        		farZ.get());
        
        renderShared(gl2);
	}
	
	// OpenGL camera: -Z=forward, +X=right, +Y=up
	public void renderOrtho(GL2 gl2) {
        // set up the projection matrix
    	gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glLoadIdentity();
		
	}
	
	public void renderPick(GL2 gl2,double pickX,double pickY) {
        // get the current viewport dimensions to set up the projection matrix
        int[] viewport = new int[4];
		gl2.glGetIntegerv(GL2.GL_VIEWPORT,viewport,0);

		// opengl rendering context
        if(glu==null) glu = GLU.createGLU(gl2);
        
		// Set up a tiny viewport that only covers the area behind the cursor.  Tiny viewports are faster?
        gl2.glMatrixMode(GL2.GL_PROJECTION);
        gl2.glLoadIdentity();
		glu.gluPickMatrix(pickX, viewport[3]-pickY, 5.0, 5.0, viewport,0);

		renderShared(gl2);
	}
	
	public void renderShared(GL2 gl2) {
    	gl2.glMatrixMode(GL2.GL_MODELVIEW);
        gl2.glLoadIdentity();
    	
		PhysicalEntity camera = getAttachedTo();
		Matrix4d mFinal = camera.getPoseWorld();
		mFinal.invert();
		MatrixHelper.applyMatrix(gl2, mFinal);
	}
	
	// reach out from the camera into the world and find the nearest object (if any) that the ray intersects.
	public Vector3d rayPick() {
		PhysicalEntity camera = getAttachedTo();
		Matrix4d mFinal = camera.getPoseWorld();
		Vector3d pickRay = MatrixHelper.getZAxis(mFinal);
		Vector3d vy = MatrixHelper.getYAxis(mFinal);
		Vector3d vx = MatrixHelper.getXAxis(mFinal);
		vy.scale(cursorY);
		vx.scale(cursorX);
		
		// TODO shouldn't this scale the vy instead of vz?
		pickRay.scale(-canvasHeight*Math.sin(Math.toRadians(fieldOfView.get())));
		pickRay.add(vx);
		pickRay.add(vy);
		pickRay.normalize();

		return pickRay;
	}

	public void setCursor(int x,int y) {
		cursorX= x - canvasWidth/2;
		cursorY= canvasHeight/2 - y;
        //System.out.println("X"+cursorX+" Y"+cursorY);
	}

	public void pressed() {
		isPressed=true;
	}

	public void released() {
		isPressed=false;
	}
	
	public boolean isPressed() {
		return isPressed;
	}
	
	public int getCanvasWidth() {
		return canvasWidth;
	}

	public void setCanvasWidth(int canvasWidth) {
		this.canvasWidth = canvasWidth;
	}

	public int getCanvasHeight() {
		return canvasHeight;
	}

	public void setCanvasHeight(int canvasHeight) {
		this.canvasHeight = canvasHeight;
	}
	
	public double getAspectRatio() {
		return canvasWidth/canvasHeight;
	}

	public PhysicalEntity getAttachedTo() {
		return (PhysicalEntity)findByPath(attachedTo.get());
	}
}
