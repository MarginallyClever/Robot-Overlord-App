package com.marginallyclever.robotoverlord.parameters;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.vecmath.Vector3d;

public class ListParameterTest {
    @Test
    public void addAndRemove() {
        ListParameter<BooleanParameter> a = new ListParameter<>("A",new BooleanParameter());
        a.add(new BooleanParameter("a1",true));
        a.add(new BooleanParameter("a2",false));
        a.add(new BooleanParameter("a3",true));
        a.remove(0);
        Assertions.assertEquals(2,a.size());
    }

    @Test
    public void saveAndLoadBoolean() throws Exception {
        ListParameter<BooleanParameter> a = new ListParameter<>("A",new BooleanParameter());
        ListParameter<BooleanParameter> b = new ListParameter<>("B",new BooleanParameter());
        a.add(new BooleanParameter("a1",true));
        a.add(new BooleanParameter("a2",false));
        AbstractParameterTest.saveAndLoad(a,b);
    }

    @Test
    public void saveAndLoadColor() throws Exception {
        ListParameter<ColorParameter> a = new ListParameter<>("A",new ColorParameter());
        ListParameter<ColorParameter> b = new ListParameter<>("B",new ColorParameter());
        a.add(new ColorParameter("a1",0,0,0,0));
        a.add(new ColorParameter("a2",1,1,1,1));
        AbstractParameterTest.saveAndLoad(a,b);
    }

    @Test
    public void saveAndLoadDouble() throws Exception {
        ListParameter<DoubleParameter> a = new ListParameter<>("A",new DoubleParameter());
        ListParameter<DoubleParameter> b = new ListParameter<>("B",new DoubleParameter());
        a.add(new DoubleParameter("a1",0.0));
        a.add(new DoubleParameter("a2",1.0));
        AbstractParameterTest.saveAndLoad(a,b);
    }

    @Test
    public void saveAndLoadInt() throws Exception {
        ListParameter<IntParameter> a = new ListParameter<>("A",new IntParameter());
        ListParameter<IntParameter> b = new ListParameter<>("B",new IntParameter());
        a.add(new IntParameter("a1",0));
        a.add(new IntParameter("a2",1));
        AbstractParameterTest.saveAndLoad(a,b);
    }

    @Test
    public void saveAndLoadString() throws Exception {
        ListParameter<StringParameter> a = new ListParameter<>("A",new StringParameter());
        ListParameter<StringParameter> b = new ListParameter<>("B",new StringParameter());
        a.add(new StringParameter("a1","0"));
        a.add(new StringParameter("a2","1"));
        AbstractParameterTest.saveAndLoad(a,b);
    }

    @Test
    public void saveAndLoadVector3D() throws Exception {
        ListParameter<Vector3DParameter> a = new ListParameter<>("A",new Vector3DParameter());
        ListParameter<Vector3DParameter> b = new ListParameter<>("B",new Vector3DParameter());
        a.add(new Vector3DParameter("a1",0,0,0));
        a.add(new Vector3DParameter("a2",1,1,1));
        AbstractParameterTest.saveAndLoad(a,b);
    }
}
