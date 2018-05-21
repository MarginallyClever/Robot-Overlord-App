package com.marginallyclever.robotOverlord.material;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.junit.Test;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.marginallyclever.convenience.FileAccess;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;


public class Material extends Entity {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5412746663260874116L;
	
	public float[] diffuse		= {1.00f,1.00f,1.00f,1.00f};
	public float[] specular 	= {0.85f,0.85f,0.85f,1.00f};
	public float[] emission 	= {0.01f,0.01f,0.01f,1.00f};
	public float[] ambient		= {0.01f,0.01f,0.01f,1.00f};
	private float shininess		= 10.0f;
	private Texture texture     = null;
	private boolean isLit		= true;
	private String textureFilename = new String();
	private transient boolean textureDirty;
	private transient MaterialControlPanel materialPanel;
	
	
	public Material() {
		textureDirty=true;
	}
	
	
	public ArrayList<JPanel> getContextPanel(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getContextPanel(gui);
		if(list==null) list = new ArrayList<JPanel>();
		
		materialPanel = new MaterialControlPanel(gui,this);
		list.add(materialPanel);
		
		return list;
	}
	
	public void render(GL2 gl2) {
		gl2.glColor4f(diffuse[0],diffuse[1],diffuse[2],diffuse[3]);
		gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, diffuse,0);
	    gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, specular,0);
	    gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION, emission,0);
	    gl2.glMaterialf(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, shininess);
	    gl2.glColorMaterial(GL2.GL_FRONT_AND_BACK,GL2.GL_AMBIENT_AND_DIFFUSE );
	    
	    boolean isColorEnabled = gl2.glIsEnabled(GL2.GL_COLOR_MATERIAL);
		gl2.glDisable(GL2.GL_COLOR_MATERIAL);
		
		gl2.glShadeModel(GL2.GL_SMOOTH);
	    if(isLit()) {
	    	gl2.glEnable(GL2.GL_LIGHTING);
	    } else {
	    	gl2.glDisable(GL2.GL_LIGHTING);
	    }

		if(textureDirty) {
			// texture has changed, load the new texture.
			try {
				if(textureFilename == null || textureFilename.length()==0) texture = null;
				else {
					texture = TextureIO.newTexture(FileAccess.open(textureFilename), false, textureFilename.substring(textureFilename.lastIndexOf('.')+1));
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
			textureDirty=false;
		}
	    if(texture==null) {
			gl2.glDisable(GL2.GL_TEXTURE_2D);
	    } else {
			gl2.glEnable(GL2.GL_TEXTURE_2D);
	    	texture.bind(gl2);
	    }
	    
	    if(isColorEnabled) gl2.glEnable(GL2.GL_COLOR_MATERIAL);
	}
	

	public void setShininess(float arg0) {
		if(arg0>128) arg0=128;
		if(arg0<0) arg0=0;
		shininess = arg0;
	}
	public float getShininess() {
		return shininess;
	}
	
	

	public void setDiffuseColor(float r,float g,float b,float a) {
		diffuse[0]=r;
		diffuse[1]=g;
		diffuse[2]=b;
		diffuse[3]=a;
	}
	

	public void setSpecularColor(float r,float g,float b,float a) {
		specular[0]=r;
		specular[1]=g;
		specular[2]=b;
		specular[3]=a;
	}
	

	public void setEmissionColor(float r,float g,float b,float a) {
		emission[0]=r;
		emission[1]=g;
		emission[2]=b;
		emission[3]=a;
	}
	

	public void setAmbientColor(float r,float g,float b,float a) {
		ambient[0]=r;
		ambient[1]=g;
		ambient[2]=b;
		ambient[3]=a;
	}
    
	public float[] getDiffuseColor() {
		return diffuse.clone();
	}

	public float[] getAmbientColor() {
		return ambient.clone();
	}
	
	public float[] getSpecular() {
		return specular.clone();
	}
	
	
	public void setTextureFilename(String arg0) {
		if(arg0==null) arg0 = new String();
		if(textureFilename==null) textureFilename =  new String();
		if(arg0.equals(textureFilename)) {
			return;
		}
		textureFilename = arg0;
		textureDirty = true;
	}
	public String getTextureFilename() {
		return textureFilename;
	}

	public boolean isLit() {
		return isLit;
	}

	public void setLit(boolean isLit) {
		this.isLit = isLit;
	}
	
	@Test
	public void testLoadAndSave() {
		Material m1 = new Material();
		@SuppressWarnings("unused")
		Material m2;
		
		File tempFile;
		try {
			tempFile = File.createTempFile("test", "ro");
		} catch (IOException e1) {
			System.out.println("Temp file not created.");
			e1.printStackTrace();
			return;
		}
		tempFile.deleteOnExit();
		FileOutputStream fout=null;
		ObjectOutputStream objectOut=null;
		try {
			fout = new FileOutputStream(tempFile.getCanonicalPath());
			objectOut = new ObjectOutputStream(fout);
			objectOut.writeObject(m1);
		} catch(java.io.NotSerializableException e) {
			System.out.println("World can't be serialized.");
			e.printStackTrace();
		} catch(IOException e) {
			System.out.println("World save failed.");
			e.printStackTrace();
		} finally {
			if(objectOut!=null) {
				try {
					objectOut.close();
				} catch(IOException e) {}
			}
			if(fout!=null) {
				try {
					fout.close();
				} catch(IOException e) {}
			}
		}
		

		FileInputStream fin=null;
		ObjectInputStream objectIn=null;
		try {
			// Create a file input stream
			fin = new FileInputStream(tempFile.getCanonicalPath());
	
			// Create an object input stream
			objectIn = new ObjectInputStream(fin);
	
			// Read an object in from object store, and cast it to a GameWorld
			m2 = (Material) objectIn.readObject();
		} catch(IOException e) {
			System.out.println("Material load failed (file io).");
			e.printStackTrace();
		} catch(ClassNotFoundException e) {
			System.out.println("Material load failed (class not found)");
			e.printStackTrace();
		} finally {
			if(objectIn!=null) {
				try {
					objectIn.close();
				} catch(IOException e) {}
			}
			if(fin!=null) {
				try {
					fin.close();
				} catch(IOException e) {}
			}
		}
		
		// TODO compare m1 and m2
	}
}
