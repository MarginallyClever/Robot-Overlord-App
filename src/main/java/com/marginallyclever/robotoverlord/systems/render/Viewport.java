package com.marginallyclever.robotoverlord.systems.render;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.OpenGLHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.convenience.Ray;
import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.parameters.BooleanParameter;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.parameters.swing.ViewPanelFactory;

import javax.vecmath.*;

/**
 * Wrapper for all projection matrix stuff at the start of the systems pipeline.
 * OpenGL camera: -Z=forward, +X=right, +Y=up
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class Viewport extends Entity {
	private int canvasWidth, canvasHeight;
	private final int[] viewportDimensions = new int[4];

	/**
	 * The mouse cursor position in screen coordinates.
 	 */
	private double cursorX, cursorY;

	private final DoubleParameter nearZ=new DoubleParameter("Near Z",1.0);
	private final DoubleParameter farZ=new DoubleParameter("Far Z",1000.0);
	private final DoubleParameter fieldOfView=new DoubleParameter("FOV",60.0);
	private final BooleanParameter drawOrthographic=new BooleanParameter("Orthographic",false);
	
	private CameraComponent camera;
	
	
	public Viewport() {
		super();
	}

	@Deprecated
	public void renderPerspective(GL3 gl) {
		double zNear = nearZ.get();
		double zFar = farZ.get();
		double fH = Math.tan(Math.toRadians(fieldOfView.get() / 2)) * zNear;
		double aspect = (double)canvasWidth / (double)canvasHeight;
		double fW = fH * aspect;
		gl.glFrustum(-fW,fW,-fH,fH,zNear,zFar);
	}

	/**
	 * Render the scene in orthographic projection.
	 * @param gl the OpenGL context
	 * @param zoom the zoom factor
	 */
	@Deprecated
	public void renderOrthographic(GL3 gl, double zoom) {
		double w = canvasWidth / 2.0;
		double h = canvasHeight / 2.0;
		gl.glOrtho(-w / zoom, w / zoom, -h / zoom, h / zoom, nearZ.get(), farZ.get());
	}

	@Deprecated
	public void renderOrthographic(GL3 gl) {
		renderOrthographic(gl,camera.getOrbitDistance()/100.0);
	}

	@Deprecated
	public void renderChosenProjection(GL3 gl) {
		gl.glMatrixMode(GL3.GL_PROJECTION);
		gl.glLoadIdentity();

		if(drawOrthographic.get()) {
			renderOrthographic(gl);
		} else {
			renderPerspective(gl);
		}

		gl.glMatrixMode(GL3.GL_MODELVIEW);
		gl.glLoadIdentity();
		if(camera !=null) {
			PoseComponent pose = camera.getEntity().getComponent(PoseComponent.class);
			Matrix4d inverseCamera = pose.getWorld();
			inverseCamera.invert();
			MatrixHelper.applyMatrix(gl, inverseCamera);
		}
	}

	public Matrix4d getPerspectiveFrustum() {
		double nearVal = nearZ.get();
		double farVal = farZ.get();
		double fovY = fieldOfView.get();
		double aspect = (double)canvasWidth / (double)canvasHeight;

		return MatrixHelper.perspectiveMatrix4d(fovY,aspect,nearVal,farVal);
	}

	/**
	 * Render the scene in orthographic projection.
	 * @param zoom the zoom factor
	 */
	public Matrix4d getOrthographicMatrix(double zoom) {
		double w = canvasWidth/2.0f;
		double h = canvasHeight/2.0f;

		double left = -w/zoom;
		double right = w/zoom;
		double bottom = -h/zoom;
		double top = h/zoom;
		double nearVal = nearZ.get();
		double farVal = farZ.get();

		return MatrixHelper.orthographicMatrix4d(left,right,bottom,top,nearVal,farVal);
	}

	public Matrix4d getOrthographicMatrix() {
		return getOrthographicMatrix(1.0);
	}
	
	public Matrix4d getChosenProjectionMatrix() {
		if (drawOrthographic.get()) {
			return getOrthographicMatrix();
		} else {
			return getPerspectiveFrustum();
		}
	}

	public Matrix4d getViewMatrix() {
		if(camera !=null) {
			PoseComponent pose = camera.getEntity().getComponent(PoseComponent.class);
			Matrix4d inverseCamera = pose.getWorld();
			inverseCamera.invert();
			return inverseCamera;
		}
		return MatrixHelper.createIdentityMatrix4();
	}

	/**
	 * Return the ray coming through the viewport in the current projection.
	 * @return the ray coming through the viewport in the current projection.
	 */
	public Ray getRayThroughCursor() {
		return getRayThroughPoint(cursorX,cursorY);
	}

	/**
	 * Return the ray coming through the viewport in the current projection.
	 * @param x the cursor position in screen coordinates [-1,1]
	 * @param y the cursor position in screen coordinates [-1,1]
	 * @return the ray coming through the viewport in the current projection.
	 */
	public Ray getRayThroughPoint(double x,double y) {
		// OpenGL camera: -Z=forward, +X=right, +Y=up
		// get the ray coming through the viewport in the current projection.
		Point3d origin;
		Vector3d direction;

		//double px = (cursorX+1.0) * canvasWidth / 2.0d;
		//double py = (cursorY+1.0) * canvasHeight / 2.0d;

		if(drawOrthographic.get()) {
			// orthographic projection
			origin = new Point3d(
					x*canvasWidth/10,
					y*canvasHeight/10,
					0);
			direction = new Vector3d(0,0,-1);
			PoseComponent pose = camera.getEntity().getComponent(PoseComponent.class);
			Matrix4d m2 = pose.getWorld();
			m2.transform(direction);
			m2.transform(origin);
		} else {
			// perspective projection
			double [] cn = getCursorAsNormalized();
			double t = Math.tan(Math.toRadians(fieldOfView.get()/2));
			direction = new Vector3d((cn[0])*t*getAspectRatio(),(cn[1])*t,-1);
			
			// adjust the ray by the camera world pose.
			PoseComponent pose = camera.getEntity().getComponent(PoseComponent.class);
			Matrix4d m2 = pose.getWorld();
			m2.transform(direction);
			origin = new Point3d(pose.getPosition());
		}

		return new Ray(origin,direction);
	}

	@Deprecated
	public void showPickingTest(GL3 gl) {
		renderChosenProjection(gl);
		gl.glPushMatrix();

		Ray r = getRayThroughCursor();

		double cx=cursorX;
		double cy=cursorY;
        int w = canvasWidth;
        int h = canvasHeight;
        setCursor(0,0);	Ray tl = getRayThroughCursor();
        setCursor(w,0);		Ray tr = getRayThroughCursor();
        setCursor(0,h);		Ray bl = getRayThroughCursor();
        setCursor(w,h);			Ray br = getRayThroughCursor();
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

		boolean tex = OpenGLHelper.disableTextureStart(gl);
		
        gl.glColor3d(1, 0, 0);
		gl.glBegin(GL3.GL_LINES);
		drawPoint(gl,tl.getOrigin());		drawPoint(gl,tl2);
		drawPoint(gl,tr.getOrigin());		drawPoint(gl,tr2);
		drawPoint(gl,bl.getOrigin());		drawPoint(gl,bl2);
		drawPoint(gl,br.getOrigin());		drawPoint(gl,br2);
        gl.glColor3d(1, 1, 1);
		drawPoint(gl,r.getOrigin());		drawPoint(gl,r2);
		gl.glEnd();
        gl.glColor3d(0, 1, 0);
		gl.glBegin(GL3.GL_LINE_LOOP);
		drawPoint(gl,tl2);
		drawPoint(gl,tr2);
		drawPoint(gl,br2);
		drawPoint(gl,bl2);
		gl.glEnd();
        gl.glColor3d(0, 0, 1);
		gl.glBegin(GL3.GL_LINE_LOOP);
		drawPoint(gl,tl.getOrigin());
		drawPoint(gl,tr.getOrigin());
		drawPoint(gl,br.getOrigin());
		drawPoint(gl,bl.getOrigin());
		gl.glEnd();
		
		PrimitiveSolids.drawStar(gl,r2,5);
		gl.glPopMatrix();

		OpenGLHelper.disableTextureEnd(gl,tex);
	}

	private void drawPoint(GL3 gl, Tuple3d vector) {
		gl.glVertex3d(vector.x, vector.y, vector.z);
	}

	/**
	 * Set the cursor position in the canvas.
	 * @param x the x position in the canvas.  0....canvasWidth
	 * @param y the y position in the canvas.  0....canvasHeight
	 */
	public void setCursor(int x,int y) {
		cursorX = x;
		cursorY = y;
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

	@Deprecated
	public void getView(ViewPanelFactory view) {
		view.add(drawOrthographic);
		view.add(farZ);
		view.add(nearZ);
		view.add(fieldOfView);
	}

	public double [] getCursor() {
		return new double[]{cursorX,cursorY};
	}

	/**
	 * Returns the cursor position as values from -1...1.
	 * @return the cursor position as values from -1...1.
	 */
	public double [] getCursorAsNormalized() {
		double px = (2.0*cursorX/canvasWidth)-1.0;
		double py = 1.0-(2.0*cursorY/canvasHeight);

		return new double[]{px,py};
	}

	public CameraComponent getCamera() {
		return camera;
	}

	public void setCamera(CameraComponent camera) {
		this.camera = camera;
	}
}
