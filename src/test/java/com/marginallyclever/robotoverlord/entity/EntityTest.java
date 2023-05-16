package com.marginallyclever.robotoverlord.entity;

import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EntityTest {
    private static void saveAndLoad(Entity a, Entity b) throws Exception {
        b.parseJSON(a.toJSON());
        Assertions.assertEquals(a.toString(),b.toString());
    }

    @Test
    public void saveAndLoad() throws Exception {
        Entity a = new Entity();
        Entity b = new Entity();
        EntityTest.saveAndLoad(a,b);

        a.setExpanded(!a.getExpanded());
        EntityTest.saveAndLoad(a,b);
    }
    @Test
    public void addAndRemoveOneComponent() {
        Entity e = new Entity();
        Assertions.assertEquals(1,e.getComponentCount());

        CameraComponent c = new CameraComponent();
        e.addComponent(c);
        Assertions.assertEquals(2,e.getComponentCount());
        Assertions.assertEquals(e,c.getEntity());

        e.removeComponent(c);
        Assertions.assertEquals(1,e.getComponentCount());
    }

    /**
     * A instance of a class derived from {@link Component} can only exist once on each {@link Entity}.
     */
    @Test
    public void addOnlyUniqueComponentSubclassesToEntities() {
        Entity e = new Entity();
        CameraComponent c0 = new CameraComponent();
        CameraComponent c1 = new CameraComponent();
        e.addComponent(c0);
        Assertions.assertTrue(e.containsAnInstanceOfTheSameClass(c1));

        e.addComponent(c1);
        Assertions.assertEquals(2, e.getComponentCount());
        Assertions.assertEquals(c0,e.getComponent(CameraComponent.class));

        e.addComponent(new PoseComponent());
        Assertions.assertEquals(2, e.getComponentCount());

    }

    @Test
    public void getComponentWithGenerics() {
        Entity e = new Entity();
        Component c = e.getComponent(PoseComponent.class);
        Assertions.assertNotNull(c);
        e.removeComponent(c);
        Assertions.assertNull(e.getComponent(PoseComponent.class));
    }

    @Test
    public void searchNestedEntitiesForComponent() {
        EntityManager entityManager = new EntityManager();
        Entity e0 = new Entity();
        Entity e1 = new Entity();
        Entity e2 = new Entity();
        entityManager.addEntityToParent(e1,e0);
        entityManager.addEntityToParent(e2,e1);
        e1.addComponent(new PoseComponent());
        e2.addComponent(new CameraComponent());
        e0.addComponent(new CameraComponent());
        Assertions.assertEquals(e0.getComponent(CameraComponent.class),e0.findFirstComponentRecursive(CameraComponent.class));
        Assertions.assertNotEquals(e2.getComponent(CameraComponent.class),e0.findFirstComponentRecursive(CameraComponent.class));
        Assertions.assertNotNull(e0.findFirstComponentRecursive(PoseComponent.class));
        Assertions.assertNotNull(e1.findFirstComponentInParents(CameraComponent.class));
    }
}