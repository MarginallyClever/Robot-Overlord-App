package com.marginallyclever.robotoverlord.swinginterface.entitytreepanel;

import com.marginallyclever.robotoverlord.Entity;

import javax.swing.tree.DefaultMutableTreeNode;

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
		return ((Entity)userObject).getName();
	}
}
