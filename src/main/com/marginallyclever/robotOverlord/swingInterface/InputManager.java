package com.marginallyclever.robotOverlord.swingInterface;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import com.marginallyclever.convenience.log.Log;

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
		STICK_X(0),
		STICK_CIRCLE(1),
		STICK_SQUARE(2),
		STICK_TRIANGLE(3),
		STICK_L1(4),
		STICK_R1(5),
		STICK_SHARE(6),
		STICK_OPTIONS(7),
		
		STICK_L3(8),
		STICK_R3(9),
		
		STICK_DPAD(10),
		STICK_DPAD_U(70),
		STICK_DPAD_R(71),
		STICK_DPAD_D(72),
		STICK_DPAD_L(73),
//		STICK_DPADX(11),
		
		STICK_RX(11),
		STICK_RY(12),
//		STICK_R2(12),
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
	
	private static double [] keyStateOld = new double[Source.values().length];
	private static double [] keyState = new double[Source.values().length];
	private static boolean hasFocus=false;
	private static ArrayList<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();
	private static InputManager self = new InputManager();
	
	public static void addPropertyChangeListener(PropertyChangeListener p) {
		listeners.add(p);
	}
	
	public static void removePropertyChangeListener(PropertyChangeListener p) {
		listeners.remove(p);
	}
	
	private static void firePropertyChangeEvent(PropertyChangeEvent evt) {
		for(PropertyChangeListener p : listeners) {
			p.propertyChange(evt);
		}
	}
	
	public static void start() {
		Log.message("InputManager start");
		
		String libPath = System.getProperty("net.java.games.input.librarypath");
		Log.message("INPUT library path="+libPath);
		if(libPath==null) {
			Log.message("Trying to force local path, likely to fail.");
			System.setProperty("net.java.games.input.librarypath", System.getProperty("user.dir"));
		}
		//Log.message(System.mapLibraryName(""));

		listAllControllers();
	}

	// Output all controllers, their components, and some details about those components.
	public static void listAllControllers() {
		ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
		Log.message("supported="+ce.isSupported());
		
		
		Controller[] ca = ce.getControllers();
        for(int i =0;i<ca.length;i++) {
            Component[] components = ca[i].getComponents();

            Log.message("Controller "+i+":"+ca[i].getName()+" ("+ca[i].getType().toString()+")");
            for(int j=0;j<components.length;j++){
            	Log.message("    "+j+": "+components[j].getName() + " " + components[j].getIdentifier().getName()
            			+" (" + (components[j].isRelative() ? "Relative" : "Absolute")
                		+ " " + (components[j].isAnalog()? "Analog" : "Digital")
                		+ ")");
            }
        }
	}
	
	public static void update(boolean isMouseIn) {
		if(!hasFocus) return;
		
		Controller[] ca = ControllerEnvironment.getDefaultEnvironment().getControllers();

		updateOldKeyStates();
		
        for(int i=0;i<ca.length;i++) {
        	// poll all controllers once per frame
        	if(!ca[i].poll()) {
        		continue;  // TODO poll failed, device disconnected?
        	}

        	if(ca[i].getType()==Controller.Type.MOUSE) {
        		// only listen to the first mouse and only listen if it's in
        		if(isMouseIn) updateMouse(ca[i]);
        	} else if(ca[i].getType()==Controller.Type.GAMEPAD) {
       			updateStick(ca[i]);
        	} else if(ca[i].getType()==Controller.Type.KEYBOARD) {
           		updateKeyboard(ca[i]);
        	}
        }
        
        firePropertyChangeEvent(new PropertyChangeEvent(self,"input",null,null));
	}
	
	public static boolean isOn(Source i) {
		return keyState[i.getValue()]==1;
	}

	public static boolean isOff(Source i) {
		return keyState[i.getValue()]==0;
	}
	
	public static boolean isPressed(Source i) {
		return keyState[i.getValue()]==1 && keyStateOld[i.getValue()]==0;
	}
	
	public static boolean isReleased(Source i) {
		return keyState[i.getValue()]==0 && keyStateOld[i.getValue()]==1;
	}
	
	public static double getRawValue(Source i) {
		return keyState[i.getValue()];
	}
	
	static private void setRawValue(Source i,double value) {
		int v = i.getValue();
		keyState[v] = value;
	}
	
	static private void addRawValue(Source i,double value) {
		int v = i.getValue();
		keyState[v] += value;
	}

	static private void updateOldKeyStates() {
		for(int i=0;i<keyState.length;++i) {
			keyStateOld[i] = keyState[i];
			keyState[i] = 0;
		}
	}

	static private void resetKeyStates() {
		for(int i=0;i<keyState.length;++i) {
			keyStateOld[i] = 0;
			keyState[i] = 0;
		}
	}

	public static void updateStick(Controller controller) {
    	//*
		Component[] components = controller.getComponents();
        for(int j=0;j<components.length;j++){
			Component c = components[j];
			Identifier cid = c.getIdentifier();
		/*/
		EventQueue queue = controller.getEventQueue();
		Event event = new Event();
		while(queue.getNextEvent(event)) {
			Component c = event.getComponent();
			//*/
//			if(Math.abs(c.getPollData()) > 0.1) {
//				Log.message("\t"+c.getName()+
//	        			":"+cid.getName()+
//	        			":"+(c.isAnalog()?"Abs":"Rel")+
//	        			":"+(c.isAnalog()?"Analog":"Digital")+
//	        			":"+(c.getDeadZone())+
//	           			":"+(c.getPollData()));//
//			}
        	if(c.isAnalog()) {
        		double v = c.getPollData();
        		final double DEADZONE=0.1;
        		double deadzone = DEADZONE;  // c.getDeadZone() is very small?
        			 if(v> deadzone) v=(v-deadzone)/(1.0-deadzone);  // scale 0....1
        		else if(v<-deadzone) v=(v+deadzone)/(1.0-deadzone);  // scale 0...-1
        		else continue;  // inside dead zone, ignore.
        			 
                     if(cid==Identifier.Axis.X ) setRawValue(Source.STICK_LX,v);
                else if(cid==Identifier.Axis.Y ) setRawValue(Source.STICK_LY,v);  
                else if(cid==Identifier.Axis.RX) setRawValue(Source.STICK_RX,v);
                else if(cid==Identifier.Axis.RY) setRawValue(Source.STICK_RY,v);  
                else if(cid==Identifier.Axis.Z ) setRawValue(Source.STICK_L2,v);
        	} else {
        		// digital
    			if(c.getPollData()==1) {
    				     if(cid==Identifier.Button._0 ) setRawValue(Source.STICK_X,1); 
    				else if(cid==Identifier.Button._1 ) setRawValue(Source.STICK_CIRCLE,1);  	
    				else if(cid==Identifier.Button._2 ) setRawValue(Source.STICK_SQUARE,1);  	
    				else if(cid==Identifier.Button._3 ) setRawValue(Source.STICK_TRIANGLE,1);  
    				else if(cid==Identifier.Button._4 ) setRawValue(Source.STICK_L1,1);  		
    				else if(cid==Identifier.Button._5 ) setRawValue(Source.STICK_R1,1);  		
    				else if(cid==Identifier.Button._6 ) setRawValue(Source.STICK_SHARE,1);  	
    				else if(cid==Identifier.Button._7 ) setRawValue(Source.STICK_OPTIONS,1);  	
    				else if(cid==Identifier.Button._8 ) setRawValue(Source.STICK_L3,1);  	
    				else if(cid==Identifier.Button._9 ) setRawValue(Source.STICK_R3,1); 
//    				else if(cid==Identifier.Button._13) setRawValue(Source.STICK_TOUCHPAD,1);  // touch pad
        		}
				if(cid==Identifier.Axis.POV) {
					float pollData = c.getPollData();
//					setRawValue(Source.STICK_DPAD,pollData);	// "UP"= 0.25, "RIGHT"= 0.5, "DOWN"=0.75, "LEFT"= 1.0
					     if(pollData == Component.POV.UP   ) setRawValue(Source.STICK_DPAD_U,1);
					else if(pollData == Component.POV.RIGHT) setRawValue(Source.STICK_DPAD_R,1);
					else if(pollData == Component.POV.DOWN ) setRawValue(Source.STICK_DPAD_D,1);
					else if(pollData == Component.POV.LEFT ) setRawValue(Source.STICK_DPAD_L,1);
				}
        	}
    	}
	}
	
	public static void updateMouse(Controller controller) {
    	//*
		Component[] components = controller.getComponents();
        for(int j=0;j<components.length;j++){
			Component c = components[j];
			Identifier cid = c.getIdentifier();
		/*/
		EventQueue queue = controller.getEventQueue();
		Event event = new Event();
		while(queue.getNextEvent(event)) {
			Component c = event.getComponent();
			//*/
        	/*
        	Log.message("\t"+c.getName()+
        			":"+cid.getName()+
        			":"+(c.isAnalog()?"Abs":"Rel")+
        			":"+(c.isAnalog()?"Analog":"Digital")+
        			":"+(c.getDeadZone())+
           			":"+(c.getPollData()));//*/
    		if(c.isAnalog()) {
        		double v = c.getPollData();
        		if(cid==Identifier.Axis.X) {
        			setRawValue(Source.MOUSE_X,v);
        			//System.out.println("mx="+v);
        		}
    			if(cid==Identifier.Axis.Y) {
    				setRawValue(Source.MOUSE_Y,v);
        			//System.out.println("my="+v);
    			}
    			if(cid==Identifier.Axis.Z) {
    				setRawValue(Source.MOUSE_Z,v);
        			//System.out.println("mz="+v);
    			}
        	} else {
        		// digital
    			if(c.getPollData()==1) {
    				if(cid==Identifier.Button.LEFT  ) {
    					addRawValue(Source.MOUSE_LEFT,1);
            			//System.out.println("ml="+v);
    				}
    				if(cid==Identifier.Button.MIDDLE) {
    					addRawValue(Source.MOUSE_MIDDLE,1);
            			//System.out.println("mm="+v);
    				}
    				if(cid==Identifier.Button.RIGHT ) {
    					addRawValue(Source.MOUSE_RIGHT,1);
            			//System.out.println("mr="+v);
    				}
    			}
        	}
        }
	}
	
	public static void updateKeyboard(Controller controller) {
    	//*
		Component[] components = controller.getComponents();
        for(int j=0;j<components.length;j++) {
			Component c = components[j];
			Identifier cid = c.getIdentifier();
		/*/
		EventQueue queue = controller.getEventQueue();
		Event event = new Event();
		while(queue.getNextEvent(event)) {
			Component c = event.getComponent();
			//*/

        	/*
        	Log.message("\t"+c.getName()+
        			":"+cid.getName()+
        			":"+(c.isAnalog()?"Abs":"Rel")+
        			":"+(c.isAnalog()?"Analog":"Digital")+
        			":"+(c.getDeadZone())+
           			":"+(c.getPollData()));//*/
        	if(!c.isAnalog()) {
        		// digital
    			if(c.getPollData()==1) {
    				//Log.message(cid.getName());
    				     if(cid==Identifier.Key.DELETE ) setRawValue(Source.KEY_DELETE,1);
    				else if(cid==Identifier.Key.RETURN ) setRawValue(Source.KEY_RETURN,1);
    				else if(cid==Identifier.Key.LSHIFT ) setRawValue(Source.KEY_LSHIFT,1);
    				else if(cid==Identifier.Key.RSHIFT ) setRawValue(Source.KEY_RSHIFT,1);
    				else if(cid==Identifier.Key.RALT ) setRawValue(Source.KEY_RALT,1);
    				else if(cid==Identifier.Key.LALT ) setRawValue(Source.KEY_LALT,1);
    				else if(cid==Identifier.Key.RCONTROL) setRawValue(Source.KEY_RCONTROL,1);
    				else if(cid==Identifier.Key.LCONTROL) setRawValue(Source.KEY_LCONTROL,1);
    				else if(cid==Identifier.Key.BACK ) setRawValue(Source.KEY_BACKSPACE,1);
    				else if(cid==Identifier.Key.Q ) setRawValue(Source.KEY_Q,1);
    				else if(cid==Identifier.Key.E ) setRawValue(Source.KEY_E,1);
    				else if(cid==Identifier.Key.W ) setRawValue(Source.KEY_W,1);
    				else if(cid==Identifier.Key.A ) setRawValue(Source.KEY_A,1);
    				else if(cid==Identifier.Key.S ) setRawValue(Source.KEY_S,1);
    				else if(cid==Identifier.Key.D ) setRawValue(Source.KEY_D,1);
    				else if(cid==Identifier.Key.NUMPADENTER) setRawValue(Source.KEY_ENTER,1);
    				else if(cid==Identifier.Key.TAB) setRawValue(Source.KEY_TAB,1);

    				else if(cid==Identifier.Key._0) setRawValue(Source.KEY_0,1);
    				else if(cid==Identifier.Key._1) setRawValue(Source.KEY_1,1);
    				else if(cid==Identifier.Key._2) setRawValue(Source.KEY_2,1);
    				else if(cid==Identifier.Key._3) setRawValue(Source.KEY_3,1);
    				else if(cid==Identifier.Key._4) setRawValue(Source.KEY_4,1);
    				else if(cid==Identifier.Key._5) setRawValue(Source.KEY_5,1);
    				else if(cid==Identifier.Key._6) setRawValue(Source.KEY_6,1);
    				else if(cid==Identifier.Key._7) setRawValue(Source.KEY_7,1);
    				else if(cid==Identifier.Key._8) setRawValue(Source.KEY_8,1);
    				else if(cid==Identifier.Key._9) setRawValue(Source.KEY_9,1);

    				else if(cid==Identifier.Key.ESCAPE) setRawValue(Source.KEY_ESCAPE,1);
    				else if(cid==Identifier.Key.SLASH) setRawValue(Source.KEY_FRONTSLASH,1);
    				else if(cid==Identifier.Key.BACKSLASH) setRawValue(Source.KEY_BACKSLASH,1);
    				else if(cid==Identifier.Key.EQUALS) setRawValue(Source.KEY_PLUS,1);
    				else if(cid==Identifier.Key.ADD) setRawValue(Source.KEY_PLUS,1);
    				else if(cid==Identifier.Key.COMMA) setRawValue(Source.KEY_LESSTHAN,1);
    				else if(cid==Identifier.Key.PERIOD) setRawValue(Source.KEY_GREATERTHAN,1);
    				
    				else if(cid==Identifier.Key.SPACE) setRawValue(Source.KEY_SPACE,1);
    				else if(cid==Identifier.Key.GRAVE) setRawValue(Source.KEY_TILDE,1);

    				else if(cid==Identifier.Key.F1) setRawValue(Source.KEY_F1,1);
    				else if(cid==Identifier.Key.F2) setRawValue(Source.KEY_F2,1);
    				else if(cid==Identifier.Key.F3) setRawValue(Source.KEY_F3,1);
    				else if(cid==Identifier.Key.F4) setRawValue(Source.KEY_F4,1);
    				else if(cid==Identifier.Key.F5) setRawValue(Source.KEY_F5,1);
    				else if(cid==Identifier.Key.F6) setRawValue(Source.KEY_F6,1);
    				else if(cid==Identifier.Key.F7) setRawValue(Source.KEY_F7,1);
    				else if(cid==Identifier.Key.F8) setRawValue(Source.KEY_F8,1);
    				else if(cid==Identifier.Key.F9) setRawValue(Source.KEY_F9,1);
    				else if(cid==Identifier.Key.F10) setRawValue(Source.KEY_F10,1);
    				else if(cid==Identifier.Key.F11) setRawValue(Source.KEY_F11,1);
    				else if(cid==Identifier.Key.F12) setRawValue(Source.KEY_F12,1);
    			}
        	}
        }
	}
	
	public static void focusLost() {
		hasFocus=false;
		resetKeyStates();
	}
	
	public static void focusGained() {
		hasFocus=true;
		resetKeyStates();
	}
}
