package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.convenience.PathCalculator;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.apps.nodeselector.NodeSelector;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.NodePath;
import org.json.JSONObject;

import javax.swing.*;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import java.awt.*;
import java.util.List;

/**
 * <p>{@link LookAt} is a pose that always faces a target.
 * The target is another pose.
 * The target pose is not required to be a child of this pose.
 * The target pose can be anywhere in the scene graph.</p>
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
        JPanel pane = new JPanel(new GridLayout(0,2));
        list.add(pane);
        pane.setName(LookAt.class.getSimpleName());

        NodeSelector<Pose> selector = new NodeSelector<>(Pose.class,target.getSubject());
        selector.addPropertyChangeListener("subject", (evt) -> {
            target.setRelativePath(this,selector.getSubject());
        } );
        addLabelAndComponent(pane,"Target",selector);

        super.getComponents(list);
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        Pose myTarget = target.getSubject();
        if(myTarget!=null) {
            Pose parent = findParent(Pose.class);
            Matrix4d parentWorld = parent==null ? MatrixHelper.createIdentityMatrix4() : findParent(Pose.class).getWorld();
            Matrix4d targetWorld = myTarget.getWorld();
            Matrix3d lookAt = MatrixHelper.lookAt(MatrixHelper.getPosition(parentWorld),MatrixHelper.getPosition(targetWorld));
            Matrix4d look = new Matrix4d();
            look.set(lookAt);
            parentWorld.invert();
            look.mul(parentWorld,look);
            look.setTranslation(new javax.vecmath.Vector3d());
            getLocal().set(look);

        }
    }

    @Override
    public JSONObject toJSON() {
        var json = super.toJSON();
        json.put("version",1);
        if(target.getSubject()!=null) {
            json.put("target", target.getPath());
        }
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        int version = from.has("version") ? from.getInt("version") : 0;
        if (from.has("target")) {
            if(version == 1) {
                target.setPath(from.getString("target"));
            } else if(version == 0) {
                Pose pose = getRootNode().findNodeByID(from.getString("target"),Pose.class);
                target.setPath( PathCalculator.getRelativePath(this,pose) );
            }
        }
    }
}
