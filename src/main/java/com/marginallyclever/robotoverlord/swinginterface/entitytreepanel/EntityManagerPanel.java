package com.marginallyclever.robotoverlord.swinginterface.entitytreepanel;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.SceneChangeListener;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import com.marginallyclever.robotoverlord.swinginterface.EditorAction;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.actions.*;
import com.marginallyclever.robotoverlord.swinginterface.edits.SelectEdit;

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
public class EntityManagerPanel extends JPanel implements TreeSelectionListener, SceneChangeListener {
	private final JTree myTree = new JTree();
	private final List<EntityTreePanelListener> listeners = new ArrayList<>();
	private final List<AbstractAction> actions = new ArrayList<>();
	private JPopupMenu popupMenu = null;


	public EntityManagerPanel() {
		super(new BorderLayout());

		myTree.setShowsRootHandles(true);
		myTree.addTreeSelectionListener(this);
		myTree.removeAll();
		myTree.setModel(new DefaultTreeModel(null));
		myTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		addMouseListener();
		addExpansionListener();

		myTree.setDragEnabled(true);
		myTree.setDropMode(DropMode.ON_OR_INSERT);
		myTree.setTransferHandler(new EntityTreeTransferHandler());

		addEntityTreePanelListener((e)-> {
			if (e.eventType == EntityTreePanelEvent.SELECT) {
				UndoSystem.addEvent(this,new SelectEdit(Clipboard.getSelectedEntities(),e.subjects));
			}
		});

		JScrollPane scroll = new JScrollPane();
		scroll.setViewportView(myTree);
		this.add(scroll,BorderLayout.CENTER);
		this.add(createMenu(),BorderLayout.NORTH);
	}

	private JComponent createMenu() {
		JToolBar menu = new JToolBar();

		EntityAddChildAction entityAddAction = new EntityAddChildAction(this);
		EntityCopyAction entityCopyAction = new EntityCopyAction();
		EntityPasteAction entityPasteAction = new EntityPasteAction();
		EntityDeleteAction entityDeleteAction = new EntityDeleteAction();
		EntityCutAction entityCutAction = new EntityCutAction(entityDeleteAction, entityCopyAction);
		EntityRenameAction entityRenameAction = new EntityRenameAction(this);

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

			myTree.setSelectionPaths(paths);
		} else {
			myTree.clearSelection();
		}
	}

	private EntityTreeNode findTreeNode(Entity e) {
		EntityTreeNode root = ((EntityTreeNode)myTree.getModel().getRoot());
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
				System.out.println("findTreeNode problem @ "+treeNode);
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
					myTree.expandPath(path);
					// only expand children if the parent is also expanded.
					list.addAll(Collections.list(n.children()));
				} else {
					myTree.collapsePath(path);
				}
			}
		}
	}

    /**
	 * List all objects in scene.  Click an item to load its {@link com.marginallyclever.robotoverlord.swinginterface.ComponentPanel}.
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
			myTree.setModel(new DefaultTreeModel(newNode));
			setNodeExpandedState((EntityTreeNode)myTree.getModel().getRoot());
		}
	}

	public void removeEntity(Entity entity) {
		EntityTreeNode node = findTreeNode(entity);
		if(node!=null) {
			EntityTreeNode parent = (EntityTreeNode)node.getParent();
			if(parent!=null) {
				parent.remove(node);
			} else {
				myTree.setModel(new DefaultTreeModel(null));
			}
		}
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
					if(e.getClickCount()==2){
						// TODO rename in place for convenience
					}
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

	// TreeSelectionListener event
	@Override
	public void valueChanged(TreeSelectionEvent arg0) {
		List<Entity> selected = new ArrayList<>();
		TreePath[] paths = myTree.getSelectionPaths();
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

	public void setPopupMenu(JPopupMenu abContainer) {
		popupMenu = abContainer;
	}

	@Override
	public void addEntityToParent(Entity parent, Entity child) {
		EntityTreeNode parentNode = findTreeNode(parent);
		if(parentNode!=null) {
			recursivelyAddChildren(parentNode,child);

			((DefaultTreeModel)myTree.getModel()).reload(parentNode);
			setNodeExpandedState((EntityTreeNode)myTree.getModel().getRoot());
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
	public void removeEntityFromParent(Entity parent, Entity child) {
		EntityTreeNode parentNode = findTreeNode(parent);
		EntityTreeNode childNode = findTreeNode(child);
		if(parentNode!=null && childNode!=null) {
			parentNode.remove(childNode);
			((DefaultTreeModel)myTree.getModel()).reload(parentNode);
			setNodeExpandedState((EntityTreeNode)myTree.getModel().getRoot());
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
