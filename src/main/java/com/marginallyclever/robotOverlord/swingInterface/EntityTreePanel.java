package com.marginallyclever.robotOverlord.swingInterface;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.swingInterface.commands.CommandAddEntity;
import com.marginallyclever.robotOverlord.swingInterface.commands.CommandRemoveEntity;

public class EntityTreePanel extends JPanel implements TreeSelectionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected RobotOverlord ro;
	
    protected DefaultMutableTreeNode oldTop; 
	protected JTree oldTree;
	protected JButton a,b;
	protected JPanel abContainer;
	protected JScrollPane scroll = new JScrollPane();
	
	public EntityTreePanel(RobotOverlord ro) {
		super();
		this.ro=ro;
		
		a=new JButton(new CommandAddEntity(ro));
		b=new JButton(new CommandRemoveEntity(ro));
		abContainer = new JPanel(new FlowLayout());
		abContainer.add(a);
		abContainer.add(b);
		setLayout(new BorderLayout());
		this.add(abContainer,BorderLayout.NORTH);
		this.add(scroll,BorderLayout.CENTER);
		updateEntityTree();
	}

	public void setSelection(Entity e) {
		if(oldTree==null) return;
		
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
	
    /**
     * list all entities in the world.  Double click an item to get its panel.
     * See https://docs.oracle.com/javase/7/docs/api/javax/swing/JTree.html
     */
	public void updateEntityTree() {
		// list all objects in scene
	    DefaultMutableTreeNode newTop = createTreeNodes(ro);
		JTree newTree = new JTree(newTop);

	    newTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	    newTree.setShowsRootHandles(true);
	    newTree.addTreeSelectionListener(this);
		//tree.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

		if(oldTree!=null) {
			// preserve the original expansions
			ArrayList<TreePath> expanded = new ArrayList<TreePath>();
			for(int i=0;i<oldTree.getRowCount();++i) {
				if(oldTree.isExpanded(i)) {
					expanded.add(oldTree.getPathForRow(i));
				}
			}
			// restore the expanded paths
			for(TreePath p : expanded) {
				newTree.expandPath(p);
			}
			// restore the selected paths
			TreePath[] paths = oldTree.getSelectionPaths();
			newTree.setSelectionPaths(paths);
		}
		
		if(!newTree.equals(oldTree)) {
			scroll.setViewportView(newTree);
			oldTree=newTree;
			oldTop =newTop;
		}
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

	@Override
	public void valueChanged(TreeSelectionEvent arg0) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)oldTree.getLastSelectedPathComponent();
        ro.pickEntity((Entity)(node.getUserObject()));
	}
}
