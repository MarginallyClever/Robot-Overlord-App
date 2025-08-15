package com.marginallyclever.ro3.node.nodes.crab;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.mesh.proceduralmesh.Box;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.material.Material;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
 *
 * Forward
 *  2   1
 *  3 x 0 Right
 *  4   5</pre>
 * <h3>Pose format</h3>
 * <p>pose is a compact array of 18 doubles.  the format is <code>coxa/femur/tibia</code> for each leg.  the sequence
 * follows the leg index.</p>
 */
public class Crab extends Node {
    public static final int NUM_LEGS = 6;  // number of legs on the crab robot
    static final String RESOURCE_PATH = "src/main/resources/com/marginallyclever/ro3/node/nodes/crab/";
    // size of each bone
    public static final double COXA = 6.2;  // coxa 62mm
    public static final double FEMUR = 15.0;  // femur 150mm
    public static final double TIBIA = 30.0;  // tibia 300mm
    // offset from body center to each coxa joint.
    public static final double CENTER_X =11.8;
    public static final double COXA_X = 7.9;
    public static final double COXA_Y = 11.4;

    public static final double COXA_OFFSET = 2.6;
    public static final double FEMUR_OFFSET = 2.0;

    public static final double SITTING_HEIGHT=4.5;
    public static final double STANDING_EPSILON = 0.01;
    public static final double TOE_STEP_HEIGHT = 5.0;  // mm to lift the toe

    public static final Vector3d [] legOffsets = {
            new Vector3d(CENTER_X,0,0),
            new Vector3d(COXA_X,COXA_Y,0),
            new Vector3d(-COXA_X,COXA_Y,0),
            new Vector3d(-CENTER_X, 0, 0),
            new Vector3d(-COXA_X,-COXA_Y,0),
            new Vector3d(COXA_X,-COXA_Y,0),
    };

    public static final String[] legNames = {
            "center right",
            "front right",
            "from left",
            "center left",
            "back left",
            "back right"
    };

    private final double [] startingCoxaAngles = { 0,45,135,180,225,315};

    public static final int DEFAULT_STANDING_HEIGHT = 8;

    // links to body parts for quick access.
    private final Pose body = new Pose("body");
    private final List<CrabLeg> legs = new ArrayList<>();

    // animation strategies
    private CrabWalkStategy chosenStrategy = CrabWalkStategy.SIT_DOWN;
    // animation timer
    private double gaitCycleTime = 0;
    // flags for sit and stand
    boolean firstSit = false;
    boolean firstStand = false;

    // walking controls
    private double movingTurning;
    private double movingForward;
    private double movingRight;
    private double torsoHeight = DEFAULT_STANDING_HEIGHT;


    public Crab() {
        super();
        setName("Crab");

        // add the body
        addChild(body);
        Crab.addMesh(body);
        var mat = new Material();
        body.addChild(mat);
        mat.setDiffuseColor(new Color(51,153,255));
        body.setPosition(new Vector3d(0,0,4.5));

        // add legs
        for (int i = 0; i < NUM_LEGS; ++i) {
            var leg = new CrabLeg(this,legNames[i]);
            legs.add(leg);
            body.addChild(leg.coxa);
        }

        fixMeshes();
        initializeAllLegPoses();
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new CrabPanel(this));
        super.getComponents(list);
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-crab-16.png")));
    }

    /**
     * The current STL files are not the correct size or orientation.
     * This method fixes the meshes at load time.
     */
    private void fixMeshes() {
        // body
        var bodyMesh = body.findFirstChild(MeshInstance.class);
        bodyMesh.setMesh(Registry.meshFactory.load(RESOURCE_PATH+"body.stl"));
        var m = new Matrix4d();
        var m2 = new Matrix4d();
        m.setIdentity();
        m.rotX(Math.toRadians(90));
        m2.rotY(Math.toRadians(180));
        m.mul(m2, m);
        bodyMesh.setLocal(m);
        // legs
        for(var leg : legs) {
            // coxa
            var meshCoxa = leg.coxa.findFirstChild(MeshInstance.class);
            meshCoxa.setMesh(Registry.meshFactory.load(RESOURCE_PATH+"coxa.stl"));
            // femur
            var meshFemur = leg.femur.findFirstChild(MeshInstance.class);
            meshFemur.setMesh(Registry.meshFactory.load(RESOURCE_PATH+"femur.stl"));
            // tibia
            var meshTibia = leg.tibia.findFirstChild(MeshInstance.class);
            meshTibia.setMesh(Registry.meshFactory.load(RESOURCE_PATH+"tibia.stl"));
        }
    }

    private void initializeAllLegPoses() {
        for(int i=0;i<NUM_LEGS;++i) {
            initializeLegPose(i, legOffsets[i], startingCoxaAngles[i], legNames[i]);
        }

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
        leg.startingCoxaAngle = rotZDeg;
    }

    /**
     * adjust both coxa and femur leg bones.
     * @param leg the {@link CrabLeg} to adjust
     */
    private void adjustCoxaAndFemurBones(CrabLeg leg) {
        translateZOnePose(leg.coxa,COXA_OFFSET);
        translateZOnePose(leg.femur,-FEMUR_OFFSET);
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

    static void addMesh(Node node) {
        var mi = new MeshInstance();
        node.addChild(mi);
        var box = new Box();
        mi.setMesh(box);
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        gaitCycleTime+=dt;

        checkLegsTouchingGround();

        // run the chosen strategy
        // TODO blend two or more together.
        switch(chosenStrategy) {
            case HOME_POSITION: homePosition();  break;
            case SIT_DOWN:  sitDown(dt);  break;
            case STAND_UP:  standUp(dt);  break;
            case TAP_TOE_ONE:  tapOneToe();  break;
            case WALK_THREE_AT_ONCE:  walkThreeAtOnce(dt);  break;
            case WALK_RIPPLE:  walkRipple1(dt);  break;
            case WALK_WAVE:  walkRipple2(dt);  break;
            default:  goLimp();
                // do nothing
                break;
        }

        for(var leg : legs) {
            leg.update(dt);
        }
    }

    /**
     * Check if each leg is touching the ground.
     * If they are, update the last contact point.
     */
    private void checkLegsTouchingGround() {
        for(var leg : legs) {
            // is the leg touching the ground?
            var p = MatrixHelper.getPosition(leg.toe.getWorld());
            leg.isTouchingGround = ( p.z <= 0.01 );
            // if yes, update the contact point.
            if(leg.isTouchingGround) {
                p.z=0;
                leg.contactPointLast.set(p);  // update the last contact point
            }
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
        for(var leg : legs) {
            // we need pos relative to coxa
            var pos = new Point3d(MatrixHelper.getPosition(leg.targetPosition.getWorld()));
            moveToeForOneLeg(leg, pos);
        }
    }

    private void homePosition() {
        for(var leg : legs) {
            leg.setAngles(leg.startingCoxaAngle,0,0);
        }
    }

    private boolean allFeetDown() {
        for(var leg : legs) {
            if(!leg.isTouchingGround) return false;
        }
        return true;
    }

    private void sitDown(double dt) {
        for(var leg : legs) {
            leg.setAngles(leg.startingCoxaAngle,-115-90,150);
            var pos = MatrixHelper.getPosition(leg.toe.getWorld());
            pos.z=0;
            leg.targetPosition.setPosition(pos);
            leg.contactPointIdeal.set(pos);
            leg.contactPointLast.set(pos);
            leg.contactPointNext.set(pos);
        }

        for(var leg : legs) {
            leg.putFootDown(dt);
        }

        if(allFeetDown()) {
            adjustBodyHeight(dt,SITTING_HEIGHT);
            // now lower the body to the ground.
            firstSit = true;
            // if we are sitting then we are not standing.
            firstStand=false;
        }

        solveKinematicsForAllLegs();
    }

    private void standUp(double dt) {
        if (!firstSit) return;

        for(var leg : legs) {
            leg.putFootDown(dt);
        }
        // when all feet down we can start standing up.
        if(allFeetDown()) {
            adjustBodyHeight(dt,torsoHeight);

            var bodyWorld = body.getWorld();
            if (bodyWorld.m23 >= torsoHeight &&
                    bodyWorld.m23 < torsoHeight + STANDING_EPSILON) {
                // now we are standing.
            }
            if( bodyWorld.m23 >= 0.01) {
                // we are standing.
                firstStand = true;
            }
        }

        solveKinematicsForAllLegs();
    }

    /**
     * Tap the first toe up and down in a sine wave.
     */
    private void tapOneToe() {
        final double cycleTime = 2.0; // seconds for one full cycle
        double timeInCycle = gaitCycleTime % (long)cycleTime;
        double timeUnit = (timeInCycle / cycleTime);  // 0 to 1
        legs.getFirst().animateStep(timeUnit);

        solveKinematicsForAllLegs();
    }

    private void walkRipple1(double dt) {
        double zeroToSix = gaitCycleTime % (double)NUM_LEGS;
             if(                zeroToSix<1) legs.get(2).animateStep(zeroToSix);
        else if(zeroToSix>=1 && zeroToSix<2) legs.get(5).animateStep(zeroToSix-1);
        else if(zeroToSix>=2 && zeroToSix<3) legs.get(4).animateStep(zeroToSix-2);
        else if(zeroToSix>=3 && zeroToSix<4) legs.get(1).animateStep(zeroToSix-3);
        else if(zeroToSix>=4 && zeroToSix<5) legs.get(4).animateStep(zeroToSix-4);
        else if(zeroToSix>=5               ) legs.get(0).animateStep(zeroToSix-5);

        walk(dt,1.0/NUM_LEGS);
        solveKinematicsForAllLegs();
    }

    /**
     * 2   1
     * 3   0
     * 4   5
     * order should be 2,5,3,1,4,0
     */
    private void walkRipple2(double dt) {
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
        legs.get(o1).animateStep(step1);
        legs.get(o2).animateStep(step2);

        // put all feet down except the active leg(s).
        for(int i=0;i<NUM_LEGS;++i) {
            if(i!=o1 && i!=o2) {
                legs.get(i).putFootDown(dt);
            }
        }

        walk(dt,2.0/NUM_LEGS);
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
        for (int i = 0; i < NUM_LEGS; ++i) {
            if ((i % 2) != legToMove) {
                legs.get(i).putFootDown(dt);
            } else {
                legs.get(i).animateStep(step);
            }
        }

        walk(dt,3.0/NUM_LEGS);
        solveKinematicsForAllLegs();
    }

    /**
     * Walk the crab robot.  This method is called by the various walk strategies.
     * <ul>
     *     <li>Move the body towards the average of the next contact points + the desired height.</li>
     *     <li>Adjust the position of contactPointNext for each leg based on the walk directions.</li>
     * </ul>
     * @param dt the time step
     * @param turnScale a scale factor for turning, based on the relative speed of the legs.
     */
    private void walk(double dt,double turnScale) {
        if(!firstStand) return;  // only walk if we are standing

        adjustBodyHeight(dt,torsoHeight);

        var m = body.getWorld();

        double angleRad = Math.toRadians(movingTurning);
        var rotZ = new Matrix4d();
        rotZ.rotZ(angleRad*dt*turnScale);

        // rotate the body without drifting.
        {
            Vector3d before = MatrixHelper.getPosition(m);
            m.mul(rotZ,m);  // apply the rotation
            m.setTranslation(before);

            // set the new body matrix
            body.setWorld(m);
        }

        // adjust the next contact points for each leg based on the walk directions and body orientation.
        var xAxis = MatrixHelper.getXAxis(m);
        var yAxis = MatrixHelper.getYAxis(m);
        var bodyPos = MatrixHelper.getPosition(m);
        rotZ.rotZ(angleRad);
        for(var leg : legs) {
            Vector3d nextContact = new Vector3d(leg.contactPointLast);
            nextContact.scaleAdd(movingRight,xAxis,nextContact);
            nextContact.scaleAdd(movingForward,yAxis,nextContact);

            // rotate the next contact point around the body position.
            nextContact.sub(bodyPos);  // make relative to body position
            rotZ.transform(nextContact);
            nextContact.add(bodyPos);  // put it back in world space
            // apply
            leg.contactPointNext.set(nextContact);
        }
    }

    private void adjustBodyHeight(double dt,double newHeight)  {
        var m = body.getWorld();
        var bodyPos = MatrixHelper.getPosition(m);

        // body average position
        Vector3d desiredBodyPos = new Vector3d();
        for (var leg : legs) {
            // average the contact points
            desiredBodyPos.x += leg.contactPointNext.x;
            desiredBodyPos.y += leg.contactPointNext.y;
            desiredBodyPos.z += leg.contactPointNext.z;
        }
        desiredBodyPos.scale(1.0 / NUM_LEGS);  // overall average
        desiredBodyPos.z += newHeight;  // plus the torso height

        // move in the direction of desired position without going past.
        Vector3d direction = new Vector3d();
        direction.sub(desiredBodyPos, bodyPos);
        if (direction.length() > dt) {
            desiredBodyPos.scaleAdd(dt, direction, bodyPos);
        }
        m.setTranslation(desiredBodyPos);
        // apply
        body.setWorld(m);
    }

    /**
     * Get all 18 joint angles that we care about.
     * @return see the post description in the class header.
     */
    public double [] getPose() {
        var list = new double [18];
        int j=0;

        for(int i=0;i<NUM_LEGS;++i) {
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

        for(int i=0;i<NUM_LEGS;++i) {
            double a =  list[j++];
            double b =  list[j++];
            double c =  list[j++];
            legs.get(i).setAngles(a,b,c);
        }
    }

    /**
     * If the kinematics are unsolvable the angles will be NaN and the leg bones will disappear.
     * @param leg the leg to move
     * @param newPosition the desired world position of the toe tip.
     */
    public void moveToeForOneLeg(CrabLeg leg, Point3d newPosition) {
        var bodyMatrix = body.getWorld();
        bodyMatrix.invert();
        var relativeToBody = new Point3d();
        bodyMatrix.transform(newPosition,relativeToBody);

        var coxaPos = MatrixHelper.getPosition(leg.coxa.getLocal());
        Vector3d relativeToCoxa = new Vector3d();
        relativeToCoxa.sub(relativeToBody,coxaPos);

        double x = relativeToCoxa.x;
        double y = relativeToCoxa.y;
        double z = relativeToCoxa.z+FEMUR_OFFSET;

        double d = Math.sqrt(x * x + y * y);  // horizontal distance from toe tip to axis of the femur motor
        double r = d - COXA;                  // horizontal distance from the coxa motor to the tip of the foot
        double c = Math.sqrt(z * z + r * r);  // linear distance from femur motor axis to tip of the foot

        // Angles in degrees
        double angleCoxa = Math.toDegrees(Math.atan2(y, x));  // aka theta0
        double angleFemur = -Math.toDegrees(Math.atan2(r,-z) + Math.acos((FEMUR * FEMUR + c * c - TIBIA * TIBIA) / (2 * FEMUR * c)));  // aka theta1
        double angleTibia = 180.0 - Math.toDegrees(Math.acos((FEMUR * FEMUR + TIBIA * TIBIA - c * c) / (2 * FEMUR * TIBIA)));  // aka theta2

        leg.setAngles(angleCoxa,angleFemur,angleTibia);
    }

    public void setChosenStrategy(CrabWalkStategy chosenStrategy) {
        this.chosenStrategy = chosenStrategy;
    }

    public CrabWalkStategy getChosenStrategy() {
        return chosenStrategy;
    }

    public void turnLeft(double speed) {
        movingTurning+=speed;
    }

    public void forward(double speed) {
        movingForward+=speed;
    }

    public void strafeRight(double speed) {
        movingRight+=speed;
    }

    public void raiseTorso(double amount) {
        torsoHeight+=amount;
    }

    public void idle() {
        movingTurning = 0;
        movingForward = 0;
        movingRight = 0;

        // set the contactPointNext of each leg to the ideal relaxed floor position.
        var p = MatrixHelper.getPosition(body.getWorld());
        for(var leg : legs) {
            var p2 = new Vector3d(leg.contactPointIdeal);
            p2.add(p);
            leg.contactPointNext.set(p2);
        }
    }
}

