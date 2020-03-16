package com.marginallyclever.robotOverlord.uiElements.view;

import com.marginallyclever.robotOverlord.entity.*;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.BooleanEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.ColorEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.IntEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.StringEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.Vector3dEntity;

/**
 * An interface for a factory that produces GUI elements
 * @author Dan Royer
 * @since 1.6.0
 */
public interface View {
	public void addReadOnly(String s);
	
	/**
	 * add a field that 
	 * - cannot be edited
	 * - follows the value in the entity.
	 * @param e
	 */
	public void addReadOnly(Entity e);

	public void addBoolean(BooleanEntity e);

	public void addEnum(IntEntity e,String [] listOptions);

	public void addFilename(StringEntity e);

	public void addColor(ColorEntity colorEntity);

	public void addVector3(Vector3dEntity vector3dEntity);

	public void addNumber(IntEntity intEntity);

	public void addNumber(DoubleEntity intEntity);
}
