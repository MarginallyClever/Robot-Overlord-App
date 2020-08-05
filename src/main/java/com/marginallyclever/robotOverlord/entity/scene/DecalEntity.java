package com.marginallyclever.robotOverlord.entity.scene;


import java.util.ArrayList;
import java.util.Observable;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.TextureEntity;
import com.marginallyclever.robotOverlord.entity.scene.modelEntity.Model;
import com.marginallyclever.robotOverlord.entity.scene.modelEntity.ModelEntity;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * A nearly two dimensional object with a texture on both sides.
 * @author aggra
 *
 */
public class DecalEntity extends ModelEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7894115384701922746L;

	protected DoubleEntity width = new DoubleEntity("Width",1.0);
	protected DoubleEntity height = new DoubleEntity("Height",1.0);
	
	public DecalEntity() {
		super();
		setName("Decal");
		addChild(width);
		addChild(height);
		
		width.addObserver(this);
		height.addObserver(this);
		
		model = new Model();
	}

	/**
	 * 
	 * @return a list of cuboids, or null.
	 */
	@Override
	public ArrayList<Cuboid> getCuboidList() {
		return super.getCuboidList();
	}

	@Override
	public void update(Observable o, Object arg) {
		updatePlane();
		super.update(o, arg);
	}
	
	private void updatePlane() {
		Point3d _boundBottom = new Point3d(-width.get()/2,-height.get()/2,-0.01);
		Point3d _boundTop    = new Point3d( width.get()/2, height.get()/2,0.01);
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
		
		float w = (float)(width.get()/2);
		float h = (float)(height.get()/2);
		
		int wParts = (int)(w/4)*2;
		int hParts = (int)(h/4)*2;
		
		Vector3d n=new Vector3d();
		Vector3d p0=new Vector3d();
		Vector3d p1=new Vector3d();
		Vector3d p2=new Vector3d();
		Vector3d p3=new Vector3d();
		
		// bottom
		n.set( 0, 0,-1);
		p0.set(-w, h,-0.01);
		p1.set( w, h,-0.01);
		p2.set( w,-h,-0.01);
		p3.set(-w,-h,-0.01);
		addSubdividedPlane(n,p0,p1,p2,p3,wParts,hParts);

		// top
		n.set( 0, 0, 1);
		p0.set( w, h,0.01);
		p1.set(-w, h,0.01);
		p2.set(-w,-h,0.01);
		p3.set( w,-h,0.01);
		addSubdividedPlane(n,p0,p1,p2,p3,wParts,hParts);
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
	
	public void setWidth(double v) {
		width.set(v);
		updatePlane();
	}
	
	public void setHeight(double v) {
		height.set(v);
		updatePlane();
	}
	

	public void setSize(double w, double h) {
		width.set(w);
		height.set(h);
		updatePlane();
	}
	
	public double getWidth() { return width.get(); }
	public double getHeight() { return height.get(); }

	@Override
	public void getView(ViewPanel view) {
		view.pushStack("De", "Decal");
		width.getView(view);
		height.getView(view);
		view.popStack();
		super.getView(view);
	}
}
