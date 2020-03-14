package com.marginallyclever.robotOverlord.entity;

/**
 * A convenience class for basic data types
 * @author Dan Royer
 * @since 1.6.0
 */
public abstract class AbstractEntity<T> extends Entity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4212633740030234657L;
	
	protected T t;

	public AbstractEntity() {
		super();
		setName("AbstractEntity");
	}
	
	public AbstractEntity(String name) {
		super(name);
	}
	
	public AbstractEntity(String name,T t) {
		super(name);
    	this.t = t;
	}
	
	public AbstractEntity(T t) {
		super();
    	this.t = t;
	}
	
    public T get() {
    	return t;
    }
    
    public void set(T t) {
    	if(hasChanged()) return;
    	setChanged();
    	this.t = t;
    	notifyObservers(t);
    }
	
	public void set(AbstractEntity<T> b) {
		super.set(b);
		
		set(b.get());
		
		parent = b.parent;
	}
}
