package com.marginallyclever.robotoverlord.components.uiexposedtypes;

import com.marginallyclever.robotoverlord.AbstractEntityTest;
import com.marginallyclever.robotoverlord.uiexposedtypes.IntEntity;
import org.junit.jupiter.api.Test;

public class IntEntityTest {
    @Test
    public void saveAndLoad() throws Exception {
        IntEntity a = new IntEntity("a",1);
        IntEntity b = new IntEntity("b",2);
        AbstractEntityTest.saveAndLoad(a,b);
    }
}
