package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.Scene;
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
	private final Scene scene;
	private final Demo demo;
	
	public DemoAction(Scene scene, Demo demo) {
		super(demo.getName());
        putValue(SHORT_DESCRIPTION, Translator.get("DemoAction.shortDescription"));
		this.scene = scene;
		this.demo = demo;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SceneClearAction action = new SceneClearAction(scene);
		action.clearScene();
		action.addDefaultEntities();
		demo.execute(scene);
	}
}
