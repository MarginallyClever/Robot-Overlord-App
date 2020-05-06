package com.marginallyclever.robotOverlord.swingInterface;

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
		KEY_ESCAPE(50),
		
		KEY_GREATERTHAN(51),
		KEY_LESSTHAN(52),
		KEY_PLUS(53),
		KEY_FRONTSLASH(54),
		KEY_BACKSLASH(55),

		KEY_SPACE(56),
		KEY_TILDE(57),

		KEY_F1(58),
		KEY_F2(59),
		KEY_F3(60),
		KEY_F4(61),
		KEY_F5(62),
		KEY_F6(63),
		KEY_F7(64),
		KEY_F8(65),
		KEY_F9(66),
		KEY_F10(67),
		KEY_F11(68),
		KEY_F12(69);
		
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

		advanceKeyStates();
		
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
	
	static public double getRawValue(Source i) {
		return keyState[i.getValue()];
	}
	
	static protected void setRawValue(Source i,double value) {
		int v = i.getValue();
		//keyStateOld[v]=keyState[v];
		keyState[v]=value;
	}

	static protected void advanceKeyStates() {
		for(int i=0;i<Source.values().length;++i) {
			keyStateOld[i]=keyState[i];
			keyState[i]=0;
		}
	}

	static protected void resetKeyStates() {
		for(int i=0;i<Source.values().length;++i) {
			keyStateOld[i]=0;
			keyState[i]=0;
		}
	}

	static public void updateStick(Controller controller) {
    	//*
		Component[] components = controller.getComponents();
        for(int j=0;j<components.length;j++){
			Component c = components[j];
		/*/
		EventQueue queue = controller.getEventQueue();
		Event event = new Event();
		while(queue.getNextEvent(event)) {
			Component c = event.getComponent();
			//*/
        	/*
        	System.out.println("\t"+c.getName()+
        			":"+c.getIdentifier().getName()+
        			":"+(c.isAnalog()?"Abs":"Rel")+
        			":"+(c.isAnalog()?"Analog":"Digital")+
        			":"+(c.getDeadZone())+
           			":"+(c.getPollData()));//*/
        	if(c.isAnalog()) {
        		double v = c.getPollData();
        		final double DEADZONE=0.1;
        		double deadzone = DEADZONE;  // c.getDeadZone() is very small?
        			 if(v> deadzone) v=(v-deadzone)/(1.0-deadzone);  // scale 0....1
        		else if(v<-deadzone) v=(v+deadzone)/(1.0-deadzone);  // scale 0...-1
        		else continue;  // inside dead zone, ignore.
        		
            	if(c.getIdentifier()==Identifier.Axis.Z ) setRawValue(Source.STICK_RX,v);  // right analog stick, + is right -1 is left
            	if(c.getIdentifier()==Identifier.Axis.RZ) setRawValue(Source.STICK_RY,v);  // right analog stick, + is down -1 is up
            	if(c.getIdentifier()==Identifier.Axis.RY) setRawValue(Source.STICK_R2,v);  // R2, +1 is pressed -1 is unpressed
            	if(c.getIdentifier()==Identifier.Axis.RX) setRawValue(Source.STICK_L2,v);  // L2, +1 is pressed -1 is unpressed
            	if(c.getIdentifier()==Identifier.Axis.X ) setRawValue(Source.STICK_LX,v);  // left analog stick, +1 is right -1 is left
            	if(c.getIdentifier()==Identifier.Axis.Y ) setRawValue(Source.STICK_LY,v);  // left analog stick, -1 is up +1 is down
        	} else {
        		// digital
    			if(c.getPollData()==1) {
    				if(c.getIdentifier()==Identifier.Button._0 ) setRawValue(Source.STICK_SQUARE,1);  // square
    				if(c.getIdentifier()==Identifier.Button._1 ) setRawValue(Source.STICK_X,1);  // x
    				if(c.getIdentifier()==Identifier.Button._2 ) setRawValue(Source.STICK_CIRCLE,1);  // circle
    				if(c.getIdentifier()==Identifier.Button._3 ) setRawValue(Source.STICK_TRIANGLE,1);  // triangle
    				if(c.getIdentifier()==Identifier.Button._4 ) setRawValue(Source.STICK_L1,1);  // L1?
    				if(c.getIdentifier()==Identifier.Button._5 ) setRawValue(Source.STICK_R1,1);  // R1?
    				if(c.getIdentifier()==Identifier.Button._8 ) setRawValue(Source.STICK_SHARE,1);  // share button
    				if(c.getIdentifier()==Identifier.Button._9 ) setRawValue(Source.STICK_OPTIONS,1);  // option button
    				if(c.getIdentifier()==Identifier.Button._13) setRawValue(Source.STICK_TOUCHPAD,1);  // touch pad
        		}
				if(c.getIdentifier()==Identifier.Axis.POV) {
					// D-pad buttons
					float pollData = c.getPollData();
						 if(pollData == Component.POV.DOWN ) setRawValue(Source.STICK_DPADY,-1);
					else if(pollData == Component.POV.UP   ) setRawValue(Source.STICK_DPADY,1);
					else if(pollData == Component.POV.LEFT ) setRawValue(Source.STICK_DPADX,-1);
					else if(pollData == Component.POV.RIGHT) setRawValue(Source.STICK_DPADX,1);
				}
        	}
    	}
	}
	
	static public void updateMouse(Controller controller) {
    	//*
		Component[] components = controller.getComponents();
        for(int j=0;j<components.length;j++){
			Component c = components[j];
		/*/
		EventQueue queue = controller.getEventQueue();
		Event event = new Event();
		while(queue.getNextEvent(event)) {
			Component c = event.getComponent();
			//*/
        	/*
        	System.out.println("\t"+c.getName()+
        			":"+c.getIdentifier().getName()+
        			":"+(c.isAnalog()?"Abs":"Rel")+
        			":"+(c.isAnalog()?"Analog":"Digital")+
        			":"+(c.getDeadZone())+
           			":"+(c.getPollData()));//*/
        	if(c.isAnalog()) {
        		double v = c.getPollData();
        		
        		if(c.getIdentifier()==Identifier.Axis.X) setRawValue(Source.MOUSE_X,v);
    			if(c.getIdentifier()==Identifier.Axis.Y) setRawValue(Source.MOUSE_Y,v);
    			if(c.getIdentifier()==Identifier.Axis.Z) setRawValue(Source.MOUSE_Z,v);
        	} else {
        		// digital
    			if(c.getPollData()==1) {
    				if(c.getIdentifier()==Identifier.Button.LEFT  ) setRawValue(Source.MOUSE_LEFT,1);
    				if(c.getIdentifier()==Identifier.Button.MIDDLE) setRawValue(Source.MOUSE_MIDDLE,1);
    				if(c.getIdentifier()==Identifier.Button.RIGHT ) setRawValue(Source.MOUSE_RIGHT,1);
    			}
        	}
        }
	}
	
	static public void updateKeyboard(Controller controller) {
    	//*
		Component[] components = controller.getComponents();
        for(int j=0;j<components.length;j++){
			Component c = components[j];
		/*/
		EventQueue queue = controller.getEventQueue();
		Event event = new Event();
		while(queue.getNextEvent(event)) {
			Component c = event.getComponent();
			//*/

        	/*
        	System.out.println("\t"+c.getName()+
        			":"+c.getIdentifier().getName()+
        			":"+(c.isAnalog()?"Abs":"Rel")+
        			":"+(c.isAnalog()?"Analog":"Digital")+
        			":"+(c.getDeadZone())+
           			":"+(c.getPollData()));//*/
        	
        	if(!c.isAnalog()) {
        		// digital
    			if(c.getPollData()==1) {
    				//System.out.println(c.getIdentifier().getName());
    				if(c.getIdentifier()==Identifier.Key.DELETE  ) setRawValue(Source.KEY_DELETE,1);
    				if(c.getIdentifier()==Identifier.Key.RETURN  ) setRawValue(Source.KEY_RETURN,1);
    				if(c.getIdentifier()==Identifier.Key.LSHIFT  ) setRawValue(Source.KEY_LSHIFT,1);
    				if(c.getIdentifier()==Identifier.Key.RSHIFT  ) setRawValue(Source.KEY_RSHIFT,1);
    				if(c.getIdentifier()==Identifier.Key.RALT    ) setRawValue(Source.KEY_RALT,1);
    				if(c.getIdentifier()==Identifier.Key.LALT    ) setRawValue(Source.KEY_LALT,1);
    				if(c.getIdentifier()==Identifier.Key.RCONTROL) setRawValue(Source.KEY_RCONTROL,1);
    				if(c.getIdentifier()==Identifier.Key.LCONTROL) setRawValue(Source.KEY_LCONTROL,1);
    				if(c.getIdentifier()==Identifier.Key.BACK    ) setRawValue(Source.KEY_BACKSPACE,1);
    				if(c.getIdentifier()==Identifier.Key.Q    ) setRawValue(Source.KEY_Q,1);
    				if(c.getIdentifier()==Identifier.Key.E    ) setRawValue(Source.KEY_E,1);
    				if(c.getIdentifier()==Identifier.Key.W    ) setRawValue(Source.KEY_W,1);
    				if(c.getIdentifier()==Identifier.Key.A    ) setRawValue(Source.KEY_A,1);
    				if(c.getIdentifier()==Identifier.Key.S    ) setRawValue(Source.KEY_S,1);
    				if(c.getIdentifier()==Identifier.Key.D    ) setRawValue(Source.KEY_D,1);
    				if(c.getIdentifier()==Identifier.Key.NUMPADENTER) setRawValue(Source.KEY_ENTER,1);
    				if(c.getIdentifier()==Identifier.Key.TAB) setRawValue(Source.KEY_TAB,1);

    				if(c.getIdentifier()==Identifier.Key._0) setRawValue(Source.KEY_0,1);
    				if(c.getIdentifier()==Identifier.Key._1) setRawValue(Source.KEY_1,1);
    				if(c.getIdentifier()==Identifier.Key._2) setRawValue(Source.KEY_2,1);
    				if(c.getIdentifier()==Identifier.Key._3) setRawValue(Source.KEY_3,1);
    				if(c.getIdentifier()==Identifier.Key._4) setRawValue(Source.KEY_4,1);
    				if(c.getIdentifier()==Identifier.Key._5) setRawValue(Source.KEY_5,1);
    				if(c.getIdentifier()==Identifier.Key._6) setRawValue(Source.KEY_6,1);
    				if(c.getIdentifier()==Identifier.Key._7) setRawValue(Source.KEY_7,1);
    				if(c.getIdentifier()==Identifier.Key._8) setRawValue(Source.KEY_8,1);
    				if(c.getIdentifier()==Identifier.Key._9) setRawValue(Source.KEY_9,1);

    				if(c.getIdentifier()==Identifier.Key.ESCAPE) setRawValue(Source.KEY_ESCAPE,1);
    				if(c.getIdentifier()==Identifier.Key.SLASH) setRawValue(Source.KEY_FRONTSLASH,1);
    				if(c.getIdentifier()==Identifier.Key.BACKSLASH) setRawValue(Source.KEY_BACKSLASH,1);
    				if(c.getIdentifier()==Identifier.Key.EQUALS) setRawValue(Source.KEY_PLUS,1);
    				if(c.getIdentifier()==Identifier.Key.ADD) setRawValue(Source.KEY_PLUS,1);
    				if(c.getIdentifier()==Identifier.Key.COMMA) setRawValue(Source.KEY_LESSTHAN,1);
    				if(c.getIdentifier()==Identifier.Key.PERIOD) setRawValue(Source.KEY_GREATERTHAN,1);
    				
    				if(c.getIdentifier()==Identifier.Key.SPACE) setRawValue(Source.KEY_SPACE,1);
    				if(c.getIdentifier()==Identifier.Key.GRAVE) setRawValue(Source.KEY_TILDE,1);

    				if(c.getIdentifier()==Identifier.Key.F1) setRawValue(Source.KEY_F1,1);
    				if(c.getIdentifier()==Identifier.Key.F2) setRawValue(Source.KEY_F2,1);
    				if(c.getIdentifier()==Identifier.Key.F3) setRawValue(Source.KEY_F3,1);
    				if(c.getIdentifier()==Identifier.Key.F4) setRawValue(Source.KEY_F4,1);
    				if(c.getIdentifier()==Identifier.Key.F5) setRawValue(Source.KEY_F5,1);
    				if(c.getIdentifier()==Identifier.Key.F6) setRawValue(Source.KEY_F6,1);
    				if(c.getIdentifier()==Identifier.Key.F7) setRawValue(Source.KEY_F7,1);
    				if(c.getIdentifier()==Identifier.Key.F8) setRawValue(Source.KEY_F8,1);
    				if(c.getIdentifier()==Identifier.Key.F9) setRawValue(Source.KEY_F9,1);
    				if(c.getIdentifier()==Identifier.Key.F10) setRawValue(Source.KEY_F10,1);
    				if(c.getIdentifier()==Identifier.Key.F11) setRawValue(Source.KEY_F11,1);
    				if(c.getIdentifier()==Identifier.Key.F12) setRawValue(Source.KEY_F12,1);
    			}
        	}
        }
	}
	
	public static void lostFocus() {
		resetKeyStates();
	}
}
