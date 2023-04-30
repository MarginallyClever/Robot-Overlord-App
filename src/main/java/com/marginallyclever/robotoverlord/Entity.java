package com.marginallyclever.robotoverlord;

import com.jogamp.opengl.GL2;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

/**
 * Entities are nodes in a tree of data that can find each other and observe/be
 * observed
 * 
 * @author Dan Royer
 *
 */
public class Entity implements PropertyChangeListener {
	public static final String PATH_SEPARATOR = "/";
	public static final String PATH_PREVIOUS = "..";
	public static final String PATH_CURRENT = ".";

	private String name;

	protected transient Entity parent;

	protected transient ArrayList<Entity> children = new ArrayList<>();
	private final List<Component> components = new ArrayList<>();

	// who is listening to me?
	protected ArrayList<PropertyChangeListener> propertyChangeListeners = new ArrayList<>();

	private boolean isExpanded =false;

	/**
	 * The unique ID of this Entity.
	 */
	private String uniqueID = UUID.randomUUID().toString();

	public Entity() {
		super();
		this.name = this.getClass().getSimpleName();
	}

	public Entity(String name) {
		super();
		this.name = name;
	}

	public String getUniqueID() {
		return uniqueID;
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
		for(Component c : components) {
			if(c.getEnabled()) c.update(dt);
		}

		for(Entity e : children) {
			e.update(dt);
		}
	}

	/**
	 * Render this Entity to the display
	 * @param gl2 the render context
	 */
	public void render(GL2 gl2) {}

	public String getUniqueChildName(Entity e) {
		String rootName = e.getName(); 
		// strip digits from end of name.
		rootName = rootName.replaceAll("\\d*$", "");
		String name = rootName;
		
		int count=1;
		boolean foundMatch;
		
		do {
			foundMatch=false;
			for( Entity c : children) {
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

	@Deprecated
	public void addEntity(int index, Entity e) {
		// check if any child has a matching name
		e.setName(getUniqueChildName(e));
		children.add(index,e);
		e.setParent(this);
	}

	/**
	 * Use {@link EntityManager#addEntityToParent(Entity, Entity)} instead.
	 * @param child the child to add
	 */
	@Deprecated
	public void addEntity(Entity child) {
		//System.out.println("add "+child.getFullPath()+" to "+this.getFullPath());
		children.add(child);
		child.setParent(this);
	}

	/**
	 * Use {@link EntityManager#removeEntityFromParent(Entity, Entity)} instead.
	 * @param child the child to remove
	 */
	@Deprecated
	public void removeEntity(Entity child) {
		if (children.contains(child)) {
			children.remove(child);
			if(child.getParent()==this) // is this always true?  then why test it?
				child.setParent(null);
		}
	}

	public List<Entity> getChildren() {
		return children;
	}

	public void removeParent() {
		parent = null;
	}

	public Entity getParent() {
		return parent;
	}

	public void setParent(Entity e) {
		if(parent != null) parent.removeEntity(this);
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
		String[] pathComponents = path.split(PATH_SEPARATOR);

		// if absolute path, start with root node.
		int i = 0;
		Entity e;
		if (path.startsWith(PATH_SEPARATOR)) {
			e = getRoot();
			i = 2;
		} else {
			e = this;
		}

		while (i < pathComponents.length) {
			String name = pathComponents[i++];

			if (e == null)
				break;
			if (name.contentEquals(PATH_PREVIOUS)) {
				// ".." = my parent
				e = e.getParent();
				continue;
			} else if (name.contentEquals(PATH_CURRENT)) {
				// "." is me!
				continue;
			}

			e = e.getChildren().stream().filter( c -> name.contentEquals(c.getName()) ).findFirst().orElse(null);
		}

		return e;
	}

	/**
	 * @return This entity's full pathname in the entity tree.
	 */
	public String getFullPath() {
		StringBuilder sum = new StringBuilder();
		Entity e = this;

		do {
			sum.insert(0, "/" + e.getName());
			e = e.getParent();
		} while (e != null);

		return sum.toString();
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		Entity e = (Entity)super.clone();
		
		e.children = new ArrayList<Entity>();
		for(int i = 0; i< children.size(); ++i) {
			e.children.add((Entity) children.get(i).clone());
		}
		
		e.propertyChangeListeners = new ArrayList<PropertyChangeListener>();
		
		return e;
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
	
	protected void notifyPropertyChangeListeners(PropertyChangeEvent evt) {
		for( PropertyChangeListener p : propertyChangeListeners ) {
			p.propertyChange(evt);
		}
	}

	@Override
	public String toString() {
		return "name=" + name + ", " +
				"uniqueID=" + uniqueID + ", " +
				"entities=" + Arrays.toString(children.toArray()) + ", " +
				"components=" + Arrays.toString(components.toArray()) +
				"expanded=" + isExpanded;
	}

	public int getComponentCount() {
		return components.size();
	}

	public void addComponent(Component c) {
		if(containsAnInstanceOfTheSameClass(c)) return;
		components.add(c);
		c.setEntity(this);
	}

	public boolean containsAnInstanceOfTheSameClass(Component c0) {
		Class<?> clazz = c0.getClass();
		for(Component c : components) {
			if(clazz == c.getClass()) return true;
		}
		return false;
	}

	public Component getComponent(int i) {
		return components.get(i);
	}

	public Component findComponentByName(String name) {
		for(Component c : components) {
			if(name.equals(c.getClass().getSimpleName())) {
				return c;
			}
		}
		return null;
	}

	public void removeComponent(Component c) {
		components.remove(c);
	}

	/**
	 * @return the first instance of class T found in component list.
	 * @param <T> the type to find and return.  Must be derived from Component.
	 */
	public <T> T findFirstComponent(Class<T> clazz) {
		for(Component c : components) {
			if(clazz.isInstance(c)) {
				return (T)c;
			}
		}
		return null;
	}

	/**
	 * @return all instances of class T found attached to this Entity.
	 * @param <T> the type to find and return.  Must be derived from Component.
	 */
	public <T> List<T> findAllComponents(Class<T> clazz) {
		List<T> list = new ArrayList<>();
		for(Component c : components) {
			if(clazz.isInstance(c)) {
				list.add((T)c);
			}
		}
		return list;
	}

	/**
	 * Search this Entity and all child Entities until a {@link Component} match is found.
	 */
	public <T> T findFirstComponentRecursive(Class<T> clazz) {
		T found = findFirstComponent(clazz);
		if(found!=null) return found;

		for(Entity e : children) {
			found = e.findFirstComponentRecursive(clazz);
			if(found!=null) return found;
		}

		return null;
	}

	/**
	 * Search the parent of this entity for a component and then that parent and so on.
	 * @param clazz the class of the Component to find
	 * @return the instance found or null
	 * @param <T> the class of the Component to find
	 */
	public <T> T findFirstComponentInParents(Class<T> clazz) {
		Entity p = parent;
		while(p!=null) {
			T found = p.findFirstComponent(clazz);
			if (found != null) return found;
			p = p.getParent();
		}
		return null;
	}

    public JSONObject toJSON() {
		JSONObject jo = new JSONObject();
		jo.put("type",this.getClass().getName());
		jo.put("uniqueID",this.uniqueID);
		jo.put("name",this.name);
		jo.put("expanded",this.isExpanded);
		if(!children.isEmpty()) jo.put("entities", getEntitiesAsJSON());
		if(!components.isEmpty()) jo.put("components",getComponentsAsJSON());
		return jo;
	}

	private JSONArray getEntitiesAsJSON() {
		JSONArray jo = new JSONArray();
		for(Entity c : children) {
			jo.put(c.toJSON());
		}
		return jo;
	}

	private JSONArray getComponentsAsJSON() {
		JSONArray jo = new JSONArray();
		for(Component c : components) {
			jo.put(c.toJSON());
		}
		return jo;
	}

	public void parseJSON(JSONObject jo) throws JSONException {
		this.name = jo.getString("name");
		if(jo.has("uniqueID")) this.uniqueID = jo.getString("uniqueID");
		if(jo.has("entities")) readEntities(jo.getJSONArray("entities"));
		if(jo.has("components")) readComponents(jo.getJSONArray("components"));
		if(jo.has("expanded")) this.isExpanded = jo.getBoolean("expanded");
	}

	private void readEntities(JSONArray jo) throws JSONException {
		for (Object o : jo) {
			JSONObject jo2 = (JSONObject) o;
			Entity entity = new Entity();
			this.addEntity(entity);
			entity.parseJSON(jo2);
		}
	}

	private void readComponents(JSONArray jo) throws JSONException {
		for (Object o : jo) {
			JSONObject jo2 = (JSONObject) o;
			Component component = ComponentFactory.load(jo2.getString("type"));
			// It's possible that a component creates another component upon which it is dependent.
			// Only one of each component class is allowed in an Entity.
			// So we check for that condition and only use the existing component.
			if(!containsAnInstanceOfTheSameClass(component)) {
				this.addComponent(component);
			} else {
				component = findFirstComponent(component.getClass());
			}
			component.parseJSON(jo2);
		}
	}

	public Entity deepCopy() {
		Entity e = new Entity();
		e.parseJSON(this.toJSON());
		return e;
	}

	public boolean getExpanded() {
		return isExpanded;
	}

	public void setExpanded(boolean arg0) {
		isExpanded = arg0;
	}
}
