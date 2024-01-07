package com.marginallyclever.ro3.apps.dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.listwithevents.ItemAddedListener;
import com.marginallyclever.ro3.listwithevents.ItemRemovedListener;
import com.marginallyclever.ro3.mesh.Mesh;
import com.marginallyclever.ro3.mesh.MeshFactory;

/**
 * <p>The {@link MeshChooserDialog} class allows for selecting a {@link com.marginallyclever.ro3.mesh.Mesh}
 * that has been previously loaded by the {@link MeshFactory}.
 * This class also provides access to the {@link MeshFactoryDialog} for loading additional meshes.</p>
 * <p>TODO In the future it would be nice to count references to a Mesh and unload it when no longer needed.</p>
 */
public class MeshChooserDialog extends JPanel implements ItemAddedListener<Mesh>, ItemRemovedListener<Mesh> {
    private final DefaultListModel<Mesh> model = new DefaultListModel<>();
    private final JList<Mesh> meshList = new JList<>();
    private final JToolBar toolBar = new JToolBar();
    private Mesh selectedMesh;
    private String viewType;

    public MeshChooserDialog() {
        super(new BorderLayout());

        setupToolbar();
        setupMeshList();

        var clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> setSelection(null));

        add(toolBar, BorderLayout.NORTH);
        add(new JScrollPane(meshList), BorderLayout.CENTER);
        add(clearButton, BorderLayout.SOUTH);
    }

    private void setSelection(Mesh mesh) {
        selectedMesh = mesh;
        if(mesh==null) {
            meshList.clearSelection();
        } else {
            meshList.setSelectedValue(mesh, true);
        }
    }

    private void setupToolbar() {
        toolBar.add(new JButton(new AbstractAction() {
            {
                putValue(Action.NAME, "Load Mesh");
                putValue(Action.SHORT_DESCRIPTION, "Load a mesh from a file.");
                putValue(Action.SMALL_ICON, new ImageIcon(Objects.requireNonNull(getClass().getResource(
                        "/com/marginallyclever/ro3/apps/actions/icons8-load-16.png"))));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                MeshFactoryDialog meshFactoryDialog = new MeshFactoryDialog();
                int result = meshFactoryDialog.run();
                if (result == JFileChooser.APPROVE_OPTION) {
                    Mesh mesh = meshFactoryDialog.getMesh();
                    setSelection(mesh);
                }
            }
        }));

        /*
        String[] viewTypes = {"List View", "Detail View", "Thumbnail View"};
        JComboBox<String> viewTypeComboBox = new JComboBox<>(viewTypes);
        viewTypeComboBox.addActionListener(e -> {
            viewType = (String) viewTypeComboBox.getSelectedItem();
            updateView();
        });
        toolbar.add(viewTypeComboBox);
        */
    }

    private void setupMeshList() {
        meshList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Mesh mesh = (Mesh) value;
                String text = mesh.getSourceName() + " (" + mesh.getNumVertices() + " vertices)";
                return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
            }
        });

        for (Mesh mesh : Registry.meshFactory.getMeshPool().getList()) {
            model.addElement(mesh);
        }
        meshList.setModel(model);
        meshList.addListSelectionListener(e -> selectedMesh = meshList.getSelectedValue());
    }

    @Override
    public void addNotify() {
        super.addNotify();
        Registry.meshFactory.getMeshPool().addItemAddedListener(this);
        Registry.meshFactory.getMeshPool().addItemRemovedListener(this);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        Registry.meshFactory.getMeshPool().removeItemAddedListener(this);
        Registry.meshFactory.getMeshPool().removeItemRemovedListener(this);
    }

    private void updateView() {
        // Update the list of meshes based on the selected view type
        // This could involve changing the ListCellRenderer of the JList
        // For example, for the "Thumbnail View", you could display a small preview of each mesh
    }

    public Mesh getMesh() {
        return selectedMesh;
    }

    @Override
    public void itemAdded(Object source, Mesh item) {
        model.addElement(item);
    }

    @Override
    public void itemRemoved(Object source, Mesh item) {
        model.removeElement(item);
    }

    /**
     * Run the Mesh selection as a dialog.
     * @param parent the parent component for the dialog.
     * @return JFileChooser.APPROVE_OPTION or JFileChooser.CANCEL_OPTION.  return type is int because this is a
     * JFileChooser replacement.  It is consistent with {@link MeshFactoryDialog} and {@link TextureFactoryDialog}.
     */
    public int run(JComponent parent) {
        int result = JOptionPane.showOptionDialog(
                parent,
                this,
                "Select",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                new ImageIcon(Objects.requireNonNull(getClass().getResource(
                        "/com/marginallyclever/ro3/apps/dialogs/icons8-mesh-16.png"))),
                null,
                null);
        if(result == JOptionPane.OK_OPTION) {
            return JFileChooser.APPROVE_OPTION;
        }
        return JFileChooser.CANCEL_OPTION;
    }
}
