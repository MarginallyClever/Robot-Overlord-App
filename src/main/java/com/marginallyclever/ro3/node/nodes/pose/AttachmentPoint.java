package com.marginallyclever.ro3.node.nodes.pose;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.Pose;
import org.json.JSONObject;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
            // don't grab yourself
            if(p.hasParent(this) || this.hasParent(p)) continue;

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
        for(Node n : Registry.getScene().getChildren()) {
            if(!(n instanceof Pose p)) continue;
            // don't grab yourself
            if(p.hasParent(this) || this.hasParent(p)) continue;

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

        // radius
        var formatter = NumberFormatHelper.getNumberFormatter();
        formatter.setMinimum(0.0);  // no negative radius
        var radiusField = new JFormattedTextField(formatter);
        radiusField.setValue(radius);
        radiusField.addPropertyChangeListener("value", e -> {
            radius = ((Number)radiusField.getValue()).doubleValue();
        });
        addLabelAndComponent(pane,"Radius",radiusField);

        var attached = buildAttachToggle();
        addLabelAndComponent(pane,"Action",attached);

        super.getComponents(list);
    }

    /**
     * Build the "attach/detach" toggle button.  Public so that it can be included in the control panel of other
     * nodes like MarlinRobotArm.
     * @return a new JToggleButton
     */
    public JComponent buildAttachToggle() {
        // attach/detach toggle
        var attached = new JToggleButton();
        attached.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource(
                "/com/marginallyclever/ro3/apps/actions/icons8-disconnect-16.png"))));
        attached.setSelected(isAttached);  // must come before action listener.
        setAttachedText(attached);

        attached.addActionListener(e -> {
            isAttached = attached.isSelected();
            if(isAttached) attemptAttach();
            else release();
            setAttachedText(attached);
        });
        return attached;
    }

    private void setAttachedText(JToggleButton attached) {
        attached.setText(isAttached ? "Release" : "Attach");
    }
}
