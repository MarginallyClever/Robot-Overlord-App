package com.marginallyclever.ro3.mesh;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.factories.Lifetime;
import com.marginallyclever.ro3.listwithevents.ListListener;
import com.marginallyclever.ro3.texture.TextureFactoryDialog;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.util.Objects;

/**
 * <p>The {@link MeshChooserDialog} class allows for selecting a {@link com.marginallyclever.ro3.mesh.Mesh}
 * that has been previously loaded by the {@link MeshFactory}.
 * This class also provides access to the {@link MeshFactoryDialog} for loading additional meshes.</p>
 * <p>TODO In the future it would be nice to count references and unload it when no longer needed.</p>
 */
public class MeshChooserDialog extends JPanel implements ListListener<Mesh> {
    private final DefaultListModel<Mesh> model = new DefaultListModel<>();
    private final JList<Mesh> list = new JList<>();
    private final JToolBar toolBar = new JToolBar();
    private Mesh selectedItem;

    public MeshChooserDialog() {
        super(new BorderLayout());
        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

        setupToolbar();
        setupList();

        add(toolBar, BorderLayout.NORTH);
        add(new JScrollPane(list), BorderLayout.CENTER);
    }

    public void setSelectedItem(Mesh mesh) {
        selectedItem = mesh;
        if(mesh==null) {
            list.clearSelection();
        } else {
            list.setSelectedValue(mesh, true);
        }
    }

    private void setupToolbar() {
        var loadButton = new JButton("Load");
        loadButton.setToolTipText("Load from a file.");
        loadButton.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource(
                "/com/marginallyclever/ro3/apps/actions/icons8-load-16.png"))));
        loadButton.addActionListener(e-> runFactoryDialog((JComponent)e.getSource()));
        toolBar.add(loadButton);


        var clearButton = new JButton("Clear");
        clearButton.setToolTipText("Choose none.");
        clearButton.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource(
                "/com/marginallyclever/ro3/apps/icons8-reset-16.png"))));
        clearButton.addActionListener(_ -> setSelectedItem(null));
        toolBar.add(clearButton);

        // TODO implement different view types.
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

    private void runFactoryDialog(JComponent parent) {
        MeshFactoryDialog meshFactoryDialog = new MeshFactoryDialog();
        int result = meshFactoryDialog.run(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            Mesh mesh = meshFactoryDialog.getMesh();
            setSelectedItem(mesh);
        }
    }

    private void setupList() {
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Mesh mesh = (Mesh) value;
                String shortName = mesh.getSourceName();
                if(shortName.length()>30) {
                    shortName = "..." + shortName.substring(shortName.length()-27);
                }
                String text = shortName + " (" + mesh.getNumVertices() + " vertices)";
                return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
            }
        });

        for (Mesh mesh : Registry.meshFactory.getResources(Lifetime.SCENE)) {
            model.addElement(mesh);
        }
        list.setModel(model);
        list.setSelectedValue(selectedItem, true);
        list.addListSelectionListener(_ -> selectedItem = list.getSelectedValue());
    }

    @Override
    public void addNotify() {
        super.addNotify();
        Registry.meshFactory.addItemListener(this);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        Registry.meshFactory.removeItemListener(this);
    }

    public Mesh getSelectedItem() {
        return selectedItem;
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
     * Run the selection as a dialog.
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
                null,
                null,
                null);
        if(result == JOptionPane.OK_OPTION) {
            return JFileChooser.APPROVE_OPTION;
        }
        return JFileChooser.CANCEL_OPTION;
    }
}
