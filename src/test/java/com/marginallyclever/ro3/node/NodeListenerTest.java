package com.marginallyclever.ro3.node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

public class NodeListenerTest {
    private Node parentNode;
    private Node childNode;
    private NodeAttachListener attachListener;
    private NodeDetachListener detachListener;
    private NodeReadyListener readyListener;
    private NodeRenameListener renameListener;

    @BeforeEach
    public void setup() {
        parentNode = new Node("Parent");
        childNode = new Node("Child");
        attachListener = Mockito.mock(NodeAttachListener.class);
        detachListener = Mockito.mock(NodeDetachListener.class);
        readyListener = Mockito.mock(NodeReadyListener.class);
        renameListener = Mockito.mock(NodeRenameListener.class);

        parentNode.addAttachListener(attachListener);
        parentNode.addDetachListener(detachListener);
        parentNode.addReadyListener(readyListener);
        parentNode.addRenameListener(renameListener);
    }

    @Test
    public void testNodeAttachListener() {
        parentNode.addChild(childNode);
        verify(attachListener, times(1)).nodeAttached(childNode);
    }

    @Test
    public void testNodeDetachListener() {
        parentNode.addChild(childNode);
        parentNode.removeChild(childNode);
        verify(detachListener, times(1)).nodeDetached(childNode);
        parentNode.removeDetachListener(detachListener);
    }

    @Test
    public void testNodeReadyListener() {
        parentNode.addChild(childNode);
        verify(readyListener, times(1)).nodeReady(childNode);
        parentNode.removeReadyListener(readyListener);
    }

    @Test
    public void testNodeRenameListener() {
        parentNode.setName("NewName");
        verify(renameListener, times(1)).nodeRenamed(parentNode);
        parentNode.removeRenameListener(renameListener);
    }
}