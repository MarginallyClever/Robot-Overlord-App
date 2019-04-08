package com.marginallyclever.robotOverlord;

import org.junit.Test;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

public class JInputTest {
	@Test
	public void reportJavaLibraryPath() {
		String property = System.getProperty("java.library.path");
		System.out.println("java.library.path="+property.replace(";","\n  "));
		
		System.out.println("Working Directory = " + System.getProperty("user.dir"));
	}
	
	@Test
	public void testControllers() {
		Controller[] ca = ControllerEnvironment.getDefaultEnvironment().getControllers();
        for(int i =0;i<ca.length;i++){
            System.out.println("Controller:"+ca[i].getName());
            System.out.println("Type: "+ca[i].getType().toString());

            // Get this controllers components (buttons and axis)
            Component[] components = ca[i].getComponents();
            System.out.println("Component Count: "+components.length);
            for(int j=0;j<components.length;j++){
                System.out.print("\tComponent "+j+": "+components[j].getName());
                System.out.print("\tIdentifier: "+ components[j].getIdentifier().getName());
                System.out.print("\tType: ");
                System.out.print(components[j].isRelative() ? "Relative " : "Absolute ");
                System.out.print(components[j].isAnalog()? "Analog " : "Digital ");
                System.out.println();
            }
        }
	}
}
