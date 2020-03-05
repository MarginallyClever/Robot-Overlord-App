package com.marginallyclever.robotOverlord.entity.physicalObject.boxObject;


import java.util.ArrayList;

import javax.swing.JPanel;
import javax.vecmath.Matrix4d;
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
	private double width, height, depth;
	
	protected BoxObjectPanel boxPanel;
	protected MaterialPanel materialPanel;
	
	Material mat;
	
	public BoxObject() {
		super();
		setName("Box");
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

			MatrixHelper.applyMatrix(gl2, this.getPose());
		
			// TODO this should probably be an option that can be toggled.
			// It is here to fix scaling of the entire model.  It won't 
			// work when the model is scaled unevenly.
			gl2.glEnable(GL2.GL_NORMALIZE);
	
			// draw placeholder
			mat.render(gl2);
			PrimitiveSolids.drawBox(gl2, (float)depth, (float)width, (float)height);
			
			//PrimitiveSolids.drawBoxWireframe(gl2,cuboid.getBoundsBottom(),cuboid.getBoundsTop());
		
		gl2.glPopMatrix();
	}

	@Override
	public void setPose(Matrix4d arg0) {
		super.setPose(arg0);
		cuboid.setPoseWorld(arg0);
	}

	/**
	 * 
	 * @return a list of cuboids, or null.
	 */
	@Override
	public ArrayList<Cuboid> getCuboidList() {
		return super.getCuboidList();
	}

	private void updateCuboid() {
		Point3d _boundBottom = new Point3d(-width/2,-depth/2,0);
		Point3d _boundTop = new Point3d(width/2,depth/2,height);
		cuboid.setBounds(_boundTop, _boundBottom);
	}

	public void setWidth(double value) {
		width=value;
		updateCuboid();
	}
	public void setDepth(double value) {
		depth=value;
		updateCuboid();
	}
	public void setHeight(double value) {
		height=value;
		updateCuboid();
	}


	public void setSize(double w, double h, double d) {
		width=w;
		depth=d;
		height=h;
		updateCuboid();
	}
	
	public double getWidth() { return width; }
	public double getDepth() { return depth; }
	public double getHeight() { return height; }
}
