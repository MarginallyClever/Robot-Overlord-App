package com.marginallyclever.robotoverlord.systems.robot.crab;

import com.marginallyclever.convenience.helpers.MathHelper;
import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.RobotComponent;
import com.marginallyclever.robotoverlord.components.demo.CrabRobotComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.parameters.swing.ViewElementButton;
import com.marginallyclever.robotoverlord.parameters.swing.ComponentSwingViewFactory;
import com.marginallyclever.robotoverlord.systems.EntitySystem;
import com.marginallyclever.robotoverlord.systems.EntitySystemUtils;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.LinkedList;
import java.util.List;

/**
 * A system to manage robot crabs.
 *
 * @author Dan Royer
 * @since 2.5.7
 */
public class CrabRobotSystem implements EntitySystem {
    private final EntityManager entityManager;

    public CrabRobotSystem(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Get the Swing view of this component.
     *
     * @param view      the factory to use to create the panel
     * @param component the component to visualize
     */
    @Override
    public void decorate(ComponentSwingViewFactory view, Component component) {
        if( component instanceof CrabRobotComponent) decorateCrab(view,component);
    }

    public void decorateCrab(ComponentSwingViewFactory view, Component component) {
        CrabRobotComponent crab = (CrabRobotComponent)component;
        view.add(crab.standingRadius);
        view.add(crab.standingHeight);
        view.add(crab.turningStrideLength);
        view.add(crab.strideLength);
        view.add(crab.strideHeight);

        view.addComboBox(crab.modeSelector, CrabRobotComponent.MODE_NAMES);
        view.add(crab.speedScale);

        ViewElementButton bMake = view.addButton("Edit Crab");
        bMake.addActionEventListener((evt)-> makeCrab(bMake,crab,"Edit Crab"));
    }

    private void makeCrab(JComponent parent, CrabRobotComponent crab, String title) {
        EntitySystemUtils.makePanel(new EditCrabPanel(crab.getEntity(), entityManager,this), parent,title);
    }

    /**
     * Update the system over time.
     *
     * @param dt the time step in seconds.
     */
    @Override
    public void update(double dt) {
        List<Entity> list = new LinkedList<>(entityManager.getEntities());
        while(!list.isEmpty()) {
            Entity e = list.remove(0);
            CrabRobotComponent crab = e.getComponent(CrabRobotComponent.class);
            if( crab!=null ) updateCrab(crab,dt);
            list.addAll(e.getChildren());
        }
    }

    public void updateCrab(CrabRobotComponent crab,double dt) {
        crab.setGaitCycleTime(crab.getGaitCycleTime() + dt);

        if(crab.getLegs()[0]==null) return;

        updateBasedOnMode(crab,dt);
    }

    private void setPointOfContact(Entity poc, Vector3d point) {
        PoseComponent pose = poc.getComponent(PoseComponent.class);
        Matrix4d m = pose.getLocal();
        m.setTranslation(point);
        pose.setLocalMatrix4(m);
    }

    private void updateBasedOnMode(CrabRobotComponent crab,double dt) {
        switch (crab.modeSelector.get()) {
            case 0 -> updateCalibrate(crab,dt);
            case 1 -> updateSitDown(crab,dt);
            case 2 -> updateStandUp(crab,dt);
            case 3 -> updateOnlyBody(crab,dt);
            case 4 -> updateRipple(crab,dt);
            case 5 -> updateWave(crab,dt);
            case 6 -> updateTripod(crab,dt);
        }
    }

    private void updateCalibrate(CrabRobotComponent crab,double dt) {
        RobotComponent [] legs = crab.getLegs();
        for(int i=0;i<CrabRobotComponent.NUM_LEGS;++i) {
            RobotComponent leg = legs[i];
            leg.getBone(0).setTheta(0);
            leg.getBone(1).setTheta(45);
            leg.getBone(2).setTheta(-90);
        }
    }

    private void updateSitDown(CrabRobotComponent crab,double dt) {
    }

    private void updateStandUp(CrabRobotComponent crab,double dt) {
        for (int i = 0; i < CrabRobotComponent.NUM_LEGS; ++i) {
            putFootDown(crab,i);
        }
    }

    private void updateOnlyBody(CrabRobotComponent crab,double dt) {
    }

    private void updateRipple(CrabRobotComponent crab,double dt) {
        double gaitCycleTime = crab.getGaitCycleTime();
        double step = (gaitCycleTime - Math.floor(gaitCycleTime));
        int legToMove = ((int) Math.floor(gaitCycleTime) % CrabRobotComponent.NUM_LEGS);

        //updateGaitTarget(dt, 1d/6d);

        for (int i = 0; i < CrabRobotComponent.NUM_LEGS; ++i) {
            if (i != legToMove) {
                putFootDown(crab,i);
            } else {
                updateGaitForOneLeg(crab,i, step);
            }
        }
    }

    private void updateWave(CrabRobotComponent crab,double dt) {
        double gaitCycleTime = crab.getGaitCycleTime();
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

        updateGaitForOneLeg(crab,o1, step1);
        updateGaitForOneLeg(crab,o2, step2);

        // Put all feet down except the active leg(s).
        for (int i = 0; i < CrabRobotComponent.NUM_LEGS; ++i) {
            if (i != o1 && i != o2) {
                putFootDown(crab,i);
            }
        }
    }

    private void updateTripod(CrabRobotComponent crab,double dt) {
        double gaitCycleTime = crab.getGaitCycleTime();
        //updateGaitTarget(dt, 0.5f);

        double step = (gaitCycleTime - Math.floor(gaitCycleTime));
        int legToMove = ((int) Math.floor(gaitCycleTime) % 2);

        // put all feet down except the active leg(s).
        for (int i = 0; i < CrabRobotComponent.NUM_LEGS; ++i) {
            if ((i % 2) != legToMove) {
                putFootDown(crab,i);
            } else {
                updateGaitForOneLeg(crab,i, step);
            }
        }
    }

    private void putFootDown(CrabRobotComponent crab,int index) {
        Point3d toe = crab.getLastPOC(index);
        toe.z=0;
        setLegTargetPosition(crab,index,toe);
    }

    private void setLegTargetPosition(CrabRobotComponent crab,int index,Point3d point) {
        RobotComponent [] legs = crab.getLegs();
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
        crab.setTarget(index,point2);
        robotLeg.set(RobotComponent.END_EFFECTOR_TARGET_POSITION,p2);
    }

    /**
     * Update the gait for one leg.
     * @param index the leg to update
     * @param step 0 to 1, 0 is start of step, 1 is end of step
     */
    private void updateGaitForOneLeg(CrabRobotComponent crab,int index, double step) {
        RobotComponent [] legs = crab.getLegs();
        RobotComponent robotLeg = legs[index];
        if(robotLeg==null) return;

        // horizontal distance from foot to next point of contact.
        Point3d start = crab.getLastPOC(index);
        Point3d end = crab.getNextPOC(index);
        Point3d mid = MathHelper.interpolate(start, end, step);

        // add in the height of the step.
        double stepAdj = (step <= 0.5f) ? step : 1 - step;
        stepAdj = Math.sin(stepAdj * Math.PI);
        mid.z = stepAdj * crab.strideHeight.get();

        // tell the leg where to go.
        setLegTargetPosition(crab,index,mid);
    }

    public void setInitialPointOfContact(CrabRobotComponent crab,Entity limb,int index) {
        Entity foot = limb.findByPath(CrabRobotComponent.HIP+"/"+CrabRobotComponent.THIGH+"/"+CrabRobotComponent.CALF+"/"+CrabRobotComponent.FOOT);
        PoseComponent footPose = foot.getComponent(PoseComponent.class);
        Vector3d toe = new Vector3d();
        footPose.getWorld().get(toe);

        PoseComponent bodyPose = crab.getEntity().getComponent(PoseComponent.class);
        Vector3d body = new Vector3d();
        bodyPose.getWorld().get(body);

        toe.sub(body);
        toe.normalize();
        toe.scaleAdd(crab.standingRadius.get(),body);
        toe.z=0;
        crab.setNextPOC(index,toe);
        crab.setLastPOC(index,toe);
    }
}
