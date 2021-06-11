package com.marginallyclever.robotOverlord;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * Entities are nodes in a tree of data that can find each other and observe/be
 * observed
 * 
 * @author Dan Royer
 *
 */
public class Entity implements PropertyChangeListener, Cloneable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -470516237871859765L;

	private String name;

	// my children
	protected transient ArrayList<Entity> children = new ArrayList<Entity>();
	
	// my parent
	protected transient Entity parent;

	// who is listening to me?
	protected ArrayList<PropertyChangeListener> propertyChangeListeners = new ArrayList<PropertyChangeListener>();
	
	
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

	public String getUniqueChildName(Entity e) {
		String rootName = e.getName(); 
		// strip digits from end of name.
		rootName = rootName.replaceAll("\\d*$", "");
		String name = rootName;
		
		int count=1;
		boolean foundMatch;
		
		do {
			foundMatch=false;
			for( Entity c : children ) {
				if( c.getName().equals(name) ) {
					// matches an existing name.  increment by one and check everybody again.
					name = rootName+Integer.toString(count++);
					foundMatch=true;
				}
			}
		} while(foundMatch);
		// unique name found.
		return name;
	}
	
	public void addChild(int index,Entity e) {
		// check if any child has a matching name
		e.setName(getUniqueChildName(e));
		children.add(index,e);
		e.setParent(this);
	}
	
	public void addChild(Entity e) {
		addChild(children.size(),e);
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
	public void getView(ViewPanel view) {}

	protected void getViewOfChildren(ViewPanel view) {
		for (Entity child : children) {
			if (child.getChildren().isEmpty()) {
				// only leaves
				child.getView(view);
			}
		}
	}

	/**
	 * Override this to let the user rename entities of this type
	 * 
	 * @return
	 */
	public boolean canBeRenamed() {
		return false;
	}
	
	@Override
	protected Object clone() {
		Entity e;
		try {
			e = (Entity)super.clone();
		} catch (CloneNotSupportedException e1) {
			throw new InternalError();
		}
		for(int i=0;i<children.size();++i) {
			e.children.set(i, (Entity)children.get(i).clone());
		}
		
		return e;
	}
	
	// Serialization
	private void writeObject(ObjectOutputStream stream) throws IOException {
		stream.writeObject(name);
		stream.writeInt(children.size());
		for( Entity c : children ) {
			stream.writeObject(c);
		}
	}

	// Serialization
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		name = (String)stream.readObject();

		// load and associate children
		children = new ArrayList<Entity>();
		int count = stream.readInt();
		for(int i=0;i<count;++i) {
			Entity child = (Entity)stream.readObject();
			addChild(child);
		}
	}

	/**
	 * Something this Entity is observing has changed. Deal with it!
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {}
	
	public void addPropertyChangeListener(PropertyChangeListener p) {
		propertyChangeListeners.add(p);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener p) {
		propertyChangeListeners.remove(p);
	}
	
	public void notifyPropertyChangeListeners(PropertyChangeEvent evt) {
		for( PropertyChangeListener p : propertyChangeListeners ) {
			p.propertyChange(evt);
		}
	}

	public void removeAllChildren() {
		while(!children.isEmpty()) removeChild(children.get(0));
	}
}
