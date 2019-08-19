package com.marginallyclever.robotOverlord.dhRobot.dhRobotPlayer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.marginallyclever.robotOverlord.CollapsiblePanel;
import com.marginallyclever.robotOverlord.RobotOverlord;

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
	
	protected JTextField fileToPlay;
	protected JButton chooseFile;
	protected JButton reset;
	protected JCheckBox buttonSingleBlock;
	protected JCheckBox buttonLoop; 
	protected JCheckBox buttonCycleStart; 

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
		//con1.anchor=GridBagConstraints.CENTER;

		//this.add(numLinks = new UserCommandSelectNumber(gui,"# links",robot.links.size()),con1);
		//con1.gridy++;
		//numLinks.addChangeListener(this);
		
		contents.add(new JLabel("File to play"), con1);
		con1.gridy++;
		contents.add(fileToPlay=new JTextField(""), con1);
		con1.gridy++;
		fileToPlay.setEditable(false);
		contents.add(chooseFile=new JButton("..."),con1);
		con1.gridy++;
		chooseFile.addActionListener(this);

		contents.add(reset = new JButton("Reset"),con1);
		con1.gridy++;
		contents.add(buttonSingleBlock = new JCheckBox("Single block"),con1);
		con1.gridy++;
		contents.add(buttonLoop = new JCheckBox("Loop at end"),con1);
		con1.gridy++;
		contents.add(buttonCycleStart = new JCheckBox("Cycle start"),con1);
		con1.gridy++; 
		
		reset.addActionListener(this);
		buttonSingleBlock.addItemListener(this);
		buttonLoop.addItemListener(this);
		buttonCycleStart.addItemListener(this);
		
		buttonSingleBlock.setSelected(player.isSingleBlock());
		buttonLoop.setSelected(player.isLoop());
		buttonCycleStart.setSelected(player.isCycleStart());
		
		contents.add(new JSeparator(JSeparator.VERTICAL), con1);
		con1.gridy++;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source == chooseFile) {
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(ro.getMainFrame());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String t = fc.getSelectedFile().getAbsolutePath();
				fileToPlay.setText(t);
				player.setFileToPlay(t);
			}
		}
		if(source == reset) player.reset();
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
		
	}
}
