package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.robotoverlord.swing.CollapsiblePanel;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;

import javax.swing.*;
import java.awt.*;
import java.io.File;
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

        JButton button = new JButton();
        setMeshButtonLabel(button);
        button.addActionListener(e -> {
            MeshFactoryPanel meshFactoryPanel = new MeshFactoryPanel();
            int result = meshFactoryPanel.run();
            if(result == JFileChooser.APPROVE_OPTION) {
                mesh = meshFactoryPanel.getMesh();
                setMeshButtonLabel(button);
            }
        });
        addLabelAndComponent(pane,"Mesh",button);

        if(mesh!=null) {
            addLabelAndComponent(pane,"Vertices",new JLabel(""+mesh.getNumVertices()));
            addLabelAndComponent(pane,"Triangles",new JLabel(""+mesh.getNumTriangles()));
        }

        super.getComponents(list);
    }

    private void setMeshButtonLabel(JButton button) {
        button.setText((mesh==null) ? "..." : mesh.getSourceName().substring(mesh.getSourceName().lastIndexOf(File.separatorChar)+1));
    }

    public Mesh getMesh() {
        return mesh;
    }
}
