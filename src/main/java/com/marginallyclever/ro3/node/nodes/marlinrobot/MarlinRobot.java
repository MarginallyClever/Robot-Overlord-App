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
 * High-level helper node for communicating with a Marlin-based robot.
 *
 * <p>This node acts as the "presentation" layer for Marlin G-code communication. It
 * will ensure a {@link com.marginallyclever.ro3.node.nodes.networksession.NetworkSession}
 * child exists (created on attach if missing) and registers itself as a
 * {@link com.marginallyclever.communications.session.SessionLayerListener} to receive
 * incoming Marlin replies.</p>
 *
 * Usage summary:
 * <ul>
 *   <li>Add an instance of {@code MarlinRobot} to your scene graph (or obtain a
 *       reference to an existing one).</li>
 *   <li>Listen for responses from Marlin by registering a {@code MarlinListener}
 *       with {@link #addMarlinListener}.</li>
 *   <li>Call {@link #sendGCode(String)} to send a single G-code command. Responses
 *       from the firmware are delivered asynchronously via {@code MarlinListener}.</li>
 *   <li>Test connectivity with {@link #isConnected()}.</li>
 * </ul>
 *
 * Integration notes:
 * <ul>
 *   <li>{@code MarlinRobotArm} and {@code LinearStewartPlatform} are existing
 *       consumers/examples in this project that use a {@code MarlinRobot} node to
 *       send motion commands. Follow those implementations as usage examples.</li>
 *   <li>The helper stores no blocking state — sending is delegated to the
 *       {@code NetworkSession}. Responses arrive on the session thread and are
 *       re-fired to listeners on this node.</li>
 *   <li>Implementations wanting to format kinematic commands can call
 *       {@link #getMotorsAndFeedrateAsString()} (currently a placeholder) or build
 *       their own G-code strings and call {@link #sendGCode(String)}.</li>
 * </ul>
 *
 * Threading and error handling:
 * <ul>
 *   <li>Connect/disconnect events are propagated via the attached
 *       {@code NetworkSession} and this node updates {@link #isConnected}.
 *   <li>Network errors and I/O are managed by the {@code NetworkSession}; code
 *       that calls {@link #sendGCode} should not block and should expect asynchronous
 *       replies.</li>
 * </ul>
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
        if(networkSession==null || !networkSession.isConnected()) {
            // not connected to a network session
            //logger.debug("not connected.");
            //fireMarlinMessage("Error: unknown command " + gcode);
            return;
        }
        logger.debug("sendGCode: {}",gcode);
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
