package com.marginallyclever.robotOverlord.camera;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import com.marginallyclever.robotOverlord.RobotOverlord;

/**
 * Context sensitive GUI for the camera 
 * @author Dan Royer
 *
 */
public class CameraControlPanel extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8529190569816182683L;
	
	private Camera camera=null;
	/*
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
*/
	
	public CameraControlPanel(RobotOverlord gui,Camera cam) {
		super();
		
		camera=cam;
		this.setName("Camera");
/*
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=1;
		c.weighty=1;
		c.anchor=GridBagConstraints.NORTHWEST;
		c.fill=GridBagConstraints.HORIZONTAL;

		JPanel p;
		CollapsiblePanel p2 = new CollapsiblePanel("Fly");
		JPanel p1 = p2.getContentPane();
		p1.setLayout(new GridLayout(0,1));
			p = new JPanel(new GridLayout(3,3));
			p.add(new JLabel(""));
			p.add(buttonFlyUp = createButton("Up"));
			p.add(new JLabel(""));
			
			p.add(buttonFlyLeft = createButton("Left"));
			p.add(new JLabel(""));
			p.add(buttonFlyRight = createButton("Right"));
			
			p.add(new JLabel(""));
			p.add(buttonFlyDown = createButton("Down"));
			p.add(new JLabel(""));
			p1.add(p);

			p = new JPanel(new GridLayout(2,1));
			p.add(buttonFlyForward = createButton("Forward"));
			p.add(buttonFlyBackward = createButton("Backward"));
			p1.add(p);
		this.add(p2,c);
		c.gridy++;

		p2 = new CollapsiblePanel("Look");
		p1 = p2.getContentPane();
		p1.setLayout(new GridLayout(3,3));
			p1.add(new JLabel(""));
			p1.add(buttonLookUp = createButton("Up"));
			p1.add(new JLabel(""));
			p1.add(buttonLookLeft = createButton("Left"));
			p1.add(new JLabel(""));
			p1.add(buttonLookRight = createButton("Right"));
			p1.add(new JLabel(""));
			p1.add(buttonLookDown = createButton("Down"));
		this.add(p2,c);
		c.gridy++;*/
	}

	
	public void actionPerformed(ActionEvent e) {	
		/*
		Object subject = e.getSource();	
 // TODO cleanup
		if( subject == buttonFlyUp			) {	camera.move_up     = ( camera.move_up     == 1 ) ? 0 :  1;		}
		if( subject == buttonFlyDown		) {	camera.move_up     = ( camera.move_up     ==-1 ) ? 0 : -1;		}
		if( subject == buttonFlyLeft		) {	camera.move_left   = ( camera.move_left   == 1 ) ? 0 :  1;		}
		if( subject == buttonFlyRight		) {	camera.move_left   = ( camera.move_left   ==-1 ) ? 0 : -1;		}
		if( subject == buttonFlyForward		) {	camera.move_forward= ( camera.move_forward== 1 ) ? 0 :  1;		}
		if( subject == buttonFlyBackward	) {	camera.move_forward= ( camera.move_forward==-1 ) ? 0 : -1;		}
		if( subject == buttonLookDown		) {	camera.tilt_dir    = ( camera.tilt_dir    ==-1 ) ? 0 : -1;		}
		if( subject == buttonLookUp			) {	camera.tilt_dir    = ( camera.tilt_dir    == 1 ) ? 0 :  1;		}
		if( subject == buttonLookLeft		) {	camera.pan_dir     = ( camera.pan_dir     ==-1 ) ? 0 : -1;		}
		if( subject == buttonLookRight		) {	camera.pan_dir     = ( camera.pan_dir     == 1 ) ? 0 :  1;		}*/
	}
	
	
	public void updateFields() {}
}
