package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.demos.Demo;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Load a programmed demo.
 * @author Dan Royer
 *
 */
public class DemoAction extends AbstractAction implements ActionListener {
	private final RobotOverlord ro;
	private final Demo demo;
	
	public DemoAction(RobotOverlord ro,Demo demo) {
		super(demo.getName());
        putValue(SHORT_DESCRIPTION, Translator.get("DemoAction.shortDescription"));
		this.ro = ro;
		this.demo = demo;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SceneClearAction action = new SceneClearAction(ro);
		action.clearScene();
		action.addDefaultEntities();
		demo.execute(ro);
	}
}
