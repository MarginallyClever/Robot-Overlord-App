package com.marginallyclever.robotoverlord.components.program;

import com.marginallyclever.robotoverlord.components.ComponentTest;
import com.marginallyclever.robotoverlord.components.program.ProgramPathComponent;
import org.junit.jupiter.api.Test;

public class ProgramPathComponentTest {
    @Test
    public void testSaveAndLoad() throws Exception {
        ProgramPathComponent a = new ProgramPathComponent();
        ProgramPathComponent b = new ProgramPathComponent();
        ComponentTest.saveAndLoad(a,b);

        a.moveSpeed.set(2.0);
        a.moveType.set(ProgramPathComponent.MOVE_RAPID);
        ComponentTest.saveAndLoad(a,b);

        a.moveSpeed.set(5.0);
        a.moveType.set(ProgramPathComponent.MOVE_LINEAR);
        ComponentTest.saveAndLoad(a,b);
    }
}
