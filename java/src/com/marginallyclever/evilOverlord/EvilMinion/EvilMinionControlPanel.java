package com.marginallyclever.evilOverlord.EvilMinion;

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

import com.marginallyclever.evilOverlord.CollapsiblePanel;

public class EvilMinionControlPanel extends JPanel implements ActionListener, ChangeListener {
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
	
	public JLabel xPos,yPos,zPos;
	public JLabel a1,b1,c1,d1,e1;
	public JLabel a2,b2,c2,d2,e2;
	private JLabel speedNow;
	private JLabel uid;
	private JSlider speedControl;
	
	private EvilMinionRobot robotArm=null;
	
	
	private JButton createButton(String name) {
		JButton b = new JButton(name);
		b.addActionListener(this);
		return b;
	}


	public EvilMinionControlPanel(EvilMinionRobot arm) {
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

		
		p = new JPanel(new GridLayout(5,3));
		fkPanel.getContentPane().add(p);
		con1.gridy++;

		p.add(arm5Apos = createButton("A+"));
		p.add(a1);
		p.add(arm5Aneg = createButton("A-"));

		con1.gridy++;
		p.add(arm5Bpos = createButton("B+"));
		p.add(b1);
		p.add(arm5Bneg = createButton("B-"));

		p.add(arm5Cpos = createButton("C+"));
		p.add(c1);
		p.add(arm5Cneg = createButton("C-"));

		p.add(arm5Dpos = createButton("D+"));
		p.add(d1);
		p.add(arm5Dneg = createButton("D-"));

		p.add(arm5Epos = createButton("E+"));
		p.add(e1);
		p.add(arm5Eneg = createButton("E-"));

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
	}
	
	
	public void setUID(long id) {
		if(uid!=null) {
			uid.setText("Evil Minion #"+Long.toString(id));
		}
	}
}
