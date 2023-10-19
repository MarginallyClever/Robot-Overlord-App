package com.marginallyclever.robotoverlord.swing.entitytreepanel;

import com.marginallyclever.robotoverlord.entity.Entity;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * This version of DefaultMutableTreeNode exists only to override the toString() method.
 * @author Dan Royer
 *
 */
public class EntityTreeNode extends DefaultMutableTreeNode {
	public EntityTreeNode(Entity obj) {
		super(obj);
	}

	@Override
	public String toString() {
		return getEntity().getName();
	}

	public Entity getEntity() {
		return (Entity)userObject;
	}
}
