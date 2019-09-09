package com.marginallyclever.robotOverlord;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Component.Identifier;

/**
 * Poll devices and store events we care about in keystate.
 * Eventually keystate should be expanded to hold all input events.
 * @author dan royer
 *
 */
public class InputManager {
	public static final int STICK_SQUARE=0;
	public static final int STICK_X=1;
	public static final int STICK_CIRCLE=2;
	public static final int STICK_TRIANGLE=3;
	public static final int STICK_L1=4;
	public static final int STICK_R1=5;
	public static final int STICK_SHARE=6;
	public static final int STICK_OPTIONS=7;

	public static final int STICK_DPADY =8;
	public static final int STICK_DPADX =9;
	
	public static final int STICK_RX =10;
	public static final int STICK_RY =11;
	public static final int STICK_R2=12;
	public static final int STICK_L2=13;

	public static final int STICK_LX =14;
	public static final int STICK_LY =15;
	public static final int STICK_TOUCHPAD=16;
	
	public static final int MOUSE_X = 17;
	public static final int MOUSE_Y = 18;
	public static final int MOUSE_Z = 19;
	public static final int MOUSE_LEFT  =20;
	public static final int MOUSE_MIDDLE=21;
	public static final int MOUSE_RIGHT =22;
	
	public static final int KEY_DELETE  =23;
	public static final int KEY_RETURN  =24;
	public static final int KEY_LSHIFT  =25;
	public static final int KEY_RSHIFT  =26;
	public static final int KEY_RALT    =27;
	public static final int KEY_LALT    =28;
	public static final int KEY_RCONTROL=29;
	public static final int KEY_LCONTROL=30;
	public static final int KEY_BACKSPACE=31;

	public static final int KEY_ENTER    =32;
	
	public static final int MAX_KEYS = 33;

	protected static double [] keyStateOld = new double[MAX_KEYS];
	protected static double [] keyState = new double[MAX_KEYS];
	
	static public void start() {}
	
	static public void update(boolean isMouseIn) {
		Controller[] ca = ControllerEnvironment.getDefaultEnvironment().getControllers();

		// reset the key states
		for(int i=0;i<MAX_KEYS;++i) {
			keyStateOld[i]=keyState[i];
			keyState[i]=0;
		}
		
        for(int i=0;i<ca.length;i++){
        	// poll all controllers once per frame
        	if(!ca[i].poll()) {
        		// TODO poll failed, device disconnected?
        		return;
        	}

        	//System.out.println(ca[i].getType());
        	if(ca[i].getType()==Controller.Type.STICK) {
	            updateStick(ca[i]);
        	} else if(ca[i].getType()==Controller.Type.MOUSE) {
	            if(isMouseIn) updateMouse(ca[i]);
        	} else if(ca[i].getType()==Controller.Type.KEYBOARD) {
        		updateKeyboard(ca[i]);
        	}
        }
	}
	
	static public boolean isOn(int i) {
		return keyState[i]==1;
	}

	static public boolean isOff(int i) {
		return keyState[i]==0;
	}
	
	static public boolean isPressed(int i) {
		return keyState[i]==1 && keyStateOld[i]==0;
	}
	
	static public boolean isReleased(int i) {
		return keyState[i]==0 && keyStateOld[i]==1;
	}
	
	static public double rawValue(int i) {
		return keyState[i];
	}
	
	static public void updateStick(Controller controller) {
    	Component[] components = controller.getComponents();
        for(int j=0;j<components.length;j++){
        	/*
        	System.out.println("\t"+components[j].getName()+
        			":"+components[j].getIdentifier().getName()+
        			":"+(components[j].isAnalog()?"Abs":"Rel")+
        			":"+(components[j].isAnalog()?"Analog":"Digital")+
        			":"+(components[j].getDeadZone())+
           			":"+(components[j].getPollData()));//*/
        	if(components[j].isAnalog()) {
        		double v = components[j].getPollData();
        		final double DEADZONE=0.1;
        		double deadzone = DEADZONE;  // components[j].getDeadZone() is very small?
        			 if(v> deadzone) v=(v-deadzone)/(1.0-deadzone);  // scale 0....1
        		else if(v<-deadzone) v=(v+deadzone)/(1.0-deadzone);  // scale 0...-1
        		else continue;  // inside dead zone, ignore.
        		
            	if(components[j].getIdentifier()==Identifier.Axis.Z ) keyState[STICK_RX]=v;  // right analog stick, + is right -1 is left
            	if(components[j].getIdentifier()==Identifier.Axis.RZ) keyState[STICK_RY]=v;  // right analog stick, + is down -1 is up
            	if(components[j].getIdentifier()==Identifier.Axis.RY) keyState[STICK_R2]=v;  // R2, +1 is pressed -1 is unpressed
            	if(components[j].getIdentifier()==Identifier.Axis.RX) keyState[STICK_L2]=v;  // L2, +1 is pressed -1 is unpressed
            	if(components[j].getIdentifier()==Identifier.Axis.X ) keyState[STICK_LX]=v;  // left analog stick, +1 is right -1 is left
            	if(components[j].getIdentifier()==Identifier.Axis.Y ) keyState[STICK_LY]=v;  // left analog stick, -1 is up +1 is down
        	} else {
        		// digital
    			if(components[j].getPollData()==1) {
    				if(components[j].getIdentifier()==Identifier.Button._0) keyState[STICK_SQUARE] = 1;  // square
    				if(components[j].getIdentifier()==Identifier.Button._1) keyState[STICK_X] = 1;  // x
    				if(components[j].getIdentifier()==Identifier.Button._2) keyState[STICK_CIRCLE] = 1;  // circle
    				if(components[j].getIdentifier()==Identifier.Button._3) keyState[STICK_TRIANGLE] = 1;  // triangle
    				if(components[j].getIdentifier()==Identifier.Button._4) keyState[STICK_L1] = 1;  // L1?
    				if(components[j].getIdentifier()==Identifier.Button._5) keyState[STICK_R1] = 1;  // R1?
    				if(components[j].getIdentifier()==Identifier.Button._8) keyState[STICK_SHARE] = 1;  // share button
    				if(components[j].getIdentifier()==Identifier.Button._9) keyState[STICK_OPTIONS] = 1;  // option button
    				if(components[j].getIdentifier()==Identifier.Button._13) keyState[STICK_TOUCHPAD] = 1;  // touch pad
        		}
				if(components[j].getIdentifier()==Identifier.Axis.POV) {
					// D-pad buttons
					float pollData = components[j].getPollData();
						 if(pollData == Component.POV.DOWN ) keyState[STICK_DPADY] = -1;
					else if(pollData == Component.POV.UP   ) keyState[STICK_DPADY] =  1;
					else if(pollData == Component.POV.LEFT ) keyState[STICK_DPADX] = -1;
					else if(pollData == Component.POV.RIGHT) keyState[STICK_DPADX] =  1;
				}
        	}
    	}
	}
	
	static public void updateMouse(Controller controller) {
    	Component[] components = controller.getComponents();
        for(int j=0;j<components.length;j++){
        	/*
        	System.out.println("\t"+components[j].getName()+
        			":"+components[j].getIdentifier().getName()+
        			":"+(components[j].isAnalog()?"Abs":"Rel")+
        			":"+(components[j].isAnalog()?"Analog":"Digital")+
        			":"+(components[j].getDeadZone())+
           			":"+(components[j].getPollData()));//*/
        	if(components[j].isAnalog()) {
        		double v = components[j].getPollData();
        		
        		if(components[j].getIdentifier()==Identifier.Axis.X) keyState[MOUSE_X]=v;
    			if(components[j].getIdentifier()==Identifier.Axis.Y) keyState[MOUSE_Y]=v;
    			if(components[j].getIdentifier()==Identifier.Axis.Z) keyState[MOUSE_Z]=v;
        	} else {
        		// digital
    			if(components[j].getPollData()==1) {
    				if(components[j].getIdentifier()==Identifier.Button.LEFT  ) keyState[MOUSE_LEFT  ] = 1;
    				if(components[j].getIdentifier()==Identifier.Button.MIDDLE) keyState[MOUSE_MIDDLE] = 1;
    				if(components[j].getIdentifier()==Identifier.Button.RIGHT ) keyState[MOUSE_RIGHT ] = 1;
    			}
        	}
        }
	}
	
	static public void updateKeyboard(Controller controller) {
    	Component[] components = controller.getComponents();
        for(int j=0;j<components.length;j++){
        	if(!components[j].isAnalog()) {
        		// digital
    			if(components[j].getPollData()==1) {
    				if(components[j].getIdentifier()==Identifier.Key.DELETE  ) keyState[KEY_DELETE  ] = 1;
    				if(components[j].getIdentifier()==Identifier.Key.RETURN  ) keyState[KEY_RETURN  ] = 1;
    				if(components[j].getIdentifier()==Identifier.Key.LSHIFT  ) keyState[KEY_LSHIFT  ] = 1;
    				if(components[j].getIdentifier()==Identifier.Key.RSHIFT  ) keyState[KEY_RSHIFT  ] = 1;
    				if(components[j].getIdentifier()==Identifier.Key.RALT    ) keyState[KEY_RALT    ] = 1;
    				if(components[j].getIdentifier()==Identifier.Key.LALT    ) keyState[KEY_LALT    ] = 1;
    				if(components[j].getIdentifier()==Identifier.Key.RCONTROL) keyState[KEY_RCONTROL] = 1;
    				if(components[j].getIdentifier()==Identifier.Key.LCONTROL) keyState[KEY_LCONTROL] = 1;
    				if(components[j].getIdentifier()==Identifier.Key.BACK    ) keyState[KEY_BACKSPACE] = 1;
    				if(components[j].getIdentifier()==Identifier.Key.NUMPADENTER) keyState[KEY_ENTER] = 1;
    			}
        	}
        }
	}
}
