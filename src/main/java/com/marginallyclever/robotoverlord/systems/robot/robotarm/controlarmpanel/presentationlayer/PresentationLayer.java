package com.marginallyclever.robotoverlord.systems.robot.robotarm.controlarmpanel.presentationlayer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * In the <a href="https://en.wikipedia.org/wiki/OSI_model">OSI model</a>, the Presentation layer is responsible for
 * translating data between the application layer and the session.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public interface PresentationLayer {
    ArrayList<ActionListener> listeners = new ArrayList<>();

    default void addListener(ActionListener listener) {
        listeners.add(listener);
    }

    default void removeListener(ActionListener listener) {
        listeners.remove(listener);
    }

    JPanel getPanel();

    boolean isIdleCommand(ActionEvent e);

    void sendGoHome();

    void closeConnection();
}
