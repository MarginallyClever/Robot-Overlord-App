package com.marginallyclever.robotOverlord.model;

import java.io.Serializable;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import com.jogamp.opengl.GL2;

/**
 * contains the vertex, normal, and texture data for a 3D model.
 * @author dan royer
 *
 */
public class Model implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7136313382885361812L;

	public final static int NUM_BUFFERS=3;  // verts, normals, textureCoordinates
	
	protected transient String sourceName;
	protected transient boolean isLoaded;
	public ArrayList<Float> vertexArray = new ArrayList<Float>();
	public ArrayList<Float> normalArray = new ArrayList<Float>();
	public ArrayList<Float> texCoordArray = new ArrayList<Float>();
	
	protected transient FloatBuffer vertices;
	protected transient FloatBuffer normals;
	protected transient FloatBuffer texCoords;
	protected transient int VBO[];

	public transient boolean hasNormals;
	public transient boolean hasTextureCoordinates;
	
	public transient float loadScale;
	
	protected float adjustX,adjustY,adjustZ;

	
	public Model() {
		adjustX=adjustY=adjustZ=0;
		isLoaded=false;
		loadScale=1.0f;
		VBO = null;
		sourceName=null;
		hasNormals=false;
		hasTextureCoordinates=false;
	}

	
	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	
	public String getSourceName() {
		return sourceName;
	}

	
	public boolean isLoaded() {
		return isLoaded;
	}

	
	public void setLoaded(boolean loaded) {
		isLoaded=loaded;
	}
	

	public void unload(GL2 gl2) {
		if(!isLoaded) return;
		if(VBO == null) return;
		gl2.glDeleteBuffers(NUM_BUFFERS, VBO,0);
		VBO=null;
		isLoaded=false;
	}
	
	

	
	
	private void createBuffers(GL2 gl2) {
		VBO = new int[NUM_BUFFERS];
		gl2.glGenBuffers(NUM_BUFFERS, VBO, 0);
	}
	
	
	private void updateBuffers(GL2 gl2) {
		int numVertexes = vertexArray.size()/3;
		Iterator<Float> fi;
		int j=0;

		vertices = FloatBuffer.allocate(vertexArray.size());
		fi = vertexArray.iterator();
		while(fi.hasNext()) {
			float px = fi.next().floatValue();
			float py = fi.next().floatValue();
			float pz = fi.next().floatValue();
			vertices.put(j++, px*loadScale+adjustX);
			vertices.put(j++, py*loadScale+adjustY);
			vertices.put(j++, pz*loadScale+adjustZ);
		}
		
		normals = FloatBuffer.allocate(normalArray.size());
		fi = normalArray.iterator();
		while(fi.hasNext()) {
			normals.put(fi.next().floatValue());
		}
		
		texCoords = FloatBuffer.allocate(texCoordArray.size());
		fi = texCoordArray.iterator();
		while(fi.hasNext()) {
			texCoords.put(fi.next().floatValue());
		}

		int totalBufferSize = numVertexes*3;
		int s=(Float.SIZE/8);  // bits per float / bits per byte = bytes per float

		// bind a buffer
		vertices.rewind();
		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, VBO[0]);
	    // Write out vertex buffer to the currently bound VBO.
	    gl2.glBufferData(GL2.GL_ARRAY_BUFFER, totalBufferSize*s, vertices, GL2.GL_STATIC_DRAW);

		if(hasNormals) {
		    // repeat for normals
			normals.rewind();
			gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, VBO[1]);
		    gl2.glBufferData(GL2.GL_ARRAY_BUFFER, totalBufferSize*s, normals, GL2.GL_STATIC_DRAW);
		}
		if(hasTextureCoordinates) {
		    // repeat for textures
		    texCoords.rewind();
			gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, VBO[2]);
		    gl2.glBufferData(GL2.GL_ARRAY_BUFFER, numVertexes*2*s, texCoords, GL2.GL_STATIC_DRAW);
		}
	}
	
	
	public void render(GL2 gl2) {
		if(!isLoaded) {
			createBuffers(gl2);
			updateBuffers(gl2);
			isLoaded=true;
		}
		if(VBO==null) return;
		
		gl2.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		// Bind the vertex buffer to work with
		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, VBO[0]);
		gl2.glVertexPointer(3, GL2.GL_FLOAT, 0, 0);
	    
		if(hasNormals) {
			gl2.glEnableClientState(GL2.GL_NORMAL_ARRAY);
			// Bind the normal buffer to work with
			gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, VBO[1]);
			gl2.glNormalPointer(GL2.GL_FLOAT, 0, 0);
		}
		if(hasTextureCoordinates) {
			gl2.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
			// Bind the texture buffer to work with
			gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, VBO[2]);
			gl2.glTexCoordPointer(2, GL2.GL_FLOAT, 0, 0);
		}
		
		gl2.glDrawArrays(GL2.GL_TRIANGLES, 0, vertexArray.size()/3);
		//gl2.glDrawArrays(GL2.GL_LINE_LOOP, 0, vertexArray.size()/3);
		
		gl2.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		gl2.glDisableClientState(GL2.GL_NORMAL_ARRAY);
		gl2.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
	}
	
	
	/**
	 * Translate all the vertexes by a given amount
	 * @param dx amount to translate on X axis
	 * @param dy amount to translate on Y axis
	 * @param dz amount to translate on Z axis
	 */
	public void adjustOrigin(float dx,float dy,float dz) {
		adjustX=dx;
		adjustY=dy;
		adjustZ=dz;
	}
	
	public void addNormal(float x,float y,float z) {
		normalArray.add(x);
		normalArray.add(y);
		normalArray.add(z);
	}
	public void addVertex(float x,float y,float z) {
		vertexArray.add(x);
		vertexArray.add(y);
		vertexArray.add(z);
	}
	public void addTexCoord(float x,float y) {
		texCoordArray.add(x);
		texCoordArray.add(y);
	}
}
