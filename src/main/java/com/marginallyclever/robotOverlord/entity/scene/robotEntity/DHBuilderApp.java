package com.marginallyclever.robotOverlord.entity.scene.robotEntity;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFileChooser;
import javax.vecmath.Matrix4d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
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
	public static final int NUM_BONES=6;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected ModelEntity anchor = new ModelEntity();
	protected ModelEntity [] models = new ModelEntity[NUM_BONES];
	protected DHLink [] links = new DHLink[NUM_BONES];
	protected String [] name = { "X", "Y", "Z", "U", "V", "W" };
	protected MaterialEntity mat = new MaterialEntity();
	
	public DHBuilderApp() {
		super();
		setName("DHBuilderApp");

		addChild(anchor);
		anchor.setName("Anchor");
		anchor.setMaterial(mat);
		
		for(int i=0;i<NUM_BONES;++i) {
			models[i] = new ModelEntity();
			models[i].setName("model "+name[i]);
			addChild(models[i]);
			models[i].setMaterial(mat);
		}
		
		Entity parent = this;
		for(int i=0;i<NUM_BONES;++i) {
			links[i] = new DHLink();
			links[i].setLetter(name[i]);
			links[i].flags = LinkAdjust.ALL;
			parent.addChild(links[i]);
			parent = links[i];
		}
		
		addChild(mat);
		mat.setDiffuseColor(1, 1, 1, 0.5f);
	}
	
	@Override
	public void render(GL2 gl2) {
		super.render(gl2);
		
		// draw all links
		gl2.glPushMatrix();
		for( DHLink link : links ) {
			MatrixHelper.applyMatrix(gl2, pose.get());
			link.renderLineage(gl2);
		}
		gl2.glPopMatrix();
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
					if(bone.getModel()!=null) {
						Matrix4d iWP = bone.getPoseWorld();
						iWP.invert();
						bone.getModel().adjustMatrix(iWP);
					}
				}
			}
		});
		
		ViewElement saveButton = view.addButton("Save");
		saveButton.addObserver(new Observer() {
			@Override
			public void update(Observable arg0, Object arg1) {
				RobotOverlord ro = (RobotOverlord)getRoot();
				
				for(int i=0;i<links.length;++i) {
					DHLink bone=links[i];
					ro.saveEntityToFileJSON(links[i].getName()+File.separator+".json",bone);
				}
			}
		});
		
		ViewElement loadButton = view.addButton("Load");
		loadButton.addObserver(new Observer() {
			@Override
			public void update(Observable arg0, Object arg1) {
				//private static 
				String lastPath=System.getProperty("user.dir");
				String [] extensions = { ".obj",".stl" };
				RobotOverlord ro = (RobotOverlord)getRoot();

				//https://stackoverflow.com/questions/4779360/browse-for-folder-dialog
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if( lastPath!=null ) chooser.setCurrentDirectory(new File(lastPath));
				if( chooser.showOpenDialog(ro.getMainFrame()) == JFileChooser.APPROVE_OPTION ) {
					File yourFolder = chooser.getSelectedFile();
					for(int i=0;i<NUM_BONES;++i) {
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
						links[i] = ro.loadEntityFromFileJSON(File.separator+links[i].getName()+".json");
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
		});
		
		view.popStack();
		
		// TODO Auto-generated method stub
		super.getView(view);
	}
}
