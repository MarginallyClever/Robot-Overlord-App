package com.marginallyclever.robotoverlord.parameters;

import com.marginallyclever.robotoverlord.SerializationContext;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class IntParameter extends AbstractParameter<Integer> {
	public IntParameter(String name, int value) {
		super(name,value);
	}

	public IntParameter(String name) {
		this(name,0);
	}

	public IntParameter() {
		this("int");
	}
	
	@Override
	public String toString() {
		return getName()+"="+ get().toString();
	}

	@Override
	public JSONObject toJSON(SerializationContext context) {
		JSONObject jo = super.toJSON(context);
		jo.put("value",get());
		return jo;
	}

	@Override
	public void parseJSON(JSONObject jo,SerializationContext context) throws JSONException {
		super.parseJSON(jo,context);
		set(jo.getInt("value"));
	}
}
