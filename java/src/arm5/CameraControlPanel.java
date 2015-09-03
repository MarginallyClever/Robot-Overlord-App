package arm5;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
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
		this.add(buttonFlyUp = createButton("fly up"));
		this.add(buttonFlyDown = createButton("fly down"));
		this.add(buttonFlyLeft = createButton("fly left"));
		this.add(buttonFlyRight = createButton("fly right"));
		this.add(buttonFlyForward = createButton("fly forward"));
		this.add(buttonFlyBackward = createButton("fly backward"));

		this.add(buttonLookUp = createButton("look up"));
		this.add(buttonLookDown = createButton("look down"));
		this.add(buttonLookLeft = createButton("look left"));
		this.add(buttonLookRight = createButton("look right"));
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
