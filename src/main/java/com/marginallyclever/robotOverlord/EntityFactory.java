package com.marginallyclever.robotOverlord;

import java.security.InvalidParameterException;
import java.util.ArrayList;

public class EntityFactory {
	private static Class<?> [] available = {
		com.marginallyclever.robotOverlord.robots.robotArm.RobotArmIK.class,
		com.marginallyclever.robotOverlord.robots.LinearStewartPlatform.class,
		com.marginallyclever.robotOverlord.robots.RotaryStewartPlatform.class,
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
	};
	
	public static ArrayList<String> getAllEntityNames() {
		ArrayList<String> names = new ArrayList<String>(); 
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
