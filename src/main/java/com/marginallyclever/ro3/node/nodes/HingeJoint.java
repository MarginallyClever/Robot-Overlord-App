package com.marginallyclever.ro3.node.nodes;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.mesh.Mesh;
import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshProvider;
import org.json.JSONObject;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.util.List;
import java.util.Objects;

/**
 * <p>a {@link HingeJoint} is a joint that can rotate around the local Z axis.</p>
 * <p>a {@link HingeJoint} should be attached to a child {@link Pose} referenced as the axle.  In this way the axle's
 * parent {@link Pose} can be thought of as the initial pose.  This helps prevent drift over time.</p>
 */
public class HingeJoint extends MechanicalJoint implements MeshProvider {
    private static final float RING_SCALE = 3.0f;

    private double angle = 0;  // degrees
    private double minAngle = 0;  // degrees
    private double maxAngle = 360;  // degrees
    private double velocity = 0;  // degrees/s
    private double acceleration = 0;  // degrees/s/s
    private final NodePath<Pose> axle = new NodePath<>(this,Pose.class);

    private final Mesh jointMesh = new Mesh();

    public HingeJoint() {
        this("HingeJoint");
    }

    public HingeJoint(String name) {
        super(name);
        setupJointMesh();
    }

    private void setupJointMesh() {
        jointMesh.setRenderStyle(GL3.GL_LINES);
        // vertex 0: origin (angle line start, always fixed)
        jointMesh.addVertex(0, 0, 0);
        // vertex 1: angle line tip (updated in getMesh)
        jointMesh.addVertex(RING_SCALE, 0, 0);
        // vertices 2..362: arc ring points, one per degree 0..360 (updated in getMesh)
        for (int i = 0; i <= 360; i++) {
            jointMesh.addVertex(RING_SCALE, 0, 0);
        }
        // angle indicator line
        jointMesh.addIndex(0);  jointMesh.addIndex(1);
        // arc segments: 360 consecutive pairs
        for (int i = 0; i < 360; i++) {
            jointMesh.addIndex(2 + i);
            jointMesh.addIndex(3 + i);
        }
    }

    // ---- MeshProvider ----

    @Override
    public Mesh getMesh() {
        // Update angle line tip
        double angleRad = Math.toRadians(angle);
        jointMesh.setVertex(1,
                RING_SCALE * Math.cos(angleRad),
                RING_SCALE * Math.sin(angleRad), 0);

        // Update arc: active range from minAngle to maxAngle, collapse rest
        int range = Math.max(0, (int)(maxAngle - minAngle));
        double endX = RING_SCALE * Math.cos(Math.toRadians(maxAngle));
        double endY = RING_SCALE * Math.sin(Math.toRadians(maxAngle));
        for (int i = 0; i <= 360; i++) {
            if (i <= range) {
                double rad = Math.toRadians(minAngle + i);
                jointMesh.setVertex(2 + i,
                        RING_SCALE * Math.cos(rad),
                        RING_SCALE * Math.sin(rad), 0);
            } else {
                // Collapse to the last active point — zero-length lines are invisible
                jointMesh.setVertex(2 + i, endX, endY, 0);
            }
        }
        jointMesh.setDirty(true);
        return jointMesh;
    }

    @Override
    public boolean isActive() {
        return Registry.selection.getList().contains(this)
            || Registry.pinned.getList().contains(this);
    }

    @Override
    public boolean getHasShadow() {
        return false;
    }

    @Override
    public Matrix4d getWorld() {
        Pose parentPose = findParent(Pose.class);
        return (parentPose == null) ? MatrixHelper.createIdentityMatrix4() : parentPose.getWorld();
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new HingeJointPanel(this));
        super.getComponents(list);
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        velocity += acceleration * dt;
        setAngle(angle + velocity * dt);
        updateAxleLocationInSpace();
    }

    /**
     * Set the axle's location in space based on the current angle.
     */
    public void updateAxleLocationInSpace() {
        if(axle.getSubject()==null) return;

        var subject = axle.getSubject();
        var m = subject.getLocal();
        Vector3d translation = new Vector3d();
        m.get(translation);

        m.rotZ(Math.toRadians(angle));

        m.setTranslation(translation);
        subject.setLocal(m);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();
        json.put("angle",angle);
        json.put("minAngle",minAngle);
        json.put("maxAngle",maxAngle);
        json.put("velocity",velocity);
        json.put("acceleration",acceleration);
        json.put("version",2);
        if(axle.getSubject()!=null) json.put("axle",axle.getUniqueID());

        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("angle")) angle = from.getDouble("angle");
        if(from.has("minAngle")) minAngle = from.getDouble("minAngle");
        if(from.has("maxAngle")) maxAngle = from.getDouble("maxAngle");
        if(from.has("velocity")) velocity = from.getDouble("velocity");
        if(from.has("acceleration")) acceleration = from.getDouble("acceleration");

        int version = from.has("version") ? from.getInt("version") : 0;
        if(from.has("axle")) {
            String s = from.getString("axle");
            if(version==1) {
                axle.setUniqueIDByNode(this.findNodeByPath(s,Pose.class));
            } else if(version==0 || version==2) {
                axle.setUniqueID(s);
            }
        }
    }

    public double getAngle() {
        return angle;
    }

    /**
     * Set the angle in degrees, clamped to minAngle and maxAngle if they are set.
     * @param degrees the angle in degrees
     */
    public void setAngle(double degrees) {
        angle = degrees;

        if(maxAngle!=360 && minAngle!=0) {
            if (angle > maxAngle) angle = maxAngle;
            if (angle < minAngle) angle = minAngle;
        }
    }

    /**
     * Set the angle in degrees without clamping to minAngle and maxAngle.
     * @param degrees the angle in degrees
     */
    public void setAngleUnsafe(double degrees) {
        angle = degrees;
    }

    public double getMinAngle() {
        return minAngle;
    }

    public void setMinAngle(double v) {
        minAngle = v;
    }

    public double getMaxAngle() {
        return maxAngle;
    }

    public void setMaxAngle(double v) {
        maxAngle = v;
    }

    public double getVelocity() {
        return velocity;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public double getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(double acceleration) {
        this.acceleration = acceleration;
    }

    public Pose getAxle() {
        return axle.getSubject();
    }

    public void setAxle(Pose subject) {
        axle.setUniqueIDByNode(subject);
        if (subject != null) {
            Matrix4d m = subject.getLocal();
            // Extract rotation around Z from the local matrix basis vectors.
            // m00 is cos(theta), m10 is sin(theta) for a Z-axis rotation.
            double rad = Math.atan2(m.m10, m.m00);
            setAngle(Math.toDegrees(rad));
        }
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/node/nodes/hinge-16.png")));
    }
}