package com.marginallyclever.robotOverlord.robots.dog;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.OpenGLHelper;
import com.marginallyclever.convenience.PrimitiveSolids;

public class DogWalkThree extends DogWalkTwo implements DogAnimator {	
	/**
	 * 
	 */
	private static final long serialVersionUID = 620414718003445609L;
	private int bestLeg=0;

	public DogWalkThree() {
		super();
		setName("DogWalkThree - choosy feet");
	}

	@Override
	public void walk(DogRobot dogRobot,GL2 gl2) {
		chooseLeg(dogRobot,gl2);
		
		super.walk(dogRobot, gl2);
	}

	private void chooseLeg(DogRobot dogRobot, GL2 gl2) {
		Vector3d toward = getDesiredDirectionOfBody(dogRobot);
		boolean moving = toward.lengthSquared()>0.001;
		
		drawBodyForce(gl2, dogRobot, toward, 0);
		
		double bestScore = -Double.MAX_VALUE;
		bestLeg = 0;
		for(int i=0;i<4;++i) {
			DogLeg leg = dogRobot.getLeg(i);
			
			double score;
			if(!moving) {
				score = getHowFarIsThisLegFromIdeal(gl2,dogRobot,leg);
			} else score = getHowBadlyThisLegNeedsToMove(gl2,dogRobot,toward,leg);
 
			if(bestScore<score) {
				bestScore=score;
				bestLeg=i;
			}
		}
		DogLeg leg = dogRobot.getLeg(bestLeg);
		PrimitiveSolids.drawSphere(gl2, 1.25, leg.toe);
	}

	private double getHowFarIsThisLegFromIdeal(GL2 gl2, DogRobot dogRobot, DogLeg leg) {
		// furthest leg from ideal
		Vector3d legCenter = leg.getPointOnFloorUnderShoulder();
		Vector3d legActual = new Vector3d(leg.toe);
		legActual.sub(legCenter);
		return legActual.lengthSquared();
	}

	private double getHowBadlyThisLegNeedsToMove(GL2 gl2,DogRobot dogRobot, Vector3d toward, DogLeg leg) {
		Vector3d oneLegDir = getDesiredDirectionOfOneLeg(dogRobot,toward,leg);
		Vector3d legCenter = leg.getPointOnFloorUnderShoulder();
		Vector3d legActual = new Vector3d(leg.toe);
		legActual.z=0;
		Vector3d legDiff = new Vector3d();
		legDiff.sub(legActual,legCenter);
		OpenGLHelper.drawLine(gl2, legCenter, legActual);
		return -legDiff.dot(oneLegDir);
	}

	@Override
	protected boolean thisLegShouldStepNow(double t, int i) {
		return bestLeg == i;
	}
}
