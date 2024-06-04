package com.marginallyclever.ro3.node.nodes.pose.poses;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.mesh.Mesh;
import com.marginallyclever.ro3.mesh.MeshChooserDialog;
import com.marginallyclever.ro3.mesh.MeshSmoother;
import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.mesh.shapes.*;
import com.marginallyclever.ro3.mesh.shapes.Box;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * GUI for a {@link MeshInstance}.
 */
public class MeshInstancePanel extends JPanel {
    private final JPanel sourceContainer = new JPanel(new GridLayout(0,2));
    private final MeshInstance meshInstance;

    public MeshInstancePanel() {
        this(new MeshInstance());
    }

    public MeshInstancePanel(MeshInstance meshInstance) {
        super(new GridBagLayout());
        this.meshInstance = meshInstance;
        this.setName(MeshInstance.class.getSimpleName());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.fill = GridBagConstraints.BOTH;

        addMeshSource(gbc);

        // mesh details
        Mesh mesh = meshInstance.getMesh();
        if(mesh!=null) {
            gbc.gridy++;
            PanelHelper.addLabelAndComponent(this,"Vertices",new JLabel(""+mesh.getNumVertices()),gbc);
            gbc.gridy++;
            PanelHelper.addLabelAndComponent(this,"Triangles",new JLabel(""+mesh.getNumTriangles()),gbc);

            gbc.gridy++;
            JButton smooth = new JButton("Smooth");
            smooth.addActionListener(e -> MeshSmoother.smoothNormals(mesh,0.01f,0.25f) );
            PanelHelper.addLabelAndComponent(this,"Normals",smooth,gbc);

            gbc.gridy++;
            JButton adjust = new JButton("Adjust");
            adjust.addActionListener(e -> meshInstance.adjustLocal());
            PanelHelper.addLabelAndComponent(this,"Local origin",adjust,gbc);

            gbc.gridy++;
            JButton reload = new JButton("Reload");
            reload.addActionListener(e-> Registry.meshFactory.reload(mesh) );
            PanelHelper.addLabelAndComponent(this,"Source",reload,gbc);
        }
    }

    private void addMeshSource(GridBagConstraints gbc) {
        // Create a list of available mesh sources
        String[] meshSources = { "Procedural", "File" }; // replace with actual mesh sources
        JComboBox<String> meshSourceComboBox = new JComboBox<>(meshSources);
        meshSourceComboBox.setSelectedItem("Procedural");
        meshSourceComboBox.addActionListener(e -> changeMeshSource(meshSourceComboBox));
        changeMeshSource(meshSourceComboBox);

        // Add the JComboBox to the panel
        PanelHelper.addLabelAndComponent(this, "Mesh Source", meshSourceComboBox,gbc);

        sourceContainer.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        gbc.gridy++;
        gbc.gridwidth=2;
        add(sourceContainer,gbc);
        gbc.gridwidth=1;
    }

    private void changeMeshSource(JComboBox<String> meshSourceComboBox) {
        sourceContainer.removeAll();
        String selectedMeshSource = (String) meshSourceComboBox.getSelectedItem();
        if(selectedMeshSource==null || selectedMeshSource.equals("File")) {
            addFileMesh();
        } else if(selectedMeshSource.equals("Procedural")) {
            addProceduralMesh();
        } else {
            throw new RuntimeException("Unknown mesh source: "+selectedMeshSource);
        }
        sourceContainer.revalidate();
    }

    private void addProceduralMesh() {
        // Add a button to create a procedural mesh
        addButton("+Box", e -> meshInstance.setMesh(new Box()));
        addButton("+Sphere", e -> meshInstance.setMesh(new Sphere()));
        addButton("+Cylinder", e -> meshInstance.setMesh(new Cylinder()));
        addButton("+Capsule", e -> meshInstance.setMesh(new Capsule()));
    }

    private void addButton(String label, ActionListener actionListener) {
        JButton createBox = new JButton(label);
        createBox.addActionListener(actionListener);
        PanelHelper.addLabelAndComponent(sourceContainer,"Procedural",createBox);
    }

    private void addFileMesh() {
        JButton chooseMesh = new JButton();
        setMeshButtonLabel(chooseMesh);
        chooseMesh.addActionListener(e -> {
            var meshChooserDialog = new MeshChooserDialog();
            meshChooserDialog.setSelectedItem(meshInstance.getMesh());
            int result = meshChooserDialog.run(this);
            if(result == JFileChooser.APPROVE_OPTION) {
                meshInstance.setMesh( meshChooserDialog.getSelectedItem() );
                setMeshButtonLabel(chooseMesh);
            }
        });
        PanelHelper.addLabelAndComponent(sourceContainer,"File",chooseMesh);
    }

    private void setMeshButtonLabel(JButton button) {
        var mesh = meshInstance.getMesh();
        if(mesh==null) {
            button.setText("...");
            return;
        }
        var src = mesh.getSourceName().trim();
        button.setText(src.isEmpty() ? "..." : src.substring(src.lastIndexOf(File.separatorChar)+1));
    }
}
