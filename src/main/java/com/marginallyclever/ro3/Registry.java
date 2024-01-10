package com.marginallyclever.ro3;

import com.marginallyclever.ro3.listwithevents.ListWithEvents;
import com.marginallyclever.ro3.mesh.MeshFactory;
import com.marginallyclever.ro3.node.nodes.*;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.marlinrobotarm.MarlinRobotArm;
import com.marginallyclever.ro3.node.nodes.pose.AttachmentPoint;
import com.marginallyclever.ro3.node.nodes.pose.Limb;
import com.marginallyclever.ro3.node.nodes.pose.LookAt;
import com.marginallyclever.ro3.node.nodes.pose.MeshInstance;
import com.marginallyclever.ro3.texture.TextureFactory;

import javax.swing.event.EventListenerList;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link Registry} is a place to store global variables.
 */
public class Registry {

    public static final EventListenerList listeners = new EventListenerList();
    public static final TextureFactory textureFactory = new TextureFactory();
    public static final MeshFactory meshFactory = new MeshFactory();
    public static final Factory<Node> nodeFactory = new Factory<>(Node.class);
    private static Node scene = new Node("Scene");
    public static final ListWithEvents<Camera> cameras = new ListWithEvents<>();
    private static Camera activeCamera = null;
    public static final ListWithEvents<Node> selection = new ListWithEvents<>();
    public static final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    public static void start() {
        nodeFactory.clear();
        Factory.Category<Node> nodule = nodeFactory.getRoot().add("Node", Node::new);
        nodule.add("DHParameter", DHParameter::new);
        nodule.add("HingeJoint", HingeJoint::new);
        nodule.add("LimbSolver", LimbSolver::new);
        nodule.add("MarlinRobotArm", MarlinRobotArm::new);
        nodule.add("Material", Material::new);
        nodule.add("MeshInstance", MeshInstance::new);
        nodule.add("Motor", Motor::new);
        nodule.add("TargetPlanner", TargetPlanner::new);
        Factory.Category<Node> pose = nodule.add("Pose", Pose::new);
            pose.add("Camera", Camera::new);
            pose.add("LookAt", LookAt::new);
            pose.add("Limb", Limb::new);
            pose.add("AttachmentPoint", AttachmentPoint::new);
        reset();
    }

    public static void reset() {
        selection.removeAll();

        // reset camera
        List<Camera> toRemove = new ArrayList<>(cameras.getList());
        for(Camera c : toRemove) cameras.remove(c);
        Camera first = new Camera("Camera 1");
        cameras.add(first);
        setActiveCamera(first);
        double v = Math.sqrt(Math.pow(50,2)/3d); // match the viewport default orbit distance.
        first.setPosition(new Vector3d(v,v,v));
        first.lookAt(new Vector3d(0,0,0));

        // reset scene
        List<Node> toRemove2 = new ArrayList<>(scene.getChildren());
        for(Node n : toRemove2) {
            scene.removeChild(n);
        }

        textureFactory.reset();
        meshFactory.reset();

        scene = new Node("Scene");
    }

    public static void addSceneChangeListener(SceneChangeListener listener) {
        listeners.add(SceneChangeListener.class,listener);
    }

    public static void removeSceneChangeListener(SceneChangeListener listener) {
        listeners.remove(SceneChangeListener.class,listener);
    }

    public static void setScene(Node newScene) {
        fireBeforeSceneChange(newScene);
        scene = newScene;
        fireAfterSceneChange(newScene);
    }

    private static void fireBeforeSceneChange(Node newScene) {
        for (SceneChangeListener listener : listeners.getListeners(SceneChangeListener.class)) {
            listener.beforeSceneChange(newScene);
        }
    }

    private static void fireAfterSceneChange(Node newScene) {
        for (SceneChangeListener listener : listeners.getListeners(SceneChangeListener.class)) {
            listener.afterSceneChange(newScene);
        }
    }

    public static Node getScene() {
        return scene;
    }

    public static Camera getActiveCamera() {
        if(cameras.getList().isEmpty()) return null;
        return activeCamera;
    }

    public static void setActiveCamera(Camera camera) {
        activeCamera = camera;
    }
}
