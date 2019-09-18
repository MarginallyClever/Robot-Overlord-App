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
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.SpringUtilities;
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
	public JLabel activeTool,gcodeLabel;
	public JButton buttonCommit;
	public JButton buttonSetTool;
	public JTextField gcodeValue;
	
	public JCheckBox showBones;
	public JCheckBox showAngleMinMax;
	public JCheckBox showPhysics;
	public JCheckBox rotateOnWorldAxies;
	public JCheckBox immediateDriving;
	
	public UserCommandSelectNumber x,y,z,rx,ry,rz;
	public JLabel valuex,valuey,valuez,valuerx,valuery,valuerz;
	
	
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
		
		SpringLayout layout = new SpringLayout();
		JPanel linkContents = new JPanel(layout);
		linkContents.setBorder(new BevelBorder(BevelBorder.LOWERED));
		int k=0;
		Iterator<DHLink> i = robot.links.iterator();
		while(i.hasNext()) {
			DHLink link = i.next();
			DHLinkPanel e = new DHLinkPanel(ro,link,k++);
			linkPanels.add(e);

			if((link.flags & DHLink.READ_ONLY_D		)==0) {	linkContents.add(e.d    );	linkContents.add(e.valueD    );	e.d    .addChangeListener(this);	}
			if((link.flags & DHLink.READ_ONLY_THETA	)==0) {	linkContents.add(e.theta);	linkContents.add(e.valueTheta);	e.theta.addChangeListener(this);	}
			if((link.flags & DHLink.READ_ONLY_R		)==0) {	linkContents.add(e.r    );	linkContents.add(e.valueR    );	e.r    .addChangeListener(this);	}
			if((link.flags & DHLink.READ_ONLY_ALPHA	)==0) {	linkContents.add(e.alpha);	linkContents.add(e.valueAlpha);	e.alpha.addChangeListener(this);	}
		}
		SpringUtilities.makeCompactGrid(linkContents, linkContents.getComponentCount()/2, 2, 2, 2, 2, 2);
		contents.add(linkContents,con1);
		con1.gridy++;

		buttonCommit=new JButton("Commit");
		//contents.add(buttonCommit,con1);
		buttonCommit.addItemListener(this);
		con1.gridy++;

		layout = new SpringLayout();
		linkContents = new JPanel(layout);
		linkContents.add(valuex=new JLabel(StringHelper.formatDouble(0),JLabel.RIGHT));
		linkContents.add(valuey=new JLabel(StringHelper.formatDouble(0),JLabel.RIGHT));
		linkContents.add(valuez=new JLabel(StringHelper.formatDouble(0),JLabel.RIGHT));
		linkContents.add(valuerx=new JLabel(StringHelper.formatDouble(0),JLabel.RIGHT));
		linkContents.add(valuery=new JLabel(StringHelper.formatDouble(0),JLabel.RIGHT));
		linkContents.add(valuerz=new JLabel(StringHelper.formatDouble(0),JLabel.RIGHT));
		SpringUtilities.makeCompactGrid(linkContents, 2, 3, 2, 2, 2, 2);
		contents.add(linkContents,con1);
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
		
		contents.add(immediateDriving=new JCheckBox(),con1);
		immediateDriving.setText("Immediate driving");
		immediateDriving.addItemListener(this);
		immediateDriving.setSelected(robot.immediateDriving);
		con1.gridy++;
		
		//this.add(toggleATC=new JButton(robot.dhTool!=null?"ATC close":"ATC open"), con1);
		contents.add(buttonSetTool=new JButton("Set tool"), con1);
		buttonSetTool.addActionListener(this);
		con1.gridy++;
		
		contents.add(activeTool=new JLabel("Tool=") ,con1);  con1.gridy++; 
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
			if(source == e.d    ) isDirty=true;
			if(source == e.theta) isDirty=true;
			if(source == e.r    ) isDirty=true;
			if(source == e.alpha) isDirty=true;
		}
		if(isDirty) {
			i = linkPanels.iterator();
			
			double [] arr = new double[linkPanels.size()*4];
			int j=0;
			
			while(i.hasNext()) {
				DHLinkPanel e = i.next();
				arr[j++]=e.link.d    ;
				arr[j++]=e.link.theta;
				arr[j++]=e.link.r    ;
				arr[j++]=e.link.alpha;
				e.link.d = e.d.getValue();
				e.link.theta = e.theta.getValue();
				e.link.r = e.r.getValue();
				e.link.alpha = e.alpha.getValue();
			}
			robot.refreshPose();
			robot.interpolationQueue.offer(robot.getLiveMatrix());
			j=0;
			i = linkPanels.iterator();
			while(i.hasNext()) {
				DHLinkPanel e = i.next();
				e.link.d    =arr[j++];
				e.link.theta=arr[j++];
				e.link.r    =arr[j++];
				e.link.alpha=arr[j++];
			}
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
		Matrix3d m = new Matrix3d();
		robot.liveMatrix.get(m);
		Vector3d v = MatrixHelper.matrixToEuler(m);
		valuex.setText("X="+StringHelper.formatDouble(robot.liveMatrix.m03));
		valuey.setText("Y="+StringHelper.formatDouble(robot.liveMatrix.m13));
		valuez.setText("Z="+StringHelper.formatDouble(robot.liveMatrix.m23));
		valuerx.setText("Rx="+StringHelper.formatDouble(Math.toDegrees(v.x)));
		valuery.setText("Ry="+StringHelper.formatDouble(Math.toDegrees(v.y)));
		valuerz.setText("Rz="+StringHelper.formatDouble(Math.toDegrees(v.z)));
		gcodeValue.setText(robot.generateGCode());
		
		// report the keyframe results here
		int j=0;
		Iterator<DHLinkPanel> i = linkPanels.iterator();
		while(i.hasNext()) {
			DHLink link = robot.getLink(j++);
			DHLinkPanel linkPanel = i.next();
			if((linkPanel.link.flags & DHLink.READ_ONLY_D		)==0) linkPanel.valueD    .setText(StringHelper.formatDouble(link.d    ));
			if((linkPanel.link.flags & DHLink.READ_ONLY_THETA	)==0) linkPanel.valueTheta.setText(StringHelper.formatDouble(link.theta));
			if((linkPanel.link.flags & DHLink.READ_ONLY_R		)==0) linkPanel.valueR    .setText(StringHelper.formatDouble(link.r    ));
			if((linkPanel.link.flags & DHLink.READ_ONLY_ALPHA	)==0) linkPanel.valueAlpha.setText(StringHelper.formatDouble(link.alpha));
		}
	}
	
	public void updateGhostEnd() {
		// TODO adjust the desired angles values to match ghost angle values 
		// I don't currently store the ghost values when they are calculated,
		// which is becoming a problem.
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
		if(source == buttonCommit) {
			robot.moveToTargetPose();
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
			if(robot.getCurrentTool()!=null 
					&& lft.getClass() == robot.getCurrentTool().getClass()) {
				additionComboBox.setSelectedIndex(loadedTypes);
			}
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
			robot.setShowAnglesPassive(((JCheckBox)source).isSelected());
		}
		if(source == showPhysics) {
			robot.setShowPhysicsPassive(((JCheckBox)source).isSelected());
		}
		if(source == showBones) {
			robot.setShowBonesPassive(((JCheckBox)source).isSelected());
		}
		if(source == rotateOnWorldAxies) {
			robot.rotateOnWorldAxies = !robot.rotateOnWorldAxies;
		}
		if(source == immediateDriving) {
			robot.immediateDriving = !robot.immediateDriving;
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
