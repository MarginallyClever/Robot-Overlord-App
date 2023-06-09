package com.marginallyclever.robotoverlord.parameters;

import com.marginallyclever.convenience.helpers.StringHelper;
import com.marginallyclever.robotoverlord.SerializationContext;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class DoubleParameter extends AbstractParameter<Double> {
	public DoubleParameter(String name, double value) {
		super(name, value);
	}

	public DoubleParameter(String name) {
		super(name, 0.0);
	}

	public DoubleParameter() {
		super("double", 0.0);
	}

	@Override
	public String toString() {
		return getName()+"="+StringHelper.formatDouble(get());
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
		set(jo.getDouble("value"));
	}
}
