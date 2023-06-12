package com.marginallyclever.robotoverlord.parameters;

import com.marginallyclever.robotoverlord.SerializationContext;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class BooleanParameter extends AbstractParameter<Boolean> {
	public BooleanParameter(String name, boolean value) {
		super(name,value);
	}

	public BooleanParameter(String name) {
		this(name,false);
	}

	public BooleanParameter() {
		this("boolean");
	}

	@Override
	public String toString() {
		return getName()+"="+ get().toString();
	}
	
	public void toggle() {
		set(!get());
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
		set(jo.getBoolean("value"));
	}
}
