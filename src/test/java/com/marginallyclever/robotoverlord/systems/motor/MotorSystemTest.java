package com.marginallyclever.robotoverlord.systems.motor;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.marginallyclever.convenience.helpers.MathHelper;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.components.PoseComponent;
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

        ServoComponent sc = MotorFactory.createDefaultServo();
        em.getRoot().addComponent(sc);

        Entity attached = new Entity();
        em.addEntityToParent(attached,em.getRoot());

        sc.setCurrentVelocity(0);
        sc.desiredAngle.set(90.0);
        for(int i=0;i<100;++i) {
            ms.update(1);
            //System.out.println(sc.currentAngle.get());
        }
        // check servo reaches target angle
        Assertions.assertEquals(90.0,sc.currentAngle.get(),0.0001);
        // check attached shape has turned
        Vector3d xAttached = MatrixHelper.getXAxis(attached.getComponent(PoseComponent.class).getWorld());

        Assertions.assertTrue(MathHelper.equals(xAttached,new Vector3d(0,1,0),0.001));
    }
}
