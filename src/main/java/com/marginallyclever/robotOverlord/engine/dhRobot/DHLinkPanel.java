package com.marginallyclever.robotOverlord.engine.dhRobot;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.marginallyclever.convenience.PanelHelper;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandSelectNumber;

/**
 * Subsection of the DHPanel dealing with a single link in the kinematic chain of a DHRobot.
 * @author Dan Royer
 *
 */
public class DHLinkPanel extends JPanel implements ChangeListener {
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
	
	public DHLinkPanel(RobotOverlord gui,DHLink link) {
		this.link=link;
		this.setName(link.getName());
		this.setLayout(new GridBagLayout());
		this.setBorder(new EmptyBorder(5,5,5,5));
		
		d     		= new UserCommandSelectNumber(gui,"D"		,(float)link.getD()		);
		theta 		= new UserCommandSelectNumber(gui,"Theta"	,(float)link.getTheta()	);
		r     		= new UserCommandSelectNumber(gui,"R"		,(float)link.getR()		);
		alpha 		= new UserCommandSelectNumber(gui,"Alpha"	,(float)link.getAlpha()	);

		valueD     = new JLabel("D = "+StringHelper.formatDouble(link.getD()		),JLabel.LEFT);
		valueTheta = new JLabel("Theta = "+StringHelper.formatDouble(link.getTheta()	),JLabel.LEFT);
		valueR     = new JLabel("R = "+StringHelper.formatDouble(link.getR()		),JLabel.LEFT);
		valueAlpha = new JLabel("Alpha = "+StringHelper.formatDouble(link.getAlpha()	),JLabel.LEFT);

		Dimension size = valueD.getPreferredSize();
		size.width = 60;
		valueD		.setPreferredSize(size);
		valueTheta	.setPreferredSize(size);
		valueR		.setPreferredSize(size);
		valueAlpha	.setPreferredSize(size);
		
		d		.setReadOnly(link.flags != DHLink.LinkAdjust.D);
		theta	.setReadOnly(link.flags != DHLink.LinkAdjust.THETA);
		r		.setReadOnly(link.flags != DHLink.LinkAdjust.R);
		alpha	.setReadOnly(link.flags != DHLink.LinkAdjust.ALPHA);

		GridBagConstraints con1 = PanelHelper.getDefaultGridBagConstraints();
		con1.gridy++;		this.add(d,con1);
		con1.gridy++;		this.add(theta,con1);
		con1.gridy++;		this.add(r,con1);
		con1.gridy++;		this.add(alpha,con1);
		
		d.addChangeListener(this);
		theta.addChangeListener(this);
		r.addChangeListener(this);
		alpha.addChangeListener(this);

		PanelHelper.ExpandLastChild(this, con1);
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		switch(link.flags) { 
		case D: link.setD(d.getValue());  break;
		case R: link.setR(r.getValue());  break;
		case THETA: link.setTheta(theta.getValue());  break;
		case ALPHA: link.setAlpha(alpha.getValue());  break;
		default:  break;
		}
		link.refreshPoseMatrix();
	}
};