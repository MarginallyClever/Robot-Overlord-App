package com.marginallyclever.robotOverlord.entity.robotEntity.olderModels.rotaryStewartPlatform;

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
import com.marginallyclever.robotOverlord.swingInterface.CollapsiblePanel;
import com.marginallyclever.robotOverlord.swingInterface.HTMLDialogBox;
import com.marginallyclever.robotOverlord.swingInterface.actions.ActionPhysicalEntityMove;
import com.marginallyclever.robotOverlord.swingInterface.commands.CommandMoveRobot;

@Deprecated
public class RotaryStewartPlatformPanel extends JPanel implements ActionListener, ChangeListener {
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 257878994328366520L;

	private RotaryStewartPlatform robot;

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
	private JButton about;

	
	public RotaryStewartPlatformPanel(RobotOverlord gui,RotaryStewartPlatform robot) {
		super();

		this.robot = robot;
		this.setName("RSP");
		
		GridBagConstraints con1 = PanelHelper.getDefaultGridBagConstraints();

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

		JPanel p = new JPanel(new GridLayout(7,3));
		ikPanel.getContentPane().add(p);

		xPos = new JLabel("0.00");
		yPos = new JLabel("0.00");
		zPos = new JLabel("0.00");
		uPos = new JLabel("0.00");
		vPos = new JLabel("0.00");
		wPos = new JLabel("0.00");

		p.add(arm5Upos = new CommandMoveRobot(gui, robot,ActionPhysicalEntityMove.AXIS_U, 1,"U+"));		p.add(uPos);
		p.add(arm5Uneg = new CommandMoveRobot(gui, robot,ActionPhysicalEntityMove.AXIS_U,-1,"U-"));		
		p.add(arm5Vpos = new CommandMoveRobot(gui, robot,ActionPhysicalEntityMove.AXIS_V, 1,"V+"));		p.add(vPos);
		p.add(arm5Vneg = new CommandMoveRobot(gui, robot,ActionPhysicalEntityMove.AXIS_V,-1,"V-"));		
		p.add(arm5Wpos = new CommandMoveRobot(gui, robot,ActionPhysicalEntityMove.AXIS_W, 1,"W+"));		p.add(wPos);
		p.add(arm5Wneg = new CommandMoveRobot(gui, robot,ActionPhysicalEntityMove.AXIS_W,-1,"W-"));		
		p.add(arm5Xpos = new CommandMoveRobot(gui, robot,ActionPhysicalEntityMove.AXIS_X, 1,"X+"));		p.add(xPos);
		p.add(arm5Xneg = new CommandMoveRobot(gui, robot,ActionPhysicalEntityMove.AXIS_X,-1,"X-"));		
		p.add(arm5Ypos = new CommandMoveRobot(gui, robot,ActionPhysicalEntityMove.AXIS_Y, 1,"Y+"));		p.add(yPos);
		p.add(arm5Yneg = new CommandMoveRobot(gui, robot,ActionPhysicalEntityMove.AXIS_Y,-1,"Y-"));		
		p.add(arm5Zpos = new CommandMoveRobot(gui, robot,ActionPhysicalEntityMove.AXIS_Z, 1,"Z+"));		p.add(zPos);
		p.add(arm5Zneg = new CommandMoveRobot(gui, robot,ActionPhysicalEntityMove.AXIS_Z,-1,"Z-"));
		
		about = new JButton("About this robot");
		about.addActionListener(this);
		this.add(about, con1);
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

	protected void setSpeed(float speed) {
		robot.setSpeed(speed);
		speedNow.setText(Double.toString(robot.getSpeed()));
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
		
		if( subject == goHome ) robot.goHome();
		if( subject == about ) doAbout();
	}
	
	protected void doAbout() {
		HTMLDialogBox box = new HTMLDialogBox();
		box.display(this.getRootPane(), "<html><body>"
				+"<h1>Rotary Stewart Platform 2</h1>"
				+"<p>Created by Dan Royer (dan@marginallyclever.com).</p><br>"
				+"<p>A six axis manipulator.</p><br>"
				+"<p><a href='https://www.marginallyclever.com/product/rotary-stewart-platform-v2/'>Click here for more details</a>.</p>"
				+"</body></html>", "About "+robot.getName());
	}
	
	
	public void setUID(long id) {
		if(uid!=null) {
			uid.setText("Rotary Stewart Platform #"+Long.toString(id));
		}
	}
	
	public void update() { 
		// TODO rotate fingerPosition before adding position

		xPos.setText(Double.toString(MathHelper.roundOff3(robot.motionNow.fingerPosition.x)));
		yPos.setText(Double.toString(MathHelper.roundOff3(robot.motionNow.fingerPosition.y)));
		zPos.setText(Double.toString(MathHelper.roundOff3(robot.motionNow.fingerPosition.z)));
		uPos.setText(Double.toString(MathHelper.roundOff3(robot.motionNow.rotationAngleU)));
		vPos.setText(Double.toString(MathHelper.roundOff3(robot.motionNow.rotationAngleV)));
		wPos.setText(Double.toString(MathHelper.roundOff3(robot.motionNow.rotationAngleW)));

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
