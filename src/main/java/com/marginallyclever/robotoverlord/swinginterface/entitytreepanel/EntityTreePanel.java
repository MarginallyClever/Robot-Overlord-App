package com.marginallyclever.robotoverlord.swinginterface.entitytreepanel;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.marginallyclever.robotoverlord.AbstractEntity;
import com.marginallyclever.robotoverlord.Entity;

/**
 * Uses an Observer Pattern to tell subscribers about changes using EntityTreePanelEvent.
 * @author Dan Royer
 *
 */
public class EntityTreePanel extends JPanel implements TreeSelectionListener {
	@Serial
	private static final long serialVersionUID = 1L;

	protected Entity root;  // root of entity tree
	protected final ArrayList<Entity> selected = new ArrayList<>();
    protected EntityTreeNode treeRoot;
	protected JTree myTree;
	protected JScrollPane scroll = new JScrollPane();
	protected boolean multiSelect=false;
	private final List<EntityTreePanelListener> listeners = new ArrayList<>();

	private JPopupMenu popupMenu = null;

	public EntityTreePanel(boolean allowMultiSelect) {
		super();
		this.multiSelect = allowMultiSelect;
		this.setLayout(new BorderLayout());
		this.add(scroll,BorderLayout.CENTER);
	}

	public ArrayList<Entity> getSelected() {
		return selected;
	}

	public void setSelection(Entity one) {
		ArrayList<Entity> newSelectionList = new ArrayList<Entity>();
		newSelectionList.add(one);
		setSelection(newSelectionList);
	}
	
	public void setSelection(ArrayList<Entity> newSelectionList) {
		if(myTree ==null) return;
		if(selected.equals(newSelectionList)) return;

		ArrayList<TreePath> pathList = new ArrayList<TreePath>();
		
		// preserve the original expansions
		LinkedList<EntityTreeNode> list = new LinkedList<EntityTreeNode>();
		list.add(treeRoot);
		while( !list.isEmpty() ) {
			EntityTreeNode node = list.remove();
			// add children of this node so we scan through all possible nodes.
			for(int i=0; i<node.getChildCount(); ++i ) {
				EntityTreeNode child = (EntityTreeNode)node.getChildAt(i);
				list.add(child);
			}
			
			// does this node match a selected entity?
			Object o=node.getUserObject();
			if( o instanceof Entity ) {
				Entity oe = (Entity)o;
				if( newSelectionList.contains(oe) ) {
					// yes
					pathList.add(new TreePath(node.getPath()));
				}
			}
		}
		
		TreePath[] paths = new TreePath[pathList.size()];
		pathList.toArray(paths);
		myTree.setSelectionPaths(paths);
	}
	

	// expand or collapse all nodes of a tree
	// from https://www.logicbig.com/tutorials/java-swing/jtree-expand-collapse-all-nodes.html
	public static void setNodeExpandedState(JTree tree, EntityTreeNode node, boolean expanded) {
		ArrayList<TreeNode> list = (ArrayList<TreeNode>)Collections.list(node.children());
		for (TreeNode treeNode : list) {
			setNodeExpandedState(tree, (EntityTreeNode)treeNode, expanded);
		}
		
		if (!expanded && node.isRoot()) return;
		
		TreePath path = new TreePath(node.getPath());
		if (expanded) tree.expandPath(path);
		else tree.collapsePath(path);
	}

    /**
	 * list all objects in scene.  Double click an item to get its panel.
	 * See <a href="https://docs.oracle.com/javase/7/docs/api/javax/swing/JTree.html">JTree</a>
	 */
	public void update(Entity scene) {
	    EntityTreeNode newRoot = createTreeNodes(scene);
		JTree newTree = new JTree(newRoot);

		newTree.getSelectionModel().setSelectionMode( multiSelect
				? TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION
				: TreeSelectionModel.SINGLE_TREE_SELECTION );
	    newTree.setShowsRootHandles(true);
	    newTree.addTreeSelectionListener(this);
		//tree.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

		if(myTree !=null) {
			setNodeExpandedState(newTree,(EntityTreeNode)newTree.getModel().getRoot(),true);
			
			// preserve the original expansions
			for(int i = 0; i< myTree.getRowCount(); ++i) {
				if(!myTree.isExpanded(i)) {
					//Log.message("Collapsing path " + oldTree.getPathForRow(i) + ":"+(oldTree.isExpanded(i)?"o":"x"));
					TreePath p0 = myTree.getPathForRow(i);
					String p0s = p0.toString();
					
					//Log.message("Comparing to >"+p0s+"<");
					for(int j=0;j<newTree.getRowCount();++j) {
						TreePath p1 = newTree.getPathForRow(j);
						//Log.message("  Comparing to >"+p1.toString()+"<");
						if(p0s.equals(p1.toString())) {
							//Log.message("Found " + p1.toString());
							newTree.collapsePath(p1);
							break;
						}
					}
				}
			}
			
			// restore the selected paths
			newTree.setSelectionPaths(myTree.getSelectionPaths());
		}
		
		if(myTree != null) this.remove(myTree);
		scroll.setViewportView(newTree);
		
		myTree = newTree;
		treeRoot = newRoot;
		
		// clicking on empty part of tree unselects the rest.
		// https://coderanch.com/t/518163/java/Deselect-nodes-JTree-user-clicks
		myTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				
				int row= myTree.getRowForLocation(e.getX(),e.getY());
			    if(row==-1) {
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
				int row= myTree.getRowForLocation(e.getX(),e.getY());
				if(e.isPopupTrigger()) {
					if(popupMenu!=null) popupMenu.show(e.getComponent(),e.getX(),e.getY());
				}
			}
		});
	}

	// This is a ViewTree of the root entity.
	// Only add branches of the tree, ignore all leaves.  leaves *should* be handled by the ViewPanel of a single entity.
	public EntityTreeNode createTreeNodes(Entity e) {
		EntityTreeNode parent = new EntityTreeNode(e);
		for( Entity child : e.getChildren() ) {
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
		ArrayList<Entity> added = new ArrayList<Entity>();
		ArrayList<Entity> removed = new ArrayList<Entity>();
		
		for(TreePath p : arg0.getPaths()) {
			EntityTreeNode node = (EntityTreeNode)p.getLastPathComponent();
			Entity e = (node==null) ? null : (Entity)node.getUserObject();
			if(arg0.isAddedPath(p)) {
				added.add(e);
			} else {
				removed.add(e);
			}
		}
		
		selected.removeAll(removed);
		selected.addAll(added);

		updateListeners(new EntityTreePanelEvent(EntityTreePanelEvent.UNSELECT,this,removed));
		updateListeners(new EntityTreePanelEvent(EntityTreePanelEvent.SELECT,this,added));
	}
	
	public void addEntityTreePanelListener(EntityTreePanelListener arg0) {
		listeners.add(arg0);
	}
	
	public void removeEntityTreePanelListener(EntityTreePanelListener arg0) {
		listeners.remove(arg0);
	}
	
	protected void updateListeners(EntityTreePanelEvent event) {
		for( EntityTreePanelListener e : listeners ) {
			e.entityTreePanelEvent(event);
		}
	}

	public void setPopupMenu(JPopupMenu abContainer) {
		popupMenu = abContainer;
	}
}
