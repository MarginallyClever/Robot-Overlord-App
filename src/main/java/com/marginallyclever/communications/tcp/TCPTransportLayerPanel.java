package com.marginallyclever.communications.tcp;

import com.marginallyclever.communications.SessionLayer;
import com.marginallyclever.communications.TransportLayerPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Opens an SSH connection to another device, then opens a screen to the /dev/ACM0 device on that remote.
 * @author Dan Royer
 * 
 */
public class TCPTransportLayerPanel extends TransportLayerPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5157947915933861665L;
	private TCPTransportLayer layer;
	private JTextField connectionField;
	private JTextField portField;
	private JTextField userField;
	private JPasswordField passwordField;
	private static String userName = "pi";
	private static String portNumber = "22";
	private static String connectionName = "raspberrypi";
	private static String lastPassword = "";
	
	TCPTransportLayerPanel(TCPTransportLayer tcpLayer) {
		this.layer=tcpLayer;

		this.setBorder(new EmptyBorder(5,5,5,5));
		this.setLayout(new GridLayout(0, 1));
		this.add(new JLabel("IP address",JLabel.LEADING));
		this.add(connectionField = new JTextField(connectionName));
		this.add(new JLabel("Port",JLabel.LEADING));
		this.add(portField = new JTextField(portNumber));
		this.add(new JLabel("Username",JLabel.LEADING));
		this.add(userField = new JTextField(userName));
		this.add(new JLabel("Password",JLabel.LEADING));
		this.add(passwordField = new JPasswordField());
		
		connectionField.setText(connectionName);
		portField.setText(portNumber);
		passwordField.setText(lastPassword);
	}
	
	@Override
	public SessionLayer openConnection() {
		connectionName = connectionField.getText();
		portNumber = portField.getText();
		userName = userField.getText();
		lastPassword = String.copyValueOf(passwordField.getPassword());
		return layer.openConnection(userName+":"+lastPassword+"@"+connectionField.getText()+":"+portNumber);
	}
}
