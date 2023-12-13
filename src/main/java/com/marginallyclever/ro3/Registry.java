package com.marginallyclever.ro3;

import com.marginallyclever.ro3.nodes.MeshInstance;
import com.marginallyclever.ro3.nodes.Node;
import com.marginallyclever.ro3.nodes.Pose;

public class Registry {
    public static final Factory<Node> nodeFactory = new Factory<>();
    public static final Factory<DockingPanel> panelFactory = new Factory<>();

    public static Node root = new Node("root");

    public static void start() {
        nodeFactory.getRoot().add(new Factory.Category<>("Node", Node::new ));
        nodeFactory.getRoot().add(new Factory.Category<>("Pose", Pose::new ));
        nodeFactory.getRoot().add(new Factory.Category<>("MeshInstance", MeshInstance::new ));

        panelFactory.getRoot().add(new Factory.Category<>("Panel A", () -> new DockingPanel("Panel A") ));
        panelFactory.getRoot().add(new Factory.Category<>("3D view", () -> new OpenGLPanel("3D view") ));
    }
}
