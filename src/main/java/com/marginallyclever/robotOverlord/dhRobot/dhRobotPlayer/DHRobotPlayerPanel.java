package com.marginallyclever.robotOverlord.dhRobot.dhRobotPlayer;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ServiceLoader;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.actions.UndoableActionSetDHTool;
import com.marginallyclever.robotOverlord.commands.UserCommandSelectNumber;

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

	public DHRobotPlayerPanel(RobotOverlord gui,DHRobotPlayer arg0) {
		this.player = arg0;
		this.ro = gui;
		
		buildPanel();
	}
	
	protected void buildPanel() {
		this.removeAll();
		this.setBorder(new EmptyBorder(0,0,0,0));
		this.setLayout(new GridBagLayout());
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
		
		this.add(new JLabel("File to play"), con1);
		con1.gridy++;
		this.add(fileToPlay=new JTextField(""), con1);
		con1.gridy++;
		fileToPlay.setEditable(false);
		this.add(chooseFile=new JButton("Choose"),con1);
		con1.gridy++;
		chooseFile.addActionListener(this);

		this.add(new JSeparator(JSeparator.VERTICAL), con1);
		con1.gridy++;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source == chooseFile) {
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(ro.getMainFrame());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				player.setFileToPlay(fc.getSelectedFile().getAbsolutePath());
			}
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// for checkboxes
		Object source = e.getItemSelectable();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		
	}
}
