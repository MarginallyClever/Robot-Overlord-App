package com.marginallyclever.robotOverlord.swingInterface;

import java.nio.IntBuffer;

import javax.swing.event.UndoableEditEvent;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.IntEntity;
import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;
import com.marginallyclever.robotOverlord.swingInterface.actions.ActionPoseEntityMoveWorld;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * A visual manipulator that facilitates moving objects in 3D.
 * @author Dan Royer
 *
 */
public class DragBallEntity extends PoseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8786413898168510187L;
	private static final double STEP_SIZE = Math.PI/120.0;

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
	
	public Vector3d pickPoint=new Vector3d();  // the latest point picked on the ball
	public Vector3d pickPointSaved=new Vector3d();  // the point picked when the action began
	public Vector3d pickPointOnBall=new Vector3d();  // the point picked when the action began
	
	private enum Plane { X,Y,Z };
	private enum Axis { X,Y,Z };

	// for rotation
	private Plane nearestPlane;
	// for translation
	protected Axis majorAxis;
	
	public DoubleEntity ballSize = new DoubleEntity("Scale",0.2);
	public double ballSizeScaled;

	public boolean isRotateMode;
	public boolean isActivelyMoving;

	// Who is being moved?
	protected PoseEntity subject;
	// In what frame of reference?
	public IntEntity frameOfReference = new IntEntity("Frame of Reference",FrameOfReference.WORLD.toInt());
	
	// matrix of subject when move started
	protected Matrix4d startMatrix=new Matrix4d();	
	protected Matrix4d resultMatrix=new Matrix4d();
	protected Matrix4d FOR=new Matrix4d();
	
	public double valueStart;  // original angle when move started
	public double valueNow;  // current state
	public double valueLast;  // state last frame 

	public SlideDirection majorAxisSlideDirection;
	
	public DragBallEntity() {
		super();
		setName("DragBall");
		addChild(ballSize);
		
		FOR.setIdentity();
				
		isRotateMode=false;
		isActivelyMoving=false;
	}
	
	@Override
	public void update(double dt) {
		if(subject==null) return;

		RobotOverlord ro = (RobotOverlord)getRoot();
		PoseEntity camera = ro.viewport.getAttachedTo();

		// find the current frame of reference.  This could change every frame as the camera moves.
		if(!isActivelyMoving()) {
			switch(FrameOfReference.values()[frameOfReference.get()]) {
			case SUBJECT: FOR.set(subject.getPoseWorld());	break;
			case CAMERA : FOR.set(MatrixHelper.lookAt(camera.getPosition(), subject.getPosition()));  break;
			default     : FOR.setIdentity();  break;
			}
		}
		FOR.setTranslation(MatrixHelper.getPosition(subject.getPoseWorld()));

		// apply the effect of drag actions
		if(!isActivelyMoving()) {
			setRotateMode(InputManager.isOn(InputManager.Source.KEY_LSHIFT)
						|| InputManager.isOn(InputManager.Source.KEY_RSHIFT));
	
			if(InputManager.isReleased(InputManager.Source.KEY_F1)) frameOfReference.set(FrameOfReference.WORLD.toInt());
			if(InputManager.isReleased(InputManager.Source.KEY_F2)) frameOfReference.set(FrameOfReference.CAMERA.toInt());
			if(InputManager.isReleased(InputManager.Source.KEY_F3)) frameOfReference.set(FrameOfReference.SUBJECT.toInt());
		} else {
			if(InputManager.isReleased(InputManager.Source.KEY_ESCAPE)) {
				// cancel this move
				isActivelyMoving=false;
				resultMatrix.set(startMatrix);
			}
		}

		Vector3d mp = new Vector3d();
		subject.getPoseWorld().get(mp);
		this.setPosition(mp);
		mp.sub(camera.getPosition());

		double d = mp.length();
		ballSizeScaled=ballSize.get()*d;
				
		if(isRotateMode) {
			updateRotation(dt,d,mp);
		} else {
			updateTranslation(dt,d,mp);
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
		pickPointInBallSpace.sub(this.getPosition());
		iMe.transform(pickPointInBallSpace);
		return pickPointInBallSpace;
	}
	
	public void updateRotation(double dt,double d,Vector3d mp) {
		valueNow = valueLast;

		RobotOverlord ro = (RobotOverlord)getRoot();
		ViewportEntity cameraView = ro.viewport;
		PoseEntity camera = cameraView.getAttachedTo();
		Vector3d ray = cameraView.rayPick();
		
		if(!isActivelyMoving && cameraView.isPressed()) {
			// not moving yet, mouse clicked.

			// find a pick point on the ball (ray/sphere intersection)
			double Tca = ray.dot(mp);
			if(Tca>=0) {
				double d2 = Math.sqrt(d*d - Tca*Tca);
				boolean isHit = d2>=0 && d2<=ballSizeScaled;
				if(isHit) {
					// ball hit!  Start moving.
					isActivelyMoving=true;
					double Thc = Math.sqrt(ballSizeScaled*ballSizeScaled - d2*d2);
					double t0 = Tca - Thc;
					//double t1 = Tca + Thc;
					//System.out.println("d2="+d2);

					pickPointOnBall = new Vector3d(
							camera.getPosition().x+ray.x*t0,
							camera.getPosition().y+ray.y*t0,
							camera.getPosition().z+ray.z*t0);
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
					double denominator = ray.dot(majorAxisVector);
					if(denominator!=0) {
						double numerator = mp.dot(majorAxisVector);
						t0 = numerator/denominator;
						pickPoint.set(
								camera.getPosition().x+ray.x*t0,
								camera.getPosition().y+ray.y*t0,
								camera.getPosition().z+ray.z*t0);
						pickPointSaved.set(pickPoint);
						
						pickPointInFOR = getPickPointInFOR(pickPoint,FOR);
						switch(nearestPlane) {
						case X:  valueNow = -Math.atan2(pickPointInFOR.y, pickPointInFOR.z);	break;
						case Y:  valueNow = -Math.atan2(pickPointInFOR.z, pickPointInFOR.x);	break;
						case Z:  valueNow =  Math.atan2(pickPointInFOR.y, pickPointInFOR.x);	break;
						}
						pickPointInFOR.normalize();
						//System.out.println("p="+pickPointInFOR+" valueNow="+Math.toDegrees(valueNow));
					}
					valueStart=valueNow;
					valueLast=valueNow;
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
			double denominator = ray.dot(majorAxisVector);
			if(denominator!=0) {
				double numerator = mp.dot(majorAxisVector);
				double t0 = numerator/denominator;
				pickPoint.set(
						camera.getPosition().x+ray.x*t0,
						camera.getPosition().y+ray.y*t0,
						camera.getPosition().z+ray.z*t0);

				Vector3d pickPointInFOR = getPickPointInFOR(pickPoint,FOR);
				switch(nearestPlane) {
				case X:  valueNow = -Math.atan2(pickPointInFOR.y, pickPointInFOR.z);	break;
				case Y:  valueNow = -Math.atan2(pickPointInFOR.z, pickPointInFOR.x);	break;
				case Z:  valueNow =  Math.atan2(pickPointInFOR.y, pickPointInFOR.x);	break;
				}

				double da=valueNow - valueStart;

				if(da!=0) {
					switch(nearestPlane) {
					case X: rollX(da);	break;
					case Y: rollY(da);	break;
					case Z: rollZ(da);	break;
					}
				}
				//System.out.println(da);
				
				valueLast = valueNow;
				
				if(subject.canYouMoveTo(resultMatrix)) {
					ro.undoableEditHappened(new UndoableEditEvent(this,new ActionPoseEntityMoveWorld(subject,resultMatrix) ) );
				}
			}
		}
	}
	
	public void updateTranslation(double dt,double d,Vector3d mp) {
		valueNow = valueLast;

		RobotOverlord ro = (RobotOverlord)getRoot();
		ViewportEntity cameraView = ro.viewport;
		PoseEntity camera = cameraView.getAttachedTo();
		
		if(!isActivelyMoving && cameraView.isPressed()) {	
			Vector3d pos = this.getPosition();
			
			// box centers
			Vector3d nx = MatrixHelper.getXAxis(FOR);
			Vector3d ny = MatrixHelper.getYAxis(FOR);
			Vector3d nz = MatrixHelper.getZAxis(FOR);
			
			Vector3d px = new Vector3d(nx);
			Vector3d py = new Vector3d(ny);
			Vector3d pz = new Vector3d(nz);
			px.scale(ballSizeScaled);
			py.scale(ballSizeScaled);
			pz.scale(ballSizeScaled);
			px.add(pos);
			py.add(pos);
			pz.add(pos);
	
			// of the three boxes, the closest hit is the one to remember.			
			double dx = testBoxHit(camera.getPosition(),cameraView.rayPick(),px);
			double dy = testBoxHit(camera.getPosition(),cameraView.rayPick(),py);
			double dz = testBoxHit(camera.getPosition(),cameraView.rayPick(),pz);
			
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
			
			Vector3d ray=new Vector3d(cameraView.rayPick());
			pickPoint.set(
				camera.getPosition().x+ray.x*t0,
				camera.getPosition().y+ray.y*t0,
				camera.getPosition().z+ray.z*t0);
			
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
			final double scale = 5.0*dt;  // TODO something better?
			double dx = InputManager.rawValue(InputManager.Source.MOUSE_X) * scale;
			double dy = InputManager.rawValue(InputManager.Source.MOUSE_Y) * -scale;
			
			switch(majorAxisSlideDirection) {
			case SLIDE_XPOS:  valueNow+=dx;	break;
			case SLIDE_XNEG:  valueNow-=dx;	break;
			case SLIDE_YPOS:  valueNow+=dy;	break;
			case SLIDE_YNEG:  valueNow-=dy;	break;
			}
			
			if(valueNow!=valueLast) {
				switch(majorAxis) {
				case X: translate(MatrixHelper.getXAxis(FOR), valueNow);	break;
				case Y: translate(MatrixHelper.getYAxis(FOR), valueNow);	break;
				case Z: translate(MatrixHelper.getZAxis(FOR), valueNow);	break;
				}
			}
			
			valueLast = valueNow;

			
			if(subject.canYouMoveTo(resultMatrix)) {
				ro.undoableEditHappened(new UndoableEditEvent(this,new ActionPoseEntityMoveWorld(subject,resultMatrix) ) );
			}
		}
	}

	protected double testBoxHit(Vector3d pos,Vector3d ray,Vector3d n) {		
		Point3d b0 = new Point3d(); 
		Point3d b1 = new Point3d();

		b0.set(+0.05,+0.05,+0.05);
		b1.set(-0.05,-0.05,-0.05);
		b0.scale(ballSizeScaled);
		b1.scale(ballSizeScaled);
		b0.add(n);
		b1.add(n);
		
		return MathHelper.rayBoxIntersection(pos,ray,b0,b1);
	}
	
	@Override
	public void render(GL2 gl2) {
		if(subject==null) return;

		gl2.glPushMatrix();
			RobotOverlord ro = (RobotOverlord)getRoot();
			ro.viewport.renderPerspective(gl2);
			
			IntBuffer depthFunc = IntBuffer.allocate(1);
			gl2.glGetIntegerv(GL2.GL_DEPTH_FUNC, depthFunc);
			gl2.glDepthFunc(GL2.GL_ALWAYS);
			//boolean isDepth=gl2.glIsEnabled(GL2.GL_DEPTH_TEST);
			//gl2.glDisable(GL2.GL_DEPTH_TEST);

			boolean isLit = gl2.glIsEnabled(GL2.GL_LIGHTING);
			gl2.glDisable(GL2.GL_LIGHTING);


			IntBuffer lineWidth = IntBuffer.allocate(1);
			gl2.glGetIntegerv(GL2.GL_LINE_WIDTH, lineWidth);
			gl2.glLineWidth(2);

			gl2.glPushMatrix();
	
				renderOutsideCircle(gl2);

				MatrixHelper.applyMatrix(gl2, FOR);
				gl2.glScaled(ballSizeScaled,ballSizeScaled, ballSizeScaled);

				if(isRotateMode()) {
					renderRotation(gl2);
				} else {
					renderTranslation(gl2);
				}
				
			gl2.glPopMatrix();

			// set previous line width
			gl2.glLineWidth(lineWidth.get());
			
			if (isLit) gl2.glEnable(GL2.GL_LIGHTING);

			//if(isDepth) gl2.glEnable(GL2.GL_DEPTH_TEST);
			gl2.glDepthFunc(depthFunc.get());
			
		gl2.glPopMatrix();
	}
	
	/**
	 * Render the white and grey circles around the exterior, always facing the camera.
	 * @param gl2
	 */
	private void renderOutsideCircle(GL2 gl2) {
		final double whiteRadius=1.05;
		final double greyRadius=1.0;
		final int quality=40;
		
		RobotOverlord ro = (RobotOverlord)getRoot();
		PoseEntity camera = ro.viewport.getAttachedTo();
		ro.viewport.renderPerspective(gl2);
		Matrix4d lookAt = camera.getPoseWorld();
		lookAt.setTranslation(MatrixHelper.getPosition(subject.getPoseWorld()));

		gl2.glPushMatrix();

			MatrixHelper.applyMatrix(gl2, lookAt);
			gl2.glScaled(ballSizeScaled,ballSizeScaled, ballSizeScaled);
			
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
		//Vector3d up = MatrixHelper.getYAxis(camera.getPoseWorld());
		//up.normalize();
		//Matrix4d lookAt = MatrixHelper.lookAt(camera.getPosition(), subject.getPosition(), up);
		Matrix4d lookAt = MatrixHelper.lookAt(camera.getPosition(), subject.getPosition());
		Vector3d lookAtVector = MatrixHelper.getZAxis(lookAt);
		
		Matrix4d cpw = camera.getPoseWorld();
		//Matrix4d spw = subject.getPoseWorld();
		//spw.m03=spw.m13=spw.m23=0;
		cpw.m03=
		cpw.m13=
		cpw.m23=0;
		cpw.invert();
		//spw.invert();
		//spw.set(cpw);

		double r = (nearestPlane==Plane.X) ? 1 : 0.5f;
		double g = (nearestPlane==Plane.Y) ? 1 : 0.5f;
		double b = (nearestPlane==Plane.Z) ? 1 : 0.5f;

		// is a FOR axis normal almost the same as camera forward?		
		boolean drawX = (Math.abs(lookAtVector.dot(MatrixHelper.getXAxis(FOR)))>0.85);
		boolean drawY = (Math.abs(lookAtVector.dot(MatrixHelper.getYAxis(FOR)))>0.85);
		boolean drawZ = (Math.abs(lookAtVector.dot(MatrixHelper.getZAxis(FOR)))>0.85);
		//System.out.println(drawX+"\t"+drawY+"\t"+drawZ);

		gl2.glEnable(GL2.GL_CULL_FACE);
		gl2.glCullFace(GL2.GL_BACK);
		//x
		gl2.glColor3d(r, 0, 0);
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
				gl2.glVertex3d(-0.25/ballSizeScaled,y,x);
				gl2.glVertex3d( 0.25/ballSizeScaled,y,x);
			}
			gl2.glEnd();
		}

		//y
		gl2.glColor3d(0, g, 0);
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
				gl2.glVertex3d(x,-0.25/ballSizeScaled,y);
				gl2.glVertex3d(x, 0.25/ballSizeScaled,y);
			}
			gl2.glEnd();
		}
		
		//z
		gl2.glColor3d(0, 0, b);
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
				gl2.glVertex3d(y,x,-0.25/ballSizeScaled);
				gl2.glVertex3d(y,x, 0.25/ballSizeScaled);
			}
			gl2.glEnd();
		}

		
		if(isActivelyMoving) {
			// display the distance rotated.
			double start=MathHelper.capRotationRadians(valueStart);
			double end=MathHelper.capRotationRadians(valueNow);
			double range=end-start;
			while(range>Math.PI) range-=Math.PI*2;
			while(range<-Math.PI) range+=Math.PI*2;
			double absRange= Math.abs(range);
			
			//System.out.println(start+" "+end+"");

			gl2.glBegin(GL2.GL_LINE_LOOP);
			gl2.glColor3f(255,255,255);
			gl2.glVertex3d(0,0,0);
			for(double i=0;i<absRange;i+=0.01) {
				double n = range * (i/absRange) + start;
				
				switch(nearestPlane) {
				case X: gl2.glVertex3d(0,Math.cos(n+Math.PI/2),Math.sin(n+Math.PI/2));  break;
				case Y: gl2.glVertex3d(Math.cos(-n),0,Math.sin(-n));  break;
				case Z: gl2.glVertex3d(Math.cos(n),Math.sin(n),0);  break;
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
		Vector3d lookAtVector = subject.getPosition();
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
		
		if(isActivelyMoving) {
			// the distance moved.
			gl2.glBegin(GL2.GL_LINES);
			gl2.glColor3f(255,255,255);
			gl2.glVertex3d(0,0,0);
			gl2.glVertex3d(
					(startMatrix.m03-resultMatrix.m03)/ballSizeScaled,
					(startMatrix.m13-resultMatrix.m13)/ballSizeScaled,
					(startMatrix.m23-resultMatrix.m23)/ballSizeScaled);
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

	protected void translate(Vector3d v, double amount) {
		//System.out.println(amount);
		resultMatrix.m03 = startMatrix.m03 + v.x*amount;
		resultMatrix.m13 = startMatrix.m13 + v.y*amount;
		resultMatrix.m23 = startMatrix.m23 + v.z*amount;
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
			double start=MathHelper.capRotationRadians(valueStart);
			double end=MathHelper.capRotationRadians(valueNow);
			double range=Math.toDegrees(end-start);
			range = MathHelper.capRotationDegrees(range);
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
	public void setSubject(PoseEntity subject) {
		this.subject=subject;		
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Mc", "Move controls");
		view.add(ballSize);
		view.addComboBox(frameOfReference,FrameOfReference.getAll());
	}
}
