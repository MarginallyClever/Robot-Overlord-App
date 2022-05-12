package com.marginallyclever.robotOverlord.robots.robotArm.robotArmInterface.jogInterface;

import java.awt.BorderLayout;
import java.io.Serial;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * Lets the user select the scale, where scale is a number from 1 to 5.  It is intended to be used as the x value in
 * 1/2<sup>-x</sup>
 */
public class ScalePanel extends JPanel {
	@Serial
	private static final long serialVersionUID = -6566683128076864855L;
	private final JSpinner stepScale = new JSpinner(new SpinnerNumberModel(1,1,5,1));
	
	public ScalePanel() {
		super();
		
		this.setLayout(new BorderLayout());
		this.add(new JLabel("Scale 1/(2^-x)"),BorderLayout.LINE_START);
		this.add(stepScale,BorderLayout.LINE_END);
	}
	
	public double getScale() {
		return ((Number)stepScale.getValue()).doubleValue();
	}
}
