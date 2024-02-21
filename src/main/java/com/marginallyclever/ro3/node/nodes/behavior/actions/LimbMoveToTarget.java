package com.marginallyclever.ro3.node.nodes.behavior.actions;

import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.behavior.Action;
import com.marginallyclever.ro3.node.nodes.limbsolver.LimbSolver;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Objects;

/**
 * <p>{@link LimbMoveToTarget} is a {@link Action} that moves a {@link LimbSolver} target to a {@link Pose}.</p>
 * <p>While the solver is working the action returns RUNNING.  When the target arrives at the pose, the action
 * returns SUCCESS.</p>
 * <p>When the action is attached to the scene tree, it looks at all parents and </p>
 */
public class LimbMoveToTarget extends Action implements ActionListener {
    private static final Logger logger = LoggerFactory.getLogger(LimbMoveToTarget.class);
    private final NodePath<Pose> target = new NodePath<>(this,Pose.class);
    private final NodePath<LimbSolver> solver = new NodePath<>(this,LimbSolver.class);
    private Status result = Status.RUNNING;

    public LimbMoveToTarget() {
        this("LimbMoveToTarget");
    }

    public LimbMoveToTarget(String name) {
        super(name);
    }

    @Override
    public Status tick() {
        logger.debug("tick {}",getAbsolutePath());

        if(result==Status.RUNNING) {
            var mySolver = solver.getSubject();
            var myTarget = target.getSubject();
            if(mySolver==null || myTarget==null) {
                result = Status.FAILURE;
            } else if (mySolver.getTarget().getSubject() != myTarget) {
                mySolver.setTarget(myTarget);
                mySolver.addActionListener(this);
            }
        }

        return result;
    }

    public void reset() {
        super.reset();
        logger.debug("reset {}",getAbsolutePath());
        result = Status.RUNNING;
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        // look in the parents for a LimbSolver
        solver.setSubject(findParent(LimbSolver.class));
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        removeSolverListener();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals("arrivedAtGoal")) {
            result = Status.SUCCESS;
            removeSolverListener();
        }
    }

    private void removeSolverListener() {
        var mySolver = solver.getSubject();
        if(mySolver!=null) {
            mySolver.removeActionListener(this);
        }
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource(
                "icons8-enter-direction-arrow-towards-rightward-orientation-pointer-16.png")));
    }

    @Override
    public JSONObject toJSON() {
        var json = super.toJSON();
        json.put("target",target.getUniqueID());
        json.put("solver",solver.getUniqueID());
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        target.setUniqueID(from.getString("target"));
        solver.setUniqueID(from.getString("solver"));
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new LimbMoveToTargetPanel(this));
        super.getComponents(list);
    }

    public Pose getTarget() {
        return target.getSubject();
    }

    public void setTarget(Pose subject) {
        target.setSubject(subject);
    }

    public LimbSolver getSolver() {
        return solver.getSubject();
    }

    public void setSolver(LimbSolver subject) {
        solver.setSubject(subject);
    }
}
