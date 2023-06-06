package com.marginallyclever.robotoverlord.systems.motor;

import com.marginallyclever.robotoverlord.components.motors.MotorComponent;
import com.marginallyclever.robotoverlord.components.motors.ServoComponent;

/**
 * produce common motors in robotics, prefilled with torque curve.
 */
public class MotorFactory {
    public static MotorComponent createDefaultMotor() {
        MotorComponent mc = new MotorComponent();
        setDefaultMotorCurve(mc);
        return mc;
    }

    public static void setDefaultMotorCurve(MotorComponent mc) {
        mc.setTorqueAtRPM(0, 3);
        mc.setTorqueAtRPM(100, 2.5);
        mc.setTorqueAtRPM(200, 2);
        mc.setTorqueAtRPM(300, 1);
        mc.setTorqueAtRPM(400, 0);
    }

    public static ServoComponent createDefaultServo() {
        ServoComponent sc = new ServoComponent();
        MotorFactory.setDefaultMotorCurve(sc);
        return sc;
    }
}
