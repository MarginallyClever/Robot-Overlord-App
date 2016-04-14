package com.marginallyclever.robotOverlord.DeltaRobot3;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.marginallyclever.robotOverlord.CollapsiblePanel;

public class DeltaRobot3ControlPanel extends JPanel implements ActionListener, ChangeListener {
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 257878994328366520L;
	
	private DeltaRobot3 robot=null;
	
	private JLabel uid;
	private JButton goHome;

	private final double [] speedOptions = {0.1, 0.2, 0.5, 
			                                1, 2, 5, 
			                                10, 20, 50};
	private JLabel speedNow;
	private JSlider speedControl;
	
	private JButton arm5Apos;
	private JButton arm5Aneg;
	private JButton arm5Bpos;
	private JButton arm5Bneg;
	private JButton arm5Cpos;
	private JButton arm5Cneg;
	public JLabel angleA,angleB,angleC;
	
	private JButton arm5Xpos;
	private JButton arm5Xneg;
	private JButton arm5Ypos;
	private JButton arm5Yneg;
	private JButton arm5Zpos;
	private JButton arm5Zneg;

	public JLabel xPos,yPos,zPos;
	
	private JButton undoButton, redoButton;
	
	
	private JButton createButton(String name) {
		JButton b = new JButton(name);
		b.addActionListener(this);
		return b;
	}


	public DeltaRobot3ControlPanel(DeltaRobot3 deltaRobot) {
		super();
		
		robot = deltaRobot;

		this.setLayout(new GridBagLayout());
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=1;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.NORTH;

		CollapsiblePanel speedPanel = createSpeedPanel();
		this.add(speedPanel,con1);
		con1.gridy++;

		CollapsiblePanel homePanel = new CollapsiblePanel("Calibration");
		this.add(homePanel,con1);
		con1.gridy++;

		goHome=createButton("Find Home");
		homePanel.getContentPane().add(goHome);
		
		//CollapsiblePanel fkPanel = 
				createFKPanel();
		//this.add(fkPanel,con1);
		//con1.gridy++;

		CollapsiblePanel ikPanel = createIKPanel();
		this.add(ikPanel, con1);
		con1.gridy++;
		
		
		// undo/redo panel
		CollapsiblePanel urPanel = new CollapsiblePanel("History");
		this.add(urPanel, con1);
		con1.gridy++;

		JPanel p = new JPanel(new GridLayout(1,2));
		urPanel.getContentPane().add(p);
		
		p.add(undoButton = createButton("Undo"));
		p.add(redoButton = createButton("Redo"));
	}

	protected CollapsiblePanel createFKPanel() {
		CollapsiblePanel cp = new CollapsiblePanel("Forward Kinematics");
		
		JPanel p = new JPanel(new GridLayout(3,3));
		cp.getContentPane().add(p);

		// used for FK
		angleA = new JLabel("0.00");
		angleB = new JLabel("0.00");
		angleC = new JLabel("0.00");

		p.add(arm5Apos = createButton("A+"));		p.add(angleA);		p.add(arm5Aneg = createButton("A-"));
		p.add(arm5Bpos = createButton("B+"));		p.add(angleB);		p.add(arm5Bneg = createButton("B-"));
		p.add(arm5Cpos = createButton("C+"));		p.add(angleC);		p.add(arm5Cneg = createButton("C-"));

		return cp;
	}
	
	protected CollapsiblePanel createIKPanel() {
		CollapsiblePanel cp = new CollapsiblePanel("Inverse Kinematics");
		
		JPanel p = new JPanel(new GridLayout(3,3));
		cp.getContentPane().add(p);

		// used for IK
		xPos = new JLabel("0.00");
		yPos = new JLabel("0.00");
		zPos = new JLabel("0.00");
		
		p.add(arm5Xpos = createButton("X+"));		p.add(xPos);		p.add(arm5Xneg = createButton("X-"));
		p.add(arm5Ypos = createButton("Y+"));		p.add(yPos);		p.add(arm5Yneg = createButton("Y-"));
		p.add(arm5Zpos = createButton("Z+"));		p.add(zPos);		p.add(arm5Zneg = createButton("Z-"));
		
		return cp;
	}
	
	protected CollapsiblePanel createSpeedPanel() {
		double speed=robot.getSpeed();
		int speedIndex;
		for(speedIndex=0;speedIndex<speedOptions.length;++speedIndex) {
			if( speedOptions[speedIndex] >= speed )
				break;
		}
		speedNow = new JLabel(Double.toString(speedOptions[speedIndex]),JLabel.CENTER);
		java.awt.Dimension dim = speedNow.getPreferredSize();
		dim.width = 50;
		speedNow.setPreferredSize(dim);

		CollapsiblePanel speedPanel = new CollapsiblePanel("Speed");
		
		GridBagConstraints con2 = new GridBagConstraints();
		con2.gridx=0;
		con2.gridy=0;
		con2.fill=GridBagConstraints.HORIZONTAL;
		con2.anchor=GridBagConstraints.NORTHWEST;
		con2.weighty=1;
		con2.weightx=0.25;
		speedPanel.getContentPane().add(speedNow,con2);

		speedControl = new JSlider(0,speedOptions.length-1,speedIndex);
		speedControl.addChangeListener(this);
		speedControl.setMajorTickSpacing(speedOptions.length-1);
		speedControl.setMinorTickSpacing(1);
		speedControl.setPaintTicks(true);
		con2.anchor=GridBagConstraints.NORTHEAST;
		con2.fill=GridBagConstraints.HORIZONTAL;
		con2.weightx=0.75;
		con2.gridx=1;
		speedPanel.getContentPane().add(speedControl,con2);
		
		return speedPanel;
	}

	
	protected void setSpeed(double speed) {
		robot.setSpeed(speed);
		speedNow.setText(Double.toString(robot.getSpeed()));
	}
	
	
	public void stateChanged(ChangeEvent e) {
		Object subject = e.getSource();
		if( subject == speedControl ) {
			int i=speedControl.getValue();
			setSpeed(speedOptions[i]);
		}
	}
	
	
	// arm5 controls
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();			
		
		if( subject == goHome   ) robot.goHome();
		if( subject == arm5Apos ) robot.commandSequence.addEdit(new DeltaRobot3MoveCommand(robot,DeltaRobot3MoveCommand.AXIS_A, 1));
		if( subject == arm5Aneg ) robot.commandSequence.addEdit(new DeltaRobot3MoveCommand(robot,DeltaRobot3MoveCommand.AXIS_A,-1));
		if( subject == arm5Bpos ) robot.commandSequence.addEdit(new DeltaRobot3MoveCommand(robot,DeltaRobot3MoveCommand.AXIS_B, 1));
		if( subject == arm5Bneg ) robot.commandSequence.addEdit(new DeltaRobot3MoveCommand(robot,DeltaRobot3MoveCommand.AXIS_B,-1));
		if( subject == arm5Cpos ) robot.commandSequence.addEdit(new DeltaRobot3MoveCommand(robot,DeltaRobot3MoveCommand.AXIS_C, 1));
		if( subject == arm5Cneg ) robot.commandSequence.addEdit(new DeltaRobot3MoveCommand(robot,DeltaRobot3MoveCommand.AXIS_C,-1));
		
		if( subject == arm5Xpos ) robot.commandSequence.addEdit(new DeltaRobot3MoveCommand(robot,DeltaRobot3MoveCommand.AXIS_X, 1));
		if( subject == arm5Xneg ) robot.commandSequence.addEdit(new DeltaRobot3MoveCommand(robot,DeltaRobot3MoveCommand.AXIS_X,-1));
		if( subject == arm5Ypos ) robot.commandSequence.addEdit(new DeltaRobot3MoveCommand(robot,DeltaRobot3MoveCommand.AXIS_Y, 1));
		if( subject == arm5Yneg ) robot.commandSequence.addEdit(new DeltaRobot3MoveCommand(robot,DeltaRobot3MoveCommand.AXIS_Y,-1));
		if( subject == arm5Zpos ) robot.commandSequence.addEdit(new DeltaRobot3MoveCommand(robot,DeltaRobot3MoveCommand.AXIS_Z, 1));
		if( subject == arm5Zneg ) robot.commandSequence.addEdit(new DeltaRobot3MoveCommand(robot,DeltaRobot3MoveCommand.AXIS_Z,-1));
		
		if( subject == redoButton ) robot.redo();
		if( subject == undoButton ) robot.undo();
	}
	
	
	public void setUID(long id) {
		if(uid!=null) {
			uid.setText(DeltaRobot3.ROBOT_NAME+" #"+Long.toString(id));
		}
	}


	protected float roundOff(float v) {
		float SCALE = 1000.0f;
		
		return Math.round(v*SCALE)/SCALE;
	}
	
	
	public void update() {
		angleA.setText(Float.toString(roundOff(robot.motionNow.arms[0].angle)));
		angleB.setText(Float.toString(roundOff(robot.motionNow.arms[1].angle)));
		angleC.setText(Float.toString(roundOff(robot.motionNow.arms[2].angle)));
		xPos.setText(Float.toString(roundOff(robot.motionNow.fingerPosition.x)));
		yPos.setText(Float.toString(roundOff(robot.motionNow.fingerPosition.y)));
		zPos.setText(Float.toString(roundOff(robot.motionNow.fingerPosition.z)));
		
		arm5Apos.setEnabled(robot.isHomed());
		arm5Aneg.setEnabled(robot.isHomed());
		arm5Bpos.setEnabled(robot.isHomed());
		arm5Bneg.setEnabled(robot.isHomed());
		arm5Cpos.setEnabled(robot.isHomed());
		arm5Cneg.setEnabled(robot.isHomed());
		
		arm5Xpos.setEnabled(robot.isHomed());
		arm5Xneg.setEnabled(robot.isHomed());
		arm5Ypos.setEnabled(robot.isHomed());
		arm5Yneg.setEnabled(robot.isHomed());
		arm5Zpos.setEnabled(robot.isHomed());
		arm5Zneg.setEnabled(robot.isHomed());
		
		undoButton.setText(robot.commandSequence.getUndoPresentationName());
	    redoButton.setText(robot.commandSequence.getRedoPresentationName());
	    undoButton.getParent().validate();
	    undoButton.setEnabled(robot.commandSequence.canUndo());
	    redoButton.setEnabled(robot.commandSequence.canRedo());
	}
}
