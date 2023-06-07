package com.marginallyclever.robotoverlord.systems.motor;

import com.marginallyclever.robotoverlord.components.motors.ServoComponent;
import com.marginallyclever.robotoverlord.entity.Entity;

import javax.swing.*;
import java.awt.*;

public class ServoPIDandTorqueEditPanel extends JPanel {
    public ServoPIDandTorqueEditPanel(ServoComponent servo,MotorSystem motorSystem) {
        super(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Torque Curve",new TorqueCurveEditPanel(servo));
        tabbedPane.addTab("Tune PID",new TuneServoPIDPanel(servo,motorSystem));
        add(tabbedPane,BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        ServoComponent servo = MotorFactory.createDefaultServo();
        Entity entity = new Entity("Servo");
        entity.addComponent(servo);
        MotorSystem motorSystem = new MotorSystem(null);

        JFrame frame = new JFrame("Tune PID and Torque Curve");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new ServoPIDandTorqueEditPanel(servo,motorSystem));
        frame.setPreferredSize(new Dimension(300,450));
        frame.setMinimumSize(new Dimension(300,450));
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);
    }
}
