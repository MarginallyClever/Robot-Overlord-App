package com.marginallyclever.robotoverlord;

import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.components.LightComponent;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.shapes.Box;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.vecmath.Vector3d;

public class SceneTest {
    private Scene createABasicProcedurallyBuiltScene() {
        PoseComponent pose;

        Scene scene = new Scene();
        Entity mainCamera = new Entity("Main Camera");
        scene.addEntityToParent(mainCamera,scene.getRoot());
        mainCamera.addComponent(new PoseComponent());
        mainCamera.addComponent(new CameraComponent());

        Entity light0 = new Entity("light 0");
        scene.addEntityToParent(light0,scene.getRoot());
        light0.addComponent(new PoseComponent());
        light0.addComponent(new LightComponent());

        Entity boxEntity = new Entity("Box");
        boxEntity.addComponent(pose = new PoseComponent());
        Box box = new Box();
        boxEntity.addComponent(box);
        boxEntity.addComponent(new MaterialComponent());
        scene.addEntityToParent(boxEntity,scene.getRoot());
        pose.setPosition(new Vector3d(-10,0,0));

        return scene;
    }

    private static void saveAndLoad(Scene a,Scene b) throws Exception {
        b.parseJSON(a.toJSON());
        Assertions.assertEquals(a.toJSON().toString(),b.toJSON().toString());
    }

    @Test
    public void saveAndLoadTests() throws Exception {
        saveAndLoad(new Scene(),new Scene());
        saveAndLoad(createABasicProcedurallyBuiltScene(),new Scene());
    }

    @Test
    public void moveEntity() {
        Scene scene = new Scene();
        Entity a = new Entity();
        Entity b = new Entity();
        Entity c = new Entity();
        scene.addEntityToParent(a,b);
        Assertions.assertEquals(b,a.getParent());
        scene.addEntityToParent(a,c);
        Assertions.assertEquals(c,a.getParent());
        Assertions.assertEquals(c,a.getParent());
        Assertions.assertFalse(b.getChildren().contains(a));
        scene.removeEntityFromParent(a,b);
        Assertions.assertEquals(c,a.getParent());
        Assertions.assertNull(b.getParent());
        Assertions.assertEquals(c,a.getParent());
    }
}
