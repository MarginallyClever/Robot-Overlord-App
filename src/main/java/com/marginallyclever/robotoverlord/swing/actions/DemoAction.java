package com.marginallyclever.robotoverlord.swing.actions;

import com.marginallyclever.robotoverlord.Project;
import com.marginallyclever.robotoverlord.demos.Demo;
import com.marginallyclever.robotoverlord.swing.translator.Translator;

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

	/**
	 *
	 * @param project Project to load the demo into.
	 * @param demo Demo to load.
	 */
	public DemoAction(Project project, Demo demo) {
		super(demo.getName());
        putValue(SHORT_DESCRIPTION, Translator.get("DemoAction.shortDescription"));
		this.project = project;
		this.demo = demo;
	}

	/**
	 * Load the demo into the project.
	 * @param e the event to be processed
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		ProjectClearAction action = new ProjectClearAction(project);
		action.clearScene();
		action.addDefaultEntities();
		demo.execute(project.getEntityManager());
	}
}
