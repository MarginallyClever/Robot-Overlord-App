package com.marginallyclever.communications.presentation;

import com.marginallyclever.communications.SessionLayer;

/**
 * In the <a href="https://en.wikipedia.org/wiki/OSI_model">, the Presentation layer is responsible for translating
 * data between the application layer and the session.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public interface PresentationLayer {

    void onConnect(SessionLayer sessionLayer);

    void onDisconnect(SessionLayer sessionLayer);

    void processIncomingMessage(String message);

    void processOutgoingMessage(String message);
}
