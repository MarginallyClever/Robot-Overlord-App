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
	public enum Source {
		STICK_SQUARE(0),
		STICK_X(1),
		STICK_CIRCLE(2),
		STICK_TRIANGLE(3),
		STICK_L1(4),
		STICK_R1(5),
		STICK_SHARE(6),
		STICK_OPTIONS(7),
	
		STICK_DPADY(8),
		STICK_DPADX(9),
		
		STICK_RX(10),
		STICK_RY(11),
		STICK_R2(12),
		STICK_L2(13),
	
		STICK_LX(14),
		STICK_LY(15),
		STICK_TOUCHPAD(16),
		
		MOUSE_X(17),
		MOUSE_Y(18),
		MOUSE_Z(19),
		MOUSE_LEFT(20),
		MOUSE_MIDDLE(21),
		MOUSE_RIGHT(22),
		
		KEY_DELETE(23),
		KEY_RETURN(24),
		KEY_LSHIFT(25),
		KEY_RSHIFT(26),
		KEY_RALT(27),
		KEY_LALT(28),
		KEY_RCONTROL(29),
		KEY_LCONTROL(30),
		KEY_BACKSPACE(31),
		KEY_W(32),
		KEY_A(33),
		KEY_S(34),
		KEY_D(35),
		KEY_Q(36),
		KEY_E(37),
		
		KEY_ENTER(38),
		KEY_TAB(39),
	
		KEY_1(40),
		KEY_2(41),
		KEY_3(42),
		KEY_4(43),
		KEY_5(44),
		KEY_6(45),
		KEY_7(46),
		KEY_8(47),
		KEY_9(48),
		KEY_0(49),
		KEY_ESCAPE(50);
		
		private final int value;
		private Source(int v) {
			value=v;
		}
		public int getValue() {
			return value;
		}
	};
	

	protected static double [] keyStateOld = new double[Source.values().length];
	protected static double [] keyState = new double[Source.values().length];
	
	static public void start() {}
	
	static public void update(boolean isMouseIn) {
		Controller[] ca = ControllerEnvironment.getDefaultEnvironment().getControllers();

		// reset the key states
		for(int i=0;i<Source.values().length;++i) {
			keyStateOld[i]=keyState[i];
			keyState[i]=0;
		}
		
		int numMice=0;
		//int numSticks=0;
		//int numKeyboard=0;
		
        for(int i=0;i<ca.length;i++){
        	// poll all controllers once per frame
        	if(!ca[i].poll()) {
        		// TODO poll failed, device disconnected?
        		return;
        	}

        	//System.out.println(ca[i].getType());
        	if(ca[i].getType()==Controller.Type.MOUSE) {
        		if(numMice==0) {
    	            if(isMouseIn) updateMouse(ca[i]);
        		}
        		++numMice;
        	} else if(ca[i].getType()==Controller.Type.STICK) {
        		//if(numSticks==0) {
        			updateStick(ca[i]);
        		//}
        		//++numSticks;
        	} else if(ca[i].getType()==Controller.Type.KEYBOARD) {
        		//if(numKeyboard==0) {
            		updateKeyboard(ca[i]);
        		//}
        		//++numKeyboard;
        	}
        }
        //System.out.println(numSticks+"/"+numMice+"/"+numKeyboard);
	}
	
	static public boolean isOn(Source i) {
		return keyState[i.getValue()]==1;
	}

	static public boolean isOff(Source i) {
		return keyState[i.getValue()]==0;
	}
	
	static public boolean isPressed(Source i) {
		return keyState[i.getValue()]==1 && keyStateOld[i.getValue()]==0;
	}
	
	static public boolean isReleased(Source i) {
		return keyState[i.getValue()]==0 && keyStateOld[i.getValue()]==1;
	}
	
	static public double rawValue(Source i) {
		return keyState[i.getValue()];
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
        		
            	if(components[j].getIdentifier()==Identifier.Axis.Z ) keyState[Source.STICK_RX.getValue()]=v;  // right analog stick, + is right -1 is left
            	if(components[j].getIdentifier()==Identifier.Axis.RZ) keyState[Source.STICK_RY.getValue()]=v;  // right analog stick, + is down -1 is up
            	if(components[j].getIdentifier()==Identifier.Axis.RY) keyState[Source.STICK_R2.getValue()]=v;  // R2, +1 is pressed -1 is unpressed
            	if(components[j].getIdentifier()==Identifier.Axis.RX) keyState[Source.STICK_L2.getValue()]=v;  // L2, +1 is pressed -1 is unpressed
            	if(components[j].getIdentifier()==Identifier.Axis.X ) keyState[Source.STICK_LX.getValue()]=v;  // left analog stick, +1 is right -1 is left
            	if(components[j].getIdentifier()==Identifier.Axis.Y ) keyState[Source.STICK_LY.getValue()]=v;  // left analog stick, -1 is up +1 is down
        	} else {
        		// digital
    			if(components[j].getPollData()==1) {
    				if(components[j].getIdentifier()==Identifier.Button._0 ) keyState[Source.STICK_SQUARE	.getValue()] = 1;  // square
    				if(components[j].getIdentifier()==Identifier.Button._1 ) keyState[Source.STICK_X		.getValue()] = 1;  // x
    				if(components[j].getIdentifier()==Identifier.Button._2 ) keyState[Source.STICK_CIRCLE	.getValue()] = 1;  // circle
    				if(components[j].getIdentifier()==Identifier.Button._3 ) keyState[Source.STICK_TRIANGLE	.getValue()] = 1;  // triangle
    				if(components[j].getIdentifier()==Identifier.Button._4 ) keyState[Source.STICK_L1		.getValue()] = 1;  // L1?
    				if(components[j].getIdentifier()==Identifier.Button._5 ) keyState[Source.STICK_R1		.getValue()] = 1;  // R1?
    				if(components[j].getIdentifier()==Identifier.Button._8 ) keyState[Source.STICK_SHARE	.getValue()] = 1;  // share button
    				if(components[j].getIdentifier()==Identifier.Button._9 ) keyState[Source.STICK_OPTIONS	.getValue()] = 1;  // option button
    				if(components[j].getIdentifier()==Identifier.Button._13) keyState[Source.STICK_TOUCHPAD	.getValue()] = 1;  // touch pad
        		}
				if(components[j].getIdentifier()==Identifier.Axis.POV) {
					// D-pad buttons
					float pollData = components[j].getPollData();
						 if(pollData == Component.POV.DOWN ) keyState[Source.STICK_DPADY.getValue()] = -1;
					else if(pollData == Component.POV.UP   ) keyState[Source.STICK_DPADY.getValue()] =  1;
					else if(pollData == Component.POV.LEFT ) keyState[Source.STICK_DPADX.getValue()] = -1;
					else if(pollData == Component.POV.RIGHT) keyState[Source.STICK_DPADX.getValue()] =  1;
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
        		
        		if(components[j].getIdentifier()==Identifier.Axis.X) keyState[Source.MOUSE_X.getValue()]=v;
    			if(components[j].getIdentifier()==Identifier.Axis.Y) keyState[Source.MOUSE_Y.getValue()]=v;
    			if(components[j].getIdentifier()==Identifier.Axis.Z) keyState[Source.MOUSE_Z.getValue()]=v;
        	} else {
        		// digital
    			if(components[j].getPollData()==1) {
    				if(components[j].getIdentifier()==Identifier.Button.LEFT  ) keyState[Source.MOUSE_LEFT  .getValue()] = 1;
    				if(components[j].getIdentifier()==Identifier.Button.MIDDLE) keyState[Source.MOUSE_MIDDLE.getValue()] = 1;
    				if(components[j].getIdentifier()==Identifier.Button.RIGHT ) keyState[Source.MOUSE_RIGHT .getValue()] = 1;
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
    				if(components[j].getIdentifier()==Identifier.Key.DELETE  ) keyState[Source.KEY_DELETE  .getValue()] = 1;
    				if(components[j].getIdentifier()==Identifier.Key.RETURN  ) keyState[Source.KEY_RETURN  .getValue()] = 1;
    				if(components[j].getIdentifier()==Identifier.Key.LSHIFT  ) keyState[Source.KEY_LSHIFT  .getValue()] = 1;
    				if(components[j].getIdentifier()==Identifier.Key.RSHIFT  ) keyState[Source.KEY_RSHIFT  .getValue()] = 1;
    				if(components[j].getIdentifier()==Identifier.Key.RALT    ) keyState[Source.KEY_RALT    .getValue()] = 1;
    				if(components[j].getIdentifier()==Identifier.Key.LALT    ) keyState[Source.KEY_LALT    .getValue()] = 1;
    				if(components[j].getIdentifier()==Identifier.Key.RCONTROL) keyState[Source.KEY_RCONTROL.getValue()] = 1;
    				if(components[j].getIdentifier()==Identifier.Key.LCONTROL) keyState[Source.KEY_LCONTROL.getValue()] = 1;
    				if(components[j].getIdentifier()==Identifier.Key.BACK    ) keyState[Source.KEY_BACKSPACE.getValue()] = 1;
    				if(components[j].getIdentifier()==Identifier.Key.Q    ) keyState[Source.KEY_Q.getValue()] = 1;
    				if(components[j].getIdentifier()==Identifier.Key.E    ) keyState[Source.KEY_E.getValue()] = 1;
    				if(components[j].getIdentifier()==Identifier.Key.W    ) keyState[Source.KEY_W.getValue()] = 1;
    				if(components[j].getIdentifier()==Identifier.Key.A    ) keyState[Source.KEY_A.getValue()] = 1;
    				if(components[j].getIdentifier()==Identifier.Key.S    ) keyState[Source.KEY_S.getValue()] = 1;
    				if(components[j].getIdentifier()==Identifier.Key.D    ) keyState[Source.KEY_D.getValue()] = 1;
    				if(components[j].getIdentifier()==Identifier.Key.NUMPADENTER) keyState[Source.KEY_ENTER.getValue()] = 1;
    				if(components[j].getIdentifier()==Identifier.Key.TAB) keyState[Source.KEY_TAB.getValue()] = 1;

    				if(components[j].getIdentifier()==Identifier.Key._0) keyState[Source.KEY_0.getValue()] = 1;
    				if(components[j].getIdentifier()==Identifier.Key._1) keyState[Source.KEY_1.getValue()] = 1;
    				if(components[j].getIdentifier()==Identifier.Key._2) keyState[Source.KEY_2.getValue()] = 1;
    				if(components[j].getIdentifier()==Identifier.Key._3) keyState[Source.KEY_3.getValue()] = 1;
    				if(components[j].getIdentifier()==Identifier.Key._4) keyState[Source.KEY_4.getValue()] = 1;
    				if(components[j].getIdentifier()==Identifier.Key._5) keyState[Source.KEY_5.getValue()] = 1;
    				if(components[j].getIdentifier()==Identifier.Key._6) keyState[Source.KEY_6.getValue()] = 1;
    				if(components[j].getIdentifier()==Identifier.Key._7) keyState[Source.KEY_7.getValue()] = 1;
    				if(components[j].getIdentifier()==Identifier.Key._8) keyState[Source.KEY_8.getValue()] = 1;
    				if(components[j].getIdentifier()==Identifier.Key._9) keyState[Source.KEY_9.getValue()] = 1;

    				if(components[j].getIdentifier()==Identifier.Key.ESCAPE) keyState[Source.KEY_ESCAPE.getValue()] = 1;
    			}
        	}
        }
	}
}
