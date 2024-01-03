package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.apps.nodeselector.NodeSelector;
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
    private Pose target;

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

        NodeSelector<Pose> selector = new NodeSelector<>(Pose.class);
        selector.setSubject(target);
        selector.addPropertyChangeListener("subject", (evt) -> target = selector.getSubject() );
        addLabelAndComponent(pane,"Target",selector);

        super.getComponents(list);
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        if(target!=null) {
            Pose parent = findParent(Pose.class);
            Matrix4d parentWorld = parent==null ? MatrixHelper.createIdentityMatrix4() : findParent(Pose.class).getWorld();
            Matrix4d targetWorld = target.getWorld();
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
        if(target!=null) {
            json.put("target",target.getNodeID());
        }
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("target")) {
            target = this.getRootNode().findNodeByID(from.getString("target"),Pose.class);
        }
    }
}
