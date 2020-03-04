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

	public DHLinkPanel linkPanel;
	public JLabel endx,endy,endz;
	
	
	public DHToolPanel(RobotOverlord gui,DHTool tool) {
		this.tool = tool;
		this.gui = gui;
		linkPanel = new DHLinkPanel(gui,tool,tool.getName());
		
		buildPanel();
	}
	
	protected void buildPanel() {
		this.removeAll();
		this.setBorder(new EmptyBorder(0,0,0,0));
		this.setLayout(new GridBagLayout());
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=1;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		//con1.anchor=GridBagConstraints.CENTER;

		//this.add(numLinks = new UserCommandSelectNumber(gui,"# links",robot.links.size()),con1);
		//con1.gridy++;
		//numLinks.addChangeListener(this);
		
		//this.add(new JSeparator(JSeparator.VERTICAL), con1);
		//con1.gridy++;
		
		int k=0;
		DHLinkPanel e = new DHLinkPanel(gui,tool,k++);
		linkPanel = e;

		if((tool.flags & DHLink.READ_ONLY_D		)==0) {	this.add(e.d    ,con1);		con1.gridy++;	e.d    .addChangeListener(this);	}
		if((tool.flags & DHLink.READ_ONLY_THETA	)==0) {	this.add(e.theta,con1);		con1.gridy++;	e.theta.addChangeListener(this);	}
		if((tool.flags & DHLink.READ_ONLY_R		)==0) {	this.add(e.r    ,con1);		con1.gridy++;	e.r    .addChangeListener(this);	}
		if((tool.flags & DHLink.READ_ONLY_ALPHA	)==0) {	this.add(e.alpha,con1);		con1.gridy++;	e.alpha.addChangeListener(this);	}
		
		this.add(endx=new JLabel("X="), con1);	con1.gridy++;
		this.add(endy=new JLabel("Y="), con1);	con1.gridy++;
		this.add(endz=new JLabel("Z="), con1);	con1.gridy++;
	}
	
	
	@Override
	public void stateChanged(ChangeEvent event) {
		Object source = event.getSource();
		boolean isDirty=false;
		{
			DHLinkPanel e = linkPanel;
			if(source == e.d) {
				e.link.setD(e.d.getValue());
				isDirty=true;
			}
			if(source == e.theta) {
				e.link.setTheta(e.theta.getValue());
				isDirty=true;
			}
			if(source == e.r) {
				e.link.setR(e.r.getValue());
				isDirty=true;
			}
			if(source == e.alpha) {
				e.link.setAlpha(e.alpha.getValue());
				isDirty=true;
			}
		}
		if(isDirty) {
			//tool.refreshPose();
		}
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
