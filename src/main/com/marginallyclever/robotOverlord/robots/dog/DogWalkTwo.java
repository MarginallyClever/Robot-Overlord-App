package com.marginallyclever.robotOverlord.robots.dog;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.OpenGLHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

public class DogWalkTwo extends Entity implements DogAnimator {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1161074262097357976L;
	private double STEP_LENGTH = 5;
	private double STEP_HEIGHT = 3;
	
	private double leftForce = 0;
	private double forwardForce = 0;
	private double turnForce = 0;
	
	public DogWalkTwo() {
		super("DogWalkTwo - blended");
	}
	
	@Override
	public void walk(DogRobot dogRobot,GL2 gl2) {
		lowerFeetToGround(dogRobot);
		dogRobot.relaxShoulders();
		moveOneToeTarget2AtATime(dogRobot,gl2);
		dogRobot.moveToeTargetsSmoothly(1);
		dogRobot.gradientDescent();
		
		dogRobot.drawToeTarget(gl2);
	}
	
	// assumes body is right way up.
	private void lowerFeetToGround(DogRobot dogRobot) {
		for(int i=0;i<4;++i) {
			DogLeg leg = dogRobot.getLeg(i);
			leg.toeTarget2.z=0;
		}
	}

	private void moveOneToeTarget2AtATime(DogRobot dogRobot,GL2 gl2) {
		double t = System.currentTimeMillis()*0.001;

		Vector3d toward = getDesiredDirectionOfBody(dogRobot);
		Vector3d pushToBody = new Vector3d();
		double zTorque=0;
		double feetOnFloor=0;
		for(int i=0;i<4;++i) {
			DogLeg leg = dogRobot.getLeg(i);
		
			// step in the desired direction
			Vector3d oneLegDir = getDesiredDirectionOfOneLeg(dogRobot,toward,leg);
			Vector3d floorUnderShoulder = leg.getPointOnFloorUnderShoulder();
			
			if(thisLegShouldStepNow(t,i)) {
				// leg should be not touching floor, up and moving.
				drawLegToward(gl2,toward,floorUnderShoulder);
				
				double zeroToOne = getTimeIntoStep(t,i);
				double verticalMove=Math.max(0,Math.abs(Math.sin(Math.PI*zeroToOne))-0.2);
				double horizontalMove=Math.sin((Math.PI/2)*zeroToOne);
				
				if(oneLegDir.lengthSquared()>0) {
					oneLegDir.scale(STEP_LENGTH*horizontalMove);
					oneLegDir.z+= STEP_HEIGHT*verticalMove;
				}
				leg.toeTarget2.add(oneLegDir,floorUnderShoulder);
			} else if(oneLegDir.z<=0.001 && leg.toeTarget2.z<0.001) {
				// Foot is touching floor, pushing body.
				feetOnFloor++;
				pushToBody.add(oneLegDir);
				
				Vector3d v1 = getBodyCenterToShoulderOnXYPlane(dogRobot,leg);
				Vector3d v2 = getTurnVectorOfOneLeg(dogRobot,leg);
				double radians = Math.atan2(v2.length(),v1.length());
				zTorque += radians * turnForce;
			}
		}

		boolean applyFriction=true;
		if(feetOnFloor>0 && applyFriction) {
			double s = 0.25/feetOnFloor;
			zTorque *= s;
			pushToBody.scale(s);
			
			drawBodyForce(gl2,dogRobot,pushToBody,zTorque);			
			dogRobot.pushBody(pushToBody,zTorque);
		}
	}

	private void drawLegToward(GL2 gl2, Vector3d toward, Vector3d floorUnderShoulder) {
		gl2.glColor3d(1, 0, 1);
		gl2.glLineWidth(3);
		OpenGLHelper.drawVector3dFrom(gl2,toward,floorUnderShoulder);
	}

	protected void drawBodyForce(GL2 gl2,DogRobot dogRobot, Vector3d pushToBody, double zTorque) {
		Vector3d body = getPointOnFloorUnderCenterOfBody(dogRobot);
		PrimitiveSolids.drawSphere(gl2,0.5,body);
		gl2.glColor3d(1, 1, 0);
		gl2.glLineWidth(10);
		OpenGLHelper.drawVector3dFrom(gl2,pushToBody,body);
	}

	@Override
	public void getView(ViewPanel view) {
		view.pushStack("D","Driving");
		view.addButton("all stop").addPropertyChangeListener((evt)->{
			forwardForce=0;
			leftForce=0;
			turnForce=0;
		});
		view.addButton("forward"		).addPropertyChangeListener((evt)->{	forwardForce++;	});
		view.addButton("backward"		).addPropertyChangeListener((evt)->{	forwardForce--;	});
		view.addButton("strafe left"	).addPropertyChangeListener((evt)->{	leftForce++;	});
		view.addButton("strafe right"	).addPropertyChangeListener((evt)->{	leftForce--;	});
		view.addButton("turn left"		).addPropertyChangeListener((evt)->{	turnForce--;	});
		view.addButton("turn right"		).addPropertyChangeListener((evt)->{	turnForce++;	});
		
		view.popStack();
		super.getView(view);
	}
	
	// return s0...1
	private double getTimeIntoStep(double t, int i) {
		double v = (t%4)-i; 
		Math.min(1,Math.max(0, v));
		return v;
	}
	
	protected boolean thisLegShouldStepNow(double t, int i) {
		return (Math.floor(t)%4) == i;
	}
	
	private Vector3d getTurnVectorOfOneLeg(DogRobot dogRobot,DogLeg leg) {
		Vector3d worldUp = new Vector3d(0,0,1);
		Vector3d v1 = getBodyCenterToShoulderOnXYPlane(dogRobot,leg);
		v1.normalize();
		Vector3d crossProduct = new Vector3d();
		crossProduct.cross(v1, worldUp);
		crossProduct.normalize();
		return crossProduct;
	}
	
	public Vector3d getDesiredDirectionOfOneLeg(DogRobot dogRobot,final Vector3d bodyToward,DogLeg leg) {
		Vector3d f2 = new Vector3d();
		Vector3d turnVector = getTurnVectorOfOneLeg(dogRobot,leg);
		turnVector.scale(turnForce);
		f2.add(bodyToward,turnVector);

		if(f2.lengthSquared()>0) f2.normalize();
		
		return f2;
	}

	protected Vector3d getDesiredDirectionOfBody(DogRobot dogRobot) {
		Vector3d forward = new Vector3d();
		
		Matrix4d myPose = dogRobot.getPose();
		Vector3d wLeft = MatrixHelper.getXAxis(myPose);
		Vector3d wForward = MatrixHelper.getZAxis(myPose);		
		wLeft.scale(leftForce);
		wForward.scale(forwardForce);
		forward.add(wLeft,wForward);

		if(forward.lengthSquared()>0) forward.normalize();
		
		return forward;
	}

	private Vector3d getBodyCenterToShoulderOnXYPlane(DogRobot dogRobot,DogLeg leg) {
		Vector3d v1 = leg.getPointOnFloorUnderShoulder();
		Vector3d v0 = MatrixHelper.getPosition(dogRobot.getPose());
		v0.z=0;
		v1.sub(v0);
		
		return v1;
	}
	
	private Vector3d getPointOnFloorUnderCenterOfBody(DogRobot dogRobot) {
		Vector3d fp = MatrixHelper.getPosition(dogRobot.getPose());
		fp.z=0;

		return fp;
	}

	@SuppressWarnings("unused")
	private void drawFloorCirclesUnderEachShoulder(DogRobot dogRobot,GL2 gl2) {
		for(int j=0;j<4;++j) {
			DogLeg leg = dogRobot.getLeg(j);
			Vector3d fp = leg.getPointOnFloorUnderShoulder();
			drawFloorCircleUnderOneShoulder(gl2,fp);
		}
	}

	private void drawFloorCircleUnderOneShoulder(GL2 gl2, Vector3d fp) {
		gl2.glPushMatrix();
		gl2.glTranslated(fp.x,fp.y,fp.z);
		PrimitiveSolids.drawCircleXY(gl2, 5, 20);
		gl2.glPopMatrix();
	}
}
