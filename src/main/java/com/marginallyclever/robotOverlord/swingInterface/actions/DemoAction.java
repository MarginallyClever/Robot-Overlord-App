package com.marginallyclever.robotOverlord.swingInterface.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.demos.Demo;
import com.marginallyclever.robotOverlord.swingInterface.translator.Translator;

/**
 * Load a programmed demo.
 * @author Dan Royer
 *
 */
public class DemoAction extends AbstractAction implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected RobotOverlord ro;
	protected Demo demo;
	
	public DemoAction(RobotOverlord ro,Demo demo) {
		super(demo.getName());
        putValue(SHORT_DESCRIPTION, Translator.get("Open a demo."));
		this.ro = ro;
		this.demo = demo;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		demo.execute(ro);
		ro.updateEntityTree();
	}
}
