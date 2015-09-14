package com.marginallyclever.evilOverlord;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

public class Arm5ControlPanel extends JPanel implements ActionListener {
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

	JLabel xPos,yPos,zPos;
	JLabel a1,b1,c1,d1,e1;
	JLabel a2,b2,c2,d2,e2;

	
	private JButton createButton(String name) {
		JButton b = new JButton(name);
		b.addActionListener(this);
		return b;
	}


	public Arm5ControlPanel() {
		JPanel p;
		this.setLayout(new GridLayout(0,1));

		this.add(new JLabel("Forward Kinematics"));
		
		p = new JPanel(new GridLayout(1,0));
		p.add(arm5Apos = createButton("A+"));
		p.add(arm5Aneg = createButton("A-"));
		this.add(p);

		p = new JPanel(new GridLayout(1,0));
		p.add(arm5Bpos = createButton("B+"));
		p.add(arm5Bneg = createButton("B-"));
		this.add(p);

		p = new JPanel(new GridLayout(1,0));
		p.add(arm5Cpos = createButton("C+"));
		p.add(arm5Cneg = createButton("C-"));
		this.add(p);

		p = new JPanel(new GridLayout(1,0));
		p.add(arm5Dpos = createButton("D+"));
		p.add(arm5Dneg = createButton("D-"));
		this.add(p);

		p = new JPanel(new GridLayout(1,0));
		p.add(arm5Epos = createButton("E+"));
		p.add(arm5Eneg = createButton("E-"));
		this.add(p);

		this.add(new JSeparator());
		this.add(new JLabel("Inverse Kinematics"));

		p = new JPanel(new GridLayout(1,0));
		p.add(arm5Xpos = createButton("X+"));
		p.add(arm5Xneg = createButton("X-"));
		this.add(p);

		p = new JPanel(new GridLayout(1,0));
		p.add(arm5Ypos = createButton("Y+"));
		p.add(arm5Yneg = createButton("Y-"));
		this.add(p);

		p = new JPanel(new GridLayout(1,0));
		p.add(arm5Zpos = createButton("Z+"));
		p.add(arm5Zneg = createButton("Z-"));
		this.add(p);
		
		this.add(new JSeparator());

		xPos = new JLabel("0.00");
		yPos = new JLabel("0.00");
		zPos = new JLabel("0.00");
		a1 = new JLabel("0.00");
		b1 = new JLabel("0.00");
		c1 = new JLabel("0.00");
		d1 = new JLabel("0.00");
		e1 = new JLabel("0.00");
		a2 = new JLabel("0.00");
		b2 = new JLabel("0.00");
		c2 = new JLabel("0.00");
		d2 = new JLabel("0.00");
		e2 = new JLabel("0.00");

		p = new JPanel(new GridLayout(1,0));
		this.add(p);
		p.add(new JLabel("X"));
		p.add(xPos);
		p = new JPanel(new GridLayout(1,0));
		this.add(p);
		p.add(new JLabel("Y"));
		p.add(yPos);
		p = new JPanel(new GridLayout(1,0));
		this.add(p);
		p.add(new JLabel("Z"));
		p.add(zPos);
		

		p = new JPanel(new GridLayout(1,0));
		this.add(p);
		p.add(new JLabel("a"));
		p.add(a1);
		p.add(a2);

		p = new JPanel(new GridLayout(1,0));
		this.add(p);
		p.add(new JLabel("b"));
		p.add(b1);
		p.add(b2);

		p = new JPanel(new GridLayout(1,0));
		this.add(p);
		p.add(new JLabel("c"));
		p.add(c1);
		p.add(c2);

		p = new JPanel(new GridLayout(1,0));
		this.add(p);
		p.add(new JLabel("d"));
		p.add(d1);
		p.add(d2);

		p = new JPanel(new GridLayout(1,0));
		this.add(p);
		p.add(new JLabel("e"));
		p.add(e1);
		p.add(e2);		
	}


	
	// arm5 controls
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();		

		World world = MainGUI.getSingleton().world;		
		
		if( subject == arm5Apos ) world.robot0.moveA(1);
		if( subject == arm5Aneg ) world.robot0.moveA(-1);
		if( subject == arm5Bpos ) world.robot0.moveB(1);
		if( subject == arm5Bneg ) world.robot0.moveB(-1);
		if( subject == arm5Cpos ) world.robot0.moveC(1);
		if( subject == arm5Cneg ) world.robot0.moveC(-1);
		if( subject == arm5Dpos ) world.robot0.moveD(1);
		if( subject == arm5Dneg ) world.robot0.moveD(-1);
		if( subject == arm5Epos ) world.robot0.moveE(1);
		if( subject == arm5Eneg ) world.robot0.moveE(-1);
		
		if( subject == arm5Xpos ) world.robot0.moveX(1);
		if( subject == arm5Xneg ) world.robot0.moveX(-1);
		if( subject == arm5Ypos ) world.robot0.moveY(1);
		if( subject == arm5Yneg ) world.robot0.moveY(-1);
		if( subject == arm5Zpos ) world.robot0.moveZ(1);
		if( subject == arm5Zneg ) world.robot0.moveZ(-1);
		
		xPos.setText(Float.toString(world.robot0.motionNow.fingerPosition.x));
		yPos.setText(Float.toString(world.robot0.motionNow.fingerPosition.y));
		zPos.setText(Float.toString(world.robot0.motionNow.fingerPosition.z));

		a1.setText(Float.toString(world.robot0.motionNow.angleA));
		b1.setText(Float.toString(world.robot0.motionNow.angleB));
		c1.setText(Float.toString(world.robot0.motionNow.angleC));
		d1.setText(Float.toString(world.robot0.motionNow.angleD));
		e1.setText(Float.toString(world.robot0.motionNow.angleE));
		
		a2.setText(Float.toString(world.robot0.motionNow.ik_angleA));
		b2.setText(Float.toString(world.robot0.motionNow.ik_angleB));
		c2.setText(Float.toString(world.robot0.motionNow.ik_angleC));
		d2.setText(Float.toString(world.robot0.motionNow.ik_angleD));
		e2.setText(Float.toString(world.robot0.motionNow.ik_angleE));
		
	}
}
