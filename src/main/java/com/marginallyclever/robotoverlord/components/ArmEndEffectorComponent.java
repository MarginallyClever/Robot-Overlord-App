package com.marginallyclever.robotoverlord.components;

import javax.vecmath.Matrix4d;

/**
 * An end effector is the tool at the end of a robot arm.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
@ComponentDependency(components = {PoseComponent.class})
public class ArmEndEffectorComponent extends Component {
    public Matrix4d getToolCenterPoint() {
        PoseComponent p = getEntity().getComponent(PoseComponent.class);
        if(p==null) return null;
        return p.getWorld();
    }

    public void setToolCenterPoint(Matrix4d mat) {
        PoseComponent p = getEntity().getComponent(PoseComponent.class);
        if(p==null) return;
        p.setWorld(mat);
    }
}
