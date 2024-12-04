package com.marginallyclever.ro3.node.nodes.networksession;

import com.marginallyclever.communications.session.SessionLayer;
import com.marginallyclever.communications.session.SessionLayerEvent;
import com.marginallyclever.ro3.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Objects;

/**
 * A Node that represents a network session.  Can be accessed by other nodes to send and receive data.
 */
public class NetworkSession extends Node {
    private static final Logger logger = LoggerFactory.getLogger(NetworkSession.class);
    public static final int CONNECTION_OPENED = 1;
    public static final int CONNECTION_CLOSED = 2;

    private SessionLayer mySession;

    public NetworkSession() {
        this("Network Session");
    }

    public NetworkSession(String name) {
        super(name);
    }

    @Override
    protected void onDetach() {
        closeConnection();
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new NetworkSessionPanel(this));
        super.getComponents(list);
    }

    public SessionLayer getSession() {
        return mySession;
    }

    public void setSession(SessionLayer session) {
        mySession = session;
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/node/nodes/networksession/icons8-call-16.png")));
    }

    public void openConnection(SessionLayer session) {
        if(mySession!=null) {
            logger.error("Error: connection already open. ");
            return;
        }

        logger.debug("openConnection");
        mySession = session;
        mySession.addListener(this::recv);
        notifyListeners(new ActionEvent(this, NetworkSession.CONNECTION_OPENED,""));
    }

    public void closeConnection() {
        if(mySession==null) return;  // already closed

        logger.debug("closeConnection");
        mySession.removeListener(this::recv);
        mySession.closeConnection();
        mySession=null;
        notifyListeners(new ActionEvent(this,NetworkSession.CONNECTION_CLOSED,""));
    }

    // OBSERVER PATTERN

    public void addActionListener(ActionListener a) {
        listeners.add(ActionListener.class,a);
    }

    public void removeActionListener(ActionListener a) {
        listeners.remove(ActionListener.class,a);
    }

    private void notifyListeners(ActionEvent e) {
        for( var a : listeners.getListeners(ActionListener.class) ) a.actionPerformed(e);
    }

    public boolean isConnected() {
        return mySession!=null;
    }

    public void send(String message) {
        if(mySession==null) {
            logger.error("Error: connection closed. ");
            return;
        }

        logger.debug("send: {}",message);
        try {
            if(!message.endsWith("\n")) message+="\n";
            mySession.sendMessage(message);
        } catch (Exception e) {
            logger.error("Error: {}", e.getMessage());
        }
    }

    private void recv(SessionLayerEvent e) {
        if(e.flag == SessionLayerEvent.DATA_AVAILABLE) {
            logger.debug("recv: {}",e.data);
        }
    }
}
