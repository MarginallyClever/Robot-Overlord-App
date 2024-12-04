package com.marginallyclever.ro3.mesh.proceduralmesh;

import javax.swing.*;

public class CircleXYPanel extends JPanel {
    public CircleXYPanel() {
        this(new CircleXY());
    }

    public CircleXYPanel(CircleXY mesh) {
        super();
        this.setName(CircleXYPanel.class.getSimpleName());

    }
}
