package com.marginallyclever.ro3.node.nodes.pose;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.Pose;
import org.json.JSONObject;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * </p>{@link AttachmentPoint} is a point on a {@link Pose} that can be used to attach other nodes.</p>
 * <p>It provides a way to attach and release Pose nodes.  Poses that attach become children of the
 * {@link AttachmentPoint}.  When the {@link AttachmentPoint} is released, the attached nodes
 * become children of the scene root.</p>
 */
public class AttachmentPoint extends Pose {
    private boolean isAttached = false;
    private double radius = 1.0;

    public AttachmentPoint() {
        super("AttachmentPoint");
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
            if(p==this || p.hasParent(this)) continue;

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
        List<Node> list = this.getChildren();
        for(Node n : list) {
            if(!(n instanceof Pose p)) continue;

            Matrix4d world = p.getWorld();
            this.removeChild(p);
            Registry.getScene().addChild(p);
            p.setWorld(world);
        }
    }

    public void attemptAttach() {
        if(!isAttached) return;

        var myPosition = MatrixHelper.getPosition(getWorld());
        double r2 = radius*radius;

        var found = new ArrayList<Pose>();

        List<Node> list = Registry.getScene().getChildren();
        for(Node n : list) {
            if(!(n instanceof Pose p)) continue;
            if(p==this) continue;
            if(p.hasParent(this)) continue;

            var pos = MatrixHelper.getPosition(p.getWorld());
            pos.sub(myPosition);
            if( pos.lengthSquared() <= r2 ) {
                found.add(p);
            }
        }

        attach(found);
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
        JPanel pane = new JPanel(new GridLayout(0,2));
        list.add(pane);
        pane.setName(AttachmentPoint.class.getSimpleName());

        var attached = new JCheckBox("",isAttached);
        addLabelAndComponent(pane,"isAttached",attached);
        attached.addActionListener(e -> {
            isAttached = attached.isSelected();
            if(isAttached) {
                attemptAttach();
            } else {
                release();
            }
        });
        super.getComponents(list);
    }
}
