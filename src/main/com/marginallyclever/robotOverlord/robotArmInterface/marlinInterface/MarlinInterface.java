package com.marginallyclever.robotOverlord.robotArmInterface.marlinInterface;

import java.awt.Font;
import java.awt.Color;
import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import com.marginallyclever.communications.NetworkSessionEvent;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.robots.robotArm.RobotArmBone;
import com.marginallyclever.robotOverlord.robots.robotArm.RobotArmIK;

public class MarlinInterface extends JPanel {
	private static final long serialVersionUID = -6388563393882327725L;

	private RobotArmIK myArm;
	
	private int lineNumber;

	private TextInterfaceToNetworkSession chatInterface = new TextInterfaceToNetworkSession();

	private JButton bESTOP = new JButton("EMERGENCY STOP");
	private JButton bGetAngles = new JButton("M114");
	private JButton bSetHome = new JButton("Set Home");
	private JButton bGoHome = new JButton("Go Home");

	public MarlinInterface(RobotArmIK arm) {
		super();

		myArm = arm;

		this.setLayout(new BorderLayout());
		this.add(getToolBar(), BorderLayout.PAGE_START);
		this.add(chatInterface, BorderLayout.CENTER);

		arm.addPropertyChangeListener((e) -> {
			if(e.getPropertyName().contentEquals("ee")) sendGoto();	
		});

		chatInterface.addActionListener((e) -> {
			switch (e.getID()) {
			case ChooseConnectionPanel.CONNECTION_OPENED:
				onConnect();
				break;
			case ChooseConnectionPanel.CONNECTION_CLOSED:
				updateButtonAccess();
				break;
			}
		});
	}

	private void onConnect() {
		setupListener();
		
		lineNumber=0;
		
		// you are at the position I say you are at.
		new java.util.Timer().schedule(new java.util.TimerTask() {
			@Override
			public void run() {
				updateButtonAccess();
				sendSetHome();
			}
		}, 1000 // 1s delay
		);
	}

	private void setupListener() {
		chatInterface.addNetworkSessionListener((evt) -> {
			if(evt.flag == NetworkSessionEvent.DATA_AVAILABLE) {
				String message = ((String)evt.data).trim();
				if (message.startsWith("X:") && message.contains("Count")) {
					//System.out.println("FOUND " + message);
					processM114Reply(message);
				}
			}
		});
	}
	
	private void sendCommand(String str) {
		lineNumber++;
		str = "N"+lineNumber+" "+str;
		str += generateChecksum(str);
		
		chatInterface.sendCommand(str);
	}

	private String generateChecksum(String line) {
		byte checksum = 0;

		for (int i = 0; i < line.length(); ++i) {
			checksum ^= line.charAt(i);
		}

		return "*" + Integer.toString(checksum);
	}

	// format is normally X:0.00 Y:270.00 Z:0.00 U:270.00 V:180.00 W:0.00 Count X:0 Y:0 Z:0 U:0 V:0 W:0
	// trim everything after and including "Count", then read the state data.
	private void processM114Reply(String message) {
		try {
			message = message.substring(0, message.indexOf("Count"));
			String[] majorParts = message.split("\b");
			double[] angles = myArm.getAngles();
	
			for (int i = 0; i < myArm.getNumBones(); ++i) {
				RobotArmBone bone = myArm.getBone(i);
				for (String s : majorParts) {
					String[] minorParts = s.split(":");

					if (minorParts[0].contentEquals(bone.getName())) {
							angles[i] = Double.valueOf(minorParts[1]);
					}
				}
			}
			myArm.setAngles(angles);
		} catch (NumberFormatException e) {
			Log.error("M114: "+e.getMessage());
		}
	}

	private void sendGoto() {
		//System.out.println("MarlinInterface.sendGoto()");
		String action = "G1";
		for (int i = 0; i < myArm.getNumBones(); ++i) {
			RobotArmBone bone = myArm.getBone(i);
			action += " " + bone.getName() + StringHelper.formatDouble(bone.getTheta());
		}
		sendCommand(action);
	}

	private JToolBar getToolBar() {
		JToolBar bar = new JToolBar();
		bar.setRollover(true);

		bESTOP.setFont(getFont().deriveFont(Font.BOLD));
		bESTOP.setForeground(Color.RED);

		bESTOP.addActionListener((e) -> sendESTOP() );
		bGetAngles.addActionListener((e) -> sendGetPosition() );
		bSetHome.addActionListener((e) -> sendSetHome() );
		bGoHome.addActionListener((e) -> sendGoHome() );

		bar.add(bESTOP);
		bar.addSeparator();
		bar.add(bGetAngles);
		bar.add(bSetHome);
		bar.add(bGoHome);
		
		updateButtonAccess();

		return bar;
	}

	private void sendESTOP() {
		sendCommand("M112");
	}

	private void sendGetPosition() {
		sendCommand("M114");
	}

	private void updateButtonAccess() {
		boolean isConnected = chatInterface.getIsConnected();

		bESTOP.setEnabled(isConnected);
		bGetAngles.setEnabled(isConnected);
		bSetHome.setEnabled(isConnected);
		bGoHome.setEnabled(isConnected);
	}

	private void sendSetHome() {
		try {
			String action = "G92";
			RobotArmIK temp = (RobotArmIK)myArm.clone();
			for (int i = 0; i < temp.getNumBones(); ++i) {
				RobotArmBone bone = temp.getBone(i);
				action += " " + bone.getName() + StringHelper.formatDouble(bone.getTheta());
			}
			sendCommand(action);
			myArm.setAngles(temp.getAngles());
		} catch (CloneNotSupportedException e) {
			Log.error("G92: "+e.getMessage());
		}
	}

	private void sendGoHome() {
		try {
			RobotArmIK temp = (RobotArmIK)myArm.clone();
			myArm.setAngles(temp.getAngles());
			myArm.setEndEffectorTarget(myArm.getEndEffector());
		} catch (CloneNotSupportedException e) {
			Log.error("GoHome: "+e.getMessage());
		}
	}

	// TEST

	public static void main(String[] args) {
		Log.start();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}

		JFrame frame = new JFrame(MarlinInterface.class.getName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new MarlinInterface(new RobotArmIK()));
		frame.pack();
		frame.setVisible(true);
	}
}
