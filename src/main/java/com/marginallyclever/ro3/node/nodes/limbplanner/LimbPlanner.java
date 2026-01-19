package com.marginallyclever.ro3.node.nodes.limbplanner;

import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.Limb;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Objects;

/**
 * {@link LimbPlanner} knows about a {@link Limb}.
 * It moves the {@link Limb#setTarget(Pose)} to a destination.
 * It then waits for the {@link ActionEvent} "arrivedAtGoal" before moving on to the next destination.
 */
public class LimbPlanner extends Node implements ActionListener {
    private static final Logger logger = LoggerFactory.getLogger(LimbPlanner.class);
    private final NodePath<Limb> limb = new NodePath<>(this, Limb.class);
    private final NodePath<Pose> pathContainer = new NodePath<>(this, Pose.class);
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
        Limb myLimb = limb.getSubject();
        if(myLimb == null) throw new IllegalArgumentException("Solver is null.");
        var myPathContainer = pathContainer.getSubject();
        if(myPathContainer == null) throw new IllegalArgumentException("PathStart is null.");

        logger.debug("Starting run");
        previousExecutionTime = executionTime;
        executionTime = 0;

        myLimb.addActionListener( this );

        // set nextGoal to the first child of type Pose
        nextGoal.setUniqueIDByNode(myPathContainer);
        isRunning = true;
        onSolverDone();
    }

    /**
     * The solver has reached a target, so we need to find the next target.
     */
    private void onSolverDone() {
        setNextGoalOrStop();
        if(isRunning && limb.getSubject()!=null && nextGoal.getSubject()!=null) {
            setTargetToNextGoal();
        }
    }

    private void setTargetToNextGoal() {
        var myNextGoal = nextGoal.getSubject();
        if(myNextGoal==null) return;
        var limbTarget = limb.getSubject().getTarget().getSubject();
        if(limbTarget==null) return;

        logger.debug("Updating target to {}",myNextGoal.getAbsolutePath());
        limbTarget.setWorld(myNextGoal.getWorld());
    }

    /**
     * Find the next goal in the path, or stop if there are no more goals.
     * pathContainer may have multiple nested children of type Pose.
     */
    private void setNextGoalOrStop() {
        var myNextGoal = nextGoal.getSubject();
        if(myNextGoal==null) return;

        logger.debug("Finding next goal");

        // Goal has been reached.  Find the next goal.  Look in children, first.
        var kids = myNextGoal.getChildren();
        if(!kids.isEmpty()) {
            int index=0;
            while(index<kids.size() && !(kids.get(index) instanceof Pose)) {
                index++;
            }
            if(index<kids.size()) {
                // set to first viable child.
                var child = kids.getFirst();
                logger.debug("set to first child {}.",child.getAbsolutePath());
                setNextGoal((Pose)child);
                return;
            }
        }

        // move on to the next sibling of type Pose
        Node parent = myNextGoal.getParent();
        if(parent==null) {
            // no siblings.  stop!
            logger.debug("NextGoal has no parent.");
            stopRun();
            return;
        }

        // what is my index?
        kids = parent.getChildren();
        int index = kids.indexOf(nextGoal.getSubject());
        if(index<0 || index>=kids.size()) {
            // nextGoal is not longer a child of parent?  Stop!
            logger.debug("NextGoal orphaned?!");
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
        nextGoal.setUniqueIDByNode(pose);
        setTargetToNextGoal();
        limb.getSubject().setIsAtGoal(false);
    }

    public void stopRun() {
        if(!isRunning) {
            logger.debug("Already stopped.");
            return;
        }
        logger.debug("Stopping run at "+executionTime+" seconds.");
        isRunning=false;
        nextGoal.setUniqueID(pathContainer.getUniqueID());

        if(limb.getSubject()!=null) {
            Limb mySolver = this.limb.getSubject();
            mySolver.removeActionListener(this);
        }

        fireFinished();
    }

    @Override
    public JSONObject toJSON() {
        var json = super.toJSON();
        if(limb.getSubject()!=null) json.put("solver", limb.getUniqueID());
        if(pathContainer.getSubject()!=null) json.put("pathStart", pathContainer.getUniqueID());
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("solver")) limb.setUniqueID(from.getString("solver"));
        if(from.has("pathStart")) pathContainer.setUniqueID(from.getString("pathStart"));
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

    public NodePath<Pose> getPathContainer() {
        return pathContainer;
    }

    public NodePath<Limb> getLimb() {
        return limb;
    }

    public NodePath<Pose> getNextGoal() {
        return nextGoal;
    }

    /**
     * Set the solver to use.
     * solver must be in the same node tree as this instance.
     * @param Limb the solver to use.
     */
    public void setLimb(Limb Limb) {
        limb.setUniqueIDByNode(Limb);
    }

    /**
     * Set the path container, which must be a {@link Pose}.
     * @param pose the pose to use.
     */
    public void setPathContainer(Pose pose) {
        pathContainer.setUniqueIDByNode(pose);
    }

    public void setLinearVelocity(double v) {
        if(limb.getSubject()!=null) {
            limb.getSubject().setLinearVelocity(v);
        }
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/node/nodes/icons8-plan-16.png")));
    }
}
