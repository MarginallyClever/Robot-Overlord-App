package com.marginallyclever.robotoverlord.swinginterface;

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
			ViewPanel [] panels = new ViewPanel[size];
			for( int i=0;i<size;++i) {
				panels[i] = new ViewPanel(robotOverlord);
				Entity e = entityList.get(i);
				if(e != null) e.getView(panels[i]);
			}

			if(entityList.size()==1) {
				// keep the first panel.
				// TODO compare panels and keep only the matching elements and - if possible - the data in those elements.
				ViewPanel combined = panels[0];

				// TODO throw away panels that have no elements left.
				add(combined.getFinalView(),BorderLayout.PAGE_START);
			} else {
				// TODO display values shared across all selected entities
			}
		}
		
		repaint();
		revalidate();

		if( this.getParent() instanceof JScrollPane ) {
			JScrollPane scroller = (JScrollPane)getParent();
			scroller.getVerticalScrollBar().setValue(0);
		}
	}
}
