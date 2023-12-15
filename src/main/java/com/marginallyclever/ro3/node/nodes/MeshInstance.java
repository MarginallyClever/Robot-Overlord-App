package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.robotoverlord.swing.CollapsiblePanel;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;

import javax.swing.*;
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

        pane.setLayout(new GridLayout(0,2));
        JLabel label = new JLabel("Mesh");
        JButton button = new JButton((mesh==null) ? "..." : mesh.getClass().getSimpleName());
        label.setLabelFor(button);
        pane.add(label);
        pane.add(button);

        button.addActionListener(e -> {
            MeshFactoryPanel meshFactoryPanel = new MeshFactoryPanel();
            int result = meshFactoryPanel.run();
            if(result == JFileChooser.APPROVE_OPTION) {
                mesh = meshFactoryPanel.getMesh();
                button.setText(mesh.getClass().getSimpleName());
            }
        });

        if(mesh!=null) {
            pane.add(new JLabel("verts"));
            pane.add(new JLabel(""+mesh.getNumVertices()));
            pane.add(new JLabel("tris"));
            pane.add(new JLabel(""+mesh.getNumTriangles()));
        }

        super.getComponents(list);
    }

    public Mesh getMesh() {
        return mesh;
    }
}
