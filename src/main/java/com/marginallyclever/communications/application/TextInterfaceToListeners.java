package com.marginallyclever.communications.application;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A simple text interface with a send button.  Sends the text to all listeners.
 */
public class TextInterfaceToListeners extends JPanel implements KeyListener {
	public static final int MAX_HISTORY_LENGTH = 100;
	private final JTextField commandLine = new JTextField(60);
	private final JButton send = new JButton("Send");
	private final List<String> history = new ArrayList<>();
	private int historyIndex = 0;
	private final EventListenerList listeners = new EventListenerList();
		
	public TextInterfaceToListeners() {
		super(new GridBagLayout());

		commandLine.addKeyListener(this);
		commandLine.addActionListener(this::sendNow);
		send.addActionListener(this::sendNow);

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

	public void sendCommand(String command) {
		if(command.isBlank()) return;  // no blank lines!
		history.add(command);
		while(history.size()>MAX_HISTORY_LENGTH) history.remove(0);  // limit size to 100 entries.
		historyIndex = history.size();
		notifyListeners(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,command));
	}
	
	private void sendNow(ActionEvent e) {
		sendCommand(commandLine.getText());
		commandLine.setText("");
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

	public void addActionListener(ActionListener a) {
		listeners.add(ActionListener.class,a);
	}
	
	public void removeActionListener(ActionListener a) {
		listeners.remove(ActionListener.class,a);
	}
	
	private void notifyListeners(ActionEvent e) {
		for( ActionListener a : listeners.getListeners(ActionListener.class) ) {
			a.actionPerformed(e);
		}
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception ignored) {}

		JFrame frame = new JFrame(TextInterfaceToListeners.class.getName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new TextInterfaceToListeners());
		frame.pack();
		frame.setVisible(true);
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {
		if( e.getKeyCode()==KeyEvent.VK_UP ) {
			// go one back in history
			historyIndex = Math.max(0,historyIndex-1);
			setCommand(history.get(historyIndex));
		} else if( e.getKeyCode()==KeyEvent.VK_DOWN ) {
			// go one forward in history
			historyIndex = Math.min(history.size(),historyIndex+1);
			if(historyIndex < history.size()) {
				setCommand(history.get(historyIndex));
			} else {
				setCommand("");
			}
		}
	}
}
