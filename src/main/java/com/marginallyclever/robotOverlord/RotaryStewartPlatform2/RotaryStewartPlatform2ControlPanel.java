package com.marginallyclever.robotOverlord.RotaryStewartPlatform2;

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
import com.marginallyclever.robotOverlord.RobotMoveButton;
import com.marginallyclever.robotOverlord.RobotMoveCommand;
import com.marginallyclever.robotOverlord.RobotOverlord;

public class RotaryStewartPlatform2ControlPanel extends JPanel implements ActionListener, ChangeListener {
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 257878994328366520L;

	private RotaryStewartPlatform2 robot;

	private JLabel uid;
	private JButton goHome;
	
	private final float [] speedOptions = {0.1f, 0.2f, 0.5f, 1, 2, 5, 10, 20, 50};
	private JLabel speedNow;
	private JSlider speedControl;

	private JButton arm5Xpos;
	private JButton arm5Xneg;
	private JButton arm5Ypos;
	private JButton arm5Yneg;
	private JButton arm5Zpos;
	private JButton arm5Zneg;
	public JLabel xPos,yPos,zPos;

	private JButton arm5Upos;
	private JButton arm5Uneg;
	private JButton arm5Vpos;
	private JButton arm5Vneg;
	private JButton arm5Wpos;
	private JButton arm5Wneg;
	public JLabel uPos,vPos,wPos;

	
	public RotaryStewartPlatform2ControlPanel(RobotOverlord gui,RotaryStewartPlatform2 robot) {
		super();

		this.robot = robot;

		JPanel p;
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=1;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.NORTH;
		
		// home button
		goHome = new JButton("Find Home");
		goHome.addActionListener(this);
		this.add(goHome,con1);
		con1.gridy++;


		// speed panel
		CollapsiblePanel speedPanel = createSpeedPanel();
		this.add(speedPanel,con1);
		con1.gridy++;

		
		// ik panel
		CollapsiblePanel ikPanel = new CollapsiblePanel("Inverse Kinematics");
		this.add(ikPanel, con1);
		con1.gridy++;

		p = new JPanel(new GridLayout(7,3));
		ikPanel.getContentPane().add(p);

		xPos = new JLabel("0.00");
		yPos = new JLabel("0.00");
		zPos = new JLabel("0.00");
		uPos = new JLabel("0.00");
		vPos = new JLabel("0.00");
		wPos = new JLabel("0.00");

		p.add(arm5Upos = new RobotMoveButton(gui.getUndoHelper(), robot,RobotMoveCommand.AXIS_U, 1,"U+"));		p.add(uPos);
		p.add(arm5Uneg = new RobotMoveButton(gui.getUndoHelper(), robot,RobotMoveCommand.AXIS_U,-1,"U-"));		
		p.add(arm5Vpos = new RobotMoveButton(gui.getUndoHelper(), robot,RobotMoveCommand.AXIS_V, 1,"V+"));		p.add(vPos);
		p.add(arm5Vneg = new RobotMoveButton(gui.getUndoHelper(), robot,RobotMoveCommand.AXIS_V,-1,"V-"));		
		p.add(arm5Wpos = new RobotMoveButton(gui.getUndoHelper(), robot,RobotMoveCommand.AXIS_W, 1,"W+"));		p.add(wPos);
		p.add(arm5Wneg = new RobotMoveButton(gui.getUndoHelper(), robot,RobotMoveCommand.AXIS_W,-1,"W-"));		
		p.add(arm5Xpos = new RobotMoveButton(gui.getUndoHelper(), robot,RobotMoveCommand.AXIS_X, 1,"X+"));		p.add(xPos);
		p.add(arm5Xneg = new RobotMoveButton(gui.getUndoHelper(), robot,RobotMoveCommand.AXIS_X,-1,"X-"));		
		p.add(arm5Ypos = new RobotMoveButton(gui.getUndoHelper(), robot,RobotMoveCommand.AXIS_Y, 1,"Y+"));		p.add(yPos);
		p.add(arm5Yneg = new RobotMoveButton(gui.getUndoHelper(), robot,RobotMoveCommand.AXIS_Y,-1,"Y-"));		
		p.add(arm5Zpos = new RobotMoveButton(gui.getUndoHelper(), robot,RobotMoveCommand.AXIS_Z, 1,"Z+"));		p.add(zPos);
		p.add(arm5Zneg = new RobotMoveButton(gui.getUndoHelper(), robot,RobotMoveCommand.AXIS_Z,-1,"Z-"));
	}
	
	protected CollapsiblePanel createSpeedPanel() {
		float speed=robot.getSpeed();
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

	protected void setSpeed(float speed) {
		robot.setSpeed(speed);
		speedNow.setText(Float.toString(robot.getSpeed()));
	}
	
	protected float getSpeed() {
		int i=speedControl.getValue();
		return speedOptions[i];
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
	}
	
	
	public void setUID(long id) {
		if(uid!=null) {
			uid.setText("Evil Minion #"+Long.toString(id));
		}
	}
	
	public void update() { 
		// TODO rotate fingerPosition before adding position

		xPos.setText(Float.toString(RotaryStewartPlatform2.roundOff(robot.motionNow.fingerPosition.x)));
		yPos.setText(Float.toString(RotaryStewartPlatform2.roundOff(robot.motionNow.fingerPosition.y)));
		zPos.setText(Float.toString(RotaryStewartPlatform2.roundOff(robot.motionNow.fingerPosition.z)));

		uPos.setText(Float.toString(RotaryStewartPlatform2.roundOff(robot.motionNow.rotationAngleU)));
		vPos.setText(Float.toString(RotaryStewartPlatform2.roundOff(robot.motionNow.rotationAngleV)));
		wPos.setText(Float.toString(RotaryStewartPlatform2.roundOff(robot.motionNow.rotationAngleW)));

		//if( tool != null ) tool.updateGUI();
		
		arm5Upos.setEnabled(robot.isHomed());
		arm5Uneg.setEnabled(robot.isHomed());
		arm5Vpos.setEnabled(robot.isHomed());
		arm5Vneg.setEnabled(robot.isHomed());
		arm5Wpos.setEnabled(robot.isHomed());
		arm5Wneg.setEnabled(robot.isHomed());
		
		arm5Xpos.setEnabled(robot.isHomed());
		arm5Xneg.setEnabled(robot.isHomed());
		arm5Ypos.setEnabled(robot.isHomed());
		arm5Yneg.setEnabled(robot.isHomed());
		arm5Zpos.setEnabled(robot.isHomed());
		arm5Zneg.setEnabled(robot.isHomed());
	}
}
