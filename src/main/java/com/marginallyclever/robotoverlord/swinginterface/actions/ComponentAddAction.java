package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.ComponentFactory;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.components.ComponentDependency;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.ComponentAddEdit;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Display an `Add Component` dialog box.  If an {@link Component} is selected and
 * "ok" is pressed, add that Component to the world.
 * @author Dan Royer
 *
 */
public class ComponentAddAction extends AbstractAction {
	protected final RobotOverlord ro;

	public ComponentAddAction(RobotOverlord ro) {
		super(Translator.get("Add Component"));
        putValue(SHORT_DESCRIPTION, Translator.get("Add a component to the world."));
		this.ro = ro;
	}
	
    /**
     * select from a list of all object types.  An instance of that type is then added to the world.
     */
	@Override
	public void actionPerformed(ActionEvent event) {
		List<Entity> list = ro.getSelectedEntities();

		JComboBox<String> additionComboBox = buildComponentComboBox();
		int result = JOptionPane.showConfirmDialog(
				ro.getMainFrame(),
				additionComboBox, 
				(String)this.getValue(AbstractAction.NAME), 
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			for(Entity parent : list) {
				createInstanceOf(parent,additionComboBox.getItemAt(additionComboBox.getSelectedIndex()));
			}
			ro.updateComponentPanel();
		}
    }

	private JComboBox<String> buildComponentComboBox() {
		JComboBox<String> box = new JComboBox<>();
		ArrayList<String> names = ComponentFactory.getAllComponentNames();
		for( String n : names ) box.addItem(n);
		return box;
	}

	private void createInstanceOf(Entity parent,String className) {
		try {
			addComponentDependencies(parent,className);

			Component newInstance = ComponentFactory.load(className);
			if(newInstance != null) UndoSystem.addEvent(this,new ComponentAddEdit(ro,parent,newInstance));
		} catch (Exception e) {
			String msg = "Failed to instance "+className+": "+e.getLocalizedMessage();
			JOptionPane.showMessageDialog(ro.getMainFrame(),msg);
			Log.error(msg);
		}
	}

	private void addComponentDependencies(Entity parent, String className) throws Exception {
		Class<?> myClass = ComponentFactory.getClassFromName(className);
		if (myClass == null) {
			throw new Exception("no class found.");
		}

		recursivelyAddComponentDependencies(parent, myClass);
	}

	private void recursivelyAddComponentDependencies(Entity parent, Class<?> myClass) throws Exception {
		LinkedList<Class<?>> dependencies = new LinkedList<>();
		while (myClass != null) {
			dependencies.push(myClass);
			myClass = myClass.getSuperclass();
		}
		for(Class<?> c : dependencies) {
			addComponentDependencies(parent, c);
		}
	}

	private void addComponentDependencies(Entity parent, Class<?> myClass) throws Exception {
		ComponentDependency [] annotations = myClass.getAnnotationsByType(ComponentDependency.class);
		for (ComponentDependency a : annotations) {
			Class<? extends Component> [] components = a.components();
			for(Class<? extends Component> c : components) {
				if(null==parent.findFirstComponent(c)) {
					Component newInstance = ComponentFactory.createInstance(c);
					if(null != newInstance) {
						UndoSystem.addEvent(this,new ComponentAddEdit(ro,parent,newInstance));
					} else throw new Exception("Failed to instance "+c.getCanonicalName());
				}
			}
		}
	}
}
