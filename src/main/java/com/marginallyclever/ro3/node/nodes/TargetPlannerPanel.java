package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.convenience.helpers.StringHelper;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.NodePanelHelper;
import com.marginallyclever.ro3.node.nodes.limbsolver.LimbSolver;
import com.marginallyclever.ro3.view.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;

/**
 * <p>{@link TargetPlannerPanel} displays controls to run a {@link TargetPlanner}.</p>
 */
public class TargetPlannerPanel extends JPanel implements ActionListener {
    private static final Logger logger = LoggerFactory.getLogger(TargetPlannerPanel.class);
    private final TargetPlanner targetPlanner;
    private final JToggleButton runButton = new JToggleButton();
    private Timer timer;
    private final JLabel previousExecutionTimeLabel = new JLabel();
    private final JLabel executionTimeLabel = new JLabel();

    public TargetPlannerPanel(TargetPlanner targetPlanner) {
        super(new GridBagLayout());
        this.targetPlanner = targetPlanner;
        
        this.setName(TargetPlanner.class.getSimpleName());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.gridwidth=1;

        NodePanelHelper.addNodeSelector(this, "LimbSolver", targetPlanner.getSolver(), LimbSolver.class, gbc,targetPlanner);
        gbc.gridy++;
        NodePanelHelper.addNodeSelector(this, "Path start", targetPlanner.getPathStart(), Node.class, gbc,targetPlanner);
        gbc.gridy++;
        var selector = NodePanelHelper.addNodeSelector(this, "Next goal", targetPlanner.getNextGoal(), Pose.class, gbc,targetPlanner);
        selector.setEditable(false);

        // Run button
        gbc.gridy++;
        runButton.addActionListener(e -> {
            boolean isRunning = targetPlanner.isRunning();
            if (!isRunning) {
                targetPlanner.startRun();
                previousExecutionTimeLabel.setText(StringHelper.formatTime(targetPlanner.getPreviousExecutionTime()));
                setRunButtonText();
            } else {
                targetPlanner.stopRun();
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
        executionTimeLabel.setText( StringHelper.formatTime(targetPlanner.getExecutionTime()) );
        NodePanelHelper.addLabelAndComponent(this, "Execution time", executionTimeLabel, gbc);
        gbc.gridy++;
        previousExecutionTimeLabel.setText( StringHelper.formatTime(targetPlanner.getPreviousExecutionTime()) );
        NodePanelHelper.addLabelAndComponent(this, "Previous time", previousExecutionTimeLabel, gbc);
    }

    private void setRunButtonText() {
        runButton.setSelected(targetPlanner.isRunning());
        runButton.setText(targetPlanner.isRunning() ? "Stop" : "Run");
    }

    @Override
    public void addNotify() {
        super.addNotify();
        // timer
        timer = new Timer(100, (e)-> {
            if (targetPlanner.isRunning()) {
                executionTimeLabel.setText(StringHelper.formatTime(targetPlanner.getExecutionTime()));
            }
        });
        timer.start();
        targetPlanner.addActionListener(this);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        timer.stop();
        targetPlanner.removeActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals("finished")) {
            logger.debug("Stop detected");
            setRunButtonText();
        }
    }
}
