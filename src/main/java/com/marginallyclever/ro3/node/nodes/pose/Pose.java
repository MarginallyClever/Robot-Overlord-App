package com.marginallyclever.ro3.node.nodes.pose;

import com.marginallyclever.convenience.helpers.BigMatrixHelper;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.node.Node;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.util.List;
import java.util.Objects;

/**
 * <p>A {@link Pose} is a {@link Node} that has a position and rotation in space.</p>
 */
public class Pose extends Node {
    private final Matrix4d local = MatrixHelper.createIdentityMatrix4();
    private MatrixHelper.EulerSequence rotationIndex = MatrixHelper.EulerSequence.YXZ;
    private Pose parentPose;

    public Pose() {
        this("Pose");
    }

    public Pose(String name) {
        super(name);
    }

    public void addPoseChangeListener(PoseChangeListener listener) {
        listeners.add(PoseChangeListener.class, listener);
    }

    public void removePoseChangeListener(PoseChangeListener listener) {
        listeners.remove(PoseChangeListener.class, listener);
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        // if we have a parent that is a panel, register to receive pose update events.
        parentPose = findParent(Pose.class);
        if(parentPose!=null) {
            parentPose.addPoseChangeListener(this::onParentPoseChanged);
        }
        firePoseChange();
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        if(parentPose!=null) {
            parentPose.removePoseChangeListener(this::onParentPoseChanged);
        }
    }

    /**
     * Override this method to receive pose change events from the parent.
     * @param pose the parent pose that has changed.
     */
    protected void onParentPoseChanged(Pose pose) {
        firePoseChange();
    }

    /**
     * Build a Swing Component that represents this Node.
     * @param list the list to add components to.
     */
    public void getComponents(List<JPanel> list) {
        list.add(new PosePanel(this));
        super.getComponents(list);
    }

    public Matrix4d getLocal() {
        return new Matrix4d(local);
    }

    /**
     * Set the local transform of this pose.  This is the best method to override if you want to
     * capture changes to the local OR world transform.
     * @param m the new local transform.
     */
    public void setLocal(Matrix4d m) {
        if(local.equals(m)) return;
        local.set(m);
        firePoseChange();
    }

    private void firePoseChange() {
        for(var listener : listeners.getListeners(PoseChangeListener.class)) {
            listener.onPoseChange(this);
        }
    }

    /**
     * @return the world transform of this pose.
     */
    public Matrix4d getWorld() {
        // search up the tree to find the world transform.
        if(parentPose==null) {
            return getLocal();
        }
        Matrix4d result = parentPose.getWorld();
        result.mul(getLocal());
        return result;
    }

    /**
     * Set the world transform of this pose.  All cases call {@link #setLocal(Matrix4d)}.
     * @param m the new world transform.
     */
    public void setWorld(Matrix4d m) {
        // search up the tree to find the world transform.
        if(parentPose==null) {
            setLocal(m);
            return;
        }
        // Changing m could have unintended side effects, so use a temp variable.
        Matrix4d temp = parentPose.getWorld();
        temp.invert();
        temp.mul(m);
        setLocal(temp);
    }

    /**
     * @param orderOfRotation the order of rotation.
     * @return the local rotation of this pose using Euler angles in degrees.
     */
    public Vector3d getRotationEuler(MatrixHelper.EulerSequence orderOfRotation) {
        Vector3d r = MatrixHelper.matrixToEuler(local,orderOfRotation);
        r.scale(180.0/Math.PI);
        return r;
    }

    /**
     * Set the local rotation of this pose using Euler angles.
     *
     * @param r Euler angles in degrees.
     * @param orderOfRotation the order of rotation.
     */
    public void setRotationEuler(Vector3d r, MatrixHelper.EulerSequence orderOfRotation) {
        //System.out.println("setRotationEuler("+r+","+orderOfRotation+")");
        Vector3d p = getPosition();
        Vector3d rRad = new Vector3d(r);
        rRad.scale(Math.PI/180.0);

        var m4 = new Matrix4d();
        m4.set(MatrixHelper.eulerToMatrix(rRad, orderOfRotation));
        m4.setTranslation(p);
        setLocal(m4);
    }

    /**
     * @return the local position of this pose.
     */
    public Vector3d getPosition() {
        return new Vector3d(local.m03,local.m13,local.m23);
    }

    /**
     * Set the local position of this pose.
     * @param p the new position.
     */
    public void setPosition(Vector3d p) {
        var m4 = getLocal();
        MatrixHelper.setPosition(m4, p);
        setLocal(m4);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();

        double[] localArray = BigMatrixHelper.matrix4dToArray(local);
        json.put("local", new JSONArray(localArray));
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("local")) {
            JSONArray localArray = from.getJSONArray("local");
            double[] localData = new double[16];
            for (int i = 0; i < 16; i++) {
                localData[i] = localArray.getDouble(i);
            }
            local.set(localData);
        }
    }

    public MatrixHelper.EulerSequence getRotationIndex() {
        return rotationIndex;
    }

    public void setRotationIndex(MatrixHelper.EulerSequence rotationIndex) {
        this.rotationIndex = rotationIndex;
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/node/nodes/pose/icons8-xyz-16.png")));
    }
}
