package com.marginallyclever.robotOverlord.entity.scene.modelEntity;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;


import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;

/**
 * contains the vertex, normal, and texture data for a 3D model.
 * @author dan royer
 *
 */
public class Model {
	public final static int NUM_BUFFERS=4;  // verts, normals, colors, textureCoordinates
	
	protected String sourceName;
	protected ModelLoadAndSave loader;
	protected transient boolean isLoaded;
	
	public transient ArrayList<Float> vertexArray = new ArrayList<Float>();
	public transient ArrayList<Float> normalArray = new ArrayList<Float>();
	public transient ArrayList<Float> colorArray = new ArrayList<Float>();
	public transient ArrayList<Float> texCoordArray = new ArrayList<Float>();
	public int renderStyle; 
	
	protected transient FloatBuffer vertices;
	protected transient FloatBuffer normals;
	protected transient FloatBuffer colors;
	protected transient FloatBuffer texCoords;
	protected transient int VBO[];

	public transient boolean hasNormals;
	public transient boolean hasColors;
	public transient boolean hasUVs;
	
	public transient boolean isDirty;
	// correction matrix
	protected Matrix4d adjust = new Matrix4d();
	// bounding limits
	protected Cuboid cuboid = new Cuboid();

	public Model() {
		super();
		
		sourceName=null;
		loader=null;
		isLoaded=false;
		VBO = null;
		hasNormals=false;
		hasColors=false;
		hasUVs=false;
		renderStyle = GL2.GL_TRIANGLES;
		isDirty=false;
		adjust.setIdentity();
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

		Point3d boundBottom = new Point3d(Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE);
		Point3d boundTop = new Point3d(-Double.MAX_VALUE,-Double.MAX_VALUE,-Double.MAX_VALUE);
		
		vertices = FloatBuffer.allocate(vertexArray.size());
		fi = vertexArray.iterator();
		Point3d p = new Point3d();
		while(fi.hasNext()) {
			p.x = fi.next().floatValue();
			p.y = fi.next().floatValue();
			p.z = fi.next().floatValue();
			adjust.transform(p);
			vertices.put(j++, (float)p.x);
			vertices.put(j++, (float)p.y);
			vertices.put(j++, (float)p.z);
			
			// also recalculate the bounding limits			
			if(boundBottom.x>p.x) boundBottom.x=p.x;
			if(boundBottom.y>p.y) boundBottom.y=p.y;
			if(boundBottom.z>p.z) boundBottom.z=p.z;
			if(boundTop.x<p.x) boundTop.x=p.x;
			if(boundTop.y<p.y) boundTop.y=p.y;
			if(boundTop.z<p.z) boundTop.z=p.z;
		}
		
		cuboid.setBounds(boundTop, boundBottom);

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
			Matrix3d pose = new Matrix3d();
			adjust.get(pose);
			normals = FloatBuffer.allocate(normalArray.size());
			fi = normalArray.iterator();
			while(fi.hasNext()) {
				p.x = fi.next().floatValue();
				p.y = fi.next().floatValue();
				p.z = fi.next().floatValue();
				pose.transform(p);
				normals.put(j++, (float)p.x);
				normals.put(j++, (float)p.y);
				normals.put(j++, (float)p.z);
			}
			
			normals.rewind();
			gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, VBO[vboIndex]);
		    gl2.glBufferData(GL2.GL_ARRAY_BUFFER, totalBufferSize, normals, GL2.GL_STATIC_DRAW);
		    vboIndex++;
		}

		if(hasColors) {
		    // repeat for colors
			colors = FloatBuffer.allocate(colorArray.size());
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
	
	
	/**
	 * Translate all the vertexes by a given amount
	 * @param arg0 amount to translate on X, Y, and Z.
	 */
	public void adjustOrigin(Vector3d arg0) {
		adjust.setTranslation(arg0);
		isDirty=true;
	}
	
	/**
	 * Rotate all the vertexes by a given amount
	 * @param arg0 amount in degrees to rotate around X,Y, and then Z. 
	 */
	public void adjustRotation(Vector3d arg0) {
		// generate the pose matrix
		Matrix3d pose = new Matrix3d();
		Matrix3d rotX = new Matrix3d();
		Matrix3d rotY = new Matrix3d();
		Matrix3d rotZ = new Matrix3d();
		rotX.rotX((float)Math.toRadians(arg0.x));
		rotY.rotY((float)Math.toRadians(arg0.y));
		rotZ.rotZ((float)Math.toRadians(arg0.z));
		pose.set(rotX);
		pose.mul(rotY);
		pose.mul(rotZ);
		adjust.set(pose);
		isDirty=true;
	}
	
	/**
	 * magnifies the current scale
	 * @param arg0
	 */
	public void adjustScale(double arg0) {
		adjust.m00*=arg0;
		adjust.m11*=arg0;
		adjust.m22*=arg0;
		isDirty=true;
	}
	
	public void adjustMatrix(Matrix4d m) {
		adjust.set(m);
		isDirty=true;
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
		Point3d p = new Point3d();
		while(fi.hasNext()) {
			p.x = fi.next().floatValue();
			p.y = fi.next().floatValue();
			p.z = fi.next().floatValue();
			adjust.transform(p);
			
			if(boundBottom.x>p.x) boundBottom.x=p.x;
			if(boundBottom.y>p.y) boundBottom.y=p.y;
			if(boundBottom.z>p.z) boundBottom.z=p.z;
			if(boundTop.x<p.x) boundTop.x=p.x;
			if(boundTop.y<p.y) boundTop.y=p.y;
			if(boundTop.z<p.z) boundTop.z=p.z;
		}
		cuboid.setBounds(boundTop, boundBottom);
	}

	public Cuboid getCuboid() {
		if(isDirty) {
			updateCuboid();
		}
		return cuboid;
	}
	
	public int getNumTriangles() {
		return vertexArray.size()/3;
	}
}
