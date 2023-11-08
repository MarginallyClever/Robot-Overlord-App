package com.marginallyclever.robotoverlord.systems.robot.robotarm.controlarmpanel.jointhistorypanel;

import com.marginallyclever.convenience.swing.graph.MultiLineGraph;
import com.marginallyclever.convenience.swing.graph.GraphLine;
import com.marginallyclever.convenience.swing.graph.GraphModel;
import com.marginallyclever.robotoverlord.components.DHComponent;
import com.marginallyclever.robotoverlord.components.RobotComponent;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Displays a history of joint positions in a graph.
 * @author Dan Royer
 * @since 2.10.0
 */
public class JointHistoryPanel extends JPanel {
    private final MultiLineGraph graph = new MultiLineGraph();
    private double timeSpan = 60;

    public JointHistoryPanel(RobotComponent robot) {
        super(new BorderLayout());
        JPanel graphPanel = createGraphComponent(robot);
        JPanel scaleButtons = createScaleButtons();
        this.add(scaleButtons,BorderLayout.NORTH);
        this.add(graphPanel,BorderLayout.CENTER);
    }

    private JPanel createScaleButtons() {
        JPanel scaleButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton button10s = new JButton("10s");
        button10s.addActionListener(e->setTimeSpan(10));
        scaleButtons.add(button10s);
        JButton button1m = new JButton("1m");
        button1m.addActionListener(e->setTimeSpan(60));
        scaleButtons.add(button1m);
        JButton button10m = new JButton("10m");
        button10m.addActionListener(e->setTimeSpan(60*10));
        scaleButtons.add(button10m);
        JButton button1h = new JButton("1h");
        button1h.addActionListener(e->setTimeSpan(60*60));
        scaleButtons.add(button1h);
        return scaleButtons;
    }

    public void setTimeSpan(double timeSpan) {
        this.timeSpan = timeSpan;
        updateRange();
        graph.repaint();
    }

    private JPanel createGraphComponent(RobotComponent robot) {
        GraphModel graphModel = graph.getModel();

        int bones = robot.getNumBones();
        for(int i=0;i<bones;++i) {
            DHComponent bone = robot.getBone(i);
            GraphLine line = new GraphLine();
            graphModel.addLine(bone.getEntity().getName(),line);
            int finalI = i;
            bone.theta.addPropertyChangeListener(evt -> {
                double t = System.currentTimeMillis()*0.001;
                double max = bone.getJointMax();
                double min = bone.getJointMin();
                double v = (double)evt.getNewValue();

                line.addPoint(t, (v-min)/(max-min));
                System.out.println("add "+ finalI +"="+evt.getNewValue());
                // TODO remove oldest value if too many?
                updateRange();
                graph.repaint();
            });
            line.addPoint(System.currentTimeMillis()*0.001, bone.theta.get());
        }

        double t = System.currentTimeMillis()*0.001;
        graph.setRange(new Rectangle2D.Double(t,t,0,1));
        graph.assignQualitativeColors();
        graph.setBorder(new BevelBorder(BevelBorder.LOWERED));

        return graph;
    }

    private void updateRange() {
        double t = System.currentTimeMillis()*0.001;
        Rectangle2D.Double range = graph.getRange();
        double min = Math.max( range.getMinX(), t-timeSpan);
        range.setRect(min,0,timeSpan,1);
        graph.setRange(range);
    }
}
