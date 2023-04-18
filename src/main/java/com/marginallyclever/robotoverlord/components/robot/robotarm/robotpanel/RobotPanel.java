package com.marginallyclever.robotoverlord.components.robot.robotarm.robotpanel;

import com.marginallyclever.robotoverlord.components.robot.robotarm.robotpanel.presentationlayer.PresentationLayer;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotoverlord.components.RobotComponent;
import com.marginallyclever.robotoverlord.components.robot.robotarm.robotpanel.jogpanel.JogPanel;
import com.marginallyclever.robotoverlord.components.robot.robotarm.robotpanel.presentationlayer.PresentationFactory;
import com.marginallyclever.robotoverlord.robots.Robot;
import com.marginallyclever.robotoverlord.components.robot.robotarm.robotpanel.programpanel.programPanel;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;

/**
 * Application layer for a robot.  A {@link JogPanel}, a {@link programPanel}, and a {@link PresentationLayer} glued together.
 * @author Dan Royer
 */
public class RobotPanel extends JPanel {
	@Serial
	private static final long serialVersionUID = 1L;
	private final PresentationLayer presentationLayer;
	private final JogPanel jogPanel;
	private final programPanel programPanel;

	private final JButton bHome = new JButton("Home");
	private final JButton bRewind = new JButton("Rewind");
	private final JButton bStart = new JButton("Play");
	private final JButton bStep = new JButton("Step");
	private final JButton bPause = new JButton("Pause");
	private final JProgressBar progress = new JProgressBar(0, 100);

	private boolean isRunning = false;
	
	public RobotPanel(Robot robot) {
		super();
		
		presentationLayer = PresentationFactory.createPresentation("Marlin",robot);
		jogPanel = new JogPanel(robot);
		programPanel = new programPanel(robot);
		
		JTabbedPane pane = new JTabbedPane();
		pane.addTab("Jog", jogPanel);
		pane.addTab("Program", programPanel);
		pane.addTab("Presentation", presentationLayer.getPanel());

		this.setLayout(new BorderLayout());
		this.add(pane, BorderLayout.CENTER);
		this.add(getToolBar(), BorderLayout.NORTH);
		this.add(progress, BorderLayout.SOUTH);

		presentationLayer.addListener((e)-> {
			if (presentationLayer.isIdleCommand(e)) {
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
		progress.setValue((int) (100.0 * programPanel.getLineNumber() / programPanel.getMoveCount()));
	}

	private void step() {
		programPanel.step();
		if (programPanel.getLineNumber() == -1) {
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
		if (programPanel.getLineNumber() == -1) {
			programPanel.rewind();
		}
	}

	private void rewind() {
		programPanel.rewind();
		progress.setValue(0);
	}

	private void home() {
		presentationLayer.sendGoHome();
	}

	private void updateButtonStatus() {
		bHome.setEnabled(!isRunning);
		bRewind.setEnabled(!isRunning);
		bStart.setEnabled(!isRunning);
		bPause.setEnabled(isRunning);
		bStep.setEnabled(!isRunning);		
	}

	public void closeConnection() {
		presentationLayer.closeConnection();
	}
	
	// TEST

	public static void main(String[] args) {
		Log.start();
		JFrame frame = new JFrame("RobotArmInterface");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception ignored) {}
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new RobotPanel(new RobotComponent()));
		frame.pack();
		frame.setVisible(true);
	}
}
