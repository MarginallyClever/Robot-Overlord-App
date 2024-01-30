package com.marginallyclever.ro3.apps;

import javax.swing.*;
import java.awt.*;

/**
 * All apps extend from App for Reflection.
 */
public abstract class App extends JPanel {
    public App() {
        super();
    }

    public App(LayoutManager layout) {
        super(layout);
    }
}
