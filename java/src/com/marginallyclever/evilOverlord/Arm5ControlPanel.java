package com.marginallyclever.evilOverlord;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Arm5ControlPanel extends JPanel implements ActionListener, ChangeListener {
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 257878994328366520L;

	private JButton arm5Apos;
	private JButton arm5Aneg;
	private JButton arm5Bpos;
	private JButton arm5Bneg;
	private JButton arm5Cpos;
	private JButton arm5Cneg;
	private JButton arm5Dpos;
	private JButton arm5Dneg;
	private JButton arm5Epos;
	private JButton arm5Eneg;
	
	private JButton arm5Xpos;
	private JButton arm5Xneg;
	private JButton arm5Ypos;
	private JButton arm5Yneg;
	private JButton arm5Zpos;
	private JButton arm5Zneg;
	
	private JButton arm5ServoOpen;
	private JButton arm5ServoClose;

	public JLabel xPos,yPos,zPos;
	public JLabel a1,b1,c1,d1,e1;
	public JLabel a2,b2,c2,d2,e2;
	public JLabel servo;
	private JLabel speedNow;
	private JLabel uid;
	private JSlider speedControl;
	
	private Arm5Robot robotArm=null;
	
	
	private JButton createButton(String name) {
		JButton b = new JButton(name);
		b.addActionListener(this);
		return b;
	}


	public Arm5ControlPanel(Arm5Robot arm) {
		super();
		
		robotArm = arm;

		uid = new JLabel("Evil Minion");
		this.add(uid);
		
		JPanel p;
		this.setLayout(new GridLayout(0,1));

		speedNow = new JLabel("1.0");

		p = new JPanel(new GridLayout(1,0));
		this.add(p);
		
		p.add(new JLabel("Speed"));
		speedControl = new JSlider(0,10,4);
		p.add(speedNow);
		this.add(speedControl);
		speedControl.addChangeListener(this);
		speedControl.setMajorTickSpacing(10);
		speedControl.setMinorTickSpacing(1);
		speedControl.setPaintTicks(true);
		
		xPos = new JLabel("0.00");
		yPos = new JLabel("0.00");
		zPos = new JLabel("0.00");
		// used for fk testing
		a1 = new JLabel("0.00");
		b1 = new JLabel("0.00");
		c1 = new JLabel("0.00");
		d1 = new JLabel("0.00");
		e1 = new JLabel("0.00");
		// used for ik testing
		a2 = new JLabel("0.00");
		b2 = new JLabel("0.00");
		c2 = new JLabel("0.00");
		d2 = new JLabel("0.00");
		e2 = new JLabel("0.00");
		// for servo
		servo = new JLabel("0.00");

		this.add(new JLabel("Forward Kinematics"));
		
		p = new JPanel(new GridLayout(1,0));
		this.add(p);
		p.add(arm5Apos = createButton("A+"));
		p.add(a1);
		//p.add(a2);
		p.add(arm5Aneg = createButton("A-"));

		p = new JPanel(new GridLayout(1,0));
		this.add(p);
		p.add(arm5Bpos = createButton("B+"));
		p.add(b1);
		//p.add(b2);
		p.add(arm5Bneg = createButton("B-"));

		p = new JPanel(new GridLayout(1,0));
		this.add(p);
		p.add(arm5Cpos = createButton("C+"));
		p.add(c1);
		//p.add(c2);
		p.add(arm5Cneg = createButton("C-"));

		p = new JPanel(new GridLayout(1,0));
		this.add(p);
		p.add(arm5Dpos = createButton("D+"));
		p.add(d1);
		//p.add(d2);
		p.add(arm5Dneg = createButton("D-"));

		p = new JPanel(new GridLayout(1,0));
		this.add(p);
		p.add(arm5Epos = createButton("E+"));
		p.add(e1);
		//p.add(e2);	
		p.add(arm5Eneg = createButton("E-"));

		this.add(new JLabel("Finger Tip Inverse Kinematics"));

		p = new JPanel(new GridLayout(1,0));
		this.add(p);
		p.add(arm5Xpos = createButton("X+"));
		p.add(xPos);
		p.add(arm5Xneg = createButton("X-"));

		p = new JPanel(new GridLayout(1,0));
		this.add(p);
		p.add(arm5Ypos = createButton("Y+"));
		p.add(yPos);
		p.add(arm5Yneg = createButton("Y-"));

		p = new JPanel(new GridLayout(1,0));
		this.add(p);
		p.add(arm5Zpos = createButton("Z+"));
		p.add(zPos);
		p.add(arm5Zneg = createButton("Z-"));

		this.add(new JLabel("Gripper"));

		p = new JPanel(new GridLayout(1,0));
		this.add(p);
		p.add(arm5ServoOpen = createButton("Open"));
		p.add(servo);
		p.add(arm5ServoClose = createButton("Close"));
	}

	protected void setSpeed(double speed) {
		robotArm.setSpeed(speed);
		speedNow.setText(Double.toString(robotArm.getSpeed()));
	}
	
	public void stateChanged(ChangeEvent e) {
		Object subject = e.getSource();
		if( subject == speedControl ) {
			switch(speedControl.getValue()) {
			case 0:  setSpeed(0.0001);  break;
			case 1:  setSpeed(0.001);  break;
			case 2:  setSpeed(0.01);  break;
			case 3:  setSpeed(0.1);  break;
			case 4:  setSpeed(1.0);  break;
			case 5:  setSpeed(2.0);  break;
			case 6:  setSpeed(5.0);  break;
			case 7:  setSpeed(10.0);  break;
			case 8:  setSpeed(20.0);  break;
			case 9:  setSpeed(50.0);  break;
			case 10:  setSpeed(100.0);  break;
			}
		}
	}
	
	
	// arm5 controls
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();			
		
		if( subject == arm5Apos ) robotArm.moveA(1);
		if( subject == arm5Aneg ) robotArm.moveA(-1);
		if( subject == arm5Bpos ) robotArm.moveB(1);
		if( subject == arm5Bneg ) robotArm.moveB(-1);
		if( subject == arm5Cpos ) robotArm.moveC(1);
		if( subject == arm5Cneg ) robotArm.moveC(-1);
		if( subject == arm5Dpos ) robotArm.moveD(1);
		if( subject == arm5Dneg ) robotArm.moveD(-1);
		if( subject == arm5Epos ) robotArm.moveE(1);
		if( subject == arm5Eneg ) robotArm.moveE(-1);
		
		if( subject == arm5Xpos ) robotArm.moveX(1);
		if( subject == arm5Xneg ) robotArm.moveX(-1);
		if( subject == arm5Ypos ) robotArm.moveY(1);
		if( subject == arm5Yneg ) robotArm.moveY(-1);
		if( subject == arm5Zpos ) robotArm.moveZ(1);
		if( subject == arm5Zneg ) robotArm.moveZ(-1);
		
		if( subject == arm5ServoOpen ) robotArm.moveServo(-1);
		if( subject == arm5ServoClose ) robotArm.moveServo(1);
	}
	
	
	public void setUID(long id) {
		uid.setText("Evil Minion #"+Long.toString(id));
	}
}
