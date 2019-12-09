package com.marginallyclever.robotOverlord.dhRobot.dhRobotPlayer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.CollapsiblePanel;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.Translator;
import com.marginallyclever.robotOverlord.commands.UserCommandSelectFile;
import com.marginallyclever.robotOverlord.dhRobot.DHRobot;

/**
 * Control Panel for a DHRobot
 * @author Dan Royer
 *
 */
public class DHRobotControlBoxPanel extends JPanel implements ActionListener, ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected DHRobotControlBox player;
	protected RobotOverlord ro;
	
	protected UserCommandSelectFile fileToPlay;
	
	protected JButton reset;
	protected JButton buttonSingleBlock;
	protected JButton buttonLoop; 
	protected JButton buttonCycleStart;
	
	protected JTextField directCommand;
	protected JButton directCommandSend;
	protected JButton saveLivePositionToFile;
	protected JButton loadFileToLivePosition;
	
	protected JComboBox<String> userHeight;
	protected JButton setHeight;
	protected JButton firstPosition;

	protected int START_HEIGHT = 12*4;
	
	public DHRobotControlBoxPanel(RobotOverlord gui,DHRobotControlBox arg0) {
		this.player = arg0;
		this.ro = gui;
		
		buildPanel();
	}
	
	protected void buildPanel() {
		this.removeAll();
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=1;
		c.weighty=1;
		c.anchor=GridBagConstraints.NORTHWEST;
		c.fill=GridBagConstraints.HORIZONTAL;

		CollapsiblePanel oiwPanel = new CollapsiblePanel("DHRobotControlBox");
		this.add(oiwPanel,c);
		JPanel contents = oiwPanel.getContentPane();
		
		contents.setBorder(new EmptyBorder(0,0,0,0));
		contents.setLayout(new GridBagLayout());
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=1;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		
		contents.add(new JLabel("File to play"), con1);
		con1.gridy++;
		contents.add(fileToPlay=new UserCommandSelectFile(ro,Translator.get("..."),player.getFileToPlay()), con1);
		con1.gridy++;
		fileToPlay.addChoosableFileFilter(new FileNameExtensionFilter("GCode", "ngc"));

		contents.add(reset = new JButton("Reset"),con1);
		con1.gridy++;
		contents.add(buttonSingleBlock = new JButton("[ ] Single block"),con1);
		con1.gridy++;
		contents.add(buttonLoop = new JButton("[ ] Loop at end"),con1);
		con1.gridy++;
		contents.add(buttonCycleStart = new JButton("[ ] Cycle start"),con1);
		con1.gridy++; 
		
		reset.addActionListener(this);
		buttonSingleBlock.addActionListener(this);
		buttonLoop.addActionListener(this);
		buttonCycleStart.addActionListener(this);
		fileToPlay.addChangeListener(this);
		
		buttonSingleBlock.setSelected(player.isSingleBlock());
		buttonLoop.setSelected(player.isLoop());
		buttonCycleStart.setSelected(player.isCycleStart());

		contents.add(new JLabel(" "),con1);
		con1.gridy++;
		contents.add(saveLivePositionToFile = new JButton("Save as G30"),con1);
		con1.gridy++;
		saveLivePositionToFile.addActionListener(this);
		contents.add(loadFileToLivePosition = new JButton("Go to G30"),con1);
		con1.gridy++;
		loadFileToLivePosition.addActionListener(this);
		
		contents.add(new JLabel(" "),con1);
		con1.gridy++;
		contents.add(new JLabel("Direct command"),con1);
		con1.gridy++;
		contents.add(directCommand = new JTextField(),con1);
		con1.gridy++;
		contents.add(directCommandSend = new JButton("Send"),con1);
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

		contents.add(new JLabel(" "),con1);
		con1.gridy++;
		contents.add(new JLabel("Selfie mode"),con1);
		con1.gridy++;
		contents.add(userHeight=new JComboBox<String>(heightArray),con1);
		con1.gridy++;
		contents.add(setHeight=new JButton("3..2..1..Go!"),con1);
		con1.gridy++;
		contents.add(firstPosition=new JButton("First position"),con1);
		con1.gridy++;
		
		setHeight.addActionListener(this);
		firstPosition.addActionListener(this);
		userHeight.addActionListener(this);
		
		updateLabels();
	}
	
	protected void updateLabels() {
		buttonSingleBlock.setText("["+(player.isSingleBlock()?"X":" ")+"] Single block");
		buttonLoop.setText("["+(player.isLoop()?"X":" ")+"] Loop at end");
		buttonCycleStart.setText("["+(player.isCycleStart()?"X":" ")+"] Cycle start");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		if(source == reset) {
			player.reset();
			updateLabels();
		}
		if(source==buttonSingleBlock) {
			player.setSingleBlock(!player.isSingleBlock());
			updateLabels();
		}
		if(source==buttonLoop) {
			player.setLoop(!player.isLoop());
			updateLabels();
		}
		if(source==buttonCycleStart) {
			player.setCycleStart(player.isCycleStart());
			updateLabels();
		}
		
		if(source == saveLivePositionToFile) {
			saveLivePositionToFile();
		}
		if(source == loadFileToLivePosition) {
			loadFileToLivePosition();
		}
		if(source == directCommandSend) {
			DHRobot t=player.getTarget();
			if(t!=null) {
				t.parseGCode(directCommand.getText());
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

	
	public void saveLivePositionToFile() {
		DHRobot t=player.getTarget();
		String gcode = t.generateGCode();
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("G30.ngc"));
		    writer.write(gcode);
		    writer.close();
		} catch(IOException e) {
			JOptionPane.showConfirmDialog(null, "Failed to write G30.ngc");
		}
	}
	
	public void loadFileToLivePosition() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader("G30.ngc"));
			String gcode=reader.readLine();
			reader.close();
			player.getTarget().sendLineToRobot(gcode);
		} catch(IOException e) {
			JOptionPane.showConfirmDialog(null, "Failed to read G30.ngc");
		}
	}

	//Vector3d p1 = new Vector3d(9.144,0.554,37.670);  // first position 1: G0 X9.144 Y0.554 Z37.670 I157.906 J-9.010 K68.404
	// first position 2: G0 X8.663 Y1.309 Z38.386 I160.996 J-13.178 K59.727

	// first position 3: G0 X19.915 Y-1.603 Z56.819 I-7.493 J-3.974 K-68.714
	Vector3d p1 = new Vector3d(19.915,-1.603,56.819);
	// end position: G0 X65.662 Y11.137 Z61.554 I-20.775 J-4.898 K-80.431
	Vector3d p2 = new Vector3d(65.662,11.137,0);  
	
	
	protected void setHeight(double height_cm) {
		p2 = new Vector3d(11.137,80.662,0);  
		DHRobot target=player.getTarget();
		if(target==null) return;
		
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
			target.parseGCode(msg);
		}
	}
	
	
	protected void goToFirstPosition() {
		p1 = new Vector3d(5,1,36.819);
		DHRobot target=player.getTarget();
		String msg = "G0"
				+" X"+StringHelper.formatDouble(p1.x)
				+" Y"+StringHelper.formatDouble(p1.y)
				+" Z"+StringHelper.formatDouble(p1.z)
				+" I-7.493 J-3.974 K-68.714"
				+" T90.000 R0.000 S11.908 F50 A100";
		if(target!=null) {
			//System.out.println(">>>> "+msg);
			target.parseGCode(msg);
		}
	}
	
	
	@Override
	public void stateChanged(ChangeEvent e) {
		Object source = e.getSource();
		if(source==fileToPlay) {
			player.setFileToPlay(fileToPlay.getFilename());
		}
	}
}
