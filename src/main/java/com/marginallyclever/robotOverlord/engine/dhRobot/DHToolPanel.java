package com.marginallyclever.robotOverlord.engine.dhRobot;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.marginallyclever.convenience.PanelHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;

/**
 * Control Panel for a DHTool
 * @author Dan Royer
 *
 */
public class DHToolPanel extends JPanel implements ActionListener, ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	protected DHTool tool;
	protected RobotOverlord gui;

	public JLabel endx,endy,endz;
	
	
	public DHToolPanel(RobotOverlord gui,DHTool tool) {
		this.tool = tool;
		this.gui = gui;
		
		this.setName("DHTool");
		
		this.setLayout(new GridBagLayout());
		this.setBorder(new EmptyBorder(5,5,5,5));
		
		GridBagConstraints con1 = PanelHelper.getDefaultGridBagConstraints();

		con1.gridy++;
		this.add(endx=new JLabel("X="), con1);
		con1.gridy++;
		this.add(endy=new JLabel("Y="), con1);
		con1.gridy++;
		this.add(endz=new JLabel("Z="), con1);
		
		PanelHelper.ExpandLastChild(this, con1);
	}
	
	
	@Override
	public void stateChanged(ChangeEvent event) {
		// TODO fill me
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

}
