package com.marginallyclever.robotOverlord.entity.physicalObject.boxObject;


import java.util.ArrayList;

import javax.swing.JPanel;
import javax.vecmath.Point3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.material.Material;
import com.marginallyclever.robotOverlord.entity.material.MaterialPanel;
import com.marginallyclever.robotOverlord.entity.physicalObject.PhysicalObject;

public class BoxObject extends PhysicalObject {
	public double width, height, depth;
	
	protected BoxObjectPanel boxPanel;
	protected MaterialPanel materialPanel;
	
	Material mat;
	
	public BoxObject() {
		super();
		setDisplayName("Box");
		depth=width=height=1;
		mat=new Material();
	}
	

	@Override
	public ArrayList<JPanel> getContextPanel(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getContextPanel(gui);
		if(list==null) list = new ArrayList<JPanel>();
		

		// add the box panel
		boxPanel = new BoxObjectPanel(gui,this);
		list.add(boxPanel);

		// add material panel but do not add entity panel.
		ArrayList<JPanel> list2 = mat.getContextPanel(gui);
		list.add(list2.get(list2.size()-1));
		
		return list;
	}
	
	
	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();

			MatrixHelper.applyMatrix(gl2, this.getMatrix());
		
			// TODO this should probably be an option that can be toggled.
			// It is here to fix scaling of the entire model.  It won't 
			// work when the model is scaled unevenly.
			gl2.glEnable(GL2.GL_NORMALIZE);
	
			// draw placeholder
			mat.render(gl2);
			PrimitiveSolids.drawBox(gl2, (float)depth, (float)width, (float)height);
		
		gl2.glPopMatrix();
	}


	/**
	 * 
	 * @return a list of cuboids, or null.
	 */
	@Override
	public ArrayList<Cuboid> getCuboidList() {
		Point3d _boundBottom = new Point3d(-width/2,-depth/2,0);
		Point3d _boundTop = new Point3d(width/2,depth/2,height);
		cuboid.setBounds(_boundTop, _boundBottom);

		return super.getCuboidList();
	}
}
