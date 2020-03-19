package com.marginallyclever.robotOverlord;

import java.awt.Color;
import java.awt.GridBagConstraints;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;

public class TabTest extends JTabbedPane {
	JTabbedPane pane;
	GridBagConstraints gbc;
	JPanel p;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TabTest() {
		super(JTabbedPane.LEFT,JTabbedPane.SCROLL_TAB_LAYOUT);
		
		addOneTab("Az");
			addButton();
		addOneTab("Ba");
			addButton();
			addButton();
		addOneTab("Cb");
			addButton();
			addButton();
			addButton();
			addButton();
			addButton();
			addButton();
		addOneTab("Dc");
			addButton();
			addButton();
			addButton();
			addText();
	}
	
	void addButton() {
		JButton b = new JButton("Test");
		//p.add(b,gbc);
		p.add(b);
	}
	
	void addText() {
		JTextField b = new JTextField(4);
		//p.add(b,gbc);
		p.add(b);
	}
	
	void addOneTab(String title) {
		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
		p.setBorder(new LineBorder(Color.RED));
	/*
		gbc = new GridBagConstraints();
		gbc.weightx=1;
		//gbc.gridx  =0;
		gbc.fill      = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets.top   =5;
		gbc.insets.left  =5;
		gbc.insets.right =5; 
		gbc.insets.bottom=5; */
		
		addTab(title, p);
	}
	
	
	public static void main(String[] argv) {
	    //Schedule a job for the event-dispatching thread:
	    //creating and showing this application's GUI.
	    javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
	            JFrame f = new JFrame("Tab Test");
	            f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	            
	            f.add("Center", new JScrollPane(new TabTest()));
	            f.pack();
	            f.setSize(400,200);
	            f.setVisible(true);
	        }
	    });
	}
}
