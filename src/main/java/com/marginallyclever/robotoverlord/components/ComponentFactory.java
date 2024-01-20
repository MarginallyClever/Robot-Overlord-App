package com.marginallyclever.robotoverlord.components;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * A factory to list and create {@link Component}s.
 *

 */
@Deprecated
public abstract class ComponentFactory {
	private static final Class<?> [] available = {};

	public static Class<?> getClassFromName(String name) {
		for( Class<?> c : available ) {
			if( c.getName().equals(name) ) return c;
			//if( c.getSimpleName().equals(name) ) return c;
		}
		return null;
	}

	public static Component load(String name) throws IllegalArgumentException {
		Class<?> c = getClassFromName(name);
		if(c==null) {
			throw new IllegalArgumentException("ComponentFactory does not recognize '"+name+"'.");
		}
		return createInstance(c);
	}

	public static Component createInstance(Class<?> c) {
		try {
			for (Constructor<?> constructor : c.getDeclaredConstructors()) {
				if (constructor.getParameterCount() == 0) {
					return (Component) constructor.newInstance();
				}
			}
		} catch (IllegalAccessException | InvocationTargetException | InstantiationException var5) {
			var5.printStackTrace();
		}
		return null;
	}
}
