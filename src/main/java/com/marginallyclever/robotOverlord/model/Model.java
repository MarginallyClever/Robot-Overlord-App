package com.marginallyclever.robotOverlord.model;

import java.io.Serializable;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import javax.vecmath.Vector3f;

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
	
	public final static int NUM_BUFFERS=4;  // verts, normals, textureCoordinates
	
	protected String sourceName;
	protected transient boolean isLoaded;
	public transient ArrayList<Float> vertexArray;
	public transient ArrayList<Float> normalArray;
	public transient ArrayList<Float> colorArray;
	public transient ArrayList<Float> texCoordArray;
	public int renderStyle; 
	
	protected transient FloatBuffer vertices;
	protected transient FloatBuffer normals;
	protected transient FloatBuffer colors;
	protected transient FloatBuffer texCoords;
	protected transient int VBO[];

	public transient boolean hasNormals;
	public transient boolean hasColors;
	public transient boolean hasTextureCoordinates;
	
	protected transient float loadScale;
	public transient boolean isDirty;
	
	// origin adjust
	protected Vector3f adjustOrigin;

	
	public Model() {
		sourceName=null;
		isLoaded=false;
		vertexArray = new ArrayList<Float>();
		normalArray = new ArrayList<Float>();
		colorArray = new ArrayList<Float>();
		texCoordArray = new ArrayList<Float>();
		
		adjustOrigin = new Vector3f();
		loadScale=1.0f;
		VBO = null;
		hasNormals=false;
		hasColors=false;
		hasTextureCoordinates=false;
		renderStyle = GL2.GL_TRIANGLES;
		isDirty=false;
	}

	
	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
		isDirty=true;
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
			vertices.put(j++, px*loadScale+(float)adjustOrigin.x);
			vertices.put(j++, py*loadScale+(float)adjustOrigin.y);
			vertices.put(j++, pz*loadScale+(float)adjustOrigin.z);
		}
		
		

		int totalBufferSize = numVertexes;
		int s=(Float.SIZE/8);  // bits per float / bits per byte = bytes per float
		int vboIndex=0;
		
		// bind a buffer
		vertices.rewind();
		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, VBO[vboIndex]);
	    // Write out vertex buffer to the currently bound VBO.
	    gl2.glBufferData(GL2.GL_ARRAY_BUFFER, totalBufferSize*3*s, vertices, GL2.GL_STATIC_DRAW);
	    vboIndex++;
	    
		if(hasNormals) {
		    // repeat for normals
			normals = FloatBuffer.allocate(normalArray.size());
			fi = normalArray.iterator();
			while(fi.hasNext()) {
				normals.put(fi.next().floatValue());
			}
			
			normals.rewind();
			gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, VBO[vboIndex]);
		    gl2.glBufferData(GL2.GL_ARRAY_BUFFER, totalBufferSize*3*s, normals, GL2.GL_STATIC_DRAW);
		    vboIndex++;
		}

		if(hasColors) {
		    // repeat for normals
			colors = FloatBuffer.allocate(colorArray.size());
			fi = colorArray.iterator();
			while(fi.hasNext()) {
				colors.put(fi.next().floatValue());
			}
			
			colors.rewind();
			gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, VBO[vboIndex]);
		    gl2.glBufferData(GL2.GL_ARRAY_BUFFER, totalBufferSize*3*s, colors, GL2.GL_STATIC_DRAW);
		    vboIndex++;
		}
		
		if(hasTextureCoordinates) {
		    // repeat for textures
			texCoords = FloatBuffer.allocate(texCoordArray.size());
			fi = texCoordArray.iterator();
			while(fi.hasNext()) {
				texCoords.put(fi.next().floatValue());
			}
			
		    texCoords.rewind();
			gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, VBO[vboIndex]);
		    gl2.glBufferData(GL2.GL_ARRAY_BUFFER, numVertexes*2*s, texCoords, GL2.GL_STATIC_DRAW);
		    vboIndex++;
		}
	}
	
	
	public void render(GL2 gl2) {
		if(!isLoaded) {
			createBuffers(gl2);
			updateBuffers(gl2);
			isLoaded=true;
		}
		if(isDirty) {
			updateBuffers(gl2);
			isDirty=false;
		}
		if(VBO==null) return;
		
		int vboIndex=0;
		gl2.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		// Bind the vertex buffer to work with
		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, VBO[vboIndex++]);
		gl2.glVertexPointer(3, GL2.GL_FLOAT, 0, 0);
	    
		if(hasNormals) {
			gl2.glEnableClientState(GL2.GL_NORMAL_ARRAY);
			// Bind the normal buffer to work with
			gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, VBO[vboIndex++]);
			gl2.glNormalPointer(GL2.GL_FLOAT, 0, 0);
		}
		if(hasColors) {
			gl2.glEnableClientState(GL2.GL_COLOR_ARRAY);
			// Bind the clor buffer to work with
			gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, VBO[vboIndex++]);
			gl2.glNormalPointer(GL2.GL_FLOAT, 0, 0);
		}
		if(hasTextureCoordinates) {
			gl2.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
			// Bind the texture buffer to work with
			gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, VBO[vboIndex++]);
			gl2.glTexCoordPointer(2, GL2.GL_FLOAT, 0, 0);
		}
		
		int count=vertexArray.size()/3;
		if(renderStyle==GL2.GL_POINTS) {
			count*=3;
		}
		gl2.glDrawArrays(renderStyle, 0, count);
		//gl2.glDrawArrays(GL2.GL_LINE_LOOP, 0, count);
		
		gl2.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		gl2.glDisableClientState(GL2.GL_NORMAL_ARRAY);
		gl2.glDisableClientState(GL2.GL_COLOR_ARRAY);
		gl2.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
	}
	
	
	/**
	 * Translate all the vertexes by a given amount
	 * @param dx amount to translate on X axis
	 * @param dy amount to translate on Y axis
	 * @param dz amount to translate on Z axis
	 */
	public void adjustOrigin(Vector3f arg0) {
		if(!adjustOrigin.epsilonEquals(arg0, 0.01f)) {
			adjustOrigin = new Vector3f(arg0);
			isDirty=true;
		}
	}
	
	public Vector3f getAdjustOrigin() {
		return new Vector3f(adjustOrigin);
	}
	
	public void setScale(float arg0) {
		if(loadScale!=arg0) {
			loadScale=arg0;
			isDirty=true;
		}
	}
	public float getScale() {
		return loadScale;
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
