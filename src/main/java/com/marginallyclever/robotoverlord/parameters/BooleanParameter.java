package com.marginallyclever.robotoverlord.parameters;

import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;
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

	@Override
	public String toString() {
		return getName()+"="+t.toString();
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
