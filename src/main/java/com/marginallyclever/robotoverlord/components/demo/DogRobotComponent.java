package com.marginallyclever.robotoverlord.components.demo;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.components.*;
import com.marginallyclever.robotoverlord.components.shapes.Box;
import com.marginallyclever.robotoverlord.components.shapes.Cylinder;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.parameters.IntParameter;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

public class DogRobotComponent extends RenderComponent {
    public static final double KINEMATIC_BODY_WIDTH = 8;
    public static final double KINEMATIC_BODY_LENGTH = 8;
    public static final double KINEMATIC_BODY_HEIGHT = 18.5;

    public static final double VISUAL_BODY_WIDTH = 12;
    public static final double VISUAL_BODY_LENGTH = 8;
    public static final double VISUAL_BODY_HEIGHT = 30;
    private static final int NUM_LEGS = 4;
    static final String HIP = "Hip";
    static final String THIGH = "Thigh";
    static final String CALF = "Calf";
    static final String FOOT = "Foot";
    private final String[] modeNames = {
            "Calibrate",
            "Sit down",
            "Stand up",
            "Only body ",
            "Ripple",
            "Wave",
    };
    private final IntParameter modeSelector = new IntParameter("Mode", 0);
    private final DoubleParameter standingRadius = new DoubleParameter("Standing radius", 21);
    private final DoubleParameter standingHeight = new DoubleParameter("Standing height", 5.5);
    private final DoubleParameter turningStrideLength = new DoubleParameter("Turning stride length", 150);
    private final DoubleParameter strideLength = new DoubleParameter("Stride length", 15);
    private final DoubleParameter strideHeight = new DoubleParameter("Stride height", 5);
    private final DoubleParameter speedScale = new DoubleParameter("Speed scale", 1);
    private final RobotComponent[] legs = new RobotComponent[NUM_LEGS];
    private final Point3d [] lastPOC = new Point3d[NUM_LEGS];
    private final Point3d [] nextPOC = new Point3d[NUM_LEGS];
    private final Point3d [] targets = new Point3d[NUM_LEGS];

    double gaitCycleTime = 0;

    public DogRobotComponent() {
        super();

        for(int i=0;i<NUM_LEGS;++i) {
            lastPOC[i] = new Point3d();
            nextPOC[i] = new Point3d();
            targets[i] = new Point3d();
        }
    }

    @Override
    public void render(GL2 gl2) {
        gl2.glPushMatrix();
        PoseComponent myPose = getEntity().findFirstComponent(PoseComponent.class);
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

    @Override
    public void setEntity(Entity entity) {
        super.setEntity(entity);
        if(entity==null) return;

        Entity myEntity = getEntity();
        myEntity.addComponent(new PoseComponent());
        PoseComponent myPose = myEntity.findFirstComponent(PoseComponent.class);
        myPose.setPosition(new Vector3d(0,0,5.4));
        myPose.setRotation(new Vector3d(90,0,0));

        Entity mesh = createMesh("/robots/SpotMicro/torso.obj",new ColorRGB(0xffffff));
        myEntity.addEntity(mesh);
        PoseComponent meshPose = mesh.findFirstComponent(PoseComponent.class);
        meshPose.setRotation(new Vector3d(90,180,180));
        meshPose.setPosition(new Vector3d(-0.7,4.1,7));


        // head
        // 0   2
        // 1   1
        double w = KINEMATIC_BODY_WIDTH/2;
        double h = KINEMATIC_BODY_HEIGHT/2;
        int i=0;
        legs[i] = createLimb("RF",i, true, -w, h, 1);  i++;
        legs[i] = createLimb("RB",i, true, -w,-h, 1);  i++;
        legs[i] = createLimb("LF",i,false,  w, h, 1);  i++;
        legs[i] = createLimb("LB",i,false,  w,-h, 1);  i++;

        i=0;
        for(RobotComponent leg : legs) {
            myEntity.addEntity(leg.getEntity());
            setInitialPointOfContact(leg.getEntity(),i++);
        }
    }

    @Override
    public void getView(ComponentPanelFactory view) {
        super.getView(view);
        view.startComponentPanel("Config",false);
        view.add(standingRadius);
        view.add(standingHeight);
        view.add(turningStrideLength);
        view.add(strideLength);
        view.add(strideHeight);

        view.startComponentPanel("Gait",true);
        view.addComboBox(modeSelector, modeNames);
        view.add(speedScale);
    }

    @Override
    public void update(double dt) {
        gaitCycleTime += dt;

        updateBasedOnMode(dt);
    }

    private void setPointOfContact(Entity poc, Vector3d point) {
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
        for(int i=0;i<NUM_LEGS;++i) {
            RobotComponent leg = legs[i];
            //leg.getBone(0).setTheta(0);
            //leg.getBone(1).setTheta(45);
            //leg.getBone(2).setTheta(-90);
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
        PoseComponent legBasePose = robotLeg.getEntity().findFirstComponent(PoseComponent.class);
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

    private RobotComponent createLimb(String name,int index,boolean isRight, double r, double d, double s) {
        DHComponent[] dh = new DHComponent[4];
        for(int i=0;i<dh.length;++i) {
            dh[i] = new DHComponent();
            dh[i].setVisible(true);
        }
        Entity limb = createPoseEntity(name);
        PoseComponent limbPose = limb.findFirstComponent(PoseComponent.class);
        limbPose.setPosition(new Vector3d(r,0,d));

        limb.addEntity(createCylinder(4,2.1,new ColorRGB(0x9999FF)));

        Entity hip = createPoseEntity(HIP);
        limb.addEntity(hip);
        Entity thigh = createPoseEntity(THIGH);
        hip.addEntity(thigh);
        Entity calf = createPoseEntity(CALF);
        thigh.addEntity(calf);
        Entity foot = createPoseEntity(FOOT);
        calf.addEntity(foot);

        hip.addComponent(dh[0]);
        dh[0].set( 0, 0, 90*(isRight?1:-1), 90, 360, -360,true);
        hip.addEntity(createCylinder(5,2,new ColorRGB(0xFFFFFF)));

        thigh.addComponent(dh[1]);
        dh[1].set(-3.5 * s, 11.5, 0, 135*(isRight?-1:1), 360, -360,true);
        thigh.addEntity(createBox(dh[1].getR(),1,new ColorRGB(0xFFFF99)));

        calf.addComponent(new ArmEndEffectorComponent());
        calf.addComponent(dh[2]);
        dh[2].set(0, 13, 0, 90*(isRight?-1:1), 360, -360,true);
        calf.addEntity(createBox(dh[2].getR(),0.7,new ColorRGB(0xFFFF66)));

        foot.addComponent(new ArmEndEffectorComponent());

        // Done at the end so RobotComponent can find all bones DHComponents.
        RobotComponent robot = new RobotComponent();
        limb.addComponent(robot);

        setInitialPointOfContact(limb,index);

        return robot;
    }

    private Entity createBox(double r, double v,ColorRGB color) {
        Entity result = new Entity("Mesh");

        PoseComponent pose = new PoseComponent();
        result.addComponent(pose);
        pose.setPosition(new Vector3d(-r/2,0,0));
        pose.setScale(new Vector3d(r,v*2,v*2));

        MaterialComponent material = new MaterialComponent();
        result.addComponent(material);
        material.setDiffuseColor(color.red/255.0f, color.green/255.0f, color.blue/255.0f,1.0);

        result.addComponent(new Box());

        return result;
    }

    private Entity createCylinder(double r,double v,ColorRGB color) {
        Entity result = new Entity("Mesh");

        PoseComponent pose = new PoseComponent();
        result.addComponent(pose);
        pose.setScale(new Vector3d(v*2,v*2,r));

        MaterialComponent material = new MaterialComponent();
        result.addComponent(material);
        material.setDiffuseColor(color.red/255.0f, color.green/255.0f, color.blue/255.0f,1.0);

        result.addComponent(new Cylinder());

        return result;
    }

    private void setInitialPointOfContact(Entity limb,int index) {
        Entity foot = limb.findByPath(HIP);
        PoseComponent footPose = foot.findFirstComponent(PoseComponent.class);

        Vector3d toe = MatrixHelper.getPosition(footPose.getWorld());
        toe.z=0;
        nextPOC[index].set(toe);
        lastPOC[index].set(toe);
    }

    private Entity createPoseEntity(String name) {
        Entity result = new Entity(name);
        result.addComponent(new PoseComponent());
        return result;
    }

    private Entity createMesh(String filename, ColorRGB color) {
        Entity mesh = createPoseEntity("Mesh");

        MaterialComponent mc = new MaterialComponent();
        mc.setDiffuseColor(color.red/255.0,color.green/255.0,color.blue/255.0,1);
        mesh.addComponent(mc);

        MeshFromFile mff = new MeshFromFile();
        mff.setFilename(filename);
        mesh.addComponent(mff);

        return mesh;
    }
}
