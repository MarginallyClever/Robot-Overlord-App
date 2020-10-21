package com.marginallyclever.robotOverlord.swingInterface.entityTreePanel;

import javax.swing.tree.DefaultMutableTreeNode;

import com.marginallyclever.robotOverlord.entity.Entity;

/**
 * This version of DefaultMutableTreeNode exists only to override the toString() method.
 * @author Dan Royer
 *
 */
public class EntityTreeNode extends DefaultMutableTreeNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1666539321833323211L;

	public EntityTreeNode(Entity e) {
		super(e);
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return ((Entity)userObject).getName();
	}
}
