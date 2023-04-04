package com.marginallyclever.robotoverlord.swinginterface;

import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Collate all the {@link java.awt.Component}s for selected {@link Entity}s.
 */
public class ComponentPanel extends JPanel {
	private final RobotOverlord robotOverlord;

	public ComponentPanel(RobotOverlord ro) {
		super(new BorderLayout());
		robotOverlord = ro;
	}

	/**
	 * Collate all the {@link java.awt.Component}s for selected {@link Entity}s.
	 * @param entityList the list of entities to collate
	 */
	public void refreshContents(List<Entity> entityList) {
		removeAll();
		
		if(entityList != null ) {
			int size = entityList.size();

			ViewPanel panel = new ViewPanel(robotOverlord);

			if(size==1) {
				buildSingleEntityPanel(panel,entityList.get(0));
			} else if(size>0) {
				// TODO finish and re-enable this.
				//buildMultipleEntityPanel(panel,entityList);
			}

			add(panel.getFinalView(),BorderLayout.PAGE_START);
		}
		
		repaint();
		revalidate();

		if( this.getParent() instanceof JScrollPane ) {
			JScrollPane scroller = (JScrollPane)getParent();
			scroller.getVerticalScrollBar().setValue(0);
		}
	}

	private void buildSingleEntityPanel(ViewPanel panel, Entity entity) {
		if(entity != null) entity.getView(panel);
	}

	private void buildMultipleEntityPanel(ViewPanel panel, List<Entity> entityList) {
		// compare Entities and keep matching Components.
		Entity first = entityList.get(0);
		for(int i=0;i<first.getComponentCount();++i) {
			Component component = first.getComponent(i);
			if(sameComponentInAllEntities(component,entityList)) {
				// TODO display values shared across all remaining Components.  At present this shows only the first entity's component.
				panel.pushStack(component);
				component.getView(panel);
				panel.popStack();
			}
		}
	}

	private boolean sameComponentInAllEntities(Component component, List<Entity> entityList) {
		for(Entity e : entityList) {
			if(!e.containsAnInstanceOfTheSameClass(component)) {
				return false;
			}
		}
		return true;
	}
}
