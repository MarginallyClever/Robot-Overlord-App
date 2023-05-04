package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.EntityManager;
import com.marginallyclever.robotoverlord.Project;
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
	private final Project project;
	private final Demo demo;
	
	public DemoAction(Project project, Demo demo) {
		super(demo.getName());
        putValue(SHORT_DESCRIPTION, Translator.get("DemoAction.shortDescription"));
		this.project = project;
		this.demo = demo;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ProjectClearAction action = new ProjectClearAction(project);
		action.clearScene();
		action.addDefaultEntities();
		demo.execute(project.getEntityManager());
	}
}
