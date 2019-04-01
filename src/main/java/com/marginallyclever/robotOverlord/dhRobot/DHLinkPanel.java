package com.marginallyclever.robotOverlord.dhRobot;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.commands.UserCommandSelectBoolean;
import com.marginallyclever.robotOverlord.commands.UserCommandSelectNumber;

public class DHLinkPanel {
	public DHLink link;
	public UserCommandSelectBoolean isRotation;
	public UserCommandSelectNumber d;
	public UserCommandSelectNumber theta;
	public UserCommandSelectNumber r;
	public UserCommandSelectNumber alpha;
	
	public DHLinkPanel(RobotOverlord gui,DHLink link,int k) {
		this.link=link;
		isRotation = new UserCommandSelectBoolean(gui,k+" Rotation?",true);
		d     = new UserCommandSelectNumber(gui,k+" d",(float)link.d);
		theta = new UserCommandSelectNumber(gui,k+" theta",(float)link.theta);
		r     = new UserCommandSelectNumber(gui,k+" r",(float)link.r);
		alpha = new UserCommandSelectNumber(gui,k+" alpha",(float)link.alpha);

		d		.setReadOnly((link.flags & DHLink.READ_ONLY_D		)!=0);
		theta	.setReadOnly((link.flags & DHLink.READ_ONLY_THETA	)!=0);
		r		.setReadOnly((link.flags & DHLink.READ_ONLY_R		)!=0);
		alpha	.setReadOnly((link.flags & DHLink.READ_ONLY_ALPHA	)!=0);
	}
};