package com.marginallyclever.robotoverlord.systems.motor;

import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.components.motors.DCMotorComponent;
import com.marginallyclever.robotoverlord.components.motors.MotorComponent;
import com.marginallyclever.robotoverlord.components.motors.ServoComponent;
import com.marginallyclever.robotoverlord.components.motors.StepperMotorComponent;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * produce common motors in robotics, prefilled with torque curve.
 */
public class MotorFactory {
    private static final Logger logger = LoggerFactory.getLogger(MotorFactory.class);

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

    /**
     * Create a {@link MotorComponent} from JSON file.
     * @param file JSON file to load
     * @return a motor component
     */
    public static MotorComponent createMotorFromFile(File file) throws IOException {
        String newPath = file.getAbsolutePath();
        logger.debug("Loading from {}", newPath);

        Path path = Paths.get(newPath);
        String onlyPath = path.getParent().toString();

        StringBuilder responseStrBuilder = new StringBuilder();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String inputStr;
            while ((inputStr = reader.readLine()) != null) {
                responseStrBuilder.append(inputStr);
            }
        }

        SerializationContext context = new SerializationContext(onlyPath);
        JSONObject json = new JSONObject(responseStrBuilder.toString());
        return createMotorFromJSON(json,context);
    }

    public static MotorComponent createMotorFromJSON(JSONObject json, SerializationContext context) {
        MotorComponent mc = null;
        String type = json.getString("type");
        switch (type) {
            case "DCMotorComponent" -> mc = new DCMotorComponent();
            case "ServoComponent" -> mc = new ServoComponent();
            case "StepperMotorComponent" -> mc = new StepperMotorComponent();
            default -> throw new RuntimeException("Unknown motor type " + type);
        }

        mc.parseJSON(json, context);

        return mc;
    }
}
