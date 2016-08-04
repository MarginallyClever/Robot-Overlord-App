package com.marginallyclever.robotOverlord;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Load the world from a file
 * @author Admin
 *
 */
public class ActionLoad extends JMenuItem implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected RobotOverlord ro;
	
	public ActionLoad(RobotOverlord ro) {
		super("Load...",KeyEvent.VK_L);
		this.ro = ro;
		addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("RO files", "RO");
		fc.setFileFilter(filter);
		int returnVal = fc.showSaveDialog(ro.getMainFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
            ro.loadWorldFromFile(fc.getSelectedFile().getAbsolutePath());
		}
	}
}
