package com.marginallyclever.robotoverlord.parameters.swing;

import com.marginallyclever.convenience.helpers.PathHelper;
import com.marginallyclever.robotoverlord.parameters.StringParameter;
import com.marginallyclever.robotoverlord.swing.UndoSystem;
import com.marginallyclever.robotoverlord.swing.edits.StringParameterEdit;
import com.marginallyclever.robotoverlord.swing.translator.Translator;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel to alter a file parameter.
 * @author Dan Royer
 *
 */
public class ViewElementFilename extends ViewElement implements ActionListener {
	private static final JFileChooser chooser = new JFileChooser(PathHelper.SCENE_PATH);
	private final JLabel label;
	private final JTextField field = new JTextField(15);
	private final ArrayList<FileFilter> filters = new ArrayList<>();
	private final StringParameter parameter;
	
	public ViewElementFilename(StringParameter parameter) {
		super();
		this.parameter = parameter;

		chooser.setFileView(new FileView() {
			@Override
			public Boolean isTraversable(File f) {
				if(!f.isDirectory()) return false;
				return f.getAbsolutePath().startsWith(PathHelper.SCENE_PATH);
			}
		});

		//this.setBorder(BorderFactory.createLineBorder(Color.RED));

		field.setEditable(false);
		field.setText(parameter.get());
		field.setMargin(new Insets(1,0,1,0));
		//pathAndFileName.setBorder(BorderFactory.createLoweredBevelBorder());
		
		label=new JLabel(parameter.getName(),JLabel.LEADING);
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
		if(filters.size()==0) return;  // @TODO: fail!
		if(filters.size()==1) chooser.setFileFilter(filters.get(0));
		else {
			for (FileFilter filter : filters) {
				chooser.addChoosableFileFilter(filter);
			}
		}

		ViewElementFilename chooserParent = this;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				int returnVal = chooser.showDialog(SwingUtilities.getWindowAncestor(chooserParent), Translator.get("Select"));
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					UndoSystem.addEvent(new StringParameterEdit(parameter, chooser.getSelectedFile().getAbsolutePath()));
				}
			}
		});
	}

	public static void setLastPath(String lastPath) {
		if(lastPath!=null) chooser.setCurrentDirectory(new File(lastPath));
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

    public void setLabel(String s) {
		label.setText(s);
		GridBagLayout layout = (GridBagLayout)getLayout();
		GridBagConstraints g = layout.getConstraints(label);
		g.insets.right = (label.getText().trim().isEmpty()?0:5);
		layout.setConstraints(label,g);
    }
}
