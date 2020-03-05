package com.marginallyclever.convenience;

import java.awt.Component;
import java.awt.GridBagConstraints;

import javax.swing.JComponent;

/**
 * Consistent gridBagConstraints for easier style tweaking
 * 
 */
public class PanelHelper {
	static public GridBagConstraints getDefaultGridBagConstraints() { 
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=1;
		con1.weighty=0;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.NORTHWEST;
		
		return con1;
	}
	
	/**
	 * @param parent
	 * @param last
	 */
	static public void ExpandLastChild(JComponent parent,GridBagConstraints last) {
		Component child = parent.getComponent(parent.getComponentCount()-1);
		last.weighty=1;
		parent.add(child,last);
	}
}
