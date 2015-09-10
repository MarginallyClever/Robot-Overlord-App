package arm5;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Arm5ControlPanel extends JPanel implements ActionListener {
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 257878994328366520L;

	private JButton arm5Apos;
	private JButton arm5Aneg;
	private JButton arm5Bpos;
	private JButton arm5Bneg;
	private JButton arm5Cpos;
	private JButton arm5Cneg;
	private JButton arm5Dpos;
	private JButton arm5Dneg;
	private JButton arm5Epos;
	private JButton arm5Eneg;
	
	private JButton arm5Xpos;
	private JButton arm5Xneg;
	private JButton arm5Ypos;
	private JButton arm5Yneg;
	private JButton arm5Zpos;
	private JButton arm5Zneg;

	
	private JButton createButton(String name) {
		JButton b = new JButton(name);
		b.addActionListener(this);
		return b;
	}


	public Arm5ControlPanel() {
		this.setLayout(new GridLayout(0,1));
		this.add(new JLabel("Forward Kinematics"));
		this.add(arm5Apos = createButton("A+"));
		this.add(arm5Aneg = createButton("A-"));
		this.add(arm5Bpos = createButton("B+"));
		this.add(arm5Bneg = createButton("B-"));
		this.add(arm5Cpos = createButton("C+"));
		this.add(arm5Cneg = createButton("C-"));
		this.add(arm5Dpos = createButton("D+"));
		this.add(arm5Dneg = createButton("D-"));
		this.add(arm5Epos = createButton("E+"));
		this.add(arm5Eneg = createButton("E-"));
		this.add(new JLabel("Inverse Kinematics"));
		this.add(arm5Xpos = createButton("X+"));
		this.add(arm5Xneg = createButton("X-"));
		this.add(arm5Ypos = createButton("Y+"));
		this.add(arm5Yneg = createButton("Y-"));
		this.add(arm5Zpos = createButton("Z+"));
		this.add(arm5Zneg = createButton("Z-"));
	}


	// arm5 controls
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();		

		MainGUI gui = MainGUI.getSingleton();
		World world = gui.world;		
		
		if( subject == arm5Apos ) {
			world.robot0.aPos = ( !world.robot0.aPos ) ? true : false;
			world.robot0.aNeg = false;
		}
		if( subject == arm5Aneg ) {
			world.robot0.aNeg = ( !world.robot0.aNeg ) ? true : false;
			world.robot0.aPos = false;
		}
		
		if( subject == arm5Bpos ) {
			world.robot0.bPos = ( !world.robot0.bPos ) ? true : false;
			world.robot0.bNeg = false;
		}
		if( subject == arm5Bneg ) {
			world.robot0.bNeg = ( !world.robot0.bNeg ) ? true : false;
			world.robot0.bPos = false;
		}
		
		if( subject == arm5Cpos ) {
			world.robot0.cPos = ( !world.robot0.cPos ) ? true : false;
			world.robot0.cNeg = false;
		}
		if( subject == arm5Cneg ) {
			world.robot0.cNeg = ( !world.robot0.cNeg ) ? true : false;
			world.robot0.cPos = false;
		}
		
		if( subject == arm5Dpos ) {
			world.robot0.dPos = ( !world.robot0.dPos ) ? true : false;
			world.robot0.dNeg = false;
		}
		if( subject == arm5Dneg ) {
			world.robot0.dNeg = ( !world.robot0.dNeg ) ? true : false;
			world.robot0.dPos = false;
		}
		
		if( subject == arm5Epos ) {
			world.robot0.ePos = ( !world.robot0.ePos ) ? true : false;
			world.robot0.eNeg = false;
		}
		if( subject == arm5Eneg ) {
			world.robot0.eNeg = ( !world.robot0.eNeg ) ? true : false;
			world.robot0.ePos = false;
		}
		

		
		if( subject == arm5Xpos ) {
			world.robot0.xPos = ( !world.robot0.xPos ) ? true : false;
			world.robot0.xNeg = false;
		}
		if( subject == arm5Xneg ) {
			world.robot0.xNeg = ( !world.robot0.xNeg ) ? true : false;
			world.robot0.xPos = false;
		}
		
		if( subject == arm5Ypos ) {
			world.robot0.yPos = ( !world.robot0.yPos ) ? true : false;
			world.robot0.yNeg = false;
		}
		if( subject == arm5Yneg ) {
			world.robot0.yNeg = ( !world.robot0.yNeg ) ? true : false;
			world.robot0.yPos = false;
		}
		
		if( subject == arm5Zpos ) {
			world.robot0.zPos = ( !world.robot0.zPos ) ? true : false;
			world.robot0.zNeg = false;
		}
		if( subject == arm5Zneg ) {
			world.robot0.zNeg = ( !world.robot0.zNeg ) ? true : false;
			world.robot0.zPos = false;
		}
	}
}
