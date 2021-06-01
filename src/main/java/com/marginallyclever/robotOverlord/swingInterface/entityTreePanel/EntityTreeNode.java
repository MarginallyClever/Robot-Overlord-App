package com.marginallyclever.robotOverlord.swingInterface.entityTreePanel;

import javax.swing.tree.DefaultMutableTreeNode;

import com.marginallyclever.robotOverlord.Entity;

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
