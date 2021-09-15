package com.marginallyclever.robotOverlord.textInterfaces;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.marginallyclever.communications.NetworkSession;
import com.marginallyclever.communications.NetworkSessionEvent;
import com.marginallyclever.communications.NetworkSessionListener;
import com.marginallyclever.convenience.log.Log;

public class TextInterfaceToNetworkSession extends JPanel implements NetworkSessionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1032123255711692874L;
	private TextInterfaceWithHistory myInterface = new TextInterfaceWithHistory();
	private ChooseConnectionPanel myConnection = new ChooseConnectionPanel();
	private NetworkSession mySession;

	public TextInterfaceToNetworkSession() {
		super();
		
		setLayout(new BorderLayout());
		
		add(myConnection,BorderLayout.NORTH);
		add(myInterface,BorderLayout.CENTER);
		
		myInterface.setEnabled(false);
		myInterface.addActionListener((e)->sendCommandToSession(e));
		myConnection.addActionListener((e)->{
			switch(e.getID()) {
			case ChooseConnectionPanel.CONNECTION_OPENED: setNetworkSession(myConnection.getNetworkSession()); break;
			case ChooseConnectionPanel.CONNECTION_CLOSED: setNetworkSession(null); break;
			}
		});
	}
	
	private void setNetworkSession(NetworkSession session) {
		if(mySession!=null) mySession.removeListener(this);
		mySession = session;
		if(mySession!=null) mySession.addListener(this);
		
		myInterface.setEnabled(mySession!=null);
	}

	private void sendCommandToSession(ActionEvent evt) {
		if(mySession==null) return;
		try {
			String str = evt.getActionCommand();
			if(!str.endsWith("\n")) str+="\n";
			mySession.sendMessage(str);
		} catch(Exception e) {
			JOptionPane.showMessageDialog(this,e.getLocalizedMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public String getCommand() {
		return myInterface.getCommand();
	}

	public void setCommand(String str) {
		myInterface.setCommand(str);
	}
	
	@Override
	public void networkSessionEvent(NetworkSessionEvent evt) {
		if(evt.flag == NetworkSessionEvent.DATA_AVAILABLE) {
			myInterface.addToHistory(mySession.getName(),((String)evt.data).trim());
		}		
	}

	public static void main(String[] args) {
		Log.start();
		JFrame frame = new JFrame("TextInterfaceToNetworkSession");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {}
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(600, 400));
		frame.add(new TextInterfaceToNetworkSession());
		frame.pack();
		frame.setVisible(true);
	}
}
