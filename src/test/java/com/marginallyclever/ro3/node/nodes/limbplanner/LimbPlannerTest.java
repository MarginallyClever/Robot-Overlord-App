package com.marginallyclever.ro3.node.nodes.limbplanner;

import com.marginallyclever.convenience.helpers.StringHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.actions.LoadScene;
import com.marginallyclever.ro3.apps.commands.ImportScene;
import com.marginallyclever.ro3.node.nodes.Pose;
import com.marginallyclever.ro3.node.nodes.limbsolver.LimbSolver;
import com.marginallyclever.ro3.node.nodes.pose.Limb;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class LimbPlannerTest {
    private static final Logger logger = LoggerFactory.getLogger(LimbPlannerTest.class);
    Limb limb;
    LimbSolver limbSolver;
    LimbPlanner limbPlanner;
    Pose pathStart;

    private Limb build6AxisArm() throws Exception {
        var load = new LoadScene(null,null);
        File file = new File("src/test/resources/com/marginallyclever/ro3/apps/node/nodes/marlinrobotarm/Sixi3-5.RO");
        load.commitLoad(file);
        return (Limb) Registry.getScene().get("./Sixi3");
    }

    @BeforeEach
    void setUp() throws Exception {
        Registry.start();
        limb = build6AxisArm();

        // the Sixi3-5.RO file has a limb named "Sixi3" which has a LimbSolver.

        limbSolver = limb.findFirstChild(LimbSolver.class);

        limbPlanner = new LimbPlanner();
        limb.addChild(limbPlanner);
        limbPlanner.setSolver(limbSolver);

        pathStart = new Pose();
        limbPlanner.addChild(pathStart);
        limbPlanner.setPathStart(pathStart);
        limb.addChild(pathStart);
    }

    @Test
    void testStartRun1() {
        limbPlanner.startRun();
        assertFalse(limbPlanner.isRunning());
    }

    @Test
    void testStartRun2() {
        pathStart.addChild(new Pose());
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
        // start at world origin
        pathStart.addChild(new Pose("start"));

        // end 1 unit in the X direction away
        var end = new Pose("end");
        var endPosition = new Matrix4d();
        endPosition.setTranslation(new Vector3d(10, 0, 0));
        end.setLocal(endPosition);
        pathStart.addChild(end);

        // turn on the planner
        limbPlanner.setLinearVelocity(1.0);
        limbPlanner.startRun();
        // The Sixi3-5 arm is perfectly vertical, it cannot reach the start position.
        // it is guaranteed to run for 1 second.
        double dt = 0.1;
        double sum=0;
        for(int i=0;i<10;++i) {
            sum+=dt;
            logger.debug(StringHelper.formatTime(sum)+" Move "+Arrays.toString(limb.getAllJointAngles()));
            limb.update(dt);
            if(!limbPlanner.isRunning()) break;
        }
        if(limbPlanner.isRunning()) {
            limbPlanner.stopRun();
        }
        logger.debug(StringHelper.formatTime(sum)+" End "+Arrays.toString(limb.getAllJointAngles()));

        // confirm we moved for 1 second.
        assertEquals(1.0, limbPlanner.getExecutionTime(),1e-4);
        assertEquals(0.0, limbPlanner.getPreviousExecutionTime());
    }

    @Test
    void testActionPerformed() {
        limbPlanner.startRun();
        ActionEvent event = new ActionEvent(limbPlanner, ActionEvent.ACTION_PERFORMED, "arrivedAtGoal");
        limbPlanner.actionPerformed(event);
        assertFalse(limbPlanner.isRunning());
    }
}