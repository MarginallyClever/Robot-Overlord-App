package com.marginallyclever.robotOverlord.swingInterface.view;

import java.util.ArrayList;

import javax.swing.JComponent;
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
	// Views may be made of subsections, like tabs.  This starts a new section.
	void pushStack(String title,String tip);
	
	// Views may be made of subsections, like tabs.  This ends a section.  
	void popStack();
	
	// Finally, get the root component at the end of the whole process.
	JComponent getFinalView();
	
	void addReadOnly(String s);
	
	void addReadOnly(Entity e);

	void addBoolean(BooleanEntity e);

	void addEnum(IntEntity e,String [] listOptions);

	void addFilename(StringEntity e,ArrayList<FileNameExtensionFilter> f);

	void addColor(ColorEntity colorEntity);

	void addVector3(Vector3dEntity vector3dEntity);

	void addInt(IntEntity intEntity);

	void addDouble(DoubleEntity intEntity);
}
