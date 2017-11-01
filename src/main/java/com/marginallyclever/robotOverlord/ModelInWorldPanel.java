package com.marginallyclever.robotOverlord;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Iterator;
import java.util.ServiceLoader;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.vecmath.Vector3f;

import com.marginallyclever.robotOverlord.commands.UserCommandSelectFile;
import com.marginallyclever.robotOverlord.commands.UserCommandSelectVector3f;
import com.marginallyclever.robotOverlord.model.ModelLoadAndSave;

public class ModelInWorldPanel extends JPanel implements ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ModelInWorld model;
	private UserCommandSelectFile userCommandSelectFile;
	private UserCommandSelectVector3f scaleParam;
	
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
		// Find all the serviceLoaders for loading files.
		ServiceLoader<ModelLoadAndSave> loaders = ServiceLoader.load(ModelLoadAndSave.class);
		Iterator<ModelLoadAndSave> i = loaders.iterator();
		while(i.hasNext()) {
			ModelLoadAndSave loader = i.next();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(loader.getEnglishName(), loader.getValidExtensions());
			userCommandSelectFile.addChoosableFileFilter(filter);
		}
		userCommandSelectFile.addChangeListener(this);
		contents.add(userCommandSelectFile,con1);
		con1.gridy++;
		
		
		scaleParam = new UserCommandSelectVector3f(ro,"Scale",new Vector3f(1,1,1));
		scaleParam.addChangeListener(this);
		contents.add(scaleParam,con1);
		con1.gridy++;
		
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if(e.getSource()==userCommandSelectFile) {
			model.setFilename(userCommandSelectFile.getFilename());
		}
		if(e.getSource()==scaleParam) {
			model.setScale(scaleParam.getValue());
		}
	}
	
	/**
	 * Call by an {@link Entity} when it's details change so that they are reflected on the panel.
	 * This might be better as a listener pattern.
	 */
	public void updateFields() {
		Vector3f pos = model.getScale();
		scaleParam.setValue(pos);
	}
}
