package com.marginallyclever.robotOverlord.dhRobot.robots.sixi2;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.marginallyclever.robotOverlord.CollapsiblePanel;
import com.marginallyclever.robotOverlord.RobotOverlord;

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
	public JSlider gripperOpening;
	public JSlider feedrate;
	public JLabel  feedrateValue;
	
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

		contents.add(feedrate=new JSlider(),con1);
		con1.gridy++;
		feedrate.setMaximum(80);
		feedrate.setMinimum(1);
		feedrate.setMinorTickSpacing(1);
		feedrate.setValue(20);
		feedrate.addChangeListener(this);
		contents.add(feedrateValue=new JLabel(),con1);
		feedrateValue.setText(Double.toString(feedrate.getValue()));
		con1.gridy++;

		contents.add(gripperOpening=new JSlider(),con1);
		con1.gridy++;
		gripperOpening.setMaximum(130);
		gripperOpening.setMinimum(80);
		gripperOpening.setMinorTickSpacing(5);
		gripperOpening.setValue((int)robot.dhTool.getAdjustableValue());
		gripperOpening.addChangeListener(this);
	}
	
	@Override
	public void stateChanged(ChangeEvent event) {
		Object source = event.getSource();
		if(source == feedrate) {
			int v = feedrate.getValue();
			robot.setFeedrate(v);
			feedrateValue.setText(Double.toString(v));
		}
		if(source == gripperOpening) {
			int v = gripperOpening.getValue();
			robot.parseGCode("G0 T"+v);
		}
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
			robot.ghost.setPoseFK(robot.homeKey);
			robot.ghost.setTargetMatrix(robot.ghost.getLiveMatrix());
		}
		if(source==goRest) {
			robot.ghost.setPoseFK(robot.restKey);
			robot.ghost.setTargetMatrix(robot.ghost.getLiveMatrix());
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// for checkboxes
		//Object source = e.getItemSelectable();
	}
}
