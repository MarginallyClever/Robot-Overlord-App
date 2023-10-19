package com.marginallyclever.robotoverlord.systems.robot.dog;

import com.marginallyclever.convenience.helpers.MathHelper;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.RobotComponent;
import com.marginallyclever.robotoverlord.components.demo.DogRobotComponent;
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
 * A system to manage robot dogs.
 *
 * @author Dan Royer
 * @since 2.5.7
 */
public class DogRobotSystem implements EntitySystem {
    private final EntityManager entityManager;

    public DogRobotSystem(EntityManager entityManager) {
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
        if( component instanceof DogRobotComponent) decorateDog(view,component);
    }

    public void decorateDog(ComponentSwingViewFactory view, Component component) {
        DogRobotComponent dog = (DogRobotComponent)component;

        view.add(dog.standingRadius);
        view.add(dog.standingHeight);
        view.add(dog.turningStrideLength);
        view.add(dog.strideLength);
        view.add(dog.strideHeight);

        view.addComboBox(dog.modeSelector, DogRobotComponent.MODE_NAMES);
        view.add(dog.speedScale);

        ViewElementButton bMake = view.addButton("Edit Dog");
        bMake.addActionEventListener((evt)-> makeDog(bMake,dog,"Edit Dog"));
    }

    private void makeDog(JComponent parent, DogRobotComponent dog,String title) {
        EntitySystemUtils.makePanel(new EditDogPanel(dog.getEntity(), entityManager,this), parent,title);
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
            DogRobotComponent dog = e.getComponent(DogRobotComponent.class);
            if( dog!=null ) updateDog(dog,dt);
            list.addAll(e.getChildren());
        }
    }



    public void updateDog(DogRobotComponent dog,double dt) {
        dog.setGaitCycleTime(dog.getGaitCycleTime()+dt);

        updateBasedOnMode(dog,dt);
    }

    private void setPointOfContact(Entity poc, Vector3d point) {
        PoseComponent pose = poc.getComponent(PoseComponent.class);
        Matrix4d m = pose.getLocal();
        m.setTranslation(point);
        pose.setLocalMatrix4(m);
    }

    private void updateBasedOnMode(DogRobotComponent dog,double dt) {
        switch (dog.modeSelector.get()) {
            case 0 -> updateCalibrate(dog,dt);
            case 1 -> updateSitDown(dog,dt);
            case 2 -> updateStandUp(dog,dt);
            case 3 -> updateOnlyBody(dog,dt);
            case 4 -> updateRipple(dog,dt);
            case 5 -> updateWave(dog,dt);
            case 6 -> updateTripod(dog,dt);
        }
    }

    private void updateCalibrate(DogRobotComponent dog,double dt) {
        RobotComponent[] legs = dog.getLegs();
        for(int i=0;i<DogRobotComponent.NUM_LEGS;++i) {
            RobotComponent leg = legs[i];
            //leg.getBone(0).setTheta(0);
            //leg.getBone(1).setTheta(45);
            //leg.getBone(2).setTheta(-90);
        }
    }

    private void updateSitDown(DogRobotComponent dog,double dt) {
    }

    private void updateStandUp(DogRobotComponent dog,double dt) {
        for (int i = 0; i < DogRobotComponent.NUM_LEGS; ++i) {
            putFootDown(dog,i);
        }
    }

    private void updateOnlyBody(DogRobotComponent dog,double dt) {
    }

    private void updateRipple(DogRobotComponent dog,double dt) {
        double gaitCycleTime = dog.getGaitCycleTime();
        double step = (gaitCycleTime - Math.floor(gaitCycleTime));
        int legToMove = ((int) Math.floor(gaitCycleTime) % DogRobotComponent.NUM_LEGS);

        //updateGaitTarget(dt, 1d/6d);

        for (int i = 0; i < DogRobotComponent.NUM_LEGS; ++i) {
            if (i != legToMove) {
                putFootDown(dog,i);
            } else {
                updateGaitForOneLeg(dog,i, step);
            }
        }
    }

    private void updateWave(DogRobotComponent dog,double dt) {
        double gaitCycleTime = dog.getGaitCycleTime();
        //updateGaitTarget(dt, 2d/6d);

        double gc1 = gaitCycleTime + 0.5f;
        double gc2 = gaitCycleTime;

        double x1 = gc1 - Math.floor(gc1);
        double x2 = gc2 - Math.floor(gc2);
        double step1 = Math.max(0, x1);
        double step2 = Math.max(0, x2);
        int leg1 = (int) Math.floor(gc1) % 2;
        int leg2 = (int) Math.floor(gc2) % 2;

        // 0   3
        // 1 x 2
        // order should be 0,3,1,2
        int o1, o2;
        o1 = switch (leg1) {
            case 0 -> 0;
            case 1 -> 1;
            default -> 0;
        };
        o2 = switch (leg2) {
            case 0 -> 2;
            case 1 -> 3;
            default -> 0;
        };

        updateGaitForOneLeg(dog,o1, step1);
        updateGaitForOneLeg(dog,o2, step2);

        // Put all feet down except the active leg(s).
        for (int i = 0; i < DogRobotComponent.NUM_LEGS; ++i) {
            if (i != o1 && i != o2) {
                putFootDown(dog,i);
            }
        }
    }

    private void updateTripod(DogRobotComponent dog,double dt) {
        double gaitCycleTime = dog.getGaitCycleTime();
        //updateGaitTarget(dt, 0.5f);

        double step = (gaitCycleTime - Math.floor(gaitCycleTime));
        int legToMove = ((int) Math.floor(gaitCycleTime) % 2);

        // put all feet down except the active leg(s).
        for (int i = 0; i < DogRobotComponent.NUM_LEGS; ++i) {
            if ((i % 2) != legToMove) {
                putFootDown(dog,i);
            } else {
                updateGaitForOneLeg(dog,i, step);
            }
        }
    }

    private void putFootDown(DogRobotComponent dog,int index) {
        Point3d toe = dog.getLastPOC(index);
        toe.z=0;
        setLegTargetPosition(dog,index,toe);
    }

    private void setLegTargetPosition(DogRobotComponent dog,int index,Point3d point) {
        RobotComponent [] legs = dog.getLegs();
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
        dog.setTarget(index,point2);
        robotLeg.set(RobotComponent.END_EFFECTOR_TARGET_POSITION,p2);
    }

    /**
     * Update the gait for one leg.
     * @param index the leg to update
     * @param step 0 to 1, 0 is start of step, 1 is end of step
     */
    private void updateGaitForOneLeg(DogRobotComponent dog,int index, double step) {
        RobotComponent [] legs = dog.getLegs();
        RobotComponent robotLeg = legs[index];
        if(robotLeg==null) return;

        // horizontal distance from foot to next point of contact.
        Point3d start = dog.getLastPOC(index);
        Point3d end = dog.getNextPOC(index);
        Point3d mid = MathHelper.interpolate(start, end, step);

        // add in the height of the step.
        double stepAdj = (step <= 0.5f) ? step : 1 - step;
        stepAdj = Math.sin(stepAdj * Math.PI);
        mid.z = stepAdj * dog.strideHeight.get();

        // tell the leg where to go.
        setLegTargetPosition(dog,index,mid);
    }

    public void setInitialPointOfContact(DogRobotComponent dog,Entity limb,int index) {
        Entity hip = limb.findByPath(DogRobotComponent.HIP);
        PoseComponent hipPose = hip.getComponent(PoseComponent.class);

        Vector3d toe = MatrixHelper.getPosition(hipPose.getWorld());
        toe.z=0;
        dog.getNextPOC(index).set(toe);
        dog.getLastPOC(index).set(toe);
    }
}
