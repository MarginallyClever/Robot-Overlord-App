package com.marginallyclever.robotoverlord.uiexposedtypes;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotoverlord.AbstractEntity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class DoubleEntity extends AbstractEntity<Double> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3278660733983538798L;

	public DoubleEntity(String s) {
		super(0.0);
		setName(s);
	}
	
	public DoubleEntity(String s,double d) {
		super(d);
		setName(s);
	}

	public DoubleEntity(String s,float d) {
		super((double)d);
		setName(s);
	}
	
	public DoubleEntity(String s,int d) {
		super((double)d);
		setName(s);
	}
	
	@Override
	public String toString() {
		return getName()+"="+StringHelper.formatDouble(t);
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
		set(jo.getDouble("value"));
	}
}
