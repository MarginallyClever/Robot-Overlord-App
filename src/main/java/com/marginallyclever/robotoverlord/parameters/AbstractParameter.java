package com.marginallyclever.robotoverlord.parameters;

import com.marginallyclever.robotoverlord.Entity;

import java.beans.PropertyChangeEvent;

/**
 * A convenience class for basic data types
 * @author Dan Royer
 * @since 1.6.0
 */
public class AbstractParameter<T> extends Entity {
	// the data to store
	protected T t;

	protected AbstractParameter() {
		super();
	}
	
	public AbstractParameter(String name) {
		super(name);
	}
	
	public AbstractParameter(String name, T t) {
		super(name);
    	this.t = t;
	}
	
	public AbstractParameter(T t) {
		super();
    	this.t = t;
	}
	
    public T get() {
    	return t;
    }
    
    public void set(T t) {
    	if( this.t==null || !this.t.equals(t) ) {
    		T oldValue = this.t;
	    	this.t = t;
    		this.notifyPropertyChangeListeners(new PropertyChangeEvent(this,"value",oldValue,t));
    	}
    }
	
	public void set(AbstractParameter<T> b) {
		super.setName(b.getName());
		
		set(b.get());
		
		parent = b.parent;
	}
}
