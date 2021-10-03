package com.marginallyclever.robotOverlord.demos;

import com.marginallyclever.robotOverlord.robots.robotArm.RobotArmBone;
import com.marginallyclever.robotOverlord.robots.robotArm.RobotArmFK;
import com.marginallyclever.robotOverlord.shape.Shape;

public class Sixi1 extends RobotArmFK {
	private static final long serialVersionUID = 1L;

	public Sixi1() {
		super();
		setName("Sixi1");
	}
	
	@Override
	protected void loadModel() {
		setBaseShape(new Shape("Base","/Sixi/anchor.stl"));
		
		// name d r a t max min file
		addBone(new RobotArmBone("X",25         ,0 ,-90,  0,120,-120,   "/Sixi/shoulder.stl"));  
		addBone(new RobotArmBone("Y",0          ,25,  0,-90,170,-170,   "/Sixi/bicep.stl"));     
		addBone(new RobotArmBone("Z",0          ,0 ,-90,-90, 86, -91,"/Sixi/elbow.stl"));     
		addBone(new RobotArmBone("U",25         ,0 , 90,  0, 90, -90,   "/Sixi/forearm.stl"));   
		addBone(new RobotArmBone("V",0          ,0 ,-90,  0, 90, -90,   "/Sixi/wrist.stl"));     
		addBone(new RobotArmBone("W",3.9527     ,0 ,  0,  0,170,-170,   "/Sixi/hand.stl"));      
		
		adjustModelOriginsToDHLinks();		
	}
}
