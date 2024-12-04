package com.marginallyclever.ro3.node.nodes.networksession;

import com.marginallyclever.communications.session.SessionLayer;
import com.marginallyclever.communications.session.SessionLayerManager;

import javax.swing.*;
import java.awt.*;

/**
 * A panel that allows the user to open and close a connection in a {@link NetworkSession}.
 */
public class NetworkSessionPanel extends JPanel {
    public static final int CONNECTION_OPENED = 1;
    public static final int CONNECTION_CLOSED = 2;
    private final JButton bConnect = new JButton();
    private final JLabel connectionName = new JLabel("Not connected",JLabel.LEADING);
    private final NetworkSession networkSession;

    public NetworkSessionPanel() {
        this(new NetworkSession());
    }

    public NetworkSessionPanel(NetworkSession networkSession) {
        super();
        this.networkSession = networkSession;

        bConnect.addActionListener( (e) -> onToggleConnect() );

        this.setLayout(new FlowLayout(FlowLayout.LEADING));
        this.add(bConnect);
        this.add(connectionName);

        setConnected(networkSession.isConnected());
    }

    private void onToggleConnect() {
        if(networkSession.getSession() != null) {
            // we were open, so close
            onClose();
        } else {
            // we were closed, so try to open.
            SessionLayer s = SessionLayerManager.requestNewSession(this);
            if(s!=null) onOpen(s);
        }
    }

    private void onClose() {
        if(networkSession !=null) {
            networkSession.closeConnection();
        }
        setConnected(false);
    }

    private void onOpen(SessionLayer s) {
        networkSession.openConnection(s);
        s.addListener((e)->{
            if(e.flag == NetworkSession.CONNECTION_CLOSED) {
                // we care that if the connection closes, we have to update the UX.
                onClose();
            }
        });
        setConnected(true);
    }

    private void setConnected(boolean state) {
        if(state) {
            bConnect.setText("Disconnect");
            bConnect.setToolTipText("Click to disconnect.");
            bConnect.setForeground(Color.RED);
        } else {
            bConnect.setText("Connect");
            bConnect.setToolTipText("Click to connect.");
            bConnect.setForeground(Color.GREEN);
        }

        if(networkSession.isConnected()) {
            connectionName.setText(networkSession.getSession().getName());
        } else {
            connectionName.setText("Not connected");
        }
    }
}
