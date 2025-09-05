package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.ro3.mesh.proceduralmesh.Box;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MeshInstanceTest {
    @Test
    public void persistenceTest() {
        MeshInstance before = new MeshInstance();
        MeshInstance after = new MeshInstance();

        before.setMesh(new Box(10,5,2));
        before.setActive(false);

        after.fromJSON(before.toJSON());

        Assertions.assertEquals(before.isActive(), after.isActive());
        Assertions.assertEquals(before.getMesh().getClass(), after.getMesh().getClass());
        Assertions.assertEquals(before.getMesh().getSourceName(), after.getMesh().getSourceName());
        Assertions.assertEquals(before.getMesh().getNumVertices(), after.getMesh().getNumVertices());
    }
}
