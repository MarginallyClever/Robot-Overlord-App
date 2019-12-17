package com.marginallyclever.robotOverlord.dhRobot.robots.sixi2;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.Matrix4d;

import com.marginallyclever.robotOverlord.CollapsiblePanel;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.dhRobot.DHKeyframe;
import com.marginallyclever.robotOverlord.robot.RobotKeyframe;

/**
 * Control Panel for a DHRobot
 * @author Dan Royer
 *
 */
public class Sixi2Panel extends JPanel implements ActionListener, ChangeListener, ItemListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	protected Sixi2 robot;
	protected RobotOverlord ro;

	public JButton buttonCalibrate;
	public JButton goHome;
	public JButton goRest;
	
	
	public Sixi2Panel(RobotOverlord gui,Sixi2 robot) {
		this.robot = robot;
		this.ro = gui;
		
		buildPanel();
	}
	
	protected void buildPanel() {
		this.removeAll();

		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=1;
		c.weighty=1;
		c.anchor=GridBagConstraints.NORTHWEST;
		c.fill=GridBagConstraints.HORIZONTAL;

		CollapsiblePanel oiwPanel = new CollapsiblePanel("Sixi2");
		this.add(oiwPanel,c);
		JPanel contents = oiwPanel.getContentPane();		
		
		contents.setBorder(new EmptyBorder(0,0,0,0));
		contents.setLayout(new GridBagLayout());
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=1;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;

		//this.add(toggleATC=new JButton(robot.dhTool!=null?"ATC close":"ATC open"), con1);
		contents.add(buttonCalibrate=new JButton("Calibrate"), con1);
		con1.gridy++;
		buttonCalibrate.addActionListener(this);

		contents.add(goHome=new JButton("Go Home"), con1);
		con1.gridy++;
		goHome.addActionListener(this);
		
		contents.add(goRest=new JButton("Go Rest"), con1);
		con1.gridy++;
		goRest.addActionListener(this);
	}
	
	@Override
	public void stateChanged(ChangeEvent event) {
		//Object source = event.getSource();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source == buttonCalibrate) {
			// run the calibration app
			Sixi2Calibrator calibrator = new Sixi2Calibrator(ro.getMainFrame(),robot);
			calibrator.run();
		}
		if(source==goHome) {
			robot.parseGCode("G0 X0 Y0 Z0 U0 V0 W0");
		}
		if(source==goRest) {
			JOptionPane.showMessageDialog(null, "Not implemented yet.");
			//robot.parseGCode("G0 X0 Y0 Z0 U0 V0 W0");
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// for checkboxes
		//Object source = e.getItemSelectable();
	}
}
