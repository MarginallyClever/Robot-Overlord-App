package com.marginallyclever.robotoverlord.components;

import org.junit.jupiter.api.Test;

public class MaterialComponentTest {
    @Test
    public void saveAndLoad() throws Exception {
        MaterialComponent a = new MaterialComponent();
        MaterialComponent b = new MaterialComponent();
        ComponentTest.saveAndLoad(a,b);

        a.drawOnTop.set(true);
        a.diffuse.set(1,2,3,4);
        a.ambient.set(5,6,7,8);
        a.emission.set(9,10,11,12);
        a.specular.set(13,14,15,16);
        a.shininess.set(17);
        a.texture.set("texture.png");

        ComponentTest.saveAndLoad(a,b);
    }
}
