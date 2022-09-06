package com.marginallyclever.robotoverlord.components.uiexposedtypes;

import com.marginallyclever.robotoverlord.AbstractEntityTest;
import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.uiexposedtypes.BooleanEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;

public class BooleanEntityTest {
    @Test
    public void saveAndLoad() throws Exception {
        BooleanEntity a = new BooleanEntity("a",true);
        BooleanEntity b = new BooleanEntity("b",false);
        AbstractEntityTest.saveAndLoad(a,b);
    }
}
