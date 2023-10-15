package com.marginallyclever.robotoverlord.swing.componentmanagerpanel;

import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.swing.entitytreepanel.EntityTreeCellRenderer;

import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A dialog that allows the user to select one or more entities from the {@link EntityManager} tree.
 *
 * @author Dan Royer
 * @since 2.5.6
 */
public class EntityChooser extends JDialog {
    private final JTree tree;
    private final List<Entity> selectedEntities;

    public EntityChooser(JFrame frame, Entity rootEntity, boolean selectOnlyOne) {
        super(frame, "Entity Chooser", true);

        selectedEntities = new ArrayList<>();

        // Create the tree model and tree UI
        DefaultMutableTreeNode rootNode = createTreeNode(rootEntity);
        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);
        if (selectOnlyOne) {
            tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        } else {
            tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        }
        tree.setCellRenderer(new EntityTreeCellRenderer());
        JScrollPane treeScrollPane = new JScrollPane(tree);

        // Create the OK and Cancel buttons
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // Set the layout and add the components
        setLayout(new BorderLayout());
        add(treeScrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();

        this.setLocationRelativeTo(frame);
    }

    private DefaultMutableTreeNode createTreeNode(Entity entity) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(entity);
        for (Entity child : entity.getChildren()) {
            node.add(createTreeNode(child));
        }
        return node;
    }

    private void onOK() {
        selectedEntities.clear();
        TreePath[] list = tree.getSelectionPaths();
        if (list!=null) {
            for (TreePath treePath : list) {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) treePath.getLastPathComponent();
                selectedEntities.add((Entity) selectedNode.getUserObject());
            }
        }
        setVisible(false);
    }

    private void onCancel() {
        setVisible(false);
    }

    public List<Entity> getSelectedEntities() {
        return selectedEntities;
    }

    public static void main(String[] args) {
        EntityManager entityManager = createSampleEntityHierarchy();
        Entity rootEntity = entityManager.getRoot();
        EntityChooser entityChooser = new EntityChooser(null, rootEntity, false);
        entityChooser.setVisible(true);
        System.out.println("Selected entities: " + entityChooser.getSelectedEntities());
        System.exit(0);
    }

    public static List<Entity> runDialog(JFrame frame, Entity rootEntity, boolean selectOnlyOne) {
        EntityChooser entityChooser = new EntityChooser(frame, rootEntity, selectOnlyOne);
        entityChooser.setVisible(true);
        return entityChooser.getSelectedEntities();
    }

    private static EntityManager createSampleEntityHierarchy() {
        EntityManager entityManager = new EntityManager();
        Entity rootEntity = new Entity();
        Entity child1 = new Entity();
        Entity child2 = new Entity();
        Entity grandchild1 = new Entity();
        Entity grandchild2 = new Entity();
        entityManager.addEntityToParent(child1,rootEntity);
        entityManager.addEntityToParent(child2,rootEntity);
        entityManager.addEntityToParent(grandchild1,child1);
        entityManager.addEntityToParent(grandchild2,child1);
        return entityManager;
    }
}
