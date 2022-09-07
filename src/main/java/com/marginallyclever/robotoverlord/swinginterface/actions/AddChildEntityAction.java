package com.marginallyclever.robotoverlord.swinginterface.actions;

import java.awt.event.ActionEvent;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.EntityFactory;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;
import com.marginallyclever.robotoverlord.swinginterface.undoableedits.AddEntityEdit;

/**
 * Display an Add Entity dialog box.  If an entity is selected and "ok" is pressed, add that Entity to the world. 
 * @author Dan Royer
 *
 */
public class AddChildEntityAction extends AbstractAction {
	@Serial
	private static final long serialVersionUID = 1L;
	protected RobotOverlord ro;
	
	public AddChildEntityAction(RobotOverlord ro) {
		super(Translator.get("Add Entity"));
        putValue(SHORT_DESCRIPTION, Translator.get("Add an entity to the world."));
		this.ro = ro;
	}
	
    /**
     * select from a list of all object types.  An instance of that type is then added to the world.
     */
	@Override
	public void actionPerformed(ActionEvent event) {
		List<Entity> list = ro.getSelectedEntities();

		JComboBox<String> additionComboBox = buildEntityComboBox();
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
			ro.updateEntityTree();
		}
    }

	private JComboBox<String> buildEntityComboBox() {
		JComboBox<String> box = new JComboBox<>();
		ArrayList<String> names = EntityFactory.getAllEntityNames();
		for( String n : names ) box.addItem(n);
		return box;
	}

	private void createInstanceOf(Entity parent,String className) {
		try {
			Entity newInstance = EntityFactory.load(className);
			if(newInstance != null) UndoSystem.addEvent(this,new AddEntityEdit(parent,newInstance));
		} catch (Exception e) {
			String msg = "Failed to instance "+className+": "+e.getLocalizedMessage();
			JOptionPane.showMessageDialog(ro.getMainFrame(),msg);
			Log.error(msg);
		}
	}
}
