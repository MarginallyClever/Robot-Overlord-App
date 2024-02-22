package com.marginallyclever.ro3.node.nodes.dogsolver;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.limbsolver.LimbSolver;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.Limb;
import org.json.JSONObject;

import javax.swing.*;
import javax.vecmath.Vector3d;
import java.util.List;
import java.util.Objects;

/**
 * A DogSolver is a Node that controls the movement of a dog by coordinating the movement of the legs.
 * It has four legs, each of which is a LimbSolver.  Each LimbSolver is responsible for moving a Limb and has a target.
 * By adjusting the target of each LimbSolver, the DogSolver can control the movement of the dog.
 */
public class DogSolver extends Node {
    private final NodePath<Pose> torso = new NodePath<>(this, Pose.class);

    public final static int NUM_LEGS = 4;
    private final LimbState[] legs = new LimbState[NUM_LEGS];
    private GaitStyle gaitStyle = GaitStyle.NONE;
    private double strideHeight = 2.0;  // cm
    private double forwardSpeed = 5.0;  // cm/s
    private double strafeSpeed = 2.5;  // cm/s
    private double turnSpeed = 15;  // degrees/s
    private double torsoStandingHeight = 5.0;  // cm
    private double torsoLyingHeight = 1.5;  // cm


    public DogSolver() {
        this("DogSolver");
    }

    public DogSolver(String name) {
        super(name);
        legs[0] = new LimbState(this,"Front right");
        legs[1] = new LimbState(this,"Front left");
        legs[2] = new LimbState(this,"Back left");
        legs[3] = new LimbState(this,"Back right");
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new DogSolverPanel(this));
        super.getComponents(list);
    }

    public NodePath<Pose> getTorso() {
        return torso;
    }
    public LimbState getLeg(int i) {
        return legs[i];
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("torso")) torso.setUniqueID(from.getString("torso"));
        if(from.has("legFR")) legs[0].limb.setUniqueID(from.getString("legFR"));
        if(from.has("legFL")) legs[1].limb.setUniqueID(from.getString("legFL"));
        if(from.has("legBL")) legs[2].limb.setUniqueID(from.getString("legBL"));
        if(from.has("legBR")) legs[3].limb.setUniqueID(from.getString("legBR"));
        if(from.has("gaitStyle")) gaitStyle = GaitStyle.valueOf(from.getString("gaitStyle"));
        if(from.has("strideHeight")) strideHeight = from.getDouble("strideHeight");
        if(from.has("forwardSpeed")) forwardSpeed = from.getDouble("forwardSpeed");
        if(from.has("strafeSpeed")) strafeSpeed = from.getDouble("strafeSpeed");
        if(from.has("turnSpeed")) turnSpeed = from.getDouble("turnSpeed");
        if(from.has("torsoStandingHeight")) torsoStandingHeight = from.getDouble("torsoStandingHeight");
        if(from.has("torsoLyingHeight")) torsoLyingHeight = from.getDouble("torsoLyingHeight");
    }

    @Override
    public JSONObject toJSON() {
        var json = super.toJSON();

        json.put("torso",torso.getUniqueID());
        json.put("legFR",legs[0].limb.getUniqueID());
        json.put("legFL",legs[1].limb.getUniqueID());
        json.put("legBL",legs[2].limb.getUniqueID());
        json.put("legBR",legs[3].limb.getUniqueID());
        json.put("gaitStyle",gaitStyle.toString());
        json.put("strideHeight",strideHeight);
        json.put("forwardSpeed",forwardSpeed);
        json.put("strafeSpeed",strafeSpeed);
        json.put("turnSpeed",turnSpeed);
        json.put("torsoStandingHeight",torsoStandingHeight);
        json.put("torsoLyingHeight", torsoLyingHeight);

        return json;
    }

    public GaitStyle getGaitStyle() {
        return gaitStyle;
    }

    public void setGaitStyle(GaitStyle gaitStyle) {
        this.gaitStyle = gaitStyle;
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        if(!isReadyToSolve()) return;

        switch(gaitStyle) {
            case STAND -> updateStand(dt);
            case LIE_DOWN -> updateLieDown(dt);
            case SIT -> updateSit(dt);
            case WALK -> updateWalk(dt);
            case TROT -> {} // TODO
            case PACE -> {} // TODO
            case BOUND -> {} // TODO
            case GALLOP -> {} // TODO
            case AMBLING -> {} // TODO
            default -> {}
        }
    }

    private boolean isReadyToSolve() {
        // refuse to update if the legs are not connected
        if(torso.getSubject()==null) return false;

        if(legs[0].limb.getSubject()==null || !legs[0].limb.getSubject().isReadyToSolve()) return false;
        if(legs[1].limb.getSubject()==null || !legs[1].limb.getSubject().isReadyToSolve()) return false;
        if(legs[2].limb.getSubject()==null || !legs[2].limb.getSubject().isReadyToSolve()) return false;
        if(legs[3].limb.getSubject()==null || !legs[3].limb.getSubject().isReadyToSolve()) return false;

        return true;
    }

    private void putAllFeetDown() {
        putOneFootDown(legs[0].limb.getSubject());
        putOneFootDown(legs[1].limb.getSubject());
        putOneFootDown(legs[2].limb.getSubject());
        putOneFootDown(legs[3].limb.getSubject());
    }

    private void putOneFootDown(LimbSolver leg) {
        var target = leg.getTarget().getSubject();
        // no target?  don't move.
        if(target==null) return;
        var world = target.getWorld();
        var translation = new Vector3d();
        world.get(translation);
        translation.z=0;
        world.setTranslation(translation);
        target.setWorld(world);
    }

    private boolean allFeetAreOnFloor() {
        return isLegOnFloor(legs[0].limb.getSubject()) &&
               isLegOnFloor(legs[1].limb.getSubject()) &&
               isLegOnFloor(legs[2].limb.getSubject()) &&
               isLegOnFloor(legs[3].limb.getSubject());
    }

    private boolean isLegOnFloor(LimbSolver leg) {
        Limb limb = leg.getLimb().getSubject();
        if(limb==null) return false;
        Pose endEffector = limb.getEndEffector().getSubject();
        if(endEffector==null) return false;
        var world = endEffector.getWorld();
        var translation = new Vector3d();
        world.get(translation);
        return Math.abs(translation.z) < 1e-1;
    }

    private void updateStand(double dt) {
        putAllFeetDown();
        //if(allFeetAreOnFloor())
        {
            raiseTorso(torsoStandingHeight,dt);
        }
    }

    private void raiseTorso(double goalHeight,double dt) {
        var torso = this.torso.getSubject();
        if(torso==null) return;
        var world = torso.getWorld();
        var translation = new Vector3d();
        world.get(translation);

        if(translation.z+dt < goalHeight) {
            translation.z += dt;
        } else if(translation.z-dt > goalHeight) {
            translation.z -= dt;
        } else {
            translation.z = goalHeight;
        }

        world.setTranslation(translation);
        torso.setWorld(world);
    }

    private void updateLieDown(double dt) {
        putAllFeetDown();
        //if(allFeetAreOnFloor())
        {
            raiseTorso(torsoLyingHeight,dt);
        }
    }

    private void updateSit(double dt) {
        putAllFeetDown();
        //if(allFeetAreOnFloor())
        {
            // tilt the body back so the front legs are straight and the back legs are bent
        }
    }

    private void updateWalk(double dt) {

        // move a leg target.
        moveLegTarget(0);
        moveLegTarget(1);
        moveLegTarget(2);
        moveLegTarget(3);

        moveTorso(dt);
    }

    private void moveLegTarget(int legIndex) {
        var leg = legs[legIndex];
        switch (leg.action) {
            case NONE -> {
                updateLastFloorContact(leg);
                leg.action = LegAction.RISING;
            }
            case RISING -> {
                var destination = getLegDestination(leg, new Vector3d(forwardSpeed / 2, strafeSpeed / 2, strideHeight));
                setLegTarget(leg, destination);
                if (legAtDestination(leg.limb.getSubject(), destination)) {
                    leg.action = LegAction.FALLING;
                }
            }
            case FALLING -> {
                var destination = getLegDestination(leg, new Vector3d(forwardSpeed, strafeSpeed, 0));
                setLegTarget(leg, destination);
                if (legAtDestination(leg.limb.getSubject(), destination)) {
                    leg.action = LegAction.RISING;
                }
            }
        }
    }

    private void moveTorso(double dt) {
        var torso = this.torso.getSubject();
        var tw = torso.getWorld();
        var forward = MatrixHelper.getZAxis(tw);
        var strafe = MatrixHelper.getYAxis(tw);
        var destination = MatrixHelper.getPosition(tw);
        // add body forward vector * forwardSpeed
        forward.scale(forwardSpeed/4);
        destination.add(forward);
        // add body strafe vector * strafeSpeed
        strafe.scale(strafeSpeed/4);
        destination.add(strafe);

        tw.setTranslation(destination);
        torso.setWorld(tw);
    }

    private void updateLastFloorContact(LimbState leg) {
        var limb = leg.limb.getSubject();
        var w = limb.getLimb().getSubject().getWorld();
        var p = MatrixHelper.getPosition(w);
        p.z = 0;
        leg.lastFloorContact.set(p);
    }

    /**
     * @param limb the leg to check
     * @param destination the point to compare against
     * @return true if the leg is at the destination
     */
    private boolean legAtDestination(LimbSolver limb, Vector3d destination) {
        var ew = limb.getLimb().getSubject().getEndEffector().getSubject().getWorld();
        var ep = MatrixHelper.getPosition(ew);
        ep.sub(destination);
        return ep.lengthSquared()<1e-1;
    }

    /**
     * Set the target of a leg to a new position.
     * @param leg the leg to move
     * @param destination the new position
     */
    private void setLegTarget(LimbState leg,Vector3d destination) {
        var limb = leg.limb.getSubject();
        var w = limb.getTarget().getSubject();
        var p = w.getWorld();
        p.setTranslation(destination);
        w.setWorld(p);
    }

    /**
     * Get the destination of a leg based on the direction the dog is moving.
     * @param leg the leg to move
     * @param direction the direction the dog is moving relative to the torso.
     * @return the destination of the leg
     */
    public Vector3d getLegDestination(LimbState leg,Vector3d direction) {
        var tw = torso.getSubject().getWorld();
        var forward = MatrixHelper.getZAxis(tw);
        var strafe = MatrixHelper.getYAxis(tw);
        var destination = leg.lastFloorContact;
        // add stride height
        destination.z = direction.z;
        // add body forward vector * forwardSpeed
        forward.scale(forwardSpeed);
        destination.add(forward);
        // add body strafe vector * strafeSpeed
        strafe.scale(strafeSpeed);
        destination.add(strafe);
        // done
        return destination;
    }

    public double getStrideHeight() {
        return strideHeight;
    }

    public void setStrideHeight(double strideHeight) {
        this.strideHeight = strideHeight;
    }

    public double getForwardSpeed() {
        return forwardSpeed;
    }

    public void setForwardSpeed(double forwardSpeed) {
        this.forwardSpeed = forwardSpeed;
    }

    public double getStrafeSpeed() {
        return strafeSpeed;
    }

    public void setStrafeSpeed(double strafeSpeed) {
        this.strafeSpeed = strafeSpeed;
    }

    public double getTurnSpeed() {
        return turnSpeed;
    }

    public void setTurnSpeed(double turnSpeed) {
        this.turnSpeed = turnSpeed;
    }

    public double getTorsoStandingHeight() {
        return torsoStandingHeight;
    }

    public void setTorsoStandingHeight(double torsoStandingHeight) {
        this.torsoStandingHeight = torsoStandingHeight;
    }

    public double getTorsoLyingHeight() {
        return torsoLyingHeight;
    }

    public void setTorsoLyingHeight(double torsoLyingHeight) {
        this.torsoLyingHeight = torsoLyingHeight;
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-dog-16.png")));
    }
}
