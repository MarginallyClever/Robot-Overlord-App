package com.marginallyclever.communications.serial;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.communications.TransportLayer;
import com.marginallyclever.robotOverlord.log.Log;


/**
 * Created on 4/12/15.  Encapsulate all jssc serial receive/transmit implementation
 *
 * @author Peter Colapietro
 * @since v7
 */
public final class SerialConnection extends NetworkConnection implements SerialPortEventListener {
	private static final int DEFAULT_BAUD_RATE = 57600;
	private static final String CUE = "> ";
	private static final String NOCHECKSUM = "NOCHECKSUM ";
	private static final String BADCHECKSUM = "BADCHECKSUM ";
	private static final String BADLINENUM = "BADLINENUM ";
	private static final String NEWLINE = "\n";
	private static final String COMMENT_START = ";";

	private SerialPort serialPort;
	private TransportLayer transportLayer;
	private String connectionName = "";
	private boolean portOpened = false;
	private boolean waitingForCue = false;

	// parsing input from outside source
	private String inputBuffer = "";

	
	public SerialConnection(SerialTransportLayer layer) {
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

	// open a serial connection to a device.  We won't know it's the robot until
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
			waitingForCue = true;
		}
		catch(jssc.SerialPortException e) {
			// TODO display this more gracefully?
			Log.error(e.getLocalizedMessage());
		}
	}


	/**
	 * Check if the robot reports an error and if so what line number.
	 * @param line the message from the robot to be parsed
	 * @return -1 if there was no error, otherwise the line number containing the error.
	 */
	protected int errorReported(String line) {
		if (line.lastIndexOf(NOCHECKSUM) != -1) {
			String after_error = line.substring(line.lastIndexOf(NOCHECKSUM) + NOCHECKSUM.length());
			String x = getNumberPortion(after_error);
			int err = 0;
			try {
				err = Integer.decode(x);
				Log.error("NOCHECKSUM "+err);
			} catch (Exception e) {}

			return err;
		}
		if (line.lastIndexOf(BADCHECKSUM) != -1) {
			String after_error = line.substring(line.lastIndexOf(BADCHECKSUM) + BADCHECKSUM.length());
			String x = getNumberPortion(after_error);
			int err = 0;
			try {
				err = Integer.decode(x);
				Log.error("BADCHECKSUM "+x);
			} catch (Exception e) {}

			return err;
		}
		if (line.lastIndexOf(BADLINENUM) != -1) {
			String after_error = line.substring(line.lastIndexOf(BADLINENUM) + BADLINENUM.length());
			String x = getNumberPortion(after_error);
			int err = 0;
			try {
				err = Integer.decode(x);
				Log.error("BADLINENUM "+err);
			} catch (Exception e) {}

			return err;
		}

		return -1;
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
			// uh oh
			return;
		}
		
		if( len<=0 ) return;
		
		rawInput = new String(buffer,0,len);
		inputBuffer+=rawInput;
		
		// each line ends with a \n.
		for( x=inputBuffer.indexOf("\n"); x!=-1; x=inputBuffer.indexOf("\n") ) {
			x=x+1;
			oneLine = inputBuffer.substring(0,x);
			inputBuffer = inputBuffer.substring(x);
			
			reportDataReceived(oneLine);
			
			// check for error
			int error_line = errorReported(oneLine);
			if(error_line != -1) {
				notifyLineError(error_line);
			} else {
				// no error
				if(!oneLine.trim().equals(CUE.trim())) {
					notifyDataAvailable(oneLine);
				}
			}

			// wait for the cue to send another command
			if(oneLine.indexOf(CUE)==0) {
				waitingForCue=false;
			}
		}
	}

	public void reportDataSent(String msg) {
		//Log.message("SerialConnection SEND " + msg.trim());
	}

	public void reportDataReceived(String msg) {
		//Log.message("SerialConnection RECV " + msg.trim());
	}

	@Override
	public void sendMessage(String msg) throws Exception {
		if(!portOpened || waitingForCue) return;

		try {
			waitingForCue=true;
			if(msg.contains(COMMENT_START)) {
				String [] lines = msg.split(COMMENT_START);
				msg = lines[0];
			}
			if(msg.endsWith(NEWLINE) == false) {
				msg+=NEWLINE;
			}
			reportDataSent(msg.trim());
			serialPort.writeBytes(msg.getBytes());
		}
		catch(IndexOutOfBoundsException e1) {}
		catch(SerialPortException e2) {}
	}

	// connect to the last port
	@Override
	public void reconnect() throws Exception {
		openConnection(connectionName);
	}

	/**
	 * Java string to int is very picky.  this method is slightly less picky.  Only works with positive whole numbers.
	 *
	 * @param src
	 * @return the portion of the string that is actually a number
	 */
	private String getNumberPortion(String src) {
		src = src.trim();
		int length = src.length();
		String result = "";
		for (int i = 0; i < length; i++) {
			Character character = src.charAt(i);
			if (Character.isDigit(character)) {
				result += character;
			}
		}
		return result;
	}

	/**
	 * @return the port open for this serial connection.
	 */
	@Override
	public boolean isOpen() {
		return portOpened;
	}

	@Override
	public String getRecentConnection() {
		return connectionName;
	}

	@Override
	public TransportLayer getTransportLayer() {
		return this.transportLayer;
	}
}
