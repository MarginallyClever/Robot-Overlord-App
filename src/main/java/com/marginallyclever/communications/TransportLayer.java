package com.marginallyclever.communications;

import com.marginallyclever.communications.session.SessionLayer;

/**
 * Lists available connections in a medium (serial, TCP/IP, smoke signals, etc) and opens a connection of that type
 *
 * @author Dan
 * @since v7.1.0.0
 */
public interface TransportLayer {
  /**
   * opens a connection
   * @param connectionName where to connect
   * @return a connection to the device at address <code>connectionName</code>
   */
  public SessionLayer openConnection(String connectionName);

  /**
   * @return a panel with the gui options for this transport layer
   */
  public TransportLayerPanel getTransportLayerPanel();
}
