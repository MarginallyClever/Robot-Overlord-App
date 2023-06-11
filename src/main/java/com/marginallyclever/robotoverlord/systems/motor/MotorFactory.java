package com.marginallyclever.robotoverlord.systems.motor;

import com.marginallyclever.robotoverlord.components.motors.DCMotorComponent;
import com.marginallyclever.robotoverlord.components.motors.MotorComponent;
import com.marginallyclever.robotoverlord.components.motors.ServoComponent;
import com.marginallyclever.robotoverlord.components.motors.StepperMotorComponent;

/**
 * produce common motors in robotics, prefilled with torque curve.
 */
public class MotorFactory {
    public static MotorComponent createDefaultMotor() {
        MotorComponent mc = new DCMotorComponent();
        setDefaultMotorCurve(mc);
        return mc;
    }

    public static void setDefaultMotorCurve(MotorComponent mc) {
        mc.setTorqueAtRPM( 0,16);
        mc.setTorqueAtRPM(30,16);
        mc.setTorqueAtRPM(60,15);
        mc.setTorqueAtRPM(90,12);
        mc.setTorqueAtRPM(180,6);
        mc.setTorqueAtRPM(240,1.8);
    }

    public static ServoComponent createDefaultServo() {
        ServoComponent sc = new ServoComponent();
        MotorFactory.setDefaultMotorCurve(sc);

        sc.kP.set(0.3333);
        sc.kI.set(0.0);
        sc.kD.set(0.0);

        return sc;
    }

    /**
     * Curve matches 17HS13-0404S1 200-step 0.4A NEMA 17 stepper motor.
     * @return a stepper motor with a default torque curve.
     */
    public static StepperMotorComponent createDefaultStepperMotor() {
        StepperMotorComponent smc = new StepperMotorComponent();
        smc.setTorqueAtRPM( 0,16);
        smc.setTorqueAtRPM(30,16);
        smc.setTorqueAtRPM(60,15);
        smc.setTorqueAtRPM(90,12);
        smc.setTorqueAtRPM(180,6);
        smc.setTorqueAtRPM(240,1.8);
        return smc;
    }
}
