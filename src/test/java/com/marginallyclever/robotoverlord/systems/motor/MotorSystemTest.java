package com.marginallyclever.robotoverlord.systems.motor;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.marginallyclever.robotoverlord.components.motors.MotorComponent;
import com.marginallyclever.robotoverlord.components.motors.ServoComponent;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MotorSystemTest {
    @Test
    public void testNoLoadMotor() {
        EntityManager em = new EntityManager();
        MotorSystem ms = new MotorSystem(em);

        MotorComponent mc = new MotorComponent();
        em.getRoot().addComponent(mc);

        mc.setTorqueAtRPM(0, 3);
        mc.setTorqueAtRPM(100, 2.5);
        mc.setTorqueAtRPM(200, 2);
        mc.setTorqueAtRPM(300, 1);
        mc.setTorqueAtRPM(400, 0);

        Assertions.assertEquals(3,mc.getTorqueAtRpm(0));
        Assertions.assertEquals(2.5,mc.getTorqueAtRpm(100));
        Assertions.assertEquals(2,mc.getTorqueAtRpm(200));
        Assertions.assertEquals(1,mc.getTorqueAtRpm(300));
        Assertions.assertEquals(0,mc.getTorqueAtRpm(400));
        // test interpolation
        Assertions.assertEquals(1.5,mc.getTorqueAtRpm(250));

        mc.setCurrentVelocity(0);
        mc.setDesiredVelocity(10.0);
        for(int i=0;i<100;++i) {
            ms.update(1);
        }
        // check motor gets up to speed after a while
        Assertions.assertEquals(10.0,mc.getCurrentVelocity());
    }

    @Test
    public void testServo() {
        EntityManager em = new EntityManager();
        MotorSystem ms = new MotorSystem(em);

        ServoComponent sc = new ServoComponent();
        em.getRoot().addComponent(sc);

        sc.setTorqueAtRPM(0, 3);
        sc.setTorqueAtRPM(100, 2.5);
        sc.setTorqueAtRPM(200, 2);
        sc.setTorqueAtRPM(300, 1);
        sc.setTorqueAtRPM(400, 0);

        Assertions.assertEquals(3,sc.getTorqueAtRpm(0));
        Assertions.assertEquals(2.5,sc.getTorqueAtRpm(100));
        Assertions.assertEquals(2,sc.getTorqueAtRpm(200));
        Assertions.assertEquals(1,sc.getTorqueAtRpm(300));
        Assertions.assertEquals(0,sc.getTorqueAtRpm(400));
        // test interpolation
        Assertions.assertEquals(1.5,sc.getTorqueAtRpm(250));

        sc.setCurrentVelocity(0);
        sc.desiredAngle.set(90.0);
        for(int i=0;i<100;++i) {
            ms.update(1);
            //System.out.println(sc.currentAngle.get());
        }
        Assertions.assertEquals(90.0,sc.currentAngle.get(),0.0001);
    }
}
