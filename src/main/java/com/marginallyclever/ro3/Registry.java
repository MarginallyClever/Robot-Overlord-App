package com.marginallyclever.ro3;

import com.marginallyclever.ro3.listwithevents.ListWithEvents;
import com.marginallyclever.ro3.node.nodes.*;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.render.RenderPass;
import com.marginallyclever.ro3.render.renderpasses.*;
import com.marginallyclever.ro3.texture.TextureFactory;

import javax.swing.event.EventListenerList;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link Registry} is a place to store global variables.
 */
public class Registry {
    public static TextureFactory textureFactory = new TextureFactory();
    public static final Factory<Node> nodeFactory = new Factory<>(Node.class);
    public static ListWithEvents<RenderPass> renderPasses = new ListWithEvents<>();
    private static Node scene = new Node("Scene");
    public static ListWithEvents<Camera> cameras = new ListWithEvents<>();
    private static Camera activeCamera = null;

    public static EventListenerList listeners = new EventListenerList();

    public static void start() {
        nodeFactory.clear();
        Factory.Category<Node> nodule = nodeFactory.getRoot().add("Node", null);
        nodule.add("DHParameter", DHParameter::new);
        nodule.add("Material", Material::new);
        nodule.add("MeshInstance", MeshInstance::new);
        Factory.Category<Node> pose = nodule.add("Pose", Pose::new);
            pose.add("Camera", Camera::new);
            pose.add("HingeJoint", HingeJoint::new);

        renderPasses.add(new DrawBackground());
        renderPasses.add(new DrawMeshes());
        renderPasses.add(new DrawBoundingBoxes());
        renderPasses.add(new DrawPoses());
        renderPasses.add(new DrawCameras());
        renderPasses.add(new DrawDHParameters());
        renderPasses.add(new DrawHingeJoints());

        reset();
    }

    public static void reset() {
        // reset camera
        List<Camera> toRemove = new ArrayList<>(cameras.getList());
        for(Camera c : toRemove) cameras.remove(c);
        cameras.add(new Camera("Camera 1"));

        // reset scene
        List<Node> toRemove2 = new ArrayList<>(scene.getChildren());
        for(Node n : toRemove2) {
            scene.removeChild(n);
        }
        scene = new Node("Scene");
    }

    public static void addSceneChangeListener(SceneChangeListener listener) {
        listeners.add(SceneChangeListener.class,listener);
    }

    public static void removeSceneChangeListener(SceneChangeListener listener) {
        listeners.remove(SceneChangeListener.class,listener);
    }

    public static void setScene(Node newScene) {
        for (SceneChangeListener listener : listeners.getListeners(SceneChangeListener.class)) {
            listener.beforeSceneChange(newScene);
        }

        scene = newScene;

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
