package com.marginallyclever.robotOverlord.entity.camera;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.marginallyclever.convenience.PanelHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandSelectNumber;

/**
 * Context sensitive GUI for the camera 
 * @author Dan Royer
 *
 */
public class CameraPanel extends JPanel implements ActionListener, ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8529190569816182683L;
	
	private Camera cam;
	
	private UserCommandSelectNumber nearz;
	private UserCommandSelectNumber farz;
	private UserCommandSelectNumber fov;
	
	public CameraPanel(RobotOverlord ro,Camera cam) {
		super();

		this.cam=cam;
	
		this.setName("Camera");
		this.setLayout(new GridBagLayout());
		
		GridBagConstraints con1 = PanelHelper.getDefaultGridBagConstraints();
		
		nearz = new UserCommandSelectNumber(ro,"Near Z",(float)cam.getNearZ());
		farz = new UserCommandSelectNumber(ro,"Far Z",(float)cam.getFarZ());
		fov = new UserCommandSelectNumber(ro,"FOV",(float)cam.getFOV());
		
		this.add(nearz,con1);
		con1.gridy++;
		this.add(farz,con1);
		con1.gridy++;
		this.add(fov,con1);

		nearz.addChangeListener(this);
		farz.addChangeListener(this);
		fov.addChangeListener(this);
		/*
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
		
		PanelHelper.ExpandLastChild(this, con1);
	}

	@Override
	public void actionPerformed(ActionEvent e) {	
		/*
		Object subject = e.getSource();	
		if( subject == buttonFlyUp			) {	camera.move_up     = ( camera.move_up     == 1 ) ? 0 :  1;		}
		if( subject == buttonFlyDown		) {	camera.move_up     = ( camera.move_up     ==-1 ) ? 0 : -1;		}
		if( subject == buttonFlyLeft		) {	camera.move_left   = ( camera.move_left   == 1 ) ? 0 :  1;		}
		if( subject == buttonFlyRight		) {	camera.move_left   = ( camera.move_left   ==-1 ) ? 0 : -1;		}
		if( subject == buttonFlyForward		) {	camera.move_forward= ( camera.move_forward== 1 ) ? 0 :  1;		}
		if( subject == buttonFlyBackward	) {	camera.move_forward= ( camera.move_forward==-1 ) ? 0 : -1;		}
		if( subject == buttonLookDown		) {	camera.tilt_dir    = ( camera.tilt_dir    ==-1 ) ? 0 : -1;		}
		if( subject == buttonLookUp			) {	camera.tilt_dir    = ( camera.tilt_dir    == 1 ) ? 0 :  1;		}
		if( subject == buttonLookLeft		) {	camera.pan_dir     = ( camera.pan_dir     ==-1 ) ? 0 : -1;		}
		if( subject == buttonLookRight		) {	camera.pan_dir     = ( camera.pan_dir     == 1 ) ? 0 :  1;		}
		*/
	}
	
	
	public void updateFields() {}


	@Override
	public void stateChanged(ChangeEvent e) {
		Object source = e.getSource();
		if(source == nearz) {	cam.setNearZ(nearz.getValue());  }
		if(source == farz ) {	cam.setFarZ (farz.getValue ());  }
		if(source == fov  ) {	cam.setFOV  (fov.getValue  ());  }
	}
}
