package com.marginallyclever.robotOverlord.dhRobot;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.commands.UserCommandSelectBoolean;
import com.marginallyclever.robotOverlord.commands.UserCommandSelectNumber;

/**
 * Control Panel for a DH Robot
 * @author Dan Royer
 *
 */
public class DHPanel extends JPanel implements ActionListener, ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	class DHLinkPanel {
		public DHLink link;
		public UserCommandSelectBoolean isRotation;
		public UserCommandSelectNumber d;
		public UserCommandSelectNumber theta;
		public UserCommandSelectNumber r;
		public UserCommandSelectNumber alpha;
		
		public DHLinkPanel(DHLink link,int k) {
			this.link=link;
			isRotation = new UserCommandSelectBoolean(gui,k+" Rotation?",true);
			d     = new UserCommandSelectNumber(gui,k+" d",(float)link.d);
			theta = new UserCommandSelectNumber(gui,k+" theta",(float)link.theta);
			r     = new UserCommandSelectNumber(gui,k+" r",(float)link.r);
			alpha = new UserCommandSelectNumber(gui,k+" alpha",(float)link.alpha);
		}
	};
	
	protected DHRobot robot;
	protected RobotOverlord gui;

	public UserCommandSelectNumber numLinks;
	public ArrayList<DHLinkPanel> linkPanels;
	public JLabel endx,endy,endz;
	
	
	public DHPanel(RobotOverlord gui,DHRobot robot) {
		this.robot = robot;
		this.gui = gui;
		linkPanels = new ArrayList<DHLinkPanel>();
		
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

		this.add(numLinks = new UserCommandSelectNumber(gui,"links",robot.links.size()),con1);
		con1.gridy++;
		
		numLinks.addChangeListener(this);
		this.add(new JSeparator(JSeparator.VERTICAL), con1);
		con1.gridy++;
		
		int k=0;
		Iterator<DHLink> i = robot.links.iterator();
		while(i.hasNext()) {
			DHLink link = i.next();
			DHLinkPanel e = new DHLinkPanel(link,k++);
			linkPanels.add(e);

			this.add(e.d    ,con1);		con1.gridy++;	e.d    .addChangeListener(this);
			this.add(e.theta,con1);		con1.gridy++;	e.theta.addChangeListener(this);
			this.add(e.r    ,con1);		con1.gridy++;	e.r    .addChangeListener(this);
			this.add(e.alpha,con1);		con1.gridy++;	e.alpha.addChangeListener(this);
		}
		
		this.add(endx=new JLabel("X="), con1);	con1.gridy++;
		this.add(endy=new JLabel("Y="), con1);	con1.gridy++;
		this.add(endz=new JLabel("Z="), con1);	con1.gridy++;
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
		Iterator<DHLinkPanel> i = linkPanels.iterator();
		while(i.hasNext()) {
			DHLinkPanel e = i.next();
			if(source == e.d) {
				e.link.d = e.d.getValue();
				robot.refreshPose();
				updateEnd();
			}
			if(source == e.theta) {
				e.link.theta = e.theta.getValue();
				robot.refreshPose();
				updateEnd();
			}
			if(source == e.r) {
				e.link.r =  e.r.getValue();
				robot.refreshPose();
				updateEnd();
			}
			if(source == e.alpha) {
				e.link.alpha =  e.alpha.getValue();
				robot.refreshPose();
				updateEnd();
			}
		}
	}

	
	protected String formatDouble(double arg0) {
		//return Float.toString(roundOff(arg0));
		return String.format("%.3f", arg0);
	}
	
	/**
	 * Pull the latest arm end world coordinates into the panel.
	 */
	protected void updateEnd() {
		endx.setText("X="+formatDouble(robot.end.x));
		endy.setText("Y="+formatDouble(robot.end.y));
		endz.setText("Z="+formatDouble(robot.end.z));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

}
