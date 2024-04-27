package com.marginallyclever.ro3.node.nodes.ode4j;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.ode4j.odebody.ODEBox;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ode4j.ode.OdeHelper;

public class ODEHingeTest {
    @Test
    public void test() {
        Registry.start();
        Node scene = Registry.getScene();
        ODEBox box1 = new ODEBox("b1");
        scene.addChild(box1);
        ODEBox box2 = new ODEBox("b2");
        scene.addChild(box2);
        ODEHinge hinge1 = new ODEHinge("h1");
        scene.addChild(hinge1);
        ODEHinge hinge2 = new ODEHinge("h2");
        scene.addChild(hinge2);

        hinge1.setPartB(box1);
        hinge2.setPartA(box1);
        hinge2.setPartB(box2);

        assertOneWorldSpaceInScene(scene);

        // make a deep copy to/from json and confirm the links are still attached.
        JSONObject json = scene.toJSON();
        Node after = Registry.nodeFactory.create(json.getString("type"));
        after.fromJSON(json);

        assertOneWorldSpaceInScene(scene);
//        var physics = ODE4JHelper.guaranteePhysicsWorld();
    }

    private void assertOneWorldSpaceInScene(Node scene) {
        // count the instances of ODEWorldSpace in scene.
        int count = 0;
        for(Node n : scene.getChildren()) {
            if(n instanceof ODEWorldSpace) {
                count++;
            }
        }
        Assertions.assertEquals(1,count);
    }
}
