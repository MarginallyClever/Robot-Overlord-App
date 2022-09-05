package com.marginallyclever.robotoverlord.robots.robotarm.robotArmInterface.marlinInterface;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.marginallyclever.communications.NetworkSession;
import com.marginallyclever.communications.NetworkSessionEvent;
import com.marginallyclever.communications.NetworkSessionManager;
import com.marginallyclever.convenience.log.Log;

public class ChooseConnectionPanel extends JPanel {
	private static final long serialVersionUID = 4773092967249064165L;
	public static final int CONNECTION_OPENED = 1;
	public static final int CONNECTION_CLOSED = 2;
	
	private JButton bConnect = new JButton();
	private JLabel connectionName = new JLabel("Not connected",JLabel.LEADING);
	private NetworkSession mySession;
	
	public ChooseConnectionPanel() {
		super();

		bConnect.setText("Connect");
		bConnect.addActionListener((e)-> onConnectAction() );
		
		//this.setBorder(BorderFactory.createTitledBorder(ChooseConnectionPanel.class.getName()));
		this.setLayout(new FlowLayout(FlowLayout.LEADING));
		this.add(bConnect);
		this.add(connectionName);
	}

	private void onConnectAction() {
		if(mySession!=null) {
			onClose();
		} else {
			NetworkSession s = NetworkSessionManager.requestNewSession(this);
			if(s!=null) {
				onOpen(s);
				notifyListeners(new ActionEvent(this,ChooseConnectionPanel.CONNECTION_OPENED,""));
			}
		}
	}

	private void onClose() {
		Log.message("ChooseConnection closed.");
		if(mySession!=null) {
			mySession.closeConnection();
			mySession=null;
			notifyListeners(new ActionEvent(this,ChooseConnectionPanel.CONNECTION_CLOSED,""));
		}
		bConnect.setText("Connect");
		bConnect.setForeground(Color.GREEN);
		connectionName.setText("Not connected");
	}

	private void onOpen(NetworkSession s) {
		Log.message("ChooseConnection open to "+s.getName());

		mySession = s;
		mySession.addListener((e)->{
			if(e.flag == NetworkSessionEvent.CONNECTION_CLOSED) {
				onClose(); 
			}
		});
		bConnect.setText("Disconnect");
		bConnect.setForeground(Color.RED);
		connectionName.setText(s.getName());
	}

	public NetworkSession getNetworkSession() {
		return mySession;
	}
	
	public void setNetworkSession(NetworkSession s) {
		if(s!=null && s!=mySession) {
			onClose();
			onOpen(s);
		}
	}

	public void closeConnection() {
		onClose();
	}

	// OBSERVER PATTERN
	
	private ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();
	
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

	// TEST 
	
	public static void main(String[] args) {
		Log.start();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {}
		JFrame frame = new JFrame(ChooseConnectionPanel.class.getName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new ChooseConnectionPanel());
		frame.pack();
		frame.setVisible(true);
	}
}
