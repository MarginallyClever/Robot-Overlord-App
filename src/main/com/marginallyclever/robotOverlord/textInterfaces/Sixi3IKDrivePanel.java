package com.marginallyclever.robotOverlord.textInterfaces;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.marginallyclever.robotOverlord.robots.sixi3.Sixi3IK;

public class Sixi3IKDrivePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private ButtonGroup buttonGroup = new ButtonGroup();
	private JRadioButton x = makeRadioButton(buttonGroup,"X");
	private JRadioButton y = makeRadioButton(buttonGroup,"Y");
	private JRadioButton z = makeRadioButton(buttonGroup,"Z");
	private JRadioButton roll = makeRadioButton(buttonGroup,"roll");
	private JRadioButton pitch = makeRadioButton(buttonGroup,"pitch");
	private JRadioButton yaw = makeRadioButton(buttonGroup,"yaw");
	private Dial dial = new Dial();

	public Sixi3IKDrivePanel(Sixi3IK sixi3) {
		super();

		x.setSelected(true);
		
		dial.addActionListener((evt)-> {
			double v_mm = dial.getChange()*0.1;
			Matrix4d m4 = sixi3.getEndEffectorTarget();

			Vector3d p=new Vector3d();
			Matrix3d m3 = new Matrix3d(); 
			m4.get(p);
			m4.get(m3);
			
			if(x.isSelected()) m4.m03 += v_mm;
			if(y.isSelected()) m4.m13 += v_mm;
			if(z.isSelected()) m4.m23 += v_mm;
			if(roll.isSelected()) {
				Matrix3d rot = new Matrix3d();
				rot.rotZ(v_mm);
				m3.mul(rot);
				m4.set(m3);
				m4.setTranslation(p);
			}
			if(pitch.isSelected()) {
				Matrix3d rot = new Matrix3d();
				rot.rotX(v_mm);
				m3.mul(rot);
				m4.set(m3);
				m4.setTranslation(p);
			}
			if(yaw.isSelected()) {
				Matrix3d rot = new Matrix3d();
				rot.rotY(v_mm);
				m3.mul(rot);
				m4.set(m3);
				m4.setTranslation(p);
			}
			
			sixi3.setEndEffectorTarget(m4);
		});

		this.setBorder(BorderFactory.createTitledBorder("Finger tip control"));
		this.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=0;
		c.weighty=0;
		c.gridheight=1;
		c.gridwidth=1;
		c.anchor=GridBagConstraints.WEST;
		
		this.add(x,c);
		c.gridy++;
		this.add(y,c);
		c.gridy++;
		this.add(z,c);
		c.gridy++;
		this.add(roll,c);
		c.gridy++;
		this.add(pitch,c);
		c.gridy++;
		this.add(yaw,c);
		
		c.gridx=2;
		c.gridy=0;
		c.weightx=1;
		c.weighty=1;
		c.gridwidth=6;
		c.gridheight=6;
		c.anchor=GridBagConstraints.EAST;
		dial.setPreferredSize(new Dimension(120,120));
		this.add(dial,c);
	}

	private JRadioButton makeRadioButton(ButtonGroup group, String label) {
		JRadioButton rb = new JRadioButton(label);
		rb.setActionCommand(label);
		group.add(rb);
		return rb;
	}
}
