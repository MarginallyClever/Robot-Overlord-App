package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.convenience.helpers.StringHelper;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.marlinrobotarm.MarlinRobotArm;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.util.List;

/**
 * {@link TargetPlanner} knows about a {@link LimbSolver}.  It moves the {@link LimbSolver#target} to a destination.
 * It waits for the LimbSolver to reach the destination before moving on to the next destination (by subscribing to
 * {@link LimbSolver} ActionEvent "arrivedAtGoal".
 */
public class TargetPlanner extends Node implements ActionListener {
    private static final Logger logger = LoggerFactory.getLogger(TargetPlanner.class);
    private final NodePath<LimbSolver> solver = new NodePath<>(this, LimbSolver.class);
    private final NodePath<Node> pathStart = new NodePath<>(this, Node.class);
    private final NodePath<Pose> nextGoal = new NodePath<>(this, Pose.class);  // relative to pathStart
    private boolean isRunning = false;
    private double executionTime = 0;
    private double previousExecutionTime = 0;

    public TargetPlanner() {
        this("TargetPlanner");
    }

    public TargetPlanner(String name) {
        super(name);
    }

    @Override
    public void getComponents(List<JPanel> list) {
        JPanel pane = new JPanel(new GridBagLayout());
        list.add(pane);
        pane.setName(MarlinRobotArm.class.getSimpleName());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.gridwidth=1;

        addNodeSelector(pane, "LimbSolver", solver, LimbSolver.class, gbc);
        gbc.gridy++;
        addNodeSelector(pane, "Path start", pathStart, Node.class, gbc);
        gbc.gridy++;
        var selector = addNodeSelector(pane, "Next goal", nextGoal, Pose.class, gbc);
        selector.setEditable(false);

        var previousExecutionTimeLabel = new JLabel(StringHelper.formatTime(previousExecutionTime));
        var executionTimeLabel = new JLabel(StringHelper.formatTime(executionTime));
        // timer
        Timer timer = new Timer(100, (e)-> {
            if (isRunning) {
                executionTime += 0.1;
                executionTimeLabel.setText(StringHelper.formatTime(executionTime));
            }
        });

        // Run button
        gbc.gridy++;
        JToggleButton runButton = new JToggleButton();
        runButton.addActionListener(e -> {
            isRunning = runButton.isSelected();
            if (isRunning) {
                startRun();
                previousExecutionTimeLabel.setText(StringHelper.formatTime(previousExecutionTime));
                timer.start();
            } else {
                stopRun();
                timer.stop();
            }
            setRunButtonText(runButton);
        });
        setRunButtonText(runButton);

        runButton.addHierarchyListener(e -> {
            if ((HierarchyEvent.SHOWING_CHANGED & e.getChangeFlags()) !=0
                    && !runButton.isShowing()) {
                timer.stop();
            }
        });

        gbc.gridx=0;
        gbc.gridwidth=2;
        pane.add(runButton,gbc);
        gbc.gridy++;
        gbc.gridwidth=1;
        addLabelAndComponent(pane, "Execution time", executionTimeLabel, gbc);
        gbc.gridy++;
        addLabelAndComponent(pane, "Previous time", previousExecutionTimeLabel, gbc);

        super.getComponents(list);
    }

    private void setRunButtonText(JToggleButton runButton) {
        runButton.setText( isRunning ? "Stop" : "Run");
    }

    private void startRun() {
        logger.debug("Starting run");
        previousExecutionTime = executionTime;
        executionTime = 0;
        if(solver.getSubject()!=null) {
            LimbSolver mySolver = this.solver.getSubject();
            mySolver.addActionListener( this );

            // set nextGoal to the first child of type Pose
            nextGoal.setPath(pathStart.getPath());
            onSolverDone();
        }
    }

    /**
     * The solver has reached a target, so we need to find the next target.
     */
    private void onSolverDone() {
        findNextGoalOrStop();
        if(solver.getSubject()!=null) {
            LimbSolver mySolver = this.solver.getSubject();
            mySolver.setTarget(nextGoal.getSubject());
        }
    }

    private void findNextGoalOrStop() {
        if(nextGoal.getSubject()==null) return;

        // nextGoal has just been reached.  Find the next goal.
        // look in children, first.
        var kids = nextGoal.getSubject().getChildren();
        if(!kids.isEmpty()) {
            int index=0;
            while(index<kids.size() && !(kids.get(index) instanceof Pose)) {
                index++;
            }
            if(index<kids.size()) {
                // set to first viable child.
                nextGoal.setRelativePath(this, (Pose)kids.get(0));
                return;
            }
        }

        // move on to the next sibling of type Pose
        Node parent = nextGoal.getSubject().getParent();
        if(parent==null) {
            // no siblings.  stop!
            stopRun();
            return;
        }
        // what is my index?
        kids = parent.getChildren();
        int index = kids.indexOf(nextGoal.getSubject());
        if(index<0 || index>=kids.size()) {
            // nextGoal is not longer a child of parent?  Stop!
            stopRun();
            return;
        }

        // nextGoal is a child of parent.  Set nextGoal to the next valid sibling.
        index++;
        while(index<kids.size() && !(kids.get(index) instanceof Pose)) {
            index++;
        }
        if(index==kids.size()) {
            // no more children of type Pose.  Stop!
            stopRun();
            return;
        }
        // done!
        nextGoal.setRelativePath(this, (Pose)kids.get(index));
    }

    private void stopRun() {
        logger.debug("Stopping run");
        isRunning=false;
        nextGoal.setPath(pathStart.getPath());

        if(solver.getSubject()!=null) {
            LimbSolver mySolver = this.solver.getSubject();
            mySolver.removeActionListener(this);
        }

        fireActionEvent("arrivedAtGoal");
    }

    @Override
    public JSONObject toJSON() {
        var json = super.toJSON();
        if(solver.getSubject()!=null) json.put("solver",solver.getPath());
        if(pathStart.getSubject()!=null) json.put("pathStart",pathStart.getPath());
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("solver")) solver.setPath(from.getString("solver"));
        if(from.has("pathStart")) pathStart.setPath(from.getString("pathStart"));
    }

    public void addActionListener(ActionListener l) {
        listeners.add(ActionListener.class,l);
    }

    public void removeActionListener(ActionListener l) {
        listeners.remove(ActionListener.class,l);
    }

    private void fireActionEvent(String name) {
        var e = new ActionEvent(this,ActionEvent.ACTION_PERFORMED,name);
        for(ActionListener l : listeners.getListeners(ActionListener.class)) {
            l.actionPerformed(e);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals("arrivedAtGoal")) {
            onSolverDone();
        }
    }
}
