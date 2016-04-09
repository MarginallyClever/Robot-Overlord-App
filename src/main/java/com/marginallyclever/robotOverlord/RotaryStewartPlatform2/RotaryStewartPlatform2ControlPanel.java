package com.marginallyclever.robotOverlord.RotaryStewartPlatform2;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.marginallyclever.robotOverlord.CollapsiblePanel;

public class RotaryStewartPlatform2ControlPanel extends JPanel implements ActionListener, ChangeListener {
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 257878994328366520L;

	private final double [] speedOptions = {0.1, 0.2, 0.5, 
			                                1, 2, 5, 
			                                10, 20, 50};
	
	private JButton arm5Upos;
	private JButton arm5Uneg;
	private JButton arm5Vpos;
	private JButton arm5Vneg;
	private JButton arm5Wpos;
	private JButton arm5Wneg;
	
	private JButton arm5Xpos;
	private JButton arm5Xneg;
	private JButton arm5Ypos;
	private JButton arm5Yneg;
	private JButton arm5Zpos;
	private JButton arm5Zneg;
	
	private JButton goHome;
	
	public JLabel xPos,yPos,zPos;
	public JLabel a1,b1,c1;
	public JLabel a2,b2,c2;
	private JLabel speedNow;
	private JLabel uid;
	private JSlider speedControl;
	
	private RotaryStewartPlatform2 robot=null;
	
	
	private JButton createButton(String name) {
		JButton b = new JButton(name);
		b.addActionListener(this);
		return b;
	}


	public RotaryStewartPlatform2ControlPanel(RotaryStewartPlatform2 arm) {
		super();

		JPanel p;
		
		robot = arm;

		this.setLayout(new GridBagLayout());
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=1;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.NORTH;
		
		goHome = createButton("Find Home");
		this.add(goHome,con1);
		con1.gridy++;

		CollapsiblePanel speedPanel = createSpeedPanel();
		this.add(speedPanel,con1);
		con1.gridy++;

		xPos = new JLabel("0.00");
		yPos = new JLabel("0.00");
		zPos = new JLabel("0.00");
		// used for fk testing
		a1 = new JLabel("0.00");
		b1 = new JLabel("0.00");
		c1 = new JLabel("0.00");
		// used for ik testing
		a2 = new JLabel("0.00");
		b2 = new JLabel("0.00");
		c2 = new JLabel("0.00");

		CollapsiblePanel ikPanel = new CollapsiblePanel("Inverse Kinematics");
		this.add(ikPanel, con1);
		con1.gridy++;


		p = new JPanel(new GridLayout(7,3));
		ikPanel.getContentPane().add(p);


		p.add(arm5Upos = createButton("U+"));
		p.add(a1);
		p.add(arm5Uneg = createButton("U-"));

		con1.gridy++;
		p.add(arm5Vpos = createButton("V+"));
		p.add(b1);
		p.add(arm5Vneg = createButton("V-"));

		p.add(arm5Wpos = createButton("W+"));
		p.add(c1);
		p.add(arm5Wneg = createButton("W-"));

		p.add(arm5Xpos = createButton("X+"));
		p.add(xPos);
		p.add(arm5Xneg = createButton("X-"));

		p.add(arm5Ypos = createButton("Y+"));
		p.add(yPos);
		p.add(arm5Yneg = createButton("Y-"));

		p.add(arm5Zpos = createButton("Z+"));
		p.add(zPos);
		p.add(arm5Zneg = createButton("Z-"));
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
		if( subject == arm5Upos ) robot.moveU(1);
		if( subject == arm5Uneg ) robot.moveU(-1);
		if( subject == arm5Vpos ) robot.moveV(1);
		if( subject == arm5Vneg ) robot.moveV(-1);
		if( subject == arm5Wpos ) robot.moveW(1);
		if( subject == arm5Wneg ) robot.moveW(-1);
		
		if( subject == arm5Xpos ) robot.moveX(1);
		if( subject == arm5Xneg ) robot.moveX(-1);
		if( subject == arm5Ypos ) robot.moveY(1);
		if( subject == arm5Yneg ) robot.moveY(-1);
		if( subject == arm5Zpos ) robot.moveZ(1);
		if( subject == arm5Zneg ) robot.moveZ(-1);
	}
	
	
	public void setUID(long id) {
		if(uid!=null) {
			uid.setText("Evil Minion #"+Long.toString(id));
		}
	}
	
	/*
	public void propertyChange(PropertyChangeEvent e) {
		Object subject = e.getSource();

		try {
			if(subject == viewPx ) {
				float f = Float.parseFloat(viewPx.getField().getText());
				if(!Float.isNaN(f)) {
					this.motion_future.finger_tip.x = f;
					moveIfAble();
				}
			}
			if(subject == viewPy ) {
				float f = Float.parseFloat(viewPy.getField().getText());
				if(!Float.isNaN(f)) {
					this.motion_future.finger_tip.y = f;
					moveIfAble();
				}
			}
			if(subject == viewPz ) {
				float f = Float.parseFloat(viewPz.getField().getText());
				if(!Float.isNaN(f)) {
					this.motion_future.finger_tip.z = f;
					moveIfAble();
				}
			}
			
			if(subject == viewRx ) {
				float f = Float.parseFloat(viewRx.getField().getText());
				if(!Float.isNaN(f)) {
					this.motion_future.iku = f;
					moveIfAble();
				}
			}
			if(subject == viewRy ) {
				float f = Float.parseFloat(viewRy.getField().getText());
				if(!Float.isNaN(f)) {
					this.motion_future.ikv = f;
					moveIfAble();
				}
			}
			if(subject == viewRz ) {
				float f = Float.parseFloat(viewRz.getField().getText());
				if(!Float.isNaN(f)) {
					this.motion_future.ikw = f;
					moveIfAble();
				}
			}		
		} catch(NumberFormatException e2) {}
	}*/
}
