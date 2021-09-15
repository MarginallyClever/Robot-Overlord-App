package com.marginallyclever.robotOverlord.textInterfaces;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.marginallyclever.communications.NetworkSession;
import com.marginallyclever.communications.NetworkSessionManager;

public class ChooseConnectionPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4773092967249064165L;
	public static final int CONNECTION_CLOSED = 0;
	public static final int CONNECTION_OPENED = 1;
	
	private JButton bConnect = new JButton();
	private JLabel connectionName = new JLabel("Not connected",JLabel.LEADING);
	private ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();
	private NetworkSession mySession;
	
	public ChooseConnectionPanel() {
		super();

		final Component parent = this;
		
		AbstractAction connectAction = new AbstractAction("Connect") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if(mySession!=null) {
					mySession.closeConnection();
					mySession=null;
					bConnect.setText("Connect");
					connectionName.setText("Not connected");
					notifyListeners(new ActionEvent(this,ChooseConnectionPanel.CONNECTION_CLOSED,""));
				} else {
					NetworkSession s = NetworkSessionManager.requestNewSession(parent);
					if(s!=null) {
						mySession = s;
						bConnect.setText("Disconnect");
						notifyListeners(new ActionEvent(this,ChooseConnectionPanel.CONNECTION_OPENED,""));
						connectionName.setText(s.getName());
					}
				}
			}
		};
		bConnect.setAction(connectAction);
		
		setLayout(new FlowLayout(FlowLayout.LEADING));
		add(bConnect);
		add(connectionName);
	}

	public NetworkSession getNetworkSession() {
		return mySession;
	}
	
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
