package com.marginallyclever.ro3.node.nodes.crab;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.mesh.proceduralmesh.Box;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;
import com.marginallyclever.robotoverlord.components.demo.CrabRobotComponent;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Crab controller demo.</p>
 * <h3>Leg index</h3>
 * <pre>
 * 0 - center left
 * 1 - front right
 * 2 - front left
 * 3 - center right
 * 4 - back left
 * 5 - back right
 * </pre>
 * <h3>Pose format</h3>
 * <p>pose is a compact array of 18 doubles.  the format is <code>coxa/femur/tibia</code> for each leg.  the sequence
 * follows the leg index.</p>
 */
public class Crab extends Node {
    public static final double COXA = 6.2;  // coxa 62mm
    public static final double FEMUR = 15.0;  // femur 150mm
    public static final double TIBIA = 30.0;  // tibia 300mm

    public static final Vector3d CENTER_COXA = new Vector3d(11.80,0,0);
    public static final Vector3d CORNER_COXA = new Vector3d(7.90,11.40,0);
    private static final Vector3d FRONT_RIGHT_TRANSLATION = new Vector3d(CORNER_COXA.x,CORNER_COXA.y,CORNER_COXA.z);
    private static final Vector3d FRONT_LEFT_TRANSLATION = new Vector3d(-CORNER_COXA.x,CORNER_COXA.y,CORNER_COXA.z);
    private static final Vector3d CENTER_LEFT_TRANSLATION = new Vector3d(-CENTER_COXA.x,CENTER_COXA.y,CENTER_COXA.z);
    private static final Vector3d BACK_LEFT_TRANSLATION = new Vector3d(-CORNER_COXA.x,-CORNER_COXA.y,CORNER_COXA.z);
    private static final Vector3d BACK_RIGHT_TRANSLATION = new Vector3d(CORNER_COXA.x,-CORNER_COXA.y,CORNER_COXA.z);
    public static final String[] legNames = {
            "center right",
            "front right",
            "from left",
            "center left",
            "back left",
            "back right"
    };

    private final double [] startingCoxaValues = { 0,45,135,180,225,315};

    public enum WalkStategy {
        GO_LIMP("GO_LIMP"),
        HOME_POSITION("HOME_POSITION"),
        SIT_DOWN("SIT_DOWN"),
        STAND_UP("STAND_UP"),
        TAP_TOE_ONE("TAP_TOE_ONE"),
        RIPPLE1("RIPPLE1"),
        RIPPLE2("RIPPLE2"),
        WALK_THREE_AT_ONCE("WALK_THREE_AT_ONCE");

        public final String name;

        WalkStategy(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    };

    private final Pose body = new Pose("body");
    private final List<CrabLeg> legs = new ArrayList<>();

    private WalkStategy chosenStrategy = WalkStategy.GO_LIMP;
    private double gaitCycleTime = 0;


    public Crab() {
        super();
        setName("Crab");

        // add the body
        addChild(body);
        Crab.addMeshAndMaterial(body);
        body.setPosition(new Vector3d(0,0,4.5));

        // add 6 legs
        for (int i = 0; i < 6; ++i) {
            var leg = new CrabLeg(this,legNames[i]);
            legs.add(leg);
            body.addChild(leg.coxa);
            // position the legs.
            var matrix = new Matrix4d();
            matrix.setIdentity();
            matrix.rotZ(Math.toRadians(360 * i / 6.0));
            leg.coxa.setLocal(matrix);
        }

        fixMeshes();
        initializeAllLegPoses();
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new CrabPanel(this));
        super.getComponents(list);
    }

    private void fixMeshes() {
        // body
        var bodyMesh = body.findFirstChild(MeshInstance.class);
        bodyMesh.setMesh(Registry.meshFactory.load("src/main/resources/crab/body.stl"));
        var m = new Matrix4d();
        var m2 = new Matrix4d();
        m.setIdentity();
        m.rotX(Math.toRadians(90));
        m2.rotY(Math.toRadians(180));
        m.mul(m2, m);
        m.setTranslation(new Vector3d(0, 0, 0));
        bodyMesh.setLocal(m);

        for(int i=0;i<6;++i) {
            var leg = legs.get(i);
            // coxa
            var meshCoxa = leg.coxa.findFirstChild(MeshInstance.class);
            meshCoxa.setMesh(Registry.meshFactory.load("src/main/resources/crab/coxa.stl"));

            // femur
            var meshFemur = leg.femur.findFirstChild(MeshInstance.class);
            meshFemur.setMesh(Registry.meshFactory.load("src/main/resources/crab/femur.stl"));

            // tibia
            var meshTibia = leg.tibia.findFirstChild(MeshInstance.class);
            meshTibia.setMesh(Registry.meshFactory.load("src/main/resources/crab/tibia.stl"));
        }
    }

    private void initializeAllLegPoses() {
        initializeLegPose(0, CENTER_COXA, startingCoxaValues[0], "center right");
        initializeLegPose(1, FRONT_RIGHT_TRANSLATION, startingCoxaValues[1], "front right");
        initializeLegPose(2, FRONT_LEFT_TRANSLATION, startingCoxaValues[2], "front left");
        initializeLegPose(3, CENTER_LEFT_TRANSLATION, startingCoxaValues[3], "center left");
        initializeLegPose(4, BACK_LEFT_TRANSLATION, startingCoxaValues[4], "back left");
        initializeLegPose(5, BACK_RIGHT_TRANSLATION, startingCoxaValues[5], "back right");

        for(CrabLeg leg : legs) {
            adjustCoxaAndFemurBones(leg);
        }
    }

    private void initializeLegPose(int legIndex, Vector3d translation, double rotZDeg, String name) {
        Matrix4d m = new Matrix4d();
        m.setIdentity();
        m.rotZ(Math.toRadians(rotZDeg));
        m.setTranslation(translation);
        CrabLeg leg = this.legs.get(legIndex);
        leg.coxa.setLocal(m);
        leg.coxa.setName(name);
        leg.angleCoxa = rotZDeg;
        leg.coxaAdjust = rotZDeg;
    }

    /**
     * adjust both coxa and femur leg bones.
     * @param leg
     */
    private void adjustCoxaAndFemurBones(CrabLeg leg) {
        translateZOnePose(leg.coxa,2.6);
        translateZOnePose(leg.femur,-2.0);
    }

    /**
     * translate one {@link Pose} bone along z a small amount.
     * @param legBone the {@link Pose} to move
     * @param zAdj the amount to move
     */
    private void translateZOnePose(Pose legBone, double zAdj) {
        var m3 = legBone.getLocal();
        var p = new Vector3d();
        m3.get(p);
        p.z = zAdj;
        m3.setTranslation(p);
        legBone.setLocal(m3);
    }


    static void addMeshAndMaterial(Node node) {
        node.addChild(new Material());
        var mi = new MeshInstance();
        node.addChild(mi);
        var box = new Box();
        mi.setMesh(box);
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        gaitCycleTime+=dt;

        // run the choosen strategy
        // TODO blend two or more together.
        switch(chosenStrategy) {
            case HOME_POSITION: homePosition();  break;
            case SIT_DOWN:  sitDown(dt);  break;
            case STAND_UP:  standUp(dt);  break;
            case TAP_TOE_ONE:  tapOneToe(dt);  break;
            case WALK_THREE_AT_ONCE:  walkThreeAtOnce(dt);  break;
            case RIPPLE1:  ripple1(dt);  break;
            case RIPPLE2:  ripple2(dt);  break;
            default:  goLimp();
                // do nothing
                break;
        }
    }

    /**
     *  do not control the target position of each leg.
     *  this allows the app user to move them with the on screen tools and test the inverse kinematics.
     */
    private void goLimp() {
        solveKinematicsForAllLegs();
    }

    private void solveKinematicsForAllLegs() {
        for(int i=0;i<6;++i) {
            var leg = legs.get(i);
            Pose pose = leg.targetPosition;
            if(pose!=null) {
                // we need pos relative to coxa
                var pos = new Point3d(MatrixHelper.getPosition(pose.getWorld()));
                moveToeForOneLeg(i, pos);
            }
        }
    }

    private void homePosition() {
        for(int i=0;i<6;++i) {
            legs.get(i).setAngles(startingCoxaValues[i],0,0);
        }
    }

    boolean firstSit = false;

    private void sitDown(double dt) {
        for(int i=0;i<6;++i) {
            var leg = legs.get(i);
            leg.setAngles(legs.get(i).angleCoxa,-115-90,150);
            var pos = MatrixHelper.getPosition(leg.toe.getWorld());
            pos.z=0;
            leg.contactPointLast.set(pos);
            leg.contactPointNext.set(pos);
            leg.targetPosition.setPosition(new Vector3d(leg.contactPointLast));
        }
        firstSit=true;
        firstStand=false;
    }

    boolean firstStand = false;
    //double firstStandRadius = 0;

    private void standUp(double dt) {
        if(!firstSit) return;

        var bodyWorld = body.getWorld();
        bodyWorld.m22=1;
        if(bodyWorld.m23<6) bodyWorld.m23+=dt;
        if(bodyWorld.m23>6) bodyWorld.m23-=dt/2;
        body.setWorld(bodyWorld);

        for(int i=0;i<6;++i) {
            var leg = legs.get(i);

            Pose pose = leg.targetPosition;
            if (pose != null) {
                Matrix4d m = pose.getWorld();
                m.setTranslation(leg.contactPointLast);
                pose.setWorld(m);
            }
        }

        solveKinematicsForAllLegs();
    }

    /**
     * Tap the first toe up and down in a sine wave.
     * @param dt
     */
    private void tapOneToe(double dt) {
        final double cycleTime = 2.0; // seconds for one full cycle
        double timeInCycle = (System.currentTimeMillis() % (long)(cycleTime*1000)) / 1000.0;
        double timeUnit = (timeInCycle / cycleTime);  // 0 to 1
        animateOneLeg(legs.getFirst(),timeUnit);

        solveKinematicsForAllLegs();
    }

    /**
     * Interpolate a the {@link CrabLeg#targetPosition} between pointOfContactLast to pointOfContactNext based on timeUnit.
     * Interpolate the z up and down in an abs(sine) wave.
     * @param leg the leg leg to animate
     * @param timeUnit 0 to 1
     */
    private void animateOneLeg(CrabLeg leg, double timeUnit) {
        final double liftHeight = 5.0; // mm to lift the toe

        double phase = timeUnit * 1.0 * Math.PI;
        double lift = Math.abs(Math.sin(phase)) * liftHeight;
        Vector3d diff = new Vector3d(leg.contactPointNext);
        diff.sub(leg.contactPointLast);
        diff.scale(timeUnit);
        diff.add(leg.contactPointLast);
        diff.z += lift;
        leg.targetPosition.setPosition(diff);
    }

    private void ripple1(double dt) {
        var zeroToSix = (System.currentTimeMillis() % 6000) / 1000.0;
             if(zeroToSix<1) animateOneLeg(legs.get(0), zeroToSix);  // first leg
        else if(zeroToSix>2-1 && zeroToSix<2) animateOneLeg(legs.get(2), zeroToSix-1);  // second leg
        else if(zeroToSix>3-1 && zeroToSix<3) animateOneLeg(legs.get(4), zeroToSix-2);  // third leg
        else if(zeroToSix>4-1 && zeroToSix<4) animateOneLeg(legs.get(1), zeroToSix-3);  // fourth leg
        else if(zeroToSix>5-1 && zeroToSix<5) animateOneLeg(legs.get(3), zeroToSix-4);  // fifth leg
        else if(zeroToSix>5)                  animateOneLeg(legs.get(5), zeroToSix-5);  // sixth leg

        solveKinematicsForAllLegs();
    }

    /**
     * 2   1
     * 3   0
     * 4   5
     * order should be 2,5,3,1,4,0
     */
    private void ripple2(double dt) {
        double gc1 = gaitCycleTime + 0.5f;
        double gc2 = gaitCycleTime;

        double x1 = gc1 - Math.floor(gc1);
        double x2 = gc2 - Math.floor(gc2);
        double step1 = Math.max(0, x1);
        double step2 = Math.max(0, x2);
        int leg1 = (int) Math.floor(gc1) % 3;
        int leg2 = (int) Math.floor(gc2) % 3;

        int o1, o2;
        o1 = switch (leg1) {
            case 0 -> 2;
            case 1 -> 3;
            case 2 -> 4;
            default -> 0;
        };
        o2 = switch (leg2) {
            case 0 -> 5;
            case 1 -> 1;
            case 2 -> 0;
            default -> 0;
        };
        animateOneLeg(legs.get(o1), step1);
        animateOneLeg(legs.get(o2), step2);

        // put all feet down except the active leg(s).
        for(int i=0;i<6;++i) {
            if(i!=o1 && i!=o2) {
                putFootDown(legs.get(i),dt);
            }
        }

        solveKinematicsForAllLegs();
    }

    /**
     * Walk three legs at once in a tripod gait.
     * @param dt the time step
     */
    private void walkThreeAtOnce(double dt) {
        double step = (gaitCycleTime - Math.floor(gaitCycleTime));
        int legToMove = ((int) Math.floor(gaitCycleTime) % 2);

        // put all feet down except the active leg(s).
        for (int i = 0; i < CrabRobotComponent.NUM_LEGS; ++i) {
            if ((i % 2) != legToMove) {
                putFootDown(legs.get(i),dt);
            } else {
                animateOneLeg(legs.get(i), step);
            }
        }

        solveKinematicsForAllLegs();
    }

    private void putFootDown(CrabLeg leg,double dt) {
        // put the foot down at the last contact point.
        var m = leg.targetPosition.getWorld();
        var pos = MatrixHelper.getPosition(m);
        //if(pos.z>0) pos.z-= dt;
        pos.z=0;
        m.setTranslation(pos);
        leg.targetPosition.setWorld(m);
    }

    /**
     * Get all 18 joint angles that we care about.
     * @return see the post description in the class header.
     */
    public double [] getPose() {
        var list = new double [18];
        int j=0;

        for(int i=0;i<6;++i) {
            var leg =  legs.get(i);
            list[j++] = leg.angleCoxa;
            list[j++] = leg.angleFemur;
            list[j++] = leg.angleTibia;
        }

        return list;
    }

    /**
     * Set the 18 joint angles that we care about.
     * @param list see the post description in the class header.
     */
    public void setPose(double [] list) {
        if(list.length!=18) throw new IllegalArgumentException("list length must be 18.");

        int j=0;

        for(int i=0;i<6;++i) {
            double a =  list[j++];
            double b =  list[j++];
            double c =  list[j++];
            legs.get(i).setAngles(a,b,c);
        }
    }

    /**
     * If the kinematics are unsolvable the angles will be NaN and the leg bones will disappear.
     * @param legIndex the leg to move
     * @param newPosition the desired world position of the toe tip.
     */
    public void moveToeForOneLeg(int legIndex, Point3d newPosition) {
        var leg = legs.get(legIndex);

        var bodyMatrix = body.getWorld();
        bodyMatrix.invert();
        var relativeToBody = new Point3d();
        bodyMatrix.transform(newPosition,relativeToBody);

        var coxaPos = MatrixHelper.getPosition(leg.coxa.getLocal());
        Vector3d relativeToCoxa = new Vector3d();
        relativeToCoxa.sub(relativeToBody,coxaPos);

        double x = relativeToCoxa.x;
        double y = relativeToCoxa.y;
        double z = relativeToCoxa.z;

        double d = Math.sqrt(x * x + y * y);  // horizontal distance from toe tip to axis of the femur motor
        double r = d - COXA;                  // horizontal distance from the coxa motor to the tip of the foot
        double c = Math.sqrt(z * z + r * r);  // linear distance from femur motor axis to tip of the foot

        // Angles in degrees
        double angleCoxa = Math.toDegrees(Math.atan2(y, x));  // aka theta0
        double angleFemur = -Math.toDegrees(Math.atan2(r,-z) + Math.acos((FEMUR * FEMUR + c * c - TIBIA * TIBIA) / (2 * FEMUR * c)));  // aka theta1
        double angleTibia = 180.0 - Math.toDegrees(Math.acos((FEMUR * FEMUR + TIBIA * TIBIA - c * c) / (2 * FEMUR * TIBIA)));  // aka theta2

        legs.get(legIndex).setAngles(angleCoxa,angleFemur,angleTibia);
    }

    public void setChosenStrategy(WalkStategy chosenStrategy) {
        this.chosenStrategy = chosenStrategy;
    }

    public WalkStategy getChosenStrategy() {
        return chosenStrategy;
    }
}

