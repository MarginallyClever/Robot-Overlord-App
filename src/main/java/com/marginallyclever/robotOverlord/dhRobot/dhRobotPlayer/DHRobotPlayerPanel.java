package com.marginallyclever.robotOverlord.dhRobot.dhRobotPlayer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
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
public class DHRobotPlayerPanel extends JPanel implements ActionListener, ChangeListener, ItemListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected DHRobotPlayer player;
	protected RobotOverlord ro;
	
	protected UserCommandSelectFile fileToPlay;
	
	protected JButton reset;
	protected JCheckBox buttonSingleBlock;
	protected JCheckBox buttonLoop; 
	protected JCheckBox buttonCycleStart;
	
	protected JTextField directCommand;
	protected JButton directCommandSend;
	
	protected JComboBox<String> userHeight;
	protected JButton setHeight;
	protected JButton firstPosition;

	protected static final int START_HEIGHT_IN=4*12;
	protected static final int END_HEIGHT_IN=7*12;
	
	
	public DHRobotPlayerPanel(RobotOverlord gui,DHRobotPlayer arg0) {
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

		CollapsiblePanel oiwPanel = new CollapsiblePanel("DHRobotPlayer");
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
		contents.add(buttonSingleBlock = new JCheckBox("Single block"),con1);
		con1.gridy++;
		contents.add(buttonLoop = new JCheckBox("Loop at end"),con1);
		con1.gridy++;
		contents.add(buttonCycleStart = new JCheckBox("Cycle start"),con1);
		con1.gridy++; 
		
		reset.addActionListener(this);
		buttonSingleBlock.addItemListener(this);
		buttonLoop.addItemListener(this);
		buttonCycleStart.addItemListener(this);
		fileToPlay.addChangeListener(this);
		
		buttonSingleBlock.setSelected(player.isSingleBlock());
		buttonLoop.setSelected(player.isLoop());
		buttonCycleStart.setSelected(player.isCycleStart());

		contents.add(new JLabel("Direct command"),con1);
		con1.gridy++;
		contents.add(directCommand = new JTextField(),con1);
		con1.gridy++;
		contents.add(directCommandSend = new JButton("Send"),con1);
		con1.gridy++;
		directCommandSend.addActionListener(this);
		
		ArrayList<String> heightArrayList = new ArrayList<String>();
		for(int i=START_HEIGHT_IN;i<END_HEIGHT_IN;++i) {
			int f = i/12;
			int in = i%12;
			int cm = (int)((double)i*2.54);
			heightArrayList.add(""+f+"'"+in+"\" ("+cm+"cm)");
		}
		// Converting ArrayList<String> to String[] found at 
		// http://www.codebind.com/java-tutorials/java-example-arraylist-string-array/
		Object [] heightObjectList = heightArrayList.toArray();
		String [] heightArray = Arrays.copyOf(heightObjectList,heightObjectList.length,String[].class);
		
		contents.add(userHeight=new JComboBox<String>(heightArray),con1);
		con1.gridy++;
		contents.add(setHeight=new JButton("3..2..1..Go!"),con1);
		con1.gridy++;
		contents.add(firstPosition=new JButton("First position"),con1);
		con1.gridy++;
		
		setHeight.addActionListener(this);
		firstPosition.addActionListener(this);
		userHeight.addActionListener(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source == reset) player.reset();
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
			int index = userHeight.getSelectedIndex();
			setHeight((int)((float)(START_HEIGHT_IN+index)*2.54f));
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// for checkboxes
		Object source = e.getItemSelectable();
		if(source==buttonSingleBlock) player.setSingleBlock	(buttonSingleBlock	.isSelected());
		if(source==buttonLoop		) player.setLoop		(buttonLoop			.isSelected());
		if(source==buttonCycleStart	) player.setCycleStart	(buttonCycleStart	.isSelected());
	}

	
	Vector3d p1 = new Vector3d(24.641,8.929,28.765);
	
	
	protected void setHeight(double height_cm) {
		DHRobot target=player.getTarget();
		if(target==null) return;
		
		double z = height_cm-71.0-13;  // subtract table height and top of head.
		double y = -30;
		double x = 70;
		
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
		DHRobot target=player.getTarget();
		String msg = "G0"
				+" X"+StringHelper.formatDouble(p1.x)
				+" Y"+StringHelper.formatDouble(p1.y)
				+" Z"+StringHelper.formatDouble(p1.z)
				+" I72.623 J-8.924 K66.369 T90.000 R0.000 S11.908 F80 A100";
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
