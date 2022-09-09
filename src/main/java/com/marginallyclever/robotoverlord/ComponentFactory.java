package com.marginallyclever.robotoverlord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidParameterException;
import java.util.ArrayList;

public class ComponentFactory {
	private static final Logger logger = LoggerFactory.getLogger(ComponentFactory.class);
	private static final Class<?> [] available = {
			com.marginallyclever.robotoverlord.components.PoseComponent.class,
			com.marginallyclever.robotoverlord.components.LightComponent.class,
			com.marginallyclever.robotoverlord.components.CameraComponent.class,
			com.marginallyclever.robotoverlord.components.ShapeComponent.class,
			com.marginallyclever.robotoverlord.components.MaterialComponent.class,
			com.marginallyclever.robotoverlord.components.shapes.Box.class,
			com.marginallyclever.robotoverlord.components.shapes.Grid.class,
			com.marginallyclever.robotoverlord.components.shapes.Sphere.class,
			com.marginallyclever.robotoverlord.components.shapes.Decal.class,
			com.marginallyclever.robotoverlord.components.shapes.MeshFromFile.class,
	};
	
	public static ArrayList<String> getAllComponentNames() {
		ArrayList<String> names = new ArrayList<>();
		for( Class<?> c : available ) {
			names.add( c.getSimpleName() );
		}
		return names;
	}
	
	public static Component load(String name) throws IllegalArgumentException {
		for( Class<?> c : available ) {
			if(name.contentEquals(c.getSimpleName()) || name.contentEquals(c.getCanonicalName())) {
				return createInstance(c);
			}
		}
		throw new InvalidParameterException("ComponentFactory does not recognize '"+name+"'.");
	}

	private static Component createInstance(Class<?> c) {
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
