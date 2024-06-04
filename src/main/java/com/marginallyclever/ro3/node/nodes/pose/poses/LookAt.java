package com.marginallyclever.ro3.node.nodes.pose.poses;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import org.json.JSONObject;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import java.util.List;
import java.util.Objects;

/**
 * <p>{@link LookAt} is a pose that always faces a target.  The target is another {@link Pose}.
 * The target is not required to be a child of this pose.
 * The target can be anywhere in the scene graph.</p>
 *
 * <p>This class provides several functionalities:</p>
 * <ul>
 * <li>It can set and get the target {@link Pose}.</li>
 * <li>It can update its own location in space based on the target's location.</li>
 * <li>It can serialize and deserialize itself to and from JSON format.</li>
 * </ul>
 */
public class LookAt extends Pose {
    private final NodePath<Pose> target = new NodePath<>(this,Pose.class);

    public LookAt() {
        super("LookAt");
    }

    public LookAt(String name) {
        super(name);
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new LookAtPanel(this));
        super.getComponents(list);
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        Pose myTarget = target.getSubject();
        if(myTarget!=null) {
            Pose parent = findParent(Pose.class);
            Matrix4d fromWorld = parent==null ? MatrixHelper.createIdentityMatrix4() : findParent(Pose.class).getWorld();
            Matrix4d toWorld = myTarget.getWorld();

            Matrix4d look = new Matrix4d();
            look.set(MatrixHelper.lookAt(MatrixHelper.getPosition(fromWorld),MatrixHelper.getPosition(toWorld)));
            fromWorld.invert();
            look.mul(fromWorld,look);
            look.setTranslation(new javax.vecmath.Vector3d());
            this.setLocal(look);
        }
    }

    @Override
    public JSONObject toJSON() {
        var json = super.toJSON();
        json.put("version",2);
        if(target.getSubject()!=null) {
            json.put("target", target.getUniqueID());
        }
        return json;
    }
    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        int version = from.has("version") ? from.getInt("version") : 0;
        if (from.has("target")) {
            String s = from.getString("target");
            if(version==1) {
                target.setUniqueIDByNode( this.findNodeByPath(s,Pose.class) );
            } else if(version==0 || version==2) {
                target.setUniqueID(s);
            }
        }
    }

    public Pose getTarget() {
        return target.getSubject();
    }

    public void setTarget(Pose target) {
        this.target.setUniqueIDByNode(target);
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/node/nodes/pose/poses/icons8-look-16.png")));
    }
}
