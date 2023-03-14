package com.marginallyclever.robotoverlord.tools.move;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.marginallyclever.convenience.*;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.Viewport;
import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.parameters.BooleanEntity;
import com.marginallyclever.robotoverlord.parameters.DoubleEntity;
import com.marginallyclever.robotoverlord.parameters.IntEntity;
import com.marginallyclever.robotoverlord.swinginterface.InputManager;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.PoseMoveEdit;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import com.marginallyclever.robotoverlord.tools.FrameOfReference;
import org.jetbrains.annotations.NotNull;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;

/**
 * A visual manipulator that facilitates moving objects in 3D.
 * @author Dan Royer
 */
public class MoveTool extends Entity {
	private static final double STEP_SIZE = Math.PI/120.0;
	
	protected TextRenderer textRender = new TextRenderer(new Font("CourrierNew", Font.BOLD, 16));

	/**
	 * The current frame of reference 3x3 matrix and the pivot point
	 */
	private final PoseComponent pivotMatrix = new PoseComponent();
	
	public Vector3d pickPoint=new Vector3d();  // the latest point picked on the ball
	public Vector3d pickPointSaved=new Vector3d();  // the point picked when the action began
	public Vector3d pickPointOnBall=new Vector3d();  // the point picked when the action began

	// for rotation
	private enum Plane {YZ, XZ, XY}
	private Plane nearestPlane;

	// for translation
	private enum Axis { X,Y,Z }
	private Axis majorAxis;
	
	private boolean isActivelyMoving=false;
	private boolean activeMoveIsRotation;
	private boolean movePivotOnly=false;

	// Who is being moved?
	private Entity subject;
	
	// In what frame of reference?
	private final IntEntity frameOfReferenceChoice = new IntEntity("Frame of Reference", FrameOfReference.WORLD.toInt());
	// drawing scale of ball
	private final DoubleEntity ballSize = new DoubleEntity("Scale",0.2);
	// snap at all?
	private final BooleanEntity snapOn = new BooleanEntity("Snap On",true);
	// snap to what number of degrees rotation?
	private final DoubleEntity snapDegrees = new DoubleEntity("Snap degrees",5);
	// snap to what number of mm translation?
	private final DoubleEntity snapDistance = new DoubleEntity("Snap mm",1);

	// matrix of subject when move started
	private Matrix4d startMatrix=new Matrix4d();
	private final Matrix4d resultMatrix=new Matrix4d();
	
	private double valueStart;  // original angle when move started
	private double valueNow;  // current state
	private double valueLast;  // state last frame

	private SlideDirection majorAxisSlideDirection;

	// rotate handle size
	static private final double rScale=0.8;
	// translate handle size
	static private final double tScale=0.9;
	// tool transparency
	static private final double alpha=0.8;
	// distance from camera to moving item
	private double cameraDistance=1;

	private final RobotOverlord ro;
	
	public MoveTool(RobotOverlord ro) {
		super();
		this.ro=ro;

		setName("MoveTool");
		addEntity(ballSize);
		addEntity(snapOn);
		addEntity(snapDegrees);
		addEntity(snapDistance);
	}

	/**
	 * transform a world-space point to the ball's current frame of reference
	 * @param pointInWorldSpace the world space point
	 * @return the transformed {@link Vector3d}
	 */
	private Vector3d getPickPointInFrameOfReference(Vector3d pointInWorldSpace, Matrix4d frameOfReference) {
		Matrix4d iMe = new Matrix4d(frameOfReference);
		iMe.m30=iMe.m31=iMe.m32=0;
		iMe.invert();
		Vector3d pickPointInBallSpace = new Vector3d(pointInWorldSpace);
		pickPointInBallSpace.sub(pivotMatrix.getPosition());
		iMe.transform(pickPointInBallSpace);
		return pickPointInBallSpace;
	}
	
	@Override
	public void update(double dt) {
		if(subject==null) return;

		CameraComponent cameraComponent = ro.getCamera();
		if(cameraComponent==null) return;

		PoseComponent subjectPose = subject.findFirstComponent(PoseComponent.class);
		if(subjectPose==null) return;

		Matrix4d subjectPoseWorld = pivotMatrix.getWorld();
		Vector3d mp = MatrixHelper.getPosition(subjectPoseWorld);

		PoseComponent camera = cameraComponent.getEntity().findFirstComponent(PoseComponent.class);
		mp.sub(camera.getPosition());
		cameraDistance = mp.length();

		if(!isActivelyMoving()) {
			checkChangeFrameOfReference();
			updateFrameOfReference(subjectPose.getWorld(),camera);
		}
		
		// let go button stops movement
		Viewport cameraView = ro.getViewport();
		if(!cameraView.isPressed()) {
			isActivelyMoving=false;
		} else if(!isActivelyMoving) {
			// button is pressed, try to start movement.
			checkMovementBegins();
		} else {
			// button is pressed, movement is active.
			movePivotOnly = (
					InputManager.isOn(InputManager.Source.KEY_RSHIFT) ||
					InputManager.isOn(InputManager.Source.KEY_LSHIFT) );

			if(activeMoveIsRotation) updateRotation();
			else					 updateTranslation(dt);
		}
	}

	/**
	 * Find the current frame of reference.  This could change every frame as the camera moves.
 	 */
	private void updateFrameOfReference(Matrix4d subjectPoseWorld,PoseComponent camera) {
		Vector3d pivotPoint = pivotMatrix.getPosition();
		switch (FrameOfReference.values()[frameOfReferenceChoice.get()]) {
			case SUBJECT -> pivotMatrix.setWorld(subjectPoseWorld);
			case CAMERA -> pivotMatrix.setWorld(MatrixHelper.lookAt(camera.getPosition(), MatrixHelper.getPosition(subjectPoseWorld)));
			default -> pivotMatrix.setWorld(MatrixHelper.createIdentityMatrix4());
		}
		pivotMatrix.setPosition(pivotPoint);
	}

	private void checkChangeFrameOfReference() {
		if(InputManager.isReleased(InputManager.Source.KEY_F1)) frameOfReferenceChoice.set(FrameOfReference.WORLD.toInt());
		if(InputManager.isReleased(InputManager.Source.KEY_F2)) frameOfReferenceChoice.set(FrameOfReference.CAMERA.toInt());
		if(InputManager.isReleased(InputManager.Source.KEY_F3)) frameOfReferenceChoice.set(FrameOfReference.SUBJECT.toInt());
	}

	private void checkMovementBegins() {
		Viewport cameraView = ro.getViewport();
		PoseComponent camera = ro.getCamera().getEntity().findFirstComponent(PoseComponent.class);
		Ray ray = cameraView.rayPick(ro.getCamera());

		Vector3d dp = new Vector3d(pivotMatrix.getPosition());
		dp.sub(ray.getOrigin());
		
		// not moving yet
		// find a pick point on the ball (ray/sphere intersection)
		// https://www.scratchapixel.com/lessons/3d-basic-rendering/minimal-ray-tracer-rendering-simple-shapes/ray-sphere-intersection
		double d = dp.length();
		
		// translation box centers
		Matrix4d frameOfReference = pivotMatrix.getWorld();
		double t0 = getNearestTranslationHandle(ray,frameOfReference);
		pickPoint.set(ray.getPoint(t0));
		
		if(t0<Double.MAX_VALUE) {
			// if hitting and pressed, begin translation.
			beginTranslation(frameOfReference,camera);
			return;
		}
		
		// rotation
		double Tca = ray.getDirection().dot(dp);
		if(Tca>=0) {
			// ball is in front of ray start
			double d2 = d*d - Tca*Tca;
			double r = ballSize.get()*cameraDistance*rScale;
			double r2=r*r;
			if(d2>=0 && d2<=r2) {
				// ball hit!  Begin rotation.
				beginRotation(ray,frameOfReference,r2,d2,Tca,dp);
			}
		}
	}

	private void beginRotation(Ray ray, Matrix4d frameOfReference, double r2, double d2, double Tca, Vector3d dp) {
		isActivelyMoving=true;
		activeMoveIsRotation=true;

		double Thc = Math.sqrt(r2 - d2);
		double t0 = Tca - Thc;
		pickPointOnBall = ray.getPoint(t0);
		startMatrix = pivotMatrix.getWorld();
		resultMatrix.set(startMatrix);

		Vector3d pickPointInFOR = getPickPointInFrameOfReference(pickPointOnBall, startMatrix);

		// find the nearest plane
		double dx = Math.abs(pickPointInFOR.x);
		double dy = Math.abs(pickPointInFOR.y);
		double dz = Math.abs(pickPointInFOR.z);
		nearestPlane=Plane.YZ;
		double nearestD=dx;
		if(dy<nearestD) {
			nearestPlane=Plane.XZ;
			nearestD=dy;
		}
		if(dz<nearestD) {
			nearestPlane=Plane.XY;
		}

		// https://www.scratchapixel.com/lessons/3d-basic-rendering/minimal-ray-tracer-rendering-simple-shapes/ray-plane-and-ray-disk-intersection
		Vector3d majorAxisVector = getMajorAxisVector(frameOfReference);

		// find the pick point on the plane of rotation
		double denominator = ray.getDirection().dot(majorAxisVector);
		if(denominator!=0) {
			double numerator = dp.dot(majorAxisVector);
			t0 = numerator/denominator;
			pickPoint.set(ray.getPoint(t0));
			pickPointSaved.set(pickPoint);

			pickPointInFOR = getPickPointInFrameOfReference(pickPoint,frameOfReference);
			switch (nearestPlane) {
				case YZ -> valueNow = -Math.atan2(pickPointInFOR.y, pickPointInFOR.z);
				case XZ -> valueNow = -Math.atan2(pickPointInFOR.z, pickPointInFOR.x);
				case XY -> valueNow = Math.atan2(pickPointInFOR.y, pickPointInFOR.x);
			}
			pickPointInFOR.normalize();
			//Log.message("p="+pickPointInFOR+" valueNow="+Math.toDegrees(valueNow));
		}
		valueStart=valueNow;
		valueLast=valueNow;
	}

	private void beginTranslation(Matrix4d frameOfReference,PoseComponent camera) {
		isActivelyMoving = true;
		activeMoveIsRotation=false;

		pickPointSaved.set(pickPoint);
		startMatrix = pivotMatrix.getWorld();
		resultMatrix.set(startMatrix);

		valueStart=0;
		valueLast=0;
		valueNow=0;

		Matrix4d cm = camera.getWorld();
		Vector3d cr = MatrixHelper.getXAxis(cm);
		Vector3d cu = MatrixHelper.getYAxis(cm);
		// determine which mouse direction is a positive movement on this axis.
		Vector3d nv = switch (majorAxis) {
			case X -> MatrixHelper.getXAxis(frameOfReference);
			case Y -> MatrixHelper.getYAxis(frameOfReference);
			case Z -> MatrixHelper.getZAxis(frameOfReference);
		};

		double cx,cy;
		cy=cu.dot(nv);
		cx=cr.dot(nv);
		if( Math.abs(cx) > Math.abs(cy) ) {
			majorAxisSlideDirection=(cx>0) ? SlideDirection.SLIDE_XPOS : SlideDirection.SLIDE_XNEG;
		} else {
			majorAxisSlideDirection=(cy>0) ? SlideDirection.SLIDE_YPOS : SlideDirection.SLIDE_YNEG;
		}
	}

	private double getNearestTranslationHandle(Ray ray, Matrix4d frameOfReference) {
		Vector3d px = new Vector3d(MatrixHelper.getXAxis(frameOfReference));
		Vector3d py = new Vector3d(MatrixHelper.getYAxis(frameOfReference));
		Vector3d pz = new Vector3d(MatrixHelper.getZAxis(frameOfReference));
		px.scale(ballSize.get()*cameraDistance*tScale);
		py.scale(ballSize.get()*cameraDistance*tScale);
		pz.scale(ballSize.get()*cameraDistance*tScale);
		px.add(pivotMatrix.getPosition());
		py.add(pivotMatrix.getPosition());
		pz.add(pivotMatrix.getPosition());

		// of the three boxes, the closest hit is the one to remember.
		double dx = testBoxHit(ray,px);
		double dy = testBoxHit(ray,py);
		double dz = testBoxHit(ray,pz);

		double t0=Double.MAX_VALUE;
		if(dx>0) {
			majorAxis=Axis.X;
			t0=dx;
		}
		if(dy>0 && dy<t0) {
			majorAxis=Axis.Y;
			t0=dy;
		}
		if(dz>0 && dz<t0) {
			majorAxis=Axis.Z;
			t0=dz;
		}
		return t0;
	}

	@NotNull
	private Vector3d getMajorAxisVector(Matrix4d frameOfReference) {
		return switch (nearestPlane) {
			case YZ -> MatrixHelper.getXAxis(frameOfReference);
			case XZ -> MatrixHelper.getYAxis(frameOfReference);
			case XY -> MatrixHelper.getZAxis(frameOfReference);
		};
	}

	private void updateRotation() {
		valueNow = valueLast;

		Viewport cameraView = ro.getViewport();
		Ray ray = cameraView.rayPick(ro.getCamera());

		Vector3d dp = new Vector3d(pivotMatrix.getPosition());
		dp.sub(ray.getOrigin());
		
		// https://www.scratchapixel.com/lessons/3d-basic-rendering/minimal-ray-tracer-rendering-simple-shapes/ray-plane-and-ray-disk-intersection
		Matrix4d FOR = startMatrix;
		Vector3d majorAxisVector = getMajorAxisVector(FOR);

		// find the pick point on the plane of rotation
		double denominator = ray.getDirection().dot(majorAxisVector);
		if(denominator!=0) {
			double numerator = dp.dot(majorAxisVector);
			double t0 = numerator/denominator;
			pickPoint.set(ray.getPoint(t0));

			Vector3d pickPointInFOR = getPickPointInFrameOfReference(pickPoint,FOR);
			switch (nearestPlane) {
				case YZ -> valueNow = -Math.atan2(pickPointInFOR.y, pickPointInFOR.z);
				case XZ -> valueNow = -Math.atan2(pickPointInFOR.z, pickPointInFOR.x);
				case XY -> valueNow = Math.atan2(pickPointInFOR.y, pickPointInFOR.x);
			}

			double da=valueNow - valueStart;
			if(snapOn.get()) {
				// round to snapDegrees
				double deg = snapDegrees.get();
				if( InputManager.isOn(InputManager.Source.KEY_RCONTROL) ||
					InputManager.isOn(InputManager.Source.KEY_LCONTROL) ) {
					deg *= 0.1;
				}
				
				da = Math.toDegrees(da);
				da = Math.signum(da)*Math.round(Math.abs(da)/deg)*deg;
				da = Math.toRadians(da);
			}
			if(da!=0) {
				switch (nearestPlane) {
					case YZ -> rollX(da);
					case XZ -> rollY(da);
					case XY -> rollZ(da);
				}
				valueLast = valueStart + da;
				
				attemptMove();
			}
		}
	}

	private void updateTranslation(double dt) {
		valueNow = valueLast;

		// actively being dragged
		double scale = cameraDistance*0.02*dt;  // TODO something better?
		double rawX= InputManager.getRawValue(InputManager.Source.MOUSE_X);
		double rawY= InputManager.getRawValue(InputManager.Source.MOUSE_Y);
		double dx = rawX *  scale;
		double dy = rawY * -scale;

		switch (majorAxisSlideDirection) {
			case SLIDE_XPOS -> valueNow += dx;
			case SLIDE_XNEG -> valueNow -= dx;
			case SLIDE_YPOS -> valueNow += dy;
			case SLIDE_YNEG -> valueNow -= dy;
		}
		
		double dp = valueNow - valueStart;
		if(snapOn.get()) {
			// round to the nearest mm
			double mm = snapDistance.get()*0.1;
			if( InputManager.isOn(InputManager.Source.KEY_RCONTROL) ||
				InputManager.isOn(InputManager.Source.KEY_LCONTROL) ) {
				mm *= 0.1;
			}
			dp = Math.signum(dp)*Math.round(Math.abs(dp)/mm)*mm;
		}
		if(dp!=0) {
			Matrix4d FOR = pivotMatrix.getWorld();
			switch (majorAxis) {
				case X -> translate(MatrixHelper.getXAxis(FOR), dp);
				case Y -> translate(MatrixHelper.getYAxis(FOR), dp);
				case Z -> translate(MatrixHelper.getZAxis(FOR), dp);
			}
			valueLast = valueStart + dp;

			attemptMove();
		}
	}

	public void attemptMove() {
		if(!movePivotOnly) {
			UndoSystem.addEvent(this,new PoseMoveEdit(subject,pivotMatrix.getWorld(),resultMatrix,Translator.get("MoveTool.editName")));
		}
		pivotMatrix.setWorld(resultMatrix);
	}

	protected double testBoxHit(Ray ray,Vector3d n) {		
		Point3d b0 = new Point3d(); 
		Point3d b1 = new Point3d();

		b0.set(+0.05,+0.05,+0.05);
		b1.set(-0.05,-0.05,-0.05);
		b0.scale(ballSize.get()*cameraDistance);
		b1.scale(ballSize.get()*cameraDistance);
		b0.add(n);
		b1.add(n);
		
		return IntersectionHelper.rayBox(ray,b0,b1);
	}
	
	@Override
	public void render(GL2 gl2) {
		if(subject==null) return;

		PoseComponent subjectPose = subject.findFirstComponent(PoseComponent.class);
		if(subjectPose==null) return;

		gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT);

		boolean wasText = OpenGLHelper.disableTextureStart(gl2);
		boolean lightWasOn = OpenGLHelper.disableLightingStart(gl2);
		float oldWidth = OpenGLHelper.setLineWidth(gl2, 2);

		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, pivotMatrix.getWorld());
			renderRotation(gl2);
			renderTranslation(gl2);
		gl2.glPopMatrix();

		OpenGLHelper.setLineWidth(gl2, oldWidth);
		OpenGLHelper.disableLightingEnd(gl2, lightWasOn);
		OpenGLHelper.disableTextureEnd(gl2, wasText);

		printDistanceOnScreen(gl2,ro);
	}
	
	private void printDistanceOnScreen(GL2 gl2,RobotOverlord ro) { 
		gl2.glEnable(GL2.GL_TEXTURE_2D);
		
		//ro.viewport.renderOrtho(gl2);
		textRender.beginRendering(ro.getViewport().getCanvasWidth(), ro.getViewport().getCanvasHeight());
		textRender.setColor(0,0,0,1);
		Matrix4d w = resultMatrix;
		Vector3d wp = MatrixHelper.getPosition(w);
		Vector3d eu = MatrixHelper.matrixToEuler(w);
		textRender.draw("("
				+StringHelper.formatDouble(wp.x)+","
				+StringHelper.formatDouble(wp.y)+","
				+StringHelper.formatDouble(wp.z)+")-("
				+StringHelper.formatDouble(Math.toDegrees(eu.x))+","
				+StringHelper.formatDouble(Math.toDegrees(eu.y))+","
				+StringHelper.formatDouble(Math.toDegrees(eu.z))+")", 20, 20);
		textRender.endRendering();
		//ro.viewport.renderPerspective(gl2);
	}

	public void renderRotation(GL2 gl2) {
		gl2.glPushMatrix();
			double scale = ballSize.get()*cameraDistance;
			gl2.glScaled(scale,scale,scale);

			PoseComponent camera = ro.getCamera().getEntity().findFirstComponent(PoseComponent.class);
			Matrix4d pw = camera.getWorld();
			pw.m03=
			pw.m13=
			pw.m23=0;
			pw.invert();
	
			double cr = (nearestPlane==Plane.YZ) ? 1 : 0.5f;
			double cg = (nearestPlane==Plane.XZ) ? 1 : 0.5f;
			double cb = (nearestPlane==Plane.XY) ? 1 : 0.5f;
			
			gl2.glDisable(GL2.GL_CULL_FACE);
	
			double radius0 = rScale;
			double radius1 = radius0-0.1;
			gl2.glColor4d(cr, 0, 0,alpha);		renderDiscYZ(gl2,radius0,radius1); //x
			gl2.glColor4d(0, cg, 0,alpha);		renderDiscXZ(gl2,radius0,radius1); //y
			gl2.glColor4d(0, 0, cb,alpha);		renderDiscXY(gl2,radius0,radius1); //z
			
			gl2.glEnable(GL2.GL_CULL_FACE);
			gl2.glCullFace(GL2.GL_BACK);
			
			if(isActivelyMoving && activeMoveIsRotation) {
				// display the distance rotated.
				double start=MathHelper.wrapRadians(valueStart);
				double end=MathHelper.wrapRadians(valueNow);
				double range=end-start;
				while(range>Math.PI) range-=Math.PI*2;
				while(range<-Math.PI) range+=Math.PI*2;
				double absRange= Math.abs(range);
				
				// snap ticks
				if(snapOn.get()) {
					double deg = Math.toRadians(snapDegrees.get());
					if( InputManager.isOn(InputManager.Source.KEY_RCONTROL) ||
						InputManager.isOn(InputManager.Source.KEY_LCONTROL) ) {
						deg *= 0.1;
					}

					renderTickMarksOnNearestPlane(gl2, start, deg,radius0);
				}

				renderCircleOnNearestPlane(gl2, radius0, start, range, absRange);
			}

		gl2.glPopMatrix();
	}

	private void renderTickMarksOnNearestPlane(GL2 gl2, double start, double deg, double r1) {
		double r2=r1+0.05;
		double a=0,b=0,c=0;

		gl2.glBegin(GL2.GL_LINES);
		gl2.glColor3d(1, 1, 1);
		for(double i = 0;i<Math.PI*2;i+= deg) {
			double j=i + start;
			switch (nearestPlane) {
				case YZ -> {
					b = Math.cos(j + Math.PI / 2);
					c = Math.sin(j + Math.PI / 2);
				}
				case XZ -> {
					a = Math.cos(-j);
					c = Math.sin(-j);
				}
				case XY -> {
					a = Math.cos(j);
					b = Math.sin(j);
				}
			}
			gl2.glVertex3d(a * r1, b * r1, c * r1);
			gl2.glVertex3d(a * r2, b * r2, c * r2);
		}
		gl2.glEnd();
	}

	private void renderCircleOnNearestPlane(GL2 gl2, double radius0, double start, double range, double absRange) {
		double a=0,b=0,c=0;

		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glColor3f(255,255,255);
		gl2.glVertex3d(0,0,0);
		for(double i = 0; i< absRange; i+=0.01) {
			double j = range * (i/ absRange) + start;

			switch (nearestPlane) {
				case YZ -> {
					b = Math.cos(j + Math.PI / 2);
					c = Math.sin(j + Math.PI / 2);
				}
				case XZ -> {
					a = Math.cos(-j);
					c = Math.sin(-j);
				}
				case XY -> {
					a = Math.cos(j);
					b = Math.sin(j);
				}
			}
			gl2.glVertex3d(a* radius0,b* radius0,c* radius0);
		}
		gl2.glEnd();
	}

	private void renderDiscXY(GL2 gl2,double radius0,double radius1) {
		gl2.glBegin(GL2.GL_TRIANGLE_STRIP);
		for(double n=0;n<Math.PI*4;n+=STEP_SIZE) {
			double x=Math.cos(n);
			double y=Math.sin(n);
			gl2.glVertex3d(y*radius0,x*radius0,0);
			gl2.glVertex3d(y*radius1,x*radius1,0);
		}
		gl2.glEnd();
	}
	
	private void renderDiscYZ(GL2 gl2,double radius0,double radius1) {
		gl2.glBegin(GL2.GL_TRIANGLE_STRIP);
		for(double n=0;n<Math.PI*4;n+=STEP_SIZE) {
			double x=Math.cos(n);
			double y=Math.sin(n);
			gl2.glVertex3d(0,x*radius0,y*radius0);
			gl2.glVertex3d(0,x*radius1,y*radius1);
		}
		gl2.glEnd();
	}
	
	private void renderDiscXZ(GL2 gl2,double radius0,double radius1) {
		gl2.glBegin(GL2.GL_TRIANGLE_STRIP);
		for(double n=0;n<Math.PI*4;n+=STEP_SIZE) {
			double x=Math.cos(n);
			double y=Math.sin(n);
			gl2.glVertex3d(x*radius0,0,y*radius0);
			gl2.glVertex3d(x*radius1,0,y*radius1);
		}
		gl2.glEnd();
	}
	
	public void renderTranslation(GL2 gl2) {
		gl2.glPushMatrix();
			double scale = ballSize.get()*cameraDistance; 
			gl2.glScaled(scale,scale,scale);
			
			// camera forward is -z axis
			PoseComponent camera = ro.getCamera().getEntity().findFirstComponent(PoseComponent.class);

			PoseComponent subjectPose = subject.findFirstComponent(PoseComponent.class);
			Matrix4d pw = subjectPose.getWorld();

			Vector3d lookAtVector = MatrixHelper.getPosition(pw);
			lookAtVector.sub(camera.getPosition());
			lookAtVector.normalize();
		
			float r = (majorAxis==Axis.X) ? 1 : 0.5f;
			float g = (majorAxis==Axis.Y) ? 1 : 0.5f;
			float b = (majorAxis==Axis.Z) ? 1 : 0.5f;
	
			gl2.glColor4d(r,0,0,alpha);		renderTranslationHandle(gl2,new Vector3d(tScale,0,0));
			gl2.glColor4d(0,g,0,alpha);		renderTranslationHandle(gl2,new Vector3d(0,tScale,0));
			gl2.glColor4d(0,0,b,alpha);		renderTranslationHandle(gl2,new Vector3d(0,0,tScale));
	
			gl2.glDisable(GL2.GL_CULL_FACE);
			
			// handle for XY plane
			gl2.glColor4d(r,g,0,alpha);
			gl2.glBegin(GL2.GL_QUADS);
			gl2.glVertex3d(0.00, 0.00, 0);
			gl2.glVertex3d(0.15, 0.00, 0);
			gl2.glVertex3d(0.15, 0.15, 0);
			gl2.glVertex3d(0.00, 0.15, 0);
			gl2.glEnd();
	
			// handle for XZ plane
			gl2.glColor4d(r,0,b,alpha);
			gl2.glBegin(GL2.GL_QUADS);
			gl2.glVertex3d(0.00, 0, 0.00);
			gl2.glVertex3d(0.15, 0, 0.00);
			gl2.glVertex3d(0.15, 0, 0.15);
			gl2.glVertex3d(0.00, 0, 0.15);
			gl2.glEnd();
	
			// handle for YZ plane
			gl2.glColor4d(0,g,b,alpha);
			gl2.glBegin(GL2.GL_QUADS);
			gl2.glVertex3d(0, 0.00, 0.00);
			gl2.glVertex3d(0, 0.00, 0.15);
			gl2.glVertex3d(0, 0.15, 0.15);
			gl2.glVertex3d(0, 0.15, 0.00);
			gl2.glEnd();
	
			gl2.glEnable(GL2.GL_CULL_FACE);

		gl2.glPopMatrix();

		if(isActivelyMoving && !activeMoveIsRotation) {
			gl2.glBegin(GL2.GL_LINES);
			// the distance we tried to move.
			gl2.glColor3f(1,1,1);
			gl2.glVertex3d(startMatrix.m03,startMatrix.m13,startMatrix.m23);
			gl2.glVertex3d(pw.m03,pw.m13,pw.m23);
			// distance we wanted to move.
			gl2.glColor3d(0.8,0.8,0.8);
			gl2.glVertex3d(pw.m03,pw.m13,pw.m23);
			gl2.glVertex3d(resultMatrix.m03,resultMatrix.m13,resultMatrix.m23);
			gl2.glEnd();
		}
	}
	
	private void renderTranslationHandle(GL2 gl2,Vector3d n) {
		double s = 0.05;
		// draw box
		Point3d b0 = new Point3d( s, s, s); 
		Point3d b1 = new Point3d(-s,-s,-s);

		b0.scale(1);
		b1.scale(1);
		b0.add(n);
		b1.add(n);
		PrimitiveSolids.drawBox(gl2, b0,b1);

		// draw line to box
		n.scale(1-s);
		gl2.glBegin(GL2.GL_LINES);
		gl2.glVertex3d(0,0,0);
		gl2.glVertex3d(n.x,n.y,n.z);
		gl2.glEnd();

	}

	public boolean isActivelyMoving() {
		return isActivelyMoving;
	}

	public void setActivelyMoving(boolean isActivelyMoving) {
		this.isActivelyMoving = isActivelyMoving;
	}
	
	protected void rotationInternal(Matrix4d rotation) {
		// multiply robot origin by target matrix to get target matrix in world space.

		// invert frame of reference to transform world target matrix into frame of reference space.

		Matrix4d subjectRotation = new Matrix4d(startMatrix);
		resultMatrix.set(pivotMatrix.getWorld());
		Matrix4d iFOR = new Matrix4d(resultMatrix);
		iFOR.invert();

		resultMatrix.mul(rotation);
		resultMatrix.mul(iFOR);
		resultMatrix.mul(subjectRotation);
		resultMatrix.setTranslation(MatrixHelper.getPosition(startMatrix));
	}

	protected void rollX(double angRadians) {
		// apply transform about the origin  change translation to rotate around something else.
		Matrix4d temp = new Matrix4d();
		temp.rotX(angRadians);
		rotationInternal(temp);
	}

	protected void rollY(double angRadians) {
		// apply transform about the origin  change translation to rotate around something else.
		Matrix4d temp = new Matrix4d();
		temp.rotY(angRadians);
		rotationInternal(temp);
	}
	
	protected void rollZ(double angRadians) {
		// apply transform about the origin.  change translation to rotate around something else.
		Matrix4d temp = new Matrix4d();
		temp.rotZ(angRadians);
		rotationInternal(temp);
	}

	/**
	 * resultMatrix.p = startMatrix.p + v * amount 
	 * @param v normal vector (length 1)
	 * @param amount scale of v.
	 */
	protected void translate(Vector3d v, double amount) {
		//Log.message(amount);
		resultMatrix.m03 = startMatrix.m03 + v.x*amount;
		resultMatrix.m13 = startMatrix.m13 + v.y*amount;
		resultMatrix.m23 = startMatrix.m23 + v.z*amount;
	}

	public Matrix4d getResultMatrix() {
		return resultMatrix;
	}

	/**
	 * Set which subject(s) the drag ball is going to act upon.
	 * @param subject the subject to act upon.
	 */
	public void setSubject(Entity subject) {
		this.subject = subject;
		setPivotToSubject();
	}

	private void setPivotToSubject() {
		if(subject == null) return;

		PoseComponent subjectPose = subject.findFirstComponent(PoseComponent.class);
		if(subjectPose == null) return;

		Matrix4d subjectPoseWorld = subjectPose.getWorld();
		Vector3d mp = MatrixHelper.getPosition(subjectPoseWorld);
		pivotMatrix.setPosition(mp);
	}

	@Override
	public void getView(ViewPanel view) {
		view.pushStack("MoveTool",true);
		view.add(ballSize);
		view.add(snapOn);
		view.add(snapDegrees);
		view.add(snapDistance);
		
		view.addComboBox(frameOfReferenceChoice,FrameOfReference.getAll());
		view.popStack();
		
		super.getView(view);
	}
}
