package com.marginallyclever.ro3.apps.nodeselector;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import org.junit.jupiter.api.Test;

public class NodeSelectionDialogTest {
    @Test
    public void test() {
        var pose = new Pose("test");
        Registry.start();
        Registry.getScene().addChild(pose);
        NodeSelectionDialog<Node> dialog = new NodeSelectionDialog<>();
        assert(dialog.getSelectedNode()==Registry.getScene());
        dialog.setSubject(null);
        assert(dialog.getSelectedNode()==null);
        dialog.setSubject(pose);
        assert(dialog.getSelectedNode()==pose);
    }
}
