package com.marginallyclever.robotOverlord.robots.sixi2;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
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

	protected transient JButton buttonKeyframeFirst;
	protected transient JButton buttonKeyframePrev;
	protected transient JButton buttonKeyframeNext;
	protected transient JButton buttonKeyframeLast;
	protected transient JButton buttonKeyframeAdd;
	protected transient JButton buttonKeyframeDelete;

	protected transient JButton buttonAnimateReverse;
	protected transient JButton buttonAnimatePlayPause;
	protected transient JButton buttonAnimateFastForward;

	protected transient JButton buttonKeyframesNew;
	protected transient JButton buttonKeyframesLoad;
	protected transient JButton buttonKeyframesSave;
	protected transient JButton buttonShowKeyframes;

	protected int START_HEIGHT = 12*4;
	
	public Sixi2ControlBoxPanel(RobotOverlord gui,Sixi2ControlBox arg0) {
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

/*
		CollapsiblePanel p;
		GridBagConstraints con2;
		
		p = new CollapsiblePanel("Animate");
		contents.add(p, con1);
		con1.gridy++;

		con2 = new GridBagConstraints();
		con2.gridx = 0;
		con2.gridy = 0;
		con2.weightx = 0.2;
		con2.weighty = 1;
		con2.fill = GridBagConstraints.HORIZONTAL;
		con2.anchor = GridBagConstraints.CENTER;

		p.getContentPane().add(buttonAnimateReverse = createButton(Translator.get("<<")), con2);
		con2.gridx++; // could be "\u23EA"
		p.getContentPane().add(buttonAnimatePlayPause = createButton(Translator.get("> / ||")), con2);
		con2.gridx++; // could be "\u23EF"
		p.getContentPane().add(buttonAnimateFastForward = createButton(Translator.get(">>")), con2);
		con2.gridx++; // could be "\u23E9"

		p = new CollapsiblePanel("Keyframes");
		contents.add(p, con1);
		con1.gridy++;

		con2 = new GridBagConstraints();
		con2.gridx = 0;
		con2.gridy = 0;
		con2.weightx = 0;
		con2.weighty = 1;
		con2.fill = GridBagConstraints.HORIZONTAL;
		con2.anchor = GridBagConstraints.CENTER;
		con2.gridwidth = 3;

		p.getContentPane().add(buttonKeyframesNew = createButton(Translator.get("New")), con2);
		con2.gridx += con2.gridwidth;
		p.getContentPane().add(buttonKeyframesLoad = createButton(Translator.get("Load")), con2);
		con2.gridx += con2.gridwidth;
		p.getContentPane().add(buttonKeyframesSave = createButton(Translator.get("Save")), con2);
		con2.gridx += con2.gridwidth;
		p.getContentPane().add(buttonShowKeyframes = createButton(Translator.get("Show")), con2);
		con2.gridx += con2.gridwidth;

		con2.gridx = 0;
		con2.gridy++;
		con2.gridwidth = 3;
		con2.weightx = 0.25;
		p.getContentPane().add(buttonKeyframeFirst = createButton(Translator.get("|<")), con2);
		con2.gridx += con2.gridwidth;
		p.getContentPane().add(buttonKeyframePrev = createButton(Translator.get("<")), con2);
		con2.gridx += con2.gridwidth;
		p.getContentPane().add(buttonKeyframeNext = createButton(Translator.get(">")), con2);
		con2.gridx += con2.gridwidth;
		p.getContentPane().add(buttonKeyframeLast = createButton(Translator.get(">|")), con2);
		con2.gridx += con2.gridwidth;

		con2.gridy++;
		con2.gridx = 0;
		con2.gridwidth = 6;
		con2.weightx = 0.5;
		p.getContentPane().add(buttonKeyframeAdd = createButton(Translator.get("+")), con2);
		con2.gridx += con2.gridwidth;
		p.getContentPane().add(buttonKeyframeDelete = createButton(Translator.get("-")), con2);
		con2.gridx += con2.gridwidth;*/
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
			Sixi2 t=player.getTarget();
			if(t!=null) {
				//JOptionPane.showMessageDialog(null, directCommand.getText());
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
		/*
		if (subject == buttonKeyframeFirst) {
			robot.setKeyframeIndex(0);
			robot.setKeyframeT(0);
			robot.updatePose();
		}
		if (subject == buttonKeyframePrev) {
			robot.setKeyframeIndex(robot.getKeyframeIndex() - 1);
			robot.setKeyframeT(0);
			robot.updatePose();
		}
		if (subject == buttonKeyframeNext) {
			robot.setKeyframeIndex(robot.getKeyframeIndex() + 1);
			robot.setKeyframeT(0);
			robot.updatePose();
		}
		if (subject == buttonKeyframeLast) {
			robot.setKeyframeIndex(robot.getKeyframeSize() - 1);
			robot.setKeyframeT(0);
			robot.updatePose();
		}

		if (subject == buttonKeyframeAdd) {
			robot.keyframeAddNow();
		}
		if (subject == buttonKeyframeDelete) {
			robot.keyframeDelete();
		}

		if (subject == buttonKeyframesNew) {
			newKeyframes();
		}
		if (subject == buttonKeyframesSave) {
			saveKeyframes();
		}
		if (subject == buttonKeyframesLoad) {
			loadKeyframes();
		}
		if (subject == buttonShowKeyframes) {
			toggleShowKeyframes();
		}

		if (subject == buttonAnimateFastForward) {
			double aSpeed = robot.getAnimationSpeed();
			robot.setAnimationSpeed(aSpeed <= 0 ? 1 : aSpeed + 1);
		}
		if (subject == buttonAnimatePlayPause) {
			robot.setAnimationSpeed(robot.getAnimationSpeed() == 0 ? 1 : 0);
		}
		if (subject == buttonAnimateReverse) {
			double aSpeed = robot.getAnimationSpeed();
			robot.setAnimationSpeed(aSpeed >= 0 ? -1 : aSpeed - 1);
		}

		System.out.println("K" + robot.getKeyframeIndex() + "\tT" + robot.getKeyframeT());
		*/
	}

	
	public void saveLivePositionToFile() {
		Sixi2 t=player.getTarget();
		String gcode = t.generateGCode();
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("G30.ngc"));
		    writer.write(gcode);
		    writer.close();
			//JOptionPane.showMessageDialog(null, "wrote "+gcode);
		} catch(IOException e) {
			JOptionPane.showMessageDialog(null, "Failed to write G30.ngc");
		}
	}
	
	public void loadFileToLivePosition() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader("G30.ngc"));
			String gcode=reader.readLine();
			reader.close();
			//JOptionPane.showMessageDialog(null, "read "+gcode);
			player.getTarget().parseGCode(gcode);
		} catch(IOException e) {
			JOptionPane.showMessageDialog(null, "Failed to read G30.ngc");
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
		Sixi2 target=player.getTarget();
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
		Sixi2 target=player.getTarget();
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

	protected void keyframeEditSetEnable(boolean isEnabled) {
		buttonKeyframesNew.setEnabled(isEnabled);
		buttonKeyframesSave.setEnabled(isEnabled);
		buttonKeyframesLoad.setEnabled(isEnabled);

		buttonKeyframeDelete.setEnabled(isEnabled);
		buttonKeyframeAdd.setEnabled(isEnabled);

		buttonKeyframeFirst.setEnabled(isEnabled);
		buttonKeyframePrev.setEnabled(isEnabled);
		buttonKeyframeNext.setEnabled(isEnabled);
		buttonKeyframeLast.setEnabled(isEnabled);
	}

	public void toggleShowKeyframes() {
		player.setIsDrawingKeyframes(!player.getIsDrawingKeyframes());
	}

	public void newKeyframes() {
		// move robot to start of first keyframe
		player.setKeyframeIndex(0);
		player.setKeyframeT(0);
		player.prepareMove(0);

		// wipe out all keyframes
		player.keyframes.clear();
	}

	public void saveKeyframes() {
		// open save dialog
		JFileChooser fc = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Keyframe data", "kfd");
		fc.setFileFilter(filter);
		int returnVal = fc.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String filename = fc.getSelectedFile().getAbsolutePath();
			if (filename.endsWith(".kfd"))
				filename += ".kfd";
			// if save is not cancelled, serialize the keyframe data
			player.saveKeyframes(filename);
		}
	}

	public void loadKeyframes() {
		// open save dialog
		JFileChooser fc = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Keyframe data", "kfd");
		fc.setFileFilter(filter);
		int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// if save is not cancelled, serialize the keyframe data
			player.loadKeyframes(fc.getSelectedFile().getAbsolutePath());
		}
	}
}
