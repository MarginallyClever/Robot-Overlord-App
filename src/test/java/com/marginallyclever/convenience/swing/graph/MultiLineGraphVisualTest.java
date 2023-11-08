package com.marginallyclever.convenience.swing.graph;

import com.marginallyclever.convenience.log.Log;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

public class MultiLineGraphVisualTest {
    public static void main(String[] args) {
        Log.start();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}


        MultiLineGraph graph = new MultiLineGraph();
        GraphModel model = graph.getModel();
        for(int j=0;j<6;++j) {
            GraphLine line = new GraphLine();
            model.addLine(Integer.toString(j), line);
            double v = Math.random() * 500;
            for (int i = 0; i < 250; ++i) {
                line.addPoint(i, v);
                v += Math.random() * 10 - 5;
            }
        }
        graph.assignQualitativeColors();
        graph.setRangeToModel();
        graph.setBorder(new BevelBorder(BevelBorder.LOWERED));

        JFrame frame = new JFrame(MultiLineGraph.class.getSimpleName());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(800,400));
        frame.setContentPane(graph);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
