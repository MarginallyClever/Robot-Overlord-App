package com.marginallyclever.ro3;

import com.marginallyclever.ro3.listwithevents.ListWithEvents;
import com.marginallyclever.ro3.node.nodes.*;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.render.RenderPass;
import com.marginallyclever.ro3.render.renderpasses.DrawBackground;
import com.marginallyclever.ro3.render.renderpasses.DrawCameras;
import com.marginallyclever.ro3.render.renderpasses.DrawMeshes;
import com.marginallyclever.ro3.render.renderpasses.DrawPoses;
import com.marginallyclever.ro3.texture.TextureFactory;

import javax.swing.event.EventListenerList;

/**
 * {@link Registry} is a place to store global variables.
 */
public class Registry {
    public static TextureFactory textureFactory = new TextureFactory();
    public static final Factory<Node> nodeFactory = new Factory<>(Node.class);
    public static ListWithEvents<RenderPass> renderPasses = new ListWithEvents<>();

    private static Node scene = new Node("Scene");
    public static ListWithEvents<Camera> cameras = new ListWithEvents<>();
    public static EventListenerList listeners = new EventListenerList();

    public static void start() {
        Factory.Category<Node> nodule = new Factory.Category<>( "Node", null );
        nodeFactory.getRoot().add(nodule);
        Factory.Category<Node> pose = new Factory.Category<>( Pose::new );
            pose.add(new Factory.Category<>( MeshInstance::new ));
            pose.add(new Factory.Category<>( Camera::new ));
        nodule.add(pose);
        nodule.add(new Factory.Category<>( Material::new ));
        nodule.add(new Factory.Category<>( DHParameter::new ));

        cameras.add(new Camera("Camera 1"));

        renderPasses.add(new DrawBackground());
        renderPasses.add(new DrawMeshes());
        renderPasses.add(new DrawPoses());
        renderPasses.add(new DrawCameras());
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
}
