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
		
		buildPanel();
	}
	
	protected void buildPanel() {
		this.removeAll();
		this.setBorder(new EmptyBorder(0,0,0,0));
		this.setLayout(new GridBagLayout());
		GridBagConstraints con1 = PanelHelper.getDefaultGridBagConstraints();

		this.add(endx=new JLabel("X="), con1);	con1.gridy++;
		this.add(endy=new JLabel("Y="), con1);	con1.gridy++;
		this.add(endz=new JLabel("Z="), con1);	con1.gridy++;
	}
	
	
	@Override
	public void stateChanged(ChangeEvent event) {
		// TODO fill me
	}

	
	protected String formatDouble(double arg0) {
		//return Float.toString(roundOff(arg0));
		return String.format("%.3f", arg0);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

}
