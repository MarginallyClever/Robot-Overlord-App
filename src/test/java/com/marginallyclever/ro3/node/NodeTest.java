package com.marginallyclever.ro3.node;

import com.marginallyclever.ro3.node.nodes.Camera;
import com.marginallyclever.ro3.node.nodes.Pose;
import org.json.JSONObject;
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
    public void testAddChildOutOfBounds() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            parentNode.addChild(parentNode.getChildren().size()+1,childNode);
        });
        assertThrows(IndexOutOfBoundsException.class, () -> {
            parentNode.addChild(-1,childNode);
        });
    }

    @Test
    public void failToFindMissingParent() {
        assertNull(childNode.findParent("MissingParent"));
    }

    @Test
    public void failToSetNameToMatchingSibling() {
        parentNode.addChild(childNode);
        Node siblingNode = new Node("Sibling");
        parentNode.addChild(siblingNode);
        assertThrows(IllegalArgumentException.class, () -> {
            siblingNode.setName("Child");
        });
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
        UUID id = UUID.fromString(parentNode.getUniqueID());
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
        assertEquals(childNode, parentNode.findByPath("Child"));
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
        assertNull(childNode.findFirstChild(Node.class));
        assertNull(parentNode.findFirstChild(Pose.class));
    }

    @Test
    public void testFindFirstSibling() {
        Pose siblingNode = new Pose("Sibling");
        parentNode.addChild(childNode);
        parentNode.addChild(siblingNode);
        assertEquals(siblingNode, childNode.findFirstSibling(Pose.class));
        assertNull(childNode.findFirstSibling(Camera.class));
    }

    @Test
    public void testHasParent() {
        parentNode.addChild(childNode);
        assertTrue(childNode.hasParent(parentNode));
        assertFalse(childNode.hasParent(childNode));
        assertFalse(parentNode.hasParent(childNode));
    }

    @Test
    public void testFindNodeByID() {
        parentNode.addChild(childNode);
        String id = childNode.getUniqueID().toString();
        assertEquals(childNode, parentNode.findNodeByID(id, Node.class));
    }

    @Test
    public void testFindNodeByPath() {
        parentNode.addChild(childNode);
        assertEquals(childNode, parentNode.findNodeByPath("Child", Node.class));
        assertEquals(childNode, parentNode.findNodeByPath("./Child", Node.class));
        assertEquals(childNode, parentNode.findNodeByPath("/Child", Node.class));
        assertEquals(parentNode, childNode.findNodeByPath("..", Node.class));
        assertNull(parentNode.findNodeByPath("/does/not/exist", Node.class));
        assertNull(parentNode.findNodeByPath("Child", Camera.class));
        Node grandChild = new Node("grandChild");
        childNode.addChild(grandChild);
        assertEquals(parentNode, grandChild.findNodeByPath("../..", Node.class));
        assertNull(grandChild.findNodeByPath("../../..", Node.class));
        assertNull(grandChild.findNodeByPath("../..", Pose.class));
    }

    @Test
    public void testToAndFromJSON() {
        parentNode.addChild(childNode);
        JSONObject jsonObject = parentNode.toJSON();
        String json = jsonObject.toString();
        assertTrue(json.contains("\"name\":\"Parent\""));
        assertTrue(json.contains("\"name\":\"Child\""));

        Node newNode = new Node();
        newNode.fromJSON(jsonObject);
        assertEquals(parentNode.getUniqueID(), newNode.getUniqueID());
        assertEquals(parentNode.getName(), newNode.getName());
        assertEquals(parentNode.getChildren().size(), newNode.getChildren().size());

        newNode.witnessProtection();
        assertNotEquals(parentNode.getUniqueID(), newNode.getUniqueID());
    }
}