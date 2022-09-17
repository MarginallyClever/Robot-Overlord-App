package com.marginallyclever.robotoverlord.robots.dog;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import com.marginallyclever.robotoverlord.parameters.BooleanEntity;

public class DogWalkOne extends Entity implements DogAnimator {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3164195940151204314L;
	private BooleanEntity isFrozen=new BooleanEntity("Freeze!",true);

	public DogWalkOne() {
		super("DogWalkOne - Fixed motion");
	}
	
	@Override
	public void walk(DogRobot dogRobot,GL2 gl2) {
		dogRobot.setIdealStandingAngles();
		
		double t = getTime();
		for(int i=0;i<DogRobot.NUM_LEGS;++i) {
			DogLeg leg = dogRobot.getLeg(i);
			double [] angles = leg.getJointAngles();
			angles[2] += Math.toDegrees(Math.sin(t))/DogRobot.NUM_LEGS;
			angles[3] += Math.toDegrees(Math.cos(t))/DogRobot.NUM_LEGS;
			leg.setJointAngles(angles);
			t+=Math.PI/2;
		}
		dogRobot.updateAllLegMatrixes();
	}
	
	public boolean getIsFrozen() {
		return isFrozen.get();
	}

	public void setIsRunning(boolean state) {
		isFrozen.set(state);
	}
	
	public double getTime() {
		double t = System.currentTimeMillis()*0.001;
		if(getIsFrozen()) t=0;
		return t;
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("One",true);
		view.add(isFrozen);
		view.popStack();
		super.getView(view);
	}
}
