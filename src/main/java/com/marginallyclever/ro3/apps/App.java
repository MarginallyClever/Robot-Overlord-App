package com.marginallyclever.ro3.apps;

import javax.swing.*;
import java.awt.*;

/**
 * All apps extend from App for Reflection.
 */
public class App extends JPanel {
    /**
     * Default constructor
     */
    public App() {
        super();
    }

    /**
     * Constructor with layout manager
     *
     * @param layout the layout manager to use
     */
    public App(LayoutManager layout) {
        super(layout);
    }
}
