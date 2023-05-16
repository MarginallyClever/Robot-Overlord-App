package com.marginallyclever.robotoverlord;

import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.components.LightComponent;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.shapes.Box;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.vecmath.Vector3d;

public class EntityManagerTest {
    private EntityManager createABasicProcedurallyBuiltScene() {
        PoseComponent pose;

        EntityManager entityManager = new EntityManager();
        Entity mainCamera = new Entity("Main Camera");
        entityManager.addEntityToParent(mainCamera, entityManager.getRoot());
        mainCamera.addComponent(new PoseComponent());
        mainCamera.addComponent(new CameraComponent());

        Entity light0 = new Entity("light 0");
        entityManager.addEntityToParent(light0, entityManager.getRoot());
        light0.addComponent(new PoseComponent());
        light0.addComponent(new LightComponent());

        Entity boxEntity = new Entity("Box");
        boxEntity.addComponent(pose = new PoseComponent());
        Box box = new Box();
        boxEntity.addComponent(box);
        boxEntity.addComponent(new MaterialComponent());
        entityManager.addEntityToParent(boxEntity, entityManager.getRoot());
        pose.setPosition(new Vector3d(-10,0,0));

        return entityManager;
    }

    private static void saveAndLoad(EntityManager a, EntityManager b) throws Exception {
        b.parseJSON(a.toJSON());
        Assertions.assertEquals(a.toJSON().toString(),b.toJSON().toString());
    }

    @Test
    public void saveAndLoadTests() throws Exception {
        saveAndLoad(new EntityManager(),new EntityManager());
        saveAndLoad(createABasicProcedurallyBuiltScene(),new EntityManager());
    }

    @Test
    public void moveEntity() {
        EntityManager entityManager = new EntityManager();
        Entity a = new Entity();
        Entity b = new Entity();
        Entity c = new Entity();
        entityManager.addEntityToParent(a,b);
        Assertions.assertEquals(b,a.getParent());
        entityManager.addEntityToParent(a,c);
        Assertions.assertEquals(c,a.getParent());
        Assertions.assertEquals(c,a.getParent());
        Assertions.assertFalse(b.getChildren().contains(a));
        entityManager.removeEntityFromParent(a,b);
        Assertions.assertEquals(c,a.getParent());
        Assertions.assertNull(b.getParent());
        Assertions.assertEquals(c,a.getParent());
    }
}
