package com.marginallyclever.makelangeloRobot.settings;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.Translator;
import com.marginallyclever.robotOverlord.actions.ActionSelectNumber;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;

public class PanelAdjustMachine extends JPanel implements ActionListener, PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -84665452555208524L;

	protected MakelangeloRobot robot;

	protected ActionSelectNumber machineWidth, machineHeight;
	protected JLabel totalBeltNeeded;
	protected JLabel totalServoNeeded;
	protected JLabel totalStepperNeeded;
	protected ActionSelectNumber acceleration;
	protected ActionSelectNumber pulleyDiameter;
	protected JCheckBox flipForGlass;


	protected JButton buttonAneg;
	protected JButton buttonApos;
	protected JButton buttonBneg;
	protected JButton buttonBpos;

	protected JCheckBox m1i;
	protected JCheckBox m2i;

	public PanelAdjustMachine( RobotOverlord gui, MakelangeloRobot robot ) {
		this.robot = robot;

		this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		// this.setLayout(new GridLayout(0,1,8,8));
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		GridBagConstraints c = new GridBagConstraints();
		GridBagConstraints d = new GridBagConstraints();
		c.ipadx = 5;
		c.ipady = 0;

		int y = 1;

		// adjust machine size
		JPanel machineSizePanel = new JPanel(new GridBagLayout());
		this.add(machineSizePanel);
		//machineSizePanel.setBorder(BorderFactory.createLineBorder(new Color(0,1,0)));
		machineSizePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

		float w = (float)robot.getSettings().getLimitWidth() * 10;
		float h = (float)robot.getSettings().getLimitHeight() * 10;
		
		machineWidth = new ActionSelectNumber(gui,Translator.get("MachineWidth"),w);
		machineHeight = new ActionSelectNumber(gui,Translator.get("MachineHeight"),h);
		Dimension s = machineHeight.getPreferredSize();
		s.width = 80;

		if(!robot.getSettings().getHardwareProperties().canChangeMachineSize()) {
			machineWidth.setValue(robot.getSettings().getHardwareProperties().getWidth());
			machineHeight.setValue(robot.getSettings().getHardwareProperties().getHeight());
		}			
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		d.anchor = GridBagConstraints.EAST;
		c.gridx = 0;		c.gridy = y;		machineSizePanel.add(machineWidth, c);
		d.gridx = 3;		d.gridy = y;		machineSizePanel.add(new JLabel("mm"), d);
		y++;
		c.gridx = 0;		c.gridy = y;		machineSizePanel.add(machineHeight, c);
		d.gridx = 3;		d.gridy = y;		machineSizePanel.add(new JLabel("mm"), d);
		y++;
	
		//machineWidth.setPreferredSize(s);
		//machineHeight.setPreferredSize(s);
		machineWidth.addPropertyChangeListener(this);
		machineHeight.addPropertyChangeListener(this);

		// stepper needed
		c.gridx = 0;
		c.gridwidth=1;
		d.gridx = 1;
		d.gridwidth=2;
		c.gridy = y;
		d.gridy = y;
		y++;
		machineSizePanel.add(new JLabel(Translator.get("StepperLengthNeeded")),c);
		machineSizePanel.add(totalStepperNeeded = new JLabel("?"),d);
		// belt needed
		c.gridy = y;
		d.gridy = y;
		y++;
		machineSizePanel.add(new JLabel(Translator.get("BeltLengthNeeded")),c);
		machineSizePanel.add(totalBeltNeeded = new JLabel("?"),d);
		// servo needed
		c.gridy = y;
		d.gridy = y;
		y++;
		machineSizePanel.add(new JLabel(Translator.get("ServoLengthNeeded")),c);
		machineSizePanel.add(totalServoNeeded = new JLabel("?"),d);
		y++;
		c.gridx=0;
		c.gridwidth=4;
		c.gridy=y;
		machineSizePanel.add(new JSeparator(SwingConstants.HORIZONTAL),c);
		
		if(!robot.getSettings().getHardwareProperties().canChangeMachineSize()) {
			machineSizePanel.setVisible(false);
		}

		// adjust pulleys
		JPanel pulleyPanel = new JPanel(new GridBagLayout());
		this.add(pulleyPanel);

		c = new GridBagConstraints();
		c.ipadx = 5;
		c.ipady = 0;
		c.gridwidth = 1;

		float left = (float)Math.floor(robot.getSettings().getPulleyDiameter() * 10.0 * 1000.0) / 1000.0f;

		// pulley diameter
		pulleyDiameter = new ActionSelectNumber(gui,Translator.get("AdjustPulleySize"),left);
		if(robot.getSettings().getHardwareProperties().canChangePulleySize()) {
			y = 2;
			c.weightx = 0;
			c.anchor = GridBagConstraints.EAST;
			d.anchor = GridBagConstraints.WEST;
			c.gridx = 0;
			c.gridy = y;
			pulleyPanel.add(pulleyDiameter, d);
			d.gridx = 1;
			d.gridy = y;
			pulleyPanel.add(new JLabel(Translator.get("Millimeters")), d);
			y++;

			s = pulleyDiameter.getPreferredSize();
			s.width = 80;
			pulleyDiameter.setPreferredSize(s);
		}
		

		// acceleration
		JPanel accelerationPanel = new JPanel(new GridBagLayout());
		this.add(accelerationPanel);
		acceleration = new ActionSelectNumber(gui,Translator.get("AdjustAcceleration"),(float)robot.getSettings().getAcceleration());
		s = acceleration.getPreferredSize();
		s.width = 80;
		acceleration.setPreferredSize(s);
		y = 0;
		c.gridx = 0;
		c.gridy = y;
		accelerationPanel.add(acceleration, d);
		y++;
		accelerationPanel.add(new JSeparator());
		if(!robot.getSettings().getHardwareProperties().canAccelerate()) {
			accelerationPanel.setVisible(false);
		}

		// Jog motors
		JPanel jogPanel = new JPanel(new GridBagLayout());
		this.add(jogPanel);

		buttonAneg = new JButton(Translator.get("JogIn"));
		buttonApos = new JButton(Translator.get("JogOut"));
		m1i = new JCheckBox(Translator.get("Invert"), robot.getSettings().isLeftMotorInverted());

		buttonBneg = new JButton(Translator.get("JogIn"));
		buttonBpos = new JButton(Translator.get("JogOut"));
		m2i = new JCheckBox(Translator.get("Invert"), robot.getSettings().isRightMotorInverted());

		c.gridx = 0;
		c.gridy = 0;
		jogPanel.add(new JLabel(Translator.get("Left")), c);
		c.gridx = 0;
		c.gridy = 1;
		jogPanel.add(new JLabel(Translator.get("Right")), c);

		c.gridx = 1;
		c.gridy = 0;
		jogPanel.add(buttonAneg, c);
		c.gridx = 1;
		c.gridy = 1;
		jogPanel.add(buttonBneg, c);

		c.gridx = 2;
		c.gridy = 0;
		jogPanel.add(buttonApos, c);
		c.gridx = 2;
		c.gridy = 1;
		jogPanel.add(buttonBpos, c);

		c.gridx = 3;
		c.gridy = 0;
		jogPanel.add(m1i, c);
		c.gridx = 3;
		c.gridy = 1;
		jogPanel.add(m2i, c);

		buttonApos.addActionListener(this);
		buttonAneg.addActionListener(this);

		buttonBpos.addActionListener(this);
		buttonBneg.addActionListener(this);

		m1i.addActionListener(this);
		m2i.addActionListener(this);

		if(!robot.getSettings().getHardwareProperties().canInvertMotors()) {
			jogPanel.setVisible(false);
		} else {
			this.add(new JSeparator());
		}
		
		// flip for glass
		JPanel flipPanel = new JPanel(new GridBagLayout());
		this.add(flipPanel);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.gridx = 0;
		c.gridy=0;
		c.gridwidth = 2;
		flipForGlass = new JCheckBox(Translator.get("FlipForGlass"));
		flipForGlass.setSelected(robot.getSettings().isReverseForGlass());
		flipPanel.add(flipForGlass, c);


		// always have one extra empty at the end to push everything up.
		c.weighty = 1;
		flipPanel.add(new JLabel(), c);
		
		updateLengthNeeded();
	}

	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();

		// jog motors
		if (subject == buttonApos) robot.jogLeftMotorOut();
		if (subject == buttonAneg) robot.jogLeftMotorIn();
		if (subject == buttonBpos) robot.jogRightMotorOut();
		if (subject == buttonBneg) robot.jogRightMotorIn();

		if (subject == m1i || subject == m2i) {
			robot.getSettings().invertLeftMotor(m1i.isSelected());
			robot.getSettings().invertRightMotor(m2i.isSelected());
			robot.getSettings().saveConfig();
			robot.sendConfig();
		}
	}
	
	/**
	 * Calculate length of belt and servo needed based on machine dimensions.
	 */
	protected void updateLengthNeeded() {
		if(!robot.getSettings().getHardwareProperties().canChangeMachineSize()) return;
		
		double w = ((Number)machineWidth.getValue()).floatValue();
		double h = ((Number)machineHeight.getValue()).floatValue();
		double SAFETY_MARGIN=100;
		
		double mmBeltNeeded=(Math.sqrt(w*w+h*h)+SAFETY_MARGIN); // 10cm safety margin
		double beltNeeded = Math.ceil(mmBeltNeeded*0.001);
		totalBeltNeeded.setText(Double.toString(beltNeeded)+"m");
		
		double mmServoNeeded = (Math.sqrt(w*w+h*h)+SAFETY_MARGIN) + w/2.0; // 10cm safety margin
		double servoNeeded = Math.ceil(mmServoNeeded*0.001);
		totalServoNeeded.setText(Double.toString(servoNeeded)+"m");

		double mmStepperNeeded = w/2.0+SAFETY_MARGIN; // 10cm safety margin
		double stepperNeeded = Math.ceil(mmStepperNeeded*0.001);
		totalStepperNeeded.setText(Double.toString(stepperNeeded)+"m");
	}

	public void save() {
		double mwf = ((Number)machineWidth.getValue()).doubleValue() / 10.0;
		double mhf = ((Number)machineHeight.getValue()).doubleValue() / 10.0;
		double bld   = ((Number)pulleyDiameter.getValue()).doubleValue() / 10.0;
		double accel = ((Number)acceleration.getValue()).doubleValue();

		boolean data_is_sane = true;
		if (mwf <= 0) data_is_sane = false;
		if (mhf <= 0) data_is_sane = false;
		if (bld <= 0) data_is_sane = false;

		if (data_is_sane) {
			robot.getSettings().setReverseForGlass(flipForGlass.isSelected());
			robot.getSettings().setPulleyDiameter(bld);
			robot.getSettings().setMachineSize(mwf, mhf);
			robot.getSettings().setAcceleration(accel);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object subject = evt.getSource();

		if(subject == machineWidth || subject == machineHeight) {
			updateLengthNeeded();
		}
	}
}
