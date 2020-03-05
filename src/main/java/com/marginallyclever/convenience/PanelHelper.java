package com.marginallyclever.convenience;

import java.awt.GridBagConstraints;

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
}
