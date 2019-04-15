package com.marginallyclever.robotOverlord;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

public class InputManager {
	static public void start() {
		//describeAllControllersAndInputs();
	}
	
	static public void update() {
		Controller[] ca = ControllerEnvironment.getDefaultEnvironment().getControllers();
        for(int i =0;i<ca.length;i++){
        	if(!ca[i].poll()) {
        		// TODO poll failed, device disconnected?
        	}
        }
	}

	static public void describeAllControllersAndInputs() {
		Controller[] ca = ControllerEnvironment.getDefaultEnvironment().getControllers();
        for(int i =0;i<ca.length;i++){
            System.out.print("Controller:"+ca[i].getName()+" ("+ca[i].getType().toString()+")");
            if(ca[i].getType()==Controller.Type.UNKNOWN) continue;
            
            Component[] components = ca[i].getComponents();
            for(int j=0;j<components.length;j++){
            	System.out.println("\t"+components[j].getName()+
            			":"+(components[j].isAnalog()?"Abs":"Rel")+
            			":"+(components[j].isAnalog()?"An":"Di")
            				);
            }
        }
	}
}
