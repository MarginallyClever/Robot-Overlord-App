package com.marginallyclever.robotOverlord.entity.scene;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.IntEntity;

public class GridEntity extends PoseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3609783682680649075L;
	public IntEntity width = new IntEntity("Width (cm)",100);
	public IntEntity height = new IntEntity("Height (cm)",100);
	
	public GridEntity() {
		super();
		setName("Grid");
		
		addChild(width);
		addChild(height);
	}
	
	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2, this.pose.get());
		PrimitiveSolids.drawGrid(gl2,width.get(),height.get(),1);
		gl2.glPopMatrix();
	}
}
