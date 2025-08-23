package com.marginallyclever.ro3.node.nodes.crab;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.mesh.Mesh;
import com.marginallyclever.ro3.mesh.proceduralmesh.Sphere;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;
import com.marginallyclever.ro3.raypicking.RayHit;
import com.marginallyclever.ro3.raypicking.RayPickSystem;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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
    // animation state
    private CrabLegPhase phase = CrabLegPhase.REST;
    private double animationTime = 0;
    private final Vector3d fallDir = new Vector3d();

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

    /**
     * Update the visualization of the next and last contact points.
     */
    public void updateVisualizations() {
        setPosition(this.nextPosition, contactPointNext);
        setPosition(this.lastPosition, contactPointLast);
    }

    /**
     * Interpolate between pointOfContactLast to pointOfContactNext based on timeUnit.
     * Interpolate the z up and down in an abs(sine) wave.
     * @param timeUnit 0 to 1
     */
    public void animateStep(double timeUnit) {
        switch(phase) {
            case REST:  return;  // do nothing
            case RISE:  doRise(timeUnit);  break;
            case SWING:  doSwing(timeUnit);  break;
            case FALL:  doFall(timeUnit);  break;
        }
    }

    /**
     * rising leg motion.
     * @param dt time delta
     */
    private void doRise(double dt) {
        animationTime +=dt;
        // lift should take 0.33s.  sin curve range from 0 to PI/2.
        double t = animationTime / 0.33;
        double sinRange = Math.PI / 2.0;
        double lift = Math.abs(Math.sin(t * sinRange)) * Crab.TOE_STEP_HEIGHT;

        interpolateWithLift(animationTime,lift);

        if(animationTime > 0.33) {
            phase = CrabLegPhase.SWING;  // switch to swing phase
        }
    }

    /**
     * swinging leg motion
     * The leg is already in the air and is moving forward.
     * @param dt time delta
     */
    private void doSwing(double dt) {
        animationTime += dt;

        interpolateWithLift(animationTime,Crab.TOE_STEP_HEIGHT);

        if(animationTime > 0.66) {
            phase = CrabLegPhase.FALL;  // switch to fall phase
            fallDir.sub(contactPointNext,targetPosition.getPosition());
            fallDir.normalize();
        }
    }

    /**
     * falling leg motion.  The leg is moving down to the ground under the influence of gravity.
     * if the toe touches the ground then switch to resting phase.
     * if the falling phase runs too long then switch to resting phase.
     * @param dt time delta
     */
    private void doFall(double dt) {
        if(isTouchingGround || animationTime > 2.1) {
            phase = CrabLegPhase.REST;
            animationTime = 0;
            return;
        }

        animationTime += dt;
        double tFall = (animationTime - 0.66)/0.33;

        double lift = Crab.TOE_STEP_HEIGHT;
        // (next-last)*timeUnit + last
        Vector3d diff = new Vector3d(contactPointNext);
        diff.sub(contactPointLast);
        diff.scale(0.66);
        diff.add(contactPointLast);
        diff.z += lift;
        Vector3d downwards = new Vector3d(fallDir);
        downwards.scale(9.8 * tFall);  // move downwards over time
        diff.add(downwards);
        targetPosition.setPosition(diff);
    }

    private void interpolateWithLift(double timeUnit,double lift) {
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
     * @param dt time delta
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

    public void setPhase(CrabLegPhase phase) {
        this.phase = phase;
    }

    public CrabLegPhase getPhase() {
        return phase;
    }

    /**
     * is the leg touching the ground?
     * @param rayPickSystem the ray pick system to use for ground detection
     */
    public void checkLegTouchingGround(RayPickSystem rayPickSystem) {
        isTouchingGround = false;

        Ray ray = getRayDownFromToe();  // ray starts 1cm above the toe
        var list = rayPickSystem.findRayIntersections(ray,false);
        List<RayHit> list2 = new ArrayList<>();
        // find the first target that is not part of the crab robot.
        for(RayHit rayHit : list) {
            var target = rayHit.target();
            if (!crab.nodeIsPartOfMe(target)) list2.add(rayHit);
        }
        if(list2.isEmpty()) return;

        // At least one hit is not part of the crab.  They're already sorted by distance.
        var rayHit = list2.getFirst();
        //System.out.println(rayHit.distance());
        // check if the ray hit is close enough to the ground, accounting for the 1cm offset.
        isTouchingGround = rayHit.distance()<1.0;
        if (isTouchingGround) {
            // huzzah!
            contactPointLast.set(rayHit.point());  // update the last contact point
        }
        // it was not touching the ground.
    }

    // create a ray that begins 1cm above the toe and points straight down.
    private Ray getRayDownFromToe() {
        Point3d p = getToePosition();
        p.z+=1;
        return new Ray(p, new Vector3d(0, 0, -1), Crab.TOE_STEP_HEIGHT*3);  // ray down a short distance
    }

    public double getAnimationTime() {
        return animationTime;
    }

    void updateNextPointOfContact(double movingTurning,double movingForward,double movingRight) {
        if(getPhase() != CrabLegPhase.REST) return;

        double angleRad = Math.toRadians(movingTurning);
        var m = crab.getBody().getWorld();
        var rotZ = new Matrix4d();
        // adjust the next contact points for each leg based on the walk directions and body orientation.
        var xAxis = MatrixHelper.getXAxis(m);
        var yAxis = MatrixHelper.getYAxis(m);
        var bodyPos = MatrixHelper.getPosition(m);
        rotZ.rotZ(angleRad);

        Vector3d nextContact = new Vector3d(contactPointLast);
        nextContact.scaleAdd(movingRight,xAxis,nextContact);
        nextContact.scaleAdd(movingForward,yAxis,nextContact);

        // rotate the next contact point around the body position.
        nextContact.sub(bodyPos);  // make relative to body position
        rotZ.transform(nextContact);
        nextContact.add(bodyPos);  // put it back in world space

        // lower the next contact point a bit so it doesn't float in the air.
        // and so the toe hits the floor before the animation cycle ends.
        nextContact.z -= 0.5;

        // apply
        contactPointNext.set(nextContact);
    }
}
