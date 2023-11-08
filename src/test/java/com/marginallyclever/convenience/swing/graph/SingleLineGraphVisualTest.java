package com.marginallyclever.convenience.swing.graph;

import com.marginallyclever.convenience.log.Log;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

public class SingleLineGraphVisualTest {
    public static void main(String[] args) {
        Log.start();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SingleLineGraph graph = new SingleLineGraph();
        double v = Math.random()*500;
        for(int i=0;i<250;++i) {
            graph.addValue(i,v);
            v += Math.random()*10-5;
        }
        graph.setBoundsToData();
        graph.setBorder(new BevelBorder(BevelBorder.LOWERED));

        JFrame frame = new JFrame(SingleLineGraph.class.getSimpleName());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(800,400));
        frame.setContentPane(graph);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
