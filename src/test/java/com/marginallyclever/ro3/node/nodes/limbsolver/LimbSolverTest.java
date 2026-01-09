package com.marginallyclever.ro3.node.nodes.limbsolver;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.Limb;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LimbSolverTest {
    @Test
    void testGetSetTarget() {
        LimbSolver limbSolver = new LimbSolver();
        Pose pose = new Pose();
        limbSolver.addChild(pose);
        limbSolver.setTarget(pose);
        assertEquals(pose, limbSolver.getTarget().getSubject());
    }

    @Test
    void testUpdate() {
        LimbSolver limbSolver = new LimbSolver();
        limbSolver.update(1.0);
        // Add assertions based on the expected state of the limbSolver after calling update
    }

    @Test
    void testGetSetLimb() {
        LimbSolver limbSolver = new LimbSolver();
        Limb limb = new Limb();
        limbSolver.addChild(limb);
        limbSolver.setLimb(limb);
        assertEquals(limb, limbSolver.getLimb().getSubject());
    }

    @Test
    void testGetSetLinearVelocity() {
        LimbSolver limbSolver = new LimbSolver();
        limbSolver.setLinearVelocity(10.0);
        assertEquals(10.0, limbSolver.getLinearVelocity());
    }

    @Test
    void testGetDistanceToTarget() {
        LimbSolver limbSolver = new LimbSolver();
        // Add assertions based on the expected distance to target
    }

    @Test
    void testGetSetGoalMarginOfError() {
        LimbSolver limbSolver = new LimbSolver();
        limbSolver.setGoalMarginOfError(0.1);
        assertEquals(0.1, limbSolver.getGoalMarginOfError());
    }

    @Test
    public void toFromJSON() {
        Registry.start();

        var limb = new Limb("a");
        var target = new Pose("b");

        LimbSolver solver = new LimbSolver();
        solver.setLinearVelocity(solver.getLinearVelocity()+10);
        solver.setGoalMarginOfError(solver.getGoalMarginOfError()+10);
        solver.setIsAtGoal(!solver.getIsAtGoal());
        solver.addChild(limb);
        solver.addChild(target);
        solver.setLimb(limb);
        solver.setTarget(target);

        var json = solver.toJSON();
        LimbSolver ls2 = new LimbSolver();
        ls2.fromJSON(json);

        assertEquals(solver.getLinearVelocity(),ls2.getLinearVelocity());
        assertEquals(solver.getGoalMarginOfError(),ls2.getGoalMarginOfError());
        assertEquals(solver.getIsAtGoal(),ls2.getIsAtGoal());
        assertEquals(solver.getLimb().getSubject().getName(),ls2.getLimb().getSubject().getName());
        assertEquals(solver.getTarget().getSubject().getName(),ls2.getTarget().getSubject().getName());
    }
}