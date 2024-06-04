package com.marginallyclever.ro3.node.nodes.odenode;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.odenode.odebody.odebodies.ODEBox;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ODEHingeTest {
    @Test
    public void test() {
        Registry.start();
        Node before = Registry.getScene();
        ODEBox box1 = new ODEBox("b1");
        before.addChild(box1);
        ODEBox box2 = new ODEBox("b2");
        before.addChild(box2);
        ODEHinge hinge1 = new ODEHinge("h1");
        before.addChild(hinge1);
        ODEHinge hinge2 = new ODEHinge("h2");
        before.addChild(hinge2);

        Registry.getPhysics().update(0);
        // make sure everyone calls onFirstUpdate()
        before.update(0);

        hinge1.setPartB(box1);
        hinge2.setPartA(box1);
        hinge2.setPartB(box2);

        Assertions.assertNotNull(hinge1.getHinge().getBody(0));
        Assertions.assertNotNull(hinge2.getHinge().getBody(0));
        Assertions.assertNotNull(hinge2.getHinge().getBody(1));


        // make a deep copy to/from json
        JSONObject json = before.toJSON();
        Node after = Registry.nodeFactory.create(json.getString("type"));
        after.fromJSON(json);

        // confirm the hinges are still attached.
        ODEHinge afterHinge1 = (ODEHinge) after.findChild("h1");
        ODEHinge afterHinge2 = (ODEHinge) after.findChild("h2");

        // make sure everyone calls onFirstUpdate()
        after.update(0);

        Assertions.assertNotNull(afterHinge1.getHinge().getBody(0));
        Assertions.assertNotNull(afterHinge2.getHinge().getBody(0));
        Assertions.assertNotNull(afterHinge2.getHinge().getBody(1));
    }

    // test that serializing with infinite limits works, and with non-infinite limits works.
    @Test
    public void testLimits() {
        Registry.start();

        ODEHinge before = new ODEHinge();
        before.setAngleMax(Double.POSITIVE_INFINITY);
        before.setAngleMin(Double.NEGATIVE_INFINITY);
        JSONObject json = before.toJSON();
        ODEHinge after = new ODEHinge();
        after.fromJSON(json);
        Assertions.assertEquals(Double.POSITIVE_INFINITY,after.getAngleMax());
        Assertions.assertEquals(Double.NEGATIVE_INFINITY,after.getAngleMin());

        before.setAngleMax(90);
        before.setAngleMin(-90);
        json = before.toJSON();
        after = new ODEHinge();
        after.fromJSON(json);
        Assertions.assertEquals(90,after.getAngleMax());
        Assertions.assertEquals(-90,after.getAngleMin());
    }
}
