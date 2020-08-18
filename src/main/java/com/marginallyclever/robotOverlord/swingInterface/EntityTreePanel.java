package com.marginallyclever.robotOverlord.swingInterface;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.marginallyclever.robotOverlord.entity.Entity;

/**
 * Uses an Observer Pattern to tell subscribers about changes using EntityTreePanelEvent.
 * @author Dan Royer
 *
 */
public class EntityTreePanel extends JPanel implements TreeSelectionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected Entity root;  // root of entity tree
	protected Entity selected;
	
    protected DefaultMutableTreeNode oldTop; 
	protected JTree oldTree;
	protected JScrollPane scroll = new JScrollPane();

	private List<EntityTreePanelListener> listeners = new ArrayList<EntityTreePanelListener>();
	
	public EntityTreePanel(Entity root) {
		super();
		this.root = root;
		this.setLayout(new BorderLayout());
		this.add(scroll,BorderLayout.CENTER);
		updateEntityTree();
	}

	public Entity getSelected() {
		return selected;
	}
	
	public void setSelection(Entity e) {
		if(oldTree==null) return;
		if(selected==e) return;

		// preserve the original expansions
		LinkedList<DefaultMutableTreeNode> list = new LinkedList<DefaultMutableTreeNode>();
		list.add(oldTop);
		while( !list.isEmpty() ) {
			DefaultMutableTreeNode node = list.remove();
			Object o=node.getUserObject();
			if( o instanceof Entity ) {
				if( (Entity)o == e ) {
					TreePath selectedPath = new TreePath(node.getPath());
					oldTree.expandPath(selectedPath);
					oldTree.setSelectionPath(selectedPath);
				}
			}
			for(int i=0; i<node.getChildCount(); ++i ) {
				DefaultMutableTreeNode child = (DefaultMutableTreeNode)node.getChildAt(i);
				list.add(child);
			}
		}
	}
	

	// from https://www.logicbig.com/tutorials/java-swing/jtree-expand-collapse-all-nodes.html
	public static void setNodeExpandedState(JTree tree, DefaultMutableTreeNode node, boolean expanded) {
		TreePath path = new TreePath(node.getPath());
		if (expanded) {
			tree.expandPath(path);
		} else {
			tree.collapsePath(path);
		}
		
		@SuppressWarnings("unchecked")
		ArrayList<DefaultMutableTreeNode> list = Collections.list(node.children());
		for (DefaultMutableTreeNode treeNode : list) {
			setNodeExpandedState(tree, treeNode, expanded);
		}
		if (!expanded && node.isRoot()) {
			return;
		}
	}

    /**
     * list all entities in the world.  Double click an item to get its panel.
     * See https://docs.oracle.com/javase/7/docs/api/javax/swing/JTree.html
     */
	public void updateEntityTree() {
		// list all objects in scene
	    DefaultMutableTreeNode newTop = createTreeNodes(root);
		JTree newTree = new JTree(newTop);

	    newTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	    newTree.setShowsRootHandles(false);
	    newTree.addTreeSelectionListener(this);
		//tree.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

		if(oldTree!=null) {
			setNodeExpandedState(newTree,(DefaultMutableTreeNode)newTree.getModel().getRoot(),true);
			// preserve the original expansions
			for(int i=0;i<oldTree.getRowCount();++i) {
				if(!oldTree.isExpanded(i)) {
					//Log.message("Collapsing path " + oldTree.getPathForRow(i) + ":"+(oldTree.isExpanded(i)?"o":"x"));
					TreePath p0 = oldTree.getPathForRow(i);
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
			TreePath[] paths = oldTree.getSelectionPaths();
			newTree.setSelectionPaths(paths);
		}
		
		if(oldTree != null) this.remove(oldTree);
		scroll.setViewportView(newTree);
		
		oldTree=newTree;
		oldTop =newTop;
	}

	// This is a ViewTree of the root entity.
	// Only add branches of the tree, ignore all leaves.  leaves *should* be handled by the ViewPanel of a single entity.
	public DefaultMutableTreeNode createTreeNodes(Entity e) {
		DefaultMutableTreeNode parent = new DefaultMutableTreeNode(e);
		for(Entity child : e.getChildren() ) {
			if(!child.getChildren().isEmpty()) {
				parent.add(createTreeNodes(child));
			}
		}
		return parent;
	}

	// TreeSelectionListener event
	@Override
	public void valueChanged(TreeSelectionEvent arg0) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)oldTree.getLastSelectedPathComponent();
		Entity e = (Entity)node.getUserObject();
		selected=e;
		EntityTreePanelEvent event = new EntityTreePanelEvent(EntityTreePanelEvent.SELECT,this,e);
		updateListeners(event);
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
}
