package com.marginallyclever.robotOverlord;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ModelInWorldPanel extends JPanel implements ActionListener, FocusListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ModelInWorld model;
	private RobotOverlord gui;

	private transient JTextField fieldX;
	
	public ModelInWorldPanel(RobotOverlord gui,ModelInWorld model) {
		super();
		
		this.model = model;
		this.gui = gui;
		

		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=1;
		c.weighty=1;
		c.anchor=GridBagConstraints.NORTHWEST;
		c.fill=GridBagConstraints.HORIZONTAL;

		CollapsiblePanel oiwPanel = new CollapsiblePanel("Source file");
		this.add(oiwPanel,c);
		JPanel contents = oiwPanel.getContentPane();
		
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.CENTER;
		
		JLabel x=new JLabel("Filename:",JLabel.CENTER);
		
		fieldX = new JTextField(model.getFilename());
		x.setLabelFor(fieldX);
		fieldX.addActionListener(this);
		fieldX.addFocusListener(this);
		con1.weightx=0.25;  con1.gridx=0; contents.add(x,con1);
		con1.weightx=0.75;  con1.gridx=1; contents.add(fieldX,con1);
		con1.gridy++;
	
		// update the field values;
		updateFields();
		
	}
	
	
	public void selectFile() {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("STL files", "STL");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(gui.getMainFrame());
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			String filename = chooser.getSelectedFile().getAbsolutePath();
			System.out.println("You chose to open this file: " + filename);
			model.setFilename(filename);
		}
	}

	public void updateFields() {
		fieldX.setText(model.getFilename());
	}


	@Override
	public void actionPerformed(ActionEvent event) {}


	@Override
	public void focusGained(FocusEvent arg0) {
		// TODO Auto-generated method stub
		Object source = arg0.getSource();
		
		if(source == fieldX) selectFile();
	}


	@Override
	public void focusLost(FocusEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
