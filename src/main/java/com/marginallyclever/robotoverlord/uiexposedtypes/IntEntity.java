package com.marginallyclever.robotoverlord.uiexposedtypes;

import com.marginallyclever.robotoverlord.AbstractEntity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serial;

/**
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class IntEntity extends AbstractEntity<Integer> {
	@Serial
	private static final long serialVersionUID = -665400072120969645L;

	public IntEntity() {
		super();
	}
	
	public IntEntity(String name,int d) {
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
