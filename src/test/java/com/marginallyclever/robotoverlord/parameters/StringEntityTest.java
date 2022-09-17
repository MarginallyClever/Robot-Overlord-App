package com.marginallyclever.robotoverlord.parameters;

import com.marginallyclever.robotoverlord.AbstractEntityTest;
import org.junit.jupiter.api.Test;

public class StringEntityTest {
    @Test
    public void saveAndLoad() throws Exception {
        StringEntity a = new StringEntity("a","The quick brown fox jumped over the lazy dog");
        StringEntity b = new StringEntity("b","Quoth the raven, 'nevermore.'");
        AbstractEntityTest.saveAndLoad(a,b);
    }
}
