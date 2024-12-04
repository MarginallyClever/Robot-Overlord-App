package com.marginallyclever.ro3.node.nodes.pose;

import java.util.EventListener;

public interface PoseChangeListener extends EventListener {
    // the parent pose has changed.
    void onPoseChange(Pose pose);
}
