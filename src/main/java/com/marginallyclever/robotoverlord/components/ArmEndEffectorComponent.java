package com.marginallyclever.robotoverlord.components;

import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.Entity;

import javax.vecmath.Matrix4d;

@ComponentDependency(components = {PoseComponent.class})
public class ArmEndEffectorComponent extends Component {

    @Override
    public void setEntity(Entity entity) {
        super.setEntity(entity);
        entity.addComponent(new PoseComponent());
    }

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
