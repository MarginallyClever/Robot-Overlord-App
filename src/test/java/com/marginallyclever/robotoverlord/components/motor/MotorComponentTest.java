package com.marginallyclever.robotoverlord.components.motor;

import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.components.ComponentTest;
import com.marginallyclever.robotoverlord.components.motors.MotorComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.TreeMap;

public class MotorComponentTest {
    @Test
    public void serialize() throws Exception {
        // Create a new MotorComponent
        MotorComponent before = new MotorComponent();
        MotorComponent after = new MotorComponent();

        // Set a known Torque Curve
        TreeMap<Integer, Double> a = before.getTorqueCurve();
        a.put(1000, 200.5);
        a.put(2000, 400.7);
        a.put(3000, 600.2);
        before.gearRatio.set(2.0);

        ComponentTest.saveAndLoad(before,after);

        SerializationContext context = new SerializationContext("");
        after.parseJSON(before.toJSON(context),context);
        TreeMap<Integer, Double> b = after.getTorqueCurve();

        // Validate the Torque Curve
        Assertions.assertEquals(a, b);
        Assertions.assertEquals(before.gearRatio.get(), after.gearRatio.get());
    }
}
