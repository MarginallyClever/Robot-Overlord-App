package com.marginallyclever.robotoverlord.systems.render;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.parameters.BooleanParameter;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Wrapper for all projection matrix stuff at the start of the systems pipeline.
 * OpenGL camera: -Z=forward, +X=right, +Y=up
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class Viewport extends Entity {
	private int canvasWidth, canvasHeight;

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
