package com.marginallyclever.robotoverlord.parameters;

import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.systems.robot.robotarm.EditArmPanel;
import org.json.JSONObject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * A convenience class for basic data types
 * @author Dan Royer
 * @since 1.6.0
 */
public abstract class AbstractParameter<T> {
	private String name;
	// the data to store
	protected T t;
	private final List<PropertyChangeListener> propertyChangeListeners = new ArrayList<>();

	public AbstractParameter(String name, T t) {
		this.name = name;
    	this.t = t;
	}

	public String getName() {
		return name;
	}
	
    public T get() {
    	return t;
    }
    
    public void set(T t) {
    	if( this.t==null || !this.t.equals(t) ) {
    		T oldValue = this.t;
	    	this.t = t;
    		firePropertyChange(new PropertyChangeEvent(this,"value",oldValue,t));
    	}
    }

	protected void firePropertyChange(PropertyChangeEvent value) {
		propertyChangeListeners.forEach(l->l.propertyChange(value));
	}

	public void set(AbstractParameter<T> b) {
		name = b.getName();
		set(b.get());
	}

	public void addPropertyChangeListener(PropertyChangeListener arg0) {
		propertyChangeListeners.add(arg0);
	}

	public void removePropertyChangeListener(EditArmPanel arg0) {
		propertyChangeListeners.remove(arg0);
	}

	public JSONObject toJSON(SerializationContext context) {
		JSONObject jo = new JSONObject();
		jo.put("name",getName());
		return jo;
	}

	public void parseJSON(JSONObject jo,SerializationContext context) {
		name = jo.getString("name");
	}
}
