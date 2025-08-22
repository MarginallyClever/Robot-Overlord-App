package com.marginallyclever.ro3.node.nodes.crab;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.mesh.Mesh;
import com.marginallyclever.ro3.mesh.proceduralmesh.Sphere;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;

/**
 * Internal description of each leg for calculating kinematics.
 */
public class CrabLeg {
    private final Crab crab;
    // poses for each joint in the leg
    public Pose coxa = new Pose();
    public Pose femur = new Pose();
    public Pose tibia = new Pose();
    public Pose toe = new Pose();
    // the kinematics for the leg try bring the toe position to touch the target position.
    public Pose targetPosition = new Pose();
    // illustrates the next contact point
    public Pose nextPosition = new Pose();
    // illustrates the last contact point
    public Pose lastPosition = new Pose();

    public double startingCoxaAngle = 0;

    public double angleCoxa = 0;
    public double angleFemur = 0;
    public double angleTibia = 0;

    public final Point3d contactPointIdeal = new Point3d();
    public final Point3d contactPointLast = new Point3d();
    public final Point3d contactPointNext = new Point3d();
    public boolean isTouchingGround = false;
    // should only be true when the leg is in the air and moving towards the ground.
    public boolean isRising = false;
    // Rrue when the leg is moving.  False when the leg is stationary.
    public boolean inMotion = false;

    public CrabLeg(Crab crab,String legName) {
        this.crab = crab;

        coxa.setName(legName + " coxa");
        femur.setName("femur");
        tibia.setName("tibia");
        toe.setName("toe");
        targetPosition.setName(legName+" targetPosition");
        nextPosition.setName(legName+" nextPosition");
        lastPosition.setName(legName+" lastPosition");

        coxa.addChild(femur);
        femur.addChild(tibia);
        tibia.addChild(toe);
        crab.addChild(targetPosition);
        crab.addChild(nextPosition);
        crab.addChild(lastPosition);

        addDecoration(nextPosition,new Sphere(),Color.GREEN);
        addDecoration(lastPosition,new Sphere(),Color.BLUE);

        femur.setPosition(new Vector3d(Crab.COXA, 0, 0));
        tibia.setPosition(new Vector3d(Crab.FEMUR, 0, 0));
        toe.setPosition(new Vector3d(Crab.TIBIA, 0, 0));

        Crab.addMesh(coxa);
        Crab.addMesh(femur);
        Crab.addMesh(tibia);
    }

    private void addDecoration(Pose pose, Mesh mesh, Color color) {
        var mat = new Material();
        mat.setDiffuseColor(color);
        pose.addChild(mat);
        var mi = new MeshInstance();
        mi.setMesh(mesh);
        pose.addChild(mi);
    }

    // set the FK for one leg
    public void setAngles(double coxa, double femur, double tibia) {
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
            m3.rotY(Math.toRadians(femur+90));
            m3.setTranslation(p);
            this.femur.setLocal(m3);
            this.angleFemur = femur;
        }
        // turn tibia
        {
            var m3 = this.tibia.getLocal();
            var p = new Vector3d();
            m3.get(p);
            m3.rotY(Math.toRadians(tibia));
            m3.setTranslation(p);
            this.tibia.setLocal(m3);
            this.angleTibia = tibia;
        }
    }

    public void update(double dt) {
        setPosition(this.nextPosition, contactPointNext);
        setPosition(this.lastPosition, contactPointLast);
    }

    /**
     * Interpolate between pointOfContactLast to pointOfContactNext based on timeUnit.
     * Interpolate the z up and down in an abs(sine) wave.
     * @param timeUnit 0 to 1
     */
    public void animateStep(double timeUnit) {
        inMotion=true;
        timeUnit = Math.min(Math.max(timeUnit,0),1);
        if(isTouchingGround && timeUnit > 0.95) timeUnit = 0;  // don't animate when on the ground.

        double phase = timeUnit * Math.PI;
        this.isRising = timeUnit < 0.5;
        double lift = Math.abs(Math.sin(phase)) * Crab.TOE_STEP_HEIGHT;
        // (next-last)*tineUnit + last
        Vector3d diff = new Vector3d(this.contactPointNext);
        diff.sub(this.contactPointLast);
        diff.scale(timeUnit);
        diff.add(this.contactPointLast);
        diff.z += lift;
        this.targetPosition.setPosition(diff);
    }

    private void setPosition(Pose p,Point3d v) {
        var m = p.getWorld();
        m.setTranslation(new Vector3d(v));
        p.setWorld(m);
    }

    /**
     * Move the foot towards the floor.
     * @param dt
     */
    public void putFootDown(double dt) {
        // put the foot down at the last contact point.
        if(this.isTouchingGround) return;

        var m = this.targetPosition.getWorld();
        var pos = MatrixHelper.getPosition(m);
        pos.z -= dt*5;  // drop until contact is made
        m.setTranslation(pos);
        this.targetPosition.setWorld(m);
    }

    public Point3d getToePosition() {
        return new Point3d(MatrixHelper.getPosition(toe.getWorld()));
    }
}
