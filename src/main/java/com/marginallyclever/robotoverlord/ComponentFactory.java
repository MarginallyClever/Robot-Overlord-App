package com.marginallyclever.robotoverlord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class ComponentFactory {
	private static final Logger logger = LoggerFactory.getLogger(ComponentFactory.class);
	private static final Class<?> [] available = {
			com.marginallyclever.robotoverlord.components.PoseComponent.class,
			com.marginallyclever.robotoverlord.components.DHComponent.class,
			com.marginallyclever.robotoverlord.components.OriginAdjustComponent.class,
			com.marginallyclever.robotoverlord.components.ArmEndEffectorComponent.class,
			com.marginallyclever.robotoverlord.components.RobotComponent.class,

			com.marginallyclever.robotoverlord.components.LightComponent.class,
			com.marginallyclever.robotoverlord.components.CameraComponent.class,
			com.marginallyclever.robotoverlord.components.MaterialComponent.class,

			//com.marginallyclever.robotoverlord.components.ShapeComponent.class is not instantiated directly.
			com.marginallyclever.robotoverlord.components.shapes.MeshFromFile.class,
			com.marginallyclever.robotoverlord.components.shapes.Box.class,
			com.marginallyclever.robotoverlord.components.shapes.Grid.class,
			com.marginallyclever.robotoverlord.components.shapes.Sphere.class,
			com.marginallyclever.robotoverlord.components.shapes.Decal.class,
	};
	
	public static ArrayList<String> getAllComponentNames() {
		ArrayList<String> names = new ArrayList<>();
		for( Class<?> c : available ) {
			names.add( c.getSimpleName() );
		}
		return names;
	}

	public static Class<?> getClassFromName(String name) {
		for( Class<?> c : available ) {
			if( c.getSimpleName().equals(name) ) return c;
		}
		return null;
	}

	public static Component load(String name) throws IllegalArgumentException {
		Class<?> c = getClassFromName(name);
		if(c==null) throw new IllegalArgumentException("ComponentFactory does not recognize '"+name+"'.");
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
