package com.marginallyclever.ro3.node.nodes.behavior.actions;

import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.behavior.Action;
import com.marginallyclever.ro3.node.nodes.limbsolver.LimbSolver;
import com.marginallyclever.ro3.node.nodes.pose.Pose;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

public class LimbMoveToTarget extends Action implements ActionListener {
    private NodePath<Pose> target;
    private NodePath<LimbSolver> solver;
    private Status result = Status.RUNNING;

    public LimbMoveToTarget() {
        this("LimbMoveToTarget");
    }

    public LimbMoveToTarget(String name) {
        super(name);
    }

    @Override
    public Status tick() {
        var mySolver = solver.getSubject();
        var myTarget = target.getSubject();
        if(mySolver==null || myTarget==null) return Status.FAILURE;

        if(mySolver.getTarget().getSubject() != myTarget) {
            mySolver.setTarget(myTarget);
            mySolver.addActionListener(this);
        }

        return result;
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        // look in the parents for a LimbSolver
        solver.setUniqueIDByNode(findParent(LimbSolver.class));
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
}
