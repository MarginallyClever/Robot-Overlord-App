package com.marginallyclever.ro3.node.nodes.limbplanner;

import com.marginallyclever.ro3.node.nodes.Pose;
import com.marginallyclever.ro3.node.nodes.limbsolver.LimbSolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.event.ActionEvent;

import static org.junit.jupiter.api.Assertions.*;

class LimbPlannerTest {
    LimbPlanner limbPlanner;
    LimbSolver limbSolver;
    Pose pathStart;

    @BeforeEach
    void setUp() {
        limbPlanner = new LimbPlanner();

        limbSolver = new LimbSolver();
        limbPlanner.addChild(limbSolver);
        limbPlanner.setSolver(limbSolver);

        pathStart = new Pose();
        limbPlanner.addChild(pathStart);
        limbPlanner.setPathStart(pathStart);
    }

    @Test
    void testStartRun() {
        limbPlanner.startRun();
        assertTrue(limbPlanner.isRunning());
    }

    @Test
    void testStopRun() {
        limbPlanner.startRun();
        limbPlanner.stopRun();
        assertFalse(limbPlanner.isRunning());
    }

    @Test
    void testGetExecutionTimeAndPreviousExecutionTime() {
        limbPlanner.startRun();
        limbPlanner.update(1.0);
        limbPlanner.stopRun();
        assertEquals(1.0, limbPlanner.getExecutionTime());
        assertEquals(1.0, limbPlanner.getPreviousExecutionTime());
    }

    @Test
    void testActionPerformed() {
        limbPlanner.startRun();
        ActionEvent event = new ActionEvent(limbPlanner, ActionEvent.ACTION_PERFORMED, "arrivedAtGoal");
        limbPlanner.actionPerformed(event);
        assertFalse(limbPlanner.isRunning());
    }
}