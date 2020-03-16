package com.marginallyclever.robotOverlord.uiElements.undoRedo.commands;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.UndoableEditEvent;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.IntEntity;
import com.marginallyclever.robotOverlord.uiElements.undoRedo.actions.UndoableActionSelectComboBox;

public class UserCommandSelectComboBox extends JPanel implements ActionListener, Observer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JComboBox<String> list;
	private RobotOverlord ro;
	private IntEntity e;
	
	public UserCommandSelectComboBox(RobotOverlord ro,IntEntity e,String [] listOptions) {
		super();
		this.ro = ro;
		this.e = e;
				
		list = new JComboBox<String>(listOptions);
		list.setSelectedIndex(e.get());
		list.addActionListener(this);

		JLabel label=new JLabel(e.getName(),JLabel.LEADING);
		label.setLabelFor(list);

		this.setLayout(new BorderLayout());
		this.setBorder(new EmptyBorder(5,0,5,0));
		this.add(label,BorderLayout.LINE_START);
		this.add(list,BorderLayout.LINE_END);
	}
	
	public String getValue() {
		return list.getItemAt(e.get());
	}

	/**
	 * I have changed.  poke the entity
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		int newIndex = list.getSelectedIndex();
		if(newIndex != e.get()) {
			ro.undoableEditHappened(new UndoableEditEvent(this,new UndoableActionSelectComboBox(e, e.getName(), newIndex) ) );
		}
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub
	}
}
