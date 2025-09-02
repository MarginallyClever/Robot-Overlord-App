package com.marginallyclever.ro3.mesh.proceduralmesh;

import javax.swing.*;

public class GenerativeMeshPanel extends JPanel {
    public GenerativeMeshPanel() {
        this(new GenerativeMesh());
    }

    public GenerativeMeshPanel(GenerativeMesh generativeMesh) {
        super();
        setName(GenerativeMesh.class.getSimpleName());
    }
}
