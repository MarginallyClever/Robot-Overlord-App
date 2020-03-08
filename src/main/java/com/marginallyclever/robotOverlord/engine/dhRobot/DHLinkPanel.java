package com.marginallyclever.robotOverlord.engine.dhRobot;

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandSelectNumber;

/**
 * Subsection of the DHPanel dealing with a single link in the kinematic chain of a DHRobot.
 * @author Dan Royer
 *
 */
public class DHLinkPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// the DHLink referenced by this DHLinkPanel
	public DHLink link;

	// the displayed value for link.d
	public UserCommandSelectNumber d;

	// the displayed value for link.theta
	public UserCommandSelectNumber theta;

	// the displayed value for link.r
	public UserCommandSelectNumber r;

	// the displayed value for link.alpha
	public UserCommandSelectNumber alpha;
	
	public JLabel valueD;
	public JLabel valueTheta;
	public JLabel valueR;
	public JLabel valueAlpha;
	
	public DHLinkPanel(RobotOverlord gui,DHLink link,int k) {
		setup(gui,link,Integer.toString(k));
	}

	public DHLinkPanel(RobotOverlord gui,DHLink link,String linkName) {
		setup(gui,link,linkName);
	}
	public DHLinkPanel(RobotOverlord gui,DHLink link) {
		setup(gui,link,null);
	}
	
	protected void setup(RobotOverlord gui,DHLink link,String linkName) {
		this.link=link;
		if(linkName==null || linkName.trim().isEmpty()) {
			String name="DHLink";
			if(!link.getLetter().isEmpty())
				name += " "+link.getLetter();
				
			this.setName(name);
		} else {
			this.setName("DHLink "+linkName);
		}
		
		d     		= new UserCommandSelectNumber(gui,linkName+" d"		,(float)link.getD()		);
		theta 		= new UserCommandSelectNumber(gui,linkName+" theta"	,(float)link.getTheta()	);
		r     		= new UserCommandSelectNumber(gui,linkName+" r"		,(float)link.getR()		);
		alpha 		= new UserCommandSelectNumber(gui,linkName+" alpha"	,(float)link.getAlpha()	);

		valueD     = new JLabel(StringHelper.formatDouble(link.getD()		),JLabel.RIGHT);
		valueTheta = new JLabel(StringHelper.formatDouble(link.getTheta()	),JLabel.RIGHT);
		valueR     = new JLabel(StringHelper.formatDouble(link.getR()		),JLabel.RIGHT);
		valueAlpha = new JLabel(StringHelper.formatDouble(link.getAlpha()	),JLabel.RIGHT);

		Dimension size = valueD.getPreferredSize();
		size.width = 60;
		valueD		.setPreferredSize(size);
		valueTheta	.setPreferredSize(size);
		valueR		.setPreferredSize(size);
		valueAlpha	.setPreferredSize(size);
		
		d		.setReadOnly((link.flags & DHLink.READ_ONLY_D		)!=0);
		theta	.setReadOnly((link.flags & DHLink.READ_ONLY_THETA	)!=0);
		r		.setReadOnly((link.flags & DHLink.READ_ONLY_R		)!=0);
		alpha	.setReadOnly((link.flags & DHLink.READ_ONLY_ALPHA	)!=0);
	}
};