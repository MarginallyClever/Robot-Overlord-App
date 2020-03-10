package com.marginallyclever.robotOverlord.engine.undoRedo.commands;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.filechooser.FileFilter;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.engine.undoRedo.actions.UndoableActionSelectFile;

/**
 * Panel to alter a file parameter.
 * @author Dan Royer
 *
 */
public class UserCommandSelectFile extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static String lastPath=null;
	private JTextField fieldX;
	private RobotOverlord ro;
	private String label;
	private LinkedList<FileFilter> filters = new LinkedList<FileFilter>();
	private LinkedList<ChangeListener> changeListeners = new LinkedList<ChangeListener>();
	
	public UserCommandSelectFile(RobotOverlord ro,String labelName,String defaultValue) {
		super();
		this.ro = ro;
		this.label = labelName;
		
		lastPath = System.getProperty("user.dir");
		
		fieldX = new JTextField(15);
		fieldX.setEditable(false);
		fieldX.setText(defaultValue);
		
		JLabel label=new JLabel(labelName,JLabel.LEFT);
		label.setLabelFor(fieldX);
		label.setBorder(new EmptyBorder(0,0,0,5));

		JButton choose = new JButton("...");
		choose.addActionListener(this);

		this.setLayout(new BorderLayout());
		this.setBorder(new EmptyBorder(5,0,5,0));
		this.add(label,BorderLayout.LINE_START);
		this.add(fieldX,BorderLayout.CENTER);
		this.add(choose,BorderLayout.LINE_END);
	}
	

	@Override
	public void actionPerformed(ActionEvent arg0) {
		selectFile();
	}
	
	
	public void selectFile() {
		JFileChooser chooser = new JFileChooser();
		if(filters.size()==0) return;  // @TODO: fail!
		if(filters.size()==1) chooser.setFileFilter(filters.get(0));
		else {
			Iterator<FileFilter> i = filters.iterator();
			while(i.hasNext()) {
				chooser.addChoosableFileFilter( i.next());
			}
		}
		if(lastPath!=null) chooser.setCurrentDirectory(new File(lastPath));
		int returnVal = chooser.showOpenDialog(ro.getMainFrame());
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			String newFilename = chooser.getSelectedFile().getAbsolutePath();
			System.out.println("You chose to open this file: " + newFilename);
			lastPath = chooser.getSelectedFile().getParent();

			ro.getUndoHelper().undoableEditHappened(new UndoableEditEvent(this,new UndoableActionSelectFile(this, label, newFilename) ) );
		}
	}

	public void setFileFilter(FileFilter arg0) {
		this.filters.clear();
		this.filters.add(arg0);
	}
	
	public void addChoosableFileFilter(FileFilter arg0) {
		this.filters.add(arg0);
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
