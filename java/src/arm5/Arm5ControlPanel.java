package arm5;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

public class Arm5ControlPanel extends JPanel implements ActionListener {
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 257878994328366520L;
	
	private JButton arm5modeSwitch;
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
	private JButton arm5Sync;

	
	private JButton createButton(String name) {
		JButton b = new JButton(name);
		b.addActionListener(this);
		return b;
	}


	public Arm5ControlPanel() {
		this.setLayout(new GridLayout(0,1));
		this.add(arm5Sync = createButton("Sync"));
		this.add(arm5modeSwitch = createButton("[FK] /  IK "));
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
	}


	// arm5 controls
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();		

		MainGUI gui = MainGUI.getSingleton();
		World world = gui.world;
		
		if( subject == arm5modeSwitch ) {
			world.robot0.moveMode = !world.robot0.moveMode;
			
			if(world.robot0.moveMode) {
				arm5modeSwitch.setText(" FK  / [IK]");
			} else {
				arm5modeSwitch.setText("[FK] /  IK ");
			}
		}
		
		if( subject == arm5Apos ) {
			world.robot0.iDown = ( !world.robot0.iDown ) ? true : false;
			world.robot0.kDown = false;
		}
		if( subject == arm5Aneg ) {
			world.robot0.kDown = ( !world.robot0.kDown ) ? true : false;
			world.robot0.iDown = false;
		}
		
		if( subject == arm5Bpos ) {
			world.robot0.oDown = ( !world.robot0.oDown ) ? true : false;
			world.robot0.gDown = false;
		}
		if( subject == arm5Bneg ) {
			world.robot0.lDown = ( !world.robot0.lDown ) ? true : false;
			world.robot0.oDown = false;
		}
		
		if( subject == arm5Cpos ) {
			world.robot0.tDown = ( !world.robot0.tDown ) ? true : false;
			world.robot0.gDown = false;
		}
		if( subject == arm5Cneg ) {
			world.robot0.gDown = ( !world.robot0.gDown ) ? true : false;
			world.robot0.tDown = false;
		}
		
		if( subject == arm5Dpos ) {
			world.robot0.rDown = ( !world.robot0.rDown ) ? true : false;
			world.robot0.fDown = false;
		}
		if( subject == arm5Dneg ) {
			world.robot0.fDown = ( !world.robot0.fDown ) ? true : false;
			world.robot0.rDown = false;
		}
		
		if( subject == arm5Epos ) {
			world.robot0.yDown = ( !world.robot0.yDown ) ? true : false;
			world.robot0.hDown = false;
		}
		if( subject == arm5Eneg ) {
			world.robot0.hDown = ( !world.robot0.hDown ) ? true : false;
			world.robot0.yDown = false;
		}
		if( subject == arm5Sync ) {
			world.robot0.SendCommand("R1\n");;
		}
	}
}
