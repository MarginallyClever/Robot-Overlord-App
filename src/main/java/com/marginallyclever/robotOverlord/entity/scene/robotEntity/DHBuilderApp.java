package com.marginallyclever.robotOverlord.entity.scene.robotEntity;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFileChooser;
import javax.vecmath.Matrix4d;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.MaterialEntity;
import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink.LinkAdjust;
import com.marginallyclever.robotOverlord.entity.scene.modelEntity.ModelEntity;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewElement;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

public class DHBuilderApp extends PoseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String [] BONE_NAMES = { "X", "Y", "Z", "U", "V", "W" };
	
	protected ModelEntity anchor = new ModelEntity();
	protected ModelEntity [] models = new ModelEntity[BONE_NAMES.length];
	public DHLink [] links = new DHLink[BONE_NAMES.length];
	protected MaterialEntity mat = new MaterialEntity();
	
	public DHBuilderApp() {
		super();
		setName("DHBuilderApp");

		addChild(anchor);
		anchor.setName("Anchor");
		anchor.setMaterial(mat);
		
		for(int i=0;i<BONE_NAMES.length;++i) {
			models[i] = new ModelEntity();
			models[i].setName("model "+BONE_NAMES[i]);
			addChild(models[i]);
			models[i].setMaterial(mat);
		}
		
		Entity parent = this;
		for(int i=0;i<BONE_NAMES.length;++i) {
			links[i] = new DHLink();
			links[i].setLetter(BONE_NAMES[i]);
			links[i].flags = LinkAdjust.ALL;
			links[i].showLineage.set(true);
			parent.addChild(links[i]);
			parent = links[i];
		}
		
		addChild(mat);
		mat.setDiffuseColor(1, 1, 1, 0.5f);
	}
	
	@Override
	public void render(GL2 gl2) {
		super.render(gl2);
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("BA", "Builder App");
		
		ViewElement bindButton = view.addButton("Test");
		bindButton.addObserver(new Observer() {
			@Override
			public void update(Observable arg0, Object arg1) {
				// Use the poseWorld for each DHLink to adjust the model origins.
				for(int i=0;i<links.length;++i) {
					DHLink bone=links[i];
					bone.model.set(models[i]);
					Matrix4d iWP = bone.getPoseWorld();
					iWP.invert();
					bone.model.getModel().adjustMatrix(iWP);
				}
			}
		});
		
		ViewElement saveButton = view.addButton("Save");
		saveButton.addObserver(new Observer() {
			@Override
			public void update(Observable arg0, Object arg1) {
				//private static 
				String lastPath=System.getProperty("user.dir");
				RobotOverlord ro = (RobotOverlord)getRoot();

				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if( lastPath!=null ) chooser.setCurrentDirectory(new File(lastPath));
				if( chooser.showOpenDialog(ro.getMainFrame()) == JFileChooser.APPROVE_OPTION ) {
					saveToFolder(chooser.getSelectedFile());
				}
			}
		});

		ViewElement loadButton = view.addButton("Load");
		loadButton.addObserver(new Observer() {
			@Override
			public void update(Observable arg0, Object arg1) {
				//private static 
				String lastPath=System.getProperty("user.dir");
				RobotOverlord ro = (RobotOverlord)getRoot();

				//https://stackoverflow.com/questions/4779360/browse-for-folder-dialog
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if( lastPath!=null ) chooser.setCurrentDirectory(new File(lastPath));
				if( chooser.showOpenDialog(ro.getMainFrame()) == JFileChooser.APPROVE_OPTION ) {
					loadFromFolder(chooser.getSelectedFile());
				}
			}
		});
		
		view.popStack();
		
		super.getView(view);
	}
	
	static public class MinimalRecord implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2689287169771077750L;
		
		protected double d=0,t=0,r=0,a=0;
		protected String letter="";
		protected String modelFilename="";

		public MinimalRecord() {
			super();
		}
		
		public MinimalRecord(String l,double d,double t,double r,double a,String m) {
			super();
			
			this.letter=l;
			this.d=d;
			this.t=t;
			this.r=r;
			this.a=a;
			this.modelFilename=m;
		}

		public double getD() {
			return d;
		}

		public void setD(double d) {
			this.d = d;
		}

		public double getT() {
			return t;
		}

		public void setT(double t) {
			this.t = t;
		}

		public double getR() {
			return r;
		}

		public void setR(double r) {
			this.r = r;
		}

		public double getA() {
			return a;
		}

		public void setA(double a) {
			this.a = a;
		}

		public String getLetter() {
			return letter;
		}

		public void setLetter(String letter) {
			this.letter = letter;
		}

		public String getModelFilename() {
			return modelFilename;
		}

		public void setModelFilename(String modelFilename) {
			this.modelFilename = modelFilename;
		}
	};
	
	public void saveToFolder(File yourFolder) {
		ObjectMapper om = new ObjectMapper();
		om.enable(SerializationFeature.INDENT_OUTPUT);
		try {
			for(int i=0;i<links.length;++i) {
				DHLink bone=links[i];
				String testName = yourFolder.getAbsolutePath()+File.separator+links[i].getName()+".json";
				om.writeValue(new File(testName), new MinimalRecord(
						bone.getLetter(),
						bone.d.get(),
						bone.theta.get(),
						bone.r.get(),
						bone.alpha.get(),
						models[i].getModelFilename()
					));
			}
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void loadFromFolder(File yourFolder) {
		final String [] extensions = { ".obj",".stl" };
		
		for(int i=0;i<BONE_NAMES.length;++i) {
			// load the model files
			for( String e : extensions ) {
				// TODO relative path instead of absolute?
				String testName = yourFolder.getAbsolutePath()+File.separator+links[i].getName()+e;
				File tObj = new File(testName);
				//https://howtodoinjava.com/java/io/how-to-check-if-file-exists-in-java/
				if(tObj.exists()) {
					models[i].setModelFilename(testName);
					models[i].setMaterial(mat);
					break;
				}
			}
			// load the bone description
			String testName = yourFolder.getAbsolutePath()+File.separator+links[i].getName()+".json";
			File tObj = new File(testName);
			if(tObj.exists()) {
				ObjectMapper om = new ObjectMapper();
				om.enable(SerializationFeature.INDENT_OUTPUT);
				try {
					MinimalRecord mr = (MinimalRecord)om.readValue(new File(testName), MinimalRecord.class);
					links[i].d.set(mr.d);
					links[i].theta.set(mr.t);
					links[i].r.set(mr.r);
					links[i].alpha.set(mr.a);
					links[i].model.setModelFilename(mr.modelFilename);
				} catch (JsonParseException e) {
					e.printStackTrace();
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				links[i].showLineage.set(true);
			}
		}
		
		// Use the poseWorld for each DHLink to adjust the model origins.
		for(int i=0;i<links.length;++i) {
			DHLink bone=links[i];
			bone.model.set(models[i]);
			Matrix4d iWP = bone.getPoseWorld();
			iWP.invert();
			bone.model.getModel().adjustMatrix(iWP);
		}
		
		for( String e : extensions ) {
			String testName = yourFolder.getAbsolutePath()+File.separator+anchor.getName()+e;
			File tObj = new File(testName);
			//https://howtodoinjava.com/java/io/how-to-check-if-file-exists-in-java/
			if(tObj.exists()) {
				anchor.setModelFilename(testName);
				break;
			}
		}
	}
}
