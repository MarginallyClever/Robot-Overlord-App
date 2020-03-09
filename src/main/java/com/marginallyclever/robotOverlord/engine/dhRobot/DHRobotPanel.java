package com.marginallyclever.robotOverlord.engine.dhRobot;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ServiceLoader;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PanelHelper;
import com.marginallyclever.convenience.SpringUtilities;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.engine.undoRedo.actions.UndoableActionSetDHTool;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandSelectNumber;

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
	
	// select a tool for the sim.  later this may be open/close ATC.
	public JButton buttonSetTool;
	// placeholder for the active tool's panel.
	public JPanel activeTool;
	
	public UserCommandSelectNumber x,y,z,rx,ry,rz;
	public JLabel valuex,valuey,valuez,valuerx,valuery,valuerz;

	
	
	public DHRobotPanel(RobotOverlord gui,DHRobot robot) {
		this.robot = robot;
		this.ro = gui;
		this.setName("DHRobot");
		
		buildPanel();
	}
	
	protected void buildPanel() {
		this.removeAll();
		
		linkPanels = new ArrayList<DHLinkPanel>();
		
		this.setLayout(new GridBagLayout());
		this.setBorder(new EmptyBorder(0,0,0,0));

		GridBagConstraints con1 = PanelHelper.getDefaultGridBagConstraints();
		
		SpringLayout layout = new SpringLayout();
		JPanel linkContents = new JPanel(layout);
		linkContents.setBorder(new BevelBorder(BevelBorder.LOWERED));
		int k=0;
		for( DHLink link : robot.links ) {
			DHLinkPanel e = new DHLinkPanel(ro,link,k++);
			linkPanels.add(e);

			switch(link.flags) {
			case D	  : {	linkContents.add(e.d    );	linkContents.add(e.valueD    );	e.d    .addChangeListener(this);	}  break;
			case THETA: {	linkContents.add(e.theta);	linkContents.add(e.valueTheta);	e.theta.addChangeListener(this);	}  break;
			case R	  : {	linkContents.add(e.r    );	linkContents.add(e.valueR    );	e.r    .addChangeListener(this);	}  break;
			case ALPHA: {	linkContents.add(e.alpha);	linkContents.add(e.valueAlpha);	e.alpha.addChangeListener(this);	}  break;
			default: break;
			}
		}
		SpringUtilities.makeCompactGrid(linkContents, linkContents.getComponentCount()/2, 2, 2, 2, 2, 2);
		this.add(linkContents,con1);

		layout = new SpringLayout();
		linkContents = new JPanel(layout);
		linkContents.add(valuex=new JLabel(StringHelper.formatDouble(0),JLabel.RIGHT));
		linkContents.add(valuey=new JLabel(StringHelper.formatDouble(0),JLabel.RIGHT));
		linkContents.add(valuez=new JLabel(StringHelper.formatDouble(0),JLabel.RIGHT));
		linkContents.add(valuerx=new JLabel(StringHelper.formatDouble(0),JLabel.RIGHT));
		linkContents.add(valuery=new JLabel(StringHelper.formatDouble(0),JLabel.RIGHT));
		linkContents.add(valuerz=new JLabel(StringHelper.formatDouble(0),JLabel.RIGHT));
		SpringUtilities.makeCompactGrid(linkContents, 2, 3, 2, 2, 2, 2);
		con1.gridy++;
		this.add(linkContents,con1);
		

		con1.gridy++;
		//this.add(toggleATC=new JButton(robot.dhTool!=null?"ATC close":"ATC open"), con1);
		this.add(buttonSetTool=new JButton("Set tool"), con1);
		buttonSetTool.addActionListener(this);
		
		con1.gridy++;
		this.add(activeTool=new JPanel(),con1);

		PanelHelper.ExpandLastChild(this, con1);
		
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
				arr[j++]=e.link.getD()		;
				arr[j++]=e.link.getTheta()	;
				arr[j++]=e.link.getR()		;
				arr[j++]=e.link.getAlpha()	;
				e.link.setD		(e.d	.getValue());
				e.link.setTheta	(e.theta.getValue());
				e.link.setR		(e.r	.getValue());
				e.link.setAlpha	(e.alpha.getValue());
			}
			robot.refreshPose();
			j=0;
			i = linkPanels.iterator();
			while(i.hasNext()) {
				DHLinkPanel e = i.next();
				e.link.setD		(arr[j++]);
				e.link.setTheta	(arr[j++]);
				e.link.setR		(arr[j++]);
				e.link.setAlpha	(arr[j++]);
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
		robot.endEffectorMatrix.get(m);
		Vector3d v = MatrixHelper.matrixToEuler(m);
		valuex.setText("X="+StringHelper.formatDouble(robot.endEffectorMatrix.m03));
		valuey.setText("Y="+StringHelper.formatDouble(robot.endEffectorMatrix.m13));
		valuez.setText("Z="+StringHelper.formatDouble(robot.endEffectorMatrix.m23));
		valuerx.setText("Rx="+StringHelper.formatDouble(Math.toDegrees(v.x)));
		valuery.setText("Ry="+StringHelper.formatDouble(Math.toDegrees(v.y)));
		valuerz.setText("Rz="+StringHelper.formatDouble(Math.toDegrees(v.z)));
		
		// report the keyframe results here
		int j=0;
		Iterator<DHLinkPanel> i = linkPanels.iterator();
		while(i.hasNext()) {
			DHLink link = robot.getLink(j++);
			DHLinkPanel linkPanel = i.next();
			switch(linkPanel.link.flags) {
			case D		: linkPanel.valueD    .setText(StringHelper.formatDouble(link.getD()	));  break;
			case THETA	: linkPanel.valueTheta.setText(StringHelper.formatDouble(link.getTheta()));  break;
			case R		: linkPanel.valueR    .setText(StringHelper.formatDouble(link.getR()	));  break;
			case ALPHA	: linkPanel.valueAlpha.setText(StringHelper.formatDouble(link.getAlpha()));  break;
			default: break;
			}
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

		JComboBox<String> additionComboBox = new JComboBox<String>();
		additionList.add(additionComboBox);
		
		// service load the types available.
		ServiceLoader<DHTool> loaders = ServiceLoader.load(DHTool.class);
		int loadedTypes=0;
		Iterator<DHTool> i = loaders.iterator();
		while(i.hasNext()) {
			DHTool lft = i.next();
			additionComboBox.addItem(lft.getName());
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
				String name = lft.getName();
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
		activeTool.removeAll();
		activeTool.add(arg0.getAllContextPanels(ro));
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// for checkboxes
		//Object source = e.getItemSelectable();
	}
}
