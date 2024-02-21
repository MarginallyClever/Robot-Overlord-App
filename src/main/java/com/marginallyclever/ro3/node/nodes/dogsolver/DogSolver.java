package com.marginallyclever.ro3.node.nodes.dogsolver;

import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.limbsolver.LimbSolver;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.Limb;
import org.json.JSONObject;

import javax.swing.*;
import javax.vecmath.Vector3d;
import java.util.List;

/**
 * A DogSolver is a Node that controls the movement of a dog by coordinating the movement of the legs.
 * It has four legs, each of which is a LimbSolver.  Each LimbSolver is responsible for moving a Limb and has a target.
 * By adjusting the target of each LimbSolver, the DogSolver can control the movement of the dog.
 */
public class DogSolver extends Node {
    private final NodePath<Pose> torso = new NodePath<>(this, Pose.class);
    private final NodePath<LimbSolver> legFR = new NodePath<>(this, LimbSolver.class);
    private final NodePath<LimbSolver> legFL = new NodePath<>(this, LimbSolver.class);
    private final NodePath<LimbSolver> legBL = new NodePath<>(this, LimbSolver.class);
    private final NodePath<LimbSolver> legBR = new NodePath<>(this, LimbSolver.class);
    private GaitStyle gaitStyle = GaitStyle.NONE;
    private double strideHeight = 2.0;  // cm
    private double forwardSpeed = 5.0;  // cm/s
    private double strafeSpeed = 2.5;  // cm/s
    private double turnSpeed = 15;  // degrees/s
    private double torsoStandingHeight = 5.0;  // cm
    private double torsoLyingHeight = 1.5;  // cm


    public DogSolver() {
        super("DogSolver");
    }

    public DogSolver(String name) {
        super(name);
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new DogSolverPanel(this));
        super.getComponents(list);
    }

    public NodePath<Pose> getTorso() {
        return torso;
    }

    public NodePath<LimbSolver> getLegFR() {
        return legFR;
    }

    public NodePath<LimbSolver> getLegFL() {
        return legFL;
    }

    public NodePath<LimbSolver> getLegBL() {
        return legBL;
    }

    public NodePath<LimbSolver> getLegBR() {
        return legBR;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("torso")) torso.setUniqueID(from.getString("torso"));
        if(from.has("legFR")) legFR.setUniqueID(from.getString("legFR"));
        if(from.has("legFL")) legFL.setUniqueID(from.getString("legFL"));
        if(from.has("legBL")) legBL.setUniqueID(from.getString("legBL"));
        if(from.has("legBR")) legBR.setUniqueID(from.getString("legBR"));
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
        json.put("legFR",legFR.getUniqueID());
        json.put("legFL",legFL.getUniqueID());
        json.put("legBL",legBL.getUniqueID());
        json.put("legBR",legBR.getUniqueID());
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
        // refuse to update if the legs are not connected
        if(legFR.getSubject()==null || legFL.getSubject()==null || legBL.getSubject()==null || legBR.getSubject()==null) return;

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

    private void putAllFeetDown() {
        putOneFootDown(legFR.getSubject());
        putOneFootDown(legFL.getSubject());
        putOneFootDown(legBL.getSubject());
        putOneFootDown(legBR.getSubject());
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
        return isLegOnFloor(legFR.getSubject()) &&
               isLegOnFloor(legFL.getSubject()) &&
               isLegOnFloor(legBL.getSubject()) &&
               isLegOnFloor(legBR.getSubject());
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
        // move the legs targets in arcs.

        // move the torso
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
}
