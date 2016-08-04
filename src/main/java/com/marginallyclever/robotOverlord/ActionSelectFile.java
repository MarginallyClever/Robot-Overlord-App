package com.marginallyclever.robotOverlord;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ActionSelectFile extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField fieldX;
	private RobotOverlord ro;
	private FileNameExtensionFilter filter;
	private LinkedList<ChangeListener> changeListeners = new LinkedList<ChangeListener>();
	
	public ActionSelectFile(RobotOverlord ro,String labelName,String defaultValue) {
		super();
		this.ro = ro;
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weighty=1;
		con1.fill=GridBagConstraints.NONE;
		con1.anchor=GridBagConstraints.WEST;
		
		JLabel label=new JLabel(labelName,JLabel.CENTER);
	
		fieldX = new JTextField(15);
		fieldX.setEditable(false);
		fieldX.setText(defaultValue);
		label.setLabelFor(fieldX);

		JButton choose = new JButton("Choose...");
		choose.addActionListener(this);

		con1.weightx=1.0;
		con1.gridx=0;
		con1.gridwidth=2;	
		this.add(label,con1);
		con1.gridy++;

		con1.gridwidth=1;
		con1.weightx=1.0;
		con1.gridx=0;
		this.add(fieldX,con1);
		
		con1.weightx=0;
		con1.gridx=1;
		this.add(choose,con1);
		con1.gridy++;
	}
	

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		selectFile();
	}
	
	
	public void selectFile() {
		JFileChooser chooser = new JFileChooser();
		if(filter!=null) chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(ro.getMainFrame());
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			String newFilename = chooser.getSelectedFile().getAbsolutePath();
			System.out.println("You chose to open this file: " + newFilename);

			ro.getUndoHelper().undoableEditHappened(new UndoableEditEvent(this,new CommandSelectFile(this, newFilename) ) );
		}
	}

	public void setFileFilter(FileNameExtensionFilter filter) {
		this.filter = filter;
	}

	public String getFilename() {
		return fieldX.getText();
	}
	
	public void setFilename(String filename) {
		fieldX.setText(filename);
		this.updateUI();

		ChangeEvent arg0 = new ChangeEvent(this);
		Iterator<ChangeListener> i = changeListeners.iterator();
		while(i.hasNext()) {
			i.next().stateChanged(arg0);
		}
	}
	
	public void addChangeListener(ChangeListener arg0) {
		changeListeners.add(arg0);
	}
	
	public void removeChangeListner(ChangeListener arg0) {
		changeListeners.remove(arg0);
	}
}
