package com.marginallyclever.robotOverlord.robots.sixi2;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import com.marginallyclever.convenience.SpringUtilities;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.Translator;

/**
 * Calibration tool for setting the home position on the Sixi2 arm
 * @author dan royer
 *
 */
public class Sixi2Calibrator extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int AXIES = 6;
	private static final int MOVE_DEGREES = 5;
	
	private Component parent;
	private Sixi2 robot;
	
	private JButton [] jogPos = new JButton[AXIES];
	private JButton [] jogNeg = new JButton[AXIES];
	private JTextField [] offset = new JTextField[AXIES];
	private String [] names = {"X","Y","Z","U","V","W"};
	
	
	public Sixi2Calibrator(Component parentFrame,Sixi2 robot) {
		this.parent = parentFrame;
		this.robot = robot;
		
		SpringLayout layout = new SpringLayout();
		this.setLayout(layout);
		
		for(int i=0;i<AXIES;++i) {
			jogNeg[i]=new JButton("-");
			jogPos[i]=new JButton("+");
			offset[i]=new JTextField(6);
			
			this.add(jogNeg[i]);
			this.add(jogPos[i]);
			this.add(offset[i]);
			jogNeg[i].addActionListener(this);
			jogPos[i].addActionListener(this);
		}
		
		SpringUtilities.makeCompactGrid(this, AXIES, 4, 3, 3, 3, 3);
		updateOffsets();
	}
	
	public void run() {
		// call D23 to get the home values
		int result = JOptionPane.showConfirmDialog(parent, this,"Sixi2 calibrator",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			// approved
			// call D6 to set the new home values
		} else {
			// cancelled
			// do nothing
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		for(int i=0;i<AXIES;++i) {
			int move = 0;
			if(source == jogNeg[i]) move=-MOVE_DEGREES;
			if(source == jogPos[i]) move= MOVE_DEGREES;
			
			if(move!=0) {
				robot.sendLineToRobot("G91");  // incremental mode
				robot.sendLineToRobot("G0 "+names[i]+move+"\n");
				robot.sendLineToRobot("G90");  // absolute mode
			}
		}
		updateOffsets();
	}
	
	public void updateOffsets() {
		for(int i=0;i<AXIES;++i) {
			offset[i].setText(StringHelper.formatDouble(robot.receivedKeyframe.fkValues[i]));
		}
	}
	
	// for independent operation
	public static void main(String[] argv) {
	    //Schedule a job for the event-dispatching thread:
	    //creating and showing this application's GUI.
	    javax.swing.SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	        	Translator.start();
	        	
	            JFrame mainFrame = new JFrame( "Sixi2 calibrator test" ); 
	            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        	mainFrame.setSize( 500,500 );
	            mainFrame.setLayout(new java.awt.BorderLayout());
	            mainFrame.setVisible(true);
	            
	            Sixi2 sixi2 = new Sixi2();
	            if(sixi2.getConnection()==null || !sixi2.getConnection().isOpen()) {
	            	sixi2.openConnection();
	            }
	            
	            Sixi2Calibrator c = new Sixi2Calibrator(null,sixi2);
	        	c.run();
	        	
	        	sixi2.closeConnection();
	        }
	    });
	}
}
