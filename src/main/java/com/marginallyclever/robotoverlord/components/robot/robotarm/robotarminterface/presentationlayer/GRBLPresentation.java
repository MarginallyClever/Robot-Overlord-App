package com.marginallyclever.robotoverlord.components.robot.robotarm.robotarminterface.presentationlayer;

import com.marginallyclever.communications.presentation.PresentationLayer;
import com.marginallyclever.communications.session.SessionLayer;

import javax.swing.*;

public class GRBLPresentation implements PresentationLayer {
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

    @Override
    public javax.swing.JPanel getPanel() {
        return new JPanel();
    }
}
