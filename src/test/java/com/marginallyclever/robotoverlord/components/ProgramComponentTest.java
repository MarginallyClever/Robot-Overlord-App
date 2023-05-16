package com.marginallyclever.robotoverlord.components;

import org.junit.jupiter.api.Test;

public class ProgramComponentTest {
    @Test
    public void testSaveAndLoad() throws Exception {
        ProgramComponent a = new ProgramComponent();
        ProgramComponent b = new ProgramComponent();
        ComponentTest.saveAndLoad(a,b);

        a.isRunning.set(true);
        a.mode.set(ProgramComponent.RUN_TO_END);
        ComponentTest.saveAndLoad(a,b);

        a.isRunning.set(false);
        a.mode.set(ProgramComponent.RUN_CYCLE);
        ComponentTest.saveAndLoad(a,b);
    }
}
