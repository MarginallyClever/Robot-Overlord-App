package com.marginallyclever.robotoverlord.tools.move;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.marginallyclever.convenience.*;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.Viewport;
import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.swinginterface.InputManager;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.PoseMoveEdit;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import com.marginallyclever.robotoverlord.tools.FrameOfReference;
import com.marginallyclever.robotoverlord.uiexposedtypes.BooleanEntity;
import com.marginallyclever.robotoverlord.uiexposedtypes.DoubleEntity;
import com.marginallyclever.robotoverlord.uiexposedtypes.IntEntity;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;

/**
 * A visual manipulator that facilitates moving objects in 3D.
 * @author Dan Royer
 *
 */
public class MoveTool extends Entity {
	private static final double STEP_SIZE = Math.PI/120.0;
	
	protected TextRenderer textRender = new TextRenderer(new Font("CourrierNew", Font.BOLD, 16));

	protected Vector3d position = new Vector3d();
	
	public Vector3d pickPoint=new Vector3d();  // the latest point picked on the ball
	public Vector3d pickPointSaved=new Vector3d();  // the point picked when the action began
	public Vector3d pickPointOnBall=new Vector3d();  // the point picked when the action began
	
	private enum Plane { X,Y,Z }
	private enum Axis { X,Y,Z }

	// for rotation
	private Plane nearestPlane;
	// for translation
	private Axis majorAxis;
	
	private boolean isActivelyMoving;
	private boolean activeMoveIsRotation;

	// Who is being moved?
	private Entity subject;
	
	// In what frame of reference?
	private final IntEntity frameOfReference = new IntEntity("Frame of Reference", FrameOfReference.WORLD.toInt());
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
	private final Matrix4d FOR=new Matrix4d();
	
	private double valueStart;  // original angle when move started
	private double valueNow;  // current state
	private double valueLast;  // state last frame 
	
	private final double[] valueStarts = new double[3];
	private final double[] valueNows = new double[3];
	private final double[] valueLasts = new double[3];

	private SlideDirection majorAxisSlideDirection;

	// rotate handle size
	static private final double rScale=0.8;
	// translate handle size
	static private final double tScale=0.9;
	// tool transparency
	static private final double alpha=0.8;
	// distance from camera to moving item
	private double cameraDistance=1;
	
	public MoveTool() {
		super();
		setName("MoveTool");
		addEntity(ballSize);
		addEntity(snapOn);
		addEntity(snapDegrees);
		addEntity(snapDistance);
		
		FOR.setIdentity();
				
		isActivelyMoving=false;
	}

	/**
	 * transform a world-space point to the ball's current frame of reference
	 * @param pointInWorldSpace the world space point
	 * @return the transformed {@link Vector3d}
	 */
	private Vector3d getPickPointInFOR(Vector3d pointInWorldSpace,Matrix4d frameOfReference) {
		Matrix4d iMe = new Matrix4d(frameOfReference);
		iMe.m30=iMe.m31=iMe.m32=0;
		iMe.invert();
		Vector3d pickPointInBallSpace = new Vector3d(pointInWorldSpace);
		pickPointInBallSpace.sub(position);
		iMe.transform(pickPointInBallSpace);
		return pickPointInBallSpace;
	}
	
	@Override
	public void update(double dt) {
		if(subject==null) return;

		RobotOverlord ro = (RobotOverlord)getRoot();
		Viewport cameraView = ro.getViewport();

		CameraComponent cameraComponent = ro.getCamera();
		if(cameraComponent==null) return;
		PoseComponent camera = cameraComponent.getEntity().findFirstComponent(PoseComponent.class);

		PoseComponent subjectPose = subject.findFirstComponent(PoseComponent.class);
		if(subjectPose==null) return;

		Matrix4d subjectPoseWorld = subjectPose.getWorld();
		if(!isActivelyMoving()) {
			checkChangeFrameOfReference();
			
			// find the current frame of reference.  This could change every frame as the camera moves.
			switch (FrameOfReference.values()[frameOfReference.get()]) {
				case SUBJECT -> FOR.set(subjectPoseWorld);
				case CAMERA ->
						FOR.set(MatrixHelper.lookAt(camera.getPosition(), MatrixHelper.getPosition(subjectPoseWorld)));
				default -> FOR.setIdentity();
			}
			
			FOR.setTranslation(MatrixHelper.getPosition(subjectPoseWorld));
		}

		Vector3d mp = new Vector3d();
		subjectPoseWorld.get(mp);
		// put the drag ball on the subject
		position.set(mp);
		
		mp.sub(camera.getPosition());
		cameraDistance = mp.length();
		
		// can turn off any time.
		if(!cameraView.isPressed()) isActivelyMoving=false;
		else if(!isActivelyMoving) {
			// user just clicked
			beginMovement();
		} else {
			if(activeMoveIsRotation) updateKBRotation(dt);
			else					 updateKBTranslation(dt);
		}

		if(!isActivelyMoving) {
			updateJoyCon(dt);
		}
	}
	
	private void checkChangeFrameOfReference() {
		if(InputManager.isReleased(InputManager.Source.KEY_F1)) frameOfReference.set(FrameOfReference.WORLD.toInt());
		if(InputManager.isReleased(InputManager.Source.KEY_F2)) frameOfReference.set(FrameOfReference.CAMERA.toInt());
		if(InputManager.isReleased(InputManager.Source.KEY_F3)) frameOfReference.set(FrameOfReference.SUBJECT.toInt());

		if(InputManager.isReleased(InputManager.Source.STICK_DPAD_R)) {
			if(frameOfReference.get()==FrameOfReference.WORLD.toInt()) {
				frameOfReference.set(FrameOfReference.CAMERA.toInt());
			} else if(frameOfReference.get()==FrameOfReference.CAMERA.toInt()) {
				frameOfReference.set(FrameOfReference.SUBJECT.toInt());
			} else if(frameOfReference.get()==FrameOfReference.SUBJECT.toInt()) {
				frameOfReference.set(FrameOfReference.WORLD.toInt());
			}
		}
	}

	private void beginMovement() {
		RobotOverlord ro = (RobotOverlord)getRoot();
		Viewport cameraView = ro.getViewport();
		PoseComponent camera = ro.getCamera().getEntity().findFirstComponent(PoseComponent.class);
		Ray ray = cameraView.rayPick(ro.getCamera());

		Vector3d dp = new Vector3d(position);
		dp.sub(ray.start);
		
		// not moving yet
		// find a pick point on the ball (ray/sphere intersection)
		// https://www.scratchapixel.com/lessons/3d-basic-rendering/minimal-ray-tracer-rendering-simple-shapes/ray-sphere-intersection
		double d = dp.length();
		
		// translation
		// box centers
		Vector3d nx = MatrixHelper.getXAxis(FOR);
		Vector3d ny = MatrixHelper.getYAxis(FOR);
		Vector3d nz = MatrixHelper.getZAxis(FOR);
		
		Vector3d px = new Vector3d(nx);
		Vector3d py = new Vector3d(ny);
		Vector3d pz = new Vector3d(nz);
		px.scale(ballSize.get()*cameraDistance*tScale);
		py.scale(ballSize.get()*cameraDistance*tScale);
		pz.scale(ballSize.get()*cameraDistance*tScale);
		px.add(position);
		py.add(position);
		pz.add(position);

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
		
		pickPoint.set(ray.getPoint(t0));
		
		boolean isHit = (t0>=0 && t0<Double.MAX_VALUE);
		if(isHit) {
			// if hitting and pressed, begin movement.
			isActivelyMoving = true;
			
			pickPointSaved.set(pickPoint);

			PoseComponent subjectPose = subject.findFirstComponent(PoseComponent.class);
			startMatrix = subjectPose.getWorld();
			resultMatrix.set(startMatrix);
			
			valueStart=0;
			valueLast=0;
			valueNow=0;
			
			Matrix4d cm = camera.getWorld();
			Vector3d cr = MatrixHelper.getXAxis(cm);
			Vector3d cu = MatrixHelper.getYAxis(cm);
			// determine which mouse direction is a positive movement on this axis.
			Vector3d nv = switch (majorAxis) {
				case X -> nx;
				case Y -> ny;
				default -> nz;
			};

			double cx,cy;
			cy=cu.dot(nv);
			cx=cr.dot(nv);
			if( Math.abs(cx) > Math.abs(cy) ) {
				majorAxisSlideDirection=(cx>0) ? SlideDirection.SLIDE_XPOS : SlideDirection.SLIDE_XNEG;
			} else {
				majorAxisSlideDirection=(cy>0) ? SlideDirection.SLIDE_YPOS : SlideDirection.SLIDE_YNEG;
			}
			
			activeMoveIsRotation=false;
			return;
		}
		
		// rotation
		double Tca = ray.direction.dot(dp);
		if(Tca>=0) {
			// ball is in front of ray start
			double d2 = d*d - Tca*Tca;
			double r = ballSize.get()*cameraDistance*rScale;
			double r2=r*r;
			boolean isBallHit = d2>=0 && d2<=r2;
			if(isBallHit) {
				// ball hit!  Start move.
				
				double Thc = Math.sqrt(r2 - d2);
				t0 = Tca - Thc;
				//double t1 = Tca + Thc;
				//Log.message("d2="+d2);

				pickPointOnBall = ray.getPoint(t0);

				isActivelyMoving=true;
				PoseComponent subjectPose = subject.findFirstComponent(PoseComponent.class);
				startMatrix = subjectPose.getWorld();
				resultMatrix.set(startMatrix);
				
				Vector3d pickPointInFOR = getPickPointInFOR(pickPointOnBall,FOR);
				
				// find the nearest plane
				dx = Math.abs(pickPointInFOR.x);
				dy = Math.abs(pickPointInFOR.y);
				dz = Math.abs(pickPointInFOR.z);
				nearestPlane=Plane.X;
				double nearestD=dx;
				if(dy<nearestD) {
					nearestPlane=Plane.Y;
					nearestD=dy;
				}
				if(dz<nearestD) {
					nearestPlane=Plane.Z;
					nearestD=dz;
				}

				// https://www.scratchapixel.com/lessons/3d-basic-rendering/minimal-ray-tracer-rendering-simple-shapes/ray-plane-and-ray-disk-intersection
				new Vector3d();
				Vector3d majorAxisVector = switch (nearestPlane) {
					case X -> MatrixHelper.getXAxis(FOR);
					case Y -> MatrixHelper.getYAxis(FOR);
					case Z -> MatrixHelper.getZAxis(FOR);
				};

				// find the pick point on the plane of rotation
				double denominator = ray.direction.dot(majorAxisVector);
				if(denominator!=0) {
					double numerator = dp.dot(majorAxisVector);
					t0 = numerator/denominator;
					pickPoint.set(ray.getPoint(t0));
					pickPointSaved.set(pickPoint);
					
					pickPointInFOR = getPickPointInFOR(pickPoint,FOR);
					switch (nearestPlane) {
						case X -> valueNow = -Math.atan2(pickPointInFOR.y, pickPointInFOR.z);
						case Y -> valueNow = -Math.atan2(pickPointInFOR.z, pickPointInFOR.x);
						case Z -> valueNow = Math.atan2(pickPointInFOR.y, pickPointInFOR.x);
					}
					pickPointInFOR.normalize();
					//Log.message("p="+pickPointInFOR+" valueNow="+Math.toDegrees(valueNow));
				}
				valueStart=valueNow;
				valueLast=valueNow;
			}
			activeMoveIsRotation=true;
		}
	}
	
	private void updateKBRotation(double dt) {
		valueNow = valueLast;

		RobotOverlord ro = (RobotOverlord)getRoot();
		Viewport cameraView = ro.getViewport();
		Ray ray = cameraView.rayPick(ro.getCamera());

		Vector3d dp = new Vector3d(position);
		dp.sub(ray.start);
		
		// https://www.scratchapixel.com/lessons/3d-basic-rendering/minimal-ray-tracer-rendering-simple-shapes/ray-plane-and-ray-disk-intersection
		new Vector3d();
		Vector3d majorAxisVector = switch (nearestPlane) {
			case X -> MatrixHelper.getXAxis(FOR);
			case Y -> MatrixHelper.getYAxis(FOR);
			case Z -> MatrixHelper.getZAxis(FOR);
		};

		// find the pick point on the plane of rotation
		double denominator = ray.direction.dot(majorAxisVector);
		if(denominator!=0) {
			double numerator = dp.dot(majorAxisVector);
			double t0 = numerator/denominator;
			pickPoint.set(ray.getPoint(t0));

			Vector3d pickPointInFOR = getPickPointInFOR(pickPoint,FOR);
			switch (nearestPlane) {
				case X -> valueNow = -Math.atan2(pickPointInFOR.y, pickPointInFOR.z);
				case Y -> valueNow = -Math.atan2(pickPointInFOR.z, pickPointInFOR.x);
				case Z -> valueNow = Math.atan2(pickPointInFOR.y, pickPointInFOR.x);
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
					case X -> rollX(da);
					case Y -> rollY(da);
					case Z -> rollZ(da);
				}
				valueLast = valueStart + da;
				
				attemptMove(ro);
			}
		}
	}

	private void updateKBTranslation(double dt) {
		valueNow = valueLast;
		
		RobotOverlord ro = (RobotOverlord)getRoot();

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
			switch (majorAxis) {
				case X -> translate(MatrixHelper.getXAxis(FOR), dp);
				case Y -> translate(MatrixHelper.getYAxis(FOR), dp);
				case Z -> translate(MatrixHelper.getZAxis(FOR), dp);
			}
			valueLast = valueStart + dp;

			attemptMove(ro);
		}
	}

	// GamePad/JoyStick
	private void updateJoyCon(double dt) {
		if(isActivelyMoving) return;

		RobotOverlord ro = (RobotOverlord)getRoot();

		System.arraycopy(valueLasts, 0, valueNows, 0, valueLasts.length);

		// rotations
		if(InputManager.isOn(InputManager.Source.STICK_CIRCLE)) {
			PoseComponent subjectPose = subject.findFirstComponent(PoseComponent.class);
			startMatrix = subjectPose.getWorld();
			resultMatrix.set(startMatrix);
			valueStart =0;
			valueLast=0;
			valueNow=0;

			double scale = 0.75*dt;  // TODO something better?
			double rawXR= InputManager.getRawValue(InputManager.Source.STICK_LX);
			double rawYR= InputManager.getRawValue(InputManager.Source.STICK_LY);
			double rawZR= InputManager.getRawValue(InputManager.Source.STICK_L2);
			double dxr = rawXR * scale;
			double dyr = rawYR * -scale;
			double dzr = rawZR * scale;

			double diff = Math.abs(rawXR)-Math.abs(rawYR);
			if(diff >= 0.3) {
				valueNow = dxr;
				double dar1 = valueNow - valueStart;
				rollX(dar1);
				valueLast = valueStart+dar1;
				attemptMove(ro);
			} else if(diff <= -0.3) {
				valueNow = dyr;
				double dar1 = valueNow - valueStart;
				rollY(dar1);
				valueLast = valueStart+dar1;
				attemptMove(ro);
			} else if(rawZR!=0) {
				valueNow = dzr;
				double dar1 = valueNow - valueStart;
				rollZ(dar1);
				valueLast = valueStart+dar1;
				attemptMove(ro);
			}			
		}
		
		// translations
		if( InputManager.isOn(InputManager.Source.STICK_X)) {
			PoseComponent subjectPose = subject.findFirstComponent(PoseComponent.class);
			startMatrix = subjectPose.getWorld();
			resultMatrix.set(startMatrix);
			
			for(int i=0; i <3; i++) {
				valueStarts[i]=0;
				valueLasts[i]=0;
				valueNows[i]=0;
			}

			double scale = 50.0*dt;  // TODO something better?
			double rawx= InputManager.getRawValue(InputManager.Source.STICK_LX);
			double rawy= InputManager.getRawValue(InputManager.Source.STICK_LY);
			double rawz= InputManager.getRawValue(InputManager.Source.STICK_L2);
			
			double dx = rawx * scale;
			double dy = rawy * -scale;
			double dz = rawz * scale;
			
			valueNows[0]+=dx;
			valueNows[1]+=dy;
			valueNows[2]+=dz;
			
			double[] dp = new double[3];
			for(int i=0; i < 3; i++) dp[i] = valueNows[i] - valueStarts[i];

			if(dp[0]!=0 || dp[1]!=0 || dp[2]!=0) {
				Matrix3d mat = new Matrix3d();
				mat.setColumn(0, MatrixHelper.getXAxis(FOR));
				mat.setColumn(1, MatrixHelper.getYAxis(FOR));
				mat.setColumn(2, MatrixHelper.getZAxis(FOR));
				translateAll(mat, dp);
				
				for(int i=0; i < 3; i++) valueLasts[i] = valueStarts[i] + dp[i];
				attemptMove(ro);
			}
		}
	}
	
	public void attemptMove(RobotOverlord ro) {
		FOR.setTranslation(MatrixHelper.getPosition(resultMatrix));
		UndoSystem.addEvent(this,new PoseMoveEdit(subject,resultMatrix));
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

		gl2.glDisable(GL2.GL_TEXTURE_2D);

		RobotOverlord ro = (RobotOverlord)getRoot();

		gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT);
		boolean lightWasOn = OpenGLHelper.disableLightingStart(gl2);
		float oldWidth = OpenGLHelper.setLineWidth(gl2, 2);

		gl2.glPushMatrix();

			//PoseComponent pose = subject.getComponent(PoseComponent.class);
			//MatrixHelper.applyMatrix(gl2, pose.getWorld());

			renderOutsideCircle(gl2);
			renderRotation(gl2);
			renderTranslation(gl2);

		gl2.glPopMatrix();

		OpenGLHelper.setLineWidth(gl2, oldWidth);
		OpenGLHelper.disableLightingEnd(gl2, lightWasOn);

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
	
	/**
	 * Render the white and grey circles around the exterior, always facing the camera.
	 * @param gl2 the render context
	 */
	private void renderOutsideCircle(GL2 gl2) {
		final double whiteRadius=1.05;
		final double greyRadius=1.0;
		final int quality=50;
		
		RobotOverlord ro = (RobotOverlord)getRoot();
		PoseComponent camera = ro.getCamera().getEntity().findFirstComponent(PoseComponent.class);
		Matrix4d lookAt = new Matrix4d();

		PoseComponent subjectPose = subject.findFirstComponent(PoseComponent.class);
		Matrix4d pw = subjectPose.getWorld();
		
		Vector3d worldPosition = MatrixHelper.getPosition(pw);
		lookAt.set(MatrixHelper.lookAt(camera.getPosition(), worldPosition));
		lookAt.setTranslation(worldPosition);

		gl2.glPushMatrix();

			MatrixHelper.applyMatrix(gl2, lookAt);
			double d=ballSize.get()*cameraDistance;
			gl2.glScaled(d,d,d);
			
			//white circle on the xy plane of the camera pose, as the subject position
			gl2.glColor4d(1,1,1,0.7);
			PrimitiveSolids.drawCircleXY(gl2, whiteRadius, quality);

			//grey circle on the xy plane of the camera pose, as the subject position
			gl2.glColor4d(0.5,0.5,0.5,0.7);
			PrimitiveSolids.drawCircleXY(gl2, greyRadius, quality);

		gl2.glPopMatrix();
	}

	public void renderRotation(GL2 gl2) {
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, FOR);
			double scale = ballSize.get()*cameraDistance; 
			gl2.glScaled(scale,scale,scale);
		
			// camera forward is +z axis
			//Matrix4d pw = subject.getPoseWorld(pw);
			
			RobotOverlord ro = (RobotOverlord)getRoot();
			PoseComponent camera = ro.getCamera().getEntity().findFirstComponent(PoseComponent.class);
			Matrix4d pw = camera.getWorld();
			pw.m03=
			pw.m13=
			pw.m23=0;
			pw.invert();
	
			double cr = (nearestPlane==Plane.X) ? 1 : 0.5f;
			double cg = (nearestPlane==Plane.Y) ? 1 : 0.5f;
			double cb = (nearestPlane==Plane.Z) ? 1 : 0.5f;
			
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
					double a=0,b=0,c=0;
					double r1=radius0;
					double r2=r1+0.05;
					
					gl2.glBegin(GL2.GL_LINES);
					gl2.glColor3d(1, 1, 1);
					for(double i = 0;i<Math.PI*2;i+=deg) {
						double j=i + start;
						switch(nearestPlane) {
						case X:
							b=Math.cos(j+Math.PI/2);
							c=Math.sin(j+Math.PI/2);
							break;
						case Y:
							a=Math.cos(-j);
							c=Math.sin(-j);
							break;
						case Z:
							a=Math.cos( j);
							b=Math.sin( j);
							break;
						}
						gl2.glVertex3d(a*r1,b*r1,c*r1);
						gl2.glVertex3d(a*r2,b*r2,c*r2);
					}
					gl2.glEnd();
				}
	
				double a=0,b=0,c=0;
				gl2.glBegin(GL2.GL_LINE_LOOP);
				gl2.glColor3f(255,255,255);
				gl2.glVertex3d(0,0,0);
				for(double i=0;i<absRange;i+=0.01) {
					double n = range * (i/absRange) + start;
					
					switch(nearestPlane) {
					case X:
						b=Math.cos(n+Math.PI/2);
						c=Math.sin(n+Math.PI/2);
						break;
					case Y:
						a=Math.cos(-n);
						c=Math.sin(-n);
						break;
					case Z:
						a=Math.cos( n); 
						b=Math.sin( n);
						break;
					}
					gl2.glVertex3d(a*radius0,b*radius0,c*radius0);
				}
				gl2.glEnd();
			}

		gl2.glPopMatrix();
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
			MatrixHelper.applyMatrix(gl2, FOR);
			double scale = ballSize.get()*cameraDistance; 
			gl2.glScaled(scale,scale,scale);
			
			// camera forward is -z axis 
			RobotOverlord ro = (RobotOverlord)getRoot();
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
		Matrix4d ifor = new Matrix4d(FOR);
		ifor.invert();

		Matrix4d subjectRotation = new Matrix4d(startMatrix);		
		Matrix4d subjectAfterRotation = new Matrix4d(FOR);
		subjectAfterRotation.mul(rotation);
		subjectAfterRotation.mul(ifor);
		subjectAfterRotation.mul(subjectRotation);

		resultMatrix.set(subjectAfterRotation);
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
	
	protected void translateAll(Matrix3d v, double[] amount) {
		//Log.message(amount);
		resultMatrix.m03 = startMatrix.m03 + v.m00*amount[0] + v.m01*amount[1] +v.m02*amount[2];
		resultMatrix.m13 = startMatrix.m13 + v.m10*amount[0] + v.m11*amount[1] +v.m12*amount[2];
		resultMatrix.m23 = startMatrix.m23 + v.m20*amount[0] + v.m21*amount[1] +v.m22*amount[2];
	}

	public Matrix4d getResultMatrix() {
		return resultMatrix;
	}
	
	public void setFrameOfReference(FrameOfReference v) {
		frameOfReference.set(v.toInt());
	}
	
	public FrameOfReference getFrameOfReference() {
		return FrameOfReference.values()[frameOfReference.get()];
	}
	
	@Deprecated
	public String getStatusMessage() {
		// translate
		double dx=resultMatrix.m03 - startMatrix.m03; 
		double dy=resultMatrix.m13 - startMatrix.m13; 
		double dz=resultMatrix.m23 - startMatrix.m23;
		double distance = Math.sqrt(dx*dx + dy*dy + dz*dz);
		// rotate
		double start=MathHelper.wrapRadians(valueStart);
		double end=MathHelper.wrapRadians(valueNow);
		double range=Math.toDegrees(end-start);
		range = MathHelper.wrapDegrees(range);
		
		return "twist ["+StringHelper.formatDouble(distance)+"mm,"+StringHelper.formatDouble(range)+"deg]";
	}

	/**
	 * Set which PhysicalEntity the drag ball is going to act upon.
	 * @param subject
	 */
	public void setSubject(Entity subject) {
		this.subject=subject;		
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("MoveTool",true);
		view.add(ballSize);
		view.add(snapOn);
		view.add(snapDegrees);
		view.add(snapDistance);
		
		view.addComboBox(frameOfReference,FrameOfReference.getAll());
		view.popStack();
		
		super.getView(view);
	}
}
