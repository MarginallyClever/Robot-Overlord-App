package com.marginallyclever.robotOverlord.textInterfaces;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import com.marginallyclever.robotOverlord.robots.sixi3.Sixi3Bone;
import com.marginallyclever.robotOverlord.robots.sixi3.Sixi3IK;

public class AngleReportPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private JSlider [] joint;
	
	public AngleReportPanel(Sixi3IK sixi3) {
		super();
		
		joint = new JSlider[sixi3.getNumBones()];
		this.setBorder(BorderFactory.createTitledBorder("Joint angles"));
		this.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=0;
		c.weighty=0;
		c.gridheight=1;
		c.gridwidth=1;
		c.anchor=GridBagConstraints.WEST;
		
		for(int i=0;i<joint.length;++i) {
			Sixi3Bone bone = sixi3.getBone(i);
			c.gridx=0;
			c.weightx=0;
			c.fill = GridBagConstraints.NONE;
			this.add(new JLabel(bone.getName()),c);
			
			c.gridx=1;
			c.weightx=1;
			c.fill = GridBagConstraints.HORIZONTAL;
			joint[i] = makeSliderFromBone(bone);
			this.add(joint[i],c);
			
			c.gridy++;
		}

		sixi3.addPropertyChangeListener((e)-> {
			for(int i=0;i<joint.length;++i) {
				Sixi3Bone bone = sixi3.getBone(i);
				joint[i].setValue((int)bone.getTheta());
			}
		});	
	}

	private JSlider makeSliderFromBone(Sixi3Bone b) {
		JSlider slider = new JSlider((int)b.getAngleMin(),
							(int)b.getAngleMax(),
							(int)b.getTheta());
		slider.setEnabled(false);
		return slider;
	}
}
