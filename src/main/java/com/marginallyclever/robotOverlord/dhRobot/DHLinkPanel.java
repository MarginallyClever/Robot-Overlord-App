package com.marginallyclever.robotOverlord.dhRobot;

import java.awt.Dimension;

import javax.swing.JLabel;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.commands.UserCommandSelectNumber;

/**
 * Subsection of the DHPanel dealing with a single link in the kinematic chain of a DHRobot.
 * @author Dan Royer
 *
 */
public class DHLinkPanel {
	/**
	 * {@value #link} the DHLink referenced by this DHLinkPanel
	 */
	public DHLink link;

	/**
	 * {@value #d} the displayed value for link.d
	 */
	public UserCommandSelectNumber d;

	/**
	 * {@value #theta} the displayed value for link.theta
	 */
	public UserCommandSelectNumber theta;

	/**
	 * {@value #r} the displayed value for link.r
	 */
	public UserCommandSelectNumber r;

	/**
	 * {@value #alpha} the displayed value for link.alpha
	 */
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
	
	protected void setup(RobotOverlord gui,DHLink link,String linkName) {
		this.link=link;
		d     		= new UserCommandSelectNumber (gui,linkName+" d"		,(float)link.d		);
		theta 		= new UserCommandSelectNumber (gui,linkName+" theta"	,(float)link.theta	);
		r     		= new UserCommandSelectNumber (gui,linkName+" r"		,(float)link.r		);
		alpha 		= new UserCommandSelectNumber (gui,linkName+" alpha"	,(float)link.alpha	);

		valueD     = new JLabel(StringHelper.formatDouble(link.d    ),JLabel.RIGHT);
		valueTheta = new JLabel(StringHelper.formatDouble(link.theta),JLabel.RIGHT);
		valueR     = new JLabel(StringHelper.formatDouble(link.r    ),JLabel.RIGHT);
		valueAlpha = new JLabel(StringHelper.formatDouble(link.alpha),JLabel.RIGHT);

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