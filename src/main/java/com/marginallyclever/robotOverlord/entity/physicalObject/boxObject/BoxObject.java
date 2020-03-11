package com.marginallyclever.robotOverlord.entity.physicalObject.boxObject;


import java.util.ArrayList;

import javax.swing.JPanel;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.engine.model.Model;
import com.marginallyclever.robotOverlord.entity.material.Material;
import com.marginallyclever.robotOverlord.entity.physicalObject.PhysicalObject;

public class BoxObject extends PhysicalObject {
	private double width, height, depth;
	
	protected BoxObjectPanel boxPanel;

	protected Model model = new Model();
	protected Material mat;
	
	public BoxObject() {
		super();
		setName("Box");
		depth=width=height=1;
		mat=new Material();
	}
	

	@Override
	public ArrayList<JPanel> getContextPanels(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getContextPanels(gui);
		if(list==null) list = new ArrayList<JPanel>();
		

		// add the box panel
		boxPanel = new BoxObjectPanel(gui,this);
		list.add(boxPanel);

		// add material panel but do not add entity panel.
		ArrayList<JPanel> list2 = mat.getContextPanels(gui);
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
			//PrimitiveSolids.drawBox(gl2, (float)depth, (float)width, (float)height);
			model.render(gl2);
			
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
		
		updateModel();
	}
	
	/**
	 * Procedurally generate a list of triangles that form a box, subdivided by some amount.
	 */
	protected void updateModel() {
		model.clear();
		model.renderStyle=GL2.GL_TRIANGLES;
		//model.renderStyle=GL2.GL_LINES;  // set to see the wireframe
		
		float w = (float)(width/2);
		float d = (float)(depth/2);
		float h = (float)(height);
		
		int wParts = (int)(w/4)*2;
		int hParts = (int)(h/8)*2;
		int dParts = (int)(d/4)*2;
		
		Vector3d n=new Vector3d();
		Vector3d p0=new Vector3d();
		Vector3d p1=new Vector3d();
		Vector3d p2=new Vector3d();
		Vector3d p3=new Vector3d();
		
		// bottom
		n.set( 0, 0,-1);
		p0.set(-w, d,0);
		p1.set( w, d,0);
		p2.set( w,-d,0);
		p3.set(-w,-d,0);
		addSubdividedPlane(n,p0,p1,p2,p3,wParts,dParts);

		// top
		n.set( 0, 0, 1);
		p0.set( w, d,h);
		p1.set(-w, d,h);
		p2.set(-w,-d,h);
		p3.set( w,-d,h);
		addSubdividedPlane(n,p0,p1,p2,p3,wParts,dParts);
		
		// sides
		n.set( 0, 1, 0);
		p0.set(-w, d,h);
		p1.set( w, d,h);
		p2.set( w, d,0);
		p3.set(-w, d,0);
		addSubdividedPlane(n,p0,p1,p2,p3,wParts,hParts);

		n.set( 0,-1, 0);
		p0.set( w,-d,h);
		p1.set(-w,-d,h);
		p2.set(-w,-d,0);
		p3.set( w,-d,0);
		addSubdividedPlane(n,p0,p1,p2,p3,(int)(w/10),hParts);
		
		n.set( 1, 0, 0);
		p0.set( w, d,0);
		p1.set( w, d,h);
		p2.set( w,-d,h);
		p3.set( w,-d,0);
		addSubdividedPlane(n,p0,p1,p2,p3,dParts,hParts);
	
		n.set(-1, 0, 0);
		p0.set(-w,-d,h);
		p1.set(-w, d,h);
		p2.set(-w, d,0);
		p3.set(-w,-d,0);
		addSubdividedPlane(n,p0,p1,p2,p3,dParts,hParts);
	}

	/**
	 * Subdivide a plane into triangles.
	 * @param n plane normal
	 * @param p0 northwest corner
	 * @param p1 northeast corner
	 * @param p2 southeast corner
	 * @param p3 southwest corner
	 * @param xParts east/west divisions
	 * @param yParts north/south divisions
	 */
	protected void addSubdividedPlane(Vector3d n,
			Vector3d p0,
			Vector3d p1,
			Vector3d p2,
			Vector3d p3,
			int xParts,
			int yParts) {
		xParts = Math.max(xParts, 1);
		yParts = Math.max(yParts, 1);

		Vector3d pA=new Vector3d();
		Vector3d pB=new Vector3d();
		Vector3d pC=new Vector3d();
		Vector3d pD=new Vector3d();
		Vector3d pE=new Vector3d();
		Vector3d pF=new Vector3d();
		Vector3d pG=new Vector3d();
		Vector3d pH=new Vector3d();

		for(int x=0;x<xParts;x++) {
			pA.set(MathHelper.interpolate(p0, p1, (double)(x  )/(double)xParts));
			pB.set(MathHelper.interpolate(p0, p1, (double)(x+1)/(double)xParts));
			pC.set(MathHelper.interpolate(p3, p2, (double)(x  )/(double)xParts));
			pD.set(MathHelper.interpolate(p3, p2, (double)(x+1)/(double)xParts));
			
			for(int y=0;y<yParts;y++) {
				pE.set(MathHelper.interpolate(pA, pC, (double)(y  )/(double)yParts));
				pF.set(MathHelper.interpolate(pB, pD, (double)(y  )/(double)yParts));
				pG.set(MathHelper.interpolate(pA, pC, (double)(y+1)/(double)yParts));
				pH.set(MathHelper.interpolate(pB, pD, (double)(y+1)/(double)yParts));

				if(model.renderStyle == GL2.GL_TRIANGLES) {
					model.hasNormals=true;
					model.addNormal((float)n.x, (float)n.y, (float)n.z);
					model.addNormal((float)n.x, (float)n.y, (float)n.z);
					model.addNormal((float)n.x, (float)n.y, (float)n.z);
					
					model.addVertex((float)pE.x, (float)pE.y, (float)pE.z);
					model.addVertex((float)pF.x, (float)pF.y, (float)pF.z);
					model.addVertex((float)pH.x, (float)pH.y, (float)pH.z);

					model.addNormal((float)n.x, (float)n.y, (float)n.z);
					model.addNormal((float)n.x, (float)n.y, (float)n.z);
					model.addNormal((float)n.x, (float)n.y, (float)n.z);
					
					model.addVertex((float)pE.x, (float)pE.y, (float)pE.z);
					model.addVertex((float)pH.x, (float)pH.y, (float)pH.z);
					model.addVertex((float)pG.x, (float)pG.y, (float)pG.z);
				} else if(model.renderStyle == GL2.GL_LINES) {
					model.addVertex((float)pF.x, (float)pF.y, (float)pF.z);
					model.addVertex((float)pH.x, (float)pH.y, (float)pH.z);

					model.addVertex((float)pH.x, (float)pH.y, (float)pH.z);
					model.addVertex((float)pE.x, (float)pE.y, (float)pE.z);

					model.addVertex((float)pH.x, (float)pH.y, (float)pH.z);
					model.addVertex((float)pG.x, (float)pG.y, (float)pG.z);
					
					model.addVertex((float)pG.x, (float)pG.y, (float)pG.z);
					model.addVertex((float)pE.x, (float)pE.y, (float)pE.z);
				}
			}
		}
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
	public Material getMaterial() { return mat; }
}
