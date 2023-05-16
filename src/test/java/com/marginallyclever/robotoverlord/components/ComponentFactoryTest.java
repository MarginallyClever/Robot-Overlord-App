package com.marginallyclever.robotoverlord.components;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class ComponentFactoryTest {
    private static final Logger logger = LoggerFactory.getLogger(ComponentFactoryTest.class);
    @Test
    public void loadAll() throws Exception {
        ArrayList<String> list = ComponentFactory.getAllComponentNames();
        Assertions.assertNotEquals(0,list.size());

        for(String name : list) {
            logger.debug("instantiating "+name);
            Component c = ComponentFactory.load(name);
        }
    }
}
