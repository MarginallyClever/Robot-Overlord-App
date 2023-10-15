package com.marginallyclever.convenience.helpers;

import com.marginallyclever.robotoverlord.swing.CollapsiblePanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Consistent gridBagConstraints for easier style tweaking
 * 
 */
public class PanelHelper {
	@Deprecated
	static public GridBagConstraints getDefaultGridBagConstraints() { 
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=1;
		con1.weighty=0;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.ABOVE_BASELINE_LEADING;
		
		return con1;
	}
		
	/**
	 * Add a list of panels to a parent panel and control the layout. 
	 * @param list
	 * @param parent
	 */
	static public void formatEntityPanels(ArrayList<JPanel> list,JPanel parent) {
		parent.removeAll();
		
		//logger.info("formatEntityPanels "+parent.getName());
		
		// fill in the selectedEntityPanel
		GridBagConstraints con1 = PanelHelper.getDefaultGridBagConstraints();
		
		// true to use tab
		boolean tabbedLayout=true;
		if(tabbedLayout==false) {
			// single page layout
			JPanel sum = new JPanel();
			BoxLayout layout = new BoxLayout(sum, BoxLayout.PAGE_AXIS);
			sum.setLayout(layout);
			for( JPanel p : list ) {				
				CollapsiblePanel oiwPanel = new CollapsiblePanel(p.getName());
				oiwPanel.getContentPane().add(p);
				sum.add(oiwPanel);
			}

			JPanel b = new JPanel(new BorderLayout());
			b.add(sum, BorderLayout.PAGE_START);

			parent.add(b,con1);
		} else {
			boolean reverseOrderOfTabs = false;
			// tabbed layout
			JTabbedPane b = new JTabbedPane();
			for( JPanel p : list ) {				
				if( reverseOrderOfTabs ) {
					b.insertTab(p.getName(), null, p, null, 0);
				} else {
					b.addTab(p.getName(), p);
				}
			}
			b.setSelectedIndex( reverseOrderOfTabs ? 0 : b.getTabCount()-1 );

			parent.add(b,con1);
		}
	}
}
