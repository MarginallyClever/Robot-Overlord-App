package com.marginallyclever.robotOverlord;

import com.marginallyclever.robotOverlord.components.*;
import org.junit.jupiter.api.Test;

public class SceneTest {
    @Test
    public void createABasicScene() {
        Scene scene = new Scene();
        Entity mainCamera = new Entity("Main Camera");
        scene.addChild(mainCamera);
        mainCamera.addComponent(new PoseComponent());
        mainCamera.addComponent(new CameraComponent());

        Entity light0 = new Entity("light 0");
        scene.addChild(light0);
        light0.addComponent(new PoseComponent());
        light0.addComponent(new LightComponent());

        scene.addComponent(new SkyboxComponent());

        Entity boxEntity = new Entity("Box");
        BoxComponent box = new BoxComponent();
        boxEntity.addComponent(box);
        scene.addChild(boxEntity);
    }
}
