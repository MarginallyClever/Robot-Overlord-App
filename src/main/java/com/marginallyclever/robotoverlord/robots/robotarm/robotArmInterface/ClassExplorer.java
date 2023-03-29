package com.marginallyclever.robotoverlord.robots.robotarm.robotArmInterface;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotoverlord.robots.robotarm.robotArmInterface.marlinInterface.TextInterfaceToListeners;
import com.marginallyclever.robotoverlord.robots.robotarm.robotArmInterface.marlinInterface.TextInterfaceToSessionLayer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

/**
 * Exploring Java reflection and what can be learned about other classes at run time.
 * @author Dan
 *
 */
@Deprecated
public class ClassExplorer {
	public static void reportOnClass(Object o) {
		Class<?> c = o.getClass();
		Log.message("Class: "+c.getName());
		//reportClassFields(c);
		//reportClassAnnotations(c);
		reportClassPublicMethods(c);
	}

	@SuppressWarnings("unused")
	private static void reportClassFields(Class<?> c) {
		Field [] fields = c.getFields();
		for(Field f : fields) {
			Log.message("  field: "+f.getName());
		}
	}

	@SuppressWarnings("unused")
	private static void reportClassAnnotations(Class<?> c) {
		Annotation [] annotations = c.getAnnotations();
		for(Annotation a : annotations) {
			Log.message("  annotation: "+a.toString());
		}
	}

	@SuppressWarnings("unused")
	private static void reportClassMethods(Class<?> c) {
		Method [] methods = c.getDeclaredMethods();
		for(Method m : methods) {
			reportClassMethod(m);
            int modifiers = m.getModifiers();
            Modifier.isPublic(modifiers);
            Log.message("    modifiers: "+Modifier.toString(modifiers) + "("+modifiers+")");
            
		}
	}
	
	private static void reportClassPublicMethods(Class<?> c) {
		Method [] methods = c.getDeclaredMethods();
		for(Method m : methods) {
			int modifiers = m.getModifiers();
			if(!Modifier.isPublic(modifiers)) continue;
			reportClassMethod(m);
		}
	}
	
	private static void reportClassMethod(Method m) {
        Log.message("  method: "+m);
        //Log.message("    decl class: " + m.getDeclaringClass());
        Class<?> c = m.getReturnType();
        Log.message("    return type: "+c.getName());
        Parameter [] parameters = m.getParameters();
        for( Parameter p : parameters ) {
        	reportClassMethodParameter(p);
        }
	}

	private static void reportClassMethodParameter(Parameter p) {
		Log.message("    param: "+p);
	}

	public static void main(String[] args) {
		reportOnClass(new TapeDeckPanel());
		reportOnClass(new TextInterfaceToListeners());
		reportOnClass(new TextInterfaceToSessionLayer());
	}
}
