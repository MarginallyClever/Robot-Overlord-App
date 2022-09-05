package com.marginallyclever.robotOverlord.swingInterface;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;


public class SelectedEntityPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SelectedEntityPanel() {
		super();
		setLayout(new BorderLayout());
	}

	/**
	 * Collate all the {@link EntityPanel}s for a {@link Entity}.  
	 * @param 
	 */
	public void update(ArrayList<Entity> entityList,RobotOverlord ro) {
		removeAll();
		
		if(entityList != null ) {
			int size = entityList.size();
			ViewPanel [] panels = new ViewPanel[size];
			for( int i=0;i<size;++i) {
				panels[i] = new ViewPanel(ro);
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
