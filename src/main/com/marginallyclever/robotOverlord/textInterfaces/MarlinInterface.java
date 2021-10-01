package com.marginallyclever.robotOverlord.textInterfaces;

import java.awt.Font;
import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import com.marginallyclever.communications.NetworkSessionEvent;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.robots.sixi3.Sixi3Bone;
import com.marginallyclever.robotOverlord.robots.sixi3.Sixi3IK;

public class MarlinInterface extends JPanel {
	private static final long serialVersionUID = -6388563393882327725L;

	private Sixi3IK mySixi3;
	private TextInterfaceToNetworkSession chatInterface = new TextInterfaceToNetworkSession();

	public MarlinInterface(Sixi3IK sixi3) {
		super();

		mySixi3 = sixi3;

		this.setLayout(new BorderLayout());
		this.add(getToolBar(), BorderLayout.PAGE_START);
		this.add(chatInterface, BorderLayout.CENTER);

		sixi3.addPropertyChangeListener((e) -> {
			if(e.getPropertyName().contentEquals("ee")) sendGoto();	
		});

		chatInterface.addActionListener((e) -> {
			switch (e.getID()) {
			case ChooseConnectionPanel.NEW_CONNECTION:
				onConnect();
				break;
			}
		});
	}

	private void onConnect() {
		setupListener();
		// you are at the position I say you are at.
		new java.util.Timer().schedule(new java.util.TimerTask() {
			@Override
			public void run() {
				sendSetHome();
			}
		}, 1000 // 1s delay
		);
	}

	private void setupListener() {
		chatInterface.getNetworkSession().addListener((evt) -> {
			if (evt.flag == NetworkSessionEvent.DATA_AVAILABLE) {
				String message = ((String) evt.data).trim();
				if (message.startsWith("X:") && message.contains("Count")) {
					System.out.println("FOUND " + message);
					processM114Reply(message);
				}
			}
		});
	}

	// format is normally X:0.00 Y:270.00 Z:0.00 U:270.00 V:180.00 W:0.00 Count X:0
	// Y:0 Z:0 U:0 V:0 W:0
	// trim everything after and including "Count", then read the angles into sixi3.
	private void processM114Reply(String message) {
		message = message.substring(0, message.indexOf("Count"));
		String[] majorParts = message.split("\b");
		double[] angles = mySixi3.getAngles();

		for (int i = 0; i < mySixi3.getNumBones(); ++i) {
			Sixi3Bone bone = mySixi3.getBone(i);
			for (String s : majorParts) {
				String[] minorParts = s.split(":");

				if (minorParts[0].contentEquals(bone.getName())) {
					try {
						angles[i] = Double.valueOf(minorParts[1]);
					} catch (NumberFormatException e) {
					}
				}
			}
		}
		mySixi3.setAngles(angles);
	}

	private void sendGoto() {
		String action = "G1";
		for (int i = 0; i < mySixi3.getNumBones(); ++i) {
			Sixi3Bone bone = mySixi3.getBone(i);
			action += " " + bone.getName() + StringHelper.formatDouble(bone.getTheta());
		}
		chatInterface.sendCommand(action);
	}

	private JToolBar getToolBar() {
		JToolBar bar = new JToolBar();
		bar.setRollover(true);

		JButton bESTOP = new JButton("EMERGENCY STOP");
		JButton bGetAngles = new JButton("M114");
		JButton bSetHome = new JButton("Set Home");
		JButton bGoHome = new JButton("Go Home");

		bESTOP.setFont(getFont().deriveFont(Font.BOLD));

		bESTOP.addActionListener((e) -> chatInterface.sendCommand("M112") );
		bGetAngles.addActionListener((e) -> chatInterface.sendCommand("M114") );
		bSetHome.addActionListener((e) -> sendSetHome() );
		bGoHome.addActionListener((e) -> sendGoHome() );

		bar.add(bESTOP);
		bar.add(bGetAngles);
		bar.add(bSetHome);
		bar.add(bGoHome);

		return bar;
	}

	private void sendSetHome() {
		String action = "G92";
		Sixi3IK temp = new Sixi3IK();
		for (int i = 0; i < temp.getNumBones(); ++i) {
			Sixi3Bone bone = temp.getBone(i);
			action += " " + bone.getName() + StringHelper.formatDouble(bone.getTheta());
		}
		chatInterface.sendCommand(action);
		mySixi3.setAngles(temp.getAngles());
	}

	private void sendGoHome() {
		Sixi3IK temp = new Sixi3IK();
		mySixi3.setAngles(temp.getAngles());
		mySixi3.setEndEffectorTarget(mySixi3.getEndEffector());
	}

	// TEST

	public static void main(String[] args) {
		Log.start();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}

		JFrame frame = new JFrame("MarlinInterface");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new MarlinInterface(new Sixi3IK()));
		frame.pack();
		frame.setVisible(true);
	}
}
