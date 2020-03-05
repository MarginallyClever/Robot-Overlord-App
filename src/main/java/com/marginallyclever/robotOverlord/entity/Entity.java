package com.marginallyclever.robotOverlord.entity;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.uiElements.CollapsiblePanel;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;


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
		
		// what to do about children?  Not a deep copy?
		
		parent = b.parent;
	}
	
	/**
	 * Get the {@link EntityPanel} for this class' superclass, then the EntityPanel for this class, and so on.
	 * 
	 * @param gui the main application instance.
	 * @return the list of EntityPanels 
	 */
	public ArrayList<JPanel> getContextPanel(RobotOverlord gui) {
		ArrayList<JPanel> list = new ArrayList<JPanel>();
		
		entityPanel = new EntityPanel(gui,this);
		list.add(entityPanel);

		return list;
	}
	
	
	/**
	 * Get all the {@link EntityPanel}s for this {@link Entity}.  
	 * <p>
	 * If this class is derived from Entity, get the panels for the derived Entities, too.  Normally this is called by {@link RobotOverlord}.
	 * 
	 * @param gui the main application instance.
	 * @return an ArrayList of all the panels for this Entity and all derived classes.
	 */
	@SuppressWarnings("unused")  // because of the layout settings below
	public JComponent getAllContextPanels(RobotOverlord gui) {
		ArrayList<JPanel> list = getContextPanel(gui);
		Iterator<JPanel> pi = list.iterator();
		
		if(false) {
			// single page layout
			JPanel sum = new JPanel();
			BoxLayout layout = new BoxLayout(sum, BoxLayout.PAGE_AXIS);
			sum.setLayout(layout);
			while(pi.hasNext()) {
				JPanel p = pi.next();
				
				CollapsiblePanel oiwPanel = new CollapsiblePanel(p.getName());
				oiwPanel.getContentPane().add(p);
				sum.add(oiwPanel);
			}

			JPanel b = new JPanel(new BorderLayout());
			b.add(sum, BorderLayout.PAGE_START);
			return b;
		} else {
			boolean reverseOrderOfTabs = false;
			// tabbed layout
			JTabbedPane b = new JTabbedPane();
			while(pi.hasNext()) {
				JPanel p = pi.next();
				
				if( reverseOrderOfTabs ) {
					b.insertTab(p.getName(), null, p, null, 0);
				} else {
					b.addTab(p.getName(), p);
				}
			}
			b.setSelectedIndex( reverseOrderOfTabs ? 0 : b.getTabCount()-1 );
			
			return b;
		}
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
	
	
	public void addChild(Entity e) {
		children.add(e);
		e.setParent(this);
	}
	
	public void removeChild(Entity e) {
		int n=0;
		Iterator<Entity> i = children.iterator();
		while(i.hasNext()) {
			if(i==e) {
				children.remove(n);
				return;
			}
			++n;
		}
	}
	
	public ArrayList<Entity> getChildren() {
		return children;
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
