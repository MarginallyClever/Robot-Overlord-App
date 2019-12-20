package com.marginallyclever.robotOverlord.dhRobot.robots.sixi2;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.marginallyclever.convenience.SpringUtilities;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.CollapsiblePanel;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.dhRobot.DHLink;

/**
 * Control Panel for a DHRobot
 * @author Dan Royer
 *
 */
public class Sixi2Panel extends JPanel implements ActionListener, ChangeListener, ItemListener, Observer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	protected Sixi2 robot;
	protected RobotOverlord ro;

	public JButton buttonCalibrate;
	public JButton goHome;
	public JButton goRest;
	public JSlider feedrate, acceleration, gripperOpening;
	public JLabel  feedrateValue, accelerationValue, gripperOpeningValue;
	
	public class Pair {
		public JSlider slider;
		public DHLink  link;
		public JLabel  label;
		
		public Pair(JSlider slider0,DHLink link0,JLabel label0) {
			slider=slider0;
			link=link0;
			label=label0;
		}
	}
	
	ArrayList<Pair> liveJoints = new ArrayList<Pair>();
	ArrayList<Pair> ghostJoints = new ArrayList<Pair>();
	
	
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
		contents.add(feedrateValue=new JLabel(),con1);
		con1.gridy++;
		feedrate.setMaximum(80);
		feedrate.setMinimum(1);
		feedrate.setMinorTickSpacing(1);
		feedrate.addChangeListener(this);
		feedrate.setValue((int)robot.getFeedrate());
		stateChanged(new ChangeEvent(feedrate));

		contents.add(acceleration=new JSlider(),con1);
		con1.gridy++;
		contents.add(accelerationValue=new JLabel(),con1);
		con1.gridy++;
		acceleration.setMaximum(120);
		acceleration.setMinimum(1);
		acceleration.setMinorTickSpacing(1);
		acceleration.addChangeListener(this);
		acceleration.setValue((int)robot.getAcceleration());
		stateChanged(new ChangeEvent(acceleration));

		contents.add(gripperOpening=new JSlider(),con1);
		con1.gridy++;
		contents.add(gripperOpeningValue=new JLabel(),con1);
		con1.gridy++;
		gripperOpening.setMaximum(120);
		gripperOpening.setMinimum(90);
		gripperOpening.setMinorTickSpacing(5);
		gripperOpening.addChangeListener(this);
		gripperOpening.setValue((int)robot.dhTool.getAdjustableValue());
		stateChanged(new ChangeEvent(gripperOpening));
		
		int i;
		JLabel label;

		CollapsiblePanel livePanel = new CollapsiblePanel("Live");
		this.add(livePanel,con1);
		con1.gridy++;
		contents = livePanel.getContentPane();
		contents.setBorder(new EmptyBorder(0,0,0,0));
		contents.setLayout(new SpringLayout());
		i=0;
		for( DHLink link : robot.links ) {
			if(!link.hasAdjustableValue()) continue;
			JSlider newSlider=new JSlider(
					JSlider.HORIZONTAL,
					(int)link.rangeMin,
					(int)link.rangeMax,
					(int)link.rangeMin);
			newSlider.setMinorTickSpacing(5);
			newSlider.setEnabled(false);
			contents.add(new JLabel(Integer.toString(i++)));
			contents.add(newSlider);
			contents.add(label=new JLabel("0.000",SwingConstants.RIGHT));
			liveJoints.add(new Pair(newSlider,link,label));
			link.addObserver(this);
			newSlider.setValue((int)link.getAdjustableValue());
		}
		SpringUtilities.makeCompactGrid(contents, i, 3, 5, 5, 5, 5);
		
		CollapsiblePanel ghostPanel = new CollapsiblePanel("Ghost");
		this.add(ghostPanel,con1);
		con1.gridy++;
		contents = ghostPanel.getContentPane();
		contents.setBorder(new EmptyBorder(0,0,0,0));
		contents.setLayout(new SpringLayout());
		i=0;
		for( DHLink link : robot.ghost.links ) {
			if(!link.hasAdjustableValue()) continue;
			JSlider newSlider=new JSlider(
					JSlider.HORIZONTAL,
					(int)link.rangeMin,
					(int)link.rangeMax,
					(int)link.rangeMin);
			newSlider.setMinorTickSpacing(5);
			newSlider.setEnabled(false);
			contents.add(new JLabel(Integer.toString(i++)));
			contents.add(newSlider);
			contents.add(label=new JLabel("0.000",SwingConstants.RIGHT));
			ghostJoints.add(new Pair(newSlider,link,label));
			link.addObserver(this);
			newSlider.setValue((int)link.getAdjustableValue());
		}
		SpringUtilities.makeCompactGrid(contents, i, 3, 5, 5, 5, 5);
	}
	
	@Override
	public void stateChanged(ChangeEvent event) {
		Object source = event.getSource();
		if(source == feedrate) {
			int v = feedrate.getValue();
			robot.setFeedrate(v);
			feedrateValue.setText("feedrate = "+StringHelper.formatDouble(v));
		}
		if(source == acceleration) {
			int v = acceleration.getValue();
			robot.setAcceleration(v);
			accelerationValue.setText("acceleration = "+StringHelper.formatDouble(v));
		}
		if(source == gripperOpening) {
			int v = gripperOpening.getValue();
			robot.parseGCode("G0 T"+v);
			gripperOpeningValue.setText("gripper = "+StringHelper.formatDouble(v));
		}
		
		if(!robot.isDisablePanel()) {
			for( Pair p : ghostJoints ) {
				if(p.slider == source) {
					if(!p.link.hasChanged()) {
						int v = ((JSlider)source).getValue();
						p.link.setAdjustableValue(v);
						p.label.setText(StringHelper.formatDouble(v));
						robot.setDisablePanel(true);
						robot.ghost.refreshPose();
						robot.ghost.setTargetMatrix(robot.ghost.getLiveMatrix());
						robot.setDisablePanel(false);
						break;
					}
				}
			}
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

	@Override
	public void update(Observable arg0, Object arg1) {
		for( Pair p : liveJoints ) {
			if(p.link == arg0) {
				double v = (double)arg1;
				p.slider.setValue((int)v);
				p.label.setText(StringHelper.formatDouble(v));
				break;
			}
		}
		for( Pair p : ghostJoints ) {
			if(p.link == arg0) {
				double v = (double)arg1;
				p.slider.setValue((int)v);
				p.label.setText(StringHelper.formatDouble(v));
				break;
			}
		}
	}
}
