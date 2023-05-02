package com.marginallyclever.robotoverlord.components.demo;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.components.*;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.parameters.IntParameter;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

@ComponentDependency(components = {PoseComponent.class})
public class CrabRobotComponent extends RenderComponent {
    public static final int NUM_LEGS = 6;
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
            "Tripod",
    };
    public final IntParameter modeSelector = new IntParameter("Mode", 0);
    public final DoubleParameter standingRadius = new DoubleParameter("Standing radius", 21);
    public final DoubleParameter standingHeight = new DoubleParameter("Standing height", 5.5);
    public final DoubleParameter turningStrideLength = new DoubleParameter("Turning stride length", 150);
    public final DoubleParameter strideLength = new DoubleParameter("Stride length", 15);
    public final DoubleParameter strideHeight = new DoubleParameter("Stride height", 5);
    public final DoubleParameter speedScale = new DoubleParameter("Speed scale", 1);
    private final RobotComponent [] legs = new RobotComponent[NUM_LEGS];
    private final Point3d [] lastPOC = new Point3d[NUM_LEGS];
    private final Point3d [] nextPOC = new Point3d[NUM_LEGS];
    private final Point3d [] targets = new Point3d[NUM_LEGS];
    private double gaitCycleTime = 0;

    public CrabRobotComponent() {
        super();
        for(int i=0;i<NUM_LEGS;++i) {
            lastPOC[i] = new Point3d();
            nextPOC[i] = new Point3d();
            targets[i] = new Point3d();
        }
    }

    public void setLeg(int index,RobotComponent leg) {
        legs[index] = leg;
    }

    @Override
    public void render(GL2 gl2) {
        gl2.glPushMatrix();
        //MatrixHelper.setMatrix(gl2,MatrixHelper.createIdentityMatrix4());
        PrimitiveSolids.drawCircleXY(gl2,standingRadius.get(),32);

        for(int i=0;i<NUM_LEGS;++i) {
            //PrimitiveSolids.drawStar(gl2,lastPOC[i],0);
            //PrimitiveSolids.drawStar(gl2,nextPOC[i],1);
            drawMarker(gl2,targets[i],0);
        }
        gl2.glPopMatrix();
    }


    private void drawMarker(GL2 gl2, Tuple3d v, int color) {
        if(color==0) PrimitiveSolids.drawStar(gl2,v,5);
        else PrimitiveSolids.drawSphere(gl2,v,1);
    }

    public double getGaitCycleTime() {
        return gaitCycleTime;
    }

    public void setGaitCycleTime(double gaitCycleTime) {
        this.gaitCycleTime = gaitCycleTime;
    }

    public void setInitialPointOfContact(Entity limb,int index) {
        Entity foot = limb.findByPath(HIP+"/"+THIGH+"/"+CALF+"/"+FOOT);
        PoseComponent footPose = foot.getComponent(PoseComponent.class);
        Vector3d toe = new Vector3d();
        footPose.getWorld().get(toe);

        PoseComponent bodyPose = getEntity().getComponent(PoseComponent.class);
        Vector3d body = new Vector3d();
        bodyPose.getWorld().get(body);

        toe.sub(body);
        toe.normalize();
        toe.scaleAdd(standingRadius.get(),body);
        toe.z=0;
        nextPOC[index].set(toe);
        lastPOC[index].set(toe);
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
}
