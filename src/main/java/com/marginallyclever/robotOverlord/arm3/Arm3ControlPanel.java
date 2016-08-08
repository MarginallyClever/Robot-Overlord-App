package com.marginallyclever.robotOverlord.arm3;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.marginallyclever.robotOverlord.CollapsiblePanel;
import com.marginallyclever.robotOverlord.RobotOverlord;

public class Arm3ControlPanel extends JPanel implements ActionListener, ChangeListener {
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 257878994328366520L;

	private final double [] speedOptions = {0.1, 0.2, 0.5, 
			                                1, 2, 5, 
			                                10, 20, 50};
	
	private JButton arm5Apos;
	private JButton arm5Aneg;
	private JButton arm5Bpos;
	private JButton arm5Bneg;
	private JButton arm5Cpos;
	private JButton arm5Cneg;
	
	private JButton arm5Xpos;
	private JButton arm5Xneg;
	private JButton arm5Ypos;
	private JButton arm5Yneg;
	private JButton arm5Zpos;
	private JButton arm5Zneg;
	
	// finger tip position relative to the base of the robot.
	public JLabel xPos,yPos,zPos;
	
	public JLabel labelFK1,labelFK2,labelFK3,d1,e1;
	public JLabel labelIK1,labelIK2,labelIK3,d2,e2;
	private JLabel speedNow;
	private JLabel uid;
	private JSlider speedControl;
	protected JButton about;
	private Arm3 robotArm=null;
	
	
	private JButton createButton(String name) {
		JButton b = new JButton(name);
		b.addActionListener(this);
		return b;
	}


	public Arm3ControlPanel(RobotOverlord gui,Arm3 arm) {
		super();

		JPanel p;
		
		robotArm = arm;

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

		CollapsiblePanel fkPanel = new CollapsiblePanel("Forward Kinematics");
		this.add(fkPanel,con1);
		con1.gridy++;
		
		xPos = new JLabel("0.00");
		yPos = new JLabel("0.00");
		zPos = new JLabel("0.00");
		
		// used for fk testing
		labelFK1 = new JLabel("0.00");
		labelFK2 = new JLabel("0.00");
		labelFK3 = new JLabel("0.00");
		// used for ik testing
		labelIK1 = new JLabel("0.00");
		labelIK2 = new JLabel("0.00");
		labelIK3 = new JLabel("0.00");

		
		p = new JPanel(new GridLayout(3,3));
		fkPanel.getContentPane().add(p);
		con1.gridy++;

		p.add(arm5Apos = createButton("A+"));
		p.add(labelFK1);
		p.add(arm5Aneg = createButton("A-"));

		con1.gridy++;
		p.add(arm5Bpos = createButton("B+"));
		p.add(labelFK2);
		p.add(arm5Bneg = createButton("B-"));

		p.add(arm5Cpos = createButton("C+"));
		p.add(labelFK3);
		p.add(arm5Cneg = createButton("C-"));

		CollapsiblePanel ikPanel = new CollapsiblePanel("Inverse Kinematics");
		this.add(ikPanel, con1);
		con1.gridy++;

		p = new JPanel(new GridLayout(3,3));
		ikPanel.getContentPane().add(p);

		p.add(arm5Xpos = createButton("X+"));
		p.add(xPos);
		p.add(arm5Xneg = createButton("X-"));

		p.add(arm5Ypos = createButton("Y+"));
		p.add(yPos);
		p.add(arm5Yneg = createButton("Y-"));

		p.add(arm5Zpos = createButton("Z+"));
		p.add(zPos);
		p.add(arm5Zneg = createButton("Z-"));
		
		about = createButton("About this robot");
		this.add(about, con1);
		con1.gridy++;
	}
	
	protected CollapsiblePanel createSpeedPanel() {
		double speed=robotArm.getSpeed();
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
		robotArm.setSpeed(speed);
		speedNow.setText(Double.toString(robotArm.getSpeed()));
	}
	
	public void stateChanged(ChangeEvent e) {
		Object subject = e.getSource();
		
		if( subject == speedControl ) {
			int i=speedControl.getValue();
			setSpeed(speedOptions[i]);
		}
	}
	
	protected void doAbout() {
		JOptionPane.showMessageDialog(null,"<html><body>"
				+"<h1>Arm3</h1>"
				+"<p>Created by Dan Royer (dan@marginallyclever.com).</p><br>"
				+"<p>A three axis robot arm, modelled after the ABB model IRB 460.</p><br>"
				+"<p><a href='https://www.marginallyclever.com/product/arm3'>Click here for more details</a>.</p>"
				+"</body></html>");
	}
	
	
	// respond to buttons
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();			
		
		if( subject == arm5Apos ) robotArm.moveA(1);
		if( subject == arm5Aneg ) robotArm.moveA(-1);
		if( subject == arm5Bpos ) robotArm.moveB(1);
		if( subject == arm5Bneg ) robotArm.moveB(-1);
		if( subject == arm5Cpos ) robotArm.moveC(1);
		if( subject == arm5Cneg ) robotArm.moveC(-1);
		
		if( subject == arm5Xpos ) robotArm.moveX(1);
		if( subject == arm5Xneg ) robotArm.moveX(-1);
		if( subject == arm5Ypos ) robotArm.moveY(1);
		if( subject == arm5Yneg ) robotArm.moveY(-1);
		if( subject == arm5Zpos ) robotArm.moveZ(1);
		if( subject == arm5Zneg ) robotArm.moveZ(-1);

		if( subject == about ) doAbout();
	}
	
	
	public void setUID(long id) {
		if(uid!=null) {
			uid.setText(robotArm.getDisplayName()+" #"+Long.toString(id));
		}
	}
}
