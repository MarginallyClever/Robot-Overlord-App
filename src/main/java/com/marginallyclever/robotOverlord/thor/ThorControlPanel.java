package com.marginallyclever.robotOverlord.thor;

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
import com.marginallyclever.robotOverlord.HTMLDialogBox;
import com.marginallyclever.robotOverlord.RobotOverlord;

public class ThorControlPanel extends JPanel implements ActionListener, ChangeListener {
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
	private JButton arm5Fpos;
	private JButton arm5Fneg;
	
	private JButton arm5Xpos;
	private JButton arm5Xneg;
	private JButton arm5Ypos;
	private JButton arm5Yneg;
	private JButton arm5Zpos;
	private JButton arm5Zneg;
	
	private JButton arm5Upos;
	private JButton arm5Uneg;
	private JButton arm5Vpos;
	private JButton arm5Vneg;
	private JButton arm5Wpos;
	private JButton arm5Wneg;
	
	public JLabel xPos,yPos,zPos,uPos,vPos,wPos;
	public JLabel a1,b1,c1,d1,e1,f1;
	private JLabel speedNow;
	private JLabel uid;
	private JSlider speedControl;
	
	private JButton about;
	
	private ThorRobot robotArm=null;
	
	
	private JButton createButton(String name) {
		JButton b = new JButton(name);
		b.addActionListener(this);
		return b;
	}


	public ThorControlPanel(RobotOverlord gui,ThorRobot arm) {
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
		
		// used for fk
		a1 = new JLabel("0.00");
		b1 = new JLabel("0.00");
		c1 = new JLabel("0.00");
		d1 = new JLabel("0.00");
		e1 = new JLabel("0.00");
		f1 = new JLabel("0.00");
		
		p = new JPanel(new GridLayout(6,3));
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

		p.add(arm5Fpos = createButton("F+"));
		p.add(f1);
		p.add(arm5Fneg = createButton("F-"));

		CollapsiblePanel ikPanel = new CollapsiblePanel("Inverse Kinematics");
		this.add(ikPanel, con1);
		con1.gridy++;

		// used for ik 
		xPos = new JLabel("0.00");
		yPos = new JLabel("0.00");
		zPos = new JLabel("0.00");
		uPos = new JLabel("0.00");
		vPos = new JLabel("0.00");
		wPos = new JLabel("0.00");
		
		p = new JPanel(new GridLayout(6,3));
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

		p.add(arm5Upos = createButton("U+"));
		p.add(uPos);
		p.add(arm5Uneg = createButton("U-"));

		p.add(arm5Vpos = createButton("V+"));
		p.add(vPos);
		p.add(arm5Vneg = createButton("V-"));

		p.add(arm5Wpos = createButton("W+"));
		p.add(wPos);
		p.add(arm5Wneg = createButton("W-"));
		
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
		if( subject == arm5Fpos ) robotArm.moveF(1);
		if( subject == arm5Fneg ) robotArm.moveF(-1);
		
		if( subject == arm5Xpos ) robotArm.moveX(1);
		if( subject == arm5Xneg ) robotArm.moveX(-1);
		if( subject == arm5Ypos ) robotArm.moveY(1);
		if( subject == arm5Yneg ) robotArm.moveY(-1);
		if( subject == arm5Zpos ) robotArm.moveZ(1);
		if( subject == arm5Zneg ) robotArm.moveZ(-1);
		
		if( subject == arm5Upos ) robotArm.moveU(1);
		if( subject == arm5Uneg ) robotArm.moveU(-1);
		if( subject == arm5Vpos ) robotArm.moveV(1);
		if( subject == arm5Vneg ) robotArm.moveV(-1);
		if( subject == arm5Wpos ) robotArm.moveW(1);
		if( subject == arm5Wneg ) robotArm.moveW(-1);
		
		if( subject == about ) doAbout();
	}
	
	protected void doAbout() {
		HTMLDialogBox box = new HTMLDialogBox();
		box.display(this.getRootPane(), "<html><body>"
				+"<h1>Evil Minion</h1>"
				+"<p>Created by Dan Royer (dan@marginallyclever.com).</p><br>"
				+"<p>A five axis manipulator.  Marginally Clever Robot's third prototype robot arm.</p><br>"
				+"<p><a href='https://www.marginallyclever.com/product/evil-minion-5-axis-arm/'>Click here for more details</a>.</p>"
				+"</body></html>", "About "+this.robotArm.getDisplayName());
	}
	
	
	public void setUID(long id) {
		if(uid!=null) {
			uid.setText("Evil Minion #"+Long.toString(id));
		}
	}
}
