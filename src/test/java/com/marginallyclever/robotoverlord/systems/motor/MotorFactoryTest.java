package com.marginallyclever.robotoverlord.systems.motor;

import com.marginallyclever.robotoverlord.components.motors.MotorComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MotorFactoryTest {
    @Test
    public void testCreateDefaultMotor() {
        MotorComponent mc = MotorFactory.createDefaultMotor();

        Assertions.assertEquals(3,mc.getTorqueAtRpm(0));
        Assertions.assertEquals(2.5,mc.getTorqueAtRpm(100));
        Assertions.assertEquals(2,mc.getTorqueAtRpm(200));
        Assertions.assertEquals(1,mc.getTorqueAtRpm(300));
        Assertions.assertEquals(0,mc.getTorqueAtRpm(400));
        // test interpolation
        Assertions.assertEquals(1.5,mc.getTorqueAtRpm(250));
    }
}
