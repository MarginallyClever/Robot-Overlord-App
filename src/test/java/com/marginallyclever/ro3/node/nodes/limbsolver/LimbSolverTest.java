package com.marginallyclever.ro3.node.nodes.limbsolver;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.nodes.Pose;
import com.marginallyclever.ro3.node.nodes.pose.Limb;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

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

        LimbSolver ls = new LimbSolver();
        ls.setLinearVelocity(ls.getLinearVelocity()+10);
        ls.setGoalMarginOfError(ls.getGoalMarginOfError()+10);
        ls.setIsAtGoal(!ls.getIsAtGoal());
        ls.addChild(limb);
        ls.addChild(target);
        ls.setLimb(limb);
        ls.setTarget(target);

        var json = ls.toJSON();
        LimbSolver ls2 = new LimbSolver();
        ls2.fromJSON(json);

        assertEquals(ls.getLinearVelocity(),ls2.getLinearVelocity());
        assertEquals(ls.getGoalMarginOfError(),ls2.getGoalMarginOfError());
        assertEquals(ls.getIsAtGoal(),ls2.getIsAtGoal());
        assertEquals(ls.getLimb().getSubject().getName(),ls2.getLimb().getSubject().getName());
        assertEquals(ls.getTarget().getSubject().getName(),ls2.getTarget().getSubject().getName());
    }
}