package com.marginallyclever.robotoverlord.entity;

import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.components.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * Entities are nodes in a tree of data that can find each other and observe/be
 * observed
 * 
 * @author Dan Royer
 *
 */
public class Entity {
	public static final String PATH_SEPARATOR = "/";
	public static final String PATH_PREVIOUS = "..";
	public static final String PATH_CURRENT = ".";

	private String name;

	protected transient Entity parent;

	protected transient ArrayList<Entity> children = new ArrayList<>();
	private final List<Component> components = new ArrayList<>();

	/**
	 * The unique ID of this Entity.
	 */
	private String uniqueID = UUID.randomUUID().toString();

	public Entity() {
		super();
		this.name = this.getClass().getSimpleName();
		addComponent(new PoseComponent());
	}

	public Entity(String name) {
		this();
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
		this.name = name;
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
	 * @return the next sibling or null if none.
	 */
	public Entity getNextSibling() {
		if(parent==null) return null;  // no parent, no siblings.
		int i=parent.children.indexOf(this);
		if(i==parent.children.size()-1) return null;  // no next sibling
		return parent.children.get(i+1);
	}

	/**
	 * @return the previous sibling or null if none.
	 */
	public Entity getPreviousSibling() {
		if(parent==null) return null;  // no parent, no siblings.
		int i=parent.children.indexOf(this);
		if(i==0) return null;  // no previous sibling
		return parent.children.get(i-1);
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
		
		e.children = new ArrayList<>();
		for(int i = 0; i< children.size(); ++i) {
			e.children.add((Entity) children.get(i).clone());
		}

		return e;
	}

	@Override
	public String toString() {
		return "name=" + name + ", " +
				"uniqueID=" + uniqueID + ", " +
				"entities=" + Arrays.toString(children.toArray()) + ", " +
				"components=" + Arrays.toString(components.toArray());
	}

	public int getComponentCount() {
		return components.size();
	}

	public void addComponent(Component c) {
		if(containsAnInstanceOfTheSameClass(c)) return;
		components.add(c);
		c.setEntity(this);
		addComponentDependencies(c.getClass());
		c.onAttach();
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

	public void removeComponent(Component c) {
		components.remove(c);
	}

	/**
	 * @return the first instance of class T found in component list.
	 * @param <T> the type to find and return.  Must be derived from Component.
	 */
	public <T extends Component> T getComponent(Class<T> clazz) {
		for(Component c : components) {
			if(clazz.isInstance(c)) {
				return clazz.cast(c);
			}
		}
		return null;
	}

	/**
	 * Search this Entity and all child Entities until a {@link Component} match is found.
	 */
	public <T extends Component> T findFirstComponentRecursive(Class<T> clazz) {
		List<Entity> toSearch = new LinkedList<>();
		toSearch.add(this);
		while(!toSearch.isEmpty()) {
			Entity e = toSearch.remove(0);
			T found = e.getComponent(clazz);
			if(found!=null) return found;
			toSearch.addAll(e.getChildren());
		}

		return null;
	}

	/**
	 * Search the parent of this entity for a component and then that parent and so on.
	 * @param clazz the class of the Component to find
	 * @return the instance found or null
	 * @param <T> the class of the Component to find
	 */
	public <T extends Component> T findFirstComponentInParents(Class<T> clazz) {
		Entity p = parent;
		while(p!=null) {
			T found = p.getComponent(clazz);
			if (found != null) return found;
			p = p.getParent();
		}
		return null;
	}

    public JSONObject toJSON(SerializationContext context) {
		JSONObject jo = new JSONObject();
		jo.put("type",this.getClass().getName());
		jo.put("uniqueID",this.uniqueID);
		jo.put("name",this.name);
		if(!children.isEmpty()) jo.put("entities", getEntitiesAsJSON(context));
		if(!components.isEmpty()) jo.put("components",getComponentsAsJSON(context));
		return jo;
	}

	private JSONArray getEntitiesAsJSON(SerializationContext context) {
		JSONArray jo = new JSONArray();
		for(Entity c : children) {
			jo.put(c.toJSON(context));
		}
		return jo;
	}

	private JSONArray getComponentsAsJSON(SerializationContext context) {
		JSONArray jo = new JSONArray();
		for(Component c : components) {
			jo.put(c.toJSON(context));
		}
		return jo;
	}

	public void parseJSON(JSONObject jo,SerializationContext context) throws JSONException {
		this.name = jo.getString("name");
		if(jo.has("uniqueID")) this.uniqueID = jo.getString("uniqueID");
		if(jo.has("entities")) readEntities(jo.getJSONArray("entities"),context);
		if(jo.has("components")) readComponents(jo.getJSONArray("components"),context);
	}

	private void readEntities(JSONArray jo,SerializationContext context) throws JSONException {
		for (Object o : jo) {
			JSONObject jo2 = (JSONObject) o;
			Entity entity = new Entity();
			this.addEntity(entity);
			entity.parseJSON(jo2,context);
		}
	}

	private void readComponents(JSONArray jo,SerializationContext context) throws JSONException {
		for (Object o : jo) {
			JSONObject jo2 = (JSONObject) o;
			Component component = ComponentFactory.load(jo2.getString("type"));
			// It's possible that a component creates another component upon which it is dependent.
			// Only one of each component class is allowed in an Entity.
			// So we check for that condition and only use the existing component.
			if(!containsAnInstanceOfTheSameClass(component)) {
				this.addComponent(component);
			} else {
				component = getComponent(component.getClass());
			}
			component.parseJSON(jo2,context);
		}
	}

	/**
	 * Create a new instance of this entity without smashing the uniqueID.
	 * @return the new entity tree.
	 */
	public Entity deepCopy() {
		SerializationContext context = new SerializationContext("");

		Entity e = new Entity();
		e.parseJSON(this.toJSON(context),context);
		e.recursivelyAssignNewUniqueIDs();
		return e;
	}

	/**
	 * This entity and all its children will be assigned new uniqueIDs.
	 */
	private void recursivelyAssignNewUniqueIDs() {
		// list self and all children
		List<Entity> list = getEntireTree();

		// update all uniqueIDs
		Map<String,String> oldToNew = new HashMap<>();
		for(Entity e : list) {
			String oldID = e.uniqueID;
			e.uniqueID = UUID.randomUUID().toString();
			oldToNew.put(oldID,e.uniqueID);
		}

		// update all references
		for(Entity e : list) {
			List<Component> components = e.getComponents();
			for(Component component : components) {
				if(component instanceof ComponentWithReferences) {
					((ComponentWithReferences)component).updateReferences(oldToNew);
				}
			}
		}
	}

	// list self and all children
	public List<Entity> getEntireTree() {
		List<Entity> list = new ArrayList<>();
		List<Entity> toAdd = new LinkedList<>();
		toAdd.add(this);
		while(!toAdd.isEmpty()) {
			Entity e = toAdd.remove(0);
			list.add(e);
			toAdd.addAll(e.getChildren());
		}
		return list;
	}

	private void addComponentDependencies(Class<?> myClass) {
		while(myClass!=null) {
			ComponentDependency[] annotations = myClass.getAnnotationsByType(ComponentDependency.class);
			for (ComponentDependency a : annotations) {
				Class<? extends Component> [] components = a.components();
				for(Class<? extends Component> c : components) {
					if(null==getComponent(c)) {
						addComponent(ComponentFactory.createInstance(c));
					}
				}
			}
			myClass = myClass.getSuperclass();
		}
	}

	/**
	 * Search the children of this entity for a child with the given name.
	 * @param name the name of the child to find
	 * @return the child or null if not found
	 */
	public Entity findChildNamed(String name) {
		for( Entity child : children) {
			if(child.getName().equals(name)) {
				return child;
			}
		}
		return null;
	}

	public List<Component> getComponents() {
		return new ArrayList<>(components);
	}
}
