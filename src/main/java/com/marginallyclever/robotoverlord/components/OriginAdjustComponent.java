package com.marginallyclever.robotoverlord.components;

import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;

import javax.vecmath.Matrix4d;

/**
 * Adjusts this Entity's pose to be relative to the parent's pose.
 */
@ComponentDependency(components={PoseComponent.class})
public class OriginAdjustComponent extends Component {
    @Override
    public void getView(ViewPanel view) {
        super.getView(view);
        view.addButton("Adjust").addActionEventListener( e -> adjust() );
    }

    public void adjust() {
        PoseComponent myPose = getEntity().findFirstComponent(PoseComponent.class);
        PoseComponent parentPose = getEntity().getParent().findFirstComponent(PoseComponent.class);
        if(myPose==null || parentPose==null) return;
        Matrix4d parentMat = parentPose.getWorld();
        Matrix4d mat = myPose.getWorld();
        parentMat.invert();
        myPose.setLocalMatrix4(parentMat);
    }
}
