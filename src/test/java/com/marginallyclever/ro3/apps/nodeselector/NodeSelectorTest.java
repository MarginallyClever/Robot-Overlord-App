package com.marginallyclever.ro3.apps.nodeselector;

import com.marginallyclever.ro3.node.Node;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
public class NodeSelectorTest {
    @Test
    public void test() {
        Node node = new Node("test");
        NodeSelector<Node> ns = new NodeSelector<>(Node.class);
        ns.setSubject(node);
        assert(ns.getSubject() == node);

        ns.setEnabled(false);
        assert(!ns.isEnabled());
    }
}
