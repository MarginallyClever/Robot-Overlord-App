package com.marginallyclever.robotoverlord.swing.edits;

import com.marginallyclever.robotoverlord.parameters.AbstractParameter;

import javax.vecmath.Vector3d;

/**
 * Undoable action to select a Vector3d.
 * <p>
 * Some Entities have Vector3d (x,y,z) parameters.  This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 *
 */
public class Vector3dParameterEdit extends AbstractParameterEdit<Vector3d> {
	public Vector3dParameterEdit(AbstractParameter<Vector3d> e, Vector3d newValue) {
		super(e, newValue);
	}
}
