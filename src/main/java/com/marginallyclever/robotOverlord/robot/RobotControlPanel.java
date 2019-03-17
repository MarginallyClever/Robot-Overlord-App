package com.marginallyclever.robotOverlord.robot;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.marginallyclever.robotOverlord.CollapsiblePanel;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.Translator;
import com.marginallyclever.robotOverlord.robot.Robot;

public class RobotControlPanel extends JPanel implements ActionListener, ChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4719253861336378906L;
	// connect/rescan/disconnect dialog options
	protected transient JButton buttonConnect;
	protected transient JButton buttonKeyframeFirst;
	protected transient JButton buttonKeyframePrev;
	protected transient JButton buttonKeyframeNext;
	protected transient JButton buttonKeyframeLast;
	protected transient JButton buttonAnimateReverse;
	protected transient JButton buttonAnimatePlayPause;
	protected transient JButton buttonAnimateFastForward;
	
	private Robot robot=null;

	public RobotControlPanel(RobotOverlord gui,Robot robot) {
		this.robot = robot; 

		this.setBorder(new EmptyBorder(0,0,0,0));
		this.setLayout(new GridBagLayout());
		
		CollapsiblePanel p;
		GridBagConstraints con1,con2;

		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=1;
		c.weighty=1;
		c.anchor=GridBagConstraints.NORTHWEST;
		c.fill=GridBagConstraints.HORIZONTAL;

		CollapsiblePanel robotPanel = new CollapsiblePanel("Robot");
		this.add(robotPanel,c);
		JPanel contents = robotPanel.getContentPane();
		
		con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=1;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		//con1.anchor=GridBagConstraints.CENTER;

        buttonConnect = createButton(Translator.get("ButtonConnect"));
        contents.add(buttonConnect,con1);
		con1.gridy++;
		
		
		p = new CollapsiblePanel("Keyframe");
		contents.add(p,con1);
		con1.gridy++;

		con2 = new GridBagConstraints();
		con2.gridx=0;
		con2.gridy=0;
		con2.weightx=0.25;
		con2.weighty=1;
		con2.fill=GridBagConstraints.HORIZONTAL;
		con2.anchor=GridBagConstraints.CENTER;
		
		p.getContentPane().add(buttonKeyframeFirst=createButton(Translator.get("|<")),con2);	con2.gridx++;
		p.getContentPane().add(buttonKeyframePrev=createButton(Translator.get("<")),con2);		con2.gridx++;
		p.getContentPane().add(buttonKeyframeNext=createButton(Translator.get(">")),con2);		con2.gridx++;
		p.getContentPane().add(buttonKeyframeLast=createButton(Translator.get(">|")),con2);		con2.gridx++;
		
		
		p = new CollapsiblePanel("Animate");
		contents.add(p,con1);
		con1.gridy++;

		con2 = new GridBagConstraints();
		con2.gridx=0;
		con2.gridy=0;
		con2.weightx=0.2;
		con2.weighty=1;
		con2.fill=GridBagConstraints.HORIZONTAL;
		con2.anchor=GridBagConstraints.CENTER;
		
		p.getContentPane().add(buttonAnimateReverse=createButton(Translator.get("<<")),con2);			con2.gridx++;
		p.getContentPane().add(buttonAnimatePlayPause=createButton(Translator.get(">/||")),con2);		con2.gridx++;
		p.getContentPane().add(buttonAnimateFastForward=createButton(Translator.get(">>")),con2);		con2.gridx++;
	}
	
	private JButton createButton(String name) {
		JButton b = new JButton(name);
		b.addActionListener(this);
		return b;
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();
		
		if(subject==buttonConnect) {
			if(robot.getConnection()!=null) {
				buttonConnect.setText(Translator.get("ButtonConnect"));
				robot.closeConnection();
			} else {
				robot.openConnection();
				if(robot.getConnection()!=null) {
					buttonConnect.setText(Translator.get("ButtonDisconnect"));
				}
			}
			return;
		}
		if(subject == buttonKeyframeFirst) {
			robot.setKeyframeIndex(0);
			robot.setKeyframeT(0);
		}
		if(subject == buttonKeyframePrev) {
			robot.setKeyframeIndex(robot.getKeyframeIndex()-1);
			robot.setKeyframeT(0);
		}
		if(subject == buttonKeyframeNext) {
			robot.setKeyframeIndex(robot.getKeyframeIndex()+1);
			robot.setKeyframeT(0);
		}
		if(subject == buttonKeyframeLast) {
			robot.setKeyframeIndex(robot.getKeyframeSize()-1);
			robot.setKeyframeT(0);
		}
		if(subject == buttonAnimateFastForward) {
			if(robot.animationSpeed<=0) {
				robot.animationSpeed=1;
			} else {
				robot.animationSpeed+=1;
			}
		}
		if(subject == buttonAnimatePlayPause) {
			robot.animationSpeed=(robot.animationSpeed==0)?1:0;
		}
		if(subject == buttonAnimateReverse) {
			robot.animationSpeed=-1;
		}
	}
}
