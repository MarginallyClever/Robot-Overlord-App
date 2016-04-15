package com.marginallyclever.robotOverlord.Spidee;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.marginallyclever.robotOverlord.CollapsiblePanel;
import com.marginallyclever.robotOverlord.RobotOverlord;

public class SpideeControlPanel extends JPanel implements ChangeListener, ActionListener, ItemListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2329511559180664356L;

	protected Spidee robot;
	
	//private JLabel uid;
	private final double [] speedOptions = {0.1, 0.2, 0.3, 
			                                .4, .5, .6, 
			                                .7, .8, .9, 1.0};
	private JLabel speedNow;
	private JSlider speedControl;
	
	private JButton buttonWalkUp;
	private JButton buttonWalkDown;
	private JButton buttonWalkLeft;
	private JButton buttonWalkRight;
	private JButton buttonWalkForward;
	private JButton buttonWalkBackward;
	
	private JButton buttonTurnUp;
	private JButton buttonTurnDown;
	private JButton buttonTurnLeft;
	private JButton buttonTurnRight;

	private JButton buttonResetLegs;
	private JComboBox<String> moveModeControl;
	
	
	private JButton createButton(String name) {
		JButton b = new JButton(name);
		b.addActionListener(this);
		return b;
	}
	
	SpideeControlPanel(RobotOverlord gui,Spidee robot) {
		this.robot = robot;

		this.setLayout(new GridBagLayout());
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=1;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.NORTH;

		JPanel p;

		CollapsiblePanel speedPanel = createSpeedPanel();
		this.add(speedPanel,con1);
		con1.gridy++;

		CollapsiblePanel movePanel = new CollapsiblePanel("Move Style");
		p = movePanel.getContentPane();
		moveModeControl = new JComboBox<String>();
		moveModeControl.addItem("Calibrate");
		moveModeControl.addItem("Sit");
		moveModeControl.addItem("Stand");
		moveModeControl.addItem("Rotate body");
		moveModeControl.addItem("Ripple walk");
		moveModeControl.addItem("Wave walk");
		moveModeControl.addItem("Tripod walk");
		p.add(moveModeControl);
		moveModeControl.addItemListener(this);
		this.add(movePanel,con1);
		con1.gridy++;

		CollapsiblePanel p2 = new CollapsiblePanel("Walk");
		JPanel p1 = p2.getContentPane();
		p1.setLayout(new GridLayout(0,1));
			p = new JPanel(new GridLayout(3,3));
			p.add(new JLabel(""));
			p.add(buttonWalkUp = createButton("Higher"));
			p.add(new JLabel(""));
			
			p.add(buttonWalkLeft = createButton("Left"));
			p.add(new JLabel(""));
			p.add(buttonWalkRight = createButton("Right"));
			
			p.add(new JLabel(""));
			p.add(buttonWalkDown = createButton("Lower"));
			p.add(new JLabel(""));
			p1.add(p);

			p = new JPanel(new GridLayout(2,1));
			p.add(buttonWalkForward = createButton("Forward"));
			p.add(buttonWalkBackward = createButton("Backward"));
			p1.add(p);
		this.add(p2,con1);
		con1.gridy++;

		p2 = new CollapsiblePanel("Turn");
		p1 = p2.getContentPane();
		p1.setLayout(new GridLayout(3,3));
			p1.add(new JLabel(""));
			p1.add(buttonTurnUp = createButton("Tilt up"));
			p1.add(new JLabel(""));
			p1.add(buttonTurnLeft = createButton("Left"));
			p1.add(new JLabel(""));
			p1.add(buttonTurnRight = createButton("Right"));
			p1.add(new JLabel(""));
			p1.add(buttonTurnDown = createButton("Tilt down"));
		this.add(p2,con1);
		con1.gridy++;

		p2 = new CollapsiblePanel("Turn");
		p1 = p2.getContentPane();
		buttonResetLegs = createButton("Reset legs");
		p1.add(buttonResetLegs);
	}
	
	private CollapsiblePanel createSpeedPanel() {
		CollapsiblePanel speedPanel = new CollapsiblePanel("Speed");
		GridBagConstraints con2 = new GridBagConstraints();
		con2.gridx=0;
		con2.gridy=0;
		con2.fill=GridBagConstraints.HORIZONTAL;
		con2.anchor=GridBagConstraints.NORTHWEST;
		con2.weighty=1;
		con2.weightx=0.25;

		double speed=robot.speed_scale;
		int speedIndex;
		for(speedIndex=0;speedIndex<speedOptions.length;++speedIndex) {
			if( speedOptions[speedIndex] >= speed )
				break;
		}
		
		speedNow = new JLabel(Double.toString(speedOptions[speedIndex]),JLabel.CENTER);
		java.awt.Dimension dim = speedNow.getPreferredSize();
		dim.width = 50;
		speedNow.setPreferredSize(dim);
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
		robot.setSpeed((float)speed);
		speedNow.setText(Double.toString(speed));
	}
	
	public void stateChanged(ChangeEvent e) {
		Object subject = e.getSource();
		if( subject == speedControl ) {
			int i=speedControl.getValue();
			setSpeed(speedOptions[i]);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();
		
		if( subject == buttonResetLegs ) {
			robot.Reset_Position();
			return;
		}

/*
		  buttons[BUTTONS_Y_ROT_POS] = (int) Input.GetSingleton().GetAxisState("spidee","tilt_left");
		  buttons[BUTTONS_Y_ROT_NEG] = (int) Input.GetSingleton().GetAxisState("spidee","tilt_right");
		  buttons[BUTTONS_0]         = (int) Input.GetSingleton().GetAxisState("spidee","recenter");
*/
		if( subject == buttonWalkUp ) {
			robot.buttons[Spidee.BUTTONS_Z_POS] = (robot.buttons[Spidee.BUTTONS_Z_POS]==1? 0:1);
			robot.buttons[Spidee.BUTTONS_Z_NEG] = 0;
		}
		if( subject == buttonWalkDown ) {
			robot.buttons[Spidee.BUTTONS_Z_NEG] = (robot.buttons[Spidee.BUTTONS_Z_NEG]==1? 0:1);
			robot.buttons[Spidee.BUTTONS_Z_POS] = 0;
		}
		if( subject == buttonWalkLeft ) {
			robot.buttons[Spidee.BUTTONS_X_POS] = (robot.buttons[Spidee.BUTTONS_X_POS]==1? 0:1);
			robot.buttons[Spidee.BUTTONS_X_NEG] = 0;
		}
		if( subject == buttonWalkRight ) {
			robot.buttons[Spidee.BUTTONS_X_NEG] = (robot.buttons[Spidee.BUTTONS_X_NEG]==1? 0:1);
			robot.buttons[Spidee.BUTTONS_X_POS] = 0;
		}
		if( subject == buttonWalkForward ) {
			robot.buttons[Spidee.BUTTONS_Y_POS] = (robot.buttons[Spidee.BUTTONS_Y_POS]==1? 0:1);
			robot.buttons[Spidee.BUTTONS_Y_NEG] = 0;
		}
		if( subject == buttonWalkBackward ) {
			robot.buttons[Spidee.BUTTONS_Y_NEG] = (robot.buttons[Spidee.BUTTONS_Y_NEG]==1? 0:1);
			robot.buttons[Spidee.BUTTONS_Y_POS] = 0;
		}
		if( subject == buttonTurnDown ) {
			robot.buttons[Spidee.BUTTONS_X_ROT_NEG] = (robot.buttons[Spidee.BUTTONS_X_ROT_NEG]==1? 0:1);
			robot.buttons[Spidee.BUTTONS_X_ROT_POS] = 0;	
		}
		if( subject == buttonTurnUp ) {
			robot.buttons[Spidee.BUTTONS_X_ROT_POS] = (robot.buttons[Spidee.BUTTONS_X_ROT_POS]==1? 0:1);
			robot.buttons[Spidee.BUTTONS_X_ROT_NEG] = 0;
		}
		if( subject == buttonTurnLeft ) {
			robot.buttons[Spidee.BUTTONS_Z_ROT_POS] = (robot.buttons[Spidee.BUTTONS_Z_ROT_POS]==1? 0:1);
			robot.buttons[Spidee.BUTTONS_Z_ROT_NEG] = 0;
		}
		if( subject == buttonTurnRight ) {
			robot.buttons[Spidee.BUTTONS_Z_ROT_NEG] = (robot.buttons[Spidee.BUTTONS_Z_ROT_NEG]==1? 0:1);
			robot.buttons[Spidee.BUTTONS_Z_ROT_POS] = 0;
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		Object subject = e.getSource();
		
		if( subject == moveModeControl ) {
			Spidee.MoveModes moveMode =Spidee.MoveModes.MOVE_MODE_SITDOWN;
			
			int index = moveModeControl.getSelectedIndex();
	    	switch(index) {
	    	//case moveMode_CALIBRATE:  
	    	case 2:  moveMode = Spidee.MoveModes.MOVE_MODE_STANDUP  ;  break;
	    	case 3:  moveMode = Spidee.MoveModes.MOVE_MODE_BODY     ;  break;
	    	case 4:  moveMode = Spidee.MoveModes.MOVE_MODE_RIPPLE   ;  break;
	    	case 5:  moveMode = Spidee.MoveModes.MOVE_MODE_WAVE     ;  break;
	    	case 6:  moveMode = Spidee.MoveModes.MOVE_MODE_TRIPOD   ;  break;
	    	case 7:  moveMode = Spidee.MoveModes.MOVE_MODE_CALIBRATE;  break;
			default: break;
	    	}
	    	
	    	robot.setMoveMode(moveMode);
		}
	}
}
