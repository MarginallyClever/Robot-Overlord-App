package com.marginallyclever.robotOverlord.swingInterface.view;

import java.util.ArrayList;

import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.robotOverlord.entity.*;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.BooleanEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.ColorEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.IntEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.StringEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.Vector3dEntity;

/**
 * An interface for a factory that produces GUI elements.
 * 
 * An outside force chooses a View, then sends it Entity.getView().  The View's 
 * job is to separate the model ("display a boolean") from a specific implementation (like Swing)
 * 
 * @author Dan Royer
 * @since 1.6.0
 */
public interface View {
	public void addReadOnly(String s);
	
	public void addReadOnly(Entity e);

	public void addBoolean(BooleanEntity e);

	public void addEnum(IntEntity e,String [] listOptions);

	public void addFilename(StringEntity e,ArrayList<FileNameExtensionFilter> f);

	public void addColor(ColorEntity colorEntity);

	public void addVector3(Vector3dEntity vector3dEntity);

	public void addInt(IntEntity intEntity);

	public void addDouble(DoubleEntity intEntity);
}
