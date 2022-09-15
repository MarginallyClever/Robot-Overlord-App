package com.marginallyclever.robotoverlord;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidParameterException;
import java.util.ArrayList;

public class EntityFactory {
	private static final Class<?> [] available = {
			com.marginallyclever.robotoverlord.Entity.class,

			com.marginallyclever.robotoverlord.robots.dog.DogRobot.class,
			com.marginallyclever.robotoverlord.robots.skycam.Skycam.class,
			com.marginallyclever.robotoverlord.robots.deltarobot3.DeltaRobot3.class,
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
	
	public static Entity load(String name) throws IllegalArgumentException {
		for( Class<?> c : available ) {
			if(name.contentEquals(c.getSimpleName()) || name.contentEquals(c.getCanonicalName())) {
				return createInstance(c);
			}
		}
		throw new InvalidParameterException("EntityFactory does not recognize '"+name+"'.");
	}

	private static Entity createInstance(Class<?> c) {
		try {
			for (Constructor<?> constructor : c.getDeclaredConstructors()) {
				if (constructor.getParameterCount() == 0) {
					return (Entity) constructor.newInstance();
				}
			}
		} catch (IllegalAccessException | InvocationTargetException | InstantiationException var5) {
			var5.printStackTrace();
		}
		return null;
	}
}
