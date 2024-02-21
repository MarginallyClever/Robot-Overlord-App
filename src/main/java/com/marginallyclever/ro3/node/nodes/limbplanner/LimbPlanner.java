package com.marginallyclever.ro3.node.nodes.limbplanner;

import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.limbsolver.LimbSolver;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Objects;

/**
 * {@link LimbPlanner} knows about a {@link LimbSolver}.
 * It moves the {@link LimbSolver#setTarget(Pose)} to a destination.
 * It then waits for the {@link ActionEvent} "arrivedAtGoal" before moving on to the next destination.
 */
public class LimbPlanner extends Node implements ActionListener {
    private static final Logger logger = LoggerFactory.getLogger(LimbPlanner.class);
    private final NodePath<LimbSolver> solver = new NodePath<>(this, LimbSolver.class);
    private final NodePath<Pose> pathStart = new NodePath<>(this, Pose.class);
    private final NodePath<Pose> nextGoal = new NodePath<>(this, Pose.class);  // relative to pathStart
    private boolean isRunning = false;
    private double executionTime = 0;
    private double previousExecutionTime = 0;

    public LimbPlanner() {
        this("LimbPlanner");
    }

    public LimbPlanner(String name) {
        super(name);
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new LimbPlannerPanel(this));
        super.getComponents(list);
    }

    public void startRun() {
        if(solver.getSubject()==null) throw new IllegalArgumentException("Solver is null.");
        if(pathStart.getSubject()==null) throw new IllegalArgumentException("PathStart is null.");

        logger.debug("Starting run");
        previousExecutionTime = executionTime;
        executionTime = 0;
        if(solver.getSubject()!=null) {
            LimbSolver mySolver = solver.getSubject();
            mySolver.addActionListener( this );

            // set nextGoal to the first child of type Pose
            nextGoal.setUniqueID(pathStart.getUniqueID());
            isRunning=true;
            onSolverDone();
        }
    }

    /**
     * The solver has reached a target, so we need to find the next target.
     */
    private void onSolverDone() {
        setNextGoalOrStop();
        if(isRunning && solver.getSubject()!=null && nextGoal.getSubject()!=null) {
            logger.debug("Updating solver to {}",nextGoal.getSubject().getAbsolutePath());
            solver.getSubject().setTarget(nextGoal.getSubject());
        }
    }

    private void setNextGoalOrStop() {
        if(nextGoal.getSubject()==null) return;

        logger.debug("Finding next goal");

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
                var child = kids.get(0);
                logger.debug("set to first child {}.",child.getAbsolutePath());
                setNextGoal((Pose)child);
                return;
            }
        }

        // move on to the next sibling of type Pose
        Node parent = nextGoal.getSubject().getParent();
        if(parent==null) {
            // no siblings.  stop!
            logger.debug("No siblings.");
            stopRun();
            return;
        }
        // what is my index?
        kids = parent.getChildren();
        int index = kids.indexOf(nextGoal.getSubject());
        if(index<0 || index>=kids.size()) {
            // nextGoal is not longer a child of parent?  Stop!
            logger.debug("Orphaned?!");
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
            logger.debug("No valid siblings.");
            stopRun();
            return;
        }
        // done!
        logger.debug("Sibling found.");
        setNextGoal((Pose)kids.get(index));
    }

    private void setNextGoal(Pose pose) {
        logger.debug("Setting next goal to {}",pose.getAbsolutePath());
        nextGoal.setSubject(pose);
        solver.getSubject().setTarget(nextGoal.getSubject());
        solver.getSubject().setIsAtGoal(false);
    }

    public void stopRun() {
        if(!isRunning) {
            logger.debug("Already stopped.");
            return;
        }
        logger.debug("Stopping run at "+executionTime+" seconds.");
        isRunning=false;
        nextGoal.setUniqueID(pathStart.getUniqueID());

        if(solver.getSubject()!=null) {
            LimbSolver mySolver = this.solver.getSubject();
            mySolver.removeActionListener(this);
        }

        fireFinished();
    }

    @Override
    public JSONObject toJSON() {
        var json = super.toJSON();
        if(solver.getSubject()!=null) json.put("solver",solver.getUniqueID());
        if(pathStart.getSubject()!=null) json.put("pathStart",pathStart.getUniqueID());
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("solver")) solver.setUniqueID(from.getString("solver"));
        if(from.has("pathStart")) pathStart.setUniqueID(from.getString("pathStart"));
    }

    public void addActionListener(ActionListener l) {
        listeners.add(ActionListener.class,l);
    }

    public void removeActionListener(ActionListener l) {
        listeners.remove(ActionListener.class,l);
    }

    private void fireFinished() {
        var e = new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"finished");
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

    public boolean isRunning() {
        return isRunning;
    }

    public double getExecutionTime() {
        return executionTime;
    }

    public double getPreviousExecutionTime() {
        return previousExecutionTime;
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        if (isRunning) {
            executionTime += dt;
        }
    }

    public NodePath<Pose> getPathStart() {
        return pathStart;
    }

    public NodePath<LimbSolver> getSolver() {
        return solver;
    }

    public NodePath<Pose> getNextGoal() {
        return nextGoal;
    }

    /**
     * Set the solver to use.
     * solver must be in the same node tree as this instance.
     * @param limbSolver the solver to use.
     */
    public void setSolver(LimbSolver limbSolver) {
        solver.setSubject(limbSolver);
    }

    /**
     * Set the start of the path.
     * pose must be in the same node tree as this instance.
     * @param pose the pose to use.
     */
    public void setPathStart(Pose pose) {
        pathStart.setSubject(pose);
    }

    public void setLinearVelocity(double v) {
        if(solver.getSubject()!=null) {
            solver.getSubject().setLinearVelocityMax(v);
        }
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/node/nodes/icons8-plan-16.png")));
    }
}
