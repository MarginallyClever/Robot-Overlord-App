package com.marginallyclever.robotOverlord;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Component.Identifier;

public class InputManager {
	// A record of the state of the human input device
	public static final int MAX_KEYS = 20;
	
	public static double [] keyState = new double[MAX_KEYS];
	
	static public void start() {}
	
	static public void update() {
		Controller[] ca = ControllerEnvironment.getDefaultEnvironment().getControllers();

		// reset the key states
		for(int i=0;i<keyState.length;++i) {
			keyState[i]=0;
		}
		
        for(int i=0;i<ca.length;i++){
        	// poll all controllers once per frame
        	if(!ca[i].poll()) {
        		// TODO poll failed, device disconnected?
        		return;
        	}

        	//System.out.println(ca[i].getType());
        	if(ca[i].getType()!=Controller.Type.STICK) {
        		// currently we ignore all but joysticks.
        		continue;
        	}

        	Component[] components = ca[i].getComponents();
            for(int j=0;j<components.length;j++){
            	/*
            	System.out.println("\t"+components[j].getName()+
            			":"+components[j].getIdentifier().getName()+
            			":"+(components[j].isAnalog()?"Abs":"Rel")+
            			":"+(components[j].isAnalog()?"Analog":"Digital")+
            			":"+(components[j].getDeadZone())+
               			":"+(components[j].getPollData()));*/
            	if(!components[j].isAnalog()) {
        			if(components[j].getPollData()==1) {
        				if(components[j].getIdentifier()==Identifier.Button._0) keyState[0] = 1;  // square
        				if(components[j].getIdentifier()==Identifier.Button._1) keyState[1] = 1;  // x
        				if(components[j].getIdentifier()==Identifier.Button._2) keyState[2] = 1;  // circle
        				if(components[j].getIdentifier()==Identifier.Button._3) keyState[3] = 1;  // triangle
        				if(components[j].getIdentifier()==Identifier.Button._4) keyState[4] = 1;  // L1?
        				if(components[j].getIdentifier()==Identifier.Button._5) keyState[5] = 1;  // R1?
        				if(components[j].getIdentifier()==Identifier.Button._8) keyState[6] = 1;  // share button
        				if(components[j].getIdentifier()==Identifier.Button._9) keyState[7] = 1;  // option button
            		}
    				if(components[j].getIdentifier()==Identifier.Axis.POV) {
    					// D-pad buttons
    					float pollData = components[j].getPollData();
							 if(pollData == Component.POV.DOWN ) keyState[8] = -1;
						else if(pollData == Component.POV.UP   ) keyState[8] =  1;
    					else if(pollData == Component.POV.LEFT ) keyState[9] = -1;
    					else if(pollData == Component.POV.RIGHT) keyState[9] =  1;
    				}
            	} else {
            		double v = components[j].getPollData();
            		final double DEADZONE=0.1;
            		double deadzone = DEADZONE;  // components[j].getDeadZone() is very small?
            			 if(v> deadzone) v=(v-deadzone)/(1.0-deadzone);  // scale 0....1
            		else if(v<-deadzone) v=(v+deadzone)/(1.0-deadzone);  // scale 0...-1
            		else continue;  // inside dead zone, ignore.
            		
	            	if(components[j].getIdentifier()==Identifier.Axis.Z ) keyState[10]=v;  // right analog stick, + is right -1 is left
	            	if(components[j].getIdentifier()==Identifier.Axis.RZ) keyState[11]=v;  // right analog stick, + is down -1 is up
	            	if(components[j].getIdentifier()==Identifier.Axis.RY) keyState[12]=v;  // R2, +1 is pressed -1 is unpressed
	            	if(components[j].getIdentifier()==Identifier.Axis.RX) keyState[13]=v;  // L2, +1 is pressed -1 is unpressed
	            	if(components[j].getIdentifier()==Identifier.Axis.X ) keyState[14]=v;  // left analog stick, +1 is right -1 is left
	            	if(components[j].getIdentifier()==Identifier.Axis.Y ) keyState[15]=v;  // left analog stick, -1 is up +1 is down
            	}
        	}
        }
	}
}
