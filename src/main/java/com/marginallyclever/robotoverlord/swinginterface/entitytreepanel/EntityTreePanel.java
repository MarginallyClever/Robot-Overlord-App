package com.marginallyclever.robotoverlord.swinginterface.entitytreepanel;

import com.marginallyclever.robotoverlord.AbstractEntity;
import com.marginallyclever.robotoverlord.Entity;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Uses an Observer Pattern to tell subscribers about changes using EntityTreePanelEvent.
 * @author Dan Royer
 *
 */
public class EntityTreePanel extends JPanel implements TreeSelectionListener {
	private final JTree myTree = new JTree();
	private final List<EntityTreePanelListener> listeners = new ArrayList<>();
	private JPopupMenu popupMenu = null;

	public EntityTreePanel(boolean allowMultiSelect) {
		super(new BorderLayout());

		JScrollPane scroll = new JScrollPane();
		this.add(scroll,BorderLayout.CENTER);
		scroll.setViewportView(myTree);

		myTree.getSelectionModel().setSelectionMode(allowMultiSelect
				? TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION
				: TreeSelectionModel.SINGLE_TREE_SELECTION);
		myTree.setShowsRootHandles(true);
		myTree.addTreeSelectionListener(this);

		addMouseListener();
		addExpansionListener();
	}

	/**
	 * Set the selection list to one entity.
	 * @param one the entity to select
	 */
	public void setSelection(Entity one) {
		List<Entity> newSelectionList = new ArrayList<>();
		if(one!=null) newSelectionList.add(one);
		setSelection(newSelectionList);
	}

	/**
	 * Set the selection to the given list of entities.
	 * @param newSelectionList the list of entities to select
	 */
	public void setSelection(List<Entity> newSelectionList) {
		ArrayList<TreePath> pathList = new ArrayList<>();
		for(Entity e : newSelectionList) {
			EntityTreeNode node = findTreeNode(e);
			if(node!=null) {
				pathList.add(new TreePath(node.getPath()));
			}
		}

		TreePath[] paths = new TreePath[pathList.size()];
		pathList.toArray(paths);

		myTree.setSelectionPaths(paths);
	}

	private EntityTreeNode findTreeNode(Entity e) {
		List<TreeNode> list = Collections.list(((EntityTreeNode)myTree.getModel().getRoot()).children());
		for (TreeNode treeNode : list) {
			EntityTreeNode node = (EntityTreeNode)treeNode;
			if(e==node.getUserObject()) {
				return node;
			}
		}
		return null;
	}

	/**
	 * Recursively expand or collapse this node and all child nodes.
	 */
	private void setNodeExpandedState(EntityTreeNode node) {
		ArrayList<TreeNode> list = Collections.list(node.children());
		for(TreeNode treeNode : list) {
			setNodeExpandedState((EntityTreeNode)treeNode);
		}

		TreePath path = new TreePath(node.getPath());
		Entity e = (Entity)node.getUserObject();
		if(e.getExpanded()) {
			myTree.expandPath(path);
		} else {
			myTree.collapsePath(path);
		}
	}

    /**
	 * List all objects in scene.  Click an item to load its {@link com.marginallyclever.robotoverlord.swinginterface.ComponentPanel}.
	 * See <a href="https://docs.oracle.com/javase/7/docs/api/javax/swing/JTree.html">JTree</a>
	 */
	public void update(Entity scene) {
		myTree.removeAll();
		EntityTreeNode newRoot = createTreeNodes(scene);
		myTree.setModel(new DefaultTreeModel(newRoot));
		setNodeExpandedState(newRoot);
	}

	private void addMouseListener() {
		// clicking on empty part of tree unselects the rest.
		// https://coderanch.com/t/518163/java/Deselect-nodes-JTree-user-clicks
		myTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);

				int row = myTree.getRowForLocation(e.getX(), e.getY());
				if (row == -1) {
					// When user clicks on the "empty surface"
					myTree.clearSelection();
				} else {
					myTree.setSelectionRow(row);
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				maybePopup(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				super.mouseReleased(e);
				maybePopup(e);
			}

			private void maybePopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					if (popupMenu != null) popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
	}

	private void addExpansionListener() {
		myTree.addTreeWillExpandListener(new TreeWillExpandListener() {
			@Override
			public void treeWillExpand(TreeExpansionEvent event) {
				EntityTreeNode node = (EntityTreeNode)event.getPath().getLastPathComponent();
				Entity e = (Entity)node.getUserObject();
				e.setExpanded(true);
			}

			@Override
			public void treeWillCollapse(TreeExpansionEvent event) {
				EntityTreeNode node = (EntityTreeNode)event.getPath().getLastPathComponent();
				Entity e = (Entity)node.getUserObject();
				e.setExpanded(false);
			}
		});
	}

	public EntityTreeNode createTreeNodes(Entity e) {
		EntityTreeNode parent = new EntityTreeNode(e);
		for( Entity child : e.getEntities() ) {
			//if(!child.getChildren().isEmpty())
			if(!(child instanceof AbstractEntity)) {
				parent.add(createTreeNodes(child));
			}
		}
		return parent;
	}

	// TreeSelectionListener event
	@Override
	public void valueChanged(TreeSelectionEvent arg0) {
		List<Entity> added = new ArrayList<>();
		List<Entity> removed = new ArrayList<>();
		
		for(TreePath p : arg0.getPaths()) {
			EntityTreeNode node = (EntityTreeNode)p.getLastPathComponent();
			Entity e = (node==null) ? null : (Entity)node.getUserObject();
			if(arg0.isAddedPath(p)) {
				added.add(e);
			} else {
				removed.add(e);
			}
		}

		updateListeners(new EntityTreePanelEvent(EntityTreePanelEvent.UNSELECT,this,removed));
		updateListeners(new EntityTreePanelEvent(EntityTreePanelEvent.SELECT,this,added));
	}
	
	public void addEntityTreePanelListener(EntityTreePanelListener arg0) {
		listeners.add(arg0);
	}
	
	public void removeEntityTreePanelListener(EntityTreePanelListener arg0) {
		listeners.remove(arg0);
	}
	
	private void updateListeners(EntityTreePanelEvent event) {
		for( EntityTreePanelListener e : listeners ) {
			e.entityTreePanelEvent(event);
		}
	}

	public void setPopupMenu(JPopupMenu abContainer) {
		popupMenu = abContainer;
	}
}
