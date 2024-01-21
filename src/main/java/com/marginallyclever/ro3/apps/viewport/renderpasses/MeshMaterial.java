package com.marginallyclever.ro3.apps.viewport.renderpasses;

import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;

public record MeshMaterial(MeshInstance meshInstance, Material material) {
}
