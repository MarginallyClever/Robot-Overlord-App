package com.marginallyclever.robotoverlord.components;

import org.junit.jupiter.api.Test;

public class LightComponentTest {
    @Test
    public void saveAndLoad() throws Exception {
        LightComponent a = new LightComponent();
        LightComponent b = new LightComponent();
        ComponentTest.saveAndLoad(a,b);

        a.setDiffuse(0.1f,0.2f,0.3f,0.4f);
        a.setAmbient(0.2f,0.3f,0.4f,0.5f);
        a.setSpecular(0.3f,0.4f,0.5f,0.6f);
        a.setAttenuationLinear(a.getAttenuationConstant()+0.1f);
        a.setAttenuationLinear(a.getAttenuationLinear()+0.2f);
        a.setAttenuationQuadratic(a.getAttenuationQuadratic()+0.3f);
        a.setExponent(a.getExponent()+1);
        a.setCutoff(a.getCutoff()+2);
        ComponentTest.saveAndLoad(a,b);

        a.setPreset(1);
        ComponentTest.saveAndLoad(a,b);
    }
}
