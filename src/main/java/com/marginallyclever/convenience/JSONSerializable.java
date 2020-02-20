package com.marginallyclever.convenience;

import java.io.IOException;

import org.json.simple.JSONObject;

public interface JSONSerializable {
	
	public JSONObject toJSON();
	
	/**
	 * Derived classes that implement this method should remember
	 * to call super.fromJSON() 
	 */
	public void fromJSON(JSONObject arg0) throws IOException;
}
