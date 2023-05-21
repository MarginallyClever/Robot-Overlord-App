package com.marginallyclever.robotoverlord.components.program;

import com.marginallyclever.robotoverlord.components.ComponentTest;
import com.marginallyclever.robotoverlord.components.program.ProgramComponent;
import org.junit.jupiter.api.Test;

public class ProgramComponentTest {
    @Test
    public void testSaveAndLoad() throws Exception {
        ProgramComponent a = new ProgramComponent();
        ProgramComponent b = new ProgramComponent();
        ComponentTest.saveAndLoad(a,b);

        a.setRunning(true);
        a.mode.set(ProgramComponent.RUN_TO_END);
        ComponentTest.saveAndLoad(a,b);

        a.setRunning(false);
        a.mode.set(ProgramComponent.RUN_LOOP);
        ComponentTest.saveAndLoad(a,b);
    }
}
