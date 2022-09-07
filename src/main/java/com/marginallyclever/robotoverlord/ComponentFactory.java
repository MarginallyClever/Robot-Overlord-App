package com.marginallyclever.robotoverlord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.util.*;

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
	
	public static Component load(String name) throws Exception {
		for( Class<?> c : available ) {
			if(c.getSimpleName().contentEquals(name)) {
				return (Component)c.getDeclaredConstructor().newInstance();
			}
			if(c.getName().contentEquals(name)) {
				return (Component)c.getDeclaredConstructor().newInstance();
			}
		}
		throw new InvalidParameterException(name);
	}
}
