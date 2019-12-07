package com.marginallyclever.robotOverlord;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.camera.Camera;
import com.marginallyclever.robotOverlord.physicalObject.PhysicalObject;

public class DragBall extends PhysicalObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2233212276793302070L;
	static final public int SLIDE_XPOS=0;
	static final public int SLIDE_XNEG=1;
	static final public int SLIDE_YPOS=2;
	static final public int SLIDE_YNEG=3;
	
	public boolean wasPressed;
	
	public boolean isHitting;
	public Vector3d pickPoint;
	public int nearestPlane;
	
	protected double ballSize=0.15f;
	public double ballSizeScaled=0.15f;

	// for movements
	public Vector3d pickPointSaved;
	public Matrix3d targetMatrixSaved;
	public Matrix3d targetMatrixToSave;
	public Matrix3d FORSaved;
	public Matrix3d FORToSave;
	
	// for rotations
	public int nearestPlaneSaved;
	public double angleRadSaved, angleRadNow, angleRadLast, angleRadChanged;;

	// for translations
	protected int majorAxisToSave;
	public int majorAxisSaved;
	public int majorAxisSlideDirection;
	
	public DragBall() {
		super();
		
		isHitting=false;
		pickPoint=new Vector3d();
		pickPointSaved=new Vector3d();
		nearestPlane=-1;
		wasPressed=false;
		angleRadChanged=0;
		targetMatrixSaved=new Matrix3d();
		targetMatrixToSave=new Matrix3d();
		FORSaved=new Matrix3d();
		FORToSave=new Matrix3d();
		
		majorAxisToSave=0;
		majorAxisSaved=0;
	}
	
	public void updateRotation(double dt) {
		Camera cam = getWorld().getCamera();
		Vector3d ray = cam.rayPick();

		Matrix4d cm=new Matrix4d(cam.getMatrix());
		cm.invert();
		cm.mul(matrix);
		cm.setTranslation(new Vector3d(0,0,0));
		

		Vector3d mp = this.getPosition();
		mp.sub(cam.getPosition());
		double d = mp.length();
		ballSizeScaled=ballSize*d;
		
		// find a pick point on the ball.
		isHitting = false;
		nearestPlane=-1;
		
		angleRadNow = angleRadLast;
		angleRadChanged=0;
		
		double Tca = ray.dot(mp);
		if(Tca>=0) {
			double d2 = Math.sqrt(d*d - Tca*Tca);
			if(d2>=0 && d2<=ballSizeScaled) {
				// hit!
				isHitting=true;
				double Thc = Math.sqrt(ballSizeScaled*ballSizeScaled - d2*d2);
				double t0 = Tca - Thc;
				//double t1 = Tca + Thc;
				//System.out.println("d2="+d2);

				pickPoint.set(
					cam.getPosition().x+ray.x*t0,
					cam.getPosition().y+ray.y*t0,
					cam.getPosition().z+ray.z*t0);
				
				Matrix4d iMe = new Matrix4d(matrix);
				matrix.m30=matrix.m31=matrix.m32=0;
				iMe.invert();
				Vector3d pickPointInBallSpace = new Vector3d(pickPoint);
				pickPointInBallSpace.sub(this.getPosition());
				pickPointInBallSpace.normalize();
				iMe.transform(pickPointInBallSpace);

				// which arc is closer to the pick point?
				if(wasPressed) {
					// if we are in a move already, do not change the nearest plane!
					nearestPlane = nearestPlaneSaved;
				} else {
					// if we are not in a move, find the nearest plane.
					double dx = Math.abs(pickPointInBallSpace.x);
					double dy = Math.abs(pickPointInBallSpace.y);
					double dz = Math.abs(pickPointInBallSpace.z);
					
					if(dx<dy) {
						nearestPlane=(dx<dz) ? 0:2;
					} else {
						nearestPlane=(dy<dz) ? 1:2;
					}
				}
		
				switch(nearestPlane) {
				case 0 :	angleRadNow = -Math.atan2(pickPointInBallSpace.y, pickPointInBallSpace.z);	break;
				case 1 :	angleRadNow = -Math.atan2(pickPointInBallSpace.z, pickPointInBallSpace.x);	break;
				default:	angleRadNow = Math.atan2(pickPointInBallSpace.y, pickPointInBallSpace.x);	break;
				}
				
				//System.out.println("x"+dp.x+"\ty"+dp.y+"\tz"+dp.z+"\ta"+angleRadNow);
			}
		}
		
		if(isHitting) {
			if(cam.isPressed() && !wasPressed) {
				pickPointSaved.set(pickPoint);
				targetMatrixSaved.set(targetMatrixToSave);
				FORSaved.set(FORToSave);
				
				nearestPlaneSaved=nearestPlane;
				angleRadSaved=angleRadNow;
				angleRadLast=angleRadNow;
			}
			// can only turn on while hitting
			wasPressed=cam.isPressed();
		}

		if(wasPressed) {
			// https://www.scratchapixel.com/lessons/3d-basic-rendering/minimal-ray-tracer-rendering-simple-shapes/ray-plane-and-ray-disk-intersection
			Vector3d majorAxis = new Vector3d();
			switch(nearestPlaneSaved) {
			case 0 :	majorAxis.set(matrix.m00,matrix.m01,matrix.m02);	break;
			case 1 :	majorAxis.set(matrix.m10,matrix.m11,matrix.m12);	break;
			default:	majorAxis.set(matrix.m20,matrix.m21,matrix.m22);	break;
			}
			
			double denominator = ray.dot(majorAxis);
			if(denominator!=0) {
				double numerator = mp.dot(majorAxis);
				double t0 = numerator/denominator;
				pickPoint.set(
						cam.getPosition().x+ray.x*t0,
						cam.getPosition().y+ray.y*t0,
						cam.getPosition().z+ray.z*t0);

				Matrix4d iMe = new Matrix4d(matrix);
				matrix.m30=matrix.m31=matrix.m32=0;
				iMe.invert();
				Vector3d pickPointInBallSpace = new Vector3d(pickPoint);
				pickPointInBallSpace.sub(this.getPosition());
				pickPointInBallSpace.normalize();
				iMe.transform(pickPointInBallSpace);

				switch(nearestPlaneSaved) {
				case 0 :	angleRadNow = -Math.atan2(pickPointInBallSpace.y, pickPointInBallSpace.z);	break;
				case 1 :	angleRadNow = -Math.atan2(pickPointInBallSpace.z, pickPointInBallSpace.x);	break;
				default:	angleRadNow = Math.atan2(pickPointInBallSpace.y, pickPointInBallSpace.x);	break;
				}
				
				
				angleRadChanged = angleRadNow-angleRadLast;
				angleRadLast = angleRadNow;
			}
		}
		
		// can turn off any time.
		if(wasPressed && !cam.isPressed()) {
			wasPressed=false;
		}
	}
	
	public void renderRotation(GL2 gl2) {
		double stepSize = Math.PI/20.0;
		int inOutin;
		
		Vector3d v=new Vector3d();
		Vector3d v1=new Vector3d();

		//PrimitiveSolids.drawStar(gl2, pickPoint,0.2*d);

		Matrix4d cm=new Matrix4d(getWorld().getCamera().getMatrix());
		cm.invert();
		cm.mul(matrix);
		cm.setTranslation(new Vector3d(0,0,0));
		
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, this.matrix);

			gl2.glScaled(ballSizeScaled, ballSizeScaled, ballSizeScaled);
			/*
			//white
			gl2.glColor4d(1,1,1,1);
			gl2.glBegin(GL2.GL_LINE_LOOP);
			for(double n=0;n<Math.PI*2;n+=stepSize) {
				gl2.glVertex3d(
						(cm.m00*Math.cos(n) +cm.m10*Math.sin(n))*1.1,
						(cm.m01*Math.cos(n) +cm.m11*Math.sin(n))*1.1,
						(cm.m02*Math.cos(n) +cm.m12*Math.sin(n))*1.1  );
			}
			gl2.glEnd();
			*/
			//grey
			gl2.glColor4d(0.5,0.5,0.5,1);
			gl2.glBegin(GL2.GL_LINE_LOOP);
			for(double n=0;n<Math.PI*2;n+=stepSize) {
				gl2.glVertex3d(
						(cm.m00*Math.cos(n) +cm.m10*Math.sin(n))*1.0,
						(cm.m01*Math.cos(n) +cm.m11*Math.sin(n))*1.0,
						(cm.m02*Math.cos(n) +cm.m12*Math.sin(n))*1.0  );
			}
			gl2.glEnd();
			
			int plane = wasPressed? nearestPlaneSaved : nearestPlane;
				
			//x
			inOutin=0;
			gl2.glColor3d(plane==0?1:0.25, 0, 0);
			for(double n=0;n<Math.PI*4;n+=stepSize) {
				v.set(0,Math.cos(n),Math.sin(n));
				cm.transform(v, v1);
				if(v1.z<0 ) {
					if(inOutin==0) inOutin=1;
					if(inOutin==2) {
						gl2.glVertex3d(v.x,v.y,v.z);
						break;
					}
				}
				if(v1.z>=0) {
					if(inOutin==1) {
						inOutin=2;
						gl2.glBegin(GL2.GL_LINE_STRIP);
					}
					if(inOutin==2) {
						gl2.glVertex3d(v.x,v.y,v.z);
					}
				}
			}
			gl2.glEnd();
			
			//y
			inOutin=0;
			gl2.glColor3d(0, plane==1?1:0.5, 0);
			for(double n=0;n<Math.PI*4;n+=stepSize) {
				v.set(Math.cos(n), 0, Math.sin(n));
				cm.transform(v, v1);
				if(v1.z<0 ) {
					if(inOutin==0) inOutin=1;
					if(inOutin==2) {
						gl2.glVertex3d(v.x,v.y,v.z);
						break;
					}
				}
				if(v1.z>=0) {
					if(inOutin==1) {
						inOutin=2;
						gl2.glBegin(GL2.GL_LINE_STRIP);
					}
					if(inOutin==2) {
						gl2.glVertex3d(v.x,v.y,v.z);
					}
				}
			}
			gl2.glEnd();
			
			//z
			inOutin=0;
			gl2.glColor3d(0, 0, plane==2?1:0.15);
			for(double n=0;n<Math.PI*4;n+=Math.PI/40) {
				v.set(Math.cos(n), Math.sin(n),0);
				cm.transform(v, v1);
				if(v1.z<0 ) {
					if(inOutin==0) inOutin=1;
					if(inOutin==2) {
						gl2.glVertex3d(v.x,v.y,v.z);
						break;
					}
				}
				if(v1.z>=0) {
					if(inOutin==1) {
						inOutin=2;
						gl2.glBegin(GL2.GL_LINE_STRIP);
					}
					if(inOutin==2) {
						gl2.glVertex3d(v.x,v.y,v.z);
					}
				}
			}
			
			gl2.glEnd();
		gl2.glPopMatrix();
	}
	

	public void updateTranslation(double dt) {
		Camera cam = getWorld().getCamera();
		Vector3d mp = this.getPosition();
		mp.sub(cam.getPosition());
		double d = mp.length();
		ballSizeScaled=ballSize*d;


		Vector3d pos = this.getPosition();
		
		// box centers
		Vector3d nx = new Vector3d(FORToSave.m00,FORToSave.m10,FORToSave.m20);
		Vector3d ny = new Vector3d(FORToSave.m01,FORToSave.m11,FORToSave.m21);
		Vector3d nz = new Vector3d(FORToSave.m02,FORToSave.m12,FORToSave.m22);
		
		Vector3d px = new Vector3d(nx);
		Vector3d py = new Vector3d(ny);
		Vector3d pz = new Vector3d(nz);
		px.scale(ballSizeScaled);
		py.scale(ballSizeScaled);
		pz.scale(ballSizeScaled);
		px.add(pos);
		py.add(pos);
		pz.add(pos);

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
		
		boolean isHitting = (t0>=0 && majorAxisToSave>0);
		if(isHitting) {
			if(cam.isPressed() && !wasPressed) {
				pickPointSaved.set(pickPoint);
				targetMatrixSaved.set(targetMatrixToSave);
				FORSaved.set(FORToSave);
				
				majorAxisSaved=majorAxisToSave;
				
				Matrix4d cm=cam.getMatrix();
				Vector3d cu = new Vector3d(cm.m01,cm.m11,cm.m21);
				Vector3d cr = new Vector3d(cm.m00,cm.m10,cm.m20);
				// determine which mouse direction is a positive movement on this axis.
				double cx,cy;
				switch(majorAxisSaved) {
				case 1://x
					cy=cu.dot(nx);
					cx=cr.dot(nx);
					if( Math.abs(cx) > Math.abs(cy) ) {
						majorAxisSlideDirection=(cx>0) ? SLIDE_XPOS : SLIDE_XNEG;
					} else {
						majorAxisSlideDirection=(cy>0) ? SLIDE_YPOS : SLIDE_YNEG;
					}
					break;
				case 2://y
					cy=cu.dot(ny);
					cx=cr.dot(ny);
					if( Math.abs(cx) > Math.abs(cy) ) {
						majorAxisSlideDirection=(cx>0) ? SLIDE_XPOS : SLIDE_XNEG;
					} else {
						majorAxisSlideDirection=(cy>0) ? SLIDE_YPOS : SLIDE_YNEG;
					}
					break;
				default://z
					majorAxisSlideDirection=SLIDE_YPOS;
					break;
				}
				switch(majorAxisSlideDirection) {
				case DragBall.SLIDE_XPOS:  System.out.println("x+");	break;
				case DragBall.SLIDE_XNEG:  System.out.println("x-");	break;
				case DragBall.SLIDE_YPOS:  System.out.println("y+");	break;
				case DragBall.SLIDE_YNEG:  System.out.println("y-");	break;
				}
			}
			// can only turn on while hitting
			wasPressed=cam.isPressed();
		}

		if(wasPressed) {
			// actively being dragged
		}

		// can turn off any time.
		if(wasPressed && !cam.isPressed()) {
			wasPressed=false;
		}
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
		Matrix4d cm=new Matrix4d(cam.getMatrix());
		cm.setTranslation(pickPoint);
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, cm);
			PrimitiveSolids.drawStar(gl2, new Vector3d(), 15);
		gl2.glPopMatrix();

		Vector3d pos = this.getPosition();
		Vector3d p2 = new Vector3d(cam.getPosition());
		p2.sub(pos);
		p2.normalize();

		int majorAxisIndex = wasPressed? majorAxisSaved : majorAxisToSave;
		float r = (majorAxisIndex==1)?1:0.25f;
		float g = (majorAxisIndex==2)?1:0.25f;
		float b = (majorAxisIndex==3)?1:0.25f;

		Matrix3d FOR = wasPressed? FORSaved : FORToSave;
		Vector3d nx = new Vector3d(FOR.m00,FOR.m10,FOR.m20);
		Vector3d ny = new Vector3d(FOR.m01,FOR.m11,FOR.m21);
		Vector3d nz = new Vector3d(FOR.m02,FOR.m12,FOR.m22);
		// should we hide an axis if it points almost the same direction as the camera?
		boolean drawX = (Math.abs(nx.dot(p2))<0.95);
		boolean drawY = (Math.abs(ny.dot(p2))<0.95);
		boolean drawZ = (Math.abs(nz.dot(p2))<0.95);

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
}
