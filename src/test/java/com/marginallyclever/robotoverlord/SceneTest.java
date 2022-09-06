package com.marginallyclever.robotoverlord;

import com.marginallyclever.robotoverlord.components.*;
import com.marginallyclever.robotoverlord.components.shapes.BoxComponent;
import org.junit.jupiter.api.Test;

import javax.vecmath.Vector3d;

public class SceneTest {
    @Test
    public void createABasicScene() {
        PoseComponent pose;

        Scene scene = new Scene();
        Entity mainCamera = new Entity("Main Camera");
        scene.addChild(mainCamera);
        mainCamera.addComponent(new PoseComponent());
        mainCamera.addComponent(new CameraComponent());

        Entity light0 = new Entity("light 0");
        scene.addChild(light0);
        light0.addComponent(new PoseComponent());
        light0.addComponent(new LightComponent());

        Entity boxEntity = new Entity("Box");
        boxEntity.addComponent(pose = new PoseComponent());
        BoxComponent box = new BoxComponent();
        boxEntity.addComponent(box);
        boxEntity.addComponent(new MaterialComponent());
        scene.addChild(boxEntity);
        pose.setPosition(new Vector3d(-10,0,0));
    }
}
