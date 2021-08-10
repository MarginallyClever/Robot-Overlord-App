package com.marginallyclever.robotOverlord.dhRobotEntity;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.IntBuffer;
import javax.swing.JFileChooser;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.PoseEntity;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.dhRobotEntity.DHLink.LinkAdjust;
import com.marginallyclever.robotOverlord.dhRobotEntity.solvers.DHIKSolver_GradientDescent;
import com.marginallyclever.robotOverlord.shape.Shape;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewElement;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewElementButton;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;
import com.marginallyclever.robotOverlord.uiExposedTypes.MaterialEntity;

public class DHBuilderApp extends DHRobotModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6652532320052257924L;

	public static final String [] BONE_NAMES = { "X", "Y", "Z", "U", "V", "W" };
	
	private Shape anchor = new Shape();
	private Shape [] models = new Shape[BONE_NAMES.length];
	
	private MaterialEntity mat = new MaterialEntity();
	public DHLink endEffector = new DHLink();
	public PoseEntity endEffectorTarget = new PoseEntity();
	
	private static String LAST_PATH = System.getProperty("user.dir");
	
	private boolean inTest=false;
	
	public double [] thetaAtTestStart = new double[BONE_NAMES.length];

	private boolean eeLock=false;
	
	public DHBuilderApp() {
		super();
		setName("DHBuilderApp");
		setNumLinks(BONE_NAMES.length);
		setIKSolver(new DHIKSolver_GradientDescent());
		
		int i=0;
		for(DHLink bone : links) {
			bone.setLetter(BONE_NAMES[i++]);
			bone.flags = LinkAdjust.ALL;
			bone.showLineage.set(true);
		}
		
		endEffector.setName("End Effector");
		links.get(links.size()-1).addChild(endEffector);
		endEffector.addPropertyChangeListener(this);
		
		addChild(anchor);
		anchor.setName("Anchor");
		anchor.setMaterial(mat);
		
		for( i=0;i<BONE_NAMES.length;++i) {
			models[i] = new Shape();
			models[i].setName("model "+BONE_NAMES[i]);
			addChild(models[i]);
			models[i].setMaterial(mat);
		}
		
		endEffectorTarget.setName("End Effector Target");
		addChild(endEffectorTarget);

		endEffectorTarget.setPoseWorld(endEffector.getPoseWorld());
		endEffectorTarget.addPropertyChangeListener(this);
		
		addChild(mat);
	}
	
	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
			anchor.render(gl2);
			
			if(!inTest) {
				for( int i=0;i<BONE_NAMES.length;++i) {
					models[i].render(gl2);
				}
			} else {
				links.get(0).render(gl2);
			}
			
			boolean showBones=true;
			if(showBones==true) {
				Vector3d p0 = new Vector3d(0,0,0);
				for( int i=0;i<BONE_NAMES.length;++i) {
					Matrix4d m = links.get(i).getPoseWorld();
					Vector3d p1 = MatrixHelper.getPosition(m);
	
					IntBuffer depthFunc = IntBuffer.allocate(1);
					gl2.glGetIntegerv(GL2.GL_DEPTH_FUNC, depthFunc);
					gl2.glDepthFunc(GL2.GL_ALWAYS);
					gl2.glDisable(GL2.GL_TEXTURE_2D);
					gl2.glDisable(GL2.GL_LIGHTING);
					
					gl2.glPushMatrix();
					gl2.glColor3d(1, 1, 1);
					gl2.glBegin(GL2.GL_LINES);
					gl2.glVertex3d(p0.x,p0.y,p0.z);
					gl2.glVertex3d(p1.x,p1.y,p1.z);
					gl2.glEnd();
					p0=p1;
					gl2.glDepthFunc(depthFunc.get());
					
					MatrixHelper.applyMatrix(gl2, m);
					PrimitiveSolids.drawStar(gl2, 15);
					gl2.glPopMatrix();
				}
			}
		gl2.glPopMatrix();
		
		// don't call super.render()
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		Object o = evt.getSource();
		
		if(o==endEffectorTarget && eeLock==false) {
			eeLock=true;
			this.setPoseIK(endEffectorTarget.getPoseWorld());
			eeLock=false;
		}
		if(o==endEffector && eeLock==false) {
			eeLock=true;
			endEffectorTarget.setPoseWorld(endEffector.getPoseWorld());
			eeLock=false;
		}
	}

	@Override
	public void getView(ViewPanel view) {
		view.pushStack("BA", "Builder App");
		
		final ViewElementButton bindButton = (ViewElementButton)view.addButton(inTest ? "Stop test":"Start test");
		bindButton.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if(!inTest) {
					testStart();
					bindButton.setText("Stop test");
					inTest=true;
				} else {
					testEnd();
					bindButton.setText("Start test");
					inTest=false;
				}
			}
		});

		final ViewElementButton lockButton = view.addButton("Lock");
		lockButton.addPropertyChangeListener((evt) -> {
			if(lockButton.getText().contentEquals("Lock")) {
				for(int i=0;i<links.size();++i) {
					DHLink bone=links.get(i);
					bone.flags = LinkAdjust.THETA;
				}
				lockButton.setText("Unlock");
			} else {
				for(int i=0;i<links.size();++i) {
					DHLink bone=links.get(i);
					bone.flags = LinkAdjust.ALL;
				}
				lockButton.setText("Lock");
			}
		});
		
		ViewElement saveButton = view.addButton("Save");
		saveButton.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				//private static 
				RobotOverlord ro = (RobotOverlord)getRoot();

				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if( LAST_PATH!=null ) chooser.setCurrentDirectory(new File(LAST_PATH));
				if( chooser.showSaveDialog(ro.getMainFrame()) == JFileChooser.APPROVE_OPTION ) {
					LAST_PATH = chooser.getSelectedFile().getPath();
					saveToFolder(chooser.getSelectedFile());
				}
			}
		});

		ViewElement loadButton = view.addButton("Load");
		loadButton.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				//private static 
				RobotOverlord ro = (RobotOverlord)getRoot();

				//https://stackoverflow.com/questions/4779360/browse-for-folder-dialog
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if( LAST_PATH!=null ) chooser.setCurrentDirectory(new File(LAST_PATH));
				if( chooser.showOpenDialog(ro.getMainFrame()) == JFileChooser.APPROVE_OPTION ) {
					LAST_PATH = chooser.getSelectedFile().getPath();
					loadFromFolder(chooser.getSelectedFile());
					bindButton.setText("Start test");
					inTest=false;
				}
			}
		});
		
		view.popStack();
		
		super.getView(view);
	}

	static public class MinimalRecord implements Serializable {
		private static final long serialVersionUID = 2689287169771077750L;
		
		private double t=0,a=0,d=0,r=0,rmax=90,rmin=-90;
		private String letter="";
		private String modelFilename="";

		public MinimalRecord() {
			super();
		}
		
		public MinimalRecord(String l,double d,double t,double r,double a,double rmin,double rmax,String m) {
			super();
			
			this.letter=l;
			this.d=d;
			this.t=t;
			this.r=r;
			this.a=a;
			this.rmax=rmax;
			this.rmin=rmin;
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

		public double getRmax() {
			return rmax;
		}

		public double getRmin() {
			return rmin;
		}
	};
	
	public void saveToFolder(File yourFolder) {
		ObjectMapper om = new ObjectMapper();
		om.enable(SerializationFeature.INDENT_OUTPUT);
		try {
			for(int i=0;i<links.size();++i) {
				DHLink bone=links.get(i);
				String testName = yourFolder.getAbsolutePath()+File.separator+links.get(i).getName()+".json";
				om.writeValue(new File(testName), new MinimalRecord(
						bone.getLetter(),
						bone.d.get(),
						bone.theta.get(),
						bone.r.get(),
						bone.alpha.get(),
						bone.rangeMin.get(),
						bone.rangeMax.get(),
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

		for(int i=0;i<links.size();++i) {
			DHLink bone = links.get(i);
			
			// load the model files
			for( String e : extensions ) {
				// TODO relative path instead of absolute?
				String testName = yourFolder.getAbsolutePath()+File.separator+bone.getName()+e;
				File tObj = new File(testName);
				//https://howtodoinjava.com/java/io/how-to-check-if-file-exists-in-java/
				if(tObj.exists()) {
					models[i].setShapeFilename(testName);
					models[i].setMaterial(mat);
					break;
				}
			}
			// load the bone description
			String testName = yourFolder.getAbsolutePath()+File.separator+bone.getName()+".json";
			File tObj = new File(testName);
			if(tObj.exists()) {
				ObjectMapper om = new ObjectMapper();
				om.enable(SerializationFeature.INDENT_OUTPUT);
				try {
					MinimalRecord mr = (MinimalRecord)om.readValue(new File(testName), MinimalRecord.class);
					bone.rangeMax.set(mr.rmax);
					bone.rangeMin.set(mr.rmin);
					bone.d.set(mr.d);
					bone.theta.set(mr.t);
					bone.r.set(mr.r);
					bone.alpha.set(mr.a);
					bone.setShapeFilename(mr.modelFilename);
				} catch (JsonParseException e) {
					e.printStackTrace();
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				bone.showLineage.set(true);
			}
			bone.refreshDHMatrix();
		}
		
		for( String e : extensions ) {
			String testName = yourFolder.getAbsolutePath()+File.separator+anchor.getName()+e;
			File tObj = new File(testName);
			//https://howtodoinjava.com/java/io/how-to-check-if-file-exists-in-java/
			if(tObj.exists()) {
				anchor.setShapeFilename(testName);
				break;
			}
		}
	}
	
	private void testStart() {
		for(int i=0;i<links.size();++i) {
			DHLink bone=links.get(i);
			// save the initial theta for each link
			thetaAtTestStart[i] = links.get(i).getTheta();
			// Use the poseWorld for each DHLink to adjust the model origins.
			bone.refreshDHMatrix();
			bone.shapeEntity.setModel(models[i].getModel());
			bone.shapeEntity.setMaterial(models[i].getMaterial());
			Matrix4d iWP = bone.getPoseWorld();
			iWP.invert();
			bone.setShapeMatrix(iWP);
			// only allow theta adjustments of DH parameters
			bone.flags = LinkAdjust.THETA;
		}

		endEffectorTarget.setPoseWorld(endEffector.getPoseWorld());
	}
	
	private void testEnd() {
		Matrix4d identity = new Matrix4d();
		identity.setIdentity();
		
		// restore the initial theta for each link
		for(int i=0;i<links.size();++i) {
			DHLink bone=links.get(i);
			// allow editing of all DH parameters
			bone.flags = LinkAdjust.ALL;
			// undo changes to theta values
			bone.setTheta(thetaAtTestStart[i]);
			bone.refreshDHMatrix();
			// set all models back to world origin
			bone.setShapeMatrix(identity);
		}
		endEffectorTarget.setPoseWorld(endEffector.getPoseWorld());
	}
}
