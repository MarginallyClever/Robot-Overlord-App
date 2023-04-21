package com.marginallyclever.robotoverlord.parameters;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class StringEntity extends AbstractEntity<String> {
	public StringEntity() {
		super("","");
	}
	
	// ambiguous solution is to do both!
	public StringEntity(String t) {
		super();
		setName(t);
    	this.t = t;
	}
	
	public StringEntity(String name, String value) {
		super();
		setName(name);
		t=value;
	}

	@Override
	public String toString() {
		return getName()+"="+t;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject jo = super.toJSON();
		jo.put("value",get());
		return jo;
	}

	@Override
	public void parseJSON(JSONObject jo) throws JSONException {
		super.parseJSON(jo);
		set(jo.getString("value"));
	}
}
