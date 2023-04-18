package com.marginallyclever.robotoverlord.components.robot.robotarm.robotArmInterface;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotoverlord.components.robot.RobotComponent;
import com.marginallyclever.robotoverlord.components.robot.robotarm.robotArmInterface.jogInterface.JogInterface;
import com.marginallyclever.robotoverlord.robots.Robot;
import com.marginallyclever.robotoverlord.components.robot.robotarm.robotArmInterface.marlinInterface.MarlinInterface;
import com.marginallyclever.robotoverlord.components.robot.robotarm.robotArmInterface.programInterface.ProgramInterface;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;

public class RobotArmInterface extends JPanel {
	@Serial
	private static final long serialVersionUID = 1L;
	private final MarlinInterface marlinInterface;
	private final JogInterface jogInterface;
	private final ProgramInterface programInterface;

	private final JButton bHome = new JButton("Home");
	private final JButton bRewind = new JButton("Rewind");
	private final JButton bStart = new JButton("Play");
	private final JButton bStep = new JButton("Step");
	private final JButton bPause = new JButton("Pause");
	private final JProgressBar progress = new JProgressBar(0, 100);

	private boolean isRunning = false;
	
	public RobotArmInterface(Robot robot) {
		super();
		
		marlinInterface = new MarlinInterface(robot);
		jogInterface = new JogInterface(robot);
		programInterface = new ProgramInterface(robot);
		
		JTabbedPane pane = new JTabbedPane();
		pane.addTab("MarlinInterface", marlinInterface);
		pane.addTab("JogInterface", jogInterface);
		pane.addTab("ProgramInterface", programInterface);

		this.setLayout(new BorderLayout());
		this.add(pane, BorderLayout.CENTER);
		this.add(getToolBar(), BorderLayout.NORTH);
		this.add(progress, BorderLayout.SOUTH);

		marlinInterface.addListener((e) -> {
			if (marlinInterface.isIdleCommand(e)) {
				// logger.debug("PlotterControls heard idle");
				if (isRunning) {
					// logger.debug("PlotterControls is running");
					step();
				}
			}
			updateProgressBar();
		});
	}

	private JToolBar getToolBar() {
		JToolBar bar = new JToolBar();
		bar.setFloatable(false);
		bar.add(bHome);
		bar.addSeparator();
		bar.add(bRewind);
		bar.add(bStart);
		bar.add(bPause);
		bar.add(bStep);

		bHome.addActionListener((e) -> home());
		bRewind.addActionListener((e) -> rewind());
		bStart.addActionListener((e) -> play());
		bPause.addActionListener((e) -> pause());
		bStep.addActionListener((e) -> step());

		updateButtonStatus();

		return bar;
	}

	private void updateProgressBar() {
		progress.setValue((int) (100.0 * programInterface.getLineNumber() / programInterface.getMoveCount()));
	}

	private void step() {
		programInterface.step();
		if (programInterface.getLineNumber() == -1) {
			// done
			pause();
		}
	}

	private void pause() {
		isRunning = false;
		updateButtonStatus();
	}

	public boolean isRunning() {
		return isRunning;
	}

	private void play() {
		isRunning = true;
		updateButtonStatus();
		rewindIfNoProgramLineSelected();
		step();
	}

	private void rewindIfNoProgramLineSelected() {
		if (programInterface.getLineNumber() == -1) {
			programInterface.rewind();
		}
	}

	private void rewind() {
		programInterface.rewind();
		progress.setValue(0);
	}

	private void home() {
		marlinInterface.sendGoHome();
	}

	private void updateButtonStatus() {
		bHome.setEnabled(!isRunning);
		bRewind.setEnabled(!isRunning);
		bStart.setEnabled(!isRunning);
		bPause.setEnabled(isRunning);
		bStep.setEnabled(!isRunning);		
	}

	public void closeConnection() {
		marlinInterface.closeConnection();
	}
	
	// TEST

	public static void main(String[] args) {
		Log.start();
		JFrame frame = new JFrame("RobotArmInterface");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {}
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new RobotArmInterface(new RobotComponent()));
		frame.pack();
		frame.setVisible(true);
	}
}
