package com.marginallyclever.ro3.mesh.proceduralmesh;

import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.RO3;
import com.marginallyclever.ro3.mesh.Mesh;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;

import javax.swing.*;
import java.awt.*;

/**
 * Displays a combo box of procedural mesh shapes.  if initialized with a
 * {@link com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance}, it will display the mesh's settings.
 * when the combobox is changed the mesh will be updated.
 */
public class ProceduralMeshFactoryPanel extends JPanel {
    private final MeshInstance meshInstance;
    private final JPanel proceduralContainer = new JPanel(new BorderLayout());

    public ProceduralMeshFactoryPanel() {
        this(new MeshInstance());
    }

    public ProceduralMeshFactoryPanel(MeshInstance meshInstance) {
        super(new BorderLayout());
        this.meshInstance = meshInstance;

        this.setName(ProceduralMeshFactoryPanel.class.getSimpleName());

        // Add a button to choose the type of ProceduralMesh.
        JPanel topPanel = new JPanel(new GridLayout(0,2));
        String [] proceduralNames = ProceduralMeshFactory.getListOfProceduralMeshes();
        JComboBox<String> proceduralComboBox = new JComboBox<>(proceduralNames);
        PanelHelper.addLabelAndComponent(topPanel,"Type",proceduralComboBox);
        add(topPanel,BorderLayout.NORTH);
        // add a container for the type-specific settings.
        add(proceduralContainer,BorderLayout.CENTER);

        // If the mesh is a ProceduralMesh, set the selected item in the JComboBox.
        Mesh mesh = meshInstance.getMesh();
        proceduralComboBox.setSelectedItem((mesh instanceof ProceduralMesh) ? mesh.getClass().getSimpleName() : 0);
        onProceduralChange(proceduralComboBox);
        proceduralComboBox.addActionListener(e -> onProceduralChange(proceduralComboBox));
    }

    private void onProceduralChange(JComboBox<String> proceduralComboBox) {
        String selected = (String) proceduralComboBox.getSelectedItem();
        JPanel panel = null;
        ProceduralMesh proceduralMesh = (meshInstance.getMesh() instanceof ProceduralMesh) ? (ProceduralMesh)meshInstance.getMesh() : null;
        if (proceduralMesh == null || !proceduralMesh.getEnglishName().equals(selected)) {
            System.out.println("Creating new procedural mesh " + selected);
            proceduralMesh = ProceduralMeshFactory.createMesh(selected);
        }
        meshInstance.setMesh(proceduralMesh);
        panel = ProceduralMeshFactory.createPanel(proceduralMesh);

        proceduralContainer.removeAll();
        if(panel!=null) proceduralContainer.add(panel,BorderLayout.CENTER);
        proceduralContainer.revalidate();
    }

    public static void main(String[] args) {
        RO3.setLookAndFeel();
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.add(new ProceduralMeshFactoryPanel());
        frame.pack();
        SwingUtilities.invokeLater(()->frame.setVisible(true));
    }
}
