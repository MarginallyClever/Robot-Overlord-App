package com.marginallyclever.ro3.node;

import com.marginallyclever.ro3.node.nodes.Pose;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class NodeTest {
    private Node parentNode;
    private Node childNode;

    @BeforeEach
    public void setup() {
        parentNode = new Node("Parent");
        childNode = new Node("Child");
    }

    @Test
    public void testAddChild() {
        parentNode.addChild(childNode);
        assertTrue(parentNode.getChildren().contains(childNode));
        assertEquals(parentNode, childNode.getParent());
    }

    @Test
    public void testRemoveChild() {
        parentNode.addChild(childNode);
        parentNode.removeChild(childNode);
        assertFalse(parentNode.getChildren().contains(childNode));
        assertNull(childNode.getParent());
    }

    @Test
    public void testGetName() {
        assertEquals("Parent", parentNode.getName());
    }

    @Test
    public void testGetParent() {
        parentNode.addChild(childNode);
        assertEquals(parentNode, childNode.getParent());
    }

    @Test
    public void testGetNodeID() {
        UUID id = parentNode.getNodeID();
        assertNotNull(id);
    }

    @Test
    public void testSetName() {
        parentNode.setName("NewName");
        assertEquals("NewName", parentNode.getName());
    }

    @Test
    public void testGetChildren() {
        parentNode.addChild(childNode);
        assertEquals(1, parentNode.getChildren().size());
        assertEquals(childNode, parentNode.getChildren().get(0));
    }

    @Test
    public void testFindParent() {
        Node grandParentNode = new Node("GrandParent");
        grandParentNode.addChild(parentNode);
        parentNode.addChild(childNode);
        assertEquals(grandParentNode, childNode.findParent("GrandParent"));
    }

    @Test
    public void testFindChild() {
        parentNode.addChild(childNode);
        assertEquals(childNode, parentNode.findChild("Child"));
    }

    @Test
    public void testGet() {
        parentNode.addChild(childNode);
        assertEquals(childNode, parentNode.get("Child"));
    }

    @Test
    public void testGetRootNode() {
        Node grandParentNode = new Node("GrandParent");
        grandParentNode.addChild(parentNode);
        parentNode.addChild(childNode);
        assertEquals(grandParentNode, childNode.getRootNode());
    }

    @Test
    public void testGetAbsolutePath() {
        Node grandParentNode = new Node("GrandParent");
        grandParentNode.addChild(parentNode);
        parentNode.addChild(childNode);
        assertEquals("/GrandParent/Parent/Child", childNode.getAbsolutePath());
    }

    @Test
    public void testIsNameUsedBySibling() {
        parentNode.addChild(childNode);
        Node siblingNode = new Node("Child");
        parentNode.addChild(siblingNode);
        assertTrue(siblingNode.isNameUsedBySibling("Child"));
    }

    @Test
    public void testFindFirstChild() {
        parentNode.addChild(childNode);
        assertEquals(childNode, parentNode.findFirstChild(Node.class));
    }

    @Test
    public void testFindFirstSibling() {
        Pose siblingNode = new Pose("Sibling");
        parentNode.addChild(childNode);
        parentNode.addChild(siblingNode);
        assertEquals(siblingNode, childNode.findFirstSibling(Pose.class));
    }

    @Test
    public void testHasParent() {
        parentNode.addChild(childNode);
        assertTrue(childNode.hasParent(parentNode));
    }

    @Test
    public void testFindNodeByID() {
        parentNode.addChild(childNode);
        String id = childNode.getNodeID().toString();
        assertEquals(childNode, parentNode.findNodeByID(id, Node.class));
    }

    @Test
    public void testFindNodeByPath() {
        parentNode.addChild(childNode);
        assertEquals(childNode, parentNode.findNodeByPath("Child", Node.class));
    }
}