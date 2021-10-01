package com.marginallyclever.robotOverlord.sixi3Interface;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

import com.marginallyclever.robotOverlord.sixi3Interface.marlinInterface.TextInterfaceToListeners;
import com.marginallyclever.robotOverlord.sixi3Interface.marlinInterface.TextInterfaceToNetworkSession;

/**
 * Exploring Java reflection and what can be learned about other classes at run time.
 * @author Dan
 *
 */
@Deprecated
public class ClassExplorer {
	public static void reportOnClass(Object o) {
		Class<?> c = o.getClass();
		System.out.println("Class: "+c.getName());
		//reportClassFields(c);
		//reportClassAnnotations(c);
		reportClassPublicMethods(c);
		
		System.out.println();
	}

	@SuppressWarnings("unused")
	private static void reportClassFields(Class<?> c) {
		Field [] fields = c.getFields();
		for(Field f : fields) {
			System.out.println("  field: "+f.getName());
		}
	}

	@SuppressWarnings("unused")
	private static void reportClassAnnotations(Class<?> c) {
		Annotation [] annotations = c.getAnnotations();
		for(Annotation a : annotations) {
			System.out.println("  annotation: "+a.toString());
		}
	}

	@SuppressWarnings("unused")
	private static void reportClassMethods(Class<?> c) {
		Method [] methods = c.getDeclaredMethods();
		for(Method m : methods) {
			reportClassMethod(m);
            int modifiers = m.getModifiers();
            Modifier.isPublic(modifiers);
            System.out.println("    modifiers: "+Modifier.toString(modifiers) + "("+modifiers+")");
            
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
        System.out.println("  method: "+m);
        //System.out.println("    decl class: " + m.getDeclaringClass());
        Class<?> c = m.getReturnType();
        System.out.println("    return type: "+c.getName());
        Parameter [] parameters = m.getParameters();
        for( Parameter p : parameters ) {
        	reportClassMethodParameter(p);
        }
	}

	private static void reportClassMethodParameter(Parameter p) {
		System.out.println("    param: "+p);
	}

	public static void main(String[] args) {
		reportOnClass(new TapeDeckPanel());
		reportOnClass(new TextInterfaceToListeners());
		reportOnClass(new TextInterfaceToNetworkSession());
	}
}
