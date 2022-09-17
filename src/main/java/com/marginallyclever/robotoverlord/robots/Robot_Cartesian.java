package com.marginallyclever.robotoverlord.robots;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.RobotComponent;

/**
 * Cartesian 3 axis CNC robot like 3d printer or milling machine.
 * Effectively three prismatic joints.  Use this as an example for other cartesian machines.
 * @author Dan Royer
 *
 */
@Deprecated
public class Robot_Cartesian extends Entity {
	public MaterialComponent material;
	private final RobotComponent live = new RobotComponent();
	public Robot_Cartesian() {
		super();
		setName("Cartesian");
/*
		// roll
		live.getBone(0).setAlpha(90);
		live.getBone(0).setTheta(90);
		live.getBone(0).setRangeMax(21+8.422);
		live.getBone(0).setRangeMin(0+8.422);
		live.getBone(0).setShapeFilename("/Prusa i3 MK3/Prusa0.stl");
		
		// tilt
		live.getBone(1).setAlpha(90);
		live.getBone(1).setTheta(-90);
		live.getBone(1).setRangeMax(21);
		live.getBone(1).setRangeMin(0);
		live.getBone(1).setShapeFilename("/Prusa i3 MK3/Prusa1.stl");
		// tilt
		live.getBone(2).setAlpha(90);
		live.getBone(2).setTheta(90);
		live.getBone(2).setRangeMax(21+8.422);
		live.getBone(2).setRangeMin(0+8.422);
		live.getBone(2).setShapeFilename("/Prusa i3 MK3/Prusa2.stl");
		live.getBone(3).setShapeFilename("/Prusa i3 MK3/Prusa3.stl");

		live.getBone(0).setShapeScale(0.1);
		live.getBone(1).setShapeScale(0.1);
		live.getBone(2).setShapeScale(0.1);
		live.getBone(3).setShapeScale(0.1);

		live.getBone(0).setShapeRotation(new Vector3d(90,0,0));
		live.getBone(0).setShapeOrigin(new Vector3d(0,27.9,0));
		live.getBone(1).setShapeOrigin(new Vector3d(11.2758,-8.422,0));
		live.getBone(1).setShapeRotation(new Vector3d(0,-90,0));
		live.getBone(2).setShapeOrigin(new Vector3d(32.2679,-9.2891,-27.9));
		live.getBone(2).setShapeRotation(new Vector3d(0,0,90));
		live.getBone(3).setShapeRotation(new Vector3d(-90,0,0));
		live.getBone(3).setShapeOrigin(new Vector3d(0,-31.9,32.2679));

 */
	}
}
