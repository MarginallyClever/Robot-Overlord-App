package com.marginallyclever.robotOverlord.uiElements;

import java.io.Serializable;

public class InputToActionGlue implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1995421760741490303L;

	public String inputControllerName;
	
	public String inputComponentName;
	
	public String actionName;
	
	public boolean isAnalog;
	
	public InputToActionGlue() {
		isAnalog=false;
	}
	
	public InputToActionGlue(String action,String controller,String component,boolean isAnalog) {
		actionName = action;
		inputControllerName = controller;
		inputComponentName = component;
		this.isAnalog = isAnalog;
	}
}
