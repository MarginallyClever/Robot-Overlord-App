package com.marginallyclever.communications.application;

import com.marginallyclever.communications.session.SessionLayer;
import com.marginallyclever.communications.session.SessionLayerEvent;
import com.marginallyclever.communications.session.SessionLayerListener;
import com.marginallyclever.ro3.apps.App;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * A connection selection dialog and a {@link TextInterfaceWithHistory} glued to a {@link SessionLayer}.
 */
public class TextInterfaceToSessionLayer extends App implements SessionLayerListener {
	private final ChooseConnectionPanel myConnectionChoice = new ChooseConnectionPanel();
	private final TextInterfaceWithHistory myInterface = new TextInterfaceWithHistory();
	private SessionLayer mySession;

	public TextInterfaceToSessionLayer() {
		super(new BorderLayout());

		add(myConnectionChoice,BorderLayout.NORTH);
		add(myInterface,BorderLayout.CENTER);
		
		myInterface.setEnabled(false);
		myInterface.addActionListener( (evt) -> {
			if(mySession==null) return;
			
			String str = evt.getActionCommand();
			if(!str.endsWith("\n")) str+="\n";
			
			try {
				mySession.sendMessage(str);
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(this,e1.getLocalizedMessage(),"Error",JOptionPane.ERROR_MESSAGE);
			}
		});
		myConnectionChoice.addActionListener((e)->{
			switch(e.getID()) {
			case ChooseConnectionPanel.CONNECTION_OPENED: 
				setNetworkSession(myConnectionChoice.getNetworkSession());
				break;
			case ChooseConnectionPanel.CONNECTION_CLOSED:
				setNetworkSession(null);
				break;
			}
			
			notifyListeners(e);
		});
	}
	
	public void setNetworkSession(SessionLayer session) {
		if(mySession!=null) mySession.removeListener(this);
		mySession = session;
		if(mySession!=null) mySession.addListener(this);
		
		myConnectionChoice.setNetworkSession(session);
		myInterface.setEnabled(mySession!=null);
	}

	public void sendCommand(String str) {
		myInterface.sendCommand(str);
	}
	
	public String getCommand() {
		return myInterface.getCommand();
	}

	public void setCommand(String str) {
		myInterface.setCommand(str);
	}
	
	@Override
	public void networkSessionEvent(SessionLayerEvent evt) {
		if(evt.flag == SessionLayerEvent.DATA_AVAILABLE) {
			myInterface.addToHistory(mySession.getName(),((String)evt.data).trim());
		}
	}

	public boolean getIsConnected() { 
		return (mySession!=null && mySession.isOpen());
	}

	public void closeConnection() {
		myConnectionChoice.closeConnection();
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

	public void addNetworkSessionListener(SessionLayerListener a) {
		mySession.addListener(a);
	}
	
	public void removeNetworkSessionListener(SessionLayerListener a) {
		mySession.removeListener(a);
	}

	// TEST 
	
	public static void main(String[] args) {
		JFrame frame = new JFrame(TextInterfaceToSessionLayer.class.getName());
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception ignore) {}
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(600, 400));
		frame.add(new TextInterfaceToSessionLayer());
		frame.pack();
		frame.setVisible(true);
	}
}
