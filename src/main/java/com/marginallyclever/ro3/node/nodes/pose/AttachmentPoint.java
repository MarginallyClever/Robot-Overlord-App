package com.marginallyclever.ro3.node.nodes.pose;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.Pose;
import org.json.JSONObject;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import java.util.List;

/**
 * </p>{@link AttachmentPoint} is a point on a {@link Pose} that can be used to attach other nodes.</p>
 * <p>It provides a way to attach and release Pose nodes.  Poses that attach become children of the
 * {@link AttachmentPoint}.  When the {@link AttachmentPoint} is released, the attached nodes
 * become children of the scene root.</p>
 */
public class AttachmentPoint extends Pose {
    private boolean isAttached = false;

    public AttachmentPoint() {
        super("Attachment Point");
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

    @Override
    public JSONObject toJSON() {
        var json = super.toJSON();
        json.put("isAttached",isAttached);
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        isAttached = from.getBoolean("isAttached");
    }

    @Override
    public void getComponents(List<JPanel> list) {
        super.getComponents(list);
    }
}
