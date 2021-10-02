package com.marginallyclever.robotOverlord.robotArmInterface.jogInterface;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.UIManager;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.robots.sixi3.Sixi3Bone;
import com.marginallyclever.robotOverlord.robots.sixi3.Sixi3IK;

public class AngleReportPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private JSlider [] joint;
	private JLabel [] values;
	
	public AngleReportPanel(Sixi3IK sixi3) {
		super();
		
		joint = new JSlider[sixi3.getNumBones()];
		values = new JLabel[sixi3.getNumBones()];
		this.setBorder(BorderFactory.createTitledBorder("AngleReport"));
		this.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=0;
		c.weighty=0;
		c.gridheight=1;
		c.gridwidth=1;
		
		for(int i=0;i<joint.length;++i) {
			Sixi3Bone bone = sixi3.getBone(i);
			c.gridx=0;
			c.weightx=0;
			c.anchor=GridBagConstraints.WEST;
			c.fill = GridBagConstraints.NONE;
			this.add(new JLabel(bone.getName()),c);
			c.gridx=1;
			c.weightx=0;
			c.anchor=GridBagConstraints.EAST;
			c.fill = GridBagConstraints.NONE;
			this.add(values[i]=new JLabel(),c);
			
			c.gridx=2;
			c.weightx=1;
			c.anchor=GridBagConstraints.WEST;
			c.fill = GridBagConstraints.HORIZONTAL;
			joint[i] = makeSliderFromBone(bone);
			this.add(joint[i],c);
			
			c.gridy++;
		}

		sixi3.addPropertyChangeListener((e)-> updateReport(sixi3) );
		updateReport(sixi3);
	}

	private void updateReport(Sixi3IK sixi3) {
		for(int i=0;i<joint.length;++i) {
			Sixi3Bone bone = sixi3.getBone(i);
			double t = bone.getTheta();
			joint[i].setValue((int)t);
			values[i].setText(String.format("%.3f",t));
		}
	}

	private JSlider makeSliderFromBone(Sixi3Bone b) {
		JSlider slider = new JSlider((int)b.getAngleMin(),
							(int)b.getAngleMax(),
							(int)b.getTheta());
		slider.setEnabled(false);
		return slider;
	}

	// TEST

	public static void main(String[] args) {
		Log.start();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}

		JFrame frame = new JFrame("AngleReportPanel");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new AngleReportPanel(new Sixi3IK()));
		frame.pack();
		frame.setVisible(true);
	}
}
