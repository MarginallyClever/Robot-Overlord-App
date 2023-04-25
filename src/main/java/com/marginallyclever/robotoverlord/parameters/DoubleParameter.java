package com.marginallyclever.robotoverlord.parameters;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;
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

	@Override
	public String toString() {
		return getName()+"="+StringHelper.formatDouble(t);
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
		set(jo.getDouble("value"));
	}
}
