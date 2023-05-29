package com.marginallyclever.robotoverlord.components.motor;

import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.components.motors.MotorComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.TreeMap;

public class MotorComponentTest {

    @Test
    public void serialize() {
        // Create a new MotorComponent
        MotorComponent before = new MotorComponent();
        MotorComponent after = new MotorComponent();

        // Set a known Torque Curve
        TreeMap<Integer, Double> a = before.getTorqueCurve();
        a.put(1000, 200.5);
        a.put(2000, 400.7);
        a.put(3000, 600.2);

        SerializationContext context = new SerializationContext("");
        after.parseJSON(before.toJSON(context),context);
        TreeMap<Integer, Double> b = after.getTorqueCurve();

        // Validate the Torque Curve
        Assertions.assertEquals(a, b);
    }
}
