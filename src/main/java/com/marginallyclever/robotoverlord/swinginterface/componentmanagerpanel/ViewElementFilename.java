package com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel;

import com.marginallyclever.robotoverlord.EntityManager;
import com.marginallyclever.robotoverlord.parameters.StringParameter;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.StringParameterEdit;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.undo.AbstractUndoableEdit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

/**
 * Panel to alter a file parameter.
 * @author Dan Royer
 *
 */
public class ViewElementFilename extends ViewElement implements ActionListener {
	private static String lastPath=System.getProperty("user.dir");
	private final JTextField field = new JTextField(15);
	private final ArrayList<FileFilter> filters = new ArrayList<>();
	private final StringParameter parameter;
	private final EntityManager entityManager;
	
	public ViewElementFilename(StringParameter parameter, EntityManager entityManager) {
		super();
		this.parameter = parameter;
		this.entityManager = entityManager;
		
		//this.setBorder(BorderFactory.createLineBorder(Color.RED));

		field.setEditable(false);
		field.setText(parameter.get());
		field.setMargin(new Insets(1,0,1,0));
		//pathAndFileName.setBorder(BorderFactory.createLoweredBevelBorder());
		
		JLabel label=new JLabel(parameter.getName(),JLabel.LEADING);
		label.setLabelFor(field);

		JButton choose = new JButton("...");
		choose.addActionListener(this);
		choose.setMargin(new Insets(0, 5, 0, 5));
		
		this.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx=0;
		gbc.gridy=0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		//gbc.gridheight = GridBagConstraints.REMAINDER;
		gbc.insets.right=5;
		this.add(label,gbc);
		gbc.weightx=1;
		gbc.insets.left=0;
		gbc.insets.right=0;
		this.add(field,gbc);
		gbc.weightx=0;
		this.add(choose,gbc);
		
		parameter.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				field.setText(parameter.get());
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		JFileChooser chooser = new JFileChooser();
		if(filters.size()==0) return;  // @TODO: fail!
		if(filters.size()==1) chooser.setFileFilter(filters.get(0));
		else {
			for (FileFilter filter : filters) {
				chooser.addChoosableFileFilter(filter);
			}
		}
		if(lastPath!=null) chooser.setCurrentDirectory(new File(lastPath));
		int returnVal = chooser.showDialog(SwingUtilities.getWindowAncestor(this), Translator.get("Select"));
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			String newFilename = chooser.getSelectedFile().getAbsolutePath();
			lastPath = chooser.getSelectedFile().getParent();
			String lastScenePath = entityManager.checkForScenePath(newFilename);

			AbstractUndoableEdit event = new StringParameterEdit(parameter, lastScenePath);
			UndoSystem.addEvent(this,event);
		}
	}

	public void setFileFilter(FileFilter arg0) {
		filters.clear();
		filters.add(arg0);
	}
	
	public void addFileFilter(FileFilter arg0) {
		filters.add(arg0);
	}
	
	/**
	 * Plural form of {@link #addFileFilter}.
	 * @param arg0 {@link ArrayList} of {@link FileFilter}.
	 */
	public void addFileFilters(List<FileFilter> arg0) {
		filters.addAll(arg0);
	}


	@Override
	public void setReadOnly(boolean arg0) {
		field.setEnabled(!arg0);
	}
}
