package com.marginallyclever.robotOverlord.swingInterface;


public class InputToActionGlue  {
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
