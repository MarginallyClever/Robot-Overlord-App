package com.marginallyclever.robotoverlord.robots;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.components.RobotComponent;

/**
 * DHRobot version of Arm3, a palletizing robot I built long ago.  Incomplete!
 * @author Dan Royer
 *
 */
@Deprecated
public class Robot_Arm3 extends Entity {
	private final RobotComponent live = new RobotComponent();

	public Robot_Arm3() {
		super();
		setName("Arm3");
		/*
		// roll
		live.getBone(0).setD(13.44);
		live.getBone(0).setTheta(0);
		live.getBone(0).setRangeMin(-160);
		live.getBone(0).setRangeMax(160);
		// tilt
		live.getBone(1).setAlpha(0);
		live.getBone(2).setRangeMin(-72);
		// tilt
		live.getBone(2).setD(44.55);
		live.getBone(2).setAlpha(0);
		// interim point
		live.getBone(3).setD(40);
		live.getBone(3).setAlpha(0);

		live.getBone(0).model = ModelFactory.createModelFromFilename("/Sixi2/anchor.stl",0.1f);
		live.getBone(1).model = ModelFactory.createModelFromFilename("/Sixi2/shoulder.stl",0.1f);
		live.getBone(2).model = ModelFactory.createModelFromFilename("/Sixi2/bicep.stl",0.1f);
		live.getBone(3).model = ModelFactory.createModelFromFilename("/Sixi2/forearm.stl",0.1f);
		live.getBone(4).model = ModelFactory.createModelFromFilename("/Sixi2/hand.stl",0.1f);

		 */
	}
}
