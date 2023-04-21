package com.marginallyclever.robotoverlord.parameters;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A {@link StringParameter} that can only be set to the uniqueID of an {@link com.marginallyclever.robotoverlord.Entity}.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class ReferenceParameter extends AbstractParameter<String> {
	public ReferenceParameter() {
		super("","");
	}

	// ambiguous solution is to do both!
	public ReferenceParameter(String t) {
		super();
		setName(t);
    	this.t = t;
	}

	public ReferenceParameter(String name, String value) {
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
