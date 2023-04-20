package com.marginallyclever.robotoverlord.parameters;

import com.marginallyclever.robotoverlord.AbstractEntity;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A {@link StringEntity} that can only be set to the uniqueID of an {@link com.marginallyclever.robotoverlord.Entity}.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class ReferenceEntity extends AbstractEntity<String> {
	public ReferenceEntity() {
		super("","");
	}

	// ambiguous solution is to do both!
	public ReferenceEntity(String t) {
		super();
		setName(t);
    	this.t = t;
	}

	public ReferenceEntity(String name, String value) {
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
