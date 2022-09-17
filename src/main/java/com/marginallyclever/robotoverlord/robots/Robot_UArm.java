package com.marginallyclever.robotoverlord.robots;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.components.RobotComponent;

/**
 * Unfinished UArm implementation.
 * See <a href="https://buildmedia.readthedocs.org/media/pdf/uarmdocs/latest/uarmdocs.pdf">...</a>
 * @author Dan Royer
 */
@Deprecated
public class Robot_UArm extends Entity {
	private final RobotComponent live = new RobotComponent();

	public Robot_UArm() {
		super();
		setName("UArm");

		live.getBone(0).set("",2.4,2.0728,0,0,160,-160,"/uArm/base.STL");
		live.getBone(1).set("",9.5267-2.4,0,0,90,0,-72,"/uArm/shoulder.STL");
		live.getBone(2).set("",14.8004,0,0,0,150,-10,"/uArm/bicep.STL");
		live.getBone(3).set("",16.0136,0,0,0,0,0,"/uArm/forearm.STL");
		live.getBone(4).set("",3.545,0,0,-90,0,0,"/uArm/wrist.STL");
		live.getBone(5).set("",0,4,0,0,0,0,"/uArm/hand.STL");
	}
}
