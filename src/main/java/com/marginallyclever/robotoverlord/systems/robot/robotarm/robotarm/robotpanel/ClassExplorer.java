package com.marginallyclever.robotoverlord.systems.robot.robotarm.robotarm.robotpanel;

import com.marginallyclever.communications.application.TextInterfaceToListeners;
import com.marginallyclever.communications.application.TextInterfaceToSessionLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger logger = LoggerFactory.getLogger(ClassExplorer.class);

	public static void reportOnClass(Object o) {
		Class<?> c = o.getClass();
		logger.info("Class: "+c.getName());
		//reportClassFields(c);
		//reportClassAnnotations(c);
		reportClassPublicMethods(c);
	}

	@SuppressWarnings("unused")
	private static void reportClassFields(Class<?> c) {
		Field [] fields = c.getFields();
		for(Field f : fields) {
			logger.info("  field: "+f.getName());
		}
	}

	@SuppressWarnings("unused")
	private static void reportClassAnnotations(Class<?> c) {
		Annotation [] annotations = c.getAnnotations();
		for(Annotation a : annotations) {
			logger.info("  annotation: "+a.toString());
		}
	}

	@SuppressWarnings("unused")
	private static void reportClassMethods(Class<?> c) {
		Method [] methods = c.getDeclaredMethods();
		for(Method m : methods) {
			reportClassMethod(m);
            int modifiers = m.getModifiers();
            Modifier.isPublic(modifiers);
            logger.info("    modifiers: "+Modifier.toString(modifiers) + "("+modifiers+")");
            
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
        logger.info("  method: "+m);
        //logger.info("    decl class: " + m.getDeclaringClass());
        Class<?> c = m.getReturnType();
        logger.info("    return type: "+c.getName());
        Parameter [] parameters = m.getParameters();
        for( Parameter p : parameters ) {
        	reportClassMethodParameter(p);
        }
	}

	private static void reportClassMethodParameter(Parameter p) {
		logger.info("    param: "+p);
	}

	public static void main(String[] args) {
		reportOnClass(new TapeDeckPanel());
		reportOnClass(new TextInterfaceToListeners());
		reportOnClass(new TextInterfaceToSessionLayer());
	}
}
