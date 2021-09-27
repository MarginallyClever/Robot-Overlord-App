package com.marginallyclever.robotOverlord.textInterfaces;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.vecmath.Matrix4d;

import com.marginallyclever.communications.NetworkSession;
import com.marginallyclever.communications.NetworkSessionEvent;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.robots.sixi3.Sixi3Bone;
import com.marginallyclever.robotOverlord.robots.sixi3.Sixi3IK;

public class RobotUI extends JPanel {
	private static final long serialVersionUID = -6388563393882327725L;
	
	private Sixi3IK sixi3 = new Sixi3IK();
	private TextInterfaceToNetworkSession chatInterface = new TextInterfaceToNetworkSession();
	private JPanel angleReport = getAngleReportPanel();
	private JPanel cartesianReport = getCartesianReportPanel();
	private JPanel sixi3FKDrivePanel = getSixi3FKDrivePanel();
	private JPanel sixi3IKDrivePanel = getSixi3IKDrivePanel();
	private JToolBar toolBar = getToolBar();
	
	public RobotUI() {
		super();
		
		JPanel interior = getInteriorPanel();

		this.setLayout(new BorderLayout());
		this.add(toolBar,BorderLayout.PAGE_START);
		this.add(interior,BorderLayout.CENTER);
	}
	
	private JPanel getInteriorPanel() {
		JPanel panel = new JPanel();
		
		panel.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=1;
		c.weighty=0.75;
		c.gridheight=6;
		c.gridwidth=1;
		c.fill=GridBagConstraints.BOTH;
		c.anchor=GridBagConstraints.NORTHWEST;
		
		panel.add(chatInterface,c);
		
		c.gridheight=1;
		c.gridwidth=1;
		c.gridx=1;
		c.weightx=0.25;
		c.weighty=0;
		
		c.gridy=0;
		panel.add(angleReport,c);
		c.gridy++;
		panel.add(sixi3FKDrivePanel,c);

		c.gridy++;
		panel.add(cartesianReport,c);
		c.gridy++;
		panel.add(sixi3IKDrivePanel,c);

		c.gridy++;
		c.weighty=1;
		panel.add(new JPanel(),c);
		
		sixi3.addPropertyChangeListener((e)-> {
			sendGoto();
		});
		
		chatInterface.addActionListener((e)->{
			switch(e.getID()) {
			case ChooseConnectionPanel.NEW_CONNECTION:
				setupListener();
				// you are at the position I say you are at.
				new java.util.Timer().schedule( 
				        new java.util.TimerTask() {
				            @Override
				            public void run() {
				            	sendSetHome();
				            }
				        }, 
				        1000 // 1s delay 
				);
				break;
			}
			
		});
		
		return panel;
	}
	
	private void setupListener() {
		chatInterface.getNetworkSession().addListener((evt)->{
			if(evt.flag == NetworkSessionEvent.DATA_AVAILABLE) {
				String message = ((String)evt.data).trim();
				if(message.startsWith("X:") && message.contains("Count")) {
					System.out.println("FOUND "+message);
					processM114Reply(message);
				}
			}
		});
	}

	// format is normally X:0.00 Y:270.00 Z:0.00 U:270.00 V:180.00 W:0.00 Count X:0 Y:0 Z:0 U:0 V:0 W:0
	// trim everything after and including "Count", then read the angles into sixi3.
	private void processM114Reply(String message) {
		message = message.substring(0,message.indexOf("Count"));
		String [] majorParts = message.split("\b");
		double [] angles = sixi3.getFKValues();
		
		for(int i=0;i<sixi3.getNumBones();++i) {
			Sixi3Bone bone = sixi3.getBone(i);
			for(String s : majorParts) {
				String [] minorParts = s.split(":");
				
				if(minorParts[0].contentEquals(bone.getName())) {
					try {
						angles[i] = Double.valueOf(minorParts[1]);
					} catch(NumberFormatException e) {}
				}
			}
		}
		sixi3.setFKValues(angles);
	}

	private void sendGoto() {
		String action="G0";
		for(int i=0;i<sixi3.getNumBones();++i) {
			Sixi3Bone bone = sixi3.getBone(i);
			action+=" "+bone.getName()+StringHelper.formatDouble(bone.getTheta());
		}
		chatInterface.sendCommand(action);
	}

	private JToolBar getToolBar() {
		JToolBar bar = new JToolBar();
		bar.setRollover(true);
		
		// TODO button: "You angles are xyzuvw"
		JButton bESTOP = new JButton("EMERGENCY STOP");
		JButton bGetAngles = new JButton("Tell me your angles");
		JButton bSetHome = new JButton("You are at home");
		JButton bGoHome = new JButton("Go home");
		
		bar.add(bESTOP);
		bar.add(bGetAngles);
		bar.add(bSetHome);
		bar.add(bGoHome);
		
		bESTOP.addActionListener((e)->{
			chatInterface.sendCommand("M112");
		});
		
		bGetAngles.addActionListener((e)->{
			sendGetPosition();
		});
		
		bSetHome.addActionListener((e)->{
			sendSetHome();
		});
		
		bGoHome.addActionListener((e)->{
			sendGoHome();
		});
		
		return bar;
	}
	
	private void sendGetPosition() {
		chatInterface.sendCommand("M114");
	}

	private void sendSetHome() {
		String action="G92";
		for(int i=0;i<sixi3.getNumBones();++i) {
			Sixi3Bone bone = sixi3.getBone(i);
			action+=" "+bone.getName()+StringHelper.formatDouble(bone.getTheta());
		}
		chatInterface.sendCommand(action);
	}
	
	private void sendGoHome() {
		String action="G0";
		Sixi3IK temp = new Sixi3IK();
		double [] angles = temp.getFKValues();
		
		for(int i=0;i<sixi3.getNumBones();++i) {
			Sixi3Bone bone = sixi3.getBone(i);
			action+=" "+bone.getName()+StringHelper.formatDouble(angles[i]);
		}
		chatInterface.sendCommand(action);
	}

	private JSlider makeSliderFromBone(Sixi3Bone b) {
		JSlider slider = new JSlider((int)b.getAngleMin(),
							(int)b.getAngleMax(),
							(int)b.getTheta());
		slider.setEnabled(false);
		return slider;
	}
	
	private JPanel getAngleReportPanel() {
		JSlider [] joint = new JSlider[sixi3.getNumBones()];
		
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Joint angles"));
		panel.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=0;
		c.weighty=0;
		c.gridheight=1;
		c.gridwidth=1;
		c.anchor=GridBagConstraints.WEST;
		
		for(int i=0;i<joint.length;++i) {
			Sixi3Bone bone = sixi3.getBone(i);
			c.gridx=0;
			c.weightx=0;
			c.fill = GridBagConstraints.NONE;
			panel.add(new JLabel(bone.getName()),c);
			
			c.gridx=1;
			c.weightx=1;
			c.fill = GridBagConstraints.HORIZONTAL;
			joint[i] = makeSliderFromBone(bone);
			panel.add(joint[i],c);
			
			c.gridy++;
		}

		sixi3.addPropertyChangeListener((e)-> {
			for(int i=0;i<joint.length;++i) {
				Sixi3Bone bone = sixi3.getBone(i);
				joint[i].setValue((int)bone.getTheta());
			}
		});
		
		return panel;
	}
	
	private JPanel getSixi3FKDrivePanel() {
		ButtonGroup buttonGroup = new ButtonGroup();
		JRadioButton [] buttons = new JRadioButton[sixi3.getNumBones()];
		for(int i=0;i<buttons.length;++i) {
			buttons[i] = makeRadioButton(buttonGroup,sixi3.getBone(i).getName());
		}
		buttons[0].setSelected(true);

		Dial dial = new Dial();
		dial.addActionListener((evt)-> {
			ButtonModel b = buttonGroup.getSelection();

			System.out.println("FK " + b.getActionCommand() + " V"+dial.getChange());
			
			double [] fk = sixi3.getFKValues();
			
			for(int i=0;i<buttons.length;++i) {
				if(buttons[i].isSelected()) {
					fk[i] += dial.getChange();
				}
			}
			
			sixi3.setFKValues(fk);
		});
		
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Joint control"));
		panel.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=0;
		c.weighty=0;
		c.gridheight=1;
		c.gridwidth=1;
		c.anchor=GridBagConstraints.WEST;

		for(int i=0;i<buttons.length;++i) {
			panel.add(buttons[i],c);
			c.gridy++;
		}
		
		c.gridx=1;
		c.gridy=0;
		c.weightx=1;
		c.weighty=1;
		c.gridwidth=buttons.length;
		c.gridheight=buttons.length;
		c.anchor=GridBagConstraints.EAST;
		dial.setPreferredSize(new Dimension(120,120));
		panel.add(dial,c);
		
		return panel;
	}

	private JPanel getCartesianReportPanel() {
		JLabel x = new JLabel("",JLabel.RIGHT);
		JLabel y = new JLabel("",JLabel.RIGHT);
		JLabel z = new JLabel("",JLabel.RIGHT);
		
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Finger position"));
		panel.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=0;
		c.weighty=0;
		c.gridheight=1;
		c.gridwidth=1;
		c.anchor=GridBagConstraints.WEST;

		buildCartesianReportLine(panel,c,x,"X");
		buildCartesianReportLine(panel,c,y,"Y");
		buildCartesianReportLine(panel,c,z,"Z");

		sixi3.addPropertyChangeListener((e)-> {
			Matrix4d m = sixi3.getEndEffector();
			x.setText(StringHelper.formatDouble(m.m03));
			y.setText(StringHelper.formatDouble(m.m13));
			z.setText(StringHelper.formatDouble(m.m23));
		});
		
		return panel;
	}
	
	private void buildCartesianReportLine(JPanel panel,GridBagConstraints c,JLabel field,String label) {
		c.gridx=0;
		c.weightx=0;
		c.fill = GridBagConstraints.NONE;
		panel.add(new JLabel(label),c);
		
		c.gridx=1;
		c.weightx=1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(field,c);
		
		field.setEnabled(false);
		
		c.gridy++;
	}
	
	private JPanel getSixi3IKDrivePanel() {
		ButtonGroup buttonGroup = new ButtonGroup();
		JRadioButton x = makeRadioButton(buttonGroup,"X");
		JRadioButton y = makeRadioButton(buttonGroup,"Y");
		JRadioButton z = makeRadioButton(buttonGroup,"Z");
		JRadioButton roll = makeRadioButton(buttonGroup,"roll");
		JRadioButton pitch = makeRadioButton(buttonGroup,"pitch");
		JRadioButton yaw = makeRadioButton(buttonGroup,"yaw");
		x.setSelected(true);
		
		Dial dial = new Dial();
		dial.addActionListener((evt)-> {
			double v = dial.getChange();
			Matrix4d m = sixi3.getEndEffector();
			
			if(x.isSelected()) m.m03 += v;
			if(y.isSelected()) m.m13 += v;
			if(z.isSelected()) m.m23 += v;
			
			boolean success = sixi3.moveEndEffectorTo(m);
			
			System.out.println("IK " + buttonGroup.getSelection().getActionCommand() + " V"+v+" = "+(success?"OK":"BAD"));
		});

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Finger tip control"));
		panel.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=0;
		c.weighty=0;
		c.gridheight=1;
		c.gridwidth=1;
		c.anchor=GridBagConstraints.WEST;
		
		panel.add(x,c);
		c.gridy++;
		panel.add(y,c);
		c.gridy++;
		panel.add(z,c);
		c.gridy++;
		panel.add(roll,c);
		c.gridy++;
		panel.add(pitch,c);
		c.gridy++;
		panel.add(yaw,c);
		
		c.gridx=2;
		c.gridy=0;
		c.weightx=1;
		c.weighty=1;
		c.gridwidth=6;
		c.gridheight=6;
		c.anchor=GridBagConstraints.EAST;
		dial.setPreferredSize(new Dimension(120,120));
		panel.add(dial,c);
		
		return panel;
	}

	private JRadioButton makeRadioButton(ButtonGroup group, String label) {
		JRadioButton rb = new JRadioButton(label);
		rb.setActionCommand(label);
		group.add(rb);
		return rb;
	}

	// TEST 
	
	public static void main(String[] args) {
		Log.start();
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {}
		
		JFrame frame = new JFrame("RobotUI");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new RobotUI());
		frame.pack();
		frame.setVisible(true);
	}
}
