package com.marginallyclever.robotOverlord.engine.dhRobot;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.ServiceLoader;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
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

	@Deprecated
	public UserCommandSelectNumber numLinks;
	
	public ArrayList<DHLinkPanel> linkPanels;
	
	// select a tool for the sim.  later this may be open/close ATC.
	public JButton buttonSetTool;
	// placeholder for the active tool's panel.
	public JPanel activeToolPanel;
	
	public UserCommandSelectNumber x,y,z,rx,ry,rz;
	public JLabel pX, pY, pZ, rX, rY, rZ;

	
	
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

		GridBagConstraints con1 = PanelHelper.getDefaultGridBagConstraints();
		
		SpringLayout layout = new SpringLayout();
		JPanel linkContents = new JPanel(layout);
		linkContents.add(pX=new JLabel(StringHelper.formatDouble(0),JLabel.RIGHT));
		linkContents.add(pY=new JLabel(StringHelper.formatDouble(0),JLabel.RIGHT));
		linkContents.add(pZ=new JLabel(StringHelper.formatDouble(0),JLabel.RIGHT));
		linkContents.add(rX=new JLabel(StringHelper.formatDouble(0),JLabel.RIGHT));
		linkContents.add(rY=new JLabel(StringHelper.formatDouble(0),JLabel.RIGHT));
		linkContents.add(rZ=new JLabel(StringHelper.formatDouble(0),JLabel.RIGHT));
		SpringUtilities.makeCompactGrid(linkContents, 2, 3, 2, 2, 2, 2);
		con1.gridy++;
		this.add(linkContents,con1);
		

		con1.gridy++;
		//this.add(toggleATC=new JButton(robot.dhTool!=null?"ATC close":"ATC open"), con1);
		this.add(buttonSetTool=new JButton("Set tool"), con1);
		buttonSetTool.addActionListener(this);
		
		con1.gridy++;
		this.add(activeToolPanel=new JPanel(),con1);

		PanelHelper.ExpandLastChild(this, con1);
		
		robot.refreshPose();
		updateEnd();

		if(robot.getCurrentTool()!=null && activeToolPanel!=null) {
			ArrayList<JPanel> list = robot.getCurrentTool().getContextPanels(ro);
			PanelHelper.formatEntityPanels(list, activeToolPanel);
		}
	}
	
	@Override
	public void stateChanged(ChangeEvent event) {
		//Object source = event.getSource();
	}
	
	/**
	 * Pull the latest arm end world coordinates into the panel.
	 */
	public void updateEnd() {
		// report end effector position
		Matrix3d m = new Matrix3d();
		robot.endEffectorMatrix.get(m);
		Vector3d v = MatrixHelper.matrixToEuler(m);
		pX.setText("X="+StringHelper.formatDouble(robot.endEffectorMatrix.m03));
		pY.setText("Y="+StringHelper.formatDouble(robot.endEffectorMatrix.m13));
		pZ.setText("Z="+StringHelper.formatDouble(robot.endEffectorMatrix.m23));
		rX.setText("Rx="+StringHelper.formatDouble(Math.toDegrees(v.x)));
		rY.setText("Ry="+StringHelper.formatDouble(Math.toDegrees(v.y)));
		rZ.setText("Rz="+StringHelper.formatDouble(Math.toDegrees(v.z)));
		
		// report the keyframe results here
		int j=0;
		for( DHLinkPanel linkPanel : linkPanels ) {
			DHLink link = robot.getLink(j++);
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
			// TODO get the tool from somewhere?  Find the tool in the world adjacent to the end effector?
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
		for( DHTool lft : loaders ) {
			additionComboBox.addItem(lft.getName());
			if(robot.getCurrentTool() != null && lft.getClass() == robot.getCurrentTool().getClass()) {
				additionComboBox.setSelectedIndex(loadedTypes);
			}
			++loadedTypes;
		}
		
		assert(loadedTypes!=0);

        
		int result = JOptionPane.showConfirmDialog(ro.getMainFrame(), additionList, "Set tool...", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			String objectTypeName = additionComboBox.getItemAt(additionComboBox.getSelectedIndex());

			for( DHTool lft : loaders ) {
				if(lft.getName().equals(objectTypeName)) {
					DHTool newInstance = null;

					if(robot.getCurrentTool()!=null 
						&& lft.getClass() == robot.getCurrentTool().getClass()) {
						// we're already using that tool.
						return;
					}
					try {
						newInstance = lft.getClass().newInstance();
						// create an undoable command to set the tool.
						ro.undoableEditHappened(new UndoableEditEvent(this,new UndoableActionSetDHTool(robot,newInstance) ) );
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
		activeToolPanel.removeAll();
		ro.updateEntityTree();
		if(arg0!=null) {
			ArrayList<JPanel> list = arg0.getContextPanels(ro);
			PanelHelper.formatEntityPanels(list, activeToolPanel);
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// for checkboxes
		//Object source = e.getItemSelectable();
	}
}
