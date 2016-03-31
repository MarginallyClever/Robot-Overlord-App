package com.marginallyclever.robotOverlord;

import java.awt.FlowLayout;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


public class JLabelledTextField extends JPanel implements DocumentListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6112042391576602225L;
	private JLabel label;
	private JTextField field;
	
	public JLabelledTextField(String value_text,String label_text) {
		label = new JLabel(label_text,JLabel.TRAILING);
		field = new JTextField(value_text,10);

		JPanel p = this;

		//JPanel margin = new JPanel();
        //p.add(margin);
		//p=margin;
        //margin.setBorder(new EmptyBorder(5, 5, 5, 5));

		//JPanel container = new JPanel();
		//p.add(container);
		//p=container;
		
		p.setLayout(new FlowLayout());
		p.add(label);
		p.add(field); 
		
		field.getDocument().addDocumentListener(this);
	}
	
	public JTextField getField() { return field; }
	
	public void addActionListener(ActionListener L) {
		field.addActionListener(L);
	}
	
	public void changedUpdate(DocumentEvent e) {
		changeNow(e);
	}
	public void removeUpdate(DocumentEvent e) {
		changeNow(e);
	}
	public void insertUpdate(DocumentEvent e) {
		changeNow(e);
	}
	
	public void changeNow(DocumentEvent e) {
		this.firePropertyChange("value",null,field.getText());
	}
}
