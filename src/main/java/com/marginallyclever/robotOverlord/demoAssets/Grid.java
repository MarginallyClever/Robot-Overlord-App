package com.marginallyclever.robotOverlord.demoAssets;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.PoseEntity;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;
import com.marginallyclever.robotOverlord.uiExposedTypes.ColorEntity;
import com.marginallyclever.robotOverlord.uiExposedTypes.IntEntity;

public class Grid extends PoseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5329188355998127781L;
	public IntEntity width = new IntEntity("Width (cm)",100);
	public IntEntity height = new IntEntity("Height (cm)",100);
	public ColorEntity color = new ColorEntity("Color",0,0,0,1);
	
	public Grid() {
		super();
		setName("Grid");
		
		addChild(width);
		addChild(height);
		addChild(color);
	}
	
	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2, pose);
		gl2.glDisable(GL2.GL_TEXTURE_2D);
		gl2.glDisable(GL2.GL_LIGHTING);
		gl2.glColor4d(color.getR(), color.getG(), color.getB(), color.getA());
		PrimitiveSolids.drawGrid(gl2,width.get(),height.get(),1);
		gl2.glPopMatrix();
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Gr", "Grid");
		super.getViewOfChildren(view);
		view.popStack();
		super.getView(view);
	}
}
