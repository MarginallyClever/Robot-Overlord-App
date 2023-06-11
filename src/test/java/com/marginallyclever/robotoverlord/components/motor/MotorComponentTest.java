package com.marginallyclever.robotoverlord.components.motor;

import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.components.ComponentTest;
import com.marginallyclever.robotoverlord.components.motors.DCMotorComponent;
import com.marginallyclever.robotoverlord.components.motors.MotorComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.TreeMap;

public class MotorComponentTest {
    @Test
    public void serialize() throws Exception {
        // Create a new MotorComponent
        MotorComponent before = new DCMotorComponent();
        MotorComponent after = new DCMotorComponent();

        // Set a known Torque Curve
        TreeMap<Double, Double> a = before.getTorqueCurve();
        a.put(1000.0, 200.5);
        a.put(2000.0, 400.7);
        a.put(3000.0, 600.2);
        before.gearRatio.set(2.0);

        ComponentTest.saveAndLoad(before,after);

        SerializationContext context = new SerializationContext("");
        after.parseJSON(before.toJSON(context),context);
        TreeMap<Double, Double> b = after.getTorqueCurve();

        // Validate the Torque Curve
        Assertions.assertEquals(a, b);
        Assertions.assertEquals(before.gearRatio.get(), after.gearRatio.get());
    }
}
