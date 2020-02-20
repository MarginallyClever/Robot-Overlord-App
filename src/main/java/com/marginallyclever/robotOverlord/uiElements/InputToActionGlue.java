package com.marginallyclever.robotOverlord.uiElements;

import java.io.IOException;
import org.json.simple.JSONObject;

import com.marginallyclever.convenience.JSONSerializable;

public class InputToActionGlue implements JSONSerializable {
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

	@Override
	public JSONObject toJSON() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fromJSON(JSONObject arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}
}
