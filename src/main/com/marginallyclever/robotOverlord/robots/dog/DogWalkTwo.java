package com.marginallyclever.robotOverlord.robots.dog;

import java.util.ArrayList;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.OpenGLHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;
import com.marginallyclever.robotOverlord.uiExposedTypes.BooleanEntity;

public class DogWalkTwo extends DogWalkOne {
	private static final long serialVersionUID = -1161074262097357976L;
	private static final double NOMINAL_STEP_LENGTH = 5;
	private static final int NUM_PLANNERS=4; 
	private ArrayList<ArcZPlanner> planners = new ArrayList<ArcZPlanner>();
	private BooleanEntity isWalking=new BooleanEntity("Walk",false);
	
	public DogWalkTwo() {
		super();
		setName("DogWalkTwo - blended");
		
		for(int i=0;i<NUM_PLANNERS;++i) planners.add(new ArcZPlanner());
	}
	
	@Override
	public void walk(DogRobot dogRobot,GL2 gl2) {
		dogRobot.lowerFeetToGround();
		dogRobot.relaxShoulders();
		
		planWhereIStepFromHere(gl2,dogRobot);
		
		gl2.glLineWidth(3);
		planners.forEach(e->e.render(gl2, 40));
		gl2.glLineWidth(1);
		
		if(getTime()!=0) {
			double dt = (1.0/30.0);  // 30 fps TODO replace me
			
			if(getIsWalking()) moveOneFootAtATime(gl2,dogRobot,dt);
			dogRobot.moveToeTargetsSmoothly(0.25);
			dogRobot.gradientDescent();
		}
		
		dogRobot.drawToeTargets(gl2);
	}

	private void moveOneFootAtATime(GL2 gl2, DogRobot dogRobot, double dt) {
		int i = chooseALeg(dogRobot);		
		ArcZPlanner myPlanner = planners.get(i);
		DogLeg leg = dogRobot.getLeg(i);
		if(dt>0) {
			myPlanner.advance(dt);
			leg.toeTarget2.set(myPlanner.getPointNow());
		}
	}
	
	private int chooseALeg(DogRobot dogRobot) {
		double t = getTime();
		return (int)Math.floor(t%DogRobot.NUM_LEGS);
	}

	protected void planWhereIStepFromHere(GL2 gl2,DogRobot dogRobot) {
		double speed=1;  // TODO make me adjustable
		
		for(int i=0;i<DogRobot.NUM_LEGS;++i) {
			ArcZPlanner myPlanner = planners.get(i);
			DogLeg leg = dogRobot.getLeg(i);
			if(leg.isToeTouchingTheFloor()) {
				Vector3d startPoint = leg.getPointOnFloorUnderToe(); 
				Vector3d endPoint = getWhereIStepFromHere(dogRobot,leg,speed,startPoint);
				myPlanner.planStep(startPoint,endPoint,DogLeg.DEFAULT_STEP_HEIGHT,speed);
			}
		}
	}

	private Vector3d getWhereIStepFromHere(DogRobot dogRobot, DogLeg leg, double speed,Vector3d startPoint) {
		Vector3d bodyToward = getDesiredDirectionOfBody(dogRobot);
		Vector3d direction = getDesiredDirectionOfOneLeg(dogRobot,bodyToward,leg);
		direction.scale(NOMINAL_STEP_LENGTH*speed);
		 
		Vector3d endPoint = leg.getPointOnFloorUnderShoulder();
		endPoint.add(direction);
		return endPoint;
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
		view.pushStack("2","Two");
		view.add(isWalking);
		view.popStack();
		super.getView(view);
	}
	
	public boolean getIsWalking() {
		return isWalking.get();
	}
	
	protected boolean thisLegShouldStepNow(double t, int i) {
		return (Math.floor(t)%DogRobot.NUM_LEGS) == i;
	}
	
	@SuppressWarnings("unused")
	private Vector3d getTurnVectorOfOneLeg(DogRobot dogRobot,DogLeg leg) {
		Vector3d worldUp = new Vector3d(0,0,1);
		Vector3d v1 = getBodyCenterToShoulderOnXYPlane(dogRobot,leg);
		v1.normalize();
		Vector3d crossProduct = new Vector3d();
		crossProduct.cross(worldUp,v1);
		crossProduct.normalize();
		return crossProduct;
	}
	
	public Vector3d getDesiredDirectionOfOneLeg(DogRobot dogRobot,final Vector3d bodyToward,DogLeg leg) {
		//Vector3d turnVector = getTurnVectorOfOneLeg(dogRobot,leg);
		//turnVector.scale(turnForce);

		Vector3d f2 = new Vector3d();
		//f2.add(bodyToward,turnVector);
		if(f2.lengthSquared()>0) f2.normalize();
		
		return f2;
	}

	protected Vector3d getDesiredDirectionOfBody(DogRobot dogRobot) {
		Vector3d forward = new Vector3d();
/*
		Matrix4d myPose = dogRobot.getPose();
		Vector3d wLeft = MatrixHelper.getXAxis(myPose);
		Vector3d wForward = MatrixHelper.getZAxis(myPose);		
		wLeft.scale(leftForce);
		wForward.scale(forwardForce);
		forward.add(wLeft,wForward);
*/
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
		for(int j=0;j<DogRobot.NUM_LEGS;++j) {
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
