package com.marginallyclever.convenience.log;

import javax.swing.*;
import java.awt.*;


public class LogDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3554043754842683959L;
	private LogPanel logPanel = new LogPanel();

	public LogDialog(Frame owner,String name) {
		super(owner,name);
		setTitle(name);
		
		setPreferredSize(new Dimension(600,400));
		add(logPanel);
		pack();
	}
	
	public void run() {
		setVisible(true);
	}
}
