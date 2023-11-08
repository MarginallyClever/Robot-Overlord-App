package com.marginallyclever.robotoverlord.systems.motor;

import com.marginallyclever.convenience.swing.graph.SingleLineGraph;
import com.marginallyclever.robotoverlord.components.motors.ServoComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.parameters.swing.ViewElementDouble;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;

public class TuneServoPIDPanel extends JPanel {
    private final SingleLineGraph graph = new SingleLineGraph();
    private final ServoComponent servo;
    private final MotorSystem motorSystem;
    private final DoubleParameter start = new DoubleParameter("start (deg)",0);
    private final DoubleParameter target = new DoubleParameter("target (deg)",90);
    private final DoubleParameter time = new DoubleParameter("time (s)",25);

    public TuneServoPIDPanel(ServoComponent servo, MotorSystem motorSystem) {
        super(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        this.motorSystem = motorSystem;
        this.servo = servo;

        GridBagConstraints g = new GridBagConstraints();
        g.gridx=0;
        g.gridy=0;
        g.fill=GridBagConstraints.BOTH;
        g.anchor=GridBagConstraints.WEST;
        g.weightx=1;
        g.weighty=1;
        add(graph,g);
        g.gridy++;

        g.weightx=1;
        g.weighty=0;

        add(new ViewElementDouble(start   ),g);     g.gridy++;
        add(new ViewElementDouble(target  ),g);     g.gridy++;
        add(new ViewElementDouble(time    ),g);     g.gridy++;
        add(new ViewElementDouble(servo.kP),g);     g.gridy++;
        add(new ViewElementDouble(servo.kI),g);     g.gridy++;
        add(new ViewElementDouble(servo.kD),g);     g.gridy++;

        updateGraph(null);

        start.addPropertyChangeListener(this::updateGraph);
        target.addPropertyChangeListener(this::updateGraph);
        time.addPropertyChangeListener(this::updateGraph);
        servo.kP.addPropertyChangeListener(this::updateGraph);
        servo.kI.addPropertyChangeListener(this::updateGraph);
        servo.kD.addPropertyChangeListener(this::updateGraph);
    }

    private void updateGraph(PropertyChangeEvent propertyChangeEvent) {
        graph.clear();
        graph.setGridSpacingX(1);
        graph.setGridSpacingY(10);
        servo.resetPIDMemory();
        servo.currentAngle.set(start.get());
        servo.desiredAngle.set(target.get());
        servo.currentRPM.set(0.0);
        servo.desiredRPM.set(0.0);

        double maxTime = time.get();
        double stepSize=1.0/30.0;
        for(double t=0;t<maxTime;t+=stepSize) {
            motorSystem.updateMotor(servo,stepSize);
            graph.addValue(t,servo.currentAngle.get());
        }
        graph.setBoundsToData();
        graph.repaint();
    }

    public static void main(String[] args) {
        ServoComponent servo = MotorFactory.createDefaultServo();
        Entity entity = new Entity("Servo");
        entity.addComponent(servo);
        MotorSystem motorSystem = new MotorSystem(null);

        JFrame frame = new JFrame("Tune PID");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new TuneServoPIDPanel(servo,motorSystem));
        frame.setPreferredSize(new Dimension(300,350));
        frame.setMinimumSize(new Dimension(300,350));
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);
    }
}
