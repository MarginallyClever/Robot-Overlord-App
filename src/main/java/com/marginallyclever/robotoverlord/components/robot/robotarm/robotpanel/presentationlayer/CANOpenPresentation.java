package com.marginallyclever.robotoverlord.components.robot.robotarm.robotpanel.presentationlayer;

import com.marginallyclever.robotoverlord.robots.Robot;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * {@link CANOpenPresentation} is a {@link PresentationLayer} for CANOpen protocol.
 * @author Dan Royer
 * @since 2.5.0
 */
public class CANOpenPresentation implements PresentationLayer {
    private Robot myRobot;

    public CANOpenPresentation(Robot robot) {
        super();
        myRobot = robot;
    }

    @Override
    public javax.swing.JPanel getPanel() {
        return new JPanel();
    }

    @Override
    public boolean isIdleCommand(ActionEvent e) {
        return false;
    }

    @Override
    public void sendGoHome() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void closeConnection() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
