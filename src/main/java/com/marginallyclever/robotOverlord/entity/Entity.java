package com.marginallyclever.robotOverlord.entity;

import java.util.ArrayList;
import java.util.Observable;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.RobotOverlord;
import javax.swing.JPanel;


/**
 * An object in the world that can have a user interface.  Does not require physical presence.
 * @author danroyer
 *
 */
public class Entity extends Observable {
	private String name;
	private int pickName;

	protected ArrayList<Entity> children;
	protected Entity parent;
	
	
	// unique ids for all objects in the world.  
	// zero is reserved to indicate no object.
	// first 9 are reserved for MotionHelper.
	static private int pickNameCounter=10;
	
	private transient EntityPanel entityPanel;
	
	
	public Entity() {
		children = new ArrayList<Entity>();
		pickName = pickNameCounter++;
	}
	
	public void set(Entity b) {
		name = b.name;
		
		// what to do about children?  Not a deep copy for now
		
		parent = b.parent;
	}
	
	/**
	 * Get the {@link EntityPanel} for this class' superclass, then the EntityPanel for this class, and so on.
	 * 
	 * @param gui the main application instance.
	 * @return the list of EntityPanels 
	 */
	public ArrayList<JPanel> getContextPanels(RobotOverlord gui) {
		ArrayList<JPanel> list = new ArrayList<JPanel>();
		
		entityPanel = new EntityPanel(gui,this);
		list.add(entityPanel);

		return list;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public int getPickName() {
		return pickName;
	}
	
	public boolean hasPickName(int name) {
		return pickName==name;
	}
	
	public void pick() {}
	
	public void unPick() {}
	
	
	public void update(double dt) {
		for(Entity e : children ) {
			e.update(dt);
		}
	}
	
	public void render(GL2 gl2) {
		for(Entity e : children ) {
			e.render(gl2);
		}
	}
	

	public boolean hasChild(Entity o) {
		return children.contains(o);
	}
	
	public void addChild(Entity e) {
		children.add(e);
		e.setParent(this);
	}
	
	public void removeChild(Entity e) {
		if(children.contains(e)) {
			children.remove(e);
			e.setParent(null);
		}
	}
	
	public ArrayList<Entity> getChildren() {
		return children;
	}
	
	public Entity findChildWithName(String name) {
		for( Entity obj : children ) {
			String objectName = obj.getName();
			if(name.equals(objectName)) return obj; 
		}
		
		return null;
	}
	
	public void removeParent() {
		parent=null;
	}

	public Entity getParent() {
		return parent;
	}
	
	public void setParent(Entity e) {
		parent = e;
	}
	
	/**
	 * Do you have a status to report to the GUI?
	 * Override this with your class' status.
	 * @return a String.  Cannot be null.  default is "".  
	 */
	public String getStatusMessage() {
		return "";
	}
	
	public String toString() {
		return name;
	}
}
