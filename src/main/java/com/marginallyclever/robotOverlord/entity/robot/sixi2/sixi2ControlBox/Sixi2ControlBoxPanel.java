package com.marginallyclever.robotOverlord.entity.robot.sixi2.sixi2ControlBox;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.PanelHelper;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.engine.translator.Translator;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandSelectFile;
import com.marginallyclever.robotOverlord.entity.robot.sixi2.Sixi2;

/**
 * Control Panel for a DHRobot
 * @author Dan Royer
 *
 */
public class Sixi2ControlBoxPanel extends JPanel implements ActionListener, ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected Sixi2ControlBox player;
	protected Sixi2 robot;
	protected RobotOverlord ro;
	
	protected UserCommandSelectFile fileToPlay;
	
	protected JButton reset;
	protected JButton buttonSingleBlock;
	protected JButton buttonLoop; 
	protected JButton buttonCycleStart;
	
	protected JTextField directCommand;
	protected JButton directCommandSend;
	
	protected JComboBox<String> userHeight;
	protected JButton setHeight;
	protected JButton firstPosition;

	protected int START_HEIGHT = 12*4;

	//Vector3d p1 = new Vector3d(9.144,0.554,37.670);  // first position 1: G0 X9.144 Y0.554 Z37.670 I157.906 J-9.010 K68.404
	// first position 2: G0 X8.663 Y1.309 Z38.386 I160.996 J-13.178 K59.727

	// first position 3: G0 X19.915 Y-1.603 Z56.819 I-7.493 J-3.974 K-68.714
	Vector3d p1 = new Vector3d(19.915,-1.603,56.819);
	// end position: G0 X65.662 Y11.137 Z61.554 I-20.775 J-4.898 K-80.431
	Vector3d p2 = new Vector3d(65.662,11.137,0);  
	
	
	public Sixi2ControlBoxPanel(RobotOverlord gui,Sixi2ControlBox arg0) {
		this.player = arg0;
		this.ro = gui;
		
		buildPanel();
	}
	
	protected void buildPanel() {
		robot = (Sixi2)player.getParent();
		
		this.removeAll();
		
		this.setName("Sixi 2 Control Box");
		this.setLayout(new GridBagLayout());

		GridBagConstraints con1 = PanelHelper.getDefaultGridBagConstraints();
		
		this.add(new JLabel("File to play"), con1);
		con1.gridy++;
		this.add(fileToPlay=new UserCommandSelectFile(ro,Translator.get("..."),robot.recording.fileFrom), con1);
		con1.gridy++;
		fileToPlay.addChoosableFileFilter(new FileNameExtensionFilter("GCode", "ngc"));

		this.add(reset = new JButton("Reset"),con1);
		con1.gridy++;
		this.add(buttonSingleBlock = new JButton("[ ] Single block"),con1);
		con1.gridy++;
		this.add(buttonLoop = new JButton("[ ] Loop at end"),con1);
		con1.gridy++;
		this.add(buttonCycleStart = new JButton("[ ] Cycle start"),con1);
		con1.gridy++; 
		
		reset.addActionListener(this);
		buttonSingleBlock.addActionListener(this);
		buttonLoop.addActionListener(this);
		buttonCycleStart.addActionListener(this);
		fileToPlay.addChangeListener(this);
		
		buttonSingleBlock.setSelected(robot.isSingleBlock());
		buttonCycleStart.setSelected(robot.isCycleStart());

		this.add(new JLabel(" "),con1);
		con1.gridy++;
		this.add(new JLabel("Direct command"),con1);
		con1.gridy++;
		this.add(directCommand = new JTextField(),con1);
		con1.gridy++;
		this.add(directCommandSend = new JButton("Send"),con1);
		con1.gridy++;
		directCommandSend.addActionListener(this);
		
		ArrayList<String> heights = new ArrayList<String>();
		for(int i=START_HEIGHT;i<12*7;++i) {
			int inches = i%12;
			int feet = i/12;
			int cm = (int)(i*2.54);
			heights.add(new String(""+feet+"'"+inches+"\" ("+cm+"cm)"));
		}
		
		String[] heightArray = heights.toArray(new String[1]);

		this.add(new JLabel(" "),con1);
		con1.gridy++;
		this.add(new JLabel("Selfie mode"),con1);
		con1.gridy++;
		this.add(userHeight=new JComboBox<String>(heightArray),con1);
		con1.gridy++;
		this.add(setHeight=new JButton("3..2..1..Go!"),con1);
		con1.gridy++;

		this.add(firstPosition=new JButton("First position"),con1);
		
		setHeight.addActionListener(this);
		firstPosition.addActionListener(this);
		userHeight.addActionListener(this);

		PanelHelper.ExpandLastChild(this, con1);
		updateLabels();
	}
	
	protected void updateLabels() {
		buttonSingleBlock.setText("["+(robot.isSingleBlock()?"X":" ")+"] Single block");
		buttonCycleStart.setText("["+(robot.isCycleStart()?"X":" ")+"] Cycle start");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		if(source == reset) {
			robot.reset();
			updateLabels();
		}
		if(source==buttonSingleBlock) {
			robot.toggleSingleBlock();
			updateLabels();
		}
		if(source==buttonCycleStart) {
			robot.toggleCycleStart();
			updateLabels();
		}
		
		if(source == directCommandSend) {
			if(robot!=null) {
				//JOptionPane.showMessageDialog(null, directCommand.getText());
				robot.sendCommand(directCommand.getText());
			}
		}
		if(source == firstPosition) {
			goToFirstPosition();
		}
		if(source == setHeight) {
			int height = (int)((userHeight.getSelectedIndex()+START_HEIGHT)*2.54);
			setHeight(height);
		}
	}
	
	
	protected void setHeight(double height_cm) {
		p2 = new Vector3d(11.137,80.662,0);
		if(robot==null) return;
		
		double z = height_cm-71.0-10;  // subtract table height and top of head.
		double y = p2.x;
		double x = p2.y;
		
		Vector3d eyes = new Vector3d(x,y,z);  // remove height of table

		// How close can we get to that position and still be solvable?
		// Who cares!  Send them all and some might not work out.  *shrug*
		
		//if(height_cm<p1.z-20 || height_cm>p1.z+170) return;
		//System.out.println(">>>> "+height_cm);
		
		for(double t=0;t<=1;t+=0.05) {
			Vector3d c = MathHelper.interpolate(p1, eyes, t);
			String msg = "G0"
					+" X"+StringHelper.formatDouble(c.x)
					+" Y"+StringHelper.formatDouble(c.y)
					+" Z"+StringHelper.formatDouble(c.z)
					+"";
			//System.out.println(">>>> "+msg);
			robot.sendCommand(msg);
		}
	}
	
	
	protected void goToFirstPosition() {
		if(robot==null) return;
		p1 = new Vector3d(5,1,36.819);
		String msg = "G0"
				+" X"+StringHelper.formatDouble(p1.x)
				+" Y"+StringHelper.formatDouble(p1.y)
				+" Z"+StringHelper.formatDouble(p1.z)
				+" I-7.493 J-3.974 K-68.714"
				+" T90.000 R0.000 S11.908 F50 A100";
		//System.out.println(">>>> "+msg);
		robot.sendCommand(msg);
	}
	
	
	@Override
	public void stateChanged(ChangeEvent e) {
		Object source = e.getSource();
		if(source==fileToPlay) {
			robot.recording.loadRecording(fileToPlay.getFilename());
		}
	}
}
