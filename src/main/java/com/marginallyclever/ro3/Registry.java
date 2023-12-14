package com.marginallyclever.ro3;

import com.marginallyclever.ro3.listwithevents.ListWithEvents;
import com.marginallyclever.ro3.nodes.*;
import com.marginallyclever.ro3.render.OpenGLPanel;

import java.util.ArrayList;
import java.util.List;

public class Registry {
    public static final Factory<Node> nodeFactory = new Factory<>();
    public static final Factory<DockingPanel> panelFactory = new Factory<>();

    public static Node scene = new Node("Scene");
    public static ListWithEvents<Camera> cameras = new ListWithEvents<>();

    public static void start() {
        Factory.Category<Node> nodule = new Factory.Category<>("Node", null);
        nodeFactory.getRoot().add(nodule);
        Factory.Category<Node> pose = new Factory.Category<>("Pose", Pose::new);
        pose.add(new Factory.Category<>("MeshInstance", MeshInstance::new ));
        pose.add(new Factory.Category<>("Camera", Camera::new ));
        pose.add(new Factory.Category<>("Light", Light::new ));
        nodule.add(pose);

        panelFactory.getRoot().add(new Factory.Category<>("Panel A", () -> new DockingPanel("Panel A") ));
        panelFactory.getRoot().add(new Factory.Category<>("3D view", () -> new OpenGLPanel("3D view") ));

        cameras.add(new Camera("Camera 1"));
    }
}
