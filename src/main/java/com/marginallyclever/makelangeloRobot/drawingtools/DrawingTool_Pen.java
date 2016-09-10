package com.marginallyclever.makelangeloRobot.drawingtools;


import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.Translator;
import com.marginallyclever.robotOverlord.actions.ActionSelectNumber;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;


public class DrawingTool_Pen extends DrawingTool implements ActionListener {
	protected JDialog dialog;
	protected JPanel panel;
	
	protected ActionSelectNumber penDiameter;
	protected ActionSelectNumber penFeedRate;
	protected ActionSelectNumber penUp;
	protected ActionSelectNumber penDown;
	protected ActionSelectNumber penZRate;

	protected JButton buttonTestUp;
	protected JButton buttonTestDown;
	protected JButton buttonSave;
	protected JButton buttonCancel;

	
	public DrawingTool_Pen(MakelangeloRobot robot) {
		super(robot);

		diameter = 1.5f;
		zRate = 50;
		zOn = 90;
		zOff = 50;
		toolNumber = 0;
		feedRateXY = 3500;
		name = "Pen";
	}

	public DrawingTool_Pen(String name2, int tool_id, MakelangeloRobot robot) {
		super(robot);

		diameter = 1.5f;
		zRate = 120;
		zOn = 90;
		zOff = 50;
		toolNumber = tool_id;
		feedRateXY = 3500;
		name = name2;
	}

	@Override
	public JPanel getPanel(RobotOverlord gui) {
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
	    panel.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));

	    JPanel p = new JPanel(new GridBagLayout());
	    panel.add(p);
	    
		penDiameter = new ActionSelectNumber(gui,Translator.get("penToolDiameter"),getDiameter());
		penFeedRate = new ActionSelectNumber(gui,Translator.get("penToolMaxFeedRate"),feedRateXY);
		penUp = new ActionSelectNumber(gui,Translator.get("penToolUp"),zOff);
		penDown = new ActionSelectNumber(gui,Translator.get("penToolDown"),zOn);
		penZRate = new ActionSelectNumber(gui,Translator.get("penToolLiftSpeed"),zRate);
		buttonTestUp = new JButton(Translator.get("penToolTest"));
		buttonTestDown = new JButton(Translator.get("penToolTest"));

	    Dimension s = buttonTestUp.getPreferredSize();
	    s.width = 80;
	    buttonTestUp.setPreferredSize(s);
	    buttonTestDown.setPreferredSize(s);

		GridBagConstraints c = new GridBagConstraints();
		GridBagConstraints d = new GridBagConstraints();

		c.ipadx=5;
	    c.ipady=0;

		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.HORIZONTAL;
		d.fill = GridBagConstraints.NONE;
		d.anchor = GridBagConstraints.EAST;
		d.weightx = 50;
		int y = 0;
		c.gridx=0;
		c.gridwidth=2;
		d.gridx=1;
		d.gridwidth=1;

		c.gridy = y++;		p.add(penDiameter, c);
		c.gridy = y++;		p.add(penFeedRate, c);
		c.gridy = y++;		p.add(penUp, c);
		d.gridy = y++;		p.add(buttonTestUp, d);
		c.gridy = y++;		p.add(penDown, c);
		d.gridy = y++;		p.add(buttonTestDown, d);
		c.gridy = y++;		p.add(penZRate, c);
		
		buttonTestUp.addActionListener(this);
		buttonTestDown.addActionListener(this);
		
		return panel;
	}
	
	
	public void actionPerformed(ActionEvent event) {
		Object subject = event.getSource();

		if (subject == buttonTestUp  ) robot.testPenAngle(((Number)penUp.getValue()).floatValue());
		if (subject == buttonTestDown) robot.testPenAngle(((Number)penDown.getValue()).floatValue());
	}
	
	
	@Override
	public void save() {
		setDiameter(penDiameter.getValue());
		feedRateXY = penFeedRate.getValue();
		zRate = penZRate.getValue();
		zOff = penUp.getValue();
		zOn = penDown.getValue();
	}
}
