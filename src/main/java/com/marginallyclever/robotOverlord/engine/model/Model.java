package com.marginallyclever.robotOverlord.engine.model;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.json.JSONObject;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.JSONSerializable;

/**
 * contains the vertex, normal, and texture data for a 3D model.
 * @author dan royer
 *
 */
public class Model implements JSONSerializable {
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
	
	// display correction matrix
	protected Vector3d adjustOrigin;
	protected Vector3d adjustRotation;
	
	// bounding limits
	protected Point3d boundBottom, boundTop;

	public Model() {
		sourceName=null;
		isLoaded=false;
		vertexArray = new ArrayList<Float>();
		normalArray = new ArrayList<Float>();
		colorArray = new ArrayList<Float>();
		texCoordArray = new ArrayList<Float>();
		
		boundTop = new Point3d();
		boundBottom = new Point3d();
		adjustOrigin = new Vector3d();
		adjustRotation = new Vector3d();
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
	
	/**
	 * Regenerate the optimized rendering buffers for the fixed function pipeline.
	 * Also recalculate the bounding box.
	 * @param gl2
	 */
	private void updateBuffers(GL2 gl2) {
		int numVertexes = vertexArray.size()/3;
		Iterator<Float> fi;
		int j=0;

		// generate the pose matrix
		Matrix4d rot = new Matrix4d();
		Matrix4d rotX = new Matrix4d();
		Matrix4d rotY = new Matrix4d();
		Matrix4d rotZ = new Matrix4d();
		rotX.rotX((float)Math.toRadians(adjustRotation.x));
		rotY.rotY((float)Math.toRadians(adjustRotation.y));
		rotZ.rotZ((float)Math.toRadians(adjustRotation.z));
		rot.set(rotX);
		rot.mul(rotY);
		rot.mul(rotZ);
		Matrix4d pose = new Matrix4d(rot);
		pose.setScale(loadScale);
		pose.setTranslation(adjustOrigin);

		boundBottom.set(Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE);
		boundTop.set(-Double.MAX_VALUE,-Double.MAX_VALUE,-Double.MAX_VALUE);
		
		vertices = FloatBuffer.allocate(vertexArray.size());
		fi = vertexArray.iterator();
		Point3d p = new Point3d();
		while(fi.hasNext()) {
			p.x = fi.next().floatValue();
			p.y = fi.next().floatValue();
			p.z = fi.next().floatValue();
			pose.transform(p);
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
			j=0;
		    // repeat for normals
			normals = FloatBuffer.allocate(normalArray.size());
			fi = normalArray.iterator();
			while(fi.hasNext()) {
				p.x = fi.next().floatValue();
				p.y = fi.next().floatValue();
				p.z = fi.next().floatValue();
				rot.transform(p);
				normals.put(j++, (float)p.x);
				normals.put(j++, (float)p.y);
				normals.put(j++, (float)p.z);
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
	 * @param arg0 amount to translate on X, Y, and Z.
	 */
	public void adjustOrigin(Vector3d arg0) {
		if(!adjustOrigin.epsilonEquals(arg0, 0.01f)) {
			adjustOrigin.set(arg0);
			isDirty=true;
		}
	}
	
	public Vector3d getAdjustOrigin() {
		return adjustOrigin;
	}

	/**
	 * Rotate all the vertexes by a given amount
	 * @param arg0 amount in degrees to rotate around X,Y, and then Z. 
	 */
	public void adjustRotation(Vector3d arg0) {
		if(!adjustRotation.epsilonEquals(arg0, 0.01f)) {
			adjustRotation.set(arg0);
			isDirty=true;
		}
	}
	
	public Vector3d getAdjustRotation() {
		return adjustRotation;
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
	
	/**
	 * Force recalculation of the the minimum bounding box to contain this STL file.
	 * Done automatically every time updateBuffers() is called.
	 * Meaningless if there is no vertexArray of points.
	 */
	public void findBounds() {
		if(vertexArray == null) return;

		boundBottom.set(Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE);
		boundTop.set(-Double.MAX_VALUE,-Double.MAX_VALUE,-Double.MAX_VALUE);

		// generate the pose matrix
		Matrix4d rot = new Matrix4d();
		Matrix4d rotX = new Matrix4d();
		Matrix4d rotY = new Matrix4d();
		Matrix4d rotZ = new Matrix4d();
		rot.setIdentity();
		rotX.rotX((float)Math.toRadians(adjustRotation.x));
		rotY.rotY((float)Math.toRadians(adjustRotation.y));
		rotZ.rotZ((float)Math.toRadians(adjustRotation.z));
		rot.set(rotX);
		rot.mul(rotY);
		rot.mul(rotZ);
		Matrix4d pose = new Matrix4d(rot);
		pose.setScale(loadScale);
		pose.setTranslation(adjustOrigin);
		
		// transform and calculate
		Iterator<Float> fi = vertexArray.iterator();
		Point3d p = new Point3d();
		while(fi.hasNext()) {
			p.x = fi.next().floatValue();
			p.y = fi.next().floatValue();
			p.z = fi.next().floatValue();
			pose.transform(p);
			
			if(boundBottom.x>p.x) boundBottom.x=p.x;
			if(boundBottom.y>p.y) boundBottom.y=p.y;
			if(boundBottom.z>p.z) boundBottom.z=p.z;
			if(boundTop.x<p.x) boundTop.x=p.x;
			if(boundTop.y<p.y) boundTop.y=p.y;
			if(boundTop.z<p.z) boundTop.z=p.z;
		}
	}

	
	public Point3d getBoundBottom() {
		return boundBottom;
	}


	public Point3d getBoundTop() {
		return boundTop;
	}


	@Override
	public JSONObject toJSON() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void fromJSON(JSONObject arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}
}
