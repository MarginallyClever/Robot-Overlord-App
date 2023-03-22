package com.marginallyclever.robotoverlord;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.convenience.Ray;
import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.parameters.BooleanEntity;
import com.marginallyclever.robotoverlord.parameters.DoubleEntity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

/**
 * Wrapper for all projection matrix stuff at the start of the render pipeline.
 * OpenGL camera: -Z=forward, +X=right, +Y=up
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class Viewport extends Entity {
	private int canvasWidth, canvasHeight;
	// mouse position in GUI
	private double cursorX,cursorY;
	// is mouse pressed in GUI?
	private boolean isPressed;

	private final DoubleEntity nearZ=new DoubleEntity("Near Z",0.5);
	private final DoubleEntity farZ=new DoubleEntity("Far Z",2000.0);
	private final DoubleEntity fieldOfView=new DoubleEntity("FOV",60.0);
	private final BooleanEntity drawOrthographic=new BooleanEntity("Orthographic",false);
	
	private CameraComponent camera;
	
	
	public Viewport() {
		super();

		addEntity(drawOrthographic);
		addEntity(farZ);
		addEntity(nearZ);
		addEntity(fieldOfView);
			
		isPressed=false;
	}
	
	public void renderPerspective(GL2 gl2) {
		double zNear = nearZ.get();
		double zFar = farZ.get();
		double fH = Math.tan( Math.toRadians(fieldOfView.get()/2) ) * zNear;
		double aspect = (double)canvasWidth / (double)canvasHeight;
		double fW = fH * aspect;
	
		gl2.glFrustum(-fW,fW,-fH,fH,zNear,zFar);
	}

	/**
	 * Render the scene in orthographic projection.
	 * @param gl2 the OpenGL context
	 * @param zoom the zoom factor
	 */
	public void renderOrthographic(GL2 gl2, double zoom) {
        double w = canvasWidth/2.0;
        double h = canvasHeight/2.0;
		gl2.glOrtho(-w/zoom, w/zoom, -h/zoom, h/zoom, nearZ.get(), farZ.get());
	}
	
	public void renderOrthographic(GL2 gl2) {
        renderOrthographic(gl2,camera.getOrbitDistance()/100.0);
	}

	public void renderShared(GL2 gl2,CameraComponent camera) {
    	gl2.glMatrixMode(GL2.GL_MODELVIEW);
		gl2.glLoadIdentity();

		if(camera !=null) {
			PoseComponent pose = camera.getEntity().findFirstComponent(PoseComponent.class);
			Matrix4d inverseCamera = pose.getWorld();
			inverseCamera.invert();
			MatrixHelper.applyMatrix(gl2, inverseCamera);
		}
	}
	
	public void renderChosenProjection(GL2 gl2) {
    	gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glLoadIdentity();
		
		if(drawOrthographic.get()) {
			renderOrthographic(gl2);
		} else {
			renderPerspective(gl2);
		}
		
        renderShared(gl2,camera);
	}
	
	public void renderPick(GL2 gl2) {
        // get the current viewport dimensions to set up the projection matrix
        int[] viewportDimensions = new int[4];
		gl2.glGetIntegerv(GL2.GL_VIEWPORT,viewportDimensions,0);

		GLU glu = GLU.createGLU(gl2);
        
		// Set up a tiny viewport that only covers the area behind the cursor. 
		// Tiny viewports are faster.
        gl2.glMatrixMode(GL2.GL_PROJECTION);
        gl2.glLoadIdentity();
		glu.gluPickMatrix(cursorX, canvasHeight-cursorY, 5.0, 5.0, viewportDimensions,0);

		if(drawOrthographic.get()) {
			renderOrthographic(gl2);
		} else {
			renderPerspective(gl2);
		}
		
		renderShared(gl2,camera);
	}
	
	// reach out from the camera into the world and find the nearest object (if any) that the ray intersects.
	public Ray rayPick() {
		// OpenGL camera: -Z=forward, +X=right, +Y=up
		// get the ray coming through the viewport in the current projection.
		Point3d origin;
		Vector3d direction;

		if(drawOrthographic.get()) {
			// orthographic projection
			origin = new Point3d(
					cursorX*canvasWidth/10,
					cursorY*canvasHeight/10,
					0);
			direction = new Vector3d(0,0,-1);
			PoseComponent pose = camera.getEntity().findFirstComponent(PoseComponent.class);
			Matrix4d m2 = pose.getWorld();
			m2.transform(direction);
			m2.transform(origin);
		} else {
			// perspective projection
			double aspect = (double)canvasWidth / (double)canvasHeight;
			double t = Math.tan(Math.toRadians(fieldOfView.get()/2));
			direction = new Vector3d(cursorX*t*aspect,cursorY*t,-1);
			
			// adjust the ray by the camera world pose.
			PoseComponent pose = camera.getEntity().findFirstComponent(PoseComponent.class);
			Matrix4d m2 = pose.getWorld();
			m2.transform(direction);
			origin = new Point3d(pose.getPosition());
		}

		return new Ray(origin,direction);
	}

	public void showPickingTest(GL2 gl2) {
		renderChosenProjection(gl2);
		gl2.glPushMatrix();

		Ray r = rayPick();

		double cx=cursorX;
		double cy=cursorY;
        int w = canvasWidth;
        int h = canvasHeight;
        setCursor(0,0);	Ray tl = rayPick();
        setCursor(w,0);		Ray tr = rayPick();
        setCursor(0,h);		Ray bl = rayPick();
        setCursor(w,h);			Ray br = rayPick();
		cursorX=cx;
		cursorY=cy;

        double scale=20;
        
        Vector3d tl2 = new Vector3d(tl.getDirection());
        Vector3d tr2 = new Vector3d(tr.getDirection());
        Vector3d bl2 = new Vector3d(bl.getDirection());
        Vector3d br2 = new Vector3d(br.getDirection());
        Vector3d r2  = new Vector3d(r .getDirection());

		tl2.scale(scale);
		tr2.scale(scale);
		bl2.scale(scale);
		br2.scale(scale);
		r2 .scale(scale);

        tl2.add(tl.getOrigin());
        tr2.add(tr.getOrigin());
        bl2.add(bl.getOrigin());
        br2.add(br.getOrigin());
        r2.add(r.getOrigin());
        
        gl2.glDisable(GL2.GL_TEXTURE_2D);
		gl2.glDisable(GL2.GL_LIGHTING);
		
        gl2.glColor3d(1, 0, 0);
		gl2.glBegin(GL2.GL_LINES);
		drawPoint(gl2,tl.getOrigin());		drawPoint(gl2,tl2);
		drawPoint(gl2,tr.getOrigin());		drawPoint(gl2,tr2);
		drawPoint(gl2,bl.getOrigin());		drawPoint(gl2,bl2);
		drawPoint(gl2,br.getOrigin());		drawPoint(gl2,br2);
        gl2.glColor3d(1, 1, 1);
		drawPoint(gl2,r.getOrigin());		drawPoint(gl2,r2);
		gl2.glEnd();
        gl2.glColor3d(0, 1, 0);
		gl2.glBegin(GL2.GL_LINE_LOOP);
		drawPoint(gl2,tl2);
		drawPoint(gl2,tr2);
		drawPoint(gl2,br2);
		drawPoint(gl2,bl2);
		gl2.glEnd();
        gl2.glColor3d(0, 0, 1);
		gl2.glBegin(GL2.GL_LINE_LOOP);
		drawPoint(gl2,tl.getOrigin());
		drawPoint(gl2,tr.getOrigin());
		drawPoint(gl2,br.getOrigin());
		drawPoint(gl2,bl.getOrigin());
		gl2.glEnd();
		
		PrimitiveSolids.drawStar(gl2,r2,5);
		gl2.glPopMatrix();
	}

	private void drawPoint(GL2 gl2, Tuple3d vector) {
		gl2.glVertex3d(vector.x, vector.y, vector.z);
	}

	public void setCursor(int x,int y) {
		cursorX= (2.0*x/canvasWidth)-1.0;
		cursorY= 1.0-(2.0*y/canvasHeight);
        //Log.message("X"+cursorX+" Y"+cursorY);
	}

	// mouse was pressed in GUI
	public void pressed() {
		isPressed=true;
	}

	// mouse was released in GUI
	public void released() {
		isPressed=false;
	}
	
	// is mouse pressed in GUI?
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
		return (double)canvasWidth/(double)canvasHeight;
	}

	public double getFieldOfView() {
		return fieldOfView.get();
	}

	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Viewport",true);
		view.add(drawOrthographic);
		view.add(farZ);
		view.add(nearZ);
		view.add(fieldOfView);
		view.popStack();
		super.getView(view);
	}

	public double [] getCursor() {
		return new double[]{cursorX,cursorY};
	}

	public CameraComponent getCamera() {
		return camera;
	}

	public void setCamera(CameraComponent camera) {
		this.camera = camera;
	}
}
