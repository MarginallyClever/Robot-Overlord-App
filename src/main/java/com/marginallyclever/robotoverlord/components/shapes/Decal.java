package com.marginallyclever.robotoverlord.components.shapes;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serial;
import javax.vecmath.Vector3d;

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.components.ShapeComponent;
import com.marginallyclever.robotoverlord.mesh.Mesh;
import com.marginallyclever.robotoverlord.mesh.ShapeEntity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import com.marginallyclever.robotoverlord.uiexposedtypes.DoubleEntity;

/**
 * A nearly two dimensional object with a texture on both sides.
 * @author Dan Royer
 *
 */
public class Decal extends ShapeComponent implements PropertyChangeListener {
	@Serial
	private static final long serialVersionUID = -4934794752752097855L;
	
	protected DoubleEntity width = new DoubleEntity("Width",1.0);
	protected DoubleEntity height = new DoubleEntity("Height",1.0);
	
	public Decal() {
		super();
		
		width.addPropertyChangeListener(this);
		height.addPropertyChangeListener(this);
		
		myMesh = new Mesh();
		updateModel();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		updateModel();
	}
	
	/**
	 * Procedurally generate a list of triangles that form a box, subdivided by some amount.
	 */
	protected void updateModel() {
		myMesh.clear();
		myMesh.renderStyle=GL2.GL_TRIANGLES;
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

				if(myMesh.renderStyle == GL2.GL_TRIANGLES) {
					myMesh.addNormal((float)n.x, (float)n.y, (float)n.z);
					myMesh.addNormal((float)n.x, (float)n.y, (float)n.z);
					myMesh.addNormal((float)n.x, (float)n.y, (float)n.z);
					
					myMesh.addVertex((float)pE.x, (float)pE.y, (float)pE.z);
					myMesh.addVertex((float)pF.x, (float)pF.y, (float)pF.z);
					myMesh.addVertex((float)pH.x, (float)pH.y, (float)pH.z);

					myMesh.addNormal((float)n.x, (float)n.y, (float)n.z);
					myMesh.addNormal((float)n.x, (float)n.y, (float)n.z);
					myMesh.addNormal((float)n.x, (float)n.y, (float)n.z);
					
					myMesh.addVertex((float)pE.x, (float)pE.y, (float)pE.z);
					myMesh.addVertex((float)pH.x, (float)pH.y, (float)pH.z);
					myMesh.addVertex((float)pG.x, (float)pG.y, (float)pG.z);
				} else if(myMesh.renderStyle == GL2.GL_LINES) {
					myMesh.addVertex((float)pF.x, (float)pF.y, (float)pF.z);
					myMesh.addVertex((float)pH.x, (float)pH.y, (float)pH.z);

					myMesh.addVertex((float)pH.x, (float)pH.y, (float)pH.z);
					myMesh.addVertex((float)pE.x, (float)pE.y, (float)pE.z);

					myMesh.addVertex((float)pH.x, (float)pH.y, (float)pH.z);
					myMesh.addVertex((float)pG.x, (float)pG.y, (float)pG.z);
					
					myMesh.addVertex((float)pG.x, (float)pG.y, (float)pG.z);
					myMesh.addVertex((float)pE.x, (float)pE.y, (float)pE.z);
				}
			}
		}
	}
	
	public void setWidth(double v) {
		width.set(v);
		updateModel();
	}
	
	public void setHeight(double v) {
		height.set(v);
		updateModel();
	}
	

	public void setSize(double w, double h) {
		width.set(w);
		height.set(h);
		updateModel();
	}
	
	public double getWidth() { return width.get(); }
	public double getHeight() { return height.get(); }

	@Override
	public void getView(ViewPanel view) {
		super.getView(view);
		width.getView(view);
		height.getView(view);
	}

	@Override
	public void save(BufferedWriter writer) throws IOException {
		super.save(writer);
		width.save(writer);
		height.save(writer);
	}

	@Override
	public void load(BufferedReader reader) throws Exception {
		super.load(reader);
		width.load(reader);
		height.load(reader);
	}
}
