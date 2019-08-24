package com.marginallyclever.robotOverlord.dhRobot.dhRobotPlayer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.robotOverlord.CollapsiblePanel;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.Translator;
import com.marginallyclever.robotOverlord.commands.UserCommandSelectFile;
import com.marginallyclever.robotOverlord.dhRobot.DHRobot;

/**
 * Control Panel for a DHRobot
 * @author Dan Royer
 *
 */
public class DHRobotPlayerPanel extends JPanel implements ActionListener, ChangeListener, ItemListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected DHRobotPlayer player;
	protected RobotOverlord ro;
	
	protected UserCommandSelectFile fileToPlay;
	
	protected JButton reset;
	protected JCheckBox buttonSingleBlock;
	protected JCheckBox buttonLoop; 
	protected JCheckBox buttonCycleStart;
	
	protected JTextField directCommand;
	protected JButton directCommandSend;

	public DHRobotPlayerPanel(RobotOverlord gui,DHRobotPlayer arg0) {
		this.player = arg0;
		this.ro = gui;
		
		buildPanel();
	}
	
	protected void buildPanel() {
		this.removeAll();

		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=1;
		c.weighty=1;
		c.anchor=GridBagConstraints.NORTHWEST;
		c.fill=GridBagConstraints.HORIZONTAL;

		CollapsiblePanel oiwPanel = new CollapsiblePanel("DHRobotPlayer");
		this.add(oiwPanel,c);
		JPanel contents = oiwPanel.getContentPane();
		
		contents.setBorder(new EmptyBorder(0,0,0,0));
		contents.setLayout(new GridBagLayout());
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=1;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		
		contents.add(new JLabel("File to play"), con1);
		con1.gridy++;
		contents.add(fileToPlay=new UserCommandSelectFile(ro,Translator.get("..."),player.getFileToPlay()), con1);
		con1.gridy++;
		fileToPlay.addChoosableFileFilter(new FileNameExtensionFilter("GCode", "ngc"));

		contents.add(reset = new JButton("Reset"),con1);
		con1.gridy++;
		contents.add(buttonSingleBlock = new JCheckBox("Single block"),con1);
		con1.gridy++;
		contents.add(buttonLoop = new JCheckBox("Loop at end"),con1);
		con1.gridy++;
		JButton b = new JButton();
		b.add(buttonCycleStart = new JCheckBox("Cycle start"));
		contents.add(b,con1);
		con1.gridy++; 
		
		reset.addActionListener(this);
		buttonSingleBlock.addItemListener(this);
		buttonLoop.addItemListener(this);
		buttonCycleStart.addItemListener(this);
		fileToPlay.addChangeListener(this);
		
		buttonSingleBlock.setSelected(player.isSingleBlock());
		buttonLoop.setSelected(player.isLoop());
		buttonCycleStart.setSelected(player.isCycleStart());

		contents.add(Box.createVerticalGlue(),con1);
		con1.gridy++;
		
		contents.add(new JLabel("Direct command"),con1);
		con1.gridy++;
		contents.add(directCommand = new JTextField(),con1);
		con1.gridy++;
		contents.add(directCommandSend = new JButton("Send"),con1);
		con1.gridy++;
		directCommandSend.addActionListener(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source == reset) player.reset();
		if(source == directCommandSend) {
			DHRobot t=player.getTarget();
			if(t!=null) t.parseGCode(directCommand.getText());
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// for checkboxes
		Object source = e.getItemSelectable();
		if(source==buttonSingleBlock) player.setSingleBlock	(buttonSingleBlock	.isSelected());
		if(source==buttonLoop		) player.setLoop		(buttonLoop			.isSelected());
		if(source==buttonCycleStart	) player.setCycleStart	(buttonCycleStart	.isSelected());
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Object source = e.getSource();
		if(source==fileToPlay) {
			player.setFileToPlay(fileToPlay.getFilename());
		}
	}
}
