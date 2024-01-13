package com.marginallyclever.ro3.node.nodes.limbplanner;

import com.marginallyclever.convenience.helpers.StringHelper;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.NodePanelHelper;
import com.marginallyclever.ro3.node.nodes.Pose;
import com.marginallyclever.ro3.node.nodes.limbsolver.LimbSolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;

/**
 * <p>{@link LimbPlannerPanel} displays controls to run a {@link LimbPlanner}.</p>
 */
public class LimbPlannerPanel extends JPanel implements ActionListener {
    private static final Logger logger = LoggerFactory.getLogger(LimbPlannerPanel.class);
    private final LimbPlanner limbPlanner;
    private final JToggleButton runButton = new JToggleButton();
    private Timer timer;
    private final JLabel previousExecutionTimeLabel = new JLabel();
    private final JLabel executionTimeLabel = new JLabel();

    public LimbPlannerPanel(LimbPlanner limbPlanner) {
        super(new GridBagLayout());
        this.limbPlanner = limbPlanner;
        
        this.setName(LimbPlanner.class.getSimpleName());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.gridwidth=1;

        NodePanelHelper.addNodeSelector(this, "LimbSolver", limbPlanner.getSolver(), LimbSolver.class, gbc);
        gbc.gridy++;
        NodePanelHelper.addNodeSelector(this, "Path start", limbPlanner.getPathStart(), Pose.class, gbc);
        gbc.gridy++;
        var selector = NodePanelHelper.addNodeSelector(this, "Next goal", limbPlanner.getNextGoal(), Pose.class, gbc);
        selector.setEditable(false);

        // Run button
        gbc.gridy++;
        runButton.addActionListener(e -> {
            boolean isRunning = limbPlanner.isRunning();
            if (!isRunning) {
                limbPlanner.startRun();
                previousExecutionTimeLabel.setText(StringHelper.formatTime(limbPlanner.getPreviousExecutionTime()));
                setRunButtonText();
            } else {
                limbPlanner.stopRun();
                setRunButtonText();
            }
        });
        setRunButtonText();

        runButton.addHierarchyListener(e -> {
            if ((HierarchyEvent.SHOWING_CHANGED & e.getChangeFlags()) !=0
                    && !runButton.isShowing()) {
                timer.stop();
            }
        });

        gbc.gridx=0;
        gbc.gridwidth=2;
        this.add(runButton,gbc);
        gbc.gridy++;
        gbc.gridwidth=1;
        executionTimeLabel.setText( StringHelper.formatTime(limbPlanner.getExecutionTime()) );
        NodePanelHelper.addLabelAndComponent(this, "Execution time", executionTimeLabel, gbc);
        gbc.gridy++;
        previousExecutionTimeLabel.setText( StringHelper.formatTime(limbPlanner.getPreviousExecutionTime()) );
        NodePanelHelper.addLabelAndComponent(this, "Previous time", previousExecutionTimeLabel, gbc);
    }

    private void setRunButtonText() {
        runButton.setSelected(limbPlanner.isRunning());
        runButton.setText(limbPlanner.isRunning() ? "Stop" : "Run");
    }

    @Override
    public void addNotify() {
        super.addNotify();
        // timer
        timer = new Timer(100, (e)-> {
            if (limbPlanner.isRunning()) {
                executionTimeLabel.setText(StringHelper.formatTime(limbPlanner.getExecutionTime()));
            }
        });
        timer.start();
        limbPlanner.addActionListener(this);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        timer.stop();
        limbPlanner.removeActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals("finished")) {
            logger.debug("Stop detected");
            setRunButtonText();
        }
    }
}
