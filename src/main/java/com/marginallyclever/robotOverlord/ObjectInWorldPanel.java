package com.marginallyclever.robotOverlord;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.vecmath.Vector3f;

public class ObjectInWorldPanel extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ObjectInWorld oiw;

	private transient JTextField fieldX,fieldY,fieldZ;
	
	public ObjectInWorldPanel(RobotOverlord gui,ObjectInWorld oiw) {
		super();
		
		this.oiw = oiw;


		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=1;
		c.weighty=1;
		c.anchor=GridBagConstraints.NORTHWEST;
		c.fill=GridBagConstraints.HORIZONTAL;

		CollapsiblePanel oiwPanel = new CollapsiblePanel("Move Origin");
		this.add(oiwPanel,c);
		JPanel contents = oiwPanel.getContentPane();
		
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.CENTER;
		
		JLabel x=new JLabel("X",JLabel.CENTER);
		fieldX = new JTextField(Float.toString(oiw.getPosition().x));
		x.setLabelFor(fieldX);
		fieldX.addActionListener(this);
		con1.weightx=0.25;  con1.gridx=0; contents.add(x,con1);
		con1.weightx=0.75;  con1.gridx=1; contents.add(fieldX,con1);
		con1.gridy++;
		
		JLabel y=new JLabel("Y",JLabel.CENTER);
		fieldY = new JTextField(Float.toString(oiw.getPosition().y));
		y.setLabelFor(fieldY);
		fieldY.addActionListener(this);
		con1.weightx=0.25;  con1.gridx=0; contents.add(y,con1);
		con1.weightx=0.75;  con1.gridx=1; contents.add(fieldY,con1);
		con1.gridy++;
		
		JLabel z=new JLabel("Z",JLabel.CENTER);
		fieldZ = new JTextField(Float.toString(oiw.getPosition().z));
		z.setLabelFor(fieldZ);
		fieldZ.addActionListener(this);
		con1.weightx=0.25;  con1.gridx=0; contents.add(z,con1);
		con1.weightx=0.75;  con1.gridx=1; contents.add(fieldZ,con1);
		con1.gridy++;
	
		// update the field values;
		updateFields();
	}
	

	public void updateFields() {
		fieldX.setText(Float.toString(oiw.getPosition().x));
		fieldY.setText(Float.toString(oiw.getPosition().y));
		fieldZ.setText(Float.toString(oiw.getPosition().z));
	}


	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		
		if(source == fieldX) {
			try {
				float f = Float.parseFloat(fieldX.getText());
				Vector3f pos = oiw.getPosition();
				pos.x = f;
				oiw.setPosition(pos);
			} catch(NumberFormatException e) {}
		}
		
		if(source == fieldY) {
			try {
				float f = Float.parseFloat(fieldY.getText());
				Vector3f pos = oiw.getPosition();
				pos.y = f;
				oiw.setPosition(pos);
			} catch(NumberFormatException e) {}
		}
		
		if(source == fieldZ) {
			try {
				float f = Float.parseFloat(fieldZ.getText());
				Vector3f pos = oiw.getPosition();
				pos.z = f;
				oiw.setPosition(pos);
			} catch(NumberFormatException e) {}
		}
	}
}
