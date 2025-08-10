package com.marginallyclever.ro3.node.nodes.crab;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.mesh.proceduralmesh.Box;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;

/**
 * Crab controller demo
 */
public class Crab extends Pose {
    public static final double COXA = 6.2;  // coxa 62mm
    public static final double FEMUR = 15.0;  // femur 150mm
    public static final double  TIBIA = 30.0;  // tibia 300mm

    public static final Vector3d CENTER_COXA = new Vector3d(11.80,0,0);
    public static final Vector3d CORNER_COXA = new Vector3d(7.90,11.40,0);


    public enum WalkStategy {
        DO_NOTHING,
        SIT_DOWN,
        STAND_UP,
        WALK_THREE_AT_ONCE,
        RIPPLE1,
        RIPPLE2,
    };

    private WalkStategy chosenStrategy = WalkStategy.DO_NOTHING;

    public class Leg {
        public Pose coxa = new Pose();
        public Pose femur = new Pose();
        public Pose tibia = new Pose();
        public Pose toe = new Pose();

        public double angleCoxa=0;
        public double angleFemur=0;
        public double angleTibia=0;

        public Leg() {
            coxa.addChild(femur);
            femur.addChild(tibia);
            tibia.addChild(toe);

            coxa.setName("coxa");
            femur.setName("femur");
            tibia.setName("tibia");
            toe.setName("toe");

            femur.setPosition(new Vector3d(COXA, 0, 0));
            tibia.setPosition(new Vector3d(FEMUR, 0, 0));
            toe.setPosition(new Vector3d(TIBIA, 0, 0));

            addMeshAndMaterial(coxa);
            addMeshAndMaterial(femur);
            addMeshAndMaterial(tibia);
            addMeshAndMaterial(toe);
        }

        // set the FK for one leg
        private void setAngles(double coxa,double femur,double tibia) {
            // turn coxa
            {
                var m3 = this.coxa.getLocal();
                var p = new Vector3d();
                m3.get(p);
                m3.rotZ(Math.toRadians(coxa));
                m3.setTranslation(p);
                this.coxa.setLocal(m3);
                this.angleCoxa = coxa;
            }
            // turn femur
            {
                var m3 = this.femur.getLocal();
                var p = new Vector3d();
                m3.get(p);
                m3.rotY(Math.toRadians(coxa));
                m3.setTranslation(p);
                this.femur.setLocal(m3);
                this.angleFemur = femur;
            }
            // turn tibia
            {
                var m3 = this.tibia.getLocal();
                var p = new Vector3d();
                m3.get(p);
                m3.rotY(Math.toRadians(coxa));
                m3.setTranslation(p);
                this.tibia.setLocal(m3);
                this.angleTibia = tibia;
            }
        }
    }

    private final List<Leg> legs = new ArrayList<>();

    public Crab() {
        super();
        setName("Crab");

        addMeshAndMaterial(this);

        // add 6 legs
        for (int i = 0; i < 6; ++i) {
            var leg = new Leg();
            legs.add(leg);
            this.addChild(leg.coxa);

            // position the legs.
            var matrix = new Matrix4d();
            matrix.setIdentity();
            matrix.rotZ(Math.toRadians(360 * i / 6.0));
            leg.coxa.setLocal(matrix);
        }


        fixMeshes();
        positionLegs();

        // raise the body
        this.setPosition(new Vector3d(0,0,4.5));
    }

    private void fixMeshes() {
        // body
        var bodyMesh = this.findFirstChild(MeshInstance.class);
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

    private void positionLegs() {
        var m = new Matrix4d();
        // center leg left (x+)
        m = new Matrix4d();
        m.setIdentity();
        m.setTranslation(CENTER_COXA);
        var centerLeft = this.legs.get(0);
        centerLeft.coxa.setLocal(m);
        centerLeft.coxa.setName("center left");

        // front right
        m = new Matrix4d();
        m.setIdentity();
        m.rotZ(Math.toRadians(45));

        m.setTranslation(new Vector3d(CORNER_COXA.x,CORNER_COXA.y,CORNER_COXA.z));
        var frontRight = this.legs.get(1);
        frontRight.coxa.setLocal(m);
        frontRight.coxa.setName("front right");
        frontRight.angleCoxa = 45;

        // front left
        m = new Matrix4d();
        m.setIdentity();
        m.rotZ(Math.toRadians(45+90));
        m.setTranslation(new Vector3d(-CORNER_COXA.x,CORNER_COXA.y,CORNER_COXA.z));
        var frontLeft = this.legs.get(2);
        frontLeft.coxa.setLocal(m);
        frontLeft.coxa.setName("front left");
        frontLeft.angleCoxa = 45+90;

        // center leg right (x-)
        m = new Matrix4d();
        m.setIdentity();
        m.rotZ(Math.toRadians(180));
        m.setTranslation(new Vector3d(-CENTER_COXA.x,CENTER_COXA.y,CENTER_COXA.z));
        var centerRight = this.legs.get(3);
        centerRight.coxa.setLocal(m);
        centerRight.coxa.setName("center right");
        centerRight.angleCoxa = 180;

        // back left
        m = new Matrix4d();
        m.setIdentity();
        m.rotZ(Math.toRadians(45+90+90));
        m.setTranslation(new Vector3d(-CORNER_COXA.x,-CORNER_COXA.y,CORNER_COXA.z));
        var backLeft = this.legs.get(4);
        backLeft.coxa.setLocal(m);
        backLeft.coxa.setName("back left");
        backLeft.angleCoxa = 45+90+90;

        // back right
        m = new Matrix4d();
        m.setIdentity();
        m.rotZ(Math.toRadians(45+90+90+90));
        m.setTranslation(new Vector3d(CORNER_COXA.x,-CORNER_COXA.y,CORNER_COXA.z));
        var backRight = this.legs.get(5);
        backRight.coxa.setLocal(m);
        backRight.coxa.setName("back right");
        backRight.angleCoxa = 45+90+90+90;

        for(int i=0;i<6;++i) {
            var leg = legs.get(i);
            {
                var m3 = leg.coxa.getLocal();
                var p = new Vector3d();
                m3.get(p);
                p.z = 2.6;
                m3.setTranslation(p);
                leg.coxa.setLocal(m3);
            }
            {
                var m3 = leg.femur.getLocal();
                var p = new Vector3d();
                m3.get(p);
                p.z = -2.0;
                m3.setTranslation(p);
                leg.femur.setLocal(m3);
            }
        }
    }

    private void addMeshAndMaterial(Node node) {
        node.addChild(new Material());
        var mi = new MeshInstance();
        node.addChild(mi);
        var box = new Box();
        mi.setMesh(box);
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        // choose one of many strategies
        chosenStrategy = WalkStategy.SIT_DOWN;
        // or blend two or more together
        switch(chosenStrategy) {
            case SIT_DOWN:  sitDown();  break;
            case STAND_UP:  standUp();  break;
            case WALK_THREE_AT_ONCE:  walkThreeAtOnce();  break;
            case RIPPLE1:  ripple1();  break;
            case RIPPLE2:  ripple2();  break;
            default:
                // do nothing
                break;
        }
    }

    private void sitDown() {
        for(int i=0;i<6;++i) {
            legs.get(i).setAngles(legs.get(i).angleCoxa,-115,150);
        }
    }

    private void standUp() {
    }

    private void walkThreeAtOnce() {

    }

    private void ripple1() {
    }

    private void ripple2() {
    }
}

