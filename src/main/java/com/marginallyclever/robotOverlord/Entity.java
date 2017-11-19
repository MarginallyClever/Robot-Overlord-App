package com.marginallyclever.robotOverlord;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import com.jogamp.opengl.GL2;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.vecmath.Vector3f;


/**
 * An object in the world that can have a user interface
 * @author danroyer
 *
 */
public class Entity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2461060493057258044L;
	
	private Vector3f position;
	private Vector3f xAxis,yAxis,zAxis;
	
	private String displayName;
	private int pickName;
	
	// unique ids for all objects in the world.  zero is reserved to indicate no object.
	static private int pickNameCounter=1;
	
	private transient EntityPanel entityPanel;
	protected Material material;
	
	
	public Entity() {
		pickName = pickNameCounter++;
		position = new Vector3f();
		material = new Material();
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
	public JPanel getAllContextPanels(RobotOverlord gui) {
		JPanel sum = new JPanel();
		sum.setLayout(new BoxLayout(sum, BoxLayout.PAGE_AXIS));
		
		ArrayList<JPanel> list = getContextPanel(gui);
		Iterator<JPanel> pi = list.iterator();
		while(pi.hasNext()) {
			JPanel p = pi.next();
			sum.add(p);
		}
		
		return sum;
	}
	
	
	public String getDisplayName() {
		return displayName;
	}


	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}


	public int getPickName() {
		return pickName;
	}
	
	public boolean hasPickName(int name) {
		return pickName==name;
	}
	public void unPick() {}
	
	public void render(GL2 gl2) {}
	

	public Vector3f getPosition() {		return position;	}
	public Vector3f getXAxis() {		return xAxis;	}
	public Vector3f getYAxis() {		return yAxis;	}
	public Vector3f getZAxis() {		return zAxis;	}
	public void setPosition(Vector3f pos) {		position.set(pos);  if(entityPanel!=null) entityPanel.updateFields();	}
	public void setXAxis(Vector3f pos) {		xAxis.set(pos);  if(entityPanel!=null) entityPanel.updateFields();	}
	public void setYAxis(Vector3f pos) {		yAxis.set(pos);  if(entityPanel!=null) entityPanel.updateFields();	}
	public void setZAxis(Vector3f pos) {		zAxis.set(pos);  if(entityPanel!=null) entityPanel.updateFields();	}
}
