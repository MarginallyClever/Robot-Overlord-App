package com.marginallyclever.ro3.apps.commands;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodefactory.NodeFactory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class MoveNodeTest {
    /**
     * Create a tree of nodes.
     * Grab some non-contiguous nodes.  Move them to a new parent.  Check they are in the right place.
     * Undo the move.  Check they are back in the original place.
     */
    @Test
    public void testMoveNode() {
        Registry.start();

        Node[] list = new Node[10];
        for(int i=0;i<list.length;++i) {
            list[i] = Registry.nodeFactory.create("Pose");
            list[i].setName("Node "+i);
            Registry.getScene().addChild(list[i]);
        }

        List<Node> selection = new ArrayList<>();
        for(int i=0;i<4;++i) {
            selection.add(list[2 + i * 2]);
        }

        MoveNode move = new MoveNode(selection,list[0],0);
        //list[0].getChildren().forEach(System.out::println);

        for(int i=0;i<list.length/2-2;++i) {
            assert(list[2 + i * 2].getParent()==list[0]);
        }

        move.undo();

        assert(list[0].getChildren().isEmpty());

        //list[0].getChildren().forEach(System.out::println);
        var compare = Registry.getScene().getChildren();
        for(int i=0;i<list.length;++i) {
            assert(list[i].getParent()==Registry.getScene());
            assert(compare.indexOf(list[i])==i);
        }
    }
}
