package com.marginallyclever.robotoverlord;

import com.marginallyclever.robotoverlord.components.*;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.shapes.Box;
import org.junit.jupiter.api.Test;

import javax.vecmath.Vector3d;

public class SceneTest {
    private Scene createABasicProcedurallyBuiltScene() {
        PoseComponent pose;

        Scene scene = new Scene();
        Entity mainCamera = new Entity("Main Camera");
        scene.addEntity(mainCamera);
        mainCamera.addComponent(new PoseComponent());
        mainCamera.addComponent(new CameraComponent());

        Entity light0 = new Entity("light 0");
        scene.addEntity(light0);
        light0.addComponent(new PoseComponent());
        light0.addComponent(new LightComponent());

        Entity boxEntity = new Entity("Box");
        boxEntity.addComponent(pose = new PoseComponent());
        Box box = new Box();
        boxEntity.addComponent(box);
        boxEntity.addComponent(new MaterialComponent());
        scene.addEntity(boxEntity);
        pose.setPosition(new Vector3d(-10,0,0));

        return scene;
    }

    @Test
    public void saveAndLoadTests() throws Exception {
        EntityTest.saveAndLoad(new Scene(),new Scene());
        EntityTest.saveAndLoad(createABasicProcedurallyBuiltScene(),new Scene());
    }

}
