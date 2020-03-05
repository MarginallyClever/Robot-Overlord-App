package com.marginallyclever.robotOverlord.entity.robot.deltaRobot3;

import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.PanelHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.engine.undoRedo.actions.UndoableActionRobotMove;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandMoveRobot;
import com.marginallyclever.robotOverlord.uiElements.CollapsiblePanel;
import com.marginallyclever.robotOverlord.uiElements.HTMLDialogBox;

@Deprecated
public class DeltaRobot3Panel extends JPanel implements ActionListener, ChangeListener {
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 257878994328366520L;
	
	private DeltaRobot3 robot=null;
	
	private JLabel uid;
	private JButton goHome;

	private final double [] speedOptions = {0.1, 0.2, 0.5, 1, 2, 5, 10, 20, 50};
	private JLabel speedNow;
	private JSlider speedControl;
	
	private UserCommandMoveRobot arm5Apos;
	private UserCommandMoveRobot arm5Aneg;
	private UserCommandMoveRobot arm5Bpos;
	private UserCommandMoveRobot arm5Bneg;
	private UserCommandMoveRobot arm5Cpos;
	private UserCommandMoveRobot arm5Cneg;
	public JLabel angleA,angleB,angleC;
	
	private UserCommandMoveRobot arm5Xpos;
	private UserCommandMoveRobot arm5Xneg;
	private UserCommandMoveRobot arm5Ypos;
	private UserCommandMoveRobot arm5Yneg;
	private UserCommandMoveRobot arm5Zpos;
	private UserCommandMoveRobot arm5Zneg;

	private JButton about;
	
	public JLabel xPos,yPos,zPos;
	
	
	private JButton createButton(String name) {
		JButton b = new JButton(name);
		b.addActionListener(this);
		return b;
	}


	public DeltaRobot3Panel(RobotOverlord gui,DeltaRobot3 deltaRobot) {
		super();
		
		robot = deltaRobot;
		this.setName("Delta Robot");

		GridBagConstraints con1 = PanelHelper.getDefaultGridBagConstraints();
		
		CollapsiblePanel speedPanel = createSpeedPanel();
		this.add(speedPanel,con1);

		con1.gridy++;
		CollapsiblePanel homePanel = new CollapsiblePanel("Calibration");
		homePanel.getContentPane().add(goHome=createButton("Find Home"));
		this.add(homePanel,con1);
		
		//CollapsiblePanel fkPanel = 
				createFKPanel(gui);
		//con1.gridy++;
		//this.add(fkPanel,con1);

		con1.gridy++;
		this.add(createIKPanel(gui), con1);
		
		con1.gridy++;
		about = createButton("About this robot");
		this.add(about, con1);
		
		PanelHelper.ExpandLastChild(this, con1);
	}

	protected CollapsiblePanel createFKPanel(RobotOverlord gui) {
		CollapsiblePanel cp = new CollapsiblePanel("Forward Kinematics");
		
		JPanel p = new JPanel(new GridLayout(3,3));
		cp.getContentPane().add(p);

		p.add(arm5Apos = new UserCommandMoveRobot(gui, robot, UndoableActionRobotMove.AXIS_A, 1, "A+"));		p.add(angleA = new JLabel("0.00"));
		p.add(arm5Aneg = new UserCommandMoveRobot(gui, robot, UndoableActionRobotMove.AXIS_A,-1, "A-"));		
		p.add(arm5Bpos = new UserCommandMoveRobot(gui, robot, UndoableActionRobotMove.AXIS_B, 1, "B+"));		p.add(angleB = new JLabel("0.00"));
		p.add(arm5Bneg = new UserCommandMoveRobot(gui, robot, UndoableActionRobotMove.AXIS_B,-1, "B-"));		
		p.add(arm5Cpos = new UserCommandMoveRobot(gui, robot, UndoableActionRobotMove.AXIS_C, 1, "C+"));		p.add(angleC = new JLabel("0.00"));
		p.add(arm5Cneg = new UserCommandMoveRobot(gui, robot, UndoableActionRobotMove.AXIS_C,-1, "C-"));

		return cp;
	}
	
	protected CollapsiblePanel createIKPanel(RobotOverlord gui) {
		CollapsiblePanel cp = new CollapsiblePanel("Inverse Kinematics");
		
		JPanel p = new JPanel(new GridLayout(3,3));
		cp.getContentPane().add(p);
		
		p.add(arm5Xpos = new UserCommandMoveRobot(gui, robot, UndoableActionRobotMove.AXIS_X, 1, "X+"));		p.add(xPos = new JLabel("0.00"));
		p.add(arm5Xneg = new UserCommandMoveRobot(gui, robot, UndoableActionRobotMove.AXIS_X,-1, "X-"));		
		p.add(arm5Ypos = new UserCommandMoveRobot(gui, robot, UndoableActionRobotMove.AXIS_Y, 1, "Y+"));		p.add(yPos = new JLabel("0.00"));
		p.add(arm5Yneg = new UserCommandMoveRobot(gui, robot, UndoableActionRobotMove.AXIS_Y,-1, "Y-"));		
		p.add(arm5Zpos = new UserCommandMoveRobot(gui, robot, UndoableActionRobotMove.AXIS_Z, 1, "Z+"));		p.add(zPos = new JLabel("0.00"));
		p.add(arm5Zneg = new UserCommandMoveRobot(gui, robot, UndoableActionRobotMove.AXIS_Z,-1, "Z-"));
		
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

		GridBagConstraints con2 = PanelHelper.getDefaultGridBagConstraints();
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
	
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();			
		
		if( subject == goHome ) robot.goHome();
		if( subject == about ) doAbout();
	}
	
	protected void doAbout() {
		HTMLDialogBox box = new HTMLDialogBox();
		box.display(this.getRootPane(), "<html><body>"
				+"<h1>Delta Robot 3</h1>"
				+"<p>Created by Dan Royer (dan@marginallyclever.com).</p><br>"
				+"<p>A three armed, three axis manipulator.  Marginally Clever Robot's first Delta predates the Kossel/Rostock 3D printer.</p><br>"
				+"<p><a href='https://www.marginallyclever.com/product/delta-robot-v3/'>Click here for more details</a>.</p>"
				+"</body></html>", "About "+robot.getName());
	}
	
	public void setUID(long id) {
		if(uid!=null) {
			uid.setText(DeltaRobot3.ROBOT_NAME+" #"+Long.toString(id));
		}
	}
	
	public void update() {
		angleA.setText(Double.toString(MathHelper.roundOff3(robot.motionNow.arms[0].angle)));
		angleB.setText(Double.toString(MathHelper.roundOff3(robot.motionNow.arms[1].angle)));
		angleC.setText(Double.toString(MathHelper.roundOff3(robot.motionNow.arms[2].angle)));
		xPos.setText(Double.toString(MathHelper.roundOff3(robot.motionNow.fingerPosition.x)));
		yPos.setText(Double.toString(MathHelper.roundOff3(robot.motionNow.fingerPosition.y)));
		zPos.setText(Double.toString(MathHelper.roundOff3(robot.motionNow.fingerPosition.z)));
		
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
	}
}
