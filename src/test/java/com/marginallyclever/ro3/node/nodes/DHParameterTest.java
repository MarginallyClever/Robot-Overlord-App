package com.marginallyclever.ro3.node.nodes;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import org.junit.jupiter.api.Test;

import javax.vecmath.Matrix4d;

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

    @Test
    public void setDHMatrix() {
    	var a = new DHParameter();
        a.setD(1.0);
        a.setR(2.0);
        a.setAlpha(3.0);
        a.setTheta(4.0);
        a.getDHMatrix();
        var b = new DHParameter();
    	b.setDHMatrix(a.getDHMatrix());
    	assertEquals(1.0, b.getD(),1e-6);
    	assertEquals(2.0, b.getR(),1e-6);
    	assertEquals(3.0, b.getAlpha(),1e-6);
    	assertEquals(4.0, b.getTheta(),1e-6);
    }
}