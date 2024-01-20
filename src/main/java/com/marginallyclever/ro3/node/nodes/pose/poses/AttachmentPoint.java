package com.marginallyclever.ro3.node.nodes.pose.poses;

import com.marginallyclever.convenience.helpers.IntersectionHelper;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;

/**
 * </p>{@link AttachmentPoint} is a point on a {@link Pose} that can be used to attach other nodes.</p>
 * <p>Users can click the attach button in the control panel.  Developers can use the {@link #attemptAttach()} method.</p>
 * <p>Things in reach must be {@link Pose} items within {@link #radius} of {@link AttachmentPoint}.  They must also be
 * immediate children of the Scene root.</p>
 * <p>The attached item will move from the Scene root and become a child of {@link AttachmentPoint}.  On release all
 * children of {@link AttachmentPoint} will be moved back to the Scene root.  In both cases their relative pose
 * will be adjusted so they do not teleport.</p>
 */
public class AttachmentPoint extends Pose {
    private static final Logger logger = LoggerFactory.getLogger(AttachmentPoint.class);
    private boolean isAttached = false;
    private double radius = 1.0;

    public AttachmentPoint() {
        this("AttachmentPoint");
    }

    public AttachmentPoint(String name) {
        super(name);
    }

    /**
     * Attach a list of Pose nodes to this node and adjust their world transform to compensate.
     * @param list list of nodes to attach.
     */
    public void attach(List<Pose> list) {
        for(Pose p : list) {
            if(p.hasParent(this) || this.hasParent(p)) continue;  // don't grab yourself
            if(p.getChildren().isEmpty()) continue;  // don't grab empty nodes

            logger.debug("attach "+p.getAbsolutePath());
            Matrix4d world = p.getWorld();
            Node parent = p.getParent();
            parent.removeChild(p);
            this.addChild(p);
            p.setWorld(world);
        }
    }

    /**
     * Release all attached nodes.  Move them to the scene root and adjust their world transform to compensate.
     */
    public void release() {
        var list = new ArrayList<>(this.getChildren());
        logger.debug("release "+list.size()+" children");
        for(Node n : list) {
            if(!(n instanceof Pose p)) continue;

            logger.debug("release "+p.getAbsolutePath());
            Matrix4d world = p.getWorld();
            this.removeChild(p);
            Registry.getScene().addChild(p);
            p.setWorld(world);
        }
    }

    public void attemptAttach() {
        if(!isAttached) return;

        var center = MatrixHelper.getPosition(getWorld());
        double r2 = radius*radius;

        var found = new ArrayList<Pose>();
        var list = new ArrayList<>(Registry.getScene().getChildren());
        while(!list.isEmpty()) {
            var node = list.remove(0);
            list.addAll(node.getChildren());
            if(!(node instanceof Pose pose)) continue;  // grab only Poses
            // don't grab yourself
            if(pose == this || pose.hasParent(this) || this.hasParent(pose)) continue;

            if(canGrab(pose,center,r2)) {
                found.add(pose);
            }
        }

        attach(found);
    }

    /**
     * Check if a {@link Pose} is within reach and has a {@link MeshInstance}.
     * @param pose the pose to check.
     * @param center the center of the sphere.
     * @param r2 the sphere radius squared.
     * @return true if the pose is within reach.
     */
    private boolean canGrab(Pose pose, Vector3d center, double r2) {
        var meshInstance = pose.findFirstChild(MeshInstance.class);
        if (meshInstance == null) return false;
        var mesh = meshInstance.getMesh();
        if (mesh == null) return false;

        int version=2;
        if(version==1) {
            // version 1, radius test.
            var pos = MatrixHelper.getPosition(pose.getWorld());
            pos.sub(center);
            return pos.lengthSquared() < r2;
        } else {
            // version 2, bounding box to radius test.
            var boundingBox = mesh.getBoundingBox();
            // convert the center to meshInstance space
            var im = meshInstance.getWorld();
            im.invert();
            Point3d center2 = new Point3d(center);
            im.transform(center2);

            var max = boundingBox.getBoundsTop();
            var min = boundingBox.getBoundsBottom();
            if( IntersectionHelper.sphereBox(center2,r2,max,min) ) {
                // TODO version 3, radius to triangles test.
                logger.debug("canGrab " + pose.getAbsolutePath());
                return true;
            }
        }

        return false;
    }

    @Override
    public JSONObject toJSON() {
        var json = super.toJSON();
        json.put("isAttached",isAttached);
        json.put("radius",radius);
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        isAttached = from.getBoolean("isAttached");
        radius = from.getDouble("radius");
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new AttachmentPointPanel(this));
        super.getComponents(list);
    }
    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        if(radius<=0) throw new IllegalArgumentException("radius must be >0");
        this.radius = radius;
    }

    public boolean getIsAttached() {
        return isAttached;
    }

    public void setIsAttached(boolean isAttached) {
        this.isAttached = isAttached;
    }
}
