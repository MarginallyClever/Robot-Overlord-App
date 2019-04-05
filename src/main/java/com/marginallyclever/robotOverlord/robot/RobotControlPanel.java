package com.marginallyclever.robotOverlord.robot;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

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
	protected transient JButton buttonKeyframeAdd;
	protected transient JButton buttonKeyframeDelete;

	protected transient JButton buttonAnimateReverse;
	protected transient JButton buttonAnimatePlayPause;
	protected transient JButton buttonAnimateFastForward;

	protected transient JButton buttonKeyframesNew;
	protected transient JButton buttonKeyframesLoad;
	protected transient JButton buttonKeyframesSave;
	protected transient JButton buttonShowKeyframes;

	private Robot robot = null;

	public RobotControlPanel(RobotOverlord gui, Robot robot) {
		this.robot = robot;

		this.setBorder(new EmptyBorder(0, 0, 0, 0));
		this.setLayout(new GridBagLayout());

		CollapsiblePanel p;
		GridBagConstraints con1, con2;

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;

		CollapsiblePanel robotPanel = new CollapsiblePanel("Robot");
		this.add(robotPanel, c);
		JPanel contents = robotPanel.getContentPane();

		con1 = new GridBagConstraints();
		con1.gridx = 0;
		con1.gridy = 0;
		con1.weightx = 1;
		con1.weighty = 1;
		con1.fill = GridBagConstraints.HORIZONTAL;
		// con1.anchor=GridBagConstraints.CENTER;

		buttonConnect = createButton(Translator.get("ButtonConnect"));
		contents.add(buttonConnect, con1);
		con1.gridy++;

		p = new CollapsiblePanel("Animate");
		contents.add(p, con1);
		con1.gridy++;

		con2 = new GridBagConstraints();
		con2.gridx = 0;
		con2.gridy = 0;
		con2.weightx = 0.2;
		con2.weighty = 1;
		con2.fill = GridBagConstraints.HORIZONTAL;
		con2.anchor = GridBagConstraints.CENTER;

		p.getContentPane().add(buttonAnimateReverse = createButton(Translator.get("<<")), con2);
		con2.gridx++; // could be "\u23EA"
		p.getContentPane().add(buttonAnimatePlayPause = createButton(Translator.get("> / ||")), con2);
		con2.gridx++; // could be "\u23EF"
		p.getContentPane().add(buttonAnimateFastForward = createButton(Translator.get(">>")), con2);
		con2.gridx++; // could be "\u23E9"

		p = new CollapsiblePanel("Keyframes");
		contents.add(p, con1);
		con1.gridy++;

		con2 = new GridBagConstraints();
		con2.gridx = 0;
		con2.gridy = 0;
		con2.weightx = 0;
		con2.weighty = 1;
		con2.fill = GridBagConstraints.HORIZONTAL;
		con2.anchor = GridBagConstraints.CENTER;
		con2.gridwidth = 3;

		p.getContentPane().add(buttonKeyframesNew = createButton(Translator.get("New")), con2);
		con2.gridx += con2.gridwidth;
		p.getContentPane().add(buttonKeyframesLoad = createButton(Translator.get("Load")), con2);
		con2.gridx += con2.gridwidth;
		p.getContentPane().add(buttonKeyframesSave = createButton(Translator.get("Save")), con2);
		con2.gridx += con2.gridwidth;
		p.getContentPane().add(buttonShowKeyframes = createButton(Translator.get("Show")), con2);
		con2.gridx += con2.gridwidth;

		con2.gridx = 0;
		con2.gridy++;
		con2.gridwidth = 3;
		con2.weightx = 0.25;
		p.getContentPane().add(buttonKeyframeFirst = createButton(Translator.get("|<")), con2);
		con2.gridx += con2.gridwidth;
		p.getContentPane().add(buttonKeyframePrev = createButton(Translator.get("<")), con2);
		con2.gridx += con2.gridwidth;
		p.getContentPane().add(buttonKeyframeNext = createButton(Translator.get(">")), con2);
		con2.gridx += con2.gridwidth;
		p.getContentPane().add(buttonKeyframeLast = createButton(Translator.get(">|")), con2);
		con2.gridx += con2.gridwidth;

		con2.gridy++;
		con2.gridx = 0;
		con2.gridwidth = 6;
		con2.weightx = 0.5;
		p.getContentPane().add(buttonKeyframeAdd = createButton(Translator.get("+")), con2);
		con2.gridx += con2.gridwidth;
		p.getContentPane().add(buttonKeyframeDelete = createButton(Translator.get("-")), con2);
		con2.gridx += con2.gridwidth;
	}

	private JButton createButton(String name) {
		JButton b = new JButton(name);

		// Font font = new Font("Segoe UI Symbol",
		// Font.PLAIN,b.getFont().getSize());
		// .b.setFont(font);

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

		if (subject == buttonConnect) {
			if (robot.getConnection() != null) {
				buttonConnect.setText(Translator.get("ButtonConnect"));
				robot.closeConnection();
			} else {
				robot.openConnection();
				if (robot.getConnection() != null) {
					buttonConnect.setText(Translator.get("ButtonDisconnect"));
				}
			}
			return;
		}
		if (subject == buttonKeyframeFirst) {
			robot.setKeyframeIndex(0);
			robot.setKeyframeT(0);
			robot.updatePose();
		}
		if (subject == buttonKeyframePrev) {
			robot.setKeyframeIndex(robot.getKeyframeIndex() - 1);
			robot.setKeyframeT(0);
			robot.updatePose();
		}
		if (subject == buttonKeyframeNext) {
			robot.setKeyframeIndex(robot.getKeyframeIndex() + 1);
			robot.setKeyframeT(0);
			robot.updatePose();
		}
		if (subject == buttonKeyframeLast) {
			robot.setKeyframeIndex(robot.getKeyframeSize() - 1);
			robot.setKeyframeT(0);
			robot.updatePose();
		}

		if (subject == buttonKeyframeAdd) {
			robot.keyframeAddNow();
		}
		if (subject == buttonKeyframeDelete) {
			robot.keyframeDelete();
		}

		if (subject == buttonKeyframesNew) {
			newKeyframes();
		}
		if (subject == buttonKeyframesSave) {
			saveKeyframes();
		}
		if (subject == buttonKeyframesLoad) {
			loadKeyframes();
		}
		if (subject == buttonShowKeyframes) {
			toggleShowKeyframes();
		}

		if (subject == buttonAnimateFastForward) {
			double aSpeed = robot.getAnimationSpeed();
			robot.setAnimationSpeed(aSpeed <= 0 ? 1 : aSpeed + 1);
		}
		if (subject == buttonAnimatePlayPause) {
			robot.setAnimationSpeed(robot.getAnimationSpeed() == 0 ? 1 : 0);
		}
		if (subject == buttonAnimateReverse) {
			double aSpeed = robot.getAnimationSpeed();
			robot.setAnimationSpeed(aSpeed >= 0 ? -1 : aSpeed - 1);
		}

		System.out.println("K" + robot.getKeyframeIndex() + "\tT" + robot.getKeyframeT());
	}

	protected void keyframeEditSetEnable(boolean isEnabled) {
		buttonKeyframesNew.setEnabled(isEnabled);
		buttonKeyframesSave.setEnabled(isEnabled);
		buttonKeyframesLoad.setEnabled(isEnabled);

		buttonKeyframeDelete.setEnabled(isEnabled);
		buttonKeyframeAdd.setEnabled(isEnabled);

		buttonKeyframeFirst.setEnabled(isEnabled);
		buttonKeyframePrev.setEnabled(isEnabled);
		buttonKeyframeNext.setEnabled(isEnabled);
		buttonKeyframeLast.setEnabled(isEnabled);
	}

	public void toggleShowKeyframes() {
		// TODO toggle keyframe visibility
		robot.setIsDrawingKeyframes(!robot.getIsDrawingKeyframes());
	}

	public void newKeyframes() {
		// move robot to start of first keyframe
		robot.setKeyframeIndex(0);
		robot.setKeyframeT(0);
		robot.prepareMove(0);

		// wipe out all keyframes
		robot.keyframes.clear();
	}

	public void saveKeyframes() {
		// open save dialog
		JFileChooser fc = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Keyframe data", "kfd");
		fc.setFileFilter(filter);
		int returnVal = fc.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String filename = fc.getSelectedFile().getAbsolutePath();
			if (filename.endsWith(".kfd"))
				filename += ".kfd";
			// if save is not cancelled, serialize the keyframe data
			robot.saveKeyframes(filename);
		}
	}

	public void loadKeyframes() {
		// open save dialog
		JFileChooser fc = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Keyframe data", "kfd");
		fc.setFileFilter(filter);
		int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// if save is not cancelled, serialize the keyframe data
			robot.loadKeyframes(fc.getSelectedFile().getAbsolutePath());
		}
	}
}
