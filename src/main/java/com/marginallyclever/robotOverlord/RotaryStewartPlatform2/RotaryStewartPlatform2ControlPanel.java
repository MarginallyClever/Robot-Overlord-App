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

public class RotaryStewartPlatform2ControlPanel extends JPanel implements ActionListener, ChangeListener {
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 257878994328366520L;

	private RotaryStewartPlatform2 robot=null;

	private JLabel uid;
	
	private JButton goHome;
	
	private final float [] speedOptions = {0.1f, 0.2f, 0.5f, 
			                                1, 2, 5, 
			                                10, 20, 50};
	private JLabel speedNow;
	private JSlider speedControl;

	private JButton arm5Xpos, arm5Xneg;
	private JButton arm5Ypos, arm5Yneg;
	private JButton arm5Zpos, arm5Zneg;
	public JLabel xPos,yPos,zPos;

	private JButton arm5Upos, arm5Uneg;
	private JButton arm5Vpos, arm5Vneg;
	private JButton arm5Wpos, arm5Wneg;
	public JLabel uPos,vPos,wPos;
	
	private JButton undoButton, redoButton;

	
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
		
		// home button
		goHome = createButton("Find Home");
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

		p.add(arm5Upos = createButton("U+"));		p.add(uPos);		p.add(arm5Uneg = createButton("U-"));
		p.add(arm5Vpos = createButton("V+"));		p.add(vPos);		p.add(arm5Vneg = createButton("V-"));
		p.add(arm5Wpos = createButton("W+"));		p.add(wPos);		p.add(arm5Wneg = createButton("W-"));
		p.add(arm5Xpos = createButton("X+"));		p.add(xPos);		p.add(arm5Xneg = createButton("X-"));
		p.add(arm5Ypos = createButton("Y+"));		p.add(yPos);		p.add(arm5Yneg = createButton("Y-"));
		p.add(arm5Zpos = createButton("Z+"));		p.add(zPos);		p.add(arm5Zneg = createButton("Z-"));
		
		
		// undo/redo panel
		CollapsiblePanel urPanel = new CollapsiblePanel("History");
		this.add(urPanel, con1);
		con1.gridy++;

		p = new JPanel(new GridLayout(1,2));
		urPanel.getContentPane().add(p);
		
		p.add(undoButton = createButton("Undo"));
		p.add(redoButton = createButton("Redo"));
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
		if( subject == arm5Upos ) robot.commandSequence.addEdit(new RotaryStewartPlatform2MoveCommand(robot,RotaryStewartPlatform2.AXIS_U, 1.0f));
		if( subject == arm5Uneg ) robot.commandSequence.addEdit(new RotaryStewartPlatform2MoveCommand(robot,RotaryStewartPlatform2.AXIS_U,-1.0f));
		if( subject == arm5Vpos ) robot.commandSequence.addEdit(new RotaryStewartPlatform2MoveCommand(robot,RotaryStewartPlatform2.AXIS_V, 1.0f));
		if( subject == arm5Vneg ) robot.commandSequence.addEdit(new RotaryStewartPlatform2MoveCommand(robot,RotaryStewartPlatform2.AXIS_V,-1.0f));
		if( subject == arm5Wpos ) robot.commandSequence.addEdit(new RotaryStewartPlatform2MoveCommand(robot,RotaryStewartPlatform2.AXIS_W, 1.0f));
		if( subject == arm5Wneg ) robot.commandSequence.addEdit(new RotaryStewartPlatform2MoveCommand(robot,RotaryStewartPlatform2.AXIS_W,-1.0f));
		
		if( subject == arm5Xpos ) robot.commandSequence.addEdit(new RotaryStewartPlatform2MoveCommand(robot,RotaryStewartPlatform2.AXIS_X, 1.0f));
		if( subject == arm5Xneg ) robot.commandSequence.addEdit(new RotaryStewartPlatform2MoveCommand(robot,RotaryStewartPlatform2.AXIS_X,-1.0f));
		if( subject == arm5Ypos ) robot.commandSequence.addEdit(new RotaryStewartPlatform2MoveCommand(robot,RotaryStewartPlatform2.AXIS_Y, 1.0f));
		if( subject == arm5Yneg ) robot.commandSequence.addEdit(new RotaryStewartPlatform2MoveCommand(robot,RotaryStewartPlatform2.AXIS_Y,-1.0f));
		if( subject == arm5Zpos ) robot.commandSequence.addEdit(new RotaryStewartPlatform2MoveCommand(robot,RotaryStewartPlatform2.AXIS_Z, 1.0f));
		if( subject == arm5Zneg ) robot.commandSequence.addEdit(new RotaryStewartPlatform2MoveCommand(robot,RotaryStewartPlatform2.AXIS_Z,-1.0f));
		
		if( subject == redoButton ) robot.redo();
		if( subject == undoButton ) robot.undo();
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
		
		undoButton.setText(robot.commandSequence.getUndoPresentationName());
	    redoButton.setText(robot.commandSequence.getRedoPresentationName());
	    undoButton.getParent().validate();
	    undoButton.setEnabled(robot.commandSequence.canUndo());
	    redoButton.setEnabled(robot.commandSequence.canRedo());
	}
}
