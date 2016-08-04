package com.marginallyclever.robotOverlord;

import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;

import com.jogamp.opengl.GL2;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.vecmath.Vector3f;


/**
 * an object in the world that can have a gui interface
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
	
	private transient EntityPanel oiwPanel;
	protected Material material = new Material();
	
	
	
	//protected transient EvilOverlord gui;
	
	
	public Entity() {
		pickName = pickNameCounter++;
		position = new Vector3f();
	}
	
	
	public ArrayList<JPanel> getControlPanels(RobotOverlord gui) {
		ArrayList<JPanel> list = new ArrayList<JPanel>();
		
		oiwPanel = new EntityPanel(gui,this);
		list.add(oiwPanel);

		return list;
	}
	
	
	public JPanel buildPanel(RobotOverlord gui) {
		JPanel sum = new JPanel();
		sum.setLayout(new BoxLayout(sum, BoxLayout.PAGE_AXIS));
		
		ArrayList<JPanel> list = getControlPanels(gui);
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
	
	
	public void render(GL2 gl2) {}
	public void save(Writer writer) {}
	public void load(Reader reader) {}
	

	public Vector3f getPosition() {		return position;	}
	public Vector3f getXAxis() {		return xAxis;	}
	public Vector3f getYAxis() {		return yAxis;	}
	public Vector3f getZAxis() {		return zAxis;	}
	public void setPosition(Vector3f pos) {		position.set(pos);  if(oiwPanel!=null) oiwPanel.updateFields();	}
	public void setXAxis(Vector3f pos) {		xAxis.set(pos);  if(oiwPanel!=null) oiwPanel.updateFields();	}
	public void setYAxis(Vector3f pos) {		yAxis.set(pos);  if(oiwPanel!=null) oiwPanel.updateFields();	}
	public void setZAxis(Vector3f pos) {		zAxis.set(pos);  if(oiwPanel!=null) oiwPanel.updateFields();	}
}
