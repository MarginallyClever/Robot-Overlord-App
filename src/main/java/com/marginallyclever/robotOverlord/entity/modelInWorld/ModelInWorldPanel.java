package com.marginallyclever.robotOverlord.entity.modelInWorld;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Iterator;
import java.util.ServiceLoader;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.convenience.PanelHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.engine.model.ModelLoadAndSave;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandSelectFile;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandSelectNumber;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandSelectVector3d;
import com.marginallyclever.robotOverlord.entity.Entity;

public class ModelInWorldPanel extends JPanel implements ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ModelInWorld model;
	private UserCommandSelectFile userCommandSelectFile;
	private UserCommandSelectNumber setScale;
	private UserCommandSelectVector3d setOrigin;
	private UserCommandSelectVector3d setRotation;
	
	public ModelInWorldPanel(RobotOverlord gui,ModelInWorld model) {
		super();
		
		this.model = model;

		this.setName("Model");
		this.setLayout(new GridBagLayout());

		GridBagConstraints con1 = PanelHelper.getDefaultGridBagConstraints();

		userCommandSelectFile = new UserCommandSelectFile(gui,"Filename",model.getFilename());
		// Find all the serviceLoaders for loading files.
		ServiceLoader<ModelLoadAndSave> loaders = ServiceLoader.load(ModelLoadAndSave.class);
		Iterator<ModelLoadAndSave> i = loaders.iterator();
		while(i.hasNext()) {
			ModelLoadAndSave loader = i.next();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(loader.getEnglishName(), loader.getValidExtensions());
			userCommandSelectFile.addChoosableFileFilter(filter);
		}
		userCommandSelectFile.addChangeListener(this);
		this.add(userCommandSelectFile,con1);

		con1.gridy++;
		setScale = new UserCommandSelectNumber(gui,"Scale",model.getModelScale());
		setScale.addChangeListener(this);
		this.add(setScale,con1);

		con1.gridy++;
		setOrigin = new UserCommandSelectVector3d(gui,"Adjust origin",model.getModelOrigin());
		setOrigin.addChangeListener(this);
		this.add(setOrigin,con1);

		con1.gridy++;
		setRotation = new UserCommandSelectVector3d(gui,"Adjust rotation",model.getModelRotation());
		setRotation.addChangeListener(this);
		this.add(setRotation,con1);

		PanelHelper.ExpandLastChild(this, con1);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if(e.getSource()==userCommandSelectFile) {
			model.setFilename(userCommandSelectFile.getFilename());
		}
		if(e.getSource()==setScale) {
			model.setModelScale(setScale.getValue());
		}
		if(e.getSource()==setOrigin) {
			model.setModelOrigin(setOrigin.getValue());
		}
		if(e.getSource()==setRotation) {
			model.setModelRotation(setRotation.getValue());
		}
	}
	
	/**
	 * Call by an {@link Entity} when it's details change so that they are reflected on the panel.
	 * This might be better as a listener pattern.
	 */
	public void updateFields() {
		setScale.setValue(model.scale);
		setOrigin.setValue(model.getModelOrigin());
		setRotation.setValue(model.getModelRotation());
	}
}
