package com.marginallyclever.robotOverlord.swingInterface;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

@Deprecated
public class SecondaryPanel extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public JTabbedPane tabbedPane;
	public JPanel gcodePanel;
	public JPanel logPanel;
	
	public JPanel bottom;
	
	public JTextArea gcode, log;
	
	public JButton playStop, rewind;
	public JProgressBar progress;
	

	public SecondaryPanel() {
		super();
		
		makeBottomPanel();
		makeGCodePanel();
		makeLogPanel();
		
		// now that the panels are all made, glue them together
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Log", logPanel);
		tabbedPane.addTab("GCODE", gcodePanel);
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridx=0;
		c.gridy=0;
		c.weightx=1.0;
		c.anchor=GridBagConstraints.NORTH;

		//c.fill = GridBagConstraints.HORIZONTAL;	c.weighty=0.0;	this.add(top, 		c);		c.gridy++;
		c.fill = GridBagConstraints.BOTH;		c.weighty=1.0;	this.add(tabbedPane,c);		c.gridy++;
		c.fill = GridBagConstraints.HORIZONTAL;	c.weighty=0.0;	this.add(bottom,	c);		c.gridy++;
	}

	protected void makeLogPanel() {
		// setup the elements
		log = new JTextArea();
		log.setEditable(false);
		log.setColumns(128);
		log.setRows(512);
		JScrollPane areaScrollPane = new JScrollPane(log);
		areaScrollPane.setVerticalScrollBarPolicy(
		                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		logPanel = new JPanel(new GridBagLayout());

		// connect it together
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=1.0;
		c.weighty=1.0;
		c.fill=GridBagConstraints.BOTH;
		c.insets = new Insets(0,3,3,0);		
		c.anchor=GridBagConstraints.CENTER;
		logPanel.add(areaScrollPane,c);
	}
	
	protected void makeGCodePanel() {
		// setup the elements
		gcode = new JTextArea();
		gcode.setEditable(true);
		gcode.setColumns(128);
		gcode.setRows(512);

		JScrollPane areaScrollPane = new JScrollPane(gcode);
		areaScrollPane.setVerticalScrollBarPolicy(
		                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		gcodePanel = new JPanel(new GridBagLayout());

		// connect it together
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=1.0;
		c.weighty=1.0;
		c.fill=GridBagConstraints.BOTH;
		c.insets = new Insets(3,3,3,3);		
		c.anchor=GridBagConstraints.CENTER;
		gcodePanel.add(areaScrollPane,c);
	}
	
	protected void makeBottomPanel() {
		// setup the elements
		progress=new JProgressBar();
		progress.setMaximum(100);
		progress.setMinimum(0);
		
		rewind=new JButton("Rewind");
		rewind.addActionListener(this);
		
		playStop=new JButton("Play");
		playStop.addActionListener(this);
		
		// create the panel
		bottom = new JPanel();
		bottom.setLayout(new GridBagLayout());
		
		// connect them all together
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=1.0;
		c.weighty=1.0;
		c.fill=GridBagConstraints.HORIZONTAL;
		
		c.weightx=0.05;		c.gridwidth=1;		bottom.add(rewind,c);	c.gridx+=1;
		c.weightx=0.05;		c.gridwidth=1;		bottom.add(playStop,c);		c.gridx+=1;
		
		c.weightx=1.0 - (c.gridx/10);
		c.gridwidth=10 - c.gridx;
		c.ipady=5;
		c.insets = new Insets(0,3,3,0);
		bottom.add(progress,c);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Bottom panel elements
		Object source = e.getSource();
		if(source == rewind) {
			// gcode reset to start.  
			progress.setValue(0);
		}
		if(source == playStop) {
			// find the robot
			// start sending the gcode?
			
			// adjust the buttons
			if(playStop.getText().equals("Play")) {
				playStop.setText("Stop");
				rewind.setEnabled(false);
			} else {
				playStop.setText("Play");
				rewind.setEnabled(true);
			}
		}
	}
}
