package com.marginallyclever.robotoverlord.components.robot.robotarm.robotarminterface.presentationlayer;

import com.marginallyclever.communications.presentation.PresentationLayer;
import com.marginallyclever.communications.session.SessionLayer;

/**
 * {@link CANOpen} is a {@link PresentationLayer} for CANOpen protocol.
 * @author Dan Royer
 */
public class CANOpen implements PresentationLayer {
    @Override
    public void onConnect(SessionLayer sessionLayer) {

    }

    @Override
    public void onDisconnect(SessionLayer sessionLayer) {

    }

    @Override
    public void processIncomingMessage(String message) {

    }

    @Override
    public void processOutgoingMessage(String message) {

    }
}
