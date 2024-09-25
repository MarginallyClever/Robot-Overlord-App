package com.marginallyclever.ro3.node.nodes.marlinrobot;

import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.marlinrobot.marlinrobotarm.MarlinRobotArmPanel;

import javax.swing.*;
import java.util.List;
import java.util.Objects;

/**
 * {@link MarlinRobot} represents a robot with Marlin firmware installed.
 */
public class MarlinRobot extends Node {
    public MarlinRobot() {
        this("Marlin Robot");
    }

    public MarlinRobot(String name) {
        super(name);
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

    /**
     * <p>Send a single gcode command to the marlin robot.  It will reply by firing a
     * {@link MarlinListener#messageFromMarlin} event with the String response.</p>
     * @param gcode GCode command
     */
    public void sendGCode(String gcode) {
        fireMarlinMessage( "Error: unknown command "+gcode );
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/node/nodes/marlinrobot/marlin.png")));
    }
}
