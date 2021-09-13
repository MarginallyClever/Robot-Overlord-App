package com.marginallyclever.robotOverlord.textInterfaces;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import com.marginallyclever.convenience.log.Log;

public class TextInterfaceWithHistory extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5542831703742185676L;
	private TextInterfaceToListeners myInterface = new TextInterfaceToListeners();
	private HistoryList myHistory = new HistoryList();
	
	public TextInterfaceWithHistory() {
		super();
		
		setBorder(new EmptyBorder(2,2,2,2));
		setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridy++;
		c.fill = GridBagConstraints.BOTH;
		c.weightx=1;
		c.weighty=1;
		add(myHistory,c);
		myHistory.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		c.gridy++;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weighty=0;
		add(myInterface,c);
		
		myInterface.addActionListener((e)->addToHistory(e.getActionCommand()));
		myHistory.addListSelectionListener((e)->{
			if(e.getValueIsAdjusting()) return;
			int i = myHistory.getSelectedIndex();
			if(i!=-1) myInterface.setCommand(myHistory.getSelectedValue());
		});
	}

	private void addToHistory(String actionCommand) {
		myHistory.addElement(actionCommand);
	}
	
	public void addActionListener(ActionListener e) {
		myInterface.addActionListener(e);
	}
	
	public void removeActionListener(ActionListener e) {
		myInterface.removeActionListener(e);
	}

	public String getCommand() {
		return myInterface.getCommand();
	}

	public void setCommand(String str) {
		myInterface.setCommand(str);
	}

	public static void main(String[] args) {
		Log.start();
		JFrame frame = new JFrame("TextInterfaceWithHistory");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {}
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(600, 400));
		frame.add(new TextInterfaceWithHistory());
		frame.pack();
		frame.setVisible(true);
	}
	
	@Override
	public void setEnabled(boolean state) {
		super.setEnabled(state);
		myInterface.setEnabled(state);
		myHistory.setEnabled(state);
	}
}
