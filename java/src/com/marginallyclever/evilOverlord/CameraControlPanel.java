package com.marginallyclever.evilOverlord;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class CameraControlPanel extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8529190569816182683L;
	
	
	private JButton buttonFlyUp;
	private JButton buttonFlyDown;
	private JButton buttonFlyLeft;
	private JButton buttonFlyRight;
	private JButton buttonFlyForward;
	private JButton buttonFlyBackward;
	
	private JButton buttonLookUp;
	private JButton buttonLookDown;
	private JButton buttonLookLeft;
	private JButton buttonLookRight;

	
	private JButton createButton(String name) {
		JButton b = new JButton(name);
		b.addActionListener(this);
		return b;
	}


	public CameraControlPanel() {
		this.setLayout(new GridLayout(0,1));
		JPanel p;
		
		p = new JPanel(new GridLayout(3,3));
		this.add(p);
		p.add(new JLabel(""));
		p.add(buttonFlyUp = createButton("up"));
		p.add(new JLabel(""));
		p.add(buttonFlyLeft = createButton("left"));
		p.add(new JLabel("Fly"));
		p.add(buttonFlyRight = createButton("right"));
		p.add(new JLabel(""));
		p.add(buttonFlyDown = createButton("down"));

		this.add(buttonFlyForward = createButton("fly forward"));
		this.add(buttonFlyBackward = createButton("fly backward"));

		p = new JPanel(new GridLayout(3,3));
		this.add(p);
		p.add(new JLabel(""));
		p.add(buttonLookUp = createButton("up"));
		p.add(new JLabel(""));
		p.add(buttonLookLeft = createButton("left"));
		p.add(new JLabel("Look"));
		p.add(buttonLookRight = createButton("right"));
		p.add(new JLabel(""));
		p.add(buttonLookDown = createButton("down"));
	}


	// arm5 controls
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();		

		MainGUI gui = MainGUI.getSingleton();
		World world = gui.world;


		if( subject == buttonFlyUp ) {
			world.camera.move_ud= ( world.camera.move_ud==1 ) ? 0 : 1;
		}
		if( subject == buttonFlyDown ) {
			world.camera.move_ud= ( world.camera.move_ud==-1 ) ? 0 : -1;
		}
		if( subject == buttonFlyLeft ) {
			world.camera.move_lr= ( world.camera.move_lr==1 ) ? 0 : 1;
		}
		if( subject == buttonFlyRight ) {
			world.camera.move_lr= ( world.camera.move_lr==-1 ) ? 0 : -1;
		}
		if( subject == buttonFlyForward ) {
			world.camera.move_fb= ( world.camera.move_fb==1 ) ? 0 : 1;
		}
		if( subject == buttonFlyBackward ) {
			world.camera.move_fb= ( world.camera.move_fb==-1 ) ? 0 : -1;
		}
		if( subject == buttonLookDown ) {
			world.camera.tilt_dir= ( world.camera.tilt_dir==-1 ) ? 0 : -1;	
		}
		if( subject == buttonLookUp ) {
			world.camera.tilt_dir= ( world.camera.tilt_dir==1 ) ? 0 : 1;
		}
		if( subject == buttonLookLeft ) {
			world.camera.pan_dir= ( world.camera.pan_dir==-1 ) ? 0 : -1;
		}
		if( subject == buttonLookRight ) {
			world.camera.pan_dir= ( world.camera.pan_dir==1 ) ? 0 : 1;
		}
	}
}
