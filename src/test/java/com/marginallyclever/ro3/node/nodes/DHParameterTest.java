package com.marginallyclever.ro3.node.nodes;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DHParameterTest {
    @Test
    void testGetD() {
        DHParameter dhParameter = new DHParameter();
        dhParameter.setD(10.0);
        assertEquals(10.0, dhParameter.getD());
    }

    @Test
    void testGetR() {
        DHParameter dhParameter = new DHParameter();
        dhParameter.setR(20.0);
        assertEquals(20.0, dhParameter.getR());
    }

    @Test
    void testGetAlpha() {
        DHParameter dhParameter = new DHParameter();
        dhParameter.setAlpha(30.0);
        assertEquals(30.0, dhParameter.getAlpha());
    }

    @Test
    void testGetTheta() {
        DHParameter dhParameter = new DHParameter();
        dhParameter.setTheta(40.0);
        assertEquals(40.0, dhParameter.getTheta());
    }
}