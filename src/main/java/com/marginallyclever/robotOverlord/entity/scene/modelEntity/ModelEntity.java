package com.marginallyclever.robotOverlord.entity.scene.modelEntity;


import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Observable;
import java.util.ServiceLoader;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.FileAccess;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.BooleanEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.IntEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.MaterialEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.StringEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.Vector3dEntity;
import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;
import com.marginallyclever.robotOverlord.log.Log;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;


public class ModelEntity extends PoseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5888928381757734702L;

	// the pool of all models loaded
	private static LinkedList<Model> modelPool = new LinkedList<Model>();

	// the model for this entity
	protected transient Model model;

	protected StringEntity filename = new StringEntity("File","");
	protected MaterialEntity material = new MaterialEntity();
	
	// model adjustments
	protected DoubleEntity scale = new DoubleEntity("Scale",1.0);
	protected Vector3dEntity rotationAdjust = new Vector3dEntity("Rotation");
	protected Vector3dEntity originAdjust = new Vector3dEntity("Origin");

	IntEntity numTriangles = new IntEntity("Triangles",0);
	BooleanEntity hasNormals = new BooleanEntity("Has normals",false);
	BooleanEntity hasColors = new BooleanEntity("Has colors",false);
	BooleanEntity hasUVs = new BooleanEntity("Has UVs",false);
			
	public ModelEntity() {
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
	
	public ModelEntity(String filename) {
		this();
		setModelFilename(filename);
	}

	public void set(ModelEntity b) {
		super.set(b);
		scale.set(b.scale.get());
		
		filename.set(b.filename.get());
		model = b.model;
		material.set(b.material);
		originAdjust.set(b.originAdjust.get());
		rotationAdjust.set(b.rotationAdjust.get());
	}
    
	public String getModelFilename() {
		return filename.get();
	}

	/**
	 * Sets the new model filename, which causes the model to be reloaded.
	 * @param newFilename
	 */
	public void setModelFilename(String newFilename) {
		// if the filename has changed, throw out the model so it will be reloaded.
		//if( this.filename.get().equals(newFilename) ) return;
		
		try {
			model = createModelFromFilename(newFilename);
			model.adjustScale(scale.get());
			model.adjustOrigin(originAdjust.get());
			model.adjustRotation(rotationAdjust.get());
			model.updateCuboid();

			numTriangles.set(model.getNumTriangles());
			hasNormals.set(model.hasNormals);
			hasColors.set(model.hasColors);
			hasUVs.set(model.hasUVs);
					
			// only change this after loading has completely succeeded.
			this.filename.set(newFilename);
		} catch (Exception e) {
			Log.error("Loading model '"+newFilename+"' failed: "+e.getLocalizedMessage());
		}
	}

	public void setModelScale(double arg0) {
		scale.set(arg0);
		if(model!=null) {
			model.adjustScale(arg0);
		}
	}
	
	public double getModelScale() {
		return scale.get();
	}

	public void setModelOrigin(double x,double y,double z) {
		originAdjust.set(x,y,z);
		if(model!=null) model.adjustOrigin(originAdjust.get());
	}

	public void setModelOrigin(Vector3d arg0) {
		originAdjust.set(arg0);
		if(model!=null) model.adjustOrigin(originAdjust.get());
	}
	
	public Vector3d getModelOrigin() {
		return new Vector3d(originAdjust.get());
	}

	public void setModelRotation(double x,double y,double z) {
		rotationAdjust.set(x,y,z);
		if(model!=null) model.adjustRotation(rotationAdjust.get());
	}

	public void setModelRotation(Vector3d arg0) {
		rotationAdjust.set(arg0);
		if(model!=null) model.adjustRotation(rotationAdjust.get());
	}
	
	public Vector3d getModelRotation() {
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
			setModelFilename(filename.get());
		}
		if(rotationAdjust==o) {
			setModelRotation(rotationAdjust.get());
		}
		if(originAdjust==o) {
			setModelOrigin(originAdjust.get());
		}
		if(scale==o) {
			setModelScale(scale.get());
		}
	}
	
	/**
	 * obeys super.updatePoseWorld, Updates cuboid
	 */
	@Override
	public void updatePoseWorld() {
		super.updatePoseWorld();
		
		// set up the physical limits
		if(model != null) {
			Cuboid mc = model.getCuboid();
			cuboid.setBounds(mc.getBoundsTop(),mc.getBoundsBottom());
		}
	}
	
	@Override
	public void render(GL2 gl2) {	
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, pose.get());

			material.render(gl2);
			if( model==null ) {
				// draw placeholder
				PrimitiveSolids.drawBox(gl2, 1, 1, 1);
				PrimitiveSolids.drawStar(gl2,3.0);
			} else {
				model.render(gl2);
			}
		gl2.glPopMatrix();
		
		// draw children
		// physicalObject also calls applyMatrix() so this has to happen outside of the matrix push/pop
		super.render(gl2);
	}

	
	public MaterialEntity getMaterial() {
		return material;
	}

	public Model getModel() {
		return model;
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Mo","Model");

		// TODO FileNameExtensionFilter is Swing specific and should not happen here.
		ArrayList<FileFilter> filters = new ArrayList<FileFilter>();
		ServiceLoader<ModelLoadAndSave> loaders = ServiceLoader.load(ModelLoadAndSave.class);
		Iterator<ModelLoadAndSave> i = loaders.iterator();
		while(i.hasNext()) {
			ModelLoadAndSave loader = i.next();
			filters.add( new FileNameExtensionFilter(loader.getEnglishName(), loader.getValidExtensions()) );
		}
		view.addFilename(filename,filters);
		
		view.add(rotationAdjust);
		view.add(originAdjust);
		view.add(scale);
		
		Model m = this.model;
		if(m!=null) {
			view.add(numTriangles);
			view.add(hasNormals);
			view.add(hasColors);
			view.add(hasUVs);
		}

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
	public static Model createModelFromFilename(String sourceName) throws Exception {
		if(sourceName == null || sourceName.trim().length()==0) return null;
		
		// find the existing model in the pool
		Iterator<Model> iter = modelPool.iterator();
		while(iter.hasNext()) {
			Model m = iter.next();
			if(m.getSourceName().equals(sourceName)) {
				return m;
			}
		}
		
		Model m=null;
		
		// not in pool.  Find a serviceLoader that can load this file type.
		ServiceLoader<ModelLoadAndSave> loaders = ServiceLoader.load(ModelLoadAndSave.class);
		Iterator<ModelLoadAndSave> i = loaders.iterator();
		int count=0;
		while(i.hasNext()) {
			count++;
			ModelLoadAndSave loader = i.next();
			if(loader.canLoad() && loader.canLoad(sourceName)) {
				BufferedInputStream stream = FileAccess.open(sourceName);
				m = loader.load(stream);
				m.setSourceName(sourceName);
				// Maybe add a m.setSaveAndLoader(loader); ?
				modelPool.add(m);
				break;
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
}
