package com.marginallyclever.robotoverlord.entities;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.AABB;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotoverlord.Collidable;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.shapes.mesh.Mesh;
import com.marginallyclever.robotoverlord.components.shapes.mesh.load.MeshFactory;
import com.marginallyclever.robotoverlord.parameters.*;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewElementButton;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileFilter;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;

/**
 * A {@link Mesh} is a collection of points, triangles, normals, and possibly color and possibly texture coordinates.
 * A {@link ShapeEntity} is a {@link Mesh} positioned and scaled somewhere in a Scene.  That is to say, {@link ShapeEntity}
 * allows for local changes to the {@link Mesh} origin, scale, and rotation.
 * @author Dan Royer
 *
 */
@Deprecated
public class ShapeEntity extends PoseEntity implements Collidable {
	private static final Logger logger = LoggerFactory.getLogger(ShapeEntity.class);

	// The mesh attached to this shape
	protected transient Mesh myMesh;

	protected final StringEntity filename = new StringEntity("File","");

	// shape adjustments
	protected final DoubleEntity scale = new DoubleEntity("Scale",1.0);
	protected final Vector3dEntity rotationAdjust = new Vector3dEntity("Rotation");
	protected final Vector3dEntity originAdjust = new Vector3dEntity("Origin");

	private final IntEntity numTriangles = new IntEntity("Triangles",0);
	private final BooleanEntity hasNormals = new BooleanEntity("Has normals",false);
	private final BooleanEntity hasColors = new BooleanEntity("Has colors",false);
	private final BooleanEntity hasUVs = new BooleanEntity("Has UVs",false);

	private final MaterialComponent material = new MaterialComponent();
	private final AABB AABB = new AABB();
	
	public ShapeEntity() {
		this("Shape");
	}
	
	public ShapeEntity(String name) {
		super(name);
		addEntity(filename);

		addEntity(rotationAdjust);
		addEntity(originAdjust);
		addEntity(scale);
				
		filename.addPropertyChangeListener(this);
		rotationAdjust.addPropertyChangeListener(this);
		originAdjust.addPropertyChangeListener(this);
		scale.addPropertyChangeListener(this);

		addEntity(numTriangles);
		addEntity(hasNormals);
		addEntity(hasColors);
		addEntity(hasUVs);
	}
	
	public ShapeEntity(String name, String filename) {
		super(name);
		setShapeFilename(filename);
	}
	
	public void set(ShapeEntity b) {
		super.set(b);
		scale.set(b.scale.get());
		AABB.set(b.AABB);
		
		filename.set(b.filename.get());
		myMesh = b.myMesh;
		originAdjust.set(b.originAdjust.get());
		rotationAdjust.set(b.rotationAdjust.get());
	}

	public MaterialComponent getMaterial() {
		return material;
	}

	/**
	 * @return full path and file of the model on disk.
	 */
	public String getModelFilename() {
		return filename.get();
	}

	/**
	 * Sets the new shape filename, which causes the shape to be reloaded.
	 * @param newFilename
	 */
	public void setShapeFilename(String newFilename) {
		// if the filename has changed, throw out the shape so it will be reloaded.
		//if( this.filename.get().equals(newFilename) ) return;
		
		try {
			myMesh = MeshFactory.load(newFilename);
			if(myMesh!=null) {
				updateCuboid();
				numTriangles.set(myMesh.getNumTriangles());
				hasNormals.set(myMesh.getHasNormals());
				hasColors.set(myMesh.getHasColors());
				hasUVs.set(myMesh.getHasUVs());
			}
			// only change this after loading has completely succeeded.
			filename.set(newFilename);
		} catch (Exception e) {
			logger.error("Loading shape '"+newFilename+"' failed: "+e.getMessage());
		}
	}

	public void setShapeScale(double arg0) {
		scale.set(arg0);
		rebuildLocalPose();
	}
	
	public double getShapeScale() {
		return scale.get();
	}

	public void setShapeOrigin(double x,double y,double z) {
		originAdjust.set(x,y,z);
		rebuildLocalPose();
	}

	public void setShapeOrigin(Vector3d arg0) {
		originAdjust.set(arg0);
		rebuildLocalPose();
	}
	
	public Vector3d getShapeOrigin() {
		return new Vector3d(originAdjust.get());
	}

	public void setShapeRotation(double x,double y,double z) {
		rotationAdjust.set(x,y,z);
		rebuildLocalPose();
	}
	
	private void rebuildLocalPose() {
		Vector3d r = rotationAdjust.get();
		Matrix4d rotX = new Matrix4d();		rotX.rotX(Math.toRadians(r.x));		myPose.set(rotX);
		Matrix4d rotY = new Matrix4d();		rotY.rotY(Math.toRadians(r.y));		myPose.mul(rotY);
		Matrix4d rotZ = new Matrix4d();		rotZ.rotZ(Math.toRadians(r.z));		myPose.mul(rotZ);
		myPose.setScale(scale.get());
		myPose.setTranslation(originAdjust.get());
		updateCuboid();
	}

	public void setShapeRotation(Vector3d arg0) {
		rotationAdjust.set(arg0);
		rebuildLocalPose();
	}
	
	public Vector3d getShapeRotation() {
		return new Vector3d(rotationAdjust.get());
	}

	@Override
	public void update(double dt) {
		super.update(dt);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		Object o = evt.getSource();
		
		if(rotationAdjust==o || originAdjust==o || scale==o) {
			rebuildLocalPose();
		}
		if(filename==o) {
			setShapeFilename(filename.get());
		}
	}
	
	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2, myPose);

		if( myMesh==null ) {
			// draw placeholder
			PrimitiveSolids.drawBox(gl2, 1, 1, 1);
			PrimitiveSolids.drawStar(gl2,15.0);
		} else {
			material.render(gl2);
			myMesh.render(gl2);
		}
		
		gl2.glPopMatrix();

		// draw children
		super.render(gl2);
	}

	public void setModel(Mesh m) {
		myMesh = m;
	}
	
	public Mesh getModel() {
		return myMesh;
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("ShapeEntity",true);

		ArrayList<FileFilter> filters = MeshFactory.getAllExtensions();
		view.addFilename(filename,filters);
		
		view.add(rotationAdjust);
		view.add(originAdjust);
		view.add(scale);
		
		Mesh m = this.myMesh;
		if(m!=null) {
			view.add(numTriangles);
			view.add(hasNormals);
			view.add(hasColors);
			view.add(hasUVs);
		}

		ViewElementButton reloadButton = view.addButton("Reload");
		reloadButton.addActionEventListener((evt)-> reload() );
		
		view.popStack();

		super.getView(view);
	}
	
	protected void reload() {
		if(myMesh==null) return;
		try {
			MeshFactory.reload(myMesh);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Updates the {@link AABB} bounds.
	 */
	public void updateCuboid() {
		if(myMesh != null) {
			AABB.set(myMesh.getCuboid());
		} else {
			AABB.setShape(null);
			AABB.setBounds(new Point3d(0,0,0),new Point3d(0,0,0));
		}
	}

	/**
	 * @return a list of {@link AABB}, or null.
	 */
	@Override
	public ArrayList<AABB> getCuboidList() {
		ArrayList<AABB> list = new ArrayList<AABB>();
		list.add(AABB);
		Matrix4d m2 = getPoseWorld();
		AABB.setPose(m2);

		return list;
	}
}
