package com.marginallyclever.ro3.node.nodes.marlinrobot;

import com.marginallyclever.communications.session.SessionLayerEvent;
import com.marginallyclever.communications.session.SessionLayerListener;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.networksession.NetworkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.List;
import java.util.Objects;

/**
 * {@link MarlinRobot} manages communication with a robot with Marlin firmware installed.
 * In the OSI network model it is the Presentation layer.
 */
public class MarlinRobot extends Node implements SessionLayerListener {
    private final Logger logger = LoggerFactory.getLogger(MarlinRobot.class);
    private NetworkSession networkSession = null;
    private boolean isConnected=false;

    public MarlinRobot() {
        this("Marlin Robot");
    }

    public MarlinRobot(String name) {
        super(name);
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        // guarantee that this MarlinRobot has a NetworkSession as a child.
        if(findFirstChild(NetworkSession.class)==null) {
            networkSession = new NetworkSession();
            addChild(networkSession);
            networkSession.addActionListener((e)->{
                if(e.getID()==NetworkSession.CONNECTION_OPENED) {
                    isConnected=true;
                    networkSession.getSession().addListener(this);
                }
            });
        }
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        if(networkSession!=null) {
            networkSession.closeConnection();
        }
    }

    @Override
    public void networkSessionEvent(SessionLayerEvent evt) {
        if(evt.flag == SessionLayerEvent.DATA_AVAILABLE) {
            fireMarlinMessage((String)evt.data);
        }
        if(evt.flag == NetworkSession.CONNECTION_CLOSED) {
            isConnected=false;
        }
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new MarlinRobotPanel(this));
        super.getComponents(list);
    }

    public void addMarlinListener(MarlinListener editorPanel) {
        listeners.add(MarlinListener.class,editorPanel);
    }

    public void removeMarlinListener(MarlinListener editorPanel) {
        listeners.remove(MarlinListener.class,editorPanel);
    }

    protected void fireMarlinMessage(String message) {
        //logger.info(message);
        for(MarlinListener listener : listeners.getListeners(MarlinListener.class)) {
            listener.messageFromMarlin(message);
        }
    }

    public String getMotorsAndFeedrateAsString() {
        return "";
    }

    /**
     * <p>Send a single gcode command to the marlin robot.  It will reply by firing a
     * {@link MarlinListener#messageFromMarlin} event with the String response.</p>
     * @param gcode GCode command
     */
    public void sendGCode(String gcode) {
        logger.debug("sendGCode: {}",gcode);
        if(networkSession==null || !networkSession.isConnected()) {
            // not connected to a network session
            logger.debug("not connected.");
            //fireMarlinMessage("Error: unknown command " + gcode);
            return;
        }
        networkSession.send(gcode);
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/node/nodes/marlinrobot/marlin.png")));
    }

    public boolean isConnected() {
        return isConnected;
    }
}
