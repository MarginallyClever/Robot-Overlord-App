package com.marginallyclever.robotoverlord.parameters;

import com.marginallyclever.robotoverlord.AbstractEntity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serial;

/**
 * A parameter that holds a reference to another entity.  It contains the uniqueID of the entity.
 * It should display the name of the entity, and allow the user to select a new entity.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class ReferenceEntity extends AbstractEntity<Integer> {

	public ReferenceEntity() {
		super();
	}

	public ReferenceEntity(String name, int d) {
		super(d);
		setName(name);
	}
	
	@Override
	public String toString() {
		return getName()+"="+t.toString();
	}
	
	@Override
	public void getView(ViewPanel view) {
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
