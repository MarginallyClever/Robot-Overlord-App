package com.marginallyclever.ro3.apps.donatello;

import com.marginallyclever.ro3.apps.App;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * {@link Donatello} is a panel that displays the {@link com.marginallyclever.ro3.Registry}. as a 2D graph.  It
 * uses Reflection to the fields of each Node.
 */
public class Donatello extends App {
    private final JToolBar toolbar = new JToolBar();
    private final GraphViewPanel graphView = new GraphViewPanel();

    public static void main(String[] args) {
        var frame = new JFrame("Donatello");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        var app = new Donatello();
        frame.setContentPane(app);
        frame.setSize(800,600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public Donatello() {
        super(new BorderLayout());

        setupToolbar();
        add(toolbar,BorderLayout.NORTH);
        add(graphView,BorderLayout.CENTER);
    }

    private void setupToolbar() {
        toolbar.setFloatable(false);

        var toggleGrid = new JCheckBox("Grid");
        toggleGrid.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/apps/viewport/icons8-eye-16.png"))));
        toggleGrid.setSelected(graphView.getDrawGrid());
        toggleGrid.addActionListener(e -> {
            graphView.setDrawGrid(toggleGrid.isSelected());
            repaint();
        });
        toolbar.add(toggleGrid);
    }
}
