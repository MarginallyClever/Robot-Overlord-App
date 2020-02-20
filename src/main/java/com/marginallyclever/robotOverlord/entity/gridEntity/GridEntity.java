package com.marginallyclever.robotOverlord.entity.gridEntity;

import java.util.ArrayList;

import javax.swing.JPanel;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.EntityControlPanel;
import com.marginallyclever.robotOverlord.entity.physicalObject.PhysicalObject;

public class GridEntity extends PhysicalObject {
	public double width=100;
	public double height=100;
	
	protected transient GridEntityControlPanel gridEntityControlPanel;

	public GridEntity() {
		super();
		setDisplayName("Grid");
	}
	
	
	/**
	 * Get the {@link EntityControlPanel} for this class' superclass, then the EntityPanel for this class, and so on.
	 * 
	 * @param gui the main application instance.
	 * @return the list of EntityPanels 
	 */
	public ArrayList<JPanel> getContextPanel(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getContextPanel(gui);
		if(list==null) list = new ArrayList<JPanel>();
		
		gridEntityControlPanel = new GridEntityControlPanel(gui,this);
		list.add(gridEntityControlPanel);

		return list;
	}

	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2, this.matrix);
		PrimitiveSolids.drawGrid(gl2,(int)width,(int)height,1);
		gl2.glPopMatrix();
	}
}
