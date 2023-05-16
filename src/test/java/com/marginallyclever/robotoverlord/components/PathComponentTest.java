package com.marginallyclever.robotoverlord.components;

import org.junit.jupiter.api.Test;

public class PathComponentTest {
    @Test
    public void testSaveAndLoad() throws Exception {
        PathComponent a = new PathComponent();
        PathComponent b = new PathComponent();
        ComponentTest.saveAndLoad(a,b);

        a.moveSpeed.set(2.0);
        a.moveType.set(PathComponent.MOVE_RAPID);
        ComponentTest.saveAndLoad(a,b);

        a.moveSpeed.set(5.0);
        a.moveType.set(PathComponent.MOVE_LINEAR);
        ComponentTest.saveAndLoad(a,b);
    }
}
