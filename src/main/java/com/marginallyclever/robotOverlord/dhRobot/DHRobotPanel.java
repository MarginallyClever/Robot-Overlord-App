package com.marginallyclever.robotOverlord.dhRobot;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
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
import java.util.Iterator;
import java.util.ServiceLoader;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.CollapsiblePanel;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.actions.UndoableActionSetDHTool;
import com.marginallyclever.robotOverlord.commands.UserCommandSelectNumber;

/**
 * Control Panel for a DHRobot
 * @author Dan Royer
 *
 */
public class DHRobotPanel extends JPanel implements ActionListener, ChangeListener, ItemListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	protected DHRobot robot;
	protected RobotOverlord ro;

	public UserCommandSelectNumber numLinks;
	public ArrayList<DHLinkPanel> linkPanels;
	public JLabel endx,endy,endz,activeTool,gcodeLabel;
	public JButton buttonSetTool;
	public JTextField gcodeValue;
	
	public JCheckBox showBones;
	public JCheckBox showAngleMinMax;
	public JCheckBox showPhysics;
	public JCheckBox rotateOnWorldAxies;
	
	
	public DHRobotPanel(RobotOverlord gui,DHRobot robot) {
		this.robot = robot;
		this.ro = gui;
		linkPanels = new ArrayList<DHLinkPanel>();
		
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

		CollapsiblePanel oiwPanel = new CollapsiblePanel("DHRobot");
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

		//this.add(numLinks = new UserCommandSelectNumber(gui,"# links",robot.links.size()),con1);
		//con1.gridy++;
		//numLinks.addChangeListener(this);

		contents.add(new JSeparator(JSeparator.VERTICAL), con1);
		con1.gridy++;
		
		contents.add(showBones=new JCheckBox(),con1);
		showBones.setText("Show D-H bones");
		showBones.addItemListener(this);
		showBones.setSelected(robot.isShowBones());
		con1.gridy++;
		
		contents.add(showAngleMinMax=new JCheckBox(),con1);
		showAngleMinMax.setText("Show angle min/max");
		showAngleMinMax.addItemListener(this);
		showAngleMinMax.setSelected(robot.isShowAngles());
		con1.gridy++;
		
		contents.add(showPhysics=new JCheckBox(),con1);
		showPhysics.setText("Show physics model");
		showPhysics.addItemListener(this);
		showPhysics.setSelected(robot.isShowPhysics());
		con1.gridy++;
		
		contents.add(rotateOnWorldAxies=new JCheckBox(),con1);
		rotateOnWorldAxies.setText("Absolute rotation");
		rotateOnWorldAxies.addItemListener(this);
		rotateOnWorldAxies.setSelected(robot.rotateOnWorldAxies);
		con1.gridy++;
		
		
		
		contents.add(new JSeparator(JSeparator.VERTICAL), con1);
		con1.gridy++;
		
		int k=0;
		Iterator<DHLink> i = robot.links.iterator();
		while(i.hasNext()) {
			DHLink link = i.next();
			DHLinkPanel e = new DHLinkPanel(ro,link,k++);
			linkPanels.add(e);

			if((link.flags & DHLink.READ_ONLY_D		)==0) {	contents.add(e.d    ,con1);		con1.gridy++;	e.d    .addChangeListener(this);	}
			if((link.flags & DHLink.READ_ONLY_THETA	)==0) {	contents.add(e.theta,con1);		con1.gridy++;	e.theta.addChangeListener(this);	}
			if((link.flags & DHLink.READ_ONLY_R		)==0) {	contents.add(e.r    ,con1);		con1.gridy++;	e.r    .addChangeListener(this);	}
			if((link.flags & DHLink.READ_ONLY_ALPHA	)==0) {	contents.add(e.alpha,con1);		con1.gridy++;	e.alpha.addChangeListener(this);	}
		}
		
		//this.add(toggleATC=new JButton(robot.dhTool!=null?"ATC close":"ATC open"), con1);
		contents.add(buttonSetTool=new JButton("Set tool"), con1);	con1.gridy++;
		buttonSetTool.addActionListener(this);
		
		contents.add(activeTool=new JLabel("Tool=") ,con1);  con1.gridy++; 
		contents.add(endx=new JLabel("X="), con1);	con1.gridy++;
		contents.add(endy=new JLabel("Y="), con1);	con1.gridy++;
		contents.add(endz=new JLabel("Z="), con1);	con1.gridy++;
		contents.add(gcodeLabel=new JLabel("Gcode"), con1); con1.gridy++;
		contents.add(gcodeValue=new JTextField(),con1); con1.gridy++;
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

		robot.refreshPose();
		updateEnd();
	}
	
	@Override
	public void stateChanged(ChangeEvent event) {
		Object source = event.getSource();
		if(source == numLinks) {
			int newSize = (int)numLinks.getValue();
			if(robot.links.size() != newSize) {
				robot.setNumLinks(newSize);
				buildPanel();
				this.invalidate();
				return;
			}
		}
		boolean isDirty=false;
		Iterator<DHLinkPanel> i = linkPanels.iterator();
		while(i.hasNext()) {
			DHLinkPanel e = i.next();
			if(source == e.d) {
				e.link.d = e.d.getValue();
				isDirty=true;
			}
			if(source == e.theta) {
				e.link.theta = e.theta.getValue();
				isDirty=true;
			}
			if(source == e.r) {
				e.link.r = e.r.getValue();
				isDirty=true;
			}
			if(source == e.alpha) {
				e.link.alpha = e.alpha.getValue();
				isDirty=true;
			}
		}
		if(isDirty) {
			robot.refreshPose();
			updateEnd();
		}
	}
	
	/**
	 * Pull the latest arm end world coordinates into the panel.
	 */
	public void updateEnd() {
		updateActiveTool(robot.getCurrentTool());
		// report end effector position
		endx.setText("X="+StringHelper.formatDouble(robot.endMatrix.m03));
		endy.setText("Y="+StringHelper.formatDouble(robot.endMatrix.m13));
		endz.setText("Z="+StringHelper.formatDouble(robot.endMatrix.m23));
		endx.setText("Rx="+StringHelper.formatDouble(robot.endMatrix.m03));
		endy.setText("Ry="+StringHelper.formatDouble(robot.endMatrix.m13));
		endz.setText("Rz="+StringHelper.formatDouble(robot.endMatrix.m23));
		gcodeValue.setText(robot.generateGCode());
		
		// run the IK solver to see if solution works.
		//DHIKSolver solver = robot.getSolverIK();
		//DHKeyframe keyframe = (DHKeyframe)robot.createKeyframe();
		//solver.solve(robot,robot.endMatrix,keyframe);

		// report the keyframe results here
		Iterator<DHLinkPanel> i = linkPanels.iterator();
		while(i.hasNext()) {
			DHLinkPanel linkPanel = i.next();
			if((linkPanel.link.flags & DHLink.READ_ONLY_D		)==0) linkPanel.d    .setValue((float)linkPanel.link.d    ,false);
			if((linkPanel.link.flags & DHLink.READ_ONLY_THETA	)==0) linkPanel.theta.setValue((float)linkPanel.link.theta,false);
			if((linkPanel.link.flags & DHLink.READ_ONLY_R		)==0) linkPanel.r    .setValue((float)linkPanel.link.r    ,false);
			if((linkPanel.link.flags & DHLink.READ_ONLY_ALPHA	)==0) linkPanel.alpha.setValue((float)linkPanel.link.alpha,false);
		}
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source == buttonSetTool) {
			// TODO get the tool from somewhere?  Find the tool in the world adjacent to the end effector
			selectTool();
			
			//robot.toggleATC();
			buildPanel();
			this.invalidate();
		}
	}

	/**
	 * Called when user clicks button to change the tool.  Does not update the panel status.
	 */
	public void selectTool() {
		JPanel additionList = new JPanel(new GridLayout(0, 1));
		
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=1;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.NORTH;
		
		JComboBox<String> additionComboBox = new JComboBox<String>();
		additionList.add(additionComboBox);
		
		// service load the types available.
		ServiceLoader<DHTool> loaders = ServiceLoader.load(DHTool.class);
		int loadedTypes=0;
		Iterator<DHTool> i = loaders.iterator();
		while(i.hasNext()) {
			DHTool lft = i.next();
			additionComboBox.addItem(lft.getDisplayName());
			++loadedTypes;
		}
		
		assert(loadedTypes!=0);

        
		int result = JOptionPane.showConfirmDialog(ro.getMainFrame(), additionList, "Set tool...", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			String objectTypeName = additionComboBox.getItemAt(additionComboBox.getSelectedIndex());

			i = loaders.iterator();
			while(i.hasNext()) {
				DHTool lft = i.next();
				String name = lft.getDisplayName();
				if(name.equals(objectTypeName)) {
					DHTool newInstance = null;

					if(robot.getCurrentTool()!=null 
						&& lft.getClass() == robot.getCurrentTool().getClass()) {
						// we're already using that tool.
						return;
					}
					try {
						newInstance = lft.getClass().newInstance();
						// create an undoable command to set the tool.
						ro.getUndoHelper().undoableEditHappened(new UndoableEditEvent(this,new UndoableActionSetDHTool(robot,newInstance) ) );
					} catch (InstantiationException | IllegalAccessException e) {
						e.printStackTrace();
					}
					return;
				}
			}
		}
	}
	
	/**
	 * Called by the robot to update the panel status
	 */
	public void updateActiveTool(DHTool arg0) {
		String name = (arg0==null) ? "null" : arg0.getDisplayName();
		activeTool.setText("Tool="+name);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// for checkboxes
		Object source = e.getItemSelectable();
		if(source == showAngleMinMax) {
			robot.setShowAnglesPassive(!robot.isShowAngles());
		}
		if(source == showPhysics) {
			robot.setShowPhysicsPassive(!robot.isShowPhysics());
		}
		if(source == showBones) {
			robot.setShowBonesPassive(!robot.isShowBones());
		}
		if(source == rotateOnWorldAxies) {
			robot.rotateOnWorldAxies = !robot.rotateOnWorldAxies;
		}
	}

	public void setShowBones(boolean arg0) {
		showBones.setSelected(arg0);
	}

	public void setShowPhysics(boolean arg0) {
		showPhysics.setSelected(arg0);
	}

	public void setShowAngles(boolean arg0) {
		showAngleMinMax.setSelected(arg0);
	}
}
