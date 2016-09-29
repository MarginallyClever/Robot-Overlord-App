package com.marginallyclever.robotOverlord;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.robotOverlord.actions.ActionSelectFile;

public class ModelInWorldPanel extends JPanel implements ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ModelInWorld model;
	private ActionSelectFile actionSelectFile;
	
	public ModelInWorldPanel(RobotOverlord ro,ModelInWorld model) {
		super();
		
		this.model = model;

		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=1;
		c.weighty=1;
		c.anchor=GridBagConstraints.NORTHWEST;
		c.fill=GridBagConstraints.HORIZONTAL;

		CollapsiblePanel oiwPanel = new CollapsiblePanel("Model");
		this.add(oiwPanel,c);
		JPanel contents = oiwPanel.getContentPane();
		
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.CENTER;

		actionSelectFile = new ActionSelectFile(ro,"Filename",model.getFilename());
		actionSelectFile.setFileFilter(new FileNameExtensionFilter("STL files", "STL"));
		actionSelectFile.addChangeListener(this);
		contents.add(actionSelectFile,con1);
		con1.gridy++;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if(e.getSource()==actionSelectFile) {
			model.setFilename(actionSelectFile.getFilename());
		}
	}
}
