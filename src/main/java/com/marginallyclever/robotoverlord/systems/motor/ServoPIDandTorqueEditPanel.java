package com.marginallyclever.robotoverlord.systems.motor;

import com.marginallyclever.robotoverlord.components.motors.ServoComponent;
import com.marginallyclever.robotoverlord.entity.Entity;

import javax.swing.*;
import java.awt.*;

@Deprecated public class ServoPIDandTorqueEditPanel extends JPanel {
    public ServoPIDandTorqueEditPanel(ServoComponent servo,MotorSystem motorSystem) {
        super(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Torque Curve",new TorqueCurveEditPanel(servo));
        tabbedPane.addTab("Tune PID",new TuneServoPIDPanel(servo,motorSystem));
        add(tabbedPane,BorderLayout.CENTER);
    }
}
