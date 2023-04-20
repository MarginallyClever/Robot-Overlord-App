package com.marginallyclever.communications.transport.serial;

import com.marginallyclever.communications.session.SessionLayer;
import com.marginallyclever.communications.session.SessionLayerEvent;
import com.marginallyclever.communications.transport.TransportLayer;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Encapsulate all serial receive/transmit at the session layer of the OSI model.
 * @author Peter Colapietro
 * @since v7, 4/12/15.
 */
public final class SerialSession extends SessionLayer implements SerialPortEventListener {
	private static final Logger logger = LoggerFactory.getLogger(SerialSession.class);
	private static final int DEFAULT_BAUD_RATE = 250000;

	private SerialPort serialPort;
	private final TransportLayer transportLayer;
	private String connectionName = "";
	private boolean portOpened = false;

	// parsing input from outside source
	private String inputBuffer = "";

	public SerialSession(SerialTransportLayer layer) {
		super();
		transportLayer = layer;
	}

	@Override
	public void closeConnection() {		
		if (serialPort != null) {
			try {
				serialPort.removeEventListener();
				serialPort.closePort();
			} catch (SerialPortException e) {}
		}
		portOpened = false;
	}

	/**
	 * Open a serial connection to a device.  Has no idea to whom we are opening.
	 */
	@Override
	public void openConnection(String portName) throws Exception {
		if (portOpened) return;

		// open the port
		try {
			// is baud rate included?
			int baud = DEFAULT_BAUD_RATE;
			int i = portName.indexOf("@");
			if(i>=0) {
				// isolate it
				baud = Integer.parseInt(portName.substring(i+1));
				// remove it so the serial stuff doesn't get confused by the name.
				portName = portName.substring(0,i);
			}

			serialPort = new SerialPort(portName);
			serialPort.openPort();// Open serial port
			serialPort.setParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			serialPort.addEventListener(this);
	
			connectionName = portName;
			portOpened = true;
		}
		catch(jssc.SerialPortException e) {
			logger.error(e.getMessage());
		}
	}


	// Deal with something robot has sent.
	@Override
	public void serialEvent(SerialPortEvent events) {
		if(!events.isRXCHAR()) return;
		if(!portOpened) return;
		
		String rawInput, oneLine;
		int x;
		
		int len = events.getEventValue();
		byte [] buffer;
		try {
			buffer = serialPort.readBytes(len);
		} catch (SerialPortException e) {
			notifyListeners(new SessionLayerEvent(this, SessionLayerEvent.TRANSPORT_ERROR,e.getLocalizedMessage()));
			return;
		}
		
		if( len<=0 ) return;
		
		rawInput = new String(buffer,0,len);
		inputBuffer += rawInput;
		
		// each line ends with a \n.
		for( x=inputBuffer.indexOf("\n"); x!=-1; x=inputBuffer.indexOf("\n") ) {
			x++;
			oneLine = inputBuffer.substring(0,x);
			inputBuffer = inputBuffer.substring(x);

			//logger.info("SerialConnection SEND " + msg.trim());
			
			notifyListeners(new SessionLayerEvent(this, SessionLayerEvent.DATA_AVAILABLE,oneLine));
		}
	}

	@Override
	public void sendMessage(String msg) throws Exception {
		if(!portOpened) return;

		//logger.info("SerialConnection RECV " + msg.trim());
		
		serialPort.writeBytes(msg.getBytes());
	}

	// connect to the last port
	@Override
	public void reconnect() throws Exception {
		openConnection(connectionName);
	}

	/**
	 * @return the port open for this serial connection.
	 */
	@Override
	public boolean isOpen() {
		return portOpened;
	}

	@Override
	public String getName() {
		return connectionName;
	}

	@Override
	public TransportLayer getTransportLayer() {
		return this.transportLayer;
	}
}
