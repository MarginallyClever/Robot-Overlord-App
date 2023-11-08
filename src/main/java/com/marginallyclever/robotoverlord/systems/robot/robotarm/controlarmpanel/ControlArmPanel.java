package com.marginallyclever.robotoverlord.systems.robot.robotarm.controlarmpanel;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotoverlord.components.GCodePathComponent;
import com.marginallyclever.robotoverlord.components.RobotComponent;
import com.marginallyclever.robotoverlord.swing.translator.Translator;
import com.marginallyclever.robotoverlord.systems.robot.robotarm.controlarmpanel.jogpanel.JogPanel;
import com.marginallyclever.robotoverlord.systems.robot.robotarm.controlarmpanel.jointhistorypanel.JointHistoryPanel;
import com.marginallyclever.robotoverlord.systems.robot.robotarm.controlarmpanel.presentationlayer.PresentationFactory;
import com.marginallyclever.robotoverlord.systems.robot.robotarm.controlarmpanel.presentationlayer.PresentationLayer;
import com.marginallyclever.robotoverlord.systems.robot.robotarm.controlarmpanel.programpanel.ProgramPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Application layer for a robot.  A {@link JogPanel}, a {@link ProgramPanel}, and a {@link PresentationLayer} glued together.
 * @author Dan Royer
 */
public class ControlArmPanel extends JPanel {
	private final ProgramPanel programPanel;
	private final JointHistoryPanel jointHistory;

	private final JPanel presentationContainer = new JPanel(new BorderLayout());
	private final JPanel presentationSelection = new JPanel(new BorderLayout());
	private final JComboBox<String> presentationChoices = new JComboBox<>(PresentationFactory.AVAILABLE_PRESENTATIONS);
	private final JPanel presentationContainerCenter = new JPanel(new BorderLayout());
	private PresentationLayer presentationLayer;

	private final JButton bHome = new JButton(Translator.get("RobotPanel.Home"));
	private final JButton bRewind = new JButton(Translator.get("RobotPanel.Rewind"));
	private final JButton bStart = new JButton(Translator.get("RobotPanel.Play"));
	private final JButton bStep = new JButton(Translator.get("RobotPanel.Step"));
	private final JButton bPause = new JButton(Translator.get("RobotPanel.Pause"));
	private final JProgressBar progress = new JProgressBar(0, 100);

	private boolean isRunning = false;
	private final RobotComponent myRobot;
	
	public ControlArmPanel(RobotComponent robot, GCodePathComponent path) {
		super();
		this.myRobot = robot;

		JogPanel jogPanel = new JogPanel(robot);
		programPanel = new ProgramPanel(robot,path);
		jointHistory = new JointHistoryPanel(robot);
		setupPresentationContainer();

		JTabbedPane pane = new JTabbedPane();
		pane.addTab(Translator.get("RobotPanel.Jog"), jogPanel);
		pane.addTab(Translator.get("RobotPanel.Program"), programPanel);
		pane.addTab(Translator.get("RobotPanel.Connect"), presentationContainer);
		pane.addTab(Translator.get("RobotPanel.History"), jointHistory);

		this.setLayout(new BorderLayout());
		this.add(pane, BorderLayout.CENTER);
		this.add(getToolBar(), BorderLayout.NORTH);
		this.add(progress, BorderLayout.SOUTH);
	}

	private void setupPresentationContainer() {
		presentationSelection.add(new JLabel("Presentation Layer:"),BorderLayout.WEST);
		presentationSelection.add(presentationChoices,BorderLayout.CENTER);
		presentationContainer.add(presentationSelection,BorderLayout.NORTH);
		presentationContainer.add(presentationContainerCenter,BorderLayout.CENTER);

		presentationChoices.addActionListener(e->changePresentationLayer());
		changePresentationLayer();
	}

	private void changePresentationLayer() {
		if(presentationLayer!=null) {
			presentationLayer.closeConnection();
		}
		String selection = (String) presentationChoices.getSelectedItem();
		assert selection != null;
		presentationLayer = PresentationFactory.createPresentation(selection,myRobot);
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

		presentationContainerCenter.removeAll();
		presentationContainerCenter.add(presentationLayer.getPanel(),BorderLayout.CENTER);
		presentationContainerCenter.revalidate();
		presentationContainerCenter.repaint();
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
		Translator.start();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception ignored) {}

		JFrame frame = new JFrame(ControlArmPanel.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new ControlArmPanel(new RobotComponent(),null));
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
