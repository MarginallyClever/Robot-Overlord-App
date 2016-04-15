package com.marginallyclever.robotOverlord.world;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.robotOverlord.ModelInWorld;
import com.marginallyclever.robotOverlord.RobotWithConnection;

public class AddModelToWorldButton extends JMenuItem implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	transient protected RobotWithConnection robot;
	transient protected World world;
	
	public AddModelToWorldButton(World world, String buttonText) {
		super(buttonText);
		this.world=world;
		addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		// TODO Auto-generated method stub
		try {
			JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
			
			JFileChooser fc = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("STL files", "STL", "abc");
			fc.setFileFilter(filter);
			int returnVal = fc.showOpenDialog(topFrame);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String filename = fc.getSelectedFile().getAbsolutePath();
	    		ModelInWorld m = new ModelInWorld();
	    		m.setFilename( filename );
	    		world.objects.add(m);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
