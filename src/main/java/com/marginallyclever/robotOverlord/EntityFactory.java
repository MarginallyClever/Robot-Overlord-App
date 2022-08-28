package com.marginallyclever.robotOverlord;

import com.marginallyclever.robotOverlord.robots.stewartplatform.rotary.RotaryStewartPlatform2;
import com.marginallyclever.robotOverlord.robots.stewartplatform.rotary.RotaryStewartPlatformAdjustable;
import com.marginallyclever.robotOverlord.robots.stewartplatform.vertical.LinearStewartPlatform1;
import com.marginallyclever.robotOverlord.robots.stewartplatform.vertical.LinearStewartPlatformAdjustable;

import java.security.InvalidParameterException;
import java.util.ArrayList;

public class EntityFactory {
	private static Class<?> [] available = {
			//com.marginallyclever.robotOverlord.robots.robotArm.RobotArmIK.class,
			RotaryStewartPlatform2.class,
			RotaryStewartPlatformAdjustable.class,
			LinearStewartPlatform1.class,
			LinearStewartPlatformAdjustable.class,
			com.marginallyclever.robotOverlord.robots.skycam.Skycam.class,
			com.marginallyclever.robotOverlord.Camera.class,
			com.marginallyclever.robotOverlord.shape.Shape.class,
			com.marginallyclever.robotOverlord.sceneElements.Light.class,
			com.marginallyclever.robotOverlord.Decal.class,
			com.marginallyclever.robotOverlord.sceneElements.Box.class,
			com.marginallyclever.robotOverlord.sceneElements.Grid.class,
			com.marginallyclever.robotOverlord.sceneElements.Sphere.class,
			//com.marginallyclever.robotOverlord.sceneElements.SkyBox.class,
			com.marginallyclever.robotOverlord.robots.dog.DogRobot.class,

			com.marginallyclever.robotOverlord.robots.robotArm.implementations.Mantis.class,
			com.marginallyclever.robotOverlord.robots.robotArm.implementations.Sixi2.class,
			com.marginallyclever.robotOverlord.robots.robotArm.implementations.Sixi3_5axis.class,
			com.marginallyclever.robotOverlord.robots.robotArm.implementations.Sixi3_6axis.class,
			com.marginallyclever.robotOverlord.robots.robotArm.implementations.Thor.class,
			com.marginallyclever.robotOverlord.robots.robotArm.implementations.Meca500.class,
			com.marginallyclever.robotOverlord.robots.robotArm.implementations.K1_Osiris.class,
			com.marginallyclever.robotOverlord.robots.deltaRobot3.DeltaRobot3.class,
	};
	
	public static ArrayList<String> getAllEntityNames() {
		ArrayList<String> names = new ArrayList<>();
		for( Class<?> c : available ) {
			names.add( c.getSimpleName() );
		}
		return names;
	}
	
	public static Entity load(String name) throws Exception {
		for( Class<?> c : available ) {
			if(c.getSimpleName().contentEquals(name)) {
				return (Entity)c.getDeclaredConstructor().newInstance();
			}
		}
		throw new InvalidParameterException("EntityFactory does not recognize '"+name+"'.");
	}
}
