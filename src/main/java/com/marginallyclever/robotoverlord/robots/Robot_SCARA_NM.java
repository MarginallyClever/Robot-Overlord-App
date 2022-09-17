package com.marginallyclever.robotoverlord.robots;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.components.RobotComponent;

/**
 * FANUC cylindrical coordinate robot GMF M-100
 * @author Dan Royer
 *
 */
@Deprecated
public class Robot_SCARA_NM extends Entity {
	private final RobotComponent live = new RobotComponent();
	
	public Robot_SCARA_NM() {
		super();
		setName("SCARA NM");
		live.getBone(0).set("",13.784,15,0,0,240,-40,"/SCARA_NM/Scara_base.stl");
		live.getBone(1).set("",13.0,0,0,0,120,-120,"/SCARA_NM/Scara_arm1.stl");
		live.getBone(2).set("",-8,0,0,0,-10.92600+7.574,-10.92600-0.5,"/SCARA_NM/Scara_arm2.stl");
		live.getBone(3).set("",0,0,0,0,180,-180,"");
		live.getBone(4).set("",0,0,0,0,0,-0,"/SCARA_NM/Scara_screw.stl");
	}
}
