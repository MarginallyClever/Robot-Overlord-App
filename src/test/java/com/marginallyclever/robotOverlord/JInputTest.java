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
            Component[] components = ca[i].getComponents();

            System.out.println("Controller:"+ca[i].getName()+" "+ca[i].getType().toString());
            System.out.println("Component Count: "+components.length);

            // Get this controllers components (buttons and axis)
            for(int j=0;j<components.length;j++){
                System.out.print("\t"+j+": "+components[j].getName());
                System.out.print(" \""+ components[j].getIdentifier().getName()+"\" (");
                System.out.print(components[j].isRelative() ? "Relative " : "Absolute ");
                System.out.print(components[j].isAnalog()? "Analog " : "Digital ");
                System.out.println(")");
            }
        }
	}
}
