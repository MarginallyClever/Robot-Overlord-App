package com.marginallyclever.evilOverlord.Camera;

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
	
	private Camera camera=null;
	
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

	public CameraControlPanel(Camera cam) {
		super();
		
		camera=cam;
		
		this.setLayout(new GridLayout(0,1));
		JPanel p;
		
		p = new JPanel(new GridLayout(3,3));
		this.add(p);
		p.add(new JLabel(""));
		p.add(buttonFlyUp = createButton("Up"));
		p.add(new JLabel(""));
		p.add(buttonFlyLeft = createButton("Left"));
		JLabel flyLabel = new JLabel("Fly");
		flyLabel.setHorizontalAlignment(JLabel.CENTER);
		p.add(flyLabel);
		p.add(buttonFlyRight = createButton("Right"));
		p.add(new JLabel(""));
		p.add(buttonFlyDown = createButton("Down"));

		this.add(buttonFlyForward = createButton("Forward"));
		this.add(buttonFlyBackward = createButton("Backward"));

		p = new JPanel(new GridLayout(3,3));
		this.add(p);
		p.add(new JLabel(""));
		p.add(buttonLookUp = createButton("up"));
		p.add(new JLabel(""));
		p.add(buttonLookLeft = createButton("left"));
		JLabel lookLabel = new JLabel("Look");
		lookLabel.setHorizontalAlignment(JLabel.CENTER);
		p.add(lookLabel);
		p.add(buttonLookRight = createButton("right"));
		p.add(new JLabel(""));
		p.add(buttonLookDown = createButton("down"));
	}


	// arm5 controls
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();		

		if( subject == buttonFlyUp ) {
			camera.move_ud= ( camera.move_ud==1 ) ? 0 : 1;
		}
		if( subject == buttonFlyDown ) {
			camera.move_ud= ( camera.move_ud==-1 ) ? 0 : -1;
		}
		if( subject == buttonFlyLeft ) {
			camera.move_lr= ( camera.move_lr==1 ) ? 0 : 1;
		}
		if( subject == buttonFlyRight ) {
			camera.move_lr= ( camera.move_lr==-1 ) ? 0 : -1;
		}
		if( subject == buttonFlyForward ) {
			camera.move_fb= ( camera.move_fb==1 ) ? 0 : 1;
		}
		if( subject == buttonFlyBackward ) {
			camera.move_fb= ( camera.move_fb==-1 ) ? 0 : -1;
		}
		if( subject == buttonLookDown ) {
			camera.tilt_dir= ( camera.tilt_dir==-1 ) ? 0 : -1;	
		}
		if( subject == buttonLookUp ) {
			camera.tilt_dir= ( camera.tilt_dir==1 ) ? 0 : 1;
		}
		if( subject == buttonLookLeft ) {
			camera.pan_dir= ( camera.pan_dir==-1 ) ? 0 : -1;
		}
		if( subject == buttonLookRight ) {
			camera.pan_dir= ( camera.pan_dir==1 ) ? 0 : 1;
		}
	}
}
