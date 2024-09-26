package com.marginallyclever.communications.application;

import com.marginallyclever.communications.session.SessionLayer;
import com.marginallyclever.communications.session.SessionLayerEvent;
import com.marginallyclever.communications.session.SessionLayerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * A panel that allows the user to open and close a connection to a {@link SessionLayer}.
 *
 */
public class ChooseConnectionPanel extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(ChooseConnectionPanel.class);

	public static final int CONNECTION_OPENED = 1;
	public static final int CONNECTION_CLOSED = 2;
	private final JButton bConnect = new JButton();
	private final JLabel connectionName = new JLabel("Not connected",JLabel.LEADING);
	private SessionLayer mySession;
	
	public ChooseConnectionPanel() {
		super();

		bConnect.setText("Connect");
		bConnect.setToolTipText("Click to connect/disconnect.");
		bConnect.addActionListener((e)-> onConnectAction() );

		this.setLayout(new FlowLayout(FlowLayout.LEADING));
		this.add(bConnect);
		this.add(connectionName);
	}

	private void onConnectAction() {
		if(mySession!=null) {
			onClose();
		} else {
			SessionLayer s = SessionLayerManager.requestNewSession(this);
			if(s!=null) {
				onOpen(s);
				notifyListeners(new ActionEvent(this,ChooseConnectionPanel.CONNECTION_OPENED,""));
			}
		}
	}

	private void onClose() {
		logger.info("ChooseConnection closed.");
		if(mySession!=null) {
			mySession.closeConnection();
			mySession=null;
			notifyListeners(new ActionEvent(this,ChooseConnectionPanel.CONNECTION_CLOSED,""));
		}
		bConnect.setText("Connect");
		bConnect.setForeground(Color.GREEN);
		connectionName.setText("Not connected");
	}

	private void onOpen(SessionLayer s) {
		logger.info("ChooseConnection open to "+s.getName());

		mySession = s;
		mySession.addListener((e)->{
			if(e.flag == SessionLayerEvent.CONNECTION_CLOSED) {
				onClose(); 
			}
		});
		bConnect.setText("Disconnect");
		bConnect.setForeground(Color.RED);
		connectionName.setText(s.getName());
	}

	public SessionLayer getNetworkSession() {
		return mySession;
	}
	
	public void setNetworkSession(SessionLayer s) {
		if(s!=null && s!=mySession) {
			onClose();
			onOpen(s);
		}
	}

	public void closeConnection() {
		onClose();
	}

	// OBSERVER PATTERN
	
	private final EventListenerList listeners = new EventListenerList();
	
	public void addActionListener(ActionListener a) {
		listeners.add(ActionListener.class,a);
	}
	
	public void removeActionListener(ActionListener a) {
		listeners.remove(ActionListener.class,a);
	}
	
	private void notifyListeners(ActionEvent e) {
		for( var a : listeners.getListeners(ActionListener.class) ) a.actionPerformed(e);
	}
}
