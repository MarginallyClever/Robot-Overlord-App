package com.marginallyclever.ro3.nodes;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.swing.CollapsiblePanel;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import java.awt.*;
import java.util.List;

public class MeshInstance extends Pose {
    private Mesh mesh;

    public MeshInstance() {
        super("MeshInstance");
    }

    public MeshInstance(String name) {
        super(name);
    }

    /**
     * Build a Swing Component that represents this Node.
     * @param list the list to add components to.
     */
    public void getComponents(List<JComponent> list) {
        CollapsiblePanel panel = new CollapsiblePanel(MeshInstance.class.getSimpleName());
        list.add(panel);
        JPanel pane = panel.getContentPane();

        pane.setLayout(new FlowLayout(FlowLayout.LEADING));
        JLabel label = new JLabel("Mesh");
        JButton button = new JButton((mesh==null) ? "..." : mesh.getSourceName());
        label.setLabelFor(button);
        pane.add(label);
        pane.add(button);

        button.addActionListener(e -> {
            // TODO run MeshFactoryPanel?
            System.out.println("TODO run MeshFactoryPanel?");
            
        });

        super.getComponents(list);
    }
}
