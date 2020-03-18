package com.marginallyclever.robotOverlord.swingInterface;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;

public class EntityTreePanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected RobotOverlord ro;
	
	public EntityTreePanel(RobotOverlord ro) {
		super();
		this.ro=ro;
		
		setLayout(new BorderLayout());
		updateEntityTree();
	}

    /**
     * list all entities in the world.  Double click an item to get its panel.
     * See https://docs.oracle.com/javase/7/docs/api/javax/swing/JTree.html
     */
	public void updateEntityTree() {
		// list all objects in scene
	    DefaultMutableTreeNode top = createTreeNodes(ro);
		JTree tree = new JTree(top);

	    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	    tree.setShowsRootHandles(true);
	    tree.addMouseListener(new MouseAdapter() {
		    public void mousePressed(MouseEvent e) {
		        TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
		        if(selPath != null) {
		            if(e.getClickCount() == 1) {
		                //mySingleClick(selRow, selPath);
		            	DefaultMutableTreeNode o = (DefaultMutableTreeNode)selPath.getLastPathComponent();
		            	ro.pickEntity((Entity)(o.getUserObject()));
		            } else if(e.getClickCount() == 2) {
		                //myDoubleClick(selRow, selPath);
		            }
		        }
		    }
		});
		//tree.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		
		if(getComponentCount()==1) {
			JTree oldTree = (JTree)getComponent(0);
			// preserve the original expansions
			ArrayList<TreePath> expanded = new ArrayList<TreePath>();
			for(int i=0;i<oldTree.getRowCount();++i) {
				if(oldTree.isExpanded(i)) {
					expanded.add(oldTree.getPathForRow(i));
				}
			}
			// restore the expanded paths
			for(TreePath p : expanded) {
				tree.expandPath(p);
			}
			// restore the selected paths
			TreePath[] paths = oldTree.getSelectionPaths();
			tree.setSelectionPaths(paths);
		}
		
		removeAll();
		add(tree,BorderLayout.CENTER);
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
}
