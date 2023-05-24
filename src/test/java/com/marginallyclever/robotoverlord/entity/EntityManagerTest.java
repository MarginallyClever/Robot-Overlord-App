package com.marginallyclever.robotoverlord.entity;

import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.components.*;
import com.marginallyclever.robotoverlord.components.shapes.Box;
import com.marginallyclever.robotoverlord.entity.Entity;
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
        SerializationContext context = new SerializationContext("");
        b.parseJSON(a.toJSON(context),context);
        Assertions.assertEquals(a.toJSON(context).toString(),b.toJSON(context).toString());
    }

    @Test
    public void saveAndLoadTests() throws Exception {
        saveAndLoad(new EntityManager(),new EntityManager());
        saveAndLoad(createABasicProcedurallyBuiltScene(),new EntityManager());
    }

    @Test
    public void moveEntity() {
        EntityManager entityManager = new EntityManager();
        moveEntityWithEntityManager(entityManager);
    }

    public void moveEntityWithEntityManager(EntityManager entityManager) {
        Entity a = new Entity();
        Entity b = new Entity();
        Entity c = new Entity();

        entityManager.addEntityToParent(b,entityManager.getRoot());
        entityManager.addEntityToParent(c,entityManager.getRoot());
        Assertions.assertEquals(entityManager.getRoot(),b.getParent());
        Assertions.assertEquals(entityManager.getRoot(),c.getParent());
        // move b into c and make sure no loose ends
        entityManager.addEntityToParent(b,c);
        Assertions.assertEquals(c,b.getParent());
        Assertions.assertNotEquals(entityManager.getRoot(),b.getParent());
        Assertions.assertTrue(c.getChildren().contains(b));
        Assertions.assertEquals(entityManager.getRoot().getChildren().size(),1);

        entityManager.addEntityToParent(a,b);
        Assertions.assertEquals(b,a.getParent());
        // move a to c and confirm there are no loose ends.
        entityManager.addEntityToParent(a,c);
        Assertions.assertEquals(c,a.getParent());
        Assertions.assertNotEquals(b,a.getParent());
        Assertions.assertFalse(b.getChildren().contains(a));
        Assertions.assertTrue(c.getChildren().contains(a));
        // confirm removing a from b does nothing because a is not a child of b.
        entityManager.removeEntityFromParent(a,b);
        Assertions.assertEquals(c,a.getParent());
        Assertions.assertNotEquals(b,a.getParent());
    }

    @Test
    public void deepCopyWithReference() {
        EntityManager entityManager = new EntityManager();
        // add node a
        Entity a = new Entity("a");
        entityManager.addEntityToParent(a,entityManager.getRoot());
        // add node b that contains some reference to a
        Entity b = new Entity("b");
        RobotComponent robot = new RobotComponent();
        b.addComponent(robot);
        entityManager.addEntityToParent(b,entityManager.getRoot());
        robot.gcodePath.set(a.getUniqueID());

        // copy just b, expect the reference to a to be unchanged.
        Entity c = b.deepCopy();
        String cPath = c.getComponent(RobotComponent.class).gcodePath.get();
        String bPath = b.getComponent(RobotComponent.class).gcodePath.get();
        Assertions.assertEquals(cPath, bPath);
        Assertions.assertEquals(cPath,a.getUniqueID());

        // now copy both a and b, expect the reference to a to be changed.
        Entity d = entityManager.getRoot().deepCopy();

        Entity aOfd = d.getChildren().get(0);  // a of d
        Entity bOfd = d.getChildren().get(1);  // b of d
        cPath = bOfd.getComponent(RobotComponent.class).gcodePath.get();
        Assertions.assertNotEquals(cPath, bPath);
        Assertions.assertNotEquals(cPath,a.getUniqueID());
        Assertions.assertEquals(cPath,aOfd.getUniqueID());
    }
}
