package com.marginallyclever.robotOverlord.entity.scene;

import java.awt.Font;

import javax.swing.event.UndoableEditEvent;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.OpenGLHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.convenience.Ray;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.BooleanEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.IntEntity;
import com.marginallyclever.robotOverlord.swingInterface.InputManager;
import com.marginallyclever.robotOverlord.swingInterface.actions.ActionMoveTo;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * A visual manipulator that facilitates moving objects in 3D.
 * @author Dan Royer
 *
 */
public class MoveTool extends Entity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -189456892380998828L;

	private static final double STEP_SIZE = Math.PI/120.0;
	
	protected TextRenderer textRender = new TextRenderer(new Font("CourrierNew", Font.BOLD, 16));

	public enum SlideDirection {
		SLIDE_XPOS(0,"X+"),
		SLIDE_XNEG(1,"X-"),
		SLIDE_YPOS(2,"Y+"),
		SLIDE_YNEG(3,"Y-");
		
		private int number;
		private String name;
		private SlideDirection(int n,String s) {
			number=n;
			name=s;
		}
		public int toInt() {
			return number;
		}
		public String toString() {
			return name;
		}
		static public String [] getAll() {
			SlideDirection[] allModes = SlideDirection.values();
			String[] labels = new String[allModes.length];
			for(int i=0;i<labels.length;++i) {
				labels[i] = allModes[i].toString();
			}
			return labels;
		}
	};

	public enum FrameOfReference {
		SUBJECT(0,"SUBJECT"),
		CAMERA(1,"CAMERA"),
		WORLD(2,"WORLD");
		
		private int number;
		private String name;
		private FrameOfReference(int n,String s) {
			number=n;
			name=s;
		}
		public int toInt() {
			return number;
		}
		public String toString() {
			return name;
		}
		static public String [] getAll() {
			FrameOfReference[] allModes = FrameOfReference.values();
			String[] labels = new String[allModes.length];
			for(int i=0;i<labels.length;++i) {
				labels[i] = allModes[i].toString();
			}
			return labels;
		}
	}
	
	protected Vector3d position = new Vector3d();
	
	public Vector3d pickPoint=new Vector3d();  // the latest point picked on the ball
	public Vector3d pickPointSaved=new Vector3d();  // the point picked when the action began
	public Vector3d pickPointOnBall=new Vector3d();  // the point picked when the action began
	
	private enum Plane { X,Y,Z };
	private enum Axis { X,Y,Z };

	// for rotation
	private Plane nearestPlane;
	// for translation
	protected Axis majorAxis;
	
	public DoubleEntity ballSize = new DoubleEntity("Scale",20);
	public BooleanEntity snapOn = new BooleanEntity("Snap On",true);
	public DoubleEntity snapDegrees = new DoubleEntity("Snap degrees",5);
	public DoubleEntity snapDistance = new DoubleEntity("Snap mm",1);

	public boolean isRotateMode;
	public boolean isActivelyMoving;
	public boolean isBallHit;

	// Who is being moved?
	protected Moveable subject;
	// In what frame of reference?
	public IntEntity frameOfReference = new IntEntity("Frame of Reference",FrameOfReference.WORLD.toInt());
	
	// matrix of subject when move started
	protected Matrix4d startMatrix=new Matrix4d();	
	protected Matrix4d resultMatrix=new Matrix4d();
	protected Matrix4d FOR=new Matrix4d();
	
	public double valueStart;  // original angle when move started
	public double valueNow;  // current state
	public double valueLast;  // state last frame 
	
	public double[] valueStarts = new double[3];
	public double[] valueNows = new double[3];
	public double[] valueLasts = new double[3];

	public SlideDirection majorAxisSlideDirection;
	
	public MoveTool() {
		super();
		setName("MoveTool");
		addChild(ballSize);
		addChild(snapOn);
		addChild(snapDegrees);
		addChild(snapDistance);
		
		FOR.setIdentity();
				
		isRotateMode=false;
		isActivelyMoving=false;
		isBallHit=false;
	}
	
	@Override
	public void update(double dt) {
		if(subject==null) return;

		RobotOverlord ro = (RobotOverlord)getRoot();
		ViewportEntity cameraView = ro.viewport;
		PoseEntity camera = cameraView.getAttachedTo();

		// find the current frame of reference.  This could change every frame as the camera moves.
		if(!isActivelyMoving()) {
			switch(FrameOfReference.values()[frameOfReference.get()]) {
			case SUBJECT: FOR.set(subject.getPoseWorld());	break;
			case CAMERA : FOR.set(MatrixHelper.lookAt(camera.getPosition(), MatrixHelper.getPosition(subject.getPoseWorld())));  break;
			default     : FOR.setIdentity();  break;
			}
			FOR.setTranslation(MatrixHelper.getPosition(subject.getPoseWorld()));
		}

		// apply the effect of drag actions
		if(!isActivelyMoving()) {
			//setRotateMode(InputManager.isOn(InputManager.Source.STICK_CIRCLE));

			if(InputManager.isReleased(InputManager.Source.KEY_LSHIFT)
			|| InputManager.isReleased(InputManager.Source.KEY_RSHIFT)) setRotateMode(!getRotateMode());
			if(InputManager.isReleased(InputManager.Source.KEY_F1)) frameOfReference.set(FrameOfReference.WORLD.toInt());
			if(InputManager.isReleased(InputManager.Source.KEY_F2)) frameOfReference.set(FrameOfReference.CAMERA.toInt());
			if(InputManager.isReleased(InputManager.Source.KEY_F3)) frameOfReference.set(FrameOfReference.SUBJECT.toInt());
			
			if(InputManager.isReleased(InputManager.Source.STICK_DPAD_R)) {
				if(frameOfReference.get()==FrameOfReference.WORLD.toInt()) frameOfReference.set(FrameOfReference.CAMERA.toInt());
				else if(frameOfReference.get()==FrameOfReference.CAMERA.toInt()) frameOfReference.set(FrameOfReference.SUBJECT.toInt());
				else if(frameOfReference.get()==FrameOfReference.SUBJECT.toInt()) frameOfReference.set(FrameOfReference.WORLD.toInt());
			}
			
		} else {
			if(InputManager.isReleased(InputManager.Source.KEY_ESCAPE)) {
				// cancel this move
				isActivelyMoving=false;
				resultMatrix.set(startMatrix);
			}
		}

		Vector3d mp = new Vector3d();
		subject.getPoseWorld().get(mp);
		// put the dragball on the subject
		position.set(mp);
		
		if(isRotateMode) {
			updateRotation(dt);
		} else {
			updateTranslation(dt);
		}
	}
	
	/**
	 * transform a world-space point to the ball's current frame of reference
	 * @param pointInWorldSpace the world space point
	 * @return the transformed Vector3d
	 */
	public Vector3d getPickPointInFOR(Vector3d pointInWorldSpace,Matrix4d frameOfReference) {
		Matrix4d iMe = new Matrix4d(frameOfReference);
		iMe.m30=iMe.m31=iMe.m32=0;
		iMe.invert();
		Vector3d pickPointInBallSpace = new Vector3d(pointInWorldSpace);
		pickPointInBallSpace.sub(position);
		iMe.transform(pickPointInBallSpace);
		return pickPointInBallSpace;
	}
	
	public void updateRotation(double dt) {
		valueNow = valueLast;

		RobotOverlord ro = (RobotOverlord)getRoot();
		ViewportEntity cameraView = ro.viewport;
		Ray ray = cameraView.rayPick();

		Vector3d dp = new Vector3d(position);
		dp.sub(ray.start);
		
		if(!isActivelyMoving) {
			// not moving yet
			// find a pick point on the ball (ray/sphere intersection)
			// https://www.scratchapixel.com/lessons/3d-basic-rendering/minimal-ray-tracer-rendering-simple-shapes/ray-sphere-intersection
			double d = dp.length();
			
			double Tca = ray.direction.dot(dp);
			if(Tca>=0) {
				// ball is in front of ray start
				double d2 = d*d - Tca*Tca;
				double r2 = ballSize.get() * ballSize.get();
				isBallHit = d2>=0 && d2<=r2;
				if(isBallHit) {
					double Thc = Math.sqrt(r2 - d2);
					double t0 = Tca - Thc;
					//double t1 = Tca + Thc;
					//Log.message("d2="+d2);

					pickPointOnBall = ray.getPoint(t0);

					if( cameraView.isPressed() ) {
						// ball hit!  Start moving.
						isActivelyMoving=true;
						startMatrix.set(subject.getPoseWorld());
						resultMatrix.set(startMatrix);
						
						Vector3d pickPointInFOR = getPickPointInFOR(pickPointOnBall,FOR);
						
						// find nearest plane
						double dx = Math.abs(pickPointInFOR.x);
						double dy = Math.abs(pickPointInFOR.y);
						double dz = Math.abs(pickPointInFOR.z);
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
						Vector3d majorAxisVector = new Vector3d();
						switch(nearestPlane) {
						case X:  majorAxisVector = MatrixHelper.getXAxis(FOR);	break;
						case Y:  majorAxisVector = MatrixHelper.getYAxis(FOR);	break;
						case Z:  majorAxisVector = MatrixHelper.getZAxis(FOR);	break;
						}
						
						// find the pick point on the plane of rotation
						double denominator = ray.direction.dot(majorAxisVector);
						if(denominator!=0) {
							double numerator = dp.dot(majorAxisVector);
							t0 = numerator/denominator;
							pickPoint.set(ray.getPoint(t0));
							pickPointSaved.set(pickPoint);
							
							pickPointInFOR = getPickPointInFOR(pickPoint,FOR);
							switch(nearestPlane) {
							case X:  valueNow = -Math.atan2(pickPointInFOR.y, pickPointInFOR.z);	break;
							case Y:  valueNow = -Math.atan2(pickPointInFOR.z, pickPointInFOR.x);	break;
							case Z:  valueNow =  Math.atan2(pickPointInFOR.y, pickPointInFOR.x);	break;
							}
							pickPointInFOR.normalize();
							//Log.message("p="+pickPointInFOR+" valueNow="+Math.toDegrees(valueNow));
						}
						valueStart=valueNow;
						valueLast=valueNow;
					}
				}
			}
		}
		
		// can turn off any time.
		if(isActivelyMoving && !cameraView.isPressed()) {
			isActivelyMoving=false;
		}

		if(isActivelyMoving) {
			// https://www.scratchapixel.com/lessons/3d-basic-rendering/minimal-ray-tracer-rendering-simple-shapes/ray-plane-and-ray-disk-intersection
			Vector3d majorAxisVector = new Vector3d();
			switch(nearestPlane) {
			case X:  majorAxisVector = MatrixHelper.getXAxis(FOR);	break;
			case Y:  majorAxisVector = MatrixHelper.getYAxis(FOR);	break;
			case Z:  majorAxisVector = MatrixHelper.getZAxis(FOR);	break;
			}
			
			// find the pick point on the plane of rotation
			double denominator = ray.direction.dot(majorAxisVector);
			if(denominator!=0) {
				double numerator = dp.dot(majorAxisVector);
				double t0 = numerator/denominator;
				pickPoint.set(ray.getPoint(t0));

				Vector3d pickPointInFOR = getPickPointInFOR(pickPoint,FOR);
				switch(nearestPlane) {
				case X:  valueNow = -Math.atan2(pickPointInFOR.y, pickPointInFOR.z);	break;
				case Y:  valueNow = -Math.atan2(pickPointInFOR.z, pickPointInFOR.x);	break;
				case Z:  valueNow =  Math.atan2(pickPointInFOR.y, pickPointInFOR.x);	break;
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
					switch(nearestPlane) {
					case X: rollX(da);	break;
					case Y: rollY(da);	break;
					case Z: rollZ(da);	break;
					}
					valueLast = valueStart + da;
					
					attemptMove(ro);
				}
			}
		}
		if(InputManager.isOn(InputManager.Source.STICK_CIRCLE)) {
			
			startMatrix.set(subject.getPoseWorld());
			resultMatrix.set(startMatrix);
			valueStart =0;
			valueLast=0;
			valueNow=0;

			double scale = 0.75*dt;  // TODO something better?
			double rawxr= InputManager.getRawValue(InputManager.Source.STICK_LX);
			double rawyr= InputManager.getRawValue(InputManager.Source.STICK_LY);
			double rawzr= InputManager.getRawValue(InputManager.Source.STICK_L2);
			double dxr = rawxr * scale;
			double dyr = rawyr * -scale;
			double dzr = rawzr * scale;

			if(Math.abs(rawxr)-Math.abs(rawyr) >= 0.3) {
				valueNow = dxr;
				double dar1 = valueNow - valueStart;
				rollX(dar1);
				valueLast = valueStart+dar1;
				attemptMove(ro);
			} else if(Math.abs(rawxr)-Math.abs(rawyr) <= -0.3) {
				valueNow = dyr;
				double dar1 = valueNow - valueStart;
				rollY(dar1);
				valueLast = valueStart+dar1;
				attemptMove(ro);
			} else if(rawzr!=0) {
				valueNow = dzr;
				double dar1 = valueNow - valueStart;
				rollZ(dar1);
				valueLast = valueStart+dar1;
				attemptMove(ro);
			}			
		}
	}
	

	public void attemptMove(RobotOverlord ro) {
		if(subject.canYouMoveTo(resultMatrix)) {
			FOR.setTranslation(MatrixHelper.getPosition(resultMatrix));
			ro.undoableEditHappened(new UndoableEditEvent(this,new ActionMoveTo(subject,resultMatrix) ) );
		}
	}
	
	
	public void updateTranslation(double dt) {
		valueNow = valueLast;
		valueNows = valueLasts;

		RobotOverlord ro = (RobotOverlord)getRoot();
		ViewportEntity cameraView = ro.viewport;
		PoseEntity camera = cameraView.getAttachedTo();
		
		if(!isActivelyMoving && cameraView.isPressed()) {
			// box centers
			Vector3d nx = MatrixHelper.getXAxis(FOR);
			Vector3d ny = MatrixHelper.getYAxis(FOR);
			Vector3d nz = MatrixHelper.getZAxis(FOR);
			
			Vector3d px = new Vector3d(nx);
			Vector3d py = new Vector3d(ny);
			Vector3d pz = new Vector3d(nz);
			px.scale(ballSize.get());
			py.scale(ballSize.get());
			pz.scale(ballSize.get());
			px.add(position);
			py.add(position);
			pz.add(position);

			Ray ray = cameraView.rayPick();
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
				startMatrix.set(subject.getPoseWorld());
				resultMatrix.set(startMatrix);
				
				valueStart=0;
				valueLast=0;
				valueNow=0;
				
				Matrix4d cm=camera.getPose();
				Vector3d cr = MatrixHelper.getXAxis(cm);
				Vector3d cu = MatrixHelper.getYAxis(cm);
				// determine which mouse direction is a positive movement on this axis.
				Vector3d nv;
				switch(majorAxis) {
				case X: nv = nx;  break;
				case Y: nv = ny;  break;
				default: nv = nz;  break;
				}
				
				double cx,cy;
				cy=cu.dot(nv);
				cx=cr.dot(nv);
				if( Math.abs(cx) > Math.abs(cy) ) {
					majorAxisSlideDirection=(cx>0) ? SlideDirection.SLIDE_XPOS : SlideDirection.SLIDE_XNEG;
				} else {
					majorAxisSlideDirection=(cy>0) ? SlideDirection.SLIDE_YPOS : SlideDirection.SLIDE_YNEG;
				}
			}
		}

		// can turn off any time.
		if(isActivelyMoving && !cameraView.isPressed()) {
			isActivelyMoving=false;
		}

		if(isActivelyMoving) {
			// actively being dragged
			double scale = 5.0*dt;  // TODO something better?
			double rawx= InputManager.getRawValue(InputManager.Source.MOUSE_X);
			double rawy= InputManager.getRawValue(InputManager.Source.MOUSE_Y);
			double dx = rawx *  scale;
			double dy = rawy * -scale;
			
			switch(majorAxisSlideDirection) {
			case SLIDE_XPOS:  valueNow+=dx;	break;
			case SLIDE_XNEG:  valueNow-=dx;	break;
			case SLIDE_YPOS:  valueNow+=dy;	break;
			case SLIDE_YNEG:  valueNow-=dy;	break;
			}
			
			double dp = valueNow - valueStart;
			if(snapOn.get()) {
				// round to nearest mm
				double mm = snapDistance.get()*0.1;
				if( InputManager.isOn(InputManager.Source.KEY_RCONTROL) ||
					InputManager.isOn(InputManager.Source.KEY_LCONTROL) ) {
					mm *= 0.1;
				}
				dp = Math.signum(dp)*Math.round(Math.abs(dp)/mm)*mm;
			}
			//Log.message("dt="+dt+"\trawx="+rawx+"\trawy="+rawy+"\tdx="+dx+"\tdy="+dy+"\ttran="+dp);
			if(dp!=0) {
				switch(majorAxis) {
				case X: translate(MatrixHelper.getXAxis(FOR), dp);	break;
				case Y: translate(MatrixHelper.getYAxis(FOR), dp);	break;
				case Z: translate(MatrixHelper.getZAxis(FOR), dp);	break;
				}
				valueLast = valueStart + dp;

				attemptMove(ro);
			}
		} else {
			// GamePad/JoyStick
			if( InputManager.isOn(InputManager.Source.STICK_X)) {
								
				startMatrix.set(subject.getPoseWorld());
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
	}

	protected double testBoxHit(Ray ray,Vector3d n) {		
		Point3d b0 = new Point3d(); 
		Point3d b1 = new Point3d();

		b0.set(+0.05,+0.05,+0.05);
		b1.set(-0.05,-0.05,-0.05);
		b0.scale(ballSize.get());
		b1.scale(ballSize.get());
		b0.add(n);
		b1.add(n);
		
		return MathHelper.rayBoxIntersection(ray,b0,b1);
	}
	
	@Override
	public void render(GL2 gl2) {
		if(subject==null) return;

		gl2.glDisable(GL2.GL_TEXTURE_2D);

		RobotOverlord ro = (RobotOverlord)getRoot();
		ro.viewport.renderChosenProjection(gl2);

		gl2.glPushMatrix();
		
			/*if(isBallHit) {
				Vector3d dp = this.getPosition();
				
				ViewportEntity cameraView = ro.viewport;
				Ray ray = cameraView.rayPick();
				Vector3d dr = ray.getPoint(100);

				gl2.glBegin(GL2.GL_LINES);
				gl2.glColor3d(1, 1, 1);
				gl2.glVertex3d(ray.start.x, ray.start.y, ray.start.z);
				gl2.glVertex3d(dr.x, dr.y, dr.z);

				gl2.glVertex3d(dp.x,dp.y,dp.z);
				gl2.glVertex3d(pickPointOnBall.x, pickPointOnBall.y, pickPointOnBall.z);
				gl2.glEnd();
				
				PrimitiveSolids.drawStar(gl2, dp,10);
				PrimitiveSolids.drawStar(gl2, pickPointOnBall,10);
			}//*/
			
			int previousState = OpenGLHelper.drawAtopEverythingStart(gl2);
			boolean lightWasOn = OpenGLHelper.disableLightingStart(gl2);
			float oldWidth = OpenGLHelper.setLineWidth(gl2, 2);

			gl2.glPushMatrix();
	
				renderOutsideCircle(gl2);

				MatrixHelper.applyMatrix(gl2, FOR);
				gl2.glScaled(ballSize.get(),ballSize.get(),ballSize.get());

				if(isRotateMode()) {
					renderRotation(gl2);
				} else {
					renderTranslation(gl2);
				}
				
			gl2.glPopMatrix();

			OpenGLHelper.setLineWidth(gl2, oldWidth);
			OpenGLHelper.disableLightingEnd(gl2, lightWasOn);
			OpenGLHelper.drawAtopEverythingEnd(gl2, previousState);
			
		gl2.glPopMatrix();
		
		gl2.glEnable(GL2.GL_TEXTURE_2D);
		
		//ro.viewport.renderOrtho(gl2);
		textRender.beginRendering(ro.viewport.getCanvasWidth(), ro.viewport.getCanvasHeight());
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
	 * @param gl2
	 */
	private void renderOutsideCircle(GL2 gl2) {
		final double whiteRadius=1.05;
		final double greyRadius=1.0;
		final int quality=50;
		
		RobotOverlord ro = (RobotOverlord)getRoot();
		PoseEntity camera = ro.viewport.getAttachedTo();
		Matrix4d lookAt = new Matrix4d();
		
		Vector3d wp = MatrixHelper.getPosition(subject.getPoseWorld());
		lookAt.set(MatrixHelper.lookAt(camera.getPosition(), wp));
		lookAt.setTranslation(wp);

		gl2.glPushMatrix();

			MatrixHelper.applyMatrix(gl2, lookAt);
			double d=ballSize.get();
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
		
		// camera forward is +z axis 
		RobotOverlord ro = (RobotOverlord)getRoot();
		PoseEntity camera = ro.viewport.getAttachedTo();
		Matrix4d lookAt = MatrixHelper.lookAt(camera.getPosition(), MatrixHelper.getPosition(subject.getPoseWorld()));
		Vector3d lookAtVector = MatrixHelper.getZAxis(lookAt);
		
		Matrix4d cpw = camera.getPoseWorld();
		cpw.m03=
		cpw.m13=
		cpw.m23=0;
		cpw.invert();

		double cr = (nearestPlane==Plane.X) ? 1 : 0.5f;
		double cg = (nearestPlane==Plane.Y) ? 1 : 0.5f;
		double cb = (nearestPlane==Plane.Z) ? 1 : 0.5f;

		// is a FOR axis normal almost the same as camera forward?
		double vX=Math.abs(lookAtVector.dot(MatrixHelper.getXAxis(FOR)));
		double vY=Math.abs(lookAtVector.dot(MatrixHelper.getYAxis(FOR)));
		double vZ=Math.abs(lookAtVector.dot(MatrixHelper.getZAxis(FOR)));
		boolean drawX = (vX>0.85);
		boolean drawY = (vY>0.85);
		boolean drawZ = (vZ>0.85);
		//Log.message(vX+"\t"+drawX+"\t"+vY+"\t"+drawY+"\t"+vZ+"\t"+drawZ);

		gl2.glEnable(GL2.GL_CULL_FACE);
		gl2.glCullFace(GL2.GL_BACK);
		//x
		gl2.glColor3d(cr, 0, 0);
		if(drawX) {
			gl2.glBegin(GL2.GL_LINE_STRIP);
			for(double n=0;n<Math.PI*4;n+=STEP_SIZE) {
				gl2.glVertex3d(0,Math.cos(n),Math.sin(n));
			}
			gl2.glEnd();
		} else {
			gl2.glBegin(GL2.GL_TRIANGLE_STRIP);
			for(double n=0;n<Math.PI*4;n+=STEP_SIZE) {
				double x=Math.cos(n);
				double y=Math.sin(n);
				gl2.glNormal3d(0, y,x);
				gl2.glVertex3d(-0.25/ballSize.get(),y,x);
				gl2.glVertex3d( 0.25/ballSize.get(),y,x);
			}
			gl2.glEnd();
		}

		//y
		gl2.glColor3d(0, cg, 0);
		if(drawY) {
			gl2.glBegin(GL2.GL_LINE_STRIP);
			for(double n=0;n<Math.PI*4;n+=STEP_SIZE) {
				gl2.glVertex3d(Math.cos(n),0,Math.sin(n));
			}
			gl2.glEnd();
		} else {
			gl2.glBegin(GL2.GL_TRIANGLE_STRIP);
			for(double n=0;n<Math.PI*4;n+=STEP_SIZE) {
				double x=Math.cos(n);
				double y=Math.sin(n);
				gl2.glNormal3d(x,0,y);
				gl2.glVertex3d(x,-0.25/ballSize.get(),y);
				gl2.glVertex3d(x, 0.25/ballSize.get(),y);
			}
			gl2.glEnd();
		}
		
		//z
		gl2.glColor3d(0, 0, cb);
		if(drawZ) {
			gl2.glBegin(GL2.GL_LINE_STRIP);
			for(double n=0;n<Math.PI*4;n+=STEP_SIZE) {
				gl2.glVertex3d(Math.sin(n),Math.cos(n),0);
			}
			gl2.glEnd();
		} else {
			gl2.glBegin(GL2.GL_TRIANGLE_STRIP);
			for(double n=0;n<Math.PI*4;n+=STEP_SIZE) {
				double x=Math.cos(n);
				double y=Math.sin(n);
				gl2.glNormal3d(y,x,0);
				gl2.glVertex3d(y,x,-0.25/ballSize.get());
				gl2.glVertex3d(y,x, 0.25/ballSize.get());
			}
			gl2.glEnd();
		}

		
		if(isActivelyMoving) {
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
				double r1=1;
				double r2=1.05;
				
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
			
			gl2.glBegin(GL2.GL_LINE_LOOP);
			gl2.glColor3f(255,255,255);
			gl2.glVertex3d(0,0,0);
			for(double i=0;i<absRange;i+=0.01) {
				double n = range * (i/absRange) + start;
				
				switch(nearestPlane) {
				case X: gl2.glVertex3d(0,Math.cos(n+Math.PI/2),Math.sin(n+Math.PI/2));  break;
				case Y: gl2.glVertex3d(Math.cos(-n),0,Math.sin(-n));  break;
				case Z: gl2.glVertex3d(Math.cos( n),Math.sin( n),0);  break;
				}
			}
			gl2.glEnd();
		}

		gl2.glPopMatrix();
	}
	
	public void renderTranslation(GL2 gl2) {
		// camera forward is -z axis 
		RobotOverlord ro = (RobotOverlord)getRoot();
		PoseEntity camera = ro.viewport.getAttachedTo();
		Vector3d lookAtVector = MatrixHelper.getPosition(subject.getPoseWorld());
		lookAtVector.sub(camera.getPosition());
		lookAtVector.normalize();
	
		float r = (majorAxis==Axis.X) ? 1 : 0.5f;
		float g = (majorAxis==Axis.Y) ? 1 : 0.5f;
		float b = (majorAxis==Axis.Z) ? 1 : 0.5f;

		Vector3d fx = MatrixHelper.getXAxis(FOR);
		Vector3d fy = MatrixHelper.getYAxis(FOR);
		Vector3d fz = MatrixHelper.getZAxis(FOR);
		// should we hide an axis if it points almost the same direction as the camera?
		boolean drawX = (Math.abs(lookAtVector.dot(fx))<0.95);
		boolean drawY = (Math.abs(lookAtVector.dot(fy))<0.95);
		boolean drawZ = (Math.abs(lookAtVector.dot(fz))<0.95);

		if(drawX) {
			gl2.glColor3f(r,0,0);
			renderTranslationHandle(gl2,new Vector3d(1,0,0));
		}
		if(drawY) {
			gl2.glColor3f(0,g,0);
			renderTranslationHandle(gl2,new Vector3d(0,1,0));
		}
		if(drawZ) {
			gl2.glColor3f(0,0,b);
			renderTranslationHandle(gl2,new Vector3d(0,0,1));
		}
		
		if(drawX && drawY) {
			// handle for XY plane
			gl2.glColor4f(r,g,0,1);
			gl2.glBegin(GL2.GL_QUADS);
			gl2.glVertex3d(0, 0, 0);
			gl2.glVertex3d(0.15, 0, 0);
			gl2.glVertex3d(0.15, 0.15, 0);
			gl2.glVertex3d(0, 0.15, 0);
			gl2.glEnd();
			
		}
		if(drawX && drawZ) {
			// handle for XZ plane
			gl2.glColor4f(r,0,b,1);
			gl2.glBegin(GL2.GL_QUADS);
			gl2.glVertex3d(0, 0, 0);
			gl2.glVertex3d(0.15, 0, 0);
			gl2.glVertex3d(0.15, 0, 0.15);
			gl2.glVertex3d(0, 0, 0.15);
			gl2.glEnd();
		}
		if(drawY && drawZ) {
			// handle for YZ plane
			gl2.glColor4f(0,g,b,1);
			gl2.glBegin(GL2.GL_QUADS);
			gl2.glVertex3d(0, 0, 0);
			gl2.glVertex3d(0, 0, 0.15);
			gl2.glVertex3d(0, 0.15, 0.15);
			gl2.glVertex3d(0, 0.15, 0);
			gl2.glEnd();
		}
		
		if(isActivelyMoving) {
			// the distance we tried to move.
			gl2.glBegin(GL2.GL_LINES);
			gl2.glColor3f(255,255,255);
			gl2.glVertex3d(0,0,0);
			Matrix4d sw = subject.getPoseWorld();
			
			gl2.glVertex3d(
					(startMatrix.m03-sw.m03)/ballSize.get(),
					(startMatrix.m13-sw.m13)/ballSize.get(),
					(startMatrix.m23-sw.m23)/ballSize.get());
			gl2.glEnd();
		}
	}
	
	protected void renderTranslationHandle(GL2 gl2,Vector3d n) {
		// draw line to box
		gl2.glBegin(GL2.GL_LINES);
		gl2.glVertex3d(0,0,0);
		gl2.glVertex3d(n.x,n.y,n.z);
		gl2.glEnd();

		// draw box
		Point3d b0 = new Point3d(+0.05,+0.05,+0.05); 
		Point3d b1 = new Point3d(-0.05,-0.05,-0.05);

		b0.scale(1);
		b1.scale(1);
		b0.add(n);
		b1.add(n);
		PrimitiveSolids.drawBox(gl2, b0,b1);
	}

	public boolean isRotateMode() {
		return isRotateMode;
	}

	public void setRotateMode(boolean isRotateMode) {
		if(isActivelyMoving) return;
		// only allow change when not actively moving.
		this.isRotateMode = isRotateMode;
	}
	
	public boolean getRotateMode() {
		return isRotateMode;
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
		if(isRotateMode) {
			double start=MathHelper.wrapRadians(valueStart);
			double end=MathHelper.wrapRadians(valueNow);
			double range=Math.toDegrees(end-start);
			range = MathHelper.wrapDegrees(range);
			return "turn "+StringHelper.formatDouble(range)+" degrees";
			
		} else {  // translate mode
			double dx=resultMatrix.m03 - startMatrix.m03; 
			double dy=resultMatrix.m13 - startMatrix.m13; 
			double dz=resultMatrix.m23 - startMatrix.m23;

			double distance = Math.sqrt(dx*dx + dy*dy + dz*dz);
			return "slide "+StringHelper.formatDouble(distance)+"mm";
		}
	}

	/**
	 * Set which PhysicalEntity the drag ball is going to act upon.
	 * @param subject
	 */
	public void setSubject(Moveable subject) {
		this.subject=subject;		
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Db", "Dragball");
		view.add(ballSize);
		view.add(snapOn);
		view.add(snapDegrees);
		view.add(snapDistance);
		
		view.addComboBox(frameOfReference,FrameOfReference.getAll());
	}
}
