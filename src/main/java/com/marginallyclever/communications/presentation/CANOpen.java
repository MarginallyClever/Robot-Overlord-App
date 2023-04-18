package com.marginallyclever.communications.presentation;

import com.marginallyclever.communications.session.SessionLayer;
import com.marginallyclever.communications.session.SessionLayerEvent;
import com.marginallyclever.communications.session.SessionLayerListener;
import javax.swing.*;

public class CANOpen implements SessionLayerListener {
    private final SessionLayer sessionLayer;

    public CANOpen(SessionLayer sessionLayer) {
        this.sessionLayer = sessionLayer;
        this.sessionLayer.addListener(this);
    }

    public void connect(String connectionName) throws Exception {
        sessionLayer.openConnection(connectionName);
    }

    public void disconnect() {
        sessionLayer.closeConnection();
    }

    public void reconnect() throws Exception {
        sessionLayer.reconnect();
    }

    public boolean isOpen() {
        return sessionLayer.isOpen();
    }

    public void sendMessage(String message) throws Exception {
        sessionLayer.sendMessage(message);
    }

    // Implement the CANOpen interface methods here
    // ...

    @Override
    public void networkSessionEvent(SessionLayerEvent evt) {
        // Handle the network session event here, e.g., parse received messages
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Implement your event handling logic here, based on the API documentation
            }
        });
    }
}
