package com.marginallyclever.robotOverlord.sixi2Robot;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.Vector3f;

import com.marginallyclever.convenience.SpringUtilities;
import com.marginallyclever.robotOverlord.CollapsiblePanel;
import com.marginallyclever.robotOverlord.HTMLDialogBox;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.commands.UserCommandSelectNumber;

public class Sixi2RobotControlPanel extends JPanel implements ActionListener, ChangeListener {
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 257878994328366520L;

	private final double [] stepSizeOptions = {0.01, 0.05,
											0.1, 0.5, 
			                                1, 5, 
			                                10, 50};

	
	public JSlider fk0,fk1,fk2,fk3,fk4,fk5;

	private JButton ikXpos, ikXneg;
	private JButton ikYpos, ikYneg;
	private JButton ikZpos, ikZneg;
	private JButton ikUpos, ikUneg;
	private JButton ikVpos, ikVneg;
	private JButton ikWpos, ikWneg;
	
	public JLabel xPos,yPos,zPos,uPos,vPos,wPos;
	public JLabel angle5,angle4,angle3,angle2,angle1,angle0;
	private JLabel stepSizeNow;
	private JLabel uid;
	private JSlider stepSizeControl;
	private UserCommandSelectNumber feedRateControl;
	private UserCommandSelectNumber accelerationControl;
	
	private JButton showDebug;
	private JButton about;
	
	private Sixi2Robot robot=null;
	
	
	private JButton createButton(String name) {
		JButton b = new JButton(name);
		b.addActionListener(this);
		return b;
	}

	
	private JSlider createSlider() {
		JSlider b = new JSlider(JSlider.HORIZONTAL,-90,90,0);
		b.setMajorTickSpacing(45);
		b.setMinorTickSpacing(5);
		b.setPaintTicks(true);
		b.addChangeListener(this);
		return b;
	}

	public Sixi2RobotControlPanel(RobotOverlord gui,Sixi2Robot robot) {
		super();

		JPanel p;
		
		this.robot = robot;

		this.setBorder(new EmptyBorder(0,0,0,0));
		this.setLayout(new GridBagLayout());

		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=1;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		//con1.anchor=GridBagConstraints.CENTER;
		
		CollapsiblePanel speedPanel = createSpeedPanel(gui);
		this.add(speedPanel,con1);
		con1.gridy++;

		CollapsiblePanel stepSizePanel = createStepSizePanel();
		this.add(stepSizePanel,con1);
		con1.gridy++;

		// used for fk 
		CollapsiblePanel fkPanel = new CollapsiblePanel("Forward Kinematics");
		this.add(fkPanel,con1);
		con1.gridy++;
		SpringLayout layout;
		layout = new SpringLayout();
		p = new JPanel(layout);
		fkPanel.getContentPane().add(p);
		
		p.add(new JLabel("J0"));		p.add(fk0=createSlider());		p.add(angle0 = createLabel("0.00"));
		p.add(new JLabel("J1"));		p.add(fk1=createSlider());		p.add(angle1 = createLabel("0.00"));
		p.add(new JLabel("J2"));		p.add(fk2=createSlider());		p.add(angle2 = createLabel("0.00"));
		p.add(new JLabel("J3"));		p.add(fk3=createSlider());		p.add(angle3 = createLabel("0.00"));
		p.add(new JLabel("J4"));		p.add(fk4=createSlider());		p.add(angle4 = createLabel("0.00"));
		p.add(new JLabel("J5"));		p.add(fk5=createSlider());		p.add(angle5 = createLabel("0.00"));
				
		SpringUtilities.makeCompactGrid(p,
                6, 3, //rows, cols
                5, 5, //initialX, initialY
                5, 5);//xPad, yPad
		
		// used for ik 
		CollapsiblePanel ikPanel = new CollapsiblePanel("Inverse Kinematics");
		this.add(ikPanel, con1);
		con1.gridy++;
		p = new JPanel(new SpringLayout());
		ikPanel.getContentPane().add(p);
		
		p.add(ikXpos = createButton("X+"));		p.add(xPos = createLabel("0.00"));		p.add(ikXneg = createButton("X-"));
		p.add(ikYpos = createButton("Y+"));		p.add(yPos = createLabel("0.00"));		p.add(ikYneg = createButton("Y-"));
		p.add(ikZpos = createButton("Z+"));		p.add(zPos = createLabel("0.00"));		p.add(ikZneg = createButton("Z-"));
		p.add(ikUpos = createButton("U+"));		p.add(uPos = createLabel("0.00"));		p.add(ikUneg = createButton("U-"));
		p.add(ikVpos = createButton("V+"));		p.add(vPos = createLabel("0.00"));		p.add(ikVneg = createButton("V-"));
		p.add(ikWpos = createButton("W+"));		p.add(wPos = createLabel("0.00"));		p.add(ikWneg = createButton("W-"));

		SpringUtilities.makeCompactGrid(p,
                6, 3, //rows, cols
                5, 5, //initialX, initialY
                5, 5);//xPad, yPad
		
		CollapsiblePanel miscPanel = new CollapsiblePanel("Misc");
		this.add(miscPanel, con1);
		con1.gridy++;
		p = new JPanel(new GridLayout(2,1));
		miscPanel.getContentPane().add(p);
		
		p.add(showDebug = createButton("Toggle debug view"));
		p.add(about = createButton("About this robot"));
		
		updateFKPanel();
		updateIKPanel();
	}
	
	protected JLabel createLabel(String arg0) {
		JLabel newLabel = new JLabel(arg0, SwingConstants.RIGHT);
		Dimension s = newLabel.getPreferredSize();
		s.width = 80;
		newLabel.setMinimumSize(s);
		newLabel.setSize(s);
		
		s = newLabel.getPreferredSize();
		s.width = 110;
		newLabel.setMaximumSize(s);
		
		return newLabel;
	}
	
	protected CollapsiblePanel createSpeedPanel(RobotOverlord gui) {
		CollapsiblePanel speedPanel = new CollapsiblePanel("Limits");
		
		GridBagConstraints con2 = new GridBagConstraints();
		con2.gridx=0;
		con2.gridy=0;
		con2.fill=GridBagConstraints.HORIZONTAL;
		con2.anchor=GridBagConstraints.NORTHWEST;
		con2.weighty=1;
		con2.weightx=0.25;

		feedRateControl = new UserCommandSelectNumber(gui,"Speed",(float)robot.getFeedRate());
		feedRateControl.addChangeListener(this);
		speedPanel.getContentPane().add(feedRateControl,con2);
		con2.gridy++;

		accelerationControl = new UserCommandSelectNumber(gui,"Acceleration",(float)robot.getAcceleration());
		accelerationControl.addChangeListener(this);
		speedPanel.getContentPane().add(accelerationControl,con2);
		con2.gridy++;
		
		return speedPanel;
	}
	
	
	protected CollapsiblePanel createStepSizePanel() {
		CollapsiblePanel stepSizePanel = new CollapsiblePanel("Step size");
		
		GridBagConstraints con2 = new GridBagConstraints();
		con2.gridx=0;
		con2.gridy=0;
		con2.fill=GridBagConstraints.HORIZONTAL;
		con2.anchor=GridBagConstraints.NORTHWEST;
		con2.weighty=1;
		con2.weightx=0.25;
		
		double stepSize = robot.getStepSize();
		int stepSizeIndex;
		for(stepSizeIndex=0;stepSizeIndex<stepSizeOptions.length;++stepSizeIndex) {
			if( stepSizeOptions[stepSizeIndex] >= stepSize )
				break;
		}
		stepSizeNow = new JLabel(Double.toString(stepSizeOptions[stepSizeIndex]),JLabel.CENTER);
		java.awt.Dimension dim = stepSizeNow.getPreferredSize();
		dim.width = 50;
		stepSizeNow.setPreferredSize(dim);
		stepSizePanel.getContentPane().add(stepSizeNow,con2);

		stepSizeControl = new JSlider(0,stepSizeOptions.length-1,stepSizeIndex);
		stepSizeControl.addChangeListener(this);
		stepSizeControl.setMajorTickSpacing(stepSizeOptions.length-1);
		stepSizeControl.setMinorTickSpacing(1);
		stepSizeControl.setPaintTicks(true);
		con2.anchor=GridBagConstraints.NORTHEAST;
		con2.fill=GridBagConstraints.HORIZONTAL;
		con2.weightx=0.75;
		con2.gridx=1;
		stepSizePanel.getContentPane().add(stepSizeControl,con2);
		
		return stepSizePanel;
	}

	protected void setSpeed(double speed) {
		robot.setStepSize(speed);
		stepSizeNow.setText(Double.toString(robot.getStepSize()));
	}
	
	public void stateChanged(ChangeEvent e) {
		Object subject = e.getSource();
		if( subject == stepSizeControl ) {
			int i=stepSizeControl.getValue();
			setSpeed(stepSizeOptions[i]);
		}
		if( subject == feedRateControl ) {
			robot.setFeedRate(feedRateControl.getValue());
		}
		if( subject == accelerationControl ) {
			robot.setAcceleration(accelerationControl.getValue());
		}
		
		{
			if( subject == fk0 ) {  robot.setFKAxis(0,fk0.getValue());  angle0.setText(formatFloat(fk0.getValue()));  }
			if( subject == fk1 ) {  robot.setFKAxis(1,fk1.getValue());  angle1.setText(formatFloat(fk1.getValue()));  }
			if( subject == fk2 ) {  robot.setFKAxis(2,fk2.getValue());  angle2.setText(formatFloat(fk2.getValue()));  }
			if( subject == fk3 ) {  robot.setFKAxis(3,fk3.getValue());  angle3.setText(formatFloat(fk3.getValue()));  }
			if( subject == fk4 ) {  robot.setFKAxis(4,fk4.getValue());  angle4.setText(formatFloat(fk4.getValue()));  }
			if( subject == fk5 ) {  robot.setFKAxis(5,fk5.getValue());  angle5.setText(formatFloat(fk5.getValue()));  }
		}
	}
	
	
	// arm5 controls
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();		
		
		if( subject == ikXpos ) robot.move(Sixi2Robot.Axis.X, 1);
		if( subject == ikXneg ) robot.move(Sixi2Robot.Axis.X,-1);
		if( subject == ikYpos ) robot.move(Sixi2Robot.Axis.Y, 1);
		if( subject == ikYneg ) robot.move(Sixi2Robot.Axis.Y,-1);
		if( subject == ikZpos ) robot.move(Sixi2Robot.Axis.Z, 1);
		if( subject == ikZneg ) robot.move(Sixi2Robot.Axis.Z,-1);
		
		if( subject == ikUpos ) robot.move(Sixi2Robot.Axis.U, 1);
		if( subject == ikUneg ) robot.move(Sixi2Robot.Axis.U,-1);
		if( subject == ikVpos ) robot.move(Sixi2Robot.Axis.V, 1);
		if( subject == ikVneg ) robot.move(Sixi2Robot.Axis.V,-1);
		if( subject == ikWpos ) robot.move(Sixi2Robot.Axis.W, 1);
		if( subject == ikWneg ) robot.move(Sixi2Robot.Axis.W,-1);
		
		if( subject == showDebug ) robot.toggleDebug();
		if( subject == about ) doAbout();
	}
	
	
	protected void doAbout() {
		HTMLDialogBox box = new HTMLDialogBox();
		box.display(this.getRootPane(), "<html><body>"
				+"<h1>SIXI 2 Robot Arm</h1>"
				+"<p>Created by Dan Royer (dan@marginallyclever.com).</p><br>"
				+"<p>A six axis manipulator.</p><br>"
				+"</body></html>", "About "+this.robot.getDisplayName());
	}
	
	
	public void setUID(long id) {
		if(uid!=null) {
			uid.setText("Evil Minion #"+Long.toString(id));
		}
	}
	
	protected void keyframeEditSetEnable(boolean enabled) {
		feedRateControl.setEnabled(enabled);
		accelerationControl.setEnabled(enabled);
		
		fk0.setEnabled(enabled);
		fk1.setEnabled(enabled);
		fk2.setEnabled(enabled);
		fk3.setEnabled(enabled);
		fk4.setEnabled(enabled);
		fk5.setEnabled(enabled);

		ikXpos.setEnabled(enabled);
		ikYpos.setEnabled(enabled);
		ikZpos.setEnabled(enabled);
		ikUpos.setEnabled(enabled);
		ikVpos.setEnabled(enabled);
		ikWpos.setEnabled(enabled);
		
		ikXneg.setEnabled(enabled);
		ikYneg.setEnabled(enabled);
		ikZneg.setEnabled(enabled);
		ikUneg.setEnabled(enabled);
		ikVneg.setEnabled(enabled);
		ikWneg.setEnabled(enabled);

		xPos.setEnabled(enabled);
		yPos.setEnabled(enabled);
		zPos.setEnabled(enabled);
		uPos.setEnabled(enabled);
		vPos.setEnabled(enabled);
		wPos.setEnabled(enabled);
		
		angle5.setEnabled(enabled);
		angle4.setEnabled(enabled);
		angle3.setEnabled(enabled);
		angle2.setEnabled(enabled);
		angle1.setEnabled(enabled);
		angle0.setEnabled(enabled);
		
		stepSizeNow.setEnabled(enabled);
		stepSizeControl.setEnabled(enabled);
		feedRateControl.setEnabled(enabled);
		accelerationControl.setEnabled(enabled);
	}
	
	protected String formatFloat(float arg0) {
		//return Float.toString(roundOff(arg0));
		return String.format("%.3f", arg0);
	}

	public void updateFKPanel() {
		Sixi2RobotKeyframe motionNow = (Sixi2RobotKeyframe)robot.getKeyframeNow();
		
		this.fk0.setValue((int)motionNow.angle0);	this.angle0.setText(formatFloat(motionNow.angle0));
		this.fk1.setValue((int)motionNow.angle1);	this.angle1.setText(formatFloat(motionNow.angle1));
		this.fk2.setValue((int)motionNow.angle2);	this.angle2.setText(formatFloat(motionNow.angle2));
		this.fk3.setValue((int)motionNow.angle3);	this.angle3.setText(formatFloat(motionNow.angle3));
		this.fk4.setValue((int)motionNow.angle4);	this.angle4.setText(formatFloat(motionNow.angle4));
		this.fk5.setValue((int)motionNow.angle5);	this.angle5.setText(formatFloat(motionNow.angle5));
	}
	
	public void updateIKPanel() {
		Sixi2RobotKeyframe motionNow = (Sixi2RobotKeyframe)robot.getKeyframeNow();
		
		Vector3f v = new Vector3f();
		v.set(motionNow.fingerPosition);
		v.add(robot.getPosition());
		this.xPos.setText(formatFloat(v.x));
		this.yPos.setText(formatFloat(v.y));
		this.zPos.setText(formatFloat(v.z));
		this.uPos.setText(formatFloat(motionNow.ikU));
		this.vPos.setText(formatFloat(motionNow.ikV));
		this.wPos.setText(formatFloat(motionNow.ikW));
	}
}
