package com.marginallyclever.robotoverlord.components;

import com.marginallyclever.robotoverlord.Component;

import javax.vecmath.Matrix4d;

public class ArmEndEffectorComponent extends Component {
    public Matrix4d getToolCenterPoint() {
        PoseComponent p = getEntity().findFirstComponent(PoseComponent.class);
        if(p==null) return null;
        return p.getWorld();
    }

    public void setToolCenterPoint(Matrix4d mat) {
        PoseComponent p = getEntity().findFirstComponent(PoseComponent.class);
        if(p==null) return;
        p.setWorld(mat);
    }
}
