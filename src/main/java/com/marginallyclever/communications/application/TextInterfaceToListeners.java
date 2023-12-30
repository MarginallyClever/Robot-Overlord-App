package com.marginallyclever.communications.application;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.util.ArrayList;

/**
 * A simple text interface with a send button.  Sends the text to all listeners.
 * @author Dan Royer
 */
public class TextInterfaceToListeners extends JPanel {
	private final JTextField commandLine = new JTextField(60);
	private final  JButton send = new JButton("Send");
		
	public TextInterfaceToListeners() {
		super();
		
		commandLine.addActionListener((e)->sendNow());
		send.addActionListener((e)->sendNow());
		
		//this.setBorder(BorderFactory.createTitledBorder(TextInterfaceToListeners.class.getSimpleName()));
		setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.fill= GridBagConstraints.HORIZONTAL;
		c.weightx=1;
		this.add(commandLine,c);
		
		c.gridx=1;
		c.fill=GridBagConstraints.NONE;
		c.weightx=0;
		this.add(send,c);
	}

	public void sendCommand(String str) {
		notifyListeners(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,str));
	}
	
	public void sendNow() {
		sendCommand(commandLine.getText());
		commandLine.setText("");
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {}
		
		JFrame frame = new JFrame(TextInterfaceToListeners.class.getName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new TextInterfaceToListeners());
		frame.pack();
		frame.setVisible(true);
	}

	public void setCommand(String msg) {
		commandLine.setText(msg);
	}
	
	public String getCommand() {
		return commandLine.getText();
	}
	
	@Override
	public void setEnabled(boolean state) {
		super.setEnabled(state);
		commandLine.setEnabled(state);
		send.setEnabled(state);
	}

	// OBSERVER PATTERN

	private final ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();
	public void addActionListener(ActionListener a) {
		listeners.add(a);
	}
	
	public void removeActionListener(ActionListener a) {
		listeners.remove(a);
	}
	
	private void notifyListeners(ActionEvent e) {
		for( ActionListener a : listeners ) {
			a.actionPerformed(e);
		}
	}
}
