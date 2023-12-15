package com.marginallyclever.ro3;

import com.marginallyclever.ro3.listwithevents.ListWithEvents;
import com.marginallyclever.ro3.node.nodes.Camera;
import com.marginallyclever.ro3.node.nodes.MeshInstance;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.Pose;
import com.marginallyclever.ro3.render.OpenGLPanel;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;
import com.marginallyclever.robotoverlord.systems.render.mesh.load.MeshLoader;

public class Registry {
    public static final Factory<Node> nodeFactory = new Factory<>(Node.class);
    public static final Factory<DockingPanel> panelFactory = new Factory<>(DockingPanel.class);
    public static final Factory<MeshLoader> meshLoaderFactory = new Factory<>(MeshLoader.class);

    public static Node scene = new Node("Scene");
    public static ListWithEvents<Camera> cameras = new ListWithEvents<>();
    public static ListWithEvents<Mesh> meshes = new ListWithEvents<>();

    public static void start() {
        Factory.Category<Node> nodule = new Factory.Category<>("Node", null);
        nodeFactory.getRoot().add(nodule);
        Factory.Category<Node> pose = new Factory.Category<>("Pose", Pose::new);
        pose.add(new Factory.Category<>("MeshInstance", MeshInstance::new ));
        pose.add(new Factory.Category<>("Camera", Camera::new ));
        nodule.add(pose);

        panelFactory.getRoot().add(new Factory.Category<>("Panel A", () -> new DockingPanel("Panel A") ));
        panelFactory.getRoot().add(new Factory.Category<>("3D view", () -> new OpenGLPanel("3D view") ));

        cameras.add(new Camera("Camera 1"));
    }
}
