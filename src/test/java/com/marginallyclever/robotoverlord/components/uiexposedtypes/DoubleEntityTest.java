package com.marginallyclever.robotoverlord.components.uiexposedtypes;

import com.marginallyclever.robotoverlord.AbstractEntityTest;
import com.marginallyclever.robotoverlord.uiexposedtypes.DoubleEntity;
import org.junit.jupiter.api.Test;

public class DoubleEntityTest {
    @Test
    public void saveAndLoad() throws Exception {
        DoubleEntity a = new DoubleEntity("a",1.2);
        DoubleEntity b = new DoubleEntity("b",3.4);
        AbstractEntityTest.saveAndLoad(a,b);
    }
}
