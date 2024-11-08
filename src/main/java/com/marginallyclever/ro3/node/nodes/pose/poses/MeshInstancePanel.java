package com.marginallyclever.ro3.node.nodes.pose.poses;

import com.jogamp.opengl.GL3;
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
    public static final String[] meshSources = {
            "Procedural",
            "File"
    };

    // matching the values sent to glDrawArrays and glDrawElements.
    public static final String [] renderStyleNames = {
            "Points",
            "Line strip",
            "Line loop",
            "Lines",
            "Triangle strip",
            "Triangle fan",
            "Triangles",
            "Quad strip",
            "Quads",
            "Polygon"
    };

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
            JComboBox<String> renderStyle = new JComboBox<>(renderStyleNames);
            setRenderStyleComboBox(mesh, renderStyle);
            renderStyle.addActionListener(e->updateMeshStyle(mesh, renderStyle));
            PanelHelper.addLabelAndComponent(this,"Style",renderStyle,gbc);

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

    private void updateMeshStyle(Mesh mesh, JComboBox<String> renderStyle) {
        mesh.setRenderStyle(switch(renderStyle.getSelectedIndex()) {
            case 0 -> GL3.GL_POINTS;
            case 1 -> GL3.GL_LINE_STRIP;
            case 2 -> GL3.GL_LINE_LOOP;
            case 3 -> GL3.GL_LINES;
            case 4 -> GL3.GL_TRIANGLE_STRIP;
            case 5 -> GL3.GL_TRIANGLE_FAN;
            case 6 -> GL3.GL_TRIANGLES;
            //case 7 -> GL3.GL_QUAD_STRIP;
            case 8 -> GL3.GL_QUADS;
            //case 9 -> GL3.GL_POLYGON;
            default -> GL3.GL_TRIANGLES;
        });
    }

    private void setRenderStyleComboBox(Mesh mesh, JComboBox<String> renderStyle) {
        renderStyle.setSelectedIndex(switch(mesh.getRenderStyle()) {
            case GL3.GL_POINTS -> 0;
            case GL3.GL_LINE_STRIP -> 1;
            case GL3.GL_LINE_LOOP -> 2;
            case GL3.GL_LINES -> 3;
            case GL3.GL_TRIANGLE_STRIP -> 4;
            case GL3.GL_TRIANGLE_FAN -> 5;
            case GL3.GL_TRIANGLES -> 6;
            //case GL3.GL_QUAD_STRIP -> 7;
            case GL3.GL_QUADS -> 8;
            //case GL3.GL_POLYGON -> 9;
            default -> 0;
        });
    }

    private void addMeshSource(GridBagConstraints gbc) {
        // Create a list of available mesh sources
        JComboBox<String> meshSourceComboBox = new JComboBox<>(meshSources);
        var filename = meshInstance.getMesh().getSourceName();
        meshSourceComboBox.setSelectedItem((filename!=null && !filename.isEmpty())?"File":"Procedural");
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
