package com.marginallyclever.robotoverlord.systems.render.mesh;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.AABB;
import com.marginallyclever.convenience.Ray;
import com.marginallyclever.convenience.RayHit;
import com.marginallyclever.convenience.helpers.IntersectionHelper;
import com.marginallyclever.convenience.helpers.OpenGLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * {@link Mesh} contains the vertex, normal, maybe color, and maybe texture data for a 3D model.
 * It uses Vertex Buffer Objects to optimize rendering large collections of triangles.
 * @author Dan Royer
 */
public class Mesh {
	private static final Logger logger = LoggerFactory.getLogger(Mesh.class);
	public static final int NUM_BUFFERS=5;  // verts, normals, colors, textureCoordinates, index
	public static final int BYTES_PER_INT = Integer.SIZE/8;
	public static final int BYTES_PER_FLOAT = Float.SIZE/8;

	public final transient List<Float> vertexArray = new ArrayList<>();
	public final transient List<Float> normalArray = new ArrayList<>();
	public final transient List<Float> colorArray = new ArrayList<>();
	public final transient List<Float> textureArray = new ArrayList<>();
	public final transient List<Integer> indexArray = new ArrayList<>();

	private transient boolean hasNormals = false;
	private transient boolean hasColors = false;
	private transient boolean isTransparent = false;
	private transient boolean hasTextures = false;
	private transient boolean hasIndexes = false;
	private transient boolean isDirty = false;
	private transient boolean isLoaded = false;

	private transient int[] VAO;
	private transient int[] VBO;

	public int renderStyle = GL3.GL_TRIANGLES;
	private String fileName = null;

	// bounding limits
	protected final AABB AABB = new AABB();

	public Mesh() {
		super();
		AABB.setShape(this);
	}

	public Mesh(int renderStyle) {
		this();
		this.renderStyle = renderStyle;
	}
	
	/**
	 * Remove all vertexes, normals, colors, texture coordinates, etc.
	 * on the next call to systems() the mesh will be rebuilt to nothing.
	 */
	public void clear() {
		vertexArray.clear();
		normalArray.clear();
		colorArray.clear();
		textureArray.clear();
		indexArray.clear();
		isDirty=true;
	}

	public void setSourceName(String filename) {
		this.fileName = filename;
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

	public boolean isTransparent() {
		return isTransparent;
	}

	public void unload(GL3 gl) {
		if(!isLoaded) return;
		isLoaded=false;
		destroyBuffers(gl);
	}
	
	private void createBuffers(GL3 gl) {
		VAO = new int[1];
		gl.glGenVertexArrays(1, VAO, 0);
		OpenGLHelper.checkGLError(gl,logger);

		VBO = new int[NUM_BUFFERS];
		gl.glGenBuffers(NUM_BUFFERS, VBO, 0);
		OpenGLHelper.checkGLError(gl,logger);
	}

	private void destroyBuffers(GL3 gl) {
		if(VBO != null) {
			gl.glDeleteBuffers(NUM_BUFFERS, VBO, 0);
			VBO = null;
		}
		if(VAO != null) {
			gl.glDeleteVertexArrays(1, VAO, 0);
			VAO = null;
		}
	}
	
	/**
	 * Regenerate the optimized rendering buffers for the fixed function pipeline.
	 * Also recalculate the bounding box.
	 * @param gl the OpenGL context
	 */
	private void updateBuffers(GL3 gl) {
		long numVertexes = getNumVertices();

		gl.glBindVertexArray(VAO[0]);
		OpenGLHelper.checkGLError(gl,logger);

		disableAllVertexAttribArrays(gl);

		int attribIndex=0;
		setupArray(gl,attribIndex++,3,numVertexes,vertexArray);
		if(hasNormals ) setupArray(gl,attribIndex++,3,numVertexes,normalArray );
		else gl.glDisableVertexAttribArray(attribIndex++);
		if(hasColors  ) setupArray(gl,attribIndex++,4,numVertexes,colorArray  );
		else gl.glDisableVertexAttribArray(attribIndex++);
		if(hasTextures) setupArray(gl,attribIndex++,2,numVertexes,textureArray);
		else gl.glDisableVertexAttribArray(attribIndex++);

		if(hasIndexes) {
			IntBuffer data = IntBuffer.allocate(indexArray.size());
			for (Integer integer : indexArray) data.put(integer);
			data.rewind();
			gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, VBO[4]);
			gl.glBufferData(GL3.GL_ELEMENT_ARRAY_BUFFER, (long) indexArray.size() *BYTES_PER_INT, data, GL3.GL_STATIC_DRAW);
		}

		gl.glBindVertexArray(0);
	}

	private void disableAllVertexAttribArrays(GL3 gl) {
		int[] maxAttribs = new int[1];
		gl.glGetIntegerv(GL3.GL_MAX_VERTEX_ATTRIBS, maxAttribs, 0);
		for(int i=0;i<maxAttribs[0];i++) gl.glDisableVertexAttribArray(i);
	}

	private void bindArray(GL3 gl, int attribIndex, int size) {
		gl.glEnableVertexAttribArray(attribIndex);
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, VBO[attribIndex]);
		gl.glVertexAttribPointer(attribIndex,size,GL3.GL_FLOAT,false,0,0);
		OpenGLHelper.checkGLError(gl,logger);
	}

	private void setupArray(GL3 gl, int attribIndex, int size, long numVertexes,List<Float> list) {
		FloatBuffer data = FloatBuffer.allocate(list.size());
		for( Float f : list ) data.put(f);
		data.rewind();
		bindArray(gl,attribIndex,size);
		gl.glBufferData(GL3.GL_ARRAY_BUFFER, numVertexes*size*BYTES_PER_FLOAT, data, GL3.GL_STATIC_DRAW);
		OpenGLHelper.checkGLError(gl,logger);
	}

	public void render(GL3 gl) {
		if(!isLoaded) {
			isLoaded=true;
			isDirty=true;
		}
		if(isDirty) {
			createBuffers(gl);
			updateBuffers(gl);
			isDirty=false;
		}

		gl.glBindVertexArray(VAO[0]);
		OpenGLHelper.checkGLError(gl,logger);

		int attribIndex=0;
		bindArray(gl,attribIndex++,3);
		if(hasNormals ) bindArray(gl,attribIndex++,3);
		if(hasColors  ) bindArray(gl,attribIndex++,4);
		if(hasTextures) bindArray(gl,attribIndex++,2);

		if (hasIndexes) {
			gl.glDrawElements(renderStyle, indexArray.size(), GL3.GL_UNSIGNED_INT, 0);
		} else {
			gl.glDrawArrays(renderStyle, 0, getNumVertices());
		}
		OpenGLHelper.checkGLError(gl,logger);
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
		if(a!=1) isTransparent=true;
		hasColors=true;
	}
	
	public void addTexCoord(float u,float v) {
		textureArray.add(u);
		textureArray.add(v);
		hasTextures =true;
	}
	
	public void addIndex(int n) {
		indexArray.add(n);
		hasIndexes=true;
	}
	
	/**
	 * Force recalculation of the minimum bounding box to contain this STL file.
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
			x = fi.next();
			y = fi.next();
			z = fi.next();
			boundTop.x = Math.max(x, boundTop.x);
			boundTop.y = Math.max(y, boundTop.y);
			boundTop.z = Math.max(z, boundTop.z);
			boundBottom.x = Math.min(x, boundBottom.x);
			boundBottom.y = Math.min(y, boundBottom.y);
			boundBottom.z = Math.min(z, boundBottom.z);
		}
		AABB.setBounds(boundTop, boundBottom);
	}

	public AABB getCuboid() {
		return AABB;
	}
	
	public int getNumTriangles() {
		return vertexArray.size()/9;
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

	public Vector3d getNormal(int t) {
		t*=3;
		double x = normalArray.get(t++);
		double y = normalArray.get(t++);
		double z = normalArray.get(t++);
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

	public boolean getHasTextures() {
		return hasTextures;
	}

	public boolean getHasIndexes() {
		return hasIndexes;
	}

	public void setRenderStyle(int style) {
		renderStyle = style;
	}

	public int getRenderStyle() {
		return renderStyle;
	}

	/**
	 * Intersect a ray with this mesh.
	 * @param ray The ray to intersect with.
	 * @return The RayHit object containing the intersection point and normal, or null if no intersection.
	 */
	public RayHit intersect(Ray ray) {

		if( renderStyle != GL3.GL_TRIANGLES &&
			renderStyle != GL3.GL_TRIANGLE_FAN &&
			renderStyle != GL3.GL_TRIANGLE_STRIP) return null;

		VertexProvider vp;
		if (hasIndexes) {
			vp = new VertexProvider() {
				@Override
				public Vector3d provideVertex(int index) {
					return getVertex(indexArray.get(index));
				}
				@Override
				public Vector3d provideNormal(int index) {
					return getNormal(indexArray.get(index));
				}
				@Override
				public int provideCount() {
					return indexArray.size();
				}
			};
		} else {
			vp = new VertexProvider() {
				@Override
				public Vector3d provideVertex(int index) {
					return getVertex(index);
				}
				@Override
				public Vector3d provideNormal(int index) {
					return getNormal(index);
				}
				@Override
				public int provideCount() {
					return getNumVertices();
				}
			};
		}

		return intersect(ray,vp);
	}


	/**
	 *
	 * @param ray the ray to intersect with
	 * @param provider a VertexProvider that will provide the vertices and normals of the triangles to intersect with
	 * @return null if no intersection, otherwise a RayHit object with the intersection point and normal.
	 */
	private RayHit intersect(Ray ray,VertexProvider provider) {
		int a=0,b=0,c=0;

		double nearest = Double.MAX_VALUE;
		for(int i=0;i<provider.provideCount();i+=3) {
			Vector3d v0 = provider.provideVertex(i);
			Vector3d v1 = provider.provideVertex(i+1);
			Vector3d v2 = provider.provideVertex(i+2);
			double t = IntersectionHelper.rayTriangle(ray, v0, v1, v2);
			if(nearest > t) {
				nearest = t;
				a=i;
				b=i+1;
				c=i+2;
			}
		}

		if(nearest<ray.getMaxDistance()) {
			Vector3d normal;
			if(hasNormals) {
				normal =   provider.provideNormal(a);
				normal.add(provider.provideNormal(b));
				normal.add(provider.provideNormal(c));
				normal.normalize();
			} else {
				Vector3d v0 = provider.provideVertex(a);
				Vector3d v1 = provider.provideVertex(b);
				Vector3d v2 = provider.provideVertex(c);
				normal = IntersectionHelper.buildNormalFrom3Points(v0, v1, v2);
			}
			return new RayHit(null,nearest,normal);
		}
		return null;
	}
}
