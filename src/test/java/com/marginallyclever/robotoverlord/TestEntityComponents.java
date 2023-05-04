package com.marginallyclever.robotoverlord;

import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestEntityComponents {
    @Test
    public void addAndRemoveOneComponent() {
        Entity e = new Entity();
        Assertions.assertEquals(0,e.getComponentCount());

        Component c = new Component();
        e.addComponent(c);
        Assertions.assertEquals(1,e.getComponentCount());
        Assertions.assertEquals(e,c.getEntity());

        e.removeComponent(c);
        Assertions.assertEquals(0,e.getComponentCount());
    }

    /**
     * A instance of a class derived from {@link Component} can only exist once on each {@link Entity}.
     */
    @Test
    public void addOnlyUniqueComponentSubclassesToEntities() {
        Entity e = new Entity();
        Component c0 = new Component();
        Component c1 = new Component();
        e.addComponent(c0);
        Assertions.assertTrue(e.containsAnInstanceOfTheSameClass(c1));

        e.addComponent(c1);
        Assertions.assertEquals(1, e.getComponentCount());
        Assertions.assertEquals(c0,e.getComponent(Component.class));

        e.addComponent(new PoseComponent());
        Assertions.assertEquals(2, e.getComponentCount());

    }

    @Test
    public void getComponentWithGenerics() {
        Entity e = new Entity();
        Assertions.assertNull(e.getComponent(PoseComponent.class));
        e.addComponent(new PoseComponent());
        Assertions.assertNotNull(e.getComponent(PoseComponent.class));
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
/*
    @Test
    public void getMatchingComponentsFromManyEntities() {
        throw new UnsupportedOperationException("TODO");
    }*/
}
