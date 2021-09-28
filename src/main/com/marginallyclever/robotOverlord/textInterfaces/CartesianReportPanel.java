package com.marginallyclever.robotOverlord.textInterfaces;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.vecmath.Matrix4d;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.robots.sixi3.Sixi3IK;

public class CartesianReportPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private JLabel x = new JLabel("",JLabel.RIGHT);
	private JLabel y = new JLabel("",JLabel.RIGHT);
	private JLabel z = new JLabel("",JLabel.RIGHT);
	
	public CartesianReportPanel(Sixi3IK sixi3) {
		super();
		
		this.setBorder(BorderFactory.createTitledBorder("Finger position"));
		this.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=0;
		c.weighty=0;
		c.gridheight=1;
		c.gridwidth=1;
		c.anchor=GridBagConstraints.WEST;

		buildCartesianReportLine(this,c,x,"X");
		buildCartesianReportLine(this,c,y,"Y");
		buildCartesianReportLine(this,c,z,"Z");

		sixi3.addPropertyChangeListener((e)-> {
			Matrix4d m = sixi3.getEndEffector();
			x.setText(StringHelper.formatDouble(m.m03));
			y.setText(StringHelper.formatDouble(m.m13));
			z.setText(StringHelper.formatDouble(m.m23));
		});
	}
	
	private void buildCartesianReportLine(JPanel panel,GridBagConstraints c,JLabel field,String label) {
		c.gridx=0;
		c.weightx=0;
		c.fill = GridBagConstraints.NONE;
		panel.add(new JLabel(label),c);
		
		c.gridx=1;
		c.weightx=1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(field,c);
		
		field.setEnabled(false);
		
		c.gridy++;
	}
}
