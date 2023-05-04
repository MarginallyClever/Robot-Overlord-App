package com.marginallyclever.robotoverlord.systems.render.gcodepath;

import com.marginallyclever.robotoverlord.systems.render.gcodepath.GCodePathElement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GCodePathElementTest {
    @Test
    public void getIandJ() {
        GCodePathElement element = new GCodePathElement("G2 X25.5 Y50.5 I5.25 J-3.75");
        Assertions.assertEquals(5.25,element.getI());
        Assertions.assertEquals(-3.75,element.getJ());
    }
}
