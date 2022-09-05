package com.marginallyclever.robotoverlord.robots.robotarm.implementations;

import com.marginallyclever.robotoverlord.robots.robotarm.RobotArmBone;
import com.marginallyclever.robotoverlord.robots.robotarm.RobotArmFK;
import com.marginallyclever.robotoverlord.mesh.ShapeEntity;

@Deprecated
// Kept for historical purposes.  These robot kinematics will never work as DH-parameters.  Bad design did not do the math before making hardware.
public class Sixi1 extends RobotArmFK {
	private static final long serialVersionUID = 1L;

	public Sixi1() {
		super();
		setName("Sixi1");
	}
	
	@Override
	protected void loadModel() {
		setBaseShape(new ShapeEntity("Base","/Sixi/Sixi0.stl"));
		
		// the shoulder joint is 25 up and 5 forward. https://www.calculator.net/triangle-calculator.html?vc=90&vx=25&vy=5&va=&vz=&vb=&angleunits=d&x=57&y=11
		// the ulna rotation is 20 up and 5 back.https://www.calculator.net/triangle-calculator.html?vc=90&vx=20&vy=5&va=&vz=&vb=&angleunits=d&x=57&y=11
		/* put together as a triangle
		 * 
		 * |
		 * |.
		 * |d . 
		 * |    .       ,
		 * |    c . e ,
		 * |--------.    
		 * |    b .
		 * |    .
		 * |a .
		 * |. 
		 * then
		 */
		double ae=25.495;
		double ed=20.616;
		double a = 11.31;
		double b = 78.69;
		double c = 75.964;
		//double d = 14.036;
		double e = 180-c-b;
		
		// name d r a t max min file
		addBone(new RobotArmBone("X",25         ,0 ,-90,0    ,120,-120,"/Sixi/Sixi1.stl"));  
		addBone(new RobotArmBone("Y",0          ,ae,  0,-90+a,170,-170,"/Sixi/Sixi2.stl"));     
		addBone(new RobotArmBone("Z",0          ,ed,  0,-e   , 86, -91,"/Sixi/Sixi3.stl"));     
		addBone(new RobotArmBone("U",0          ,0 , 90,0    , 90, -90,"/Sixi/Sixi4.stl"));   
		addBone(new RobotArmBone("V",0          ,0 ,-90,-90  , 90, -90,"/Sixi/Sixi5.stl"));     
		addBone(new RobotArmBone("W",3.9527     ,0 ,  0,0    ,170,-170,"/Sixi/Sixi6.stl"));
		
		showLineage.set(true);
		adjustModelOriginsToDHLinks();		
	}
}
