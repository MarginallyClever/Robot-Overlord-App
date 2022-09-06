package com.marginallyclever.robotoverlord;

import com.marginallyclever.robotoverlord.components.shapes.Decal;

import java.security.InvalidParameterException;
import java.util.ArrayList;

public class EntityFactory {
	private static final Class<?> [] available = {
			com.marginallyclever.robotoverlord.Entity.class,
			Decal.class,
			com.marginallyclever.robotoverlord.components.sceneelements.SkyBoxEntity.class,

			com.marginallyclever.robotoverlord.robots.dog.DogRobot.class,
			com.marginallyclever.robotoverlord.robots.skycam.Skycam.class,
			com.marginallyclever.robotoverlord.robots.deltarobot3.DeltaRobot3.class,
			//com.marginallyclever.robotOverlord.robots.robotArm.RobotArmIK.class,
			com.marginallyclever.robotoverlord.robots.robotarm.implementations.Mantis.class,
			com.marginallyclever.robotoverlord.robots.robotarm.implementations.Sixi2.class,
			com.marginallyclever.robotoverlord.robots.robotarm.implementations.Sixi3_5axis.class,
			com.marginallyclever.robotoverlord.robots.robotarm.implementations.Sixi3_6axis.class,
			com.marginallyclever.robotoverlord.robots.robotarm.implementations.Thor.class,
			com.marginallyclever.robotoverlord.robots.robotarm.implementations.Meca500.class,
			com.marginallyclever.robotoverlord.robots.robotarm.implementations.K1_Osiris.class,
			com.marginallyclever.robotoverlord.robots.stewartplatform.rotary.RotaryStewartPlatform2.class,
			com.marginallyclever.robotoverlord.robots.stewartplatform.rotary.RotaryStewartPlatformAdjustable.class,
			com.marginallyclever.robotoverlord.robots.stewartplatform.linear.LinearStewartPlatform1.class,
			com.marginallyclever.robotoverlord.robots.stewartplatform.linear.LinearStewartPlatformAdjustable.class,
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
			if(c.getName().contentEquals(name)) {
				return (Entity)c.getDeclaredConstructor().newInstance();
			}
		}
		throw new InvalidParameterException("EntityFactory does not recognize '"+name+"'.");
	}
}
