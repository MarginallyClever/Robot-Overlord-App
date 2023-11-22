package com.marginallyclever.robotoverlord.swing.entitytreepanel;

import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.entity.EntityManagerEvent;
import com.marginallyclever.robotoverlord.entity.EntityManagerListener;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import com.marginallyclever.robotoverlord.swing.EditorAction;
import com.marginallyclever.robotoverlord.swing.UndoSystem;
import com.marginallyclever.robotoverlord.swing.actions.*;
import com.marginallyclever.robotoverlord.swing.edits.SelectEdit;
import com.marginallyclever.robotoverlord.swing.componentmanagerpanel.ComponentManagerPanel;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link EntityTreePanel} provides a UI to view/edit the contents of an {@link EntityManager}.
 * @author Dan Royer
 */
public class EntityTreePanel extends JPanel {
	private final JTree tree = new JTree();
	private final DefaultTreeModel treeModel = new EntityTreeModel(null);
	private final List<AbstractAction> actions = new ArrayList<>();
	private final EntityManager entityManager;

	public EntityTreePanel(EntityManager entityManager) {
		super(new BorderLayout());
		this.setName("EntityTreePanel");
		this.entityManager = entityManager;

		tree.setShowsRootHandles(true);
		DefaultTreeCellRenderer treeCellRenderer = new FullNameTreeCellRenderer();
		tree.setCellRenderer(treeCellRenderer);
		tree.setCellEditor(new EntityTreeCellEditor(tree, treeCellRenderer));
		tree.setEditable(true);
		tree.setModel(treeModel);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		tree.setDragEnabled(true);
		tree.setDropMode(DropMode.ON_OR_INSERT);
		tree.setTransferHandler(new EntityTreeTransferHandler(entityManager));
		JScrollPane scroll = new JScrollPane();
		scroll.setViewportView(tree);
		this.add(scroll, BorderLayout.CENTER);
		this.add(createMenu(), BorderLayout.NORTH);

		addTreeSelectionListener();
		addExpansionListener();
		addTreeModelListener();
		addEntityManagerListener();

		addEntity(entityManager.getRoot());
	}

	private void addEntityManagerListener() {
		entityManager.addListener(new EntityManagerListener() {
			@Override
			public void entityManagerEvent(EntityManagerEvent event) {
				if(event.type == EntityManagerEvent.ENTITY_ADDED) {
					addEntityToParent(event.child,event.parent);
				} else if(event.type == EntityManagerEvent.ENTITY_REMOVED) {
					removeEntityFromParent(event.child,event.parent);
				} else if(event.type == EntityManagerEvent.ENTITY_RENAMED) {
					EntityTreeNode node = findTreeNode(event.child);
					treeModel.reload(node);
				}
			}
		});
	}

	private void addTreeModelListener() {
		treeModel.addTreeModelListener(new TreeModelListener() {
			/**
			 * <p>Invoked after a node (or a set of siblings) has changed in some
			 * way. The node(s) have not changed locations in the tree or
			 * altered their children arrays, but other attributes have
			 * changed and may affect presentation. Example: the name of a
			 * file has changed, but it is in the same location in the file
			 * system.</p>
			 *
			 * <p>To indicate the root has changed, childIndices and children
			 * will be null.</p>
			 *
			 * <p>Use {@code e.getPath()} to get the parent of the changed node(s).
			 * {@code e.getChildIndices()} returns the index(es) of the changed node(s).</p>
			 *
			 * @param e a {@code TreeModelEvent} describing changes to a tree model
			 */
			@Override
			public void treeNodesChanged(TreeModelEvent e) {
				// find the Entity associated with this node and rename the entity.
				TreeNode node = (TreeNode) e.getTreePath().getLastPathComponent();
				if (node instanceof EntityTreeNode) {
					EntityTreeNode etn = (EntityTreeNode) node;
					etn.getEntity().setName(etn.toString());
				}
			}

			/**
			 * <p>Invoked after nodes have been inserted into the tree.</p>
			 *
			 * <p>Use {@code e.getPath()} to get the parent of the new node(s).
			 * {@code e.getChildIndices()} returns the index(es) of the new node(s)
			 * in ascending order.</p>
			 *
			 * @param e a {@code TreeModelEvent} describing changes to a tree model
			 */
			@Override
			public void treeNodesInserted(TreeModelEvent e) {
				for(Object obj : e.getPath()) {
					TreeNode parentNode = (TreeNode) obj;
					if(parentNode instanceof EntityTreeNode) {
						TreeNode node = (TreeNode) e.getTreePath().getLastPathComponent();
						if (node instanceof EntityTreeNode) {
							Entity child = ((EntityTreeNode) node).getEntity();
							Entity parent = child.getParent();
							entityManager.addEntityToParent(child,parent);
						}
					}
				}
			}

			/**
			 * <p>Invoked after nodes have been removed from the tree.  Note that
			 * if a subtree is removed from the tree, this method may only be
			 * invoked once for the root of the removed subtree, not once for
			 * each individual set of siblings removed.</p>
			 *
			 * <p>Use {@code e.getPath()} to get the former parent of the deleted
			 * node(s). {@code e.getChildIndices()} returns, in ascending order, the
			 * index(es) the node(s) had before being deleted.
			 *
			 * @param e a {@code TreeModelEvent} describing changes to a tree model
			 */
			@Override
			public void treeNodesRemoved(TreeModelEvent e) {
				TreeNode node = (TreeNode) e.getTreePath().getLastPathComponent();
				if (node instanceof EntityTreeNode) {
					Entity child = ((EntityTreeNode) node).getEntity();
					Entity parent = child.getParent();
					entityManager.removeEntityFromParent(child,parent);
				}
			}

			/**
			 * <p>Invoked after the tree has drastically changed structure from a
			 * given node down.  If the path returned by e.getPath() is of length
			 * one and the first element does not identify the current root node
			 * the first element should become the new root of the tree.
			 *
			 * <p>Use {@code e.getPath()} to get the path to the node.
			 * {@code e.getChildIndices()} returns null.
			 *
			 * @param e a {@code TreeModelEvent} describing changes to a tree model
			 */
			@Override
			public void treeStructureChanged(TreeModelEvent e) {
				Object [] list = e.getPath();
				if(list.length==1 && treeModel.getRoot() != list[0]) {
					// list[0] should become the new root of the tree.  This should never happen in our case.
					Entity parent = ((EntityTreeNode) list[0]).getEntity();
					Entity child =  ((EntityTreeNode) e.getTreePath().getLastPathComponent()).getEntity();
					entityManager.addEntityToParent(child,parent);
				}
			}
		});
	}

	private JComponent createMenu() {
		JToolBar menu = new JToolBar();

		EntityAddChildAction entityAddAction = new EntityAddChildAction(entityManager);
		EntityCopyAction entityCopyAction = new EntityCopyAction(entityManager);
		EntityPasteAction entityPasteAction = new EntityPasteAction(entityManager);
		EntityDeleteAction entityDeleteAction = new EntityDeleteAction(entityManager);
		EntityCutAction entityCutAction = new EntityCutAction(entityDeleteAction, entityCopyAction);
		EntityRenameAction entityRenameAction = new EntityRenameAction(entityManager);

		menu.add(entityAddAction);
		menu.add(entityDeleteAction);
		menu.add(entityCopyAction);
		menu.add(entityCutAction);
		menu.add(entityPasteAction);
		menu.add(entityRenameAction);

		actions.add(entityCopyAction);
		actions.add(entityPasteAction);
		actions.add(entityCutAction);
		actions.add(entityDeleteAction);
		actions.add(entityRenameAction);

		return menu;
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

		if(pathList.size()>0) {
			TreePath[] paths = new TreePath[pathList.size()];
			pathList.toArray(paths);

			tree.setSelectionPaths(paths);
		} else {
			tree.clearSelection();
		}
	}

	private EntityTreeNode findTreeNode(Entity e) {
		EntityTreeNode root = ((EntityTreeNode)treeModel.getRoot());
		if(root==null) return null;

		List<TreeNode> list = new ArrayList<>();
		list.add(root);
		while(!list.isEmpty()) {
			TreeNode treeNode = list.remove(0);
			if(treeNode instanceof EntityTreeNode) {
				EntityTreeNode node = (EntityTreeNode) treeNode;
				if (e == node.getUserObject()) {
					return node;
				}
			} else {
				System.err.println("findTreeNode problem @ "+treeNode);
			}
			list.addAll(Collections.list(treeNode.children()));
		}
		return null;
	}

	/**
	 * Recursively expand or collapse this node and all child nodes.
	 */
	private void setNodeExpandedState(EntityTreeNode node) {
		List<TreeNode> list = new ArrayList<>();
		list.add(node);

		while(!list.isEmpty()) {
			EntityTreeNode n = (EntityTreeNode)list.remove(0);
			if(!n.isLeaf()) {
				Entity e = (Entity)n.getUserObject();
				TreePath path = new TreePath(n.getPath());
				if (e.getExpanded()) {
					tree.expandPath(path);
					// only expand children if the parent is also expanded.
					list.addAll(Collections.list(n.children()));
				} else {
					tree.collapsePath(path);
				}
			}
		}
	}

    /**
	 * List all objects in scene.  Click an item to load its {@link ComponentManagerPanel}.
	 * See <a href="https://docs.oracle.com/javase/7/docs/api/javax/swing/JTree.html">JTree</a>
	 */

	public void addEntity(Entity me) {
		Entity parentEntity = me.getParent();
		if(parentEntity!=null) {
			EntityTreeNode parentNode = findTreeNode(parentEntity);
			if(parentNode!=null) {
				EntityTreeNode newNode = new EntityTreeNode(me);
				parentNode.add(newNode);
				setNodeExpandedState(parentNode);
			}
		} else {
			EntityTreeNode newNode = new EntityTreeNode(me);
			treeModel.setRoot(newNode);
			setNodeExpandedState((EntityTreeNode) treeModel.getRoot());
		}

		for(Entity child : me.getChildren()) {
			addEntity(child);
		}
	}

	public void removeEntity(Entity entity) {
		EntityTreeNode node = findTreeNode(entity);
		if(node!=null) {
			EntityTreeNode parent = (EntityTreeNode)node.getParent();
			if(parent!=null) {
				parent.remove(node);
			} else {
				treeModel.setRoot(null);
			}
		}
	}

	private void addExpansionListener() {
		tree.addTreeExpansionListener(new TreeExpansionListener() {
			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				EntityTreeNode node = (EntityTreeNode)event.getPath().getLastPathComponent();
				Entity e = (Entity)node.getUserObject();
				e.setExpanded(true);
				setNodeExpandedState((EntityTreeNode)treeModel.getRoot());
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
				EntityTreeNode node = (EntityTreeNode)event.getPath().getLastPathComponent();
				Entity e = (Entity)node.getUserObject();
				e.setExpanded(false);
				setNodeExpandedState((EntityTreeNode)treeModel.getRoot());
			}
		});
	}

	private void addTreeSelectionListener() {
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent arg0) {
				List<Entity> selected = new ArrayList<>();
				TreePath[] selectedPaths = tree.getSelectionPaths();
				if(selectedPaths!=null) {
					for (TreePath selectedPath : selectedPaths) {
						EntityTreeNode selectedNode = (EntityTreeNode) selectedPath.getLastPathComponent();
						Entity entity = (Entity)selectedNode.getUserObject();
						selected.add(entity);
					}
				}
				UndoSystem.addEvent(new SelectEdit(Clipboard.getSelectedEntities(), selected));
			}
		});
	}

	private void recursivelyAddChildren(EntityTreeNode parentNode, Entity child) {
		EntityTreeNode newNode = new EntityTreeNode(child);
		parentNode.add(newNode);
		for(Entity child2 : child.getChildren()) {
			recursivelyAddChildren(newNode,child2);
		}
	}

	private void addEntityToParent(Entity parent, Entity child) {
		EntityTreeNode parentNode = findTreeNode(parent);
		if(parentNode!=null) {
			recursivelyAddChildren(parentNode,child);
			treeModel.reload(parentNode);
			setNodeExpandedState((EntityTreeNode)treeModel.getRoot());
		}
	}

	private void removeEntityFromParent(Entity parent, Entity child) {
		EntityTreeNode parentNode = findTreeNode(parent);
		EntityTreeNode childNode = findTreeNode(child);
		if(parentNode!=null && childNode!=null) {
			parentNode.remove(childNode);
			treeModel.reload(parentNode);
			setNodeExpandedState((EntityTreeNode)treeModel.getRoot());
		}
	}

	/**
	 * Tell all {@link EditorAction}s of this panel to check if they are active.
	 */
	public void updateActionEnableStatus() {
		for(AbstractAction a : actions) {
			if(a instanceof EditorAction) {
				((EditorAction)a).updateEnableStatus();
			}
		}
	}
}
