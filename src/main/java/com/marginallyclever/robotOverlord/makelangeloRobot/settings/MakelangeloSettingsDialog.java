package com.marginallyclever.robotOverlord.makelangeloRobot.settings;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ServiceLoader;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.Translator;
import com.marginallyclever.robotOverlord.makelangeloRobot.MakelangeloRobot;
import com.marginallyclever.robotOverlord.makelangeloRobot.drawingtools.DrawingTool;
import com.marginallyclever.robotOverlord.makelangeloRobot.settings.hardwareProperties.MakelangeloHardwareProperties;

/**
 * Controls related to configuring a Makelangelo machine
 *
 * @author danroyer
 * @since 7.1.4
 */
public class MakelangeloSettingsDialog
extends JDialog
implements ActionListener {
    
  /**
   * @see Serializable
   */
  private static final long serialVersionUID = 1L;

  transient protected MakelangeloRobot robot;

  transient protected JTabbedPane panes;
  transient protected JButton save, cancel;

  // TODO pull these from the class listings in the package?
  transient private JComboBox<String> hardwareVersionChoices;
  transient private ArrayList<Integer> availableHardwareVersions;
  transient private String[] hardwareVersionNames;
  transient private int originalHardwareVersion;
  
  transient private JPanel modelPanel;
  transient protected PanelAdjustMachine panelAdjustMachine;
  transient protected PanelAdjustPaper panelAdjustPaper;
  transient protected DrawingTool panelAdjustPen;
  transient private JPanel saveAndCancelPanel;
  transient private RobotOverlord gui;
  
  static protected int DIALOG_WIDTH = 450;
  static protected int DIALOG_HEIGHT = 500;
  
  public MakelangeloSettingsDialog(RobotOverlord gui, MakelangeloRobot robot) {
	super(gui.getMainFrame(),Translator.get("configureMachine"),true);
	this.gui = gui;
	this.robot = robot;
  }

  
  // display settings menu
  public void run() {
	  originalHardwareVersion = robot.getSettings().getHardwareVersion();
	  
	  this.setLayout(new GridBagLayout());
	  GridBagConstraints d = new GridBagConstraints();

	  buildModelPanel();

	  // hardware model settings
	  panes = new JTabbedPane(JTabbedPane.TOP);
	  panes.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	  //panes.setPreferredSize(new Dimension(dialogWidth,dialogHeight));

	  rebuildTabbedPanes();

	  buildSaveAndCancel();

	  // now assemble the dialog
	  d.fill=GridBagConstraints.HORIZONTAL;
	  d.gridx=0;
	  d.gridy=0;
	  d.weightx=0;
	  d.weighty=0;
	  d.gridwidth=1;
	  this.add(modelPanel, d);
	  d.fill=GridBagConstraints.BOTH;
	  d.gridy=1;
	  d.weightx=1;
	  d.weighty=1;
	  this.add(panes,d);
	  d.fill=GridBagConstraints.HORIZONTAL;
	  d.gridy=2;
	  d.weighty=0;
	  this.add(saveAndCancelPanel,d);
	  this.getRootPane().setDefaultButton(save);

	  Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	  this.setLocation((screenSize.width - DIALOG_WIDTH) / 2, (screenSize.height - DIALOG_HEIGHT) / 2);
	  this.pack();
	  this.setVisible(true);
  }

  // hardware model choice
  private void buildModelPanel() {
	  modelPanel = new JPanel(new GridBagLayout());
	  modelPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

	  GridBagConstraints d = new GridBagConstraints();
	  // the panes for the selected machine configuration
	  d.fill=GridBagConstraints.BOTH;
	  d.gridx=0;
	  d.gridy=0;
	  d.weightx=0;
	  d.weighty=0;
	  
	  JLabel modelLabel = new JLabel(Translator.get("HardwareVersion")); 
	  modelLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
	  modelPanel.add(modelLabel, d);
	  
	  findAvailableHardwareVersions();
	  
	  d.gridx=1;
	  d.gridwidth=2;
	  hardwareVersionChoices = new JComboBox<>(hardwareVersionNames);
	  // set the default
	  int hv = robot.getSettings().getHardwareVersion();
	  for(int i=0;i<availableHardwareVersions.size();++i) {
		  if(availableHardwareVersions.get(i) == hv) {
			  hardwareVersionChoices.setSelectedIndex(i);
			  break;
		  }
	  }
	  modelPanel.add(hardwareVersionChoices, d);
	  hardwareVersionChoices.addActionListener(this);
  }

  private void findAvailableHardwareVersions() {
	  availableHardwareVersions = new ArrayList<Integer>();
	  
	  // get version numbers
	  ServiceLoader<MakelangeloHardwareProperties> knownHardware = ServiceLoader.load(MakelangeloHardwareProperties.class);
	  Iterator<MakelangeloHardwareProperties> i = knownHardware.iterator();
	  while(i.hasNext()) {
		  MakelangeloHardwareProperties hw = i.next();
		  availableHardwareVersions.add(new Integer(hw.getVersion()));
	  }

	  // get names
	  hardwareVersionNames = new String[availableHardwareVersions.size()];
	  i = knownHardware.iterator();
	  int j=0;
	  while(i.hasNext()) {
		  MakelangeloHardwareProperties hw = i.next();
		  hardwareVersionNames[j] = hw.getName();
		  ++j;
	  }	  
  }
  
  // save and cancel buttons
  private void buildSaveAndCancel() {
	  cancel = new JButton(Translator.get("Cancel"));
	  save = new JButton(Translator.get("Save"));

	  saveAndCancelPanel = new JPanel(new GridBagLayout());
	  GridBagConstraints c = new GridBagConstraints();
	  c.anchor=GridBagConstraints.EAST;
	  c.gridx=0;
	  c.gridy=0;
	  c.weightx=0;
	  c.weighty=1;
	  c.gridx=1; c.gridwidth=1; saveAndCancelPanel.add(save,c);
	  c.gridx=2; c.gridwidth=1; saveAndCancelPanel.add(cancel,c);
	  c.weightx=1;
	  c.gridx=0; c.gridwidth=1; saveAndCancelPanel.add(Box.createGlue(),c);
	  cancel.addActionListener(this);
	  save.addActionListener(this);

	  saveAndCancelPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
  }
  
  
  private void rebuildTabbedPanes() {
	  panes.removeAll();
	  
	  panelAdjustMachine = new PanelAdjustMachine(gui,robot);
	  panes.addTab(Translator.get("MenuSettingsMachine"),panelAdjustMachine);

	  panelAdjustPaper = new PanelAdjustPaper(gui,robot);
	  panes.addTab(Translator.get("MenuAdjustPaper"),panelAdjustPaper);

	  panelAdjustPen = robot.getSettings().getTool(0);
	  JPanel p = panelAdjustPen.getPanel(gui);
	  panes.addTab(Translator.get("MenuAdjustTool"),p);

	  //panelAdjustTools = new PanelAdjustTools(translator,robot);
	  //panes.addTab(translator.get("MenuAdjustTool"),panelAdjustTools);

	  //panelSelectTool = new PanelSelectTool(translator,robot);
	  //panes.addTab(translator.get("MenuSelectTool"),panelSelectTool);
  }
  
  public void actionPerformed(ActionEvent e) {
	  Object src = e.getSource();
	  
	  if(src == hardwareVersionChoices) {
		  int newChoice=availableHardwareVersions.get(hardwareVersionChoices.getSelectedIndex());
		  robot.getSettings().setHardwareVersion(newChoice);
		  rebuildTabbedPanes();
	  }
	  if(src == save) {
		  panelAdjustMachine.save();
		  panelAdjustPaper.save();
		  panelAdjustPen.save();
		  robot.getSettings().saveConfig();
		  robot.sendConfig();
		  this.dispose();
	  }
	  if(src == cancel) {
		  robot.getSettings().setHardwareVersion(originalHardwareVersion);
		  this.dispose();
	  }
  }
}
