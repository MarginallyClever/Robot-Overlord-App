package com.marginallyclever.robotOverlord.entity.scene.shapeEntity;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import java.util.ServiceLoader;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.vecmath.Vector3d;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.FileAccess;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.BooleanEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.IntEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.MaterialEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.StringEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.Vector3dEntity;
import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewElementButton;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * A "shape" is a collection of triangles, normals, and possibly other elements in a single static 3D shape.
 * A ShapeEntity is a shape positioned and scaled somewhere in a Scene.
 * @author aggra
 *
 */
public class ShapeEntity extends PoseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6421492357105354857L;

	// the pool of all shapes loaded
	@JsonIgnore
	private static LinkedList<Shape> shapePool = new LinkedList<Shape>();

	// the shape for this entity
	@JsonIgnore
	protected transient Shape shape;

	protected StringEntity filename = new StringEntity("File","");
	
	@JsonIgnore
	protected MaterialEntity material = new MaterialEntity();
	
	// shape adjustments
	protected DoubleEntity scale = new DoubleEntity("Scale",1.0);
	protected Vector3dEntity rotationAdjust = new Vector3dEntity("Rotation");
	protected Vector3dEntity originAdjust = new Vector3dEntity("Origin");

	@JsonIgnore
	IntEntity numTriangles = new IntEntity("Triangles",0);
	
	@JsonIgnore
	BooleanEntity hasNormals = new BooleanEntity("Has normals",false);
	
	@JsonIgnore
	BooleanEntity hasColors = new BooleanEntity("Has colors",false);
	
	@JsonIgnore
	BooleanEntity hasUVs = new BooleanEntity("Has UVs",false);
			
	public ShapeEntity() {
		super();
		setName("Model");
		addChild(filename);

		addChild(rotationAdjust);
		addChild(originAdjust);
		addChild(scale);

		filename.addObserver(this);
		rotationAdjust.addObserver(this);
		originAdjust.addObserver(this);
		scale.addObserver(this);

		addChild(numTriangles);
		addChild(hasNormals);
		addChild(hasColors);
		addChild(hasUVs);
	}
	
	public ShapeEntity(String filename) {
		this();
		setShapeFilename(filename);
	}

	public void set(ShapeEntity b) {
		super.set(b);
		scale.set(b.scale.get());
		
		filename.set(b.filename.get());
		shape = b.shape;
		material.set(b.material);
		originAdjust.set(b.originAdjust.get());
		rotationAdjust.set(b.rotationAdjust.get());
	}
    
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
			shape = createModelFromFilename(newFilename);
			if(shape!=null) {
				shape.adjustScale(scale.get());
				shape.adjustOrigin(originAdjust.get());
				shape.adjustRotation(rotationAdjust.get());
				shape.updateCuboid();
				numTriangles.set(shape.getNumTriangles());
				hasNormals.set(shape.hasNormals);
				hasColors.set(shape.hasColors);
				hasUVs.set(shape.hasUVs);
			}
			// only change this after loading has completely succeeded.
			this.filename.set(newFilename);
		} catch (Exception e) {
			Log.error("Loading shape '"+newFilename+"' failed: "+e.getLocalizedMessage());
		}
	}

	public void setShapeScale(double arg0) {
		scale.set(arg0);
		if(shape!=null) {
			shape.adjustScale(arg0);
		}
	}
	
	public double getShapeScale() {
		return scale.get();
	}

	public void setShapeOrigin(double x,double y,double z) {
		originAdjust.set(x,y,z);
		if(shape!=null) shape.adjustOrigin(originAdjust.get());
	}

	public void setShapeOrigin(Vector3d arg0) {
		originAdjust.set(arg0);
		if(shape!=null) shape.adjustOrigin(originAdjust.get());
	}
	
	public Vector3d getShapeOrigin() {
		return new Vector3d(originAdjust.get());
	}

	public void setShapeRotation(double x,double y,double z) {
		rotationAdjust.set(x,y,z);
		if(shape!=null) shape.adjustRotation(rotationAdjust.get());
	}

	public void setShapeRotation(Vector3d arg0) {
		rotationAdjust.set(arg0);
		if(shape!=null) shape.adjustRotation(rotationAdjust.get());
	}
	
	public Vector3d getShapeRotation() {
		return new Vector3d(rotationAdjust.get());
	}

	@Override
	public void update(double dt) {
		super.update(dt);
	}
	
	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);
		if(filename==o) {
			setShapeFilename(filename.get());
		}
		if(rotationAdjust==o) {
			setShapeRotation(rotationAdjust.get());
		}
		if(originAdjust==o) {
			setShapeOrigin(originAdjust.get());
		}
		if(scale==o) {
			setShapeScale(scale.get());
		}
	}
	
	/**
	 * obeys super.updatePoseWorld, Updates cuboid
	 */
	@Override
	public void updatePoseWorld() {
		super.updatePoseWorld();
		
		// set up the physical limits
		if(shape != null) {
			Cuboid mc = shape.getCuboid();
			cuboid.setBounds(mc.getBoundsTop(),mc.getBoundsBottom());
		}
	}
	
	@Override
	public void render(GL2 gl2) {
		renderModel(gl2);
		
		// draw children
		super.render(gl2);
	}

	
	public void renderModel(GL2 gl2) {	
		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2, pose);

		if( shape==null ) {
			// draw placeholder
			PrimitiveSolids.drawBox(gl2, 1, 1, 1);
			PrimitiveSolids.drawStar(gl2,15.0);
		} else {
			material.render(gl2);
			shape.render(gl2);
		}
		gl2.glPopMatrix();
	}
	
	
	public MaterialEntity getMaterial() {
		return material;
	}

	public void setMaterial(MaterialEntity m) {
		if(m==null) return;  // bounce the null materials outta here!
		
		material = m;
	}
	
	public void setModel(Shape m) {
		shape = m;
	}
	
	public Shape getModel() {
		return shape;
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Mo","Model");

		// TODO FileNameExtensionFilter is Swing specific and should not happen here.
		ArrayList<FileFilter> filters = new ArrayList<FileFilter>();
		ServiceLoader<ShapeLoadAndSave> loaders = ServiceLoader.load(ShapeLoadAndSave.class);
		Iterator<ShapeLoadAndSave> i = loaders.iterator();
		while(i.hasNext()) {
			ShapeLoadAndSave loader = i.next();
			filters.add( new FileNameExtensionFilter(loader.getEnglishName(), loader.getValidExtensions()) );
		}
		view.addFilename(filename,filters);
		
		view.add(rotationAdjust);
		view.add(originAdjust);
		view.add(scale);
		
		Shape m = this.shape;
		if(m!=null) {
			view.add(numTriangles);
			view.add(hasNormals);
			view.add(hasColors);
			view.add(hasUVs);
		}

		ViewElementButton reloadButton = view.addButton("Reload");
		reloadButton.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				reload();
			}
		});
		
		view.popStack();
		
		material.getView(view);
		
		super.getView(view);
	}


	/**
	 * Makes sure to only load one instance of each source file.  Loads all the data immediately.
	 * @param sourceName file from which to load.  may be filename.ext or zipfile.zip:filename.ext
	 * @return the instance.
	 * @throws Exception if file cannot be read successfully
	 */
	public static Shape createModelFromFilename(String sourceName) throws Exception {
		if(sourceName == null || sourceName.trim().length()==0) return null;
		
		// find the existing shape in the pool
		Iterator<Shape> iter = shapePool.iterator();
		while(iter.hasNext()) {
			Shape m = iter.next();
			if(m.getSourceName().equals(sourceName)) {
				return m;
			}
		}
		
		Shape m=null;
		
		// not in pool.  Find a serviceLoader that can load this file type.
		ServiceLoader<ShapeLoadAndSave> loaders = ServiceLoader.load(ShapeLoadAndSave.class);
		Iterator<ShapeLoadAndSave> i = loaders.iterator();
		int count=0;
		while(i.hasNext()) {
			count++;
			ShapeLoadAndSave loader = i.next();
			if(loader.canLoad() && loader.canLoad(sourceName)) {
				BufferedInputStream stream = FileAccess.open(sourceName);
				m=new Shape();
				if(loader.load(stream,m)) {
					m.setSourceName(sourceName);
					m.setLoader(loader);
					// Maybe add a m.setSaveAndLoader(loader); ?
					shapePool.add(m);
					break;
				}
			}
		}

		if(m==null) {
			if(count==0) {
				throw new Exception("No loaders found!");
			} else {
				throw new Exception("No loader found for "+sourceName);
			}
		}
		
		return m;
	}
	
	protected void reload() {
		if(shape==null) return;
		try {
			shape.clear();
			BufferedInputStream stream = FileAccess.open(this.getModelFilename());
			ShapeLoadAndSave loader = shape.loader;
			loader.load(stream,shape);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
