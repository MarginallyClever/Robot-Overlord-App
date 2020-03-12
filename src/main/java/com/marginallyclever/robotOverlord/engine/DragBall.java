package com.marginallyclever.robotOverlord.engine;

import java.nio.IntBuffer;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.entity.camera.Camera;
import com.marginallyclever.robotOverlord.entity.physicalObject.PhysicalObject;
import com.marginallyclever.robotOverlord.uiElements.InputManager;

/**
 * A visual manipulator that facilitates moving objects in 3D.
 * @author Dan Royer
 *
 */
public class DragBall extends PhysicalObject {
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
		WORLD(0,"WORLD"),
		CAMERA(1,"CAMERA"),
		SELF(2,"SELF");
		
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
	public int nearestPlane;
	
	protected final double ballSize=0.125f;
	public double ballSizeScaled;

	public boolean isRotateMode;
	public boolean isActivelyMoving;

	// Who is being moved?
	protected PhysicalObject subject;
	// In what frame of reference?
	protected FrameOfReference frameOfReference;

	// matrix of subject when move started
	protected Matrix4d startMatrix=new Matrix4d();	
	protected Matrix4d cameraMatrix=new Matrix4d();
	protected Matrix4d resultMatrix=new Matrix4d();
	protected Matrix4d FOR=new Matrix4d();
	protected Matrix4d FORSaved=new Matrix4d();  // FOR at time of activation
	
	// for rotation
	public int nearestPlaneSaved;
	
	public double valueSaved;  // original angle when move started
	public double valueNow;  // current state
	public double valueLast;  // state last frame 
	public double valueChanged;  // change this update

	// for translation
	protected int majorAxisToSave;
	public int majorAxisSaved;
	public SlideDirection majorAxisSlideDirection;
	
	public DragBall() {
		super();

		frameOfReference = FrameOfReference.WORLD;
		nearestPlane=-1;
		valueChanged=0;
		
		FOR.setIdentity();
		FORSaved.setIdentity();
		
		majorAxisToSave=0;
		majorAxisSaved=0;
		
		isRotateMode=false;
		isActivelyMoving=false;
	}
	
	@Override
	public void update(double dt) {
		if(subject==null) return;
		
		this.setCameraMatrix(getWorld().getCamera().getPose());

		if(!isActivelyMoving()) {
			setRotateMode(InputManager.isOn(InputManager.Source.KEY_LSHIFT)
						|| InputManager.isOn(InputManager.Source.KEY_RSHIFT));
	
			if(InputManager.isReleased(InputManager.Source.KEY_1)) {
				frameOfReference=FrameOfReference.WORLD;
				//System.out.println("Frame "+frameOfReference);
			}
			if(InputManager.isReleased(InputManager.Source.KEY_2)) {
				frameOfReference=FrameOfReference.CAMERA;
				//System.out.println("Frame "+frameOfReference);
			}
			if(InputManager.isReleased(InputManager.Source.KEY_3)) {
				frameOfReference=FrameOfReference.SELF;
				//System.out.println("Frame "+frameOfReference);
			}
		} else {
			if(InputManager.isReleased(InputManager.Source.KEY_ESCAPE)) {
				// cancel this move
				isActivelyMoving=false;
				resultMatrix.set(startMatrix);
			}
		}
		
		switch(frameOfReference) {
		case CAMERA: FOR.set(cameraMatrix );			break;
		case SELF  : FOR.set(subject.getPoseWorld());	break;
		case WORLD : FOR.setIdentity();					break;
		default    : FOR.setIdentity();					break;
		}
		//System.out.println(frameOfReference + " "+FOR);

		Vector3d p = MatrixHelper.getPosition(subject.getPoseWorld());
		FOR.setTranslation(p);
		
		if(isRotateMode) {
			updateRotation(dt);
		} else {
			updateTranslation(dt);
		}
		
		subject.setPoseWorld(resultMatrix);
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
	
	public void updateRotation(double dt) {
		Camera cam = getWorld().getCamera();
		this.setPosition(MatrixHelper.getPosition(subject.getPoseWorld()));
		Vector3d mp = this.getPosition();
		mp.sub(cam.getPosition());
		valueNow = valueLast;
		valueChanged=0;

		Vector3d ray = cam.rayPick();

		if(!isActivelyMoving && cam.isPressed()) {
			// not moving yet, mouse clicked.
			
			// ray/sphere intersection
			double d = mp.length();
			ballSizeScaled=ballSize*d;
			
			// find a pick point on the ball.
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
							cam.getPosition().x+ray.x*t0,
							cam.getPosition().y+ray.y*t0,
							cam.getPosition().z+ray.z*t0);
					startMatrix.set(subject.getPoseWorld());
					resultMatrix.set(subject.getPoseWorld());
					this.setPosition(MatrixHelper.getPosition(resultMatrix));
					
					FORSaved.set(FOR);

					Vector3d pickPointInFOR = getPickPointInFOR(pickPointOnBall,FORSaved);
					
					// find nearest plane
					double dx = Math.abs(pickPointInFOR.x);
					double dy = Math.abs(pickPointInFOR.y);
					double dz = Math.abs(pickPointInFOR.z);
					nearestPlane=0;
					double nearestD=dx;
					if(dy<nearestD) {
						nearestPlane=1;
						nearestD=dy;
					}
					if(dz<nearestD) {
						nearestPlane=2;
						nearestD=dz;
					}
					nearestPlaneSaved=nearestPlane;

					// https://www.scratchapixel.com/lessons/3d-basic-rendering/minimal-ray-tracer-rendering-simple-shapes/ray-plane-and-ray-disk-intersection
					Vector3d majorAxis = new Vector3d();
					switch(nearestPlaneSaved) {
					case 0 :	majorAxis = MatrixHelper.getForward	(FORSaved);	break;
					case 1 :	majorAxis = MatrixHelper.getLeft	(FORSaved);	break;
					default:	majorAxis = MatrixHelper.getUp		(FORSaved);	break;
					}
					
					// find the pick point on the plane of rotation
					double denominator = ray.dot(majorAxis);
					if(denominator!=0) {
						double numerator = mp.dot(majorAxis);
						t0 = numerator/denominator;
						pickPoint.set(
								cam.getPosition().x+ray.x*t0,
								cam.getPosition().y+ray.y*t0,
								cam.getPosition().z+ray.z*t0);
						pickPointSaved.set(pickPoint);
						
						pickPointInFOR = getPickPointInFOR(pickPoint,FORSaved);
						switch(nearestPlaneSaved) {
						case 0 :	valueNow = -Math.atan2(pickPointInFOR.y, pickPointInFOR.z);	break;
						case 1 :	valueNow = -Math.atan2(pickPointInFOR.z, pickPointInFOR.x);	break;
						default:	valueNow =  Math.atan2(pickPointInFOR.y, pickPointInFOR.x);	break;
						}
						pickPointInFOR.normalize();
						//System.out.println("p="+pickPointInFOR+" valueNow="+Math.toDegrees(valueNow));
					}
					valueSaved=valueNow;
					valueLast=valueNow;
				}
			}
		}
		
		// can turn off any time.
		if(isActivelyMoving && !cam.isPressed()) {
			isActivelyMoving=false;
		}

		if(isActivelyMoving) {
			// https://www.scratchapixel.com/lessons/3d-basic-rendering/minimal-ray-tracer-rendering-simple-shapes/ray-plane-and-ray-disk-intersection
			Vector3d majorAxis = new Vector3d();
			switch(nearestPlaneSaved) {
			case 0 :	majorAxis = MatrixHelper.getForward	(FORSaved);	break;
			case 1 :	majorAxis = MatrixHelper.getLeft	(FORSaved);	break;
			default:	majorAxis = MatrixHelper.getUp		(FORSaved);	break;
			}
			
			// find the pick point on the plane of rotation
			double denominator = ray.dot(majorAxis);
			if(denominator!=0) {
				double numerator = mp.dot(majorAxis);
				double t0 = numerator/denominator;
				pickPoint.set(
						cam.getPosition().x+ray.x*t0,
						cam.getPosition().y+ray.y*t0,
						cam.getPosition().z+ray.z*t0);

				Vector3d pickPointInFOR = getPickPointInFOR(pickPoint,FORSaved);
				switch(nearestPlaneSaved) {
				case 0 :	valueNow = -Math.atan2(pickPointInFOR.y, pickPointInFOR.z);	break;
				case 1 :	valueNow = -Math.atan2(pickPointInFOR.z, pickPointInFOR.x);	break;
				default:	valueNow =  Math.atan2(pickPointInFOR.y, pickPointInFOR.x);	break;
				}

				double da=valueNow - valueSaved;

				if(da!=0) {
					switch(nearestPlaneSaved) {
					case 0 : rollX(da);	break;
					case 1 : rollY(da);	break;
					default: rollZ(da);	break;
					}
				}
				//System.out.println(da);
				
				valueChanged = valueNow-valueLast;
				valueLast = valueNow;
			}
		}
	}
	
	public void updateTranslation(double dt) {
		Camera cam = getWorld().getCamera();
		this.setPosition(MatrixHelper.getPosition(subject.getPoseWorld()));
		Vector3d mp = this.getPosition();
		mp.sub(cam.getPosition());
		valueNow = valueLast;
		valueChanged=0;
		
		if(!isActivelyMoving && cam.isPressed()) {
			double d = mp.length();
			ballSizeScaled=ballSize*d;
	
			Vector3d pos = this.getPosition();
			
			// box centers
			Vector3d nx = MatrixHelper.getForward(FOR);
			Vector3d ny = MatrixHelper.getLeft(FOR);
			Vector3d nz = MatrixHelper.getUp(FOR);
			
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
			majorAxisToSave = 0;
			double dx = testBoxHit(cam,px);
			double dy = testBoxHit(cam,py);
			double dz = testBoxHit(cam,pz);
			double t0=Double.MAX_VALUE;
			if(dx>0) {
				majorAxisToSave=1;
				t0=dx;
			}
			if(dy>0 && dy<t0) {
				majorAxisToSave=2;
				t0=dy;
			}
			if(dz>0 && dz<t0) {
				majorAxisToSave=3;
				t0=dz;
			}
			
			Vector3d ray=new Vector3d(cam.rayPick());
			pickPoint.set(
				cam.getPosition().x+ray.x*t0,
				cam.getPosition().y+ray.y*t0,
				cam.getPosition().z+ray.z*t0);
			
			boolean isHit = (t0>=0 && majorAxisToSave>0);
			if(isHit) {
				// if hitting and pressed, begin movement.
				isActivelyMoving = true;
				
				pickPointSaved.set(pickPoint);
				startMatrix.set(subject.getPoseWorld());
				resultMatrix.set(startMatrix);
				
				majorAxisSaved=majorAxisToSave;
				nearestPlaneSaved=nearestPlane;
				valueSaved=0;
				valueLast=0;
				valueNow=0;
				
				Matrix4d cm=cam.getPose();
				Vector3d cu = new Vector3d(cm.m01,cm.m11,cm.m21);
				Vector3d cr = new Vector3d(cm.m00,cm.m10,cm.m20);
				// determine which mouse direction is a positive movement on this axis.
				double cx,cy;
				switch(majorAxisSaved) {
				case 1://x
					cy=cu.dot(nx);
					cx=cr.dot(nx);
					if( Math.abs(cx) > Math.abs(cy) ) {
						majorAxisSlideDirection=(cx>0) ? SlideDirection.SLIDE_XPOS : SlideDirection.SLIDE_XNEG;
					} else {
						majorAxisSlideDirection=(cy>0) ? SlideDirection.SLIDE_YPOS : SlideDirection.SLIDE_YNEG;
					}
					break;
				case 2://y
					cy=cu.dot(ny);
					cx=cr.dot(ny);
					if( Math.abs(cx) > Math.abs(cy) ) {
						majorAxisSlideDirection=(cx>0) ? SlideDirection.SLIDE_XPOS : SlideDirection.SLIDE_XNEG;
					} else {
						majorAxisSlideDirection=(cy>0) ? SlideDirection.SLIDE_YPOS : SlideDirection.SLIDE_YNEG;
					}
					break;
				case 3://z
					cy=cu.dot(nz);
					cx=cr.dot(nz);
					if( Math.abs(cx) > Math.abs(cy) ) {
						majorAxisSlideDirection=(cx>0) ? SlideDirection.SLIDE_XPOS : SlideDirection.SLIDE_XNEG;
					} else {
						majorAxisSlideDirection=(cy>0) ? SlideDirection.SLIDE_YPOS : SlideDirection.SLIDE_YNEG;
					}
					break;
				}
				/*
				switch(majorAxisSlideDirection) {
				case SLIDE_XPOS:  System.out.println("x+");	break;
				case SLIDE_XNEG:  System.out.println("x-");	break;
				case SLIDE_YPOS:  System.out.println("y+");	break;
				case SLIDE_YNEG:  System.out.println("y-");	break;
				}*/
			}
		}

		// can turn off any time.
		if(isActivelyMoving && !cam.isPressed()) {
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
				switch(majorAxisSaved) {
				case 1 : translate(MatrixHelper.getForward(FOR), valueNow);	break;
				case 2 : translate(MatrixHelper.getLeft   (FOR), valueNow);	break;
				default: translate(MatrixHelper.getUp     (FOR), valueNow);	break;
				}
			}
			
			
			valueChanged = valueNow-valueLast;
			valueLast = valueNow;
		}
	}
	
	@Override
	public void render(GL2 gl2) {
		if(subject==null) return;
		
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, subject.getPoseWorld());
			
			IntBuffer depthFunc = IntBuffer.allocate(1);
			gl2.glGetIntegerv(GL2.GL_DEPTH_FUNC, depthFunc);
			gl2.glDepthFunc(GL2.GL_ALWAYS);
			//boolean isDepth=gl2.glIsEnabled(GL2.GL_DEPTH_TEST);
			//gl2.glDisable(GL2.GL_DEPTH_TEST);

			boolean isLit = gl2.glIsEnabled(GL2.GL_LIGHTING);
			gl2.glDisable(GL2.GL_LIGHTING);

			if(isRotateMode()) {
				renderRotation(gl2);
			} else {
				renderTranslation(gl2);
			}
			
			if (isLit) gl2.glEnable(GL2.GL_LIGHTING);

			//if(isDepth) gl2.glEnable(GL2.GL_DEPTH_TEST);
			gl2.glDepthFunc(depthFunc.get());
			
		gl2.glPopMatrix();
	}
	
	public void renderRotation(GL2 gl2) {
		double stepSize = Math.PI/120.0;
		int inOutin;
		Vector3d v=new Vector3d();
		

		gl2.glLineWidth(2);
		
		//PrimitiveSolids.drawStar(gl2, pickPoint,0.2*d);
		Matrix4d lookAt = MatrixHelper.lookAt(getWorld().getCamera().getPosition(), this.getPosition());
		/*
		Matrix4d iCamFOR=new Matrix4d(getWorld().getCamera().getMatrix());
		iCamFOR.invert();
		iCamFOR.mul(FOR);
		iCamFOR.setTranslation(new Vector3d(0,0,0));
		*/
		
		// camera forward is -z axis 
		//Vector3d cameraForward = MatrixHelper.getUp(getWorld().getCamera().getMatrix());
		//cameraForward.scale(-1);
		Vector3d cameraForward = MatrixHelper.getForward(lookAt);
		//System.out.println(cameraForward);
		
		Vector3d ballPosition = this.getPosition();

		gl2.glTranslated(ballPosition.x, ballPosition.y, ballPosition.z);
		gl2.glScaled(ballSizeScaled,ballSizeScaled, ballSizeScaled);
		/*
		//white
		gl2.glColor4d(1,1,1,1);
		gl2.glBegin(GL2.GL_LINE_LOOP);
		for(double n=0;n<Math.PI*2;n+=stepSize) {
			gl2.glVertex3d(
					(cm.m00*Math.cos(n) +cm.m10*Math.sin(n))*ballSizeScaled*1.1,
					(cm.m01*Math.cos(n) +cm.m11*Math.sin(n))*ballSizeScaled*1.1,
					(cm.m02*Math.cos(n) +cm.m12*Math.sin(n))*ballSizeScaled*1.1  );
		}
		gl2.glEnd();
		*/

		//grey
		gl2.glColor4d(0.5,0.5,0.5,1);
		gl2.glBegin(GL2.GL_LINE_LOOP);
		for(double n=0;n<Math.PI*2;n+=stepSize) {
			gl2.glVertex3d(
					(lookAt.m02*Math.sin(n) +lookAt.m01*Math.cos(n))*1.01,
					(lookAt.m12*Math.sin(n) +lookAt.m11*Math.cos(n))*1.01,
					(lookAt.m22*Math.sin(n) +lookAt.m21*Math.cos(n))*1.01  );
		}
		gl2.glEnd();


		int majorPlaneSaved = isActivelyMoving? nearestPlaneSaved : nearestPlane;
		float r = (majorPlaneSaved==0)?1:0.25f;
		float g = (majorPlaneSaved==1)?1:0.25f;
		float b = (majorPlaneSaved==2)?1:0.25f;

		Vector3d nx = MatrixHelper.getForward(FOR);
		Vector3d ny = MatrixHelper.getLeft(FOR);
		Vector3d nz = MatrixHelper.getUp(FOR);
		// should we hide an axis if it points almost the same direction as the camera?
		boolean drawX = (Math.abs(nx.dot(cameraForward))>0.95);
		boolean drawY = (Math.abs(ny.dot(cameraForward))>0.95);
		boolean drawZ = (Math.abs(nz.dot(cameraForward))>0.95);
		//System.out.println(drawX+"\t"+drawY+"\t"+drawZ);
		
		//x
		inOutin=0;
		gl2.glColor3d(r, 0, 0);
		gl2.glBegin(GL2.GL_LINE_STRIP);
		for(double n=0;n<Math.PI*4;n+=stepSize) {
			v.set(0,Math.cos(n),Math.sin(n));
			FOR.transform(v);
			if(drawX) {
				gl2.glVertex3d(v.x,v.y,v.z);
			} else {
				if(v.dot(cameraForward)>0) {
					if(inOutin==0) inOutin=1;
					if(inOutin==2) {
						gl2.glVertex3d(v.x,v.y,v.z);
						break;
					}
				} else {
					if(inOutin==1) {
						inOutin=2;
					}
					if(inOutin==2) {
						gl2.glVertex3d(v.x,v.y,v.z);
					}
				}
			}
		}
		gl2.glEnd();

		//y
		inOutin=0;
		gl2.glColor3d(0, g, 0);
		gl2.glBegin(GL2.GL_LINE_STRIP);
		for(double n=0;n<Math.PI*4;n+=stepSize) {
			v.set(Math.cos(n), 0, Math.sin(n));
			FOR.transform(v);
			if(drawY) {
				gl2.glVertex3d(v.x,v.y,v.z);
			} else {
				if(v.dot(cameraForward)>0) {
					if(inOutin==0) inOutin=1;
					if(inOutin==2) {
						gl2.glVertex3d(v.x,v.y,v.z);
						break;
					}
				} else {
					if(inOutin==1) {
						inOutin=2;
					}
					if(inOutin==2) {
						gl2.glVertex3d(v.x,v.y,v.z);
					}
				}
			}
		}
		gl2.glEnd();
		
		//z
		inOutin=0;
		gl2.glColor3d(0, 0, b);
		gl2.glBegin(GL2.GL_LINE_STRIP);
		for(double n=0;n<Math.PI*4;n+=stepSize) {
			v.set(Math.cos(n), Math.sin(n),0);
			FOR.transform(v);
			if(drawZ) {
				gl2.glVertex3d(v.x,v.y,v.z);
			} else {
				if(v.dot(cameraForward)>0) {
					if(inOutin==0) inOutin=1;
					if(inOutin==2) {
						gl2.glVertex3d(v.x,v.y,v.z);
						break;
					}
				} else {
					if(inOutin==1) {
						inOutin=2;
					}
					if(inOutin==2) {
						gl2.glVertex3d(v.x,v.y,v.z);
					}
				}
			}
		}
		gl2.glEnd();

		if(isActivelyMoving) {
			// display the distance rotated.
			Vector3d mid = new Vector3d();
			double start=MathHelper.capRotationRadians(valueSaved);
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
				switch(majorPlaneSaved) {
				case 0: mid.set(0,Math.cos(n+Math.PI/2),Math.sin(n+Math.PI/2));  break;
				case 1: mid.set(Math.cos(-n),0,Math.sin(-n));  break;
				case 2: mid.set(Math.cos(n),Math.sin(n),0);  break;
				}
				FOR.transform(mid);
				gl2.glVertex3d(mid.x,mid.y,mid.z);
			}
			gl2.glEnd();
		}

		gl2.glLineWidth(1);

		//Vector3d pickPointInFOR = getPickPointInFOR(pickPoint,FORSaved);
		//FORSaved.transform(pickPointInFOR);
		//pickPointInFOR.add(this.getPosition());
		//PrimitiveSolids.drawStar(gl2, pickPointOnBall,10);
		//PrimitiveSolids.drawStar(gl2, pickPointInFOR,10);
		//MatrixHelper.drawMatrix(gl2, FORSaved, 20);
		//PrimitiveSolids.drawStar(gl2, pickPoint,5);
		//PrimitiveSolids.drawStar(gl2, pickPointSaved,10);
	}
	
	protected double testBoxHit(Camera cam,Vector3d n) {
		Vector3d ray = cam.rayPick();
		
		Point3d b0 = new Point3d(); 
		Point3d b1 = new Point3d();

		b0.set(+0.05,+0.05,+0.05);
		b1.set(-0.05,-0.05,-0.05);
		b0.scale(ballSizeScaled);
		b1.scale(ballSizeScaled);
		b0.add(n);
		b1.add(n);
		
		return MathHelper.rayBoxIntersection(cam.getPosition(),ray,b0,b1);
	}
	
	public void renderTranslation(GL2 gl2) {
		Camera cam =getWorld().getCamera();
		
		//gl2.glPushMatrix();
		//	MatrixHelper.applyMatrix(gl2, cm);
		//	PrimitiveSolids.drawStar(gl2, new Vector3d(), 15);
		//gl2.glPopMatrix();

		gl2.glLineWidth(2);
		

		Vector3d pos = isActivelyMoving ? MatrixHelper.getPosition(resultMatrix) : this.getPosition();
		Vector3d p2 = new Vector3d(cam.getPosition());
		p2.sub(pos);
		p2.normalize();

		int majorAxisIndex = isActivelyMoving ? majorAxisSaved : majorAxisToSave;
		float r = (majorAxisIndex==1)?1:0.25f;
		float g = (majorAxisIndex==2)?1:0.25f;
		float b = (majorAxisIndex==3)?1:0.25f;

		Vector3d nx = MatrixHelper.getForward(FOR);
		Vector3d ny = MatrixHelper.getLeft(FOR);
		Vector3d nz = MatrixHelper.getUp(FOR);
		// should we hide an axis if it points almost the same direction as the camera?
		boolean drawX = (Math.abs(nx.dot(p2))<0.95);
		boolean drawY = (Math.abs(ny.dot(p2))<0.95);
		boolean drawZ = (Math.abs(nz.dot(p2))<0.95);

		gl2.glPushMatrix();
		if(drawX) {
			nx.scale(ballSizeScaled);
			nx.add(pos);
			gl2.glColor3f(r,0,0);
			drawAxis(gl2,nx,pos);
		}
		if(drawY) {
			ny.scale(ballSizeScaled);
			ny.add(pos);
			gl2.glColor3f(0,g,0);
			drawAxis(gl2,ny,pos);
		}
		if(drawZ) {
			nz.scale(ballSizeScaled);
			nz.add(pos);
			gl2.glColor3f(0,0,b);
			drawAxis(gl2,nz,pos);
		}
		gl2.glPopMatrix();
		
		if(isActivelyMoving) {
			// the distance moved.
			gl2.glBegin(GL2.GL_LINES);
			gl2.glColor3f(255,255,255);
			gl2.glVertex3d(resultMatrix.m03,resultMatrix.m13,resultMatrix.m23);
			gl2.glVertex3d(startMatrix.m03,startMatrix.m13,startMatrix.m23);
			gl2.glEnd();
		}
		gl2.glLineWidth(1);
	}
	
	protected void drawAxis(GL2 gl2,Vector3d n,Vector3d center) {
		gl2.glBegin(GL2.GL_LINES);
		gl2.glVertex3d(center.x,center.y,center.z);
		gl2.glVertex3d(n.x,n.y,n.z);
		gl2.glEnd();

		Point3d b0 = new Point3d(); 
		Point3d b1 = new Point3d();

		b0.set(+0.05,+0.05,+0.05);
		b1.set(-0.05,-0.05,-0.05);
		b0.scale(ballSizeScaled);
		b1.scale(ballSizeScaled);
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
	
	public Matrix4d getCameraMatrix() {
		return cameraMatrix;
	}

	public void setCameraMatrix(Matrix4d cameraMatrix) {
		this.cameraMatrix = cameraMatrix;
	}
	
	protected void rotationInternal(Matrix4d rotation) {
		Matrix4d result = new Matrix4d();
		
		// multiply robot origin by target matrix to get target matrix in world space.
		
		// invert frame of reference to transform world target matrix into frame of reference space.
		Matrix4d ifor = new Matrix4d(FOR);
		ifor.invert();

		Matrix4d subjectRotation = new Matrix4d(subject.getPoseWorld());
		Vector3d subjectPosition = MatrixHelper.getPosition(subject.getPoseWorld());
		
		Matrix4d subjectAfterRotation = new Matrix4d(FOR);
		subjectAfterRotation.mul(rotation);
		subjectAfterRotation.mul(ifor);
		subjectAfterRotation.mul(subjectRotation);

		result.set(subjectAfterRotation);
		result.setTranslation(subjectPosition);
		resultMatrix.set(result);
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
		frameOfReference=v;
	}
	
	public FrameOfReference getFrameOfReference() {
		return frameOfReference;
	}
	
	@Override
	public String getStatusMessage() {
		if(isRotateMode) {
			double start=MathHelper.capRotationRadians(valueSaved);
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
	 * Set which PhysicalObject the drag ball is going to act upon.
	 * @param subject
	 */
	public void setSubject(PhysicalObject subject) {
		this.subject=subject;		
	}
}
