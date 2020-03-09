package com.marginallyclever.robotOverlord.entity.robot.sixi2;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.marginallyclever.convenience.PanelHelper;
import com.marginallyclever.convenience.SpringUtilities;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.engine.dhRobot.DHLink;
import com.marginallyclever.robotOverlord.engine.dhRobot.DHRobot;
import com.marginallyclever.robotOverlord.entity.robot.sixi2.Sixi2.ControlMode;
import com.marginallyclever.robotOverlord.entity.robot.sixi2.Sixi2.OperatingMode;
import com.marginallyclever.robotOverlord.uiElements.CollapsiblePanel;

/**
 * Control Panel for a DHRobot
 * @author Dan Royer
 *
 */
public class Sixi2Panel extends JPanel implements ActionListener, ChangeListener, ItemListener, Observer {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;


	protected Sixi2 robot;
	protected RobotOverlord ro;

	public JButton goHome;
	public JButton goRest;
	public JSlider feedrate, acceleration, gripperOpening;
	public JLabel  feedrateValue, accelerationValue, gripperOpeningValue;

	public JCheckBox recordMode, liveOperation, singleBlock;
//	public JComboBox<String> frameOfReferenceSelection;

	public JLabel gcodeLabel;
	public JTextField gcodeValue;
	public JPanel ghostPosPanel;
	public JPanel livePosPanel;
	public ReentrantLock sliderLock;

	public JButton resetRecording,playRecording,addCommand,setCommand,delCommand;
	JScrollPane scrollPane;
	public JList<String> recordedCommands;
	public JSlider scrubber;
	public ReentrantLock scrubberLock;


	public class Pair {
		public JSlider slider;
		public DHLink  link;
		public JLabel  label;

		public Pair(JSlider slider0,DHLink link0,JLabel label0) {
			slider=slider0;
			link=link0;
			label=label0;
		}
	}

	ArrayList<Pair> liveJoints = new ArrayList<Pair>();
	ArrayList<Pair> ghostJoints = new ArrayList<Pair>();

	// enumerate these?
	String[] framesOfReference = {
			"World", //0
			"Camera", //1
			"Finger tip"//2
			};


	public Sixi2Panel(RobotOverlord gui,Sixi2 robot) {
		this.robot = robot;
		this.ro = gui;
		sliderLock = new ReentrantLock();
		scrubberLock = new ReentrantLock();

		buildPanel();
	}

	protected void buildPanel() {
		this.removeAll();

		this.setName("Sixi 2");
		this.setLayout(new GridBagLayout());
		this.setBorder(new EmptyBorder(0,0,0,0));

		GridBagConstraints con1 = PanelHelper.getDefaultGridBagConstraints();

		//this.add(toggleATC=new JButton(robot.dhTool!=null?"ATC close":"ATC open"), con1);

		// GO HOME BUTTON
		this.add(goHome=new JButton("Go Home"), con1);
		con1.gridy++;
		goHome.addActionListener(this);
		// GO REST BUTTON
		this.add(goRest=new JButton("Go Rest"), con1);
		con1.gridy++;
		goRest.addActionListener(this);

		// FEEDRATE SLIDER
		this.add(feedrateValue=new JLabel(),con1);
		con1.gridy++;
		this.add(feedrate=new JSlider(),con1);
		con1.gridy++;
		feedrate.setMaximum(80);
		feedrate.setMinimum(1);
		feedrate.setMinorTickSpacing(1);
		feedrate.addChangeListener(this);
		feedrate.setValue((int)robot.getFeedRate());
		stateChanged(new ChangeEvent(feedrate));
		// ACCELERATION SLIDER
		this.add(accelerationValue=new JLabel(),con1);
		con1.gridy++;
		this.add(acceleration=new JSlider(),con1);
		con1.gridy++;
		acceleration.setMaximum(120);
		acceleration.setMinimum(1);
		acceleration.setMinorTickSpacing(1);
		acceleration.addChangeListener(this);
		acceleration.setValue((int)robot.getAcceleration());
		stateChanged(new ChangeEvent(acceleration));

		// RECORD MODE CHECK
		this.add(recordMode=new JCheckBox(),con1);
		con1.gridy++;
		recordMode.setText("Record Mode ON");
		recordMode.setSelected(robot.controlMode==ControlMode.RECORD);
		recordMode.addItemListener(this);
		// LIVE OPERATING MODE CHECK
		this.add(liveOperation=new JCheckBox(),con1);
		con1.gridy++;
		liveOperation.setText("Live Operation Mode");
		liveOperation.setSelected(robot.operatingMode==OperatingMode.LIVE);
		liveOperation.addItemListener(this);
		// SINGLE BLOCK CHECK
		this.add(singleBlock=new JCheckBox(),con1);
		con1.gridy++;
		singleBlock.setText("Single Block Mode");
		singleBlock.setSelected(robot.singleBlock);
		singleBlock.addItemListener(this);

		// RECORDING PANEL
		CollapsiblePanel recordingPanel = new CollapsiblePanel("Recording   ");
		this.add(recordingPanel,con1);
		recordingPanel.getContentPane().setLayout(new BoxLayout(recordingPanel.getContentPane(),BoxLayout.PAGE_AXIS));

		JPanel contents = new JPanel();
		recordingPanel.getContentPane().add(contents);
		contents.setBorder(new EmptyBorder(0,0,0,0));
		contents.setLayout(new GridBagLayout());
		GridBagConstraints con2 = new GridBagConstraints();
//		con2.fill = GridBagConstraints.HORIZONTAL;
		// DELETE BUTTON
		con2.gridx=0;	con2.gridy=0;
		contents.add(delCommand=new JButton("Delete"),con2);
		delCommand.addActionListener(this);
		// ADD BUTTON
		con2.gridx=1;	con2.gridy=0;
		contents.add(addCommand=new JButton("Add"),con2);
		addCommand.addActionListener(this);
		// OVERWRITE BUTTON
		con2.gridx=2;	con2.gridy=0;
		contents.add(setCommand=new JButton("Overwrite"),con2);
		setCommand.addActionListener(this);

		con2.gridx=0;	con2.gridy=1;
		con2.gridwidth = 3;
		con2.fill = GridBagConstraints.HORIZONTAL;
//		String[] data = {"one", "222", "three", "four"};

		contents.add(scrollPane = new JScrollPane(),con2);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
//		scrollPane.setBounds(206, 154, 177, 189);

		contents.add(recordedCommands = new JList<String>(),con2);
		updateCommandList();
		scrollPane.setViewportView(recordedCommands);
		recordedCommands.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		recordedCommands.setLayoutOrientation(JList.VERTICAL);
		recordedCommands.setVisibleRowCount(130);
		

		// RESET BUTTON
		GridBagConstraints con3 = new GridBagConstraints();
		con3.ipadx=5;	con3.ipady=5;
		con3.gridx=0;	con3.gridy=2;
		contents.add(resetRecording = new JButton("Reset"),con3);
		resetRecording.addActionListener(this);
		
		// PLAY BUTTON
		con3.gridx=1;	con3.gridy=2;
		contents.add(playRecording=new JButton("Play"),con3);
		playRecording.addActionListener(this);

		// GCODE TEXT
		this.add(gcodeLabel=new JLabel("Gcode"), con1);
		con1.gridy++;
		this.add(gcodeValue=new JTextField(),con1);
		con1.gridy++;
		gcodeValue.setEditable(false);
		Dimension dim = gcodeValue.getPreferredSize();
		dim.width=60;
		gcodeValue.setPreferredSize( dim );
		gcodeValue.setMaximumSize(dim);
		gcodeValue.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
            	StringSelection stringSelection = new StringSelection(gcodeValue.getText());
            	Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            	clipboard.setContents(stringSelection, null);
            }
        });

/*
		contents.add(activeTool=new JLabel("Tool=") ,con1);
		  con1.gridy++;
		contents.add(gripperOpening=new JSlider(),con1);
		con1.gridy++;
		contents.add(gripperOpeningValue=new JLabel(),con1);
		con1.gridy++;
		gripperOpening.setMaximum(120);
		gripperOpening.setMinimum(90);
		gripperOpening.setMinorTickSpacing(5);
		gripperOpening.addChangeListener(this);
		gripperOpening.setValue((int)robot.dhTool.getAdjustableValue());
		stateChanged(new ChangeEvent(gripperOpening));
*/
/*
		contents.add(new JLabel("Frame of Reference") ,con1);  con1.gridy++;
		contents.add(frameOfReferenceSelection=new JComboBox<String>(Sixi2.Frame.values()),con1);
		frameOfReferenceSelection.addActionListener(this);
		frameOfReferenceSelection.setSelectedIndex(robot.getFrameOfReference());
		con1.gridy++;
*/
		// LIVE PANEL
		int i;
		JLabel label;

		CollapsiblePanel livePanel = new CollapsiblePanel("Live");
		this.add(livePanel,con1);
		con1.gridy++;
		livePanel.getContentPane().setLayout(new BoxLayout(livePanel.getContentPane(),BoxLayout.PAGE_AXIS));

		// live panel
		contents = new JPanel();
		livePanel.getContentPane().add(contents);

		contents.setBorder(new EmptyBorder(0,0,0,0));
		contents.setLayout(new SpringLayout());
		i=0;
		for( DHLink link : robot.live.links ) {
			if(!link.hasAdjustableValue()) continue;
			JSlider newSlider=new JSlider(
					JSlider.HORIZONTAL,
					(int)link.getRangeMin(),
					(int)link.getRangeMax(),
					(int)link.getRangeMin());
			newSlider.setMinorTickSpacing(5);
			Dimension preferredSize = newSlider.getPreferredSize();
			preferredSize.width=-1;
			newSlider.setPreferredSize(preferredSize);
			newSlider.setEnabled(false);
			contents.add(new JLabel(Integer.toString(i++)));
			contents.add(newSlider);
			contents.add(label=new JLabel("0.000",SwingConstants.RIGHT));
			liveJoints.add(new Pair(newSlider,link,label));
			link.addObserver(this);
			newSlider.setValue((int)link.getAdjustableValue());
			label.setText(StringHelper.formatDouble(link.getAdjustableValue()));
			label.setMinimumSize(new Dimension(50,16));
			label.setPreferredSize(label.getMinimumSize());
		}
		SpringUtilities.makeCompactGrid(contents, i, 3, 5, 5, 5, 5);

		livePosPanel = new JPanel();
		livePosPanel.setBorder(new EmptyBorder(0,0,0,0));
		livePosPanel.setLayout(new SpringLayout());
		livePanel.getContentPane().add(livePosPanel);
		updatePosition(robot.sim,livePosPanel);

		// GHOST PANEL
		CollapsiblePanel ghostPanel = new CollapsiblePanel("Ghost");
		this.add(ghostPanel,con1);
		con1.gridy++;
		ghostPanel.getContentPane().setLayout(new BoxLayout(ghostPanel.getContentPane(),BoxLayout.PAGE_AXIS));

		// ghost joints
		contents = new JPanel();
		ghostPanel.getContentPane().add(contents);
		contents.setBorder(new EmptyBorder(0,0,0,0));
		contents.setLayout(new SpringLayout());
		i=0;
		for( DHLink link : robot.sim.links ) {
			if(!link.hasAdjustableValue()) continue;
			JSlider newSlider=new JSlider(
					JSlider.HORIZONTAL,
					(int)link.getRangeMin(),
					(int)link.getRangeMax(),
					(int)link.getRangeMin());
			newSlider.setMinorTickSpacing(5);
			Dimension preferredSize = newSlider.getPreferredSize();
			preferredSize.width=-1;
			newSlider.setPreferredSize(preferredSize);
			contents.add(new JLabel(Integer.toString(i++)));
			contents.add(newSlider);
			contents.add(label=new JLabel("0.000",SwingConstants.RIGHT));
			ghostJoints.add(new Pair(newSlider,link,label));
			link.addObserver(this);
			newSlider.setValue((int)link.getAdjustableValue());
			label.setText(StringHelper.formatDouble(link.getAdjustableValue()));
			label.setMinimumSize(new Dimension(50,16));
			label.setPreferredSize(label.getMinimumSize());

			//newSlider.setEnabled(false);
			newSlider.addChangeListener(this);
		}
		SpringUtilities.makeCompactGrid(contents, i, 3, 5, 5, 5, 5);

		ghostPosPanel = new JPanel();
		ghostPosPanel.setBorder(new EmptyBorder(0,0,0,0));
		ghostPosPanel.setLayout(new SpringLayout());
		ghostPanel.getContentPane().add(ghostPosPanel);
		updatePosition(robot.sim,ghostPosPanel);

		gcodeValue.setText(robot.getCommand());

//		contents.add(scrubber=new JSlider(),con2);

		PanelHelper.ExpandLastChild(this, con1);

//		scrubber.addChangeListener(this);
	}

	protected void updatePosition(DHRobot r, JPanel p) {/*
		p.removeAll();
		Vector3d pos = new Vector3d();
		r.getEndEffectorMatrix().get(pos);
		p.add(new JLabel("X"));	 p.add(new JLabel(StringHelper.formatDouble(pos.x)));
		p.add(new JLabel("Y"));	 p.add(new JLabel(StringHelper.formatDouble(pos.y)));
		p.add(new JLabel("Z"));	 p.add(new JLabel(StringHelper.formatDouble(pos.z)));
		SpringUtilities.makeCompactGrid(p, 1, 6, 5, 5, 5, 5);*/
	}

	// SLIDERS
	@Override
	public void stateChanged(ChangeEvent event) {
		Object source = event.getSource();
		if(source == feedrate) {
			int v = feedrate.getValue();
			robot.setFeedRate(v);
			feedrateValue.setText("feed rate = "+StringHelper.formatDouble(v));
		}
		if(source == acceleration) {
			int v = acceleration.getValue();
			robot.setAcceleration(v);
			accelerationValue.setText("acceleration = "+StringHelper.formatDouble(v));
		}
		if(source == gripperOpening) {
			int v = gripperOpening.getValue();
			robot.sendCommand("G0 T"+v);
			gripperOpeningValue.setText("gripper = "+StringHelper.formatDouble(v));
		}

		//*
		if(!sliderLock.isLocked()) {
			sliderLock.lock();
				for( Pair p : ghostJoints ) {
					if(p.slider == source) {
						if(!p.link.hasChanged()) {
							//System.out.println("slider begins");
							int v = ((JSlider)source).getValue();
							p.link.setAdjustableValue(v);
							p.label.setText(StringHelper.formatDouble(v));
							robot.sim.refreshPose();
							//System.out.println("slider ends");
							break;
						}
					}
				}
			sliderLock.unlock();
		}
		if(scrubber==source) {
			if(!scrubberLock.isLocked()) {
				scrubberLock.lock();
				//robot.interpolator.setPlayhead(scrubber.getValue()*0.1);
				scrubberLock.unlock();
			}
		}
		//*/
		/*
		if(false) {
			// test size of labels
			double w=0,h=0;
			for( Pair p : ghostJoints ) {
				Dimension d = p.label.getSize();
				w=Math.max(w,d.getWidth());
				h=Math.max(h,d.getHeight());
			}
			System.out.println("w"+w+"\th"+h);
		}//*/
		// live and ghost joints are not updated by the user
		// because right now that would cause an infinite loop.

		if(gcodeValue!=null) {
			gcodeValue.setText(robot.getCommand());
		}
	}
	
	// BUTTONS
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		if(source==goHome) robot.goHome();
		if(source==goRest) robot.goRest();
		if(source==resetRecording) rewind();
		if(source==playRecording) {
			if(!robot.isCycleStart()) play();
			else stop();
		}
		if(source==delCommand) {
			robot.deleteCurrentCommand();
			updateCommandList();
		}
		if(source==addCommand) {
			robot.addCommand();
			updateCommandList();
		}
		if(source==setCommand) {
			robot.setCommand();
			updateCommandList();
		}
	}

	// Rebuild the contents of recordedCommands
	protected void updateCommandList() {
		// do we have a selected item?
		int selectedIndex = recordedCommands.getSelectedIndex();
		// rebuild the contents of the list
		DefaultListModel<String> list = new DefaultListModel<String>();
		ArrayList<String> robotCommands = robot.getCommandList();
		int index=0;
		for( String s : robotCommands ) {
			list.add(index++, s);
		}
		recordedCommands.setModel(list);
		// remember our selected item
		recordedCommands.setSelectedIndex(selectedIndex);
		// make sure the JList gets redrawn.
		recordedCommands.revalidate();
	}
	
	public void play() {
		playRecording.setText("Pause");
		robot.setCycleStart(true);
	}
	public void rewind() {
		playRecording.setText("Play");
		robot.reset();
	}
	public void stop() {
		playRecording.setText("Play");
		robot.setCycleStart(false);
	}

	// CHECKBOXES
	@Override
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getItemSelectable();
		if(source == recordMode) {
			robot.toggleControlMode();
		}
		if(source == liveOperation) {
			robot.toggleOperatingMode();
		}
		if(source == singleBlock) {
			robot.toggleSingleBlock();
		}
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		for( Pair p : liveJoints ) {
			if(p.link == arg0) {
				double v = (double)arg1;
				p.slider.setValue((int)v);
				p.label.setText(StringHelper.formatDouble(v));
				break;
			}
		}
		if(!sliderLock.isLocked()) {
			sliderLock.lock();
			for( Pair p : ghostJoints ) {
				if(p.link == arg0) {
					//System.out.println("observe begins");
					double v = (double)arg1;
					p.slider.setValue((int)v);
					p.label.setText(StringHelper.formatDouble(v));
					//System.out.println("observe ends");
					break;
				}
			}
			sliderLock.unlock();
		}
		updatePosition(robot.live,livePosPanel);
		updatePosition(robot.sim,ghostPosPanel);
	}

	public void setScrubHead(int pos) {
		scrubberLock.lock();
		scrubber.setValue(pos);
		scrubberLock.unlock();
	}
}
