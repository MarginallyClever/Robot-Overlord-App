package com.marginallyclever.robotoverlord.systems.motor;

import com.marginallyclever.convenience.helpers.MathHelper;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.motors.DCMotorComponent;
import com.marginallyclever.robotoverlord.components.motors.MotorComponent;
import com.marginallyclever.robotoverlord.components.motors.ServoComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.vecmath.Vector3d;

public class MotorSystemTest {

    @Test
    public void testNoLoadMotor() {
        EntityManager em = new EntityManager();
        MotorSystem ms = new MotorSystem(em);

        MotorComponent mc = MotorFactory.createDefaultMotor();
        em.getRoot().addComponent(mc);

        mc.setCurrentRPM(0);
        mc.setDesiredRPM(10.0);
        for(int i=0;i<100;++i) {
            ms.update(1);
        }
        // check motor gets up to speed after a while
        Assertions.assertEquals(10.0,mc.getCurrentRPM());
    }

    @Test
    public void testServo() {
        EntityManager em = new EntityManager();
        MotorSystem ms = new MotorSystem(em);

        ServoComponent sc = MotorFactory.createDefaultServo();
        em.getRoot().addComponent(sc);

        Entity attached = new Entity();
        em.addEntityToParent(attached,em.getRoot());
        sc.addConnection(attached);

        sc.desiredAngle.set(90.0);
        double stepSize=1.0/30.0;
        for(int i=0;i<5/stepSize;++i) {
            ms.update(stepSize);
            //System.out.println(sc.currentAngle.get());
        }
        // check servo reaches target angle
        Assertions.assertEquals(90.0,sc.currentAngle.get(),0.001);
        // check attached shape has turned
        Vector3d xAttached = MatrixHelper.getXAxis(attached.getComponent(PoseComponent.class).getWorld());

        Assertions.assertTrue(MathHelper.equals(xAttached,new Vector3d(0,1,0),0.01));
    }

    /**
     * confirm turning at 90 degrees per second for 1 second results in 90 degrees of turn.
     */
    @Test
    public void testRateOfTurn() {
        EntityManager em = new EntityManager();
        MotorSystem ms = new MotorSystem(em);
        MotorComponent mc = new DCMotorComponent();
        mc.setTorqueAtRPM(0,1000.0);
        mc.setTorqueAtRPM(10000,1000.0);

        double rpm = (90.0/360.0)*60.0;  // 90 degrees per second
        mc.currentRPM.set(rpm);
        mc.setDesiredRPM(rpm);
        mc.currentAngle.set(0.0);
        ms.updateMotor(mc,1.0);  // seconds
        Assertions.assertEquals(90,mc.currentAngle.get(),1e-3);
    }
}
