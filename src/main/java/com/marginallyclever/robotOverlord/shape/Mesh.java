package com.marginallyclever.robotOverlord.shape;

import java.io.BufferedInputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ServiceLoader;

import javax.vecmath.Point3d;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.FileAccess;

/**
 * {@link Mesh} contains the vertex, normal, maybe color, and maybe texture data for a 3D model.
 * It uses Vertex Buffer Objects to optimize rendering large collections of triangles.
 * @author Dan Royer
 */
public class Mesh {
	public final static int NUM_BUFFERS=4;  // verts, normals, colors, textureCoordinates

	// the pool of all shapes loaded
	@JsonIgnore
	private static LinkedList<Mesh> meshPool = new LinkedList<Mesh>();
	
	protected String sourceName;
	protected transient ShapeLoadAndSave loader;
	protected transient boolean isLoaded;
	protected transient boolean unloadASAP;
	
	public transient ArrayList<Float> vertexArray = new ArrayList<Float>();
	public transient ArrayList<Float> normalArray = new ArrayList<Float>();
	public transient ArrayList<Float> colorArray = new ArrayList<Float>();
	public transient ArrayList<Float> texCoordArray = new ArrayList<Float>();
	public int renderStyle; 
	
	protected transient int VBO[];

	public transient boolean hasNormals;
	public transient boolean hasColors;
	public transient boolean hasUVs;
	
	// the mesh can only be optimized after OpenGL is ready, during rendering.
	// Loading may happen early.  This one-time flag remembers it needs to be done.
	public transient boolean isDirty;
	
	// bounding limits
	protected Cuboid cuboid = new Cuboid();

	public Mesh() {
		super();
		
		sourceName=null;
		loader=null;
		isLoaded=false;
		unloadASAP=false;
		VBO = null;
		hasNormals=false;
		hasColors=false;
		hasUVs=false;
		renderStyle = GL2.GL_TRIANGLES;
		isDirty=false;
		cuboid.setShape(this);
	}
	
	/**
	 * remove all vertexes, normals, colors, texture coordinates, etc.
	 */
	public void clear() {
		vertexArray.clear();
		normalArray.clear();
		colorArray.clear();
		texCoordArray.clear();
		isDirty=true;
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
	
	/**
	 * Regenerate the optimized rendering buffers for the fixed function pipeline.
	 * Also recalculate the bounding box.
	 * @param gl2
	 */
	private void updateBuffers(GL2 gl2) {
		int numVertexes = vertexArray.size()/3;
		Iterator<Float> fi;
		int j=0;

		FloatBuffer vertices = FloatBuffer.allocate(vertexArray.size());
		fi = vertexArray.iterator();
		while(fi.hasNext()) {
			vertices.put(j++, fi.next().floatValue());
			vertices.put(j++, fi.next().floatValue());
			vertices.put(j++, fi.next().floatValue());
		}

		int s=(Float.SIZE/8);  // bits per float / bits per byte = bytes per float
		int totalBufferSize = numVertexes*3*s;
		int vboIndex=0;
		
		// bind a buffer
		vertices.rewind();
		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, VBO[vboIndex]);
	    // Write out vertex buffer to the currently bound VBO.
	    gl2.glBufferData(GL2.GL_ARRAY_BUFFER, totalBufferSize, vertices, GL2.GL_STATIC_DRAW);
	    vboIndex++;
	    
		if(hasNormals) {
			j=0;
		    // repeat for normals
			FloatBuffer normals = FloatBuffer.allocate(normalArray.size());
			fi = normalArray.iterator();
			while(fi.hasNext()) {
				normals.put(fi.next().floatValue());
				normals.put(fi.next().floatValue());
				normals.put(fi.next().floatValue());
			}
			
			normals.rewind();
			gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, VBO[vboIndex]);
		    gl2.glBufferData(GL2.GL_ARRAY_BUFFER, totalBufferSize, normals, GL2.GL_STATIC_DRAW);
		    vboIndex++;
		}

		if(hasColors) {
		    // repeat for colors
			FloatBuffer colors = FloatBuffer.allocate(colorArray.size());
			fi = colorArray.iterator();
			while(fi.hasNext()) {
				colors.put(fi.next().floatValue());
			}
			
			colors.rewind();
			gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, VBO[vboIndex]);
		    gl2.glBufferData(GL2.GL_ARRAY_BUFFER, totalBufferSize, colors, GL2.GL_STATIC_DRAW);
		    vboIndex++;
		}
		
		if(hasUVs) {
		    // repeat for textures
			FloatBuffer texCoords = FloatBuffer.allocate(texCoordArray.size());
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
		if(unloadASAP) {
			unloadASAP=false;
			unload(gl2);
		}
		if(!isLoaded) {
			createBuffers(gl2);
			isDirty=true;
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
			// Bind the color buffer to work with
			gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, VBO[vboIndex++]);
			gl2.glColorPointer(4,GL2.GL_FLOAT, 0, 0);
		}
		if(hasUVs) {
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
	
	public void addNormal(float x,float y,float z) {
		normalArray.add(x);
		normalArray.add(y);
		normalArray.add(z);
		hasNormals=true;
	}
	
	public void addVertex(float x,float y,float z) {
		vertexArray.add(x);
		vertexArray.add(y);
		vertexArray.add(z);
	}
	
	public void addColor(float r,float g,float b,float a) {
		colorArray.add(r);
		colorArray.add(g);
		colorArray.add(b);
		colorArray.add(a);
	}
	
	public void addTexCoord(float x,float y) {
		texCoordArray.add(x);
		texCoordArray.add(y);
		hasUVs=true;
	}
	
	/**
	 * Force recalculation of the the minimum bounding box to contain this STL file.
	 * Done automatically every time updateBuffers() is called.
	 * Meaningless if there is no vertexArray of points.
	 */
	public void updateCuboid() {
		Point3d boundBottom = new Point3d(Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE);
		Point3d boundTop = new Point3d(-Double.MAX_VALUE,-Double.MAX_VALUE,-Double.MAX_VALUE);
		
		// transform and calculate
		Iterator<Float> fi = vertexArray.iterator();
		double x,y,z;
		while(fi.hasNext()) {
			x = fi.next().floatValue();
			y = fi.next().floatValue();
			z = fi.next().floatValue();
			boundTop.x = Math.max(x, boundTop.x);
			boundTop.y = Math.max(y, boundTop.y);
			boundTop.z = Math.max(z, boundTop.z);
			boundBottom.x = Math.min(x, boundBottom.x);
			boundBottom.y = Math.min(y, boundBottom.y);
			boundBottom.z = Math.min(z, boundBottom.z);
		}
		cuboid.setBounds(boundTop, boundBottom);
	}

	public Cuboid getCuboid() {
		return cuboid;
	}
	
	public int getNumTriangles() {
		return vertexArray.size()/3;
	}

	public ShapeLoadAndSave getLoader() {
		return loader;
	}

	public void setLoader(ShapeLoadAndSave loader) {
		this.loader = loader;
	}

	/**
	 * Makes sure to only load one instance of each source file.  Loads all the data immediately.
	 * @param sourceName file from which to load.  may be filename.ext or zipfile.zip:filename.ext
	 * @return the instance.
	 * @throws Exception if file cannot be read successfully
	 */
	public static Mesh createModelFromFilename(String sourceName) throws Exception {
		if(sourceName == null || sourceName.trim().length()==0) return null;
		
		// find the existing shape in the pool
		Iterator<Mesh> iter = meshPool.iterator();
		while(iter.hasNext()) {
			Mesh m = iter.next();
			if(m.getSourceName().equals(sourceName)) {
				return m;
			}
		}
		
		Mesh m=null;
		
		// not in pool.  Find a serviceLoader that can load this file type.
		ServiceLoader<ShapeLoadAndSave> loaders = ServiceLoader.load(ShapeLoadAndSave.class);
		Iterator<ShapeLoadAndSave> i = loaders.iterator();
		int count=0;
		while(i.hasNext()) {
			count++;
			ShapeLoadAndSave loader = i.next();
			if(loader.canLoad() && loader.canLoad(sourceName)) {
				BufferedInputStream stream = FileAccess.open(sourceName);
				m=new Mesh();
				if(loader.load(stream,m)) {
					m.setSourceName(sourceName);
					m.setLoader(loader);
					m.updateCuboid();
					// Maybe add a m.setSaveAndLoader(loader); ?
					meshPool.add(m);
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
}
