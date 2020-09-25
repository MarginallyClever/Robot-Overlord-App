package com.marginallyclever.robotOverlord.entity;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * Entities are nodes in a tree of data that can find each other and observe/be
 * observed
 * 
 * @author Dan Royer
 *
 */
public class Entity extends Observable implements Observer {
	private String name;

	// my children
	@JsonBackReference
	protected ArrayList<Entity> children = new ArrayList<Entity>();
	// my parent
	@JsonManagedReference
	protected Entity parent;

	public Entity() {
		super();
	}

	public Entity(String name) {
		super();
		this.name = name;
	}

	public void set(Entity b) {
		setName(b.getName());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		// if(hasChanged()) return;
		// setChanged();
		this.name = name;
		// notifyObservers(name);
	}

	/**
	 * @param dt seconds since last update.
	 */
	public void update(double dt) {
		for (Entity e : children) {
			e.update(dt);
		}
	}

	/**
	 * Render this Entity to the display
	 * @param gl2
	 */
	public void render(GL2 gl2) {
		for (Entity e : children) {
			e.render(gl2);
		}
	}
	
	public boolean hasChild(Entity o) {
		return children.contains(o);
	}

	public void addChild(Entity e) {
		// check if any child has a matching name
		String rootName = e.getName();
		String name = rootName;
		int count=1;
		boolean foundMatch;
		
		do {
			foundMatch=false;
			for( Entity c : children ) {
				if( c.getName().equals(name) ) {
					// oops?
					name = rootName+Integer.toString(count++);
					foundMatch=true;
				}
			}
		} while(foundMatch);
		e.setName(name);
		children.add(e);
		e.setParent(this);
	}

	public void removeChild(Entity e) {
		if (children.contains(e)) {
			children.remove(e);
			e.setParent(null);
		}
	}

	public ArrayList<Entity> getChildren() {
		return children;
	}

	public void removeParent() {
		parent = null;
	}

	public Entity getParent() {
		return parent;
	}

	public void setParent(Entity e) {
		parent = e;
	}

	public String toString() {
		return name;
	}

	// Find the root node.
	public Entity getRoot() {
		Entity e = this;
		while (e.getParent() != null)
			e = e.getParent();
		return e;
	}

	/**
	 * Search the entity tree based on an absolute or relative Unix-style path.
	 * 
	 * @param path the search query
	 * @return the requested entity or null.
	 */
	public Entity findByPath(String path) {
		String[] pathComponents = path.split("/");

		// if absolute path, start with root node.
		int i = 0;
		Entity e;
		if (path.startsWith("/")) {
			e = getRoot();
			i = 2;
		} else {
			e = this;
		}

		while (i < pathComponents.length) {
			String name = pathComponents[i++];

			if (e == null)
				break;
			if (name.contentEquals("..")) {
				// ".." = my parent
				e = e.getParent();
			} else if (name.contentEquals(".")) {
				// "." is me!
				continue;
			} else {
				boolean found = false;
				for (Entity c : e.getChildren()) {
					if (name.contentEquals(c.getName())) {
						e = c;
						found = true;
						break;
					}
				}
				if (found == false)
					return null; // does not exist
			}
		}

		return e;
	}

	/**
	 * @return This entity's full pathname in the entity tree.
	 */
	public String getFullPath() {
		String sum = "";
		Entity e = this;

		do {
			sum = "/" + e.getName() + sum;
			e = e.getParent();
		} while (e != null);

		return sum;
	}

	/**
	 * Explains to View in abstract terms the control interface for this entity.
	 * Derivatives of View implement concrete versions of that view.
	 * 
	 * @param g
	 */
	public void getView(ViewPanel view) {
	}

	public void getViewOfChildren(ViewPanel view) {
		for (Entity child : children) {
			if (child.getChildren().isEmpty()) {
				// only leaves
				child.getView(view);
			}
		}
	}

	/**
	 * Something this Entity is observing has changed. Deal with it!
	 */
	@Override
	public void update(Observable o, Object arg) {
	}

	/**
	 * Override this to let the user rename entities of this type
	 * 
	 * @return
	 */
	public boolean canBeRenamed() {
		return false;
	}
}
