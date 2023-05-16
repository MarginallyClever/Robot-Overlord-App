package com.marginallyclever.robotoverlord.components.shapes;

import com.marginallyclever.robotoverlord.components.ComponentTest;
import org.junit.jupiter.api.Test;

public class GridTest {
    @Test
    public void saveAndLoad() throws Exception {
        Grid a = new Grid();
        Grid b = new Grid();
        ComponentTest.saveAndLoad(a,b);
    }
}
