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

    double gaitCycleTime = 0;

    public CrabRobotComponent() {
        super();
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

    @Override
    public void update(double dt) {
        gaitCycleTime += dt;

        updateBasedOnMode(dt);
    }

    private void setPointOfContact(Entity poc,Vector3d point) {
        PoseComponent pose = poc.getComponent(PoseComponent.class);
        Matrix4d m = pose.getLocal();
        m.setTranslation(point);
        pose.setLocalMatrix4(m);
    }

    private void updateBasedOnMode(double dt) {
        switch (modeSelector.get()) {
            case 0 -> updateCalibrate(dt);
            case 1 -> updateSitDown(dt);
            case 2 -> updateStandUp(dt);
            case 3 -> updateOnlyBody(dt);
            case 4 -> updateRipple(dt);
            case 5 -> updateWave(dt);
            case 6 -> updateTripod(dt);
        }
    }

    private void updateCalibrate(double dt) {
        for(int i=0;i<NUM_LEGS;++i) {
            RobotComponent leg = legs[i];
            leg.getBone(0).setTheta(0);
            leg.getBone(1).setTheta(45);
            leg.getBone(2).setTheta(-90);
        }
    }

    private void updateSitDown(double dt) {
    }

    private void updateStandUp(double dt) {
        for (int i = 0; i < NUM_LEGS; ++i) {
            putFootDown(i);
        }
    }

    private void updateOnlyBody(double dt) {
    }

    private void updateRipple(double dt) {
        double step = (gaitCycleTime - Math.floor(gaitCycleTime));
        int legToMove = ((int) Math.floor(gaitCycleTime) % NUM_LEGS);

        //updateGaitTarget(dt, 1d/6d);

        for (int i = 0; i < NUM_LEGS; ++i) {
            if (i != legToMove) {
                putFootDown(i);
            } else {
                updateGaitForOneLeg(i, step);
            }
        }
    }

    private void updateWave(double dt) {
        gaitCycleTime += dt;

        //updateGaitTarget(dt, 2d/6d);

        double gc1 = gaitCycleTime + 0.5f;
        double gc2 = gaitCycleTime;

        double x1 = gc1 - Math.floor(gc1);
        double x2 = gc2 - Math.floor(gc2);
        double step1 = Math.max(0, x1);
        double step2 = Math.max(0, x2);
        int leg1 = (int) Math.floor(gc1) % 3;
        int leg2 = (int) Math.floor(gc2) % 3;

        // 0   5
        // 1 x 4
        // 2   3
        // order should be 0,3,1,5,2,4
        int o1, o2;
        o1 = switch (leg1) {
            case 0 -> 0;
            case 1 -> 1;
            case 2 -> 2;
            default -> 0;
        };
        o2 = switch (leg2) {
            case 0 -> 3;
            case 1 -> 5;
            case 2 -> 4;
            default -> 0;
        };

        updateGaitForOneLeg(o1, step1);
        updateGaitForOneLeg(o2, step2);

        // Put all feet down except the active leg(s).
        for (int i = 0; i < NUM_LEGS; ++i) {
            if (i != o1 && i != o2) {
                putFootDown(i);
            }
        }
    }

    private void updateTripod(double dt) {
        gaitCycleTime += dt;

        //updateGaitTarget(dt, 0.5f);

        double step = (gaitCycleTime - Math.floor(gaitCycleTime));
        int legToMove = ((int) Math.floor(gaitCycleTime) % 2);

        // put all feet down except the active leg(s).
        for (int i = 0; i < NUM_LEGS; ++i) {
            if ((i % 2) != legToMove) {
                putFootDown(i);
            } else {
                updateGaitForOneLeg(i, step);
            }
        }
    }

    private void putFootDown(int index) {
        Point3d toe = lastPOC[index];
        toe.z=0;
        setLegTargetPosition(index,toe);
    }

    private void setLegTargetPosition(int index,Point3d point) {
        RobotComponent robotLeg = legs[index];
        if(robotLeg==null) return;
        PoseComponent legBasePose = robotLeg.getEntity().getComponent(PoseComponent.class);
        if(legBasePose==null) return;

        // the leg is a robot arm.  the base of the arm is the crab's shoulder.
        Matrix4d legBaseMatrix = legBasePose.getWorld();
        // all end effector positions are relative to the base of the arm.
        // so we need to transform the point into the arm's coordinate system.
        Matrix4d legBaseMatrixInv = new Matrix4d(legBaseMatrix);
        legBaseMatrixInv.invert();
        Point3d p2 = new Point3d();
        legBaseMatrixInv.transform(point,p2);
        Point3d point2 = new Point3d();
        legBaseMatrix.transform(p2,point2);
        targets[index].set(point2);
        robotLeg.set(RobotComponent.END_EFFECTOR_TARGET_POSITION,p2);
    }

    /**
     * Update the gait for one leg.
     * @param index the leg to update
     * @param step 0 to 1, 0 is start of step, 1 is end of step
     */
    private void updateGaitForOneLeg(int index, double step) {
        RobotComponent robotLeg = legs[index];
        if(robotLeg==null) return;

        // horizontal distance from foot to next point of contact.
        Point3d start = lastPOC[index];
        Point3d end = nextPOC[index];
        Point3d mid = MathHelper.interpolate(start, end, step);

        // add in the height of the step.
        double stepAdj = (step <= 0.5f) ? step : 1 - step;
        stepAdj = Math.sin(stepAdj * Math.PI);
        mid.z = stepAdj * strideHeight.get();

        // tell the leg where to go.
        setLegTargetPosition(index,mid);
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
        nextPOC[index] = new Point3d(toe);
        lastPOC[index] = new Point3d(toe);
        targets[index] = new Point3d();
    }
}
