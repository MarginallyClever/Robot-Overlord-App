package com.marginallyclever.robotoverlord.components.demo;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotoverlord.components.*;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.parameters.IntParameter;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;

/**
 * a robot with 4 legs.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
@ComponentDependency(components = {PoseComponent.class})
public class DogRobotComponent extends RenderComponent {
    public static final double KINEMATIC_BODY_WIDTH = 8;
    public static final double KINEMATIC_BODY_LENGTH = 8;
    public static final double KINEMATIC_BODY_HEIGHT = 18.5;

    public static final double VISUAL_BODY_WIDTH = 12;
    public static final double VISUAL_BODY_LENGTH = 8;
    public static final double VISUAL_BODY_HEIGHT = 30;
    public static final int NUM_LEGS = 4;
    public static final String HIP = "Hip";
    public static final String THIGH = "Thigh";
    public static final String CALF = "Calf";
    public static final String FOOT = "Foot";
    public static final String[] MODE_NAMES = {
            "Calibrate",
            "Sit down",
            "Stand up",
            "Only body ",
            "Ripple",
            "Wave",
    };
    public final IntParameter modeSelector = new IntParameter("Mode", 0);
    public final DoubleParameter standingRadius = new DoubleParameter("Standing radius", 21);
    public final DoubleParameter standingHeight = new DoubleParameter("Standing height", 5.5);
    public final DoubleParameter turningStrideLength = new DoubleParameter("Turning stride length", 150);
    public final DoubleParameter strideLength = new DoubleParameter("Stride length", 15);
    public final DoubleParameter strideHeight = new DoubleParameter("Stride height", 5);
    public final DoubleParameter speedScale = new DoubleParameter("Speed scale", 1);
    private final RobotComponent[] legs = new RobotComponent[NUM_LEGS];
    private final Point3d [] lastPOC = new Point3d[NUM_LEGS];
    private final Point3d [] nextPOC = new Point3d[NUM_LEGS];
    private final Point3d [] targets = new Point3d[NUM_LEGS];

    private double gaitCycleTime = 0;

    public DogRobotComponent() {
        super();

        for(int i=0;i<NUM_LEGS;++i) {
            lastPOC[i] = new Point3d();
            nextPOC[i] = new Point3d();
            targets[i] = new Point3d();
        }
    }

    public void setLeg(int i, RobotComponent leg) {
        legs[i] = leg;
    }

    @Override
    public void render(GL2 gl2) {
        gl2.glPushMatrix();
        PoseComponent myPose = getEntity().getComponent(PoseComponent.class);
        Matrix4d m = myPose.getWorld();
        m.invert();
        MatrixHelper.applyMatrix(gl2,m);

        for(int i=0;i<NUM_LEGS;++i) {
            PrimitiveSolids.drawStar(gl2,lastPOC[i],2);
            PrimitiveSolids.drawStar(gl2,nextPOC[i],4);
            drawMarker(gl2,targets[i],0);
        }
        gl2.glPopMatrix();

        PrimitiveSolids.drawCircleXY(gl2,standingRadius.get(),32);
    }

    private void drawMarker(GL2 gl2, Tuple3d v, int color) {
        if(color==0) PrimitiveSolids.drawStar(gl2,v,5);
        else PrimitiveSolids.drawSphere(gl2,v,1);
    }

    public RobotComponent [] getLegs() {
        return legs;
    }

    public Point3d getLastPOC(int index) {
        return lastPOC[index];
    }

    public void setTarget(int index, Point3d point2) {
        targets[index].set(point2);
    }

    public Point3d getNextPOC(int index) {
        return nextPOC[index];
    }

    public double getGaitCycleTime() {
        return gaitCycleTime;
    }

    public void setGaitCycleTime(double t) {
        gaitCycleTime = t;
    }
}
