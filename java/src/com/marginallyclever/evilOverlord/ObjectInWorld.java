package com.marginallyclever.evilOverlord;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.media.opengl.GL2;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.vecmath.Vector3f;


/**
 * an object in the world that can have a gui interface
 * @author danroyer
 *
 */
public class ObjectInWorld implements ActionListener, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2461060493057258044L;
	
	protected List<ObjectInWorld> children;
	protected Vector3f position;
	protected String displayName;
	protected int pickName;
	
	// unique ids for all objects in the world.  zero is reserved to indicate no object.
	static protected int pickNameCounter=1;
	
	private transient CollapsiblePanel oiwPanel;
	protected transient JTextField fieldX,fieldY,fieldZ;
	
	//protected transient EvilOverlord gui;
	
	
	public ObjectInWorld() {
		pickName = pickNameCounter++;
		children = new ArrayList<ObjectInWorld>();
		position = new Vector3f();
	}
	/*
	public void setGUI(EvilOverlord gui) {
		this.gui = gui;
		
		Iterator<ObjectInWorld> iter = children.iterator();
		while(iter.hasNext()) {
			iter.next().setGUI(gui);
		}
	}
	*/
	public ArrayList<JPanel> getControlPanels(EvilOverlord gui) {
		ArrayList<JPanel> list = new ArrayList<JPanel>();
		
		oiwPanel = new CollapsiblePanel("Position");
		JPanel contents = oiwPanel.getContentPane();
		
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.CENTER;
		
		JLabel x=new JLabel("X",JLabel.CENTER);
		fieldX = new JTextField(Float.toString(position.x));
		x.setLabelFor(fieldX);
		fieldX.addActionListener(this);
		con1.weightx=0.25;  con1.gridx=0; contents.add(x,con1);
		con1.weightx=0.75;  con1.gridx=1; contents.add(fieldX,con1);
		con1.gridy++;
		
		JLabel y=new JLabel("Y",JLabel.CENTER);
		fieldY = new JTextField(Float.toString(position.y));
		y.setLabelFor(fieldY);
		fieldY.addActionListener(this);
		con1.weightx=0.25;  con1.gridx=0; contents.add(y,con1);
		con1.weightx=0.75;  con1.gridx=1; contents.add(fieldY,con1);
		con1.gridy++;
		
		JLabel z=new JLabel("Z",JLabel.CENTER);
		fieldZ = new JTextField(Float.toString(position.z));
		z.setLabelFor(fieldZ);
		fieldZ.addActionListener(this);
		con1.weightx=0.25;  con1.gridx=0; contents.add(z,con1);
		con1.weightx=0.75;  con1.gridx=1; contents.add(fieldZ,con1);
		con1.gridy++;
		
		list.add(oiwPanel);

		return list;
	}
	
	public JPanel buildPanel(EvilOverlord gui) {
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


	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		
		if(source == fieldX) {
			try {
				float f = Float.parseFloat(fieldX.getText());
				this.position.x = f;
			} catch(NumberFormatException e) {}
		}
		
		if(source == fieldY) {
			try {
				float f = Float.parseFloat(fieldY.getText());
				this.position.y = f;
			} catch(NumberFormatException e) {}
		}
		
		if(source == fieldZ) {
			try {
				float f = Float.parseFloat(fieldZ.getText());
				this.position.z = f;
			} catch(NumberFormatException e) {}
		}
	}
	
	
	public void render(GL2 gl2) {}
	public void save(Writer writer) {}
	public void load(Reader reader) {}
	

	protected void setColor(GL2 gl2,float r,float g,float b,float a) {
		float [] diffuse = {r,g,b,a};
		gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, diffuse,0);
		float[] specular={0.85f,0.85f,0.85f,1.0f};
	    gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, specular,0);
	    float[] emission={0.01f,0.01f,0.01f,1f};
	    gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION, emission,0);
	    
	    gl2.glMaterialf(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, 50.0f);

	    gl2.glColor4f(r,g,b,a);
	}
}
