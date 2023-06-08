package com.marginallyclever.robotoverlord.systems.motor;

import com.marginallyclever.robotoverlord.components.motors.MotorComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MotorFactoryTest {
    @Test
    public void testCreateDefaultMotor() {
        MotorComponent mc = MotorFactory.createDefaultMotor();

        Assertions.assertEquals(16.0,mc.getTorqueAtRpm(  0));
        Assertions.assertEquals(16.0,mc.getTorqueAtRpm( 30));
        Assertions.assertEquals(15.0,mc.getTorqueAtRpm( 60));
        Assertions.assertEquals(12.0,mc.getTorqueAtRpm( 90));
        Assertions.assertEquals( 6.0,mc.getTorqueAtRpm(180));
        Assertions.assertEquals( 1.8,mc.getTorqueAtRpm(240));
        // test interpolation
        Assertions.assertEquals(13.5,mc.getTorqueAtRpm(75));
    }
}
