package com.marginallyclever.robotOverlord.shape;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;

/**
 * {@link Mesh} contains the vertex, normal, maybe color, and maybe texture data for a 3D model.
 * It uses Vertex Buffer Objects to optimize rendering large collections of triangles.
 * @author Dan Royer
 */
public class Mesh {
	public final static int NUM_BUFFERS=5;  // verts, normals, colors, textureCoordinates,index
	
	public final transient List<Float> vertexArray = new ArrayList<>();

	public final transient List<Float> normalArray = new ArrayList<>();
	private transient boolean hasNormals;
	
	public final transient List<Float> colorArray = new ArrayList<>();
	private transient boolean hasColors;

	public final transient List<Float> texCoordArray = new ArrayList<>();
	private transient boolean hasUVs;

	public final transient List<Integer> indexArray = new ArrayList<>();
	private transient boolean hasIndexes;

	// the mesh can only be optimized after OpenGL is ready, during rendering.
	// Loading may happen early.  This one-time flag remembers it needs to be done.
	private transient boolean isDirty;

	private transient boolean isLoaded;
	private transient int[] VBO;
	public int renderStyle; 
	private String fileName;
	
	// bounding limits
	protected final Cuboid cuboid = new Cuboid();

	public Mesh() {
		super();
		
		fileName=null;
		isLoaded=false;
		VBO = null;
		hasNormals=false;
		hasColors=false;
		hasUVs=false;
		hasIndexes=false;
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
		indexArray.clear();
		isDirty=true;
	}

	public void setSourceName(String filename) {
		this.fileName = filename;
		isDirty=true;
	}
	
	public String getSourceName() {
		return fileName;
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

		final int BYTES_PER_FLOAT=(Float.SIZE/8);  // bits per float / bits per byte = bytes per float
		int totalBufferSize = numVertexes*3*BYTES_PER_FLOAT;
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
		    gl2.glBufferData(GL2.GL_ARRAY_BUFFER, numVertexes*2*BYTES_PER_FLOAT, texCoords, GL2.GL_STATIC_DRAW);
		    vboIndex++;
		}
		
		if(hasIndexes) {
			IntBuffer indexes = IntBuffer.allocate(indexArray.size());
			Iterator<Integer> ii = indexArray.iterator();
			while(ii.hasNext()) {
				indexes.put(ii.next().intValue());
			}
			final int BYTES_PER_INT = Integer.SIZE/8;
			indexes.rewind();
			gl2.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, VBO[vboIndex]);
			gl2.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER, indexArray.size()*BYTES_PER_INT, indexes, GL2.GL_STATIC_DRAW);
		    vboIndex++;
		}
	}
	
	public void render(GL2 gl2) {
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
		
		if(hasIndexes) {
			gl2.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, VBO[vboIndex++]);
			gl2.glDrawElements(renderStyle, indexArray.size(), GL2.GL_UNSIGNED_INT, 0);
		} else {
			int count=vertexArray.size();
			if(renderStyle!=GL2.GL_POINTS) count/=3;
			gl2.glDrawArrays(renderStyle, 0, count);
		}
		
		gl2.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		gl2.glDisableClientState(GL2.GL_NORMAL_ARRAY);
		gl2.glDisableClientState(GL2.GL_COLOR_ARRAY);
		gl2.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		gl2.glDisableClientState(GL2.GL_ELEMENT_ARRAY_BUFFER);
	}
	
	public void drawNormals(GL2 gl2) {
		if(!hasNormals) return;
		
		double scale=2;
		
		gl2.glBegin(GL2.GL_LINES);
		for(int i=0;i<vertexArray.size();i+=3) {
			double px = vertexArray.get(i);
			double py = vertexArray.get(i+1);
			double pz = vertexArray.get(i+2);
			gl2.glVertex3d(px, py, pz);

			double nx = normalArray.get(i);
			double ny = normalArray.get(i+1);
			double nz = normalArray.get(i+2);
			
			gl2.glVertex3d( px + nx*scale, 
							py + ny*scale,
							pz + nz*scale);
		}
		gl2.glEnd();
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
		hasColors=true;
	}
	
	public void addTexCoord(float x,float y) {
		texCoordArray.add(x);
		texCoordArray.add(y);
		hasUVs=true;
	}
	
	public void addIndex(int n) {
		indexArray.add(n);
		hasIndexes=true;
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

	public int getNumVertices() {
		return (vertexArray==null) ? 0 : vertexArray.size()/3;
	}

	public Vector3d getVertex(int t) {
		t*=3;
		double x = vertexArray.get(t++); 
		double y = vertexArray.get(t++); 
		double z = vertexArray.get(t++); 
		return new Vector3d(x,y,z);
	}


	public boolean isDirty() {
		return isDirty;
	}

	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}

	public boolean getHasNormals() {
		return hasNormals;
	}

	public boolean getHasColors() {
		return hasColors;
	}

	public boolean getHasUVs() {
		return hasUVs;
	}

	public boolean getHasIndexes() {
		return hasIndexes;
	}
}
