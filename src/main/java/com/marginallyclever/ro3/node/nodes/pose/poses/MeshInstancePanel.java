package com.marginallyclever.ro3.node.nodes.pose.poses;

import com.jogamp.opengl.GL3;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.mesh.Mesh;
import com.marginallyclever.ro3.mesh.MeshChooserDialog;
import com.marginallyclever.ro3.mesh.MeshSmoother;
import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.mesh.shapes.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * GUI for a {@link MeshInstance}.
 */
public class MeshInstancePanel extends JPanel {
    public static final String[] meshSources = {
            "Procedural",
            "File"
    };

    // matching the values sent to glDrawArrays and glDrawElements.
    public static final Map<String,Integer> renderStyles = new HashMap<>();
    static {
        renderStyles.put("Points",GL3.GL_POINTS);
        renderStyles.put("Line strip",GL3.GL_LINE_STRIP);
        renderStyles.put("Line loop",GL3.GL_LINE_LOOP);
        renderStyles.put("Lines",GL3.GL_LINES);
        renderStyles.put("Triangle strip",GL3.GL_TRIANGLE_STRIP);
        renderStyles.put("Triangle fan",GL3.GL_TRIANGLE_FAN);
        renderStyles.put("Triangles",GL3.GL_TRIANGLES);
        renderStyles.put("Quads",GL3.GL_QUADS);
        //renderStyles.put("Quad strip",GL3.GL_QUADS);
        //renderStyles.put("Polygon",GL3.GL_TRIANGLES);
    }

    private final MeshInstance meshInstance;
    private final JPanel sourceContainer = new JPanel(new GridLayout(0,2));
    private final JPanel proceduralContainer = new JPanel(new BorderLayout());
    private final JPanel detailsContainer = new JPanel(new GridLayout(0,2));

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
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;

        addMeshSource(gbc);
        changeMeshDetails();

        gbc.gridwidth=2;
        add(sourceContainer,gbc);
        gbc.gridy++;
        add(proceduralContainer,gbc);
        gbc.gridy++;
        add(detailsContainer,gbc);

        meshInstance.addPropertyChangedListener(e->changeMeshDetails());
    }

    private void changeMeshDetails() {
        detailsContainer.removeAll();
        // mesh details
        Mesh mesh = meshInstance.getMesh();
        if(mesh==null) {
            PanelHelper.addLabelAndComponent(detailsContainer, "No mesh", new JLabel(""));
            return;
        }
        PanelHelper.addLabelAndComponent(detailsContainer,"Vertices",new JLabel(""+mesh.getNumVertices()));
        PanelHelper.addLabelAndComponent(detailsContainer,"Triangles",new JLabel(""+mesh.getNumTriangles()));

        JComboBox<String> renderStyle = new JComboBox<>(renderStyles.keySet().toArray(new String[0]));
        setRenderStyleComboBox(mesh, renderStyle);
        renderStyle.addActionListener(e->updateMeshStyle(mesh, renderStyle));
        PanelHelper.addLabelAndComponent(detailsContainer,"Style",renderStyle);

        JButton smooth = new JButton("Smooth");
        smooth.addActionListener(e -> MeshSmoother.smoothNormals(mesh,0.01f,0.25f) );
        PanelHelper.addLabelAndComponent(detailsContainer,"Normals",smooth);

        JButton adjust = new JButton("Adjust");
        adjust.addActionListener(e -> meshInstance.adjustLocal());
        PanelHelper.addLabelAndComponent(detailsContainer,"Local origin",adjust);

        detailsContainer.revalidate();
    }

    private void updateMeshStyle(Mesh mesh, JComboBox<String> comboBox) {
        mesh.setRenderStyle(renderStyles.get(comboBox.getItemAt(comboBox.getSelectedIndex())));
    }

    private void setRenderStyleComboBox(Mesh mesh, JComboBox<String> comboBox) {
        // get the key for the current value.
        var value = mesh.getRenderStyle();
        renderStyles.forEach((k,v)->{
            if(v==value) {
                comboBox.setSelectedItem(k);
            }
        });
    }

    private void addMeshSource(GridBagConstraints gbc) {
        // Create a list of available mesh sources
        JComboBox<String> meshSourceComboBox = new JComboBox<>(meshSources);
        var filename = meshInstance.getMesh()==null ? "" : meshInstance.getMesh().getSourceName();
        meshSourceComboBox.setSelectedItem((filename!=null && !filename.isEmpty())?"File":"Procedural");
        meshSourceComboBox.addActionListener(e -> changeMeshSource(meshSourceComboBox));
        changeMeshSource(meshSourceComboBox);
        PanelHelper.addLabelAndComponent(this, "Mesh Source", meshSourceComboBox,gbc);
    }

    private void changeMeshSource(JComboBox<String> meshSourceComboBox) {
        sourceContainer.removeAll();
        proceduralContainer.removeAll();
        String selectedMeshSource = (String) meshSourceComboBox.getSelectedItem();
        if(selectedMeshSource==null || selectedMeshSource.equals("File")) {
            addFileMesh();
        } else if(selectedMeshSource.equals("Procedural")) {
            proceduralContainer.add(new ProceduralMeshFactoryPanel(meshInstance));
        } else {
            throw new RuntimeException("Unknown mesh source: "+selectedMeshSource);
        }
        sourceContainer.revalidate();
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
                changeMeshDetails();
                setMeshButtonLabel(chooseMesh);
            }
        });
        PanelHelper.addLabelAndComponent(sourceContainer,"Path",chooseMesh);

        JButton reload = new JButton("Reload");
        reload.addActionListener(e-> Registry.meshFactory.reload(meshInstance.getMesh()) );
        PanelHelper.addLabelAndComponent(sourceContainer,"Source",reload);
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
