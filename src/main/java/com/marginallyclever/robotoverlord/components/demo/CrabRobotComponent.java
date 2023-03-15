package com.marginallyclever.robotoverlord.components.demo;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.components.*;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;
import com.marginallyclever.robotoverlord.parameters.DoubleEntity;
import com.marginallyclever.robotoverlord.parameters.IntEntity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

public class CrabRobotComponent extends RenderComponent {
    private static final int NUM_LEGS = 6;
    static final String HIP = "Hip";
    static final String THIGH = "Thigh";
    static final String CALF = "Calf";
    static final String FOOT = "Foot";
    static final String LAST_POC = "last poc";
    static final String NEXT_POC = "next poc";
    private final String[] modeNames = {
            "Calibrate",
            "Sit down",
            "Stand up",
            "Only body ",
            "Ripple",
            "Wave",
            "Tripod",
    };
    private final IntEntity modeSelector = new IntEntity("Mode", 0);
    private final DoubleEntity standingRadius = new DoubleEntity("Standing radius", 21);
    private final DoubleEntity standingHeight = new DoubleEntity("Standing height", 5.5);
    private final DoubleEntity turningStrideLength = new DoubleEntity("Turning stride length", 150);
    private final DoubleEntity strideLength = new DoubleEntity("Stride length", 15);
    private final DoubleEntity strideHeight = new DoubleEntity("Stride height", 5);
    private final DoubleEntity speedScale = new DoubleEntity("Speed scale", 1);
    private final Entity [] legs = new Entity[NUM_LEGS];

    public CrabRobotComponent() {
        super();
    }

    @Override
    public void render(GL2 gl2) {
        gl2.glColor3d(1,0,1);
        PrimitiveSolids.drawCircleXY(gl2,standingRadius.get(),32);

        for(Entity leg : legs) {
            Vector3d v = getNextPointOfContact(leg);
            gl2.glPushMatrix();
            gl2.glTranslated(v.x,v.y,v.z);
            PrimitiveSolids.drawStar(gl2,5);
            gl2.glPopMatrix();
        }
    }

    @Override
    public void setEntity(Entity entity) {
        super.setEntity(entity);
        if(entity==null) return;

        getEntity().addComponent(new PoseComponent());

        createMesh(getEntity(),"/Spidee/body.stl");

        // 0   5
        // 1 x 4
        // 2   3
        legs[0] = createLimb("LF",false,  135);
        legs[1] = createLimb("LM",false,  180);
        legs[2] = createLimb("LB",false, -135);
        legs[3] = createLimb("RB",true,   -45);
        legs[4] = createLimb("RM",true,     0);
        legs[5] = createLimb("RF",true,    45);

        for(Entity leg : legs) {
            getEntity().addEntity(leg);
        }
    }

    @Override
    public void getView(ViewPanel view) {
        super.getView(view);
        view.pushStack("Config",false);
        view.add(standingRadius);
        view.add(standingHeight);
        view.add(turningStrideLength);
        view.add(strideLength);
        view.add(strideHeight);
        view.popStack();
        view.pushStack("Gait",true);
        view.addComboBox(modeSelector, modeNames);
        view.add(speedScale);
        view.popStack();
    }

    double t=0;

    @Override
    public void update(double dt) {
        t += dt;
        float offset=0;
        for(Entity leg : legs) {
            //updateGaitForOneLeg(leg, (t+offset) % 1);
            offset+=1.0/(double)NUM_LEGS;
        }

        //updateBasedOnMode(dt);
    }

    /**
     * Update the gait for one leg.
     * @param leg the leg to update
     * @param step 0 to 1, 0 is start of step, 1 is end of step
     */
    private void updateGaitForOneLeg(Entity leg, double step) {
        RobotComponent robotLeg = leg.findFirstComponent(RobotComponent.class);
        if(robotLeg==null) return;

        Matrix4d footMatrix = (Matrix4d)robotLeg.get(RobotComponent.END_EFFECTOR_TARGET);
        Vector3d toe = new Vector3d();
        footMatrix.get(toe);

        // horizontal distance from foot to next point of contact
        Vector3d dp = new Vector3d(getNextPointOfContact(leg));

        dp.sub(getLastPointOfContact(leg));
        dp.z = 0;
        if(dp.lengthSquared()>0) {
            dp.normalize();
            dp.scale(step);
            toe.add(dp);
        }

        // add in the height of the step
        double stepAdj = (step <= 0.5f) ? step : 1 - step;
        stepAdj = Math.sin(stepAdj * Math.PI);
        toe.z = stepAdj * strideHeight.get();

        footMatrix.setTranslation(toe);
        robotLeg.set(RobotComponent.END_EFFECTOR_TARGET,footMatrix);
    }

    private Vector3d getNextPointOfContact(Entity leg) {
        Entity poc = leg.findByPath(NEXT_POC);
        assert poc != null;
        return getPointOfContact(poc);
    }

    private void setNextPointOfContact(Entity leg,Vector3d point) {
        Entity poc = leg.findByPath(NEXT_POC);
        assert poc != null;
        setPointOfContact(poc,point);
    }

    private Vector3d getLastPointOfContact(Entity leg) {
        Entity poc = leg.findByPath(LAST_POC);
        assert poc != null;
        return getPointOfContact(poc);
    }

    private void setLastPointOfContact(Entity leg,Vector3d point) {
        Entity poc = leg.findByPath(LAST_POC);
        assert poc != null;
        setPointOfContact(poc,point);
    }

    private Vector3d getPointOfContact(Entity poc) {
        Vector3d result = new Vector3d();
        PoseComponent pose = poc.findFirstComponent(PoseComponent.class);
        pose.getLocal().get(result);
        return result;
    }

    private void setPointOfContact(Entity poc,Vector3d point) {
        PoseComponent pose = poc.findFirstComponent(PoseComponent.class);
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
    }

    private void updateSitDown(double dt) {
    }

    private void updateStandUp(double dt) {
    }

    private void updateOnlyBody(double dt) {
    }

    private void updateRipple(double dt) {
    }

    private void updateWave(double dt) {
    }

    private void updateTripod(double dt) {
    }


    private Entity createLimb(String name,boolean isRight, float degrees) {
        DHComponent[] dh = new DHComponent[3];
        for(int i=0;i<dh.length;++i) {
            dh[i] = new DHComponent();
            dh[i].setVisible(true);
        }
        Entity limb = createPoseEntity(name);

        Entity hip = createPoseEntity(HIP);
        limb.addEntity(hip);
        Entity thigh = createPoseEntity(THIGH);
        hip.addEntity(thigh);
        Entity calf = createPoseEntity(CALF);
        thigh.addEntity(calf);
        Entity foot = createPoseEntity(FOOT);
        calf.addEntity(foot);
        Entity lastPoc = createPoseEntity(LAST_POC);
        limb.addEntity(lastPoc);
        Entity nextPoc = createPoseEntity(NEXT_POC);
        limb.addEntity(nextPoc);

        hip.addComponent(dh[0]);
        dh[0].set(0,2.2,90,0,60,-60);
        if(isRight) createMesh(hip,"/Spidee/shoulder_right.obj");
        else        createMesh(hip,"/Spidee/shoulder_left.obj");

        thigh.addComponent(dh[1]);
        dh[1].set( 0,8.5,0,0,106,-72);
        createMesh(thigh,"/Spidee/thigh.obj");

        calf.addComponent(dh[2]);
        dh[2].set(0,10.5,0,0,15,-160);
        if(isRight) createMesh(calf,"/Spidee/calf_right.obj");
        else		createMesh(calf,"/Spidee/calf_left.obj");

        foot.addComponent(new ArmEndEffectorComponent());

        // position limb
        PoseComponent pose = limb.findFirstComponent(PoseComponent.class);
        double r = Math.toRadians(degrees);
        pose.setPosition(new Vector3d(Math.cos(r)*10,Math.sin(r)*10,2.6));
        pose.setRotation(new Vector3d(0,0,degrees));

        // Done at the end so RobotComponent can find all bones DHComponents.
        limb.addComponent(new RobotComponent());

        setInitialPointOfContact(limb);

        return limb;
    }

    private void setInitialPointOfContact(Entity limb) {
        Entity foot = limb.findByPath(HIP+"/"+THIGH+"/"+CALF+"/"+FOOT);
        PoseComponent footPose = foot.findFirstComponent(PoseComponent.class);
        Vector3d toe = new Vector3d();
        footPose.getWorld().get(toe);

        PoseComponent bodyPose = getEntity().findFirstComponent(PoseComponent.class);
        Vector3d body = new Vector3d();
        bodyPose.getWorld().get(body);

        toe.sub(body);
        toe.normalize();
        toe.scaleAdd(standingRadius.get(),body);
        toe.z=0;
        setNextPointOfContact(limb,toe);
        setLastPointOfContact(limb,toe);
    }

    private Entity createPoseEntity(String name) {
        Entity result = new Entity(name);
        result.addComponent(new PoseComponent());
        return result;
    }

    private void createMesh(Entity parent,String filename) {
        Entity mesh = createPoseEntity("Mesh");
        parent.addEntity(mesh);

        mesh.addComponent(new MaterialComponent());

        MeshFromFile mff = new MeshFromFile();
        mff.setFilename(filename);
        mesh.addComponent(mff);

        OriginAdjustComponent oac = new OriginAdjustComponent();
        mesh.addComponent(oac);
        oac.adjust();
        mesh.removeComponent(oac);
    }
}
