package com.marginallyclever.robotOverlord.uiElements.undoRedo.commands;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.UndoableEditEvent;
import javax.swing.filechooser.FileFilter;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.StringEntity;
import com.marginallyclever.robotOverlord.uiElements.undoRedo.actions.UndoableActionSelectString;

/**
 * Panel to alter a file parameter.
 * @author Dan Royer
 *
 */
public class UserCommandSelectFile extends JPanel implements ActionListener, Observer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static String lastPath=System.getProperty("user.dir");
	private JTextField fieldX;
	private RobotOverlord ro;
	private StringEntity e;
	private LinkedList<FileFilter> filters = new LinkedList<FileFilter>();
	
	public UserCommandSelectFile(RobotOverlord ro,StringEntity e) {
		super();
		this.ro = ro;
		this.e = e;
				
		fieldX = new JTextField(15);
		fieldX.setEditable(false);
		fieldX.setText(e.get());
		
		JLabel label=new JLabel(e.getName(),JLabel.LEADING);
		label.setLabelFor(fieldX);
		label.setBorder(new EmptyBorder(0,0,0,5));

		JButton choose = new JButton("...");
		choose.addActionListener(this);

		this.setLayout(new BorderLayout());
		this.add(label,BorderLayout.LINE_START);
		this.add(fieldX,BorderLayout.CENTER);
		this.add(choose,BorderLayout.LINE_END);
	}

	/**
	 * panel action, update entity
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
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
			lastPath = chooser.getSelectedFile().getParent();

			ro.undoableEditHappened(new UndoableEditEvent(this,new UndoableActionSelectString(e, newFilename) ) );
		}
	}

	public void setFileFilter(FileFilter arg0) {
		this.filters.clear();
		this.filters.add(arg0);
	}
	
	public void addChoosableFileFilter(FileFilter arg0) {
		this.filters.add(arg0);
	}

	/**
	 * entity has changed, update panel
	 */
	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}
}
