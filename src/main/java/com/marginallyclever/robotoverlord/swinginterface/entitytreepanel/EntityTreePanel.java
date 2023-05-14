package com.marginallyclever.robotoverlord.swinginterface.entitytreepanel;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.EntityManager;
import com.marginallyclever.robotoverlord.EntityManagerEvent;
import com.marginallyclever.robotoverlord.EntityManagerListener;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import com.marginallyclever.robotoverlord.swinginterface.EditorAction;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.actions.*;
import com.marginallyclever.robotoverlord.swinginterface.edits.SelectEdit;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentManagerPanel;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Uses an Observer Pattern to tell subscribers about changes using EntityTreePanelEvent.
 * @author Dan Royer
 *
 */
public class EntityTreePanel extends JPanel implements TreeSelectionListener, EntityManagerListener {
	private final JTree tree = new JTree();
	private final DefaultTreeModel treeModel = new EntityTreeModel(null);
	private final DefaultTreeCellRenderer treeCellRenderer = new FullNameTreeCellRenderer();
	private final List<EntityTreePanelListener> listeners = new ArrayList<>();
	private final List<AbstractAction> actions = new ArrayList<>();
	private final EntityManager entityManager;

	public EntityTreePanel(EntityManager entityManager) {
		super(new BorderLayout());
		this.entityManager = entityManager;

		tree.setShowsRootHandles(true);
		tree.addTreeSelectionListener(this);
		tree.setCellRenderer(treeCellRenderer);
		tree.setCellEditor(new EntityTreeCellEditor(tree,treeCellRenderer));
		tree.setEditable(true);
		tree.setModel(treeModel);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		tree.setDragEnabled(true);
		tree.setDropMode(DropMode.ON_OR_INSERT);
		tree.setTransferHandler(new EntityTreeTransferHandler(entityManager));

		addMouseListener();
		addExpansionListener();

		addEntityTreePanelListener((e)-> {
			if (e.eventType == EntityTreePanelEvent.SELECT) {
				UndoSystem.addEvent(this,new SelectEdit(Clipboard.getSelectedEntities(),e.subjects));
			}
		});

		JScrollPane scroll = new JScrollPane();
		scroll.setViewportView(tree);
		this.add(scroll,BorderLayout.CENTER);
		this.add(createMenu(),BorderLayout.NORTH);

		addTreeModelListener();

		addEntity(entityManager.getRoot());
		entityManager.addListener(this);
	}

	private void addTreeModelListener() {
		treeModel.addTreeModelListener(new TreeModelListener() {
			@Override
			public void treeNodesChanged(TreeModelEvent e) {
				// find the Entity associated with this node and rename the entity.
				TreeNode node = (TreeNode) e.getTreePath().getLastPathComponent();
				if (node instanceof EntityTreeNode) {
					EntityTreeNode etn = (EntityTreeNode) node;
					etn.getEntity().setName(etn.toString());
				}
			}

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

			@Override
			public void treeNodesRemoved(TreeModelEvent e) {
				// find the Entity associated with this node, remove it from the scene.
				TreeNode node = (TreeNode) e.getTreePath().getLastPathComponent();
				if (node instanceof EntityTreeNode) {
					Entity child = ((EntityTreeNode) node).getEntity();
					Entity parent = child.getParent();
					entityManager.removeEntityFromParent(child,parent);
				}
			}

			@Override
			public void treeStructureChanged(TreeModelEvent e) {
				Object [] list = e.getPath();
				if(list.length==1) {
					if(treeModel.getRoot() != list[0]) {
						Entity parent = ((EntityTreeNode) list[0]).getEntity();
						Entity child =  ((EntityTreeNode) e.getTreePath().getLastPathComponent()).getEntity();
						entityManager.addEntityToParent(child,parent);
					}
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

			Entity e = (Entity)n.getUserObject();
			if(!n.isLeaf()) {
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

	private void addMouseListener() {
		// clicking on empty part of tree unselects the rest.
		// https://coderanch.com/t/518163/java/Deselect-nodes-JTree-user-clicks
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				int row = tree.getRowForLocation(e.getX(), e.getY());
				if (row == -1) {
					// When user clicks on the "empty surface"
					tree.clearSelection();
				} else {
					tree.setSelectionRow(row);
					if(e.getClickCount()==2) {
						TreePath path = tree.getPathForLocation(e.getX(), e.getY());
						if (path != null) {
							tree.startEditingAtPath(path);
						}
					}
				}
			}
		});
	}

	private void addExpansionListener() {
		tree.addTreeWillExpandListener(new TreeWillExpandListener() {
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

	// TreeSelectionListener event
	@Override
	public void valueChanged(TreeSelectionEvent arg0) {
		List<Entity> selected = new ArrayList<>();
		TreePath[] paths = tree.getSelectionPaths();
		if(paths!=null) {
			for (TreePath p : paths) {
				EntityTreeNode node = (EntityTreeNode) p.getLastPathComponent();
				Entity entity = (node == null) ? null : (Entity) node.getUserObject();
				selected.add(entity);
			}
		}
		updateListeners(new EntityTreePanelEvent(EntityTreePanelEvent.SELECT,this,selected));
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

	private void recursivelyAddChildren(EntityTreeNode parentNode, Entity child) {
		EntityTreeNode newNode = new EntityTreeNode(child);
		parentNode.add(newNode);
		for(Entity child2 : child.getChildren()) {
			recursivelyAddChildren(newNode,child2);
		}
	}

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
	 * Tell all Actions to check if they are active.
	 */
	public void updateActionEnableStatus() {
		for(AbstractAction a : actions) {
			if(a instanceof EditorAction) {
				((EditorAction)a).updateEnableStatus();
			}
		}
	}
}
