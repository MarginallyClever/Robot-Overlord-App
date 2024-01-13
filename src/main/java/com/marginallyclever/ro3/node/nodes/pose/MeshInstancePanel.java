package com.marginallyclever.ro3.node.nodes.pose;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.mesh.MeshChooserDialog;
import com.marginallyclever.ro3.mesh.MeshSmoother;
import com.marginallyclever.ro3.node.NodePanelHelper;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class MeshInstancePanel extends JPanel {
    private final MeshInstance meshInstance;

    public MeshInstancePanel(MeshInstance meshInstance) {
        super(new GridLayout(0,2));
        this.meshInstance = meshInstance;
        this.setName(MeshInstance.class.getSimpleName());

        var mesh = meshInstance.getMesh();

        JButton select = new JButton();
        setMeshButtonLabel(select);
        select.addActionListener(e -> {
            var meshChooserDialog = new MeshChooserDialog();
            meshChooserDialog.setSelectedItem(mesh);
            int result = meshChooserDialog.run(this);
            if(result == JFileChooser.APPROVE_OPTION) {
                meshInstance.setMesh( meshChooserDialog.getSelectedItem() );
                setMeshButtonLabel(select);
            }
        });
        NodePanelHelper.addLabelAndComponent(this,"Mesh",select);

        if(mesh!=null) {
            NodePanelHelper.addLabelAndComponent(this,"Vertices",new JLabel(""+mesh.getNumVertices()));
            NodePanelHelper.addLabelAndComponent(this,"Triangles",new JLabel(""+mesh.getNumTriangles()));

            JButton smooth = new JButton("Smooth");
            smooth.addActionListener(e -> MeshSmoother.smoothNormals(mesh,0.01f,0.25f) );
            NodePanelHelper.addLabelAndComponent(this,"Normals",smooth);

            JButton adjust = new JButton("Adjust");
            adjust.addActionListener(e -> meshInstance.adjustLocal());
            NodePanelHelper.addLabelAndComponent(this,"Local origin",adjust);

            JButton reload = new JButton("Reload");
            reload.addActionListener(e-> Registry.meshFactory.reload(mesh) );
            NodePanelHelper.addLabelAndComponent(this,"Source",reload);
        }
    }

    private void setMeshButtonLabel(JButton button) {
        var mesh = meshInstance.getMesh();
        button.setText((mesh==null) ? "..." : mesh.getSourceName().substring(mesh.getSourceName().lastIndexOf(File.separatorChar)+1));
    }
}
