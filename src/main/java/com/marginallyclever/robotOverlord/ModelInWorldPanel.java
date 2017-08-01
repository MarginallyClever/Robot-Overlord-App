package com.marginallyclever.robotOverlord;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.robotOverlord.commands.UserCommandSelectFile;

public class ModelInWorldPanel extends JPanel implements ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ModelInWorld model;
	private UserCommandSelectFile userCommandSelectFile;
	
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

		userCommandSelectFile = new UserCommandSelectFile(ro,"Filename",model.getFilename());
		userCommandSelectFile.setFileFilter(new FileNameExtensionFilter("STL files", "STL"));
		userCommandSelectFile.addChangeListener(this);
		contents.add(userCommandSelectFile,con1);
		con1.gridy++;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if(e.getSource()==userCommandSelectFile) {
			model.setFilename(userCommandSelectFile.getFilename());
		}
	}
}
