package com.marginallyclever.robotoverlord.parameters;

import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class IntParameter extends AbstractParameter<Integer> {

	public IntParameter() {
		super();
	}
	
	public IntParameter(String name, int d) {
		super(d);
		setName(name);
	}
	
	@Override
	public String toString() {
		return getName()+"="+t.toString();
	}
	
	@Override
	public void getView(ComponentPanelFactory view) {
		view.add(this);
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
		set(jo.getInt("value"));
	}
}
